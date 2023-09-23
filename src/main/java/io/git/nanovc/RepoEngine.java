package io.git.nanovc;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The engine for working with a nano version control repository.
 * <p>
 * The RepoEngine can be thought of as the plumbing commands in git.
 * <p>
 * A Repo Engine does not contain any state. Just the logic of how to manipulate a repo.
 * Therefore you need to pass the repo into all the calls.
 * This is good where one Repo Engine is going to be reused across many Repos.
 * The {@link RepoHandler} can be used to get the higher level porcelain commands.
 * <p>
 * A repo engine is not thread safe.
 * It is designed to be able to process many repo's as long as it is done in its own thread.
 */
public class RepoEngine
{
    /**
     * The name of the HEAD revision.
     */
    public static final String HEAD = "HEAD";

    /**
     * The name of the master branch that is used as the default branch when the repo is initialized.
     */
    public static final String MASTER_BRANCH_NAME = "master";

    /**
     * The message digest used for computing SHA-1 hashes for content.
     */
    public MessageDigest messageDigest;

    /**
     * Creates new Repo Engine.
     */
    public RepoEngine()
    {
        try
        {
            // Create the message digest for hashing:
            messageDigest = MessageDigest.getInstance("SHA1");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create an empty Git repository or reinitialize an existing one.
     * If an existing repo is already associated with this engine then it leaves that one.
     * If there is no repo associated with this engine then it creates a new repo.
     * https://git-scm.com/docs/git-init
     *
     * @param repo The repo to initialize. If this is null then a new repo is created.
     * @return The repo that was initialized. A new one if it didn't exist.
     */
    public Repo init(Repo repo)
    {
        // Check whether we have a repo associated with this engine:
        if (repo == null)
        {
            // We do not have a repo associated with this engine.
            // Create a new repo:
            repo = new Repo();
        }

        // Make sure there is a description:
        if (repo.database.description == null)
        {
            // Provide a default description:
            repo.database.description = "";
        }

        // Create the master branch:
        repo.database.HEAD = new SymbolicReference(MASTER_BRANCH_NAME);

        // When you initialize a new repository with git init,
        // Git populates the hooks directory with a bunch of example scripts,
        // many of which are useful by themselves;
        // but they also document the input values of each script.
        // https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks#_git_hooks
        // TODO: Populate sample scripts

        return repo;
    }

    /**
     * Compute object ID.
     * Computes the object ID value for an object with specified type with the contents of the content byte array,
     * This method does not write the resulting object into the object database.
     * To write the object into the database call {@link #hash_object_write(Repo, RepoObject)}
     * Reports its object ID to its standard output.
     * This is used by git cvsimport to update the index without modifying files in the work tree.
     * When <type> is not specified, it defaults to "blob".
     * <p>
     * The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     * <p>
     * https://git-scm.com/docs/git-hash-object
     *
     * @param type            Specify the type of object to create (default: "blob").
     * @param repoObjectBytes The repo object bytes to hash.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the
     * content
     */
    public Hash hash_object(ObjectType type, byte[] repoObjectBytes)
    {
        // Get a message digest we are using to compute the SHA1 hash:
        MessageDigest digest = this.messageDigest;

        // Reset the message digest so we can start computing the SHA1 hash:
        digest.reset();

        // Write the type of object to the digest:
        digest.update(type.hashBytes);

        // Write a space:
        digest.update((byte) ' ');

        // Check whether we have any content:
        if (repoObjectBytes == null || repoObjectBytes.length == 0)
        {
            // We do not have any content.
            // Hash a zero length array:
            digest.update((byte) '0');

            // Write the null byte:
            digest.update((byte) 0);

            // There is no content so don't do any more.
        }
        else
        {
            // We have content.

            // Get the length of the content:
            String lengthString = Integer.toString(repoObjectBytes.length);

            // Get the bytes for the length:
            byte[] lengthBytes = lengthString.getBytes(StandardCharsets.US_ASCII);

            // Write the length:
            digest.update(lengthBytes);

            // Write the null byte:
            digest.update((byte) 0);

            // Hash the content:
            digest.update(repoObjectBytes);
        }
        // Now we have hashed the header and content if there was some.

        // Get the SHA-1:
        byte[] sha1Bytes = digest.digest();
        // Now we have the hash bytes.

        // Get the hex string for the hash:
        String sha1 = Hex.bytesToHex(sha1Bytes);

        // Create the hash:
        Hash hash = new Hash();

        // Save the results of the hash:
        hash.value = sha1;

        return hash;
    }

    /**
     * Compute object ID and write the object to the database.
     * Computes the object ID value for an object with specified type with the contents of the byte array
     * and actually writes the resulting object into the object database.
     * To not write the object to the database but only compute the hash, call {@link #hash_object(ObjectType, byte[])}
     * Reports its object ID to its standard output.
     * This is used by git cvsimport to update the index without modifying files in the work tree.
     * When <type> is not specified, it defaults to "blob".
     * <p>
     * The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     * <p>
     * https://git-scm.com/docs/git-hash-object
     *
     * @param repo            The repo to write the object into.
     * @param type            Specify the type of object to create (default: "blob"). If this is null then it defaults to BLOB.
     * @param repoObjectBytes The repo object bytes to hash.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     */
    public Hash hash_object_write(Repo repo, ObjectType type, byte[] repoObjectBytes)
    {
        // Create a repo object from the bytes:
        RepoObject repoObject;
        switch (type)
        {
            case BLOB:
                repoObject = new Blob();
                break;
            case COMMIT:
                repoObject = new Commit();
                break;
            case TREE:
                repoObject = new Tree();
                break;
            default:
                return null;
        }
        // Now we have the repo object.

        // Create a stream of the repo object:
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repoObjectBytes);
                DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)
        )
        {
            // Read in the content from the bytes:
            repoObject.readContentFromStream(dataInputStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        // Delegate plumbing to the repo engine:
        return hash_object_write(repo, repoObject);
    }

    /**
     * Compute object ID and write the object to the database.
     * Computes the object ID value for an object with specified type with the contents of the byte array
     * and actually writes the resulting object into the object database.
     * To not write the object to the database but only compute the hash, call {@link #hash_object(ObjectType, byte[])}
     * Reports its object ID to its standard output.
     * This is used by git cvsimport to update the index without modifying files in the work tree.
     * When <type> is not specified, it defaults to "blob".
     * <p>
     * The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     * <p>
     * https://git-scm.com/docs/git-hash-object
     *
     * @param repo       The repo to write to.
     * @param repoObject The repo object to update with the hash and write to the database.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the
     * content
     */
    public Hash hash_object_write(Repo repo, RepoObject repoObject)
    {
        // Get the bytes for the repo object:
        byte[] repoObjectBytes = repoObject.getByteArray();

        // Get the hash for the object:
        Hash hash = hash_object(repoObject.getObjectType(), repoObjectBytes);

        // Update the repo object with the hash:
        repoObject.hash = hash;

        // Save the repo object in the object database:
        repo.database.objects.put(repoObject);

        return hash;
    }

    /**
     * Provide content or type and size information for repository objects.
     * <p>
     * In its first form, the command provides the content or the type of an object in the repository.
     * The type is required unless -t or -p is used to find the object type,
     * or -s is used to find the object size,
     * or --textconv or --filters is used (which imply type "blob").
     * <p>
     * In the second form, a list of objects (separated by linefeeds) is provided on stdin, and the SHA-1, type, and
     * size of each object is printed on stdout.
     * <p>
     * The output format can be overridden using the optional <format> argument.
     * <p>
     * If either --textconv or --filters was specified, the input is expected to list the object names followed by the
     * path name,
     * separated by a single white space,
     * so that the appropriate drivers can be determined.
     * <p>
     * https://git-scm.com/docs/git-cat-file
     *
     * @param repo      The repo to read from.
     * @param hashValue The SHA-1 hash to use to get the repo object.
     * @return The repo object with the given hash.
     */
    public RepoObject cat_file(Repo repo, String hashValue)
    {
        // Get the repo object:
        RepoObject repoObject = repo.database.objects.get(hashValue);

        return repoObject;
    }

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     *
     * @param repo                  The repo to get the log from.
     * @param commitHashOrReference The SHA-1 hash or reference name (branch name) of the commit that we want to log backwards down to the root.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    public Log log(Repo repo, String commitHashOrReference)
    {
        // Check whether this is a valid hash value:
        RepoObject repoObject = repo.database.objects.get(commitHashOrReference);

        // Check if we found a commit:
        if (repoObject != null && repoObject instanceof Commit)
        {
            // This is a commit.
            // Get the log for this commit:
            return log_from_commit_hash(repo, commitHashOrReference);
        }
        else
        {
            // We did not find a commit.
            // Try find a reference with this name:
            return log_from_reference_name(repo, commitHashOrReference);
        }
    }

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     *
     * @param repo            The repo to get the log from.
     * @param commitHashValue The SHA-1 hash of the commit that we want to log backwards down to the root.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    public Log log_from_commit_hash(Repo repo, String commitHashValue)
    {
        // Get the revision list of commits:
        List<Commit> commits = rev_list(repo, commitHashValue);

        // Create a new log:
        Log log = new Log();

        // Map the commits to log entries:
        for (Commit commit : commits)
        {
            // Create a log entry:
            LogEntry logEntry = new LogEntry();

            // Map the data across:
            logEntry.commitHashValue = commit.hash.value;
            logEntry.author = commit.author;
            logEntry.authorTimeStamp = commit.authorTimeStamp;
            logEntry.committer = commit.committer;
            logEntry.committerTimeStamp = commit.committerTimeStamp;
            logEntry.message = commit.message;

            // Save the log entry:
            log.add(logEntry);
        }
        return log;
    }

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     *
     * @param repo          The repo to get the log from.
     * @param referenceName The name of the reference to log from.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    public Log log_from_reference_name(Repo repo, String referenceName)
    {
        // Find the reference with the given name:
        HashReference reference = repo.database.refs.heads.getReference(referenceName);

        // Make sure we found the reference:
        if (reference != null)
        {
            // We found the reference.

            // Return the log:
            return log_from_commit_hash(repo, reference.hash.value);
        }
        else
        {
            // We did not find a reference with the given name.
            throw new NanoRuntimeException("A reference (branch) called '" + referenceName + "' was not found. Make sure to pass in a valid reference name that already exists.");
        }
    }

    /**
     * Directly insert the specified info into the index (staging area).
     * Modifies the index or directory cache.
     * Each file mentioned is updated into the index and any unmerged or needs updating state is cleared.
     * <p>
     * See also git-add(1) for a more user-friendly way to do some of the most common operations on the index.
     * <p>
     * The way git update-index handles files it is told about can be modified using the various options:
     * <p>
     * https://git-scm.com/docs/git-update-index
     *
     * @param repo                   The repo to use.
     * @param hashValue              The SHA-1 hash of the existing content to add to the staging area (index).
     * @param absolutePathForContent The absolute path in the staging area to add the content at.
     * @return The content that was staged from the object with the given hash.
     */
    public MutableContent update_index_add_cacheInfo(Repo repo, String hashValue, String absolutePathForContent)
    {
        // Get the content from the object database:
        RepoObject repoObject = repo.database.objects.get(hashValue);

        // Check whether the repo object is a blob (which would be the usual case):
        byte[] content;
        if (repoObject instanceof Blob)
        {
            // The repo object is a blob.
            Blob blob = (Blob) repoObject;

            // Extract the content from the blob:
            content = blob.content;
        }
        else
        {
            // The object is not a blob.
            // The best we can do is use the bytes as the content:
            content = repoObject.getByteArray();
        }
        // Now we have the content from the repo object.

        // Create the content at the desired path in the staging area:
        MutableContent mutableContent = repo.stagingArea.putContent(absolutePathForContent, content);

        return mutableContent;
    }

    /**
     * Modifies the index (staging area).
     * Each file mentioned is updated into the index (staging area) and any unmerged or needs updating state is cleared.
     * <p>
     * If a specified file isn’t in the index already then it’s added. Default behaviour is to ignore new files.
     * <p>
     * Modifies the index or directory cache.
     * Each file mentioned is updated into the index and any unmerged or needs updating state is cleared.
     * <p>
     * See also git-add(1) for a more user-friendly way to do some of the most common operations on the index.
     * <p>
     * The way git update-index handles files it is told about can be modified using the various options:
     * <p>
     * https://git-scm.com/docs/git-update-index
     *
     * @param repo                               The repo to update.
     * @param absolutePathOfContentInWorkingArea The absolute path of the content in the working area to add to the staging area.
     * @return The content that was staged from the object with the given hash.
     */
    public MutableContent update_index_add(Repo repo, String absolutePathOfContentInWorkingArea)
    {
        // Get the content from the working area:
        MutableContent workingContent = repo.workingArea.getContent(absolutePathOfContentInWorkingArea);

        // Put the content in the staging area:
        MutableContent stagingContent = repo.stagingArea.putContent(absolutePathOfContentInWorkingArea, workingContent.content);

        return stagingContent;
    }


    /**
     * Create a tree object from the current index (staging area).
     * Creates a tree object using the current index.
     * The name of the new tree object is printed to standard output.
     * The root tree object will always be created even if there is no content in the stagingArea
     * <p>
     * The index must be in a fully merged state.
     * <p>
     * Conceptually, git write-tree sync()s the current index contents into a set of tree files.
     * In order to have that match what is actually in your directory right now,
     * you need to have done a git update-index phase before you did the git write-tree.
     *
     * @param repo The repo to write the current staging area as a tree into the object database.
     * @return The root tree object that was created. A tree object is created for each sub folder that is written, but this method only returns the root tree.
     * It is also created even if there is no staged content.
     */
    public Tree write_tree(Repo repo)
    {
        // Create the root of the tree:
        Tree root = new Tree();

        // Keep a map of all the tree objects that we create as we walk the paths:
        // NOTE: The key is the absolute path of the tree.
        Map<String, Tree> pathToTreeMap = new HashMap<>();

        // Keep a map of all the paths of the tree objects that we create as we walk the paths:
        // NOTE: The value is the absolute path of the tree.
        Map<Tree, String> treeToPathMap = new HashMap<>();

        // Keep a map of all the tree entries that we create as we walk the paths:
        // NOTE: The key is the absolute path of the tree entry.
        Map<String, TreeEntry> pathToTreeEntryMap = new HashMap<>();

        // Keep a list of the trees that we create and the order in which we create them:
        // NOTE: We need this so that we can create the tree objects in reverse order so that we have all the hashes.
        List<Tree> treeCreationSequence = new ArrayList<>();

        // Keep a list of all the blobs of content that we create:
        List<Blob> blobCreationSequence = new ArrayList<>();

        // Create a map of blobs to tree entries that need to be updated with their hashes:
        Map<Blob, TreeEntry> blobToTreeEntryMap = new HashMap<>();

        // Index the root note:
        RepoPath rootPath = RepoPath.atRoot();
        pathToTreeMap.put(rootPath.toString(), root);
        treeToPathMap.put(root, rootPath.toString());
        treeCreationSequence.add(root);

        // Build up a full tree from all the content in the staging area:
        for (MutableContent content : repo.stagingArea.contents)
        {
            // Get the path of the content:
            RepoPath path = RepoPath.at(content.getAbsolutePath());

            // Split the path into its parts:
            String[] parts = path.splitIntoParts();

            // Make sure there are parts:
            if (parts != null && parts.length > 0)
            {
                // There are parts of the path to walk.

                // Walk the parts, making sure that the tree structure exists:
                // NOTE: We assume that the last part is the content itself
                // so create the trees up until the second to last part.
                RepoPath currentPath = RepoPath.atRoot();
                RepoPath parentPath = currentPath;
                Tree currentTree = root;
                Tree parentTree = currentTree;
                for (int i = 0; i < parts.length - 1; i++)
                {
                    // Save a reference to our parent:
                    parentPath = currentPath;
                    parentTree = currentTree;

                    // Get the part of the path that we are processing:
                    String part = parts[i];

                    // Get the next path we want to walk to:
                    currentPath = parentPath.resolve(part);
                    String currentPathString = currentPath.toString();

                    // Get the tree we are on:
                    currentTree = pathToTreeMap.get(currentPathString);

                    // Make sure we have a tree:
                    if (currentTree == null)
                    {
                        // We don't have a tree yet.
                        // Create a new tree:
                        currentTree = new Tree();

                        // Save the path to this new tree:
                        pathToTreeMap.put(currentPathString, currentTree);
                        treeToPathMap.put(currentTree, currentPathString);

                        // Save the creation order of this tree:
                        treeCreationSequence.add(currentTree);
                    }
                    // Now we have the current tree.

                    // Check whether we have an entry for this part:
                    TreeEntry parentTreeEntry = pathToTreeEntryMap.get(currentPathString);
                    if (parentTreeEntry == null)
                    {
                        // We do not have a tree entry for this part yet (because we have not walked this folder yet).

                        // Create a new tree entry:
                        parentTreeEntry = new TreeEntry();
                        parentTreeEntry.name = part;
                        parentTreeEntry.objectType = ObjectType.TREE;
                        // NOTE: We will update the hash later once we start saving the trees.

                        // Add the entry to the parent tree:
                        parentTree.entries.add(parentTreeEntry);

                        // Save the path to this new tree entry:
                        pathToTreeEntryMap.put(currentPathString, parentTreeEntry);
                    }
                    // Now we have a tree entry for this part of the path.
                }
                // Now the currentTree points to the parent of the content.

                // Get the name of the content:
                String contentName = parts[parts.length - 1];

                // Update the current path:
                RepoPath contentPath = currentPath.resolve(contentName);

                // Check whether we have an entry for this part:
                TreeEntry contentTreeEntry = pathToTreeEntryMap.get(contentPath.toString());
                //region Content Tree Entry Creation
                if (contentTreeEntry == null)
                {
                    // We do not have a tree entry for this part yet (because we have not walked here yet).
                    // Create a new tree entry:
                    contentTreeEntry = new TreeEntry();
                    contentTreeEntry.name = contentName;
                    contentTreeEntry.objectType = ObjectType.BLOB;
                    // NOTE: We will update the hash later once we start saving the trees.

                    // Add the entry to the current tree:
                    currentTree.entries.add(contentTreeEntry);

                    // Save the path to this new tree entry:
                    pathToTreeEntryMap.put(contentPath.toString(), contentTreeEntry);
                }
                //endregion
                // Now we have a tree entry for this content.

                // Create a blob for the content:
                Blob blob = new Blob(content.content);

                // Save the blob:
                blobCreationSequence.add(blob);

                // Save the tree entry that needs to be updated once we have a hash for the blob:
                blobToTreeEntryMap.put(blob, contentTreeEntry);
            }
        }
        // Now we have worked through every content item in the staging area.

        // Put all the blobs into the object database:
        //region Blob Hashing and Tree Entry Update
        for (Blob blob : blobCreationSequence)
        {
            // Add the blob to the object database:
            Hash blobHash = hash_object_write(repo, blob);

            // Get the tree entry that needs to be updated with this hash:
            TreeEntry treeEntry = blobToTreeEntryMap.get(blob);

            // Update the tree entry hash:
            treeEntry.hashValue = blobHash.value;
        }
        //endregion
        // Now all the blobs have been added and their corresponding tree entries have had their hash values updated.

        // Go through the list of trees that we created in reverse order and start saving them:
        //region Tree Hashing and Tree Entry Update
        for (int i = treeCreationSequence.size() - 1; i >= 0; i--)
        {
            // Get the tree we are on:
            Tree tree = treeCreationSequence.get(i);

            // Add the tree to the object database:
            Hash treeHash = hash_object_write(repo, tree);

            // Get the path of this tree:
            String treePath = treeToPathMap.get(tree);

            // Get the tree entry that needs to be updated with this hash:
            TreeEntry treeEntry = pathToTreeEntryMap.get(treePath);

            // Check whether we have an entry (we won't if it's the root tree):
            if (treeEntry != null)
            {
                // Update the tree entry hash:
                treeEntry.hashValue = treeHash.value;
            }
        }
        //endregion
        // Now all the trees have been saved in reverse order and their corresponding tree entries have had their hash values updated.

        // Return the root tree, which will have the hash that it was committed with:
        return root;
    }

    /**
     * Reads tree information into the index (staging area).
     * Keep the current index contents,
     * and read the contents of the named tree-ish under the directory at <prefix>.
     * The command will refuse to overwrite entries that already existed in the original index file.
     * Note that the <prefix>/ value must end with a slash.
     * <p>
     * This method allows you to provide a prefix path for the tree that is read.
     * This is useful to put the tree at a sub path in the staging area.
     * <p>
     * Reads the tree information given by <tree-ish> into the index,
     * but does not actually update any of the files it "caches".
     * (see: git-checkout-index[1])
     * <p>
     * Optionally, it can merge a tree into the index,
     * perform a fast-forward (i.e. 2-way) merge,
     * or a 3-way merge, with the -m flag.
     * When used with -m, the -u flag causes it to also update the files in the work tree with the result of the merge.
     * <p>
     * Trivial merges are done by git read-tree itself.
     * <p>
     * Only conflicting paths will be in unmerged state when git read-tree returns.
     * <p>
     * https://git-scm.com/docs/git-read-tree
     *
     * @param repo              The repo to update.
     * @param rootTreeHashValue The hash value of the root of the tree to read out from the object database into the staging area.
     * @param prefixPath        The prefix to add to the path. The path should end with the delimiter '/'. If the prefix path is null or empty then it is placed at the root.  @return The entire list of mutable content that was placed in the staging area from this tree.
     */
    public List<MutableContent> read_tree(Repo repo, String rootTreeHashValue, String prefixPath)
    {
        // Create the list of mutable content that we read out into the staging area:
        List<MutableContent> contentList = new ArrayList<>();

        // Get the root tree:
        RepoObject repoObject = repo.database.objects.get(rootTreeHashValue);
        if (repoObject != null && repoObject instanceof Tree)
        {
            // This is a tree.
            Tree tree = (Tree) repoObject;

            // Get the current path that we must start at:
            RepoPath path = prefixPath == null ? RepoPath.atRoot() : RepoPath.at(prefixPath);

            // Recursively walk the tree and add all the entries:
            read_tree_recursively(repo.database.objects, tree, path, contentList);
            // Now we have all the content from this entire tree.

            // Put the content in the staging area:
            contentList.forEach(repo.stagingArea::putContent);
        }
        else
        {
            // This is not a tree.
            throw new NanoRuntimeException("The given rootTreeHashValue was not a valid Tree object. The hash needs to point at an existing Tree in the object database.");
        }

        return contentList;
    }

    /**
     * Reads the content from the given tree at the given path recursively and adds it to the content list.
     *
     * @param objects     The objects to interrogate for content.
     * @param tree        The current tree we are on.
     * @param treePath    The path of the current tree.
     * @param contentList The list to add the content into.
     */
    private void read_tree_recursively(RepoObjectStore objects, Tree tree, RepoPath treePath, List<MutableContent> contentList)
    {
        // Go through all the entries:
        for (TreeEntry entry : tree.entries)
        {
            // Get the new path for this entry:
            RepoPath entryPath = treePath.resolve(entry.name);

            // Check what type of entry it is:
            switch (entry.objectType)
            {
                case BLOB:
                {
                    // Get the blob from the database:
                    Blob blob = (Blob) objects.get(entry.hashValue);

                    // Create content from this blob:
                    MutableContent content = new MutableContent(entryPath.toAbsolutePath().toString(), blob.content);

                    // Save the content:
                    contentList.add(content);
                    break;
                }
                case TREE:
                {
                    // Get the tree from the database:
                    Tree childTree = (Tree) objects.get(entry.hashValue);

                    // Walk the tree recursively:
                    read_tree_recursively(objects, childTree, entryPath, contentList);
                    break;
                }
                default:
                    // Ignore this entry.
            }
        }
    }


    /**
     * Create a new commit object.
     * <p>
     * This is usually not what an end user wants to run directly. See git-commit[1] instead.
     * <p>
     * Creates a new commit object based on the provided tree object and emits the new commit object id on stdout.
     * The log message is read from the standard input, unless -m or -F options are given.
     * <p>
     * A commit object may have any number of parents.
     * With exactly one parent, it is an ordinary commit.
     * Having more than one parent makes the commit a merge between several lines of history.
     * Initial (root) commits have no parents.
     * <p>
     * While a tree represents a particular directory state of a working directory,
     * a commit represents that state in "time", and explains how to get there.
     * <p>
     * Normally a commit would identify a new "HEAD" state,
     * and while Git doesn’t care where you save the note about that state,
     * in practice we tend to just write the result to the file that is pointed at by
     * .git/HEAD,
     * so that we can always see what the last committed state was.
     *
     * @param repo               The repo to add the commit to.
     * @param rootTreeHashValue  The SHA1 hash value of the tree to use as the root of the commit.
     * @param commitMessage      The commit message to use.
     * @param author             The author of the content.
     * @param authorTimestamp    The date, time and time-zone when the author made the commit.
     * @param committer          The person making this commit on behalf of the author.
     * @param committerTimestamp The date, time and time-zone when the committer made the commit.
     * @param commitParentHashes The hashes of the parent commits for this new commit. A commit object may have any number of parents. With exactly one parent, it is an ordinary commit. Having more than one parent makes the commit a merge between several lines of history. Initial (root) commits have no parents.
     * @return The commit object that was created.
     */
    public Commit commit_tree(Repo repo, String rootTreeHashValue, String commitMessage, String author, ZonedDateTime authorTimestamp, String committer, ZonedDateTime committerTimestamp, String... commitParentHashes)
    {
        // Create a new commit:
        Commit commit = new Commit(
                author, authorTimestamp,
                committer, committerTimestamp,
                rootTreeHashValue,
                commitMessage,
                commitParentHashes);

        // Add the commit into the object database:
        hash_object_write(repo, commit);

        return commit;
    }

    /**
     * Convenience method for returning list of all of the commits in the database
     *
     * @param repo The repo to read.
     * @return all commits in the database
     */
    public List<Commit> getCommits(Repo repo)
    {
        return repo.database.objects.map.values().stream()
                .filter(repoObject -> repoObject.getObjectType().equals(ObjectType.COMMIT))
                .map(repoObject -> (Commit) repoObject)
                .collect(Collectors.toList());
    }

    /**
     * Convenience method for returning a stream of all the commits in the database
     *
     * @param repo The repo to read.
     * @return A stream of all the commit objects in the database
     */
    public Stream<Commit> getCommitStream(Repo repo)
    {
        if (repo == null || repo.database == null || repo.database.objects == null)
        {
            // This is an empty repo.
            return Stream.empty();
        }
        else
        {
            return repo.database.objects.map.values().stream()
                    .filter(repoObject -> repoObject.getObjectType().equals(ObjectType.COMMIT))
                    .map(repoObject -> (Commit) repoObject);
        }
    }

    /**
     * Gets the names of the branches in the repo.
     *
     * @param repo The repo to read.
     * @return The names of the branches in the repo.
     */
    public List<String> getBranchNames(Repo repo)
    {
        return repo.database.refs.heads.stream().map(reference -> reference.name).collect(Collectors.toList());
    }

    /**
     * Gets the hash references to commits for each branch in the repo.
     *
     * @param repo The repo to read.
     * @return The branches references in the repo.
     */
    public Map<String, Hash> getBranchCommitReferenceMap(Repo repo)
    {
        return repo.database.refs.heads.stream().collect(Collectors.toMap(o -> o.name, o -> new Hash(o.hash.value)));
    }

    /**
     * Lists commit objects in reverse chronological order.
     * <p>
     * List commits that are reachable by following the parent links from the given commit(s),
     * but exclude commits that are reachable from the one(s) given with a ^ in front of them.
     * <p>
     * The output is given in reverse chronological order by default.
     * <p>
     * You can think of this as a set operation.
     * <p>
     * Commits given on the command line form a set of commits that are reachable from any of them,
     * and then commits reachable from any of the ones given with ^ in front are subtracted from that set.
     * The remaining commits are what comes out in the command’s output.
     * Various other options and paths parameters can be used to further limit the result.
     * <p>
     * Thus, the following command:
     * <p>
     * $ git rev-list foo bar ^baz
     * means "list all the commits which are reachable from foo or bar, but not from baz".
     * <p>
     * A special notation "<commit1>..<commit2>" can be used as a short-hand for "^'<commit1>' <commit2>". For example, either of the following may be used interchangeably:
     * <p>
     * $ git rev-list origin..HEAD
     * $ git rev-list HEAD ^origin
     * Another special notation is "<commit1>…​<commit2>" which is useful for merges. The resulting set of commits is the symmetric difference between the two operands. The following two commands are equivalent:
     * <p>
     * $ git rev-list A B --not $(git merge-base --all A B)
     * $ git rev-list A...B
     * <p>
     * rev-list is a very essential Git command,
     * since it provides the ability to build and traverse commit ancestry graphs.
     * For this reason, it has a lot of different options that enables it to be used by commands as different as git bisect and git repack.
     *
     * @param repo            The repo to query.
     * @param commitHashValue The SHA-1 hash value of the commit that we want to walk backwards down to the root.
     * @return The list of commit objects in reverse chronological order.
     */
    public List<Commit> rev_list(Repo repo, String commitHashValue)
    {
        // Create a set of all commits that we have traversed:
        // NOTE: The SHA1 hash value of the commit is the key.
        Map<String, Commit> commitSet = new IdentityHashMap<>();

        // Create a list for the commits:
        List<Commit> commits = new ArrayList<>();

        // Walk the commits recursively and get all the unique commits down to the roots:
        walk_rev_list_recursively(repo.database.objects, commitHashValue, commitSet, commits, Integer.MAX_VALUE);

        return commits;
    }

    /**
     * Walks the current commit and all of it's parents recursively until it finds the roots.
     *
     * @param objects         The object database to interrogate for commits.
     * @param commitHashValue The SHA-1 hash value of the commit that we want to walk backwards down to the root.
     * @param commitSet       The set of existing commits that have been traversed. The key is the SHA-1 hash value of the commit.
     * @param commitSequence  The sequence of commits that we traverse the commits in. As we discover a new commit, we add it to this list.
     * @param depthLeft       The remaining depth that we are willing to walk. This is needed to limit the depth to which we walk.
     */
    private void walk_rev_list_recursively(RepoObjectStore objects, String commitHashValue, Map<String, Commit> commitSet, List<Commit> commitSequence, int depthLeft)
    {
        // Make sure we have some depth left:
        if (depthLeft < 0) return;
        // Now we know that we still have some remaining depth.

        // Make sure we have a hash:
        if (commitHashValue == null || commitHashValue.isEmpty()) return;
        // Now we know that the have a hash value.

        // Check whether we have already seen this commit:
        if (commitSet.containsKey(commitHashValue)) return;
        // Now we know that we have not seen this commit yet.

        // Get the commit to start walking from:
        RepoObject repoObject = objects.get(commitHashValue);

        // Make sure it's a commit:
        if (repoObject != null && repoObject instanceof Commit)
        {
            // We have a commit.
            Commit commit = (Commit) repoObject;

            // Add this commit to the set:
            commitSet.put(commit.hash.value, commit);

            // Add this commit to the sequence:
            commitSequence.add(commit);

            // Walk any of the commits parents:
            if (commit.parentCommitHashValues != null && commit.parentCommitHashValues.length > 0)
            {
                // We have parents.
                for (String parentCommitHashValue : commit.parentCommitHashValues)
                {
                    // Walk the parent recursively:
                    walk_rev_list_recursively(objects, parentCommitHashValue, commitSet, commitSequence, depthLeft - 1);
                }
            }
        }
        else
        {
            // This is not a commit.
            throw new NanoRuntimeException("The given hash is not a valid commit. A valid commit hash is needed. " + commitHashValue);
        }
    }

    /**
     * Updates or creates a reference with the given name to point at the commit with the given hash.
     * <p>
     * Update the object name stored in a ref safely.
     * <p>
     * https://git-scm.com/docs/git-update-ref
     *
     * @param repo            The repo to update the reference in.
     * @param refsToUpdate    The list of references to update. This is either 'heads' or 'tags' from the database.refs.
     * @param referenceName   The name of the reference (branch) to update or create.
     * @param commitHashValue The SHA-1 hash value of the commit that the reference must point at.
     * @return The reference that was created or updated.
     */
    public HashReference update_ref(Repo repo, HashReferenceCollection refsToUpdate, String referenceName, String commitHashValue)
    {
        // Check whether we already have a reference with this name:
        HashReference reference = refsToUpdate.getReference(referenceName);
        if (reference == null)
        {
            // We do not have this reference yet.
            // Create a new reference:
            reference = new HashReference(referenceName, commitHashValue);

            // Save the reference:
            refsToUpdate.add(reference);
        }
        else
        {
            // We already have this reference.

            // Update the hash:
            reference.hash = new Hash(commitHashValue);
        }
        return reference;
    }

    /**
     * Updates the HEAD symbolic reference to the given reference name.
     * NOTE: At the moment we only support updating HEAD.
     * You cannot define your own symbolic references yet.
     * <p>
     * https://git-scm.com/docs/git-symbolic-ref
     *
     * @param referenceName The name of the reference to update HEAD to.
     * @return The HEAD symbolic reference for the repo.
     */
    public SymbolicReference symbolic_ref(Repo repo, String referenceName)
    {
        // Make sure we have a symbolic reference for HEAD:
        SymbolicReference head = repo.database.HEAD;
        if (head == null)
        {
            // Create a new symbolic reference:
            head = new SymbolicReference();

            // Save the symbolic reference:
            repo.database.HEAD = head;
        }
        // Now we have the symbolic reference.

        // Update it:
        head.referenceName = referenceName;

        return head;
    }

    /**
     * Adds all the changed files in the working area into the staging area.
     * <p>
     * Add file contents to the index
     * https://git-scm.com/docs/git-add
     * -A
     * --all
     * --no-ignore-removal
     * Update the index not only where the working tree has a file matching <pathspec> but also where the index already has an entry.
     * This adds, modifies, and removes index entries to match the working tree.
     * <p>
     * If no <pathspec> is given when -A option is used,
     * all files in the entire working tree are updated (old versions of Git used to limit the update to the current directory and its subdirectories).
     * <p>
     * This command updates the index using the current content found in the working tree,
     * to prepare the content staged for the next commit.
     * It typically adds the current content of existing paths as a whole,
     * but with some options it can also be used to add content with only part of the changes made to the working tree
     * files applied,
     * or remove paths that do not exist in the working tree anymore.
     * <p>
     * The "index" holds a snapshot of the content of the working tree,
     * and it is this snapshot that is taken as the contents of the next commit.
     * Thus after making any changes to the working tree, and before running the commit command,
     * you must use the add command to add any new or modified files to the index.
     * <p>
     * This command can be performed multiple times before a commit.
     * It only adds the content of the specified file(s) at the time the add command is run;
     * if you want subsequent changes included in the next commit, then you must run git add again to add the new
     * content to the index.
     * <p>
     * The git status command can be used to obtain a summary of which files have changes that are staged for the next
     * commit.
     * The git add command will not add ignored files by default. If any ignored files were explicitly specified on the
     * command line, git add will fail with a list of ignored files. Ignored files reached by directory recursion or
     * filename globbing performed by Git (quote your globs before the shell) will be silently ignored. The git add
     * command can be used to add ignored files with the -f (force) option.
     *
     * @param repo            The repository to update.
     * @param createSnapshots True to create a snapshot of the content when adding it to the staging area. False to pass the content by reference, thus minimizing an expensive copy operation.
     */
    public void addAll(Repo repo, boolean createSnapshots)
    {
        // Go through all the content in the working area:
        for (MutableContent workingAreaContent : repo.workingArea.contents)
        {
            byte[] content;

            // Check whether we need to make a snapshot:
            if (createSnapshots)
            {
                // We want to create a snapshot of the content.
                // Clone the contents byte[] so that it is a snapshot at the point where we added it:
                content = workingAreaContent.getCloneOfContentAsByteArray();
            }
            else
            {
                // We do not want to create a snapshot.
                content = workingAreaContent.content;
            }

            // Save the content in the staging area:
            repo.stagingArea.putContent(workingAreaContent.getAbsolutePath(), content);
        }
    }

    /**
     * Puts the given content in the working area of the repo.
     *
     * @param repo         The repo to stage the content in.
     * @param absolutePath The absolute path in the working area where the content will be put.
     * @param content      The content bytes to store at the path.
     * @return The content in the in the working directory that was put.
     */
    public MutableContent putWorkingAreaContent(Repo repo, String absolutePath, byte... content)
    {
        return repo.workingArea.putContent(absolutePath, content);
    }

    /**
     * Gets the content in the working area of the repo.
     *
     * @param repo         The repo to stage the content in.
     * @param absolutePath The absolute path in the working area where we get the content from.
     * @return The content in the working directory at the given absolute path. Null if there is no content at that path.
     */
    public MutableContent getWorkingAreaContent(Repo repo, String absolutePath)
    {
        return repo.workingArea.getContent(absolutePath);
    }

    /**
     * Stages the given content at the given path in the given repo.
     * This is useful if you want to bypass the working area and stage content directly.
     *
     * @param repo         The repo to stage the content in.
     * @param absolutePath The absolute path in the repo to stage the content in.
     * @param content      The content to stage at the given path.
     * @return The content that was staged.
     */
    public MutableContent stage(Repo repo, String absolutePath, byte... content)
    {
        // Add the content to the staging area:
        MutableContent stagedContent = repo.stagingArea.putContent(absolutePath, content);
        return stagedContent;
    }

    /**
     * Record changes to the repository in the current branch.
     * This method gets the parent commit hashes from the current branch.
     * <p>
     * https://git-scm.com/docs/git-commit
     * <p>
     * Stores the current contents of the index in a new commit along with a log message from the user describing the changes.
     *
     * @param repo               The repo to commit into.
     * @param commitMessage      The commit message to use.
     * @param author             The author of the content.
     * @param authorTimestamp    The date, time and time-zone when the author made the commit.
     * @param committer          The person making this commit on behalf of the author.
     * @param committerTimestamp The date, time and time-zone when the committer made the commit.
     * @param createSnapshots    True to create a snapshot of the content when adding it to the staging area. False to pass the content by reference, thus minimizing an expensive copy operation.
     * @return The commit that was created.
     */
    public Commit commitAll(Repo repo, String commitMessage, String author, ZonedDateTime authorTimestamp, String committer, ZonedDateTime committerTimestamp, boolean createSnapshots)
    {
        // Check whether we have a branch already:
        HashReference currentBranch = resolveReference(repo, HEAD);

        // Check whether we have a parent commit:
        Commit parentCommit;

        // Check whether we are already on a branch:
        if (currentBranch != null)
        {
            // We are already on a branch.

            // Check whether we have a parent commit:
            parentCommit = resolveCommit(repo, HEAD);
        }
        else
        {
            // We are not on a branch yet.
            parentCommit = null;
        }

        // Get the array of parent commit hashes:
        String[] parentCommitHashes = parentCommit == null ? new String[0] : new String[]{parentCommit.hash.value};
        // Now we have the parent hash if there is one.

        return commitAll_UseCommitParentHashes(repo, commitMessage, author, authorTimestamp, committer, committerTimestamp, createSnapshots, parentCommitHashes);
    }

    /**
     * Record changes to the repository.
     * <p>
     * https://git-scm.com/docs/git-commit
     * <p>
     * Stores the current contents of the index in a new commit along with a log message from the user describing the changes.
     *
     * @param repo               The repo to commit into.
     * @param commitMessage      The commit message to use.
     * @param author             The author of the content.
     * @param authorTimestamp    The date, time and time-zone when the author made the commit.
     * @param committer          The person making this commit on behalf of the author.
     * @param committerTimestamp The date, time and time-zone when the committer made the commit.
     * @param createSnapshots    True to create a snapshot of the content when adding it to the staging area. False to pass the content by reference, thus minimizing an expensive copy operation.
     * @param commitParentHashes List of the parentCommitHashes
     * @return The commit that was created.
     */
    public Commit commitAll_UseCommitParentHashes(Repo repo, String commitMessage, String author, ZonedDateTime authorTimestamp, String committer, ZonedDateTime committerTimestamp, boolean createSnapshots, String... commitParentHashes)
    {
        // Write all the staging content as a tree:
        Tree rootTree = write_tree(repo);

        // Clear the committed area because we are about to update it:
        repo.committedArea.clear();

        // Create new immutable content from staging area content and add it to the committed area.
        repo.stagingArea.contents.forEach(mutableContent ->
        {
            // Check whether we need to create snapshots of the content:
            if (createSnapshots)
            {
                // We must create snapshots.
                // Clone the content so that anyone with a reference to the original can't modify the immutable content:
                repo.committedArea.putContent(mutableContent.getAbsolutePath(), mutableContent.getCloneOfContentAsByteArray());
            }
            else
            {
                // Just put the content in by reference to avoid a copy.
                // (NOTE: It's possible for someone with the content reference to change it then!)
                repo.committedArea.putContent(mutableContent.getAbsolutePath(), mutableContent.getContent());
            }
        });

        // Freeze the committedArea so that no more data can be written to it unless it is cleared
        repo.committedArea.freeze();

        // Create the commit:
        Commit commit = commit_tree(repo, rootTree.hash.value, commitMessage, author, authorTimestamp, committer, committerTimestamp, commitParentHashes);

        // Check whether we need to update the branch:
        if (repo.database.HEAD != null)
        {
            // We are on a branch already.

            // We want to update the HEAD ref to point to the new commit
            update_ref_in_heads(repo, repo.database.HEAD.referenceName, commit.hash.value);

            // Considerations for branching: if currentBranch is null
            // We are on a branch (or intend on being on a branch).
            // We must create a new branch.
            // Now we have updated the current branch.
        }

        return commit;
    }

    /**
     * Updates or creates a reference in 'heads' with the given name to point at the commit with the given hash.
     * <p>
     * Update the object name stored in a ref safely.
     * <p>
     * https://git-scm.com/docs/git-update-ref
     *
     * @param repo            The repo to update.
     * @param referenceName   The name of the reference (branch) to update or create.
     * @param commitHashValue The SHA-1 hash value of the commit that the reference must point at.
     * @return The reference that was created or updated.
     */
    public HashReference update_ref_in_heads(Repo repo, String referenceName, String commitHashValue)
    {
        return update_ref(repo, repo.database.refs.heads, referenceName, commitHashValue);
    }

    /**
     * Updates or creates a reference in 'tags' with the given name to point at the commit with the given hash.
     * <p>
     * Update the object name stored in a ref safely.
     * <p>
     * https://git-scm.com/docs/git-update-ref
     *
     * @param repo            The repo to update.
     * @param referenceName   The name of the reference (tag) to update or create.
     * @param commitHashValue The SHA-1 hash value of the commit that the reference must point at.
     * @return The reference that was created or updated.
     */
    public HashReference update_ref_in_tags(Repo repo, String referenceName, String commitHashValue)
    {
        return update_ref(repo, repo.database.refs.tags, referenceName, commitHashValue);
    }

    /**
     * Gets the commit with the given hash or reference (branch) name.
     * You can pass "HEAD" to get the currently checked out commit.
     *
     * @param repo                        The repo to look in.
     * @param commitHashOrReferenceOrHEAD The SHA-1 hash or reference name (branch name) of the commit that we want to get. Use "HEAD" to get the currently checked out commit.
     * @return The commit that is referenced. Null if it cannot be found.
     */
    public Commit resolveCommit(Repo repo, String commitHashOrReferenceOrHEAD)
    {
        // Make sure we have input:
        if (commitHashOrReferenceOrHEAD == null || commitHashOrReferenceOrHEAD.isEmpty()) return null;

        // Check whether it's the HEAD revision:
        if (HEAD.equals(commitHashOrReferenceOrHEAD))
        {
            // This is the HEAD revision.

            // Check whether we have a HEAD revision yet:
            if (repo.database.HEAD == null)
            {
                // We do not have a HEAD revision yet because we haven't checked anything out yet.
                return null;
            }
            else
            {
                // We have a HEAD revision.

                // Make sure we are not about to get into an infinite loop:
                if (HEAD.equals(repo.database.HEAD)) return null;

                // Call this method recursively:
                return resolveCommit(repo, repo.database.HEAD.referenceName);
            }
        }
        else
        {
            // It's not the HEAD reference.
            // Check whether this is a valid hash value:
            RepoObject repoObject = repo.database.objects.get(commitHashOrReferenceOrHEAD);

            // Check if we found a commit:
            if (repoObject != null && repoObject instanceof Commit)
            {
                // This is a commit.
                return (Commit) repoObject;
            }
            else
            {
                // We did not find a commit.
                // Try find a reference with this name:

                // Find the reference with the given name:
                HashReference reference = repo.database.refs.heads.getReference(commitHashOrReferenceOrHEAD);

                // Make sure we found the reference:
                if (reference != null)
                {
                    // We found the reference.

                    // Get the commit for that reference:
                    RepoObject refenceObject = repo.database.objects.get(reference.hash);

                    // Check if we found a commit:
                    if (refenceObject != null && refenceObject instanceof Commit)
                    {
                        // This is a commit.
                        return (Commit) refenceObject;
                    }
                }
                // We did not find a reference with the given name.
                throw new NanoRuntimeException("A reference (branch) called '" + commitHashOrReferenceOrHEAD + "' was not found. Make sure to pass in a valid reference name that already exists, a SHA1 hash of a commit or HEAD for the current checkout.");
            }
        }
    }

    /**
     * Gets the reference with the given reference (branch) name.
     * You can pass "HEAD" to get the currently checked out commit.
     *
     * @param repo            The repo to look in.
     * @param referenceOrHEAD The reference name (branch name) to get. Use "HEAD" to get the currently checked out branch.
     * @return The resolved reference. Null if it cannot be found.
     */
    public HashReference resolveReference(Repo repo, String referenceOrHEAD)
    {
        // Make sure we have input:
        if (referenceOrHEAD == null || referenceOrHEAD.isEmpty()) return null;

        // Check whether it's the HEAD revision:
        if (HEAD.equals(referenceOrHEAD))
        {
            // This is the HEAD revision.

            // Check whether we have a HEAD revision yet:
            if (repo.database.HEAD == null)
            {
                // We do not have a HEAD revision yet because we haven't checked anything out yet.
                return null;
            }
            else
            {
                // We have a HEAD revision.

                // Make sure we are not about to get into an infinite loop:
                if (HEAD.equals(repo.database.HEAD)) return null;

                // Call this method recursively:
                return resolveReference(repo, repo.database.HEAD.referenceName);
            }
        }
        else
        {
            // It's not the HEAD reference.

            // Find the reference with the given name:
            HashReference reference = repo.database.refs.heads.getReference(referenceOrHEAD);

            return reference;
        }
    }

    /**
     * Show the working tree status
     * https://git-scm.com/docs/git-status
     * <p>
     * Displays paths that have differences between the index file (staging area) and the current HEAD commit,
     * paths that have differences between the working tree and the index file (staging area),
     * and paths in the working tree that are not tracked by Git (and are not ignored by gitignore[5]). The first are
     * what you would commit by running git commit; the second and third are what you could commit by running git add
     * before running git commit.
     *
     * @param repo The repository to get the status of.
     */
    public Status status(Repo repo)
    {
        // Create the status output:
        Status status = new Status();
        return status;
    }

    /**
     * Clears the three areas for the passed repo
     *
     * @param repo The repo for which we want to clear each of the working, staging and committed Areas
     */
    public void clearAreas(Repo repo)
    {
        repo.workingArea.clear();
        repo.stagingArea.clear();
        repo.committedArea.clear();
    }

    /**
     * Updates files in the working tree to match the version in the index or the specified tree.
     * If no paths are given, git checkout will also update HEAD to set the specified branch as the current branch.
     * <p>
     * https://git-scm.com/docs/git-checkout
     * https://git-scm.com/docs/git-checkout#git-checkout-emgitcheckoutemltbranchgt
     * <p>
     * To prepare for working on <branch>,
     * switch to it by updating the index and the files in the working tree,
     * and by pointing HEAD at the branch.
     * <p>
     * Local modifications to the files in the working tree are kept,
     * so that they can be committed to the <branch>.
     * <p>
     * If <branch> is not found but there does exist a tracking branch in exactly one remote
     * (call it <remote>) with a matching name, treat as equivalent to
     * $ git checkout -b <branch> --track <remote>/<branch>
     * <p>
     * You could omit <branch>, in which case the command degenerates to
     * "check out the current branch",
     * which is a glorified no-op with a rather expensive side-effects to show only the tracking information,
     * if exists, for the current branch.
     *
     * @param repo                        The repo to checkout.
     * @param commitHashOrReferenceOrHEAD The SHA-1 hash or reference name (branch name) of the commit that we want to get. Use "HEAD" to get the currently checked out commit.
     * @param revisionOffset              The offset from the commit pointed to by the branch name. 0 means the last commit for the branch. -1 means the commit before. +1 means the commit after but this is not supported (it will return nothing when looking for commits).
     */
    public void checkout(Repo repo, String commitHashOrReferenceOrHEAD, int revisionOffset)
    {
        // Get the commit that we are interested in:
        Commit startingCommit = resolveCommit(repo, commitHashOrReferenceOrHEAD);

        // Create a set of all commits that we have traversed:
        // NOTE: The SHA1 hash value of the commit is the key.
        Map<String, Commit> commitSet = new IdentityHashMap<>();

        // Create a list for the commits:
        List<Commit> commits = new ArrayList<>();

        // Walk the commits recursively and get all the unique commits down to the roots:
        walk_rev_list_recursively(repo.database.objects, startingCommit.hash.value, commitSet, commits, -revisionOffset);

        // Make sure we found the commits:
        if (commits.size() > 0)
        {
            // Get the last commit in the chain (that is the one we want to checkout):
            Commit commit = commits.get(commits.size() - 1);

            // Clear the content areas:
            clearAreas(repo);

            // Recursively walk the tree and restore the contents:
            walk_and_checkout_tree_recursively(repo, commit.treeHashValue, RepoPath.atRoot());

            // Freeze the committed are:
            repo.committedArea.freeze();

            // Check whether an explicit checkout other than HEAD was requested:
            if (!commitHashOrReferenceOrHEAD.equals(HEAD))
            {
                // We are switching branches.
                // Update HEAD to point at this checkout:
                if (repo.database.HEAD == null)
                {
                    // We have not checked out yet.
                    repo.database.HEAD = new SymbolicReference(commitHashOrReferenceOrHEAD);
                }
                else
                {
                    // Update the reference:
                    repo.database.HEAD.referenceName = commitHashOrReferenceOrHEAD;
                }
            }
        }
        else
        {
            // We didn't find any commits.
            throw new NanoRuntimeException("We did not find any commits for " + commitHashOrReferenceOrHEAD);
        }
    }

    /**
     * Walks the current tree and checks out the tree recursively.
     * It is assumed that the repo content areas have been cleared already.
     *
     * @param repo          The repo to walk.
     * @param treeHashValue The hash of the tree that we must process, including its children recursively.
     * @param currenPath    The current path that we are on.
     */
    private void walk_and_checkout_tree_recursively(Repo repo, String treeHashValue, RepoPath currenPath)
    {
        // Get the commit to start walking from:
        RepoObject repoObject = cat_file(repo, treeHashValue);

        // Make sure it's a commit:
        if (repoObject != null && repoObject instanceof Tree)
        {
            // We have a tree.
            Tree tree = (Tree) repoObject;

            // Process all of the tree entries:
            for (TreeEntry entry : tree.entries)
            {
                // Get the path for this entry:
                RepoPath entryPath = currenPath.resolve(entry.name);

                // Process the entry:
                String contentPath = entryPath.toAbsolutePath().toString();

                // Process the entry:
                switch (entry.objectType)
                {
                    case BLOB:
                        // This is a blob of content.
                        // Get the blob of content:
                        Blob blob = (Blob) cat_file(repo, entry.hashValue);

                        // Create the content:
                        repo.committedArea.putContent(contentPath, blob.content);
                        repo.stagingArea.putContent(contentPath, blob.content);
                        repo.workingArea.putContent(contentPath, blob.content);

                        break;

                    case TREE:
                        // This is child tree.

                        // Walk the child recursively:
                        walk_and_checkout_tree_recursively(repo, entry.hashValue, entryPath);
                        break;

                    default:
                        throw new NanoRuntimeException("Unexpected content was found with the tree entry " + entry.objectType.name());
                }
            }
        }
        else
        {
            // This is not a commit.
            throw new NanoRuntimeException("The given hash is not a valid tree. A valid tree hash is needed. " + treeHashValue);
        }
    }

    /**
     * Restores the modified or deleted path to its original contents from the index.
     * <p>
     * Updates files in the working tree to match the version in the index or the specified tree.
     * If no paths are given, git checkout will also update HEAD to set the specified branch as the current branch.
     * <p>
     * https://git-scm.com/docs/git-checkout
     * https://git-scm.com/docs/git-checkout#git-checkout-emgitcheckoutem-p--patchlttree-ishgt--ltpathspecgt82308203
     * <p>
     * When <paths> or --patch are given, git checkout does not switch branches.
     * It updates the named paths in the working tree from the index file or from a named <tree-ish> (most often a commit).
     * In this case, the -b and --track options are meaningless and giving either of them results in an error.
     * <p>
     * The <tree-ish> argument can be used to specify a specific tree-ish (i.e. commit, tag or tree)
     * to update the index for the given paths before updating the working tree.
     * <p>
     * git checkout with <paths> or --patch is used to restore modified or deleted paths to their original contents from the index
     * or replace paths with the contents from a named <tree-ish> (most often a commit-ish).
     * <p>
     * The index may contain unmerged entries because of a previous failed merge.
     * By default, if you try to check out such an entry from the index,
     * the checkout operation will fail and nothing will be checked out.
     * Using -f will ignore these unmerged entries.
     * <p>
     * The contents from a specific side of the merge can be checked out of the index by using --ours or --theirs.
     * <p>
     * With -m, changes made to the working tree file can be discarded to re-create the original conflicted merge result.
     *
     * @param repo The repo to checkout.
     * @param path The path of the content to restore.
     */
    public void checkout_path(Repo repo, RepoPath path)
    {
        // Get the content from the committed area:
        ImmutableContent content = repo.committedArea.getContent(path);

        // Check whether we have content there:
        if (content != null)
        {
            // We have content.

            // Put this content in the working area:
            putWorkingAreaContent(repo, content.getAbsolutePath(), content.getCloneOfContentAsByteArray());
        }
    }

    /**
     * Restores the modified or deleted path to its original contents from the index.
     * <p>
     * Updates files in the working tree to match the version in the index or the specified tree.
     * If no paths are given, git checkout will also update HEAD to set the specified branch as the current branch.
     * <p>
     * https://git-scm.com/docs/git-checkout
     * https://git-scm.com/docs/git-checkout#git-checkout-emgitcheckoutem-p--patchlttree-ishgt--ltpathspecgt82308203
     * <p>
     * When <paths> or --patch are given, git checkout does not switch branches.
     * It updates the named paths in the working tree from the index file or from a named <tree-ish> (most often a commit).
     * In this case, the -b and --track options are meaningless and giving either of them results in an error.
     * <p>
     * The <tree-ish> argument can be used to specify a specific tree-ish (i.e. commit, tag or tree)
     * to update the index for the given paths before updating the working tree.
     * <p>
     * git checkout with <paths> or --patch is used to restore modified or deleted paths to their original contents from the index
     * or replace paths with the contents from a named <tree-ish> (most often a commit-ish).
     * <p>
     * The index may contain unmerged entries because of a previous failed merge.
     * By default, if you try to check out such an entry from the index,
     * the checkout operation will fail and nothing will be checked out.
     * Using -f will ignore these unmerged entries.
     * <p>
     * The contents from a specific side of the merge can be checked out of the index by using --ours or --theirs.
     * <p>
     * With -m, changes made to the working tree file can be discarded to re-create the original conflicted merge result.
     *
     * @param repo    The repo to checkout.
     * @param pattern The pattern of paths to restore.
     */
    public void checkout_pattern(Repo repo, RepoPattern pattern)
    {
        // Find all the matches from the committed area:
        pattern.matchStream(repo.committedArea.contentStream())
                .forEach(t -> checkout_path(repo, RepoPath.at(t.getAbsolutePath())));
    }

    /**
     * Creates a new branch with the given name.
     * If the branch already exists then the branch is updated to the current commit.
     * <p>
     * https://git-scm.com/docs/git-branch
     * <p>
     * The command’s second form creates a new branch head named <branchname> which points to the current HEAD, or <start-point> if given.
     * Note that this will create the new branch, but it will not switch the working tree to it; use "git checkout <newbranch>" to switch to the new branch.
     *
     * @param repo       The repo to add the branch to.
     * @param branchName The name of the branch to create.
     */
    public void branch(Repo repo, String branchName)
    {
        // Get the current branch:
        HashReference currentBranch = resolveReference(repo, HEAD);

        // Check whether we found the branch reference:
        if (currentBranch != null)
        {
            // Create a branch at the current commit:
            update_ref_in_heads(repo, branchName, currentBranch.hash.value);
        }
    }

    /**
     * Deletes the branches with the given names.
     * If the branches with the names don't exist then nothing happens.
     * <p>
     * https://git-scm.com/docs/git-branch
     * <p>
     * With a -d or -D option, <branchname> will be deleted.
     * You may specify more than one branch for deletion.
     * If the branch currently has a reflog then the reflog will also be deleted.
     *
     * @param repo        The repo to delete the branches from.
     * @param branchNames The names of the branches to delete.
     */
    public void branch_delete(Repo repo, String... branchNames)
    {
        for (String branchName : branchNames)
        {
            // Delete the branch:
            repo.database.refs.heads.removeReference(branchName);
        }
    }

    /**
     * Gets the name of the currently checked out branch.
     *
     * @param repo The repo to inspect.
     * @return The name of the branch that we are currently on.
     */
    public String getCurrentBranchName(Repo repo)
    {
        return repo.database.HEAD == null ? null : repo.database.HEAD.referenceName;
    }



    /**
     * Returns a string that can hopefully aid in the debugging or analysis of the repo and it's structures.
     * The string will be a similar to a tree, ordered by commit timestamps and all of the data for each of the commits.
     *
     * Commit 1 - 20170101 [This is a commit message]
     *     |- / Tree -> 04aca
     *        |- Blob -> h2cm
     *        |- record Tree -> zc40
     *           |- Blob -> d5aj
     *  ...
     *  Commit 2 - 20170102 [This is another commit message]
     *  / Tree -> 04aca
     *      |---- Blob -> h2cm
     *  ..
     *
     * @param repo The repo for which we want the debug string
     * @return A formatted string representing the repo structure.
     */
    public String getDebugString(Repo repo) {
        HashMap<String, RepoObject> objectsMap = repo.database.objects.map;
        List<Commit> sortedCommits = objectsMap.values().stream()
                .filter(obj -> obj.getObjectType().equals(ObjectType.COMMIT))
                .map(obj -> (Commit) obj)
                .sorted(new CommitTimestampComparator())
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        List<TreeEntry> treeEntries = new ArrayList<>();
        sortedCommits.forEach(commit -> {
            // We want to first print out the commit to detail which commit the tree belongs to
            sb.append(commit);
            String lineSeparator = System.lineSeparator();
            sb.append(lineSeparator);

            // We have a multimap which will contain all of the trees at a certain indentation level
            Map<Integer, List<Tree>> indentationTreeMap = new HashMap<>();
            Tree rootTree = (Tree) objectsMap.get(commit.treeHashValue);

            // Add the root tree at level 0
            List<Tree> rootTrees = indentationTreeMap.computeIfAbsent(0, integer -> new ArrayList<>());
            rootTrees.add(rootTree);

            // We now want to build the tree
            // This will leave us will all the trees at their respective levels
            buildTreeMap(indentationTreeMap, rootTree, repo, 0);

            // Get all the tree entries so we can give the trees context (their path)
            indentationTreeMap.values().stream().flatMap(trees -> trees.stream()).forEach(tree -> {
                treeEntries.addAll(tree.entries);
            });

            // For each of the trees in the map, print them out with respective markers
            for (int i = 0; i < indentationTreeMap.size(); i++) {
                Collection<Tree> trees = indentationTreeMap.get(i);
                int finalI = i;
                String joinMarker = String.join("", Collections.nCopies(finalI, emptyMarker)).concat(blobMarker);
                String embedMarker = String.join("", Collections.nCopies(finalI + 1, emptyMarker)).concat(blobMarker);
                trees.forEach(tree -> {
                    TreeEntry treeEntry = treeEntries.stream()
                            .filter(entry -> entry.hashValue.equals(tree.hash.value)).findFirst().orElse(new TreeEntry(ObjectType.TREE, tree.hash.value, "/"));
                    sb.append(joinMarker + treeEntry.name + " TREE -> " + tree.hash.value);
                    sb.append(System.lineSeparator());
                    tree.entries.stream().filter(entry -> entry.objectType.equals(ObjectType.BLOB)).forEach(entry -> {
                        sb.append(embedMarker + entry.name + ": " + entry.objectType + " -> " + entry.hashValue);
                        sb.append(System.lineSeparator());
                    });
                });
            }

            sb.append(lineSeparator);
        });
        return sb.toString();
    }

    private void buildTreeMap(Map<Integer, List<Tree>> indentationTreeMap, Tree tree, Repo repo, int indentation) {
        // Use an atomic integer for indentation for streaming purposes
        AtomicInteger integer = new AtomicInteger(indentation);
        integer.incrementAndGet();
        tree.entries.stream().filter(entry -> entry.objectType.equals(ObjectType.TREE)).forEach(entry -> {
            Tree innerTree = (Tree) repo.database.objects.map.get(entry.hashValue);
            List<Tree> trees = indentationTreeMap.computeIfAbsent(integer.get(), integer1 -> new ArrayList<>());
            trees.add( innerTree);
            buildTreeMap(indentationTreeMap, innerTree, repo, integer.get());
        });
    }

    String emptyMarker = "    ";
    String blobMarker  = " |- ";

}
