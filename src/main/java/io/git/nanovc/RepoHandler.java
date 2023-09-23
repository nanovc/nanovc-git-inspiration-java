package io.git.nanovc;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The manager for achieving Nano Version Control.
 * <p>
 * Nano Version Control is inspired by Git.
 * The idea is that each entity has an entire Git repo structure for the history.
 * The benefit of nano version control at the entity level is that each entity can be independently versioned
 * in it's entirety in memory.
 * No disk operations are required and there is no dependency between any sibling entities.
 * <p>
 * See: https://git-scm.com/book/en/v2/Git-Internals-Git-Objects
 * <p>
 * The RepoManager can be thought of as giving us access to porcelain commands in git.
 * It also gives us access to the plumbing commands by delegating them to the ({@link RepoEngine}).
 * It makes use of the lower level plumbing commands ({@link RepoEngine}) to achieve higher-level objectives ({@link
 * NanoVersionControl}).
 * <p>
 * This class can also be thought of as the Git Command Line Interface (CLI).
 * <p>
 * Create one Repo Manager for each Repo that you want to manage.
 * Alternatively, make sure that one RepoManager is used per thread.
 * You can reuse the RepoManager instance by swapping out the repo,
 * but keep in mind that the repo is the implied state that is used for running commands on the Manager.
 * <p>
 * The RepoManager is stateful, meaning that it has a reference to one and only one Repo at a time.
 * The core functionality is delegated to the {@link RepoEngine} which is stateless and can be reused for multiple
 * repos.
 */
public class RepoHandler implements NanoCommands, PorcelainCommands, PlumbingCommands
{

    /**
     * The repo that is being managed.
     * One Repo Manager is needed for each Repo that is being managed.
     * This represents the common repo state that is implied for all the commands on the RepoManager.
     */
    public Repo repo;

    /**
     * The engine used for managing the Repo at a low level.
     * This provides the low level plumbing commands in order to manipulate the
     */
    public RepoEngine engine;


    /**
     * The author that is used when creating new commits.
     */
    private String author;

    /**
     * The committer that is used when creating new commits.
     */
    private String committer;

    /**
     * The override for time for commits.
     * This is useful to set in testing situations.
     * If this is null then the actual time when the commit is performed is used instead.
     */
    private ZonedDateTime nowOverride;


    /**
     * Creates a new manager for Nano Version Control around the given Repo and RepoEngine.
     *
     * @param repo       The repo to manage.
     * @param repoEngine The repo engine to use internally.
     */
    public RepoHandler(Repo repo, RepoEngine repoEngine)
    {
        this.repo = repo;
        this.engine = repoEngine;
    }

    /**
     * Creates a new manager for Nano Version Control around the given Repo.
     * It creates a new RepoEngine automatically.
     *
     * @param repo The repo to manage.
     */
    public RepoHandler(Repo repo)
    {
        this(repo, new RepoEngine());
    }

    /**
     * Creates a new manager for Nano Version Control around the given Repo and RepoEngine.
     * No repo is created.
     * Call {@link #init()} to create a repo for this manager.
     */
    public RepoHandler()
    {
    }


    /**
     * Sets the author that is used when creating new commits.
     *
     * @param author The author to use for new commits.
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * Gets the author that is used when creating new commits.
     *
     * @return The author to use for new commits.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Gets the committer that is used when creating commits.
     *
     * @return The committer to use for new commits.
     */
    public String getCommitter()
    {
        return committer;
    }

    /**
     * Sets the committer that is used when creating new commits.
     *
     * @param committer The committer to use for new commits.
     */
    public void setCommitter(String committer)
    {
        this.committer = committer;
    }

    /**
     * Sets the author and committer that are used when creating new commits.
     *
     * @param authorAndCommitter The name to use for the author and committer for new commits.
     */
    public void setAuthorAndCommitter(String authorAndCommitter)
    {
        this.author = authorAndCommitter;
        this.committer = authorAndCommitter;
    }

    /**
     * Gets the override for time for commits.
     * This is useful to set in testing situations.
     * If this is null then the actual time when the commit is performed is used instead.
     *
     * @return The fixed timestamp to use for all new commits. If this is null then the actual commit time is used.
     */
    public ZonedDateTime getNowOverride()
    {
        return nowOverride;
    }

    /**
     * Sets the override for time for commits.
     * This is useful to set in testing situations.
     * If this is null then the actual time when the commit is performed is used instead.
     *
     * @param nowOverride The fixed timestamp to use for all new commits. If this is null then the actual commit time is used.
     */
    public void setNowOverride(ZonedDateTime nowOverride)
    {
        this.nowOverride = nowOverride;
    }

    /**
     * Create an empty Git repository or reinitialize an existing one.
     * If an existing repo is already associated with this engine then it leaves that one.
     * If there is no repo associated with this engine then it creates a new repo.
     * https://git-scm.com/docs/git-init
     */
    @Override
    public Repo init()
    {
        // Make sure we have a repo engine:
        if (this.engine == null)
        {
            this.engine = new RepoEngine();
        }
        this.repo = engine.init(repo);
        return repo;
    }

    /**
     * Stages the given content at the given path in the given repo.
     * This is useful if you want to bypass the working area and stage content directly.
     *
     * @param absolutePath The absolute path in the repo to stage the content in.
     * @param content      The content to stage at the given path.
     * @return The content that was staged.
     */
    @Override
    public MutableContent stage(String absolutePath, byte... content)
    {
        // Delegate plumbing to the repo engine:
        return this.engine.stage(this.repo, absolutePath, content);
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
     * @param createSnapshots True to create a snapshot of the content when adding it to the staging area. False to pass the content by reference, thus minimizing an expensive copy operation.
     */
    public void addAll(boolean createSnapshots)
    {
        // Delegate plumbing to the repo engine:
        this.engine.addAll(this.repo, createSnapshots);
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
        return engine.status(repo);
    }

    /**
     * Compute object ID.
     * Computes the object ID value for an object with specified type with the contents of the content byte array,
     * This method does not write the resulting object into the object database.
     * To write the object into the database call {@link #hash_object_write(ObjectType, byte[])}
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
     * content.  A new hash instance is created each time this is called.
     */
    @Override
    public Hash hash_object(ObjectType type, byte[] repoObjectBytes)
    {
        return this.engine.hash_object(type, repoObjectBytes);
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
     * @param type            Specify the type of object to create (default: "blob"). If this is null then it defaults to BLOB.
     * @param repoObjectBytes The repo object bytes to hash.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the
     * content
     */
    @Override
    public Hash hash_object_write(ObjectType type, byte[] repoObjectBytes)
    {
        return engine.hash_object_write(repo, type, repoObjectBytes);
    }

    /**
     * Compute object ID and write the object to the database as a blob.
     * Computes the object ID value for an object with specified type with the contents of the byte array
     * and actually writes the resulting object into the object database.
     * To not write the object to the database but only compute the hash, call {@link #hash_object(ObjectType, byte[])}
     * Reports its object ID to its standard output.
     * This is used by git cvsimport to update the index without modifying files in the work tree.
     * <p>
     * The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     * <p>
     * https://git-scm.com/docs/git-hash-object
     *
     * @param content The content to hash.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the
     * content
     */
    @Override
    public Hash hash_object_write_blob(byte... content)
    {
        // Create the blob:
        Blob blob = new Blob(content);

        // Delegate plumbing to the repo engine:
        return this.engine.hash_object_write(this.repo, blob);
    }

    /**
     * Compute object ID and write the string to the database as a UTF-8 blob.
     * Computes the object ID value for an object with specified type with the contents of the byte array
     * and actually writes the resulting object into the object database.
     * To not write the object to the database but only compute the hash, call {@link #hash_object(ObjectType, byte[])}
     * Reports its object ID to its standard output.
     * This is used by git cvsimport to update the index without modifying files in the work tree.
     * <p>
     * The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     * <p>
     * https://git-scm.com/docs/git-hash-object
     *
     * @param string The string content to write. The string is stored as UTF-8.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the
     * content
     */
    @Override
    public Hash hash_object_write_string(String string)
    {
        // Get the UTF8 bytes:
        byte[] bytes = string == null ? new byte[0] : string.getBytes(StandardCharsets.UTF_8);

        // Write the blob:
        return hash_object_write_blob(bytes);
    }

    /**
     * Puts the given content in the working area of the repo.
     *
     * @param absolutePath The absolute path in the working area where the content will be put.
     * @param content      The content bytes to store at the path.
     * @return The content in the in the area directory that was put.
     */
    @Override
    public MutableContent putWorkingAreaContent(String absolutePath, byte... content)
    {
        return engine.putWorkingAreaContent(repo, absolutePath, content);
    }

    /**
     * Gets the content in the working area of the repo.
     *
     * @param absolutePath The absolute path in the working area where we get the content from.
     * @return The content in the working area at the given absolute path. Null if there is no content at that path.
     */
    @Override
    public MutableContent getWorkingAreaContent(String absolutePath)
    {
        return engine.getWorkingAreaContent(repo, absolutePath);
    }

    /**
     * Returns this repo manager but only exposes the porcelain commands.
     *
     * @return The repo manager exposed with Porcelain Commands only.
     */
    public PorcelainCommands asPorcelainCommands()
    {
        return this;
    }

    /**
     * Returns this repo manager but only exposes the plumbing commands.
     *
     * @return The repo manager exposed with Plumbing Commands only.
     */
    public PlumbingCommands asPlumbingCommands()
    {
        return this;
    }

    /**
     * Returns this repo manager but only exposes the nano commands.
     *
     * @return The repo manager exposed with Nano Commands only.
     */
    public NanoCommands asNanoCommands()
    {
        return this;
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
     * @param hashValue The SHA-1 hash to use to get the repo object.
     * @return The repo object with the given hash.
     */
    @Override
    public RepoObject cat_file(String hashValue)
    {
        // Delegate plumbing to the repo engine:
        return this.engine.cat_file(this.repo, hashValue);
    }

    /**
     * Provide type information for repository objects.
     * <p>
     * In its first form, the command provides the type of an object in the repository.
     * <p>
     * https://git-scm.com/docs/git-cat-file
     *
     * @param hashValue The SHA-1 hash to use to get the repo object type.
     * @return The type of object stored with the given hash.
     */
    @Override
    public ObjectType cat_file_object_type(String hashValue)
    {
        // Get the repo object:
        RepoObject repoObject = cat_file(hashValue);

        return repoObject == null ? null : repoObject.getObjectType();
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
     * @param hashValue              The SHA-1 hash of the existing content to add to the staging area (index).
     * @param absolutePathForContent The absolute path in the staging area to add the content at.
     * @return The content that was staged from the object with the given hash.
     */
    @Override
    public MutableContent update_index_add_cacheInfo(String hashValue, String absolutePathForContent)
    {
        // Delegate plumbing to the repo engine:
        return this.engine.update_index_add_cacheInfo(this.repo, hashValue, absolutePathForContent);
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
     * @param absolutePathOfContentInWorkingArea The absolute path of the content in the working area to add to the staging area.
     * @return The content that was staged from the object with the given hash.
     */
    @Override
    public MutableContent update_index_add(String absolutePathOfContentInWorkingArea)
    {
        // Delegate plumbing to the repo engine:
        return this.engine.update_index_add(this.repo, absolutePathOfContentInWorkingArea);
    }

    /**
     * Create a tree object from the current index (staging area).
     * Creates a tree object using the current index.
     * The name of the new tree object is printed to standard output.
     * <p>
     * The index must be in a fully merged state.
     * <p>
     * Conceptually, git write-tree sync()s the current index contents into a set of tree files.
     * In order to have that match what is actually in your directory right now,
     * you need to have done a git update-index phase before you did the git write-tree.
     *
     * @return The root tree object that was created. A tree object is created for each sub folder that is written, but this method only returns the root tree.
     */
    @Override
    public Tree write_tree()
    {
        // Delegate plumbing to the repo engine:
        return this.engine.write_tree(this.repo);
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
     * @param rootTreeHashValue The SHA1 hash value of the root of the tree to read out from the object database into the staging area.
     * @param prefixPath        The prefix to add to the path. The prefix must end with a delimiter '/'. If the prefix path is null or empty then it is placed at the root.
     * @return The entire list of mutable content that was placed in the staging area from this tree.
     */
    @Override
    public List<MutableContent> read_tree(String rootTreeHashValue, String prefixPath)
    {
        // Delegate plumbing to the repo engine:
        return this.engine.read_tree(this.repo, rootTreeHashValue, prefixPath);
    }

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     *
     * @param commitHashOrReference The SHA-1 hash or reference name (branch name) of the commit that we want to log backwards down to the root.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    @Override
    public Log log(String commitHashOrReference)
    {
        return engine.log(repo, commitHashOrReference);
    }


    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     *
     * @param referenceName The name of the reference to log from.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    @Override
    public Log log_from_reference_name(String referenceName)
    {
        return engine.log_from_reference_name(repo, referenceName);
    }

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     *
     * @param commitHashValue The SHA-1 hash of the commit that we want to log backwards down to the root.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    @Override
    public Log log_from_commit_hash(String commitHashValue)
    {
        return engine.log_from_commit_hash(repo, commitHashValue);
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
     * @param commitHashValue The SHA-1 hash value of the commit that we want to walk backwards down to the root.
     * @return The list of commit objects in reverse chronological order.
     */
    @Override
    public List<Commit> rev_list(String commitHashValue)
    {
        return this.engine.rev_list(this.repo, commitHashValue);
    }

    /**
     * Updates or creates a reference in 'heads' with the given name to point at the commit with the given hash.
     * <p>
     * Update the object name stored in a ref safely.
     * <p>
     * https://git-scm.com/docs/git-update-ref
     *
     * @param referenceName   The name of the reference (branch) to update or create.
     * @param commitHashValue The SHA-1 hash value of the commit that the reference must point at.
     * @return The reference that was created or updated.
     */
    @Override
    public HashReference update_ref_in_heads(String referenceName, String commitHashValue)
    {
        return this.engine.update_ref(this.repo, this.repo.database.refs.heads, referenceName, commitHashValue);
    }

    /**
     * Updates or creates a reference in 'tags' with the given name to point at the commit with the given hash.
     * <p>
     * Update the object name stored in a ref safely.
     * <p>
     * https://git-scm.com/docs/git-update-ref
     *
     * @param referenceName   The name of the reference (tag) to update or create.
     * @param commitHashValue The SHA-1 hash value of the commit that the reference must point at.
     * @return The reference that was created or updated.
     */
    @Override
    public HashReference update_ref_in_tags(String referenceName, String commitHashValue)
    {
        return this.engine.update_ref(this.repo, this.repo.database.refs.tags, referenceName, commitHashValue);
    }

    /**
     * Updates the HEAD symbolic reference to the given reference name.
     *
     * @param referenceName The name of the reference to update HEAD to.
     * @return The HEAD symbolic reference for the repo.
     */
    @Override
    public SymbolicReference symbolic_ref(String referenceName)
    {
        return this.engine.symbolic_ref(this.repo, referenceName);
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
     * <p>
     * The current committer and author is used for the commit.
     *
     * @param rootTreeHashValue  The SHA1 hash value of the tree to use as the root of the commit.
     * @param commitMessage      The commit message to use.
     * @param commitParentHashes The hashes of the parent commits for this new commit. A commit object may have any number of parents. With exactly one parent, it is an ordinary commit. Having more than one parent makes the commit a merge between several lines of history. Initial (root) commits have no parents.
     * @return The commit object that was created.
     */
    @Override
    public Commit commit_tree(String rootTreeHashValue, String commitMessage, String... commitParentHashes)
    {
        // Capture the current context:
        ZonedDateTime now = this.getNowOverride();
        String author = this.getAuthor();
        String committer = this.getCommitter();

        // Make sure we have the commit time:
        if (now == null) now = ZonedDateTime.now();

        // Delegate plumbing to the repo engine:
        return this.engine.commit_tree(this.repo, rootTreeHashValue, commitMessage, author, now, committer, now, commitParentHashes);
    }

    /**
     * Record changes to the repository.
     * Doing a commit without any content will still have a successful commit
     * <p>
     * https://git-scm.com/docs/git-commit
     * <p>
     * Stores the current contents of the index in a new commit along with a log message from the user describing the changes.
     *
     * @param commitMessage   The commit message to use.
     * @param createSnapshots True to create a snapshot of the content when adding it to the staging area. False to pass the content by reference, thus minimizing an expensive copy operation.
     * @return The commit that was created.
     */
    @Override
    public Commit commitAll(String commitMessage, boolean createSnapshots)
    {
        // Capture the current context:
        ZonedDateTime now = this.getNowOverride();
        String author = this.getAuthor();
        String committer = this.getCommitter();

        // Make sure we have the commit time:
        if (now == null) now = ZonedDateTime.now();

        return engine.commitAll(repo, commitMessage, author, now, committer, now, createSnapshots);
    }

    /**
     * Record changes to the repository.
     * <p>
     * https://git-scm.com/docs/git-commit
     * <p>
     * Stores the current contents of the index in a new commit along with a log message from the user describing the changes.
     *
     * @param commitMessage      The commit message to use.
     * @param createSnapshots    True to create a snapshot of the content when adding it to the staging area. False to pass the content by reference, thus minimizing an expensive copy operation.
     * @param parentCommitHashes List of the parentCommitHashes
     * @return The commit that was created.
     */
    public Commit commitAll_UseCommitParentHashes(String commitMessage, boolean createSnapshots, Hash... parentCommitHashes)
    {
        // Capture the current context:
        ZonedDateTime now = this.getNowOverride();
        String author = this.getAuthor();
        String committer = this.getCommitter();

        // Make sure we have the commit time:
        if (now == null) now = ZonedDateTime.now();

        return engine.commitAll_UseCommitParentHashes(repo, commitMessage, author, now, committer, now, createSnapshots, convertHashArrayToStringArray(parentCommitHashes));
    }

    /**
     * Converts an array of hashes to an array of strings.
     *
     * @param hashes The hashes to convert.
     * @return The string representation of the hashes.
     */
    public static String[] convertHashArrayToStringArray(Hash... hashes)
    {
        return Stream.of(hashes)
                .map(hash -> hash.value)
                .toArray(String[]::new);
    }

    /**
     * Gets the commit with the given hash or reference (branch) name.
     * You can pass "HEAD" to get the currently checked out commit.
     *
     * @param commitHashOrReferenceOrHEAD The SHA-1 hash or reference name (branch name) of the commit that we want to get. Use "HEAD" to get the currently checked out commit.
     * @return The commit that is referenced. Null if it cannot be found.
     */
    @Override
    public Commit resolveCommit(String commitHashOrReferenceOrHEAD)
    {
        return engine.resolveCommit(repo, commitHashOrReferenceOrHEAD);
    }

    /**
     * Gets the reference with the given reference (branch) name.
     * You can pass "HEAD" to get the currently checked out commit.
     *
     * @param referenceOrHEAD The reference name (branch name) to get. Use "HEAD" to get the currently checked out branch.
     * @return The resolved reference. Null if it cannot be found.
     */
    @Override
    public HashReference resolveReference(String referenceOrHEAD)
    {
        return engine.resolveReference(repo, referenceOrHEAD);
    }

    /**
     * Clears the three areas the repo this handler is responsible for
     */
    public void clearAreas()
    {
        this.engine.clearAreas(this.repo);
    }

    /**
     * Convenience method for returning list of all of the commits in the database
     *
     * @return all commits in the database
     */
    public List<Commit> getCommits()
    {
        return engine.getCommits(repo);
    }

    /**
     * Convenience method for returning a stream of all the commits in the database
     *
     * @return A stream of all the commit objects in the database
     */
    public Stream<Commit> getCommitStream()
    {
        return engine.getCommitStream(repo);
    }

    /**
     * Gets the names of the branches in the repo.
     *
     * @return The names of the branches in the repo.
     */
    public List<String> getBranchNames()
    {
        return engine.getBranchNames(repo);
    }

    /**
     * Gets the hash references to commits for each branch in the repo.
     * <p>
     * The key is the branch name. The value is the hash.
     *
     * @return The branches references in the repo. The key is the branch name. The value is the hash.
     */
    public Map<String, Hash> getBranchCommitReferenceMap()
    {
        return engine.getBranchCommitReferenceMap(repo);
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
     * @param commitHashOrReferenceOrHEAD The SHA-1 hash or reference name (branch name) of the commit that we want to get. Use "HEAD" to get the currently checked out commit.
     * @param revisionOffset              The offset from the commit pointed to by the branch name. 0 means the last commit for the branch. -1 means the commit before. +1 means the commit after.
     */
    @Override
    public void checkout(String commitHashOrReferenceOrHEAD, int revisionOffset)
    {
        engine.checkout(repo, commitHashOrReferenceOrHEAD, revisionOffset);
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
     * @param path The path of the content to restore.
     */
    @Override
    public void checkout_path(RepoPath path)
    {
        engine.checkout_path(repo, path);
    }

    /**
     * Restores the content matching the pattern to its original contents from the index.
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
     * @param pattern The pattern of paths to restore.
     */
    @Override
    public void checkout_pattern(RepoPattern pattern)
    {
        engine.checkout_pattern(repo, pattern);
    }

    /**
     * Creates a new branch with the given name.
     * <p>
     * https://git-scm.com/docs/git-branch
     * <p>
     * The command’s second form creates a new branch head named <branchname> which points to the current HEAD, or <start-point> if given.
     * Note that this will create the new branch, but it will not switch the working tree to it; use "git checkout <newbranch>" to switch to the new branch.
     *
     * @param branchName The name of the branch to create.
     */
    @Override
    public void branch(String branchName)
    {
        engine.branch(repo, branchName);
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
     * @param branchNames The names of the branches to delete.
     */
    @Override
    public void branch_delete(String... branchNames)
    {
        engine.branch_delete(repo, branchNames);
    }

    /**
     * Gets the name of the currently checked out branch.
     *
     * @return The name of the branch that we are currently on.
     */
    @Override
    public String getCurrentBranchName()
    {
        return engine.getCurrentBranchName(repo);
    }

    /**
     * Returns a string that can hopefully aid in the debugging or analysis of the repo and it's structures.
     *
     * @return
     */
    public String getDebugString() {
        return this.engine.getDebugString(repo);
    }
}
