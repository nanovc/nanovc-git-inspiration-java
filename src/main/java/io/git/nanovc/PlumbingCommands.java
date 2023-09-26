package io.git.nanovc;

import java.util.List;

/**
 * The interface for low level plumbing commands on a Nano Version Control repository.
 * <p>
 * Because Git was initially a toolkit for a VCS rather than a full user-friendly VCS,
 * it has a bunch of verbs that do low-level work and were designed to be chained together UNIX style or called from
 * scripts.
 * These commands are generally referred to as "plumbing" commands,
 * and the more user-friendly commands are called "porcelain" commands.
 * https://git-scm.com/book/en/v2/Git-Internals-Plumbing-and-Porcelain
 * <p>
 * The list of porcelain commands is taken from here:
 * https://git-scm.com/docs
 * <p>
 * cat-file
 * check-ignore
 * commit-tree
 * count-objects
 * diff-index
 * for-each-ref
 * hash-object
 * ls-files
 * merge-base
 * read-tree
 * rev-list
 * rev-parse
 * show-ref
 * symbolic-ref
 * update-index
 * update-ref
 * verify-pack
 * write-tree
 * </p>
 * <p>
 * NOTE: We reveal the API as we need the commands.
 */
public interface PlumbingCommands
{


    /**
     * Provide content or type and size information for repository objects.
     * <p>
     * In its first form, the command provides the content or the type of an object in the repository.
     * The type is required unless -t or -p is used to find the object type,
     * or -s is used to find the object size,
     * or --textconv or --filters is used (which imply type "blob").
     * <p>
     * In the second form, a list of objects (separated by linefeeds) is provided on stdin, and the SHA-1, type, and size of each object is printed on stdout.
     * <p>
     * The output format can be overridden using the optional <format> argument.
     * <p>
     * If either --textconv or --filters was specified, the input is expected to list the object names followed by the path name,
     * separated by a single white space,
     * so that the appropriate drivers can be determined.
     * <p>
     * https://git-scm.com/docs/git-cat-file
     *
     * @param hashValue The SHA-1 hash to use to get the repo object.
     * @return The repo object with the given hash.
     */
    RepoObject cat_file(String hashValue);

    /**
     * Provide content or type and size information for repository objects.
     * <p>
     * In its first form, the command provides the content or the type of an object in the repository.
     * The type is required unless -t or -p is used to find the object type,
     * or -s is used to find the object size,
     * or --textconv or --filters is used (which imply type "blob").
     * <p>
     * In the second form, a list of objects (separated by linefeeds) is provided on stdin, and the SHA-1, type, and size of each object is printed on stdout.
     * <p>
     * The output format can be overridden using the optional <format> argument.
     * <p>
     * If either --textconv or --filters was specified, the input is expected to list the object names followed by the path name,
     * separated by a single white space,
     * so that the appropriate drivers can be determined.
     * <p>
     * https://git-scm.com/docs/git-cat-file
     *
     * @param hash The hash to use to get the repo object.
     * @return The repo object with the given hash.
     */
    default RepoObject cat_file(Hash hash)
    {
        // Get the hash value:
        return cat_file(hash.value);
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
    ObjectType cat_file_object_type(String hashValue);

    /**
     * Provide type information for repository objects.
     * <p>
     * In its first form, the command provides the type of an object in the repository.
     * <p>
     * https://git-scm.com/docs/git-cat-file
     *
     * @param hash The hash to use to get the repo object type.
     * @return The type of object stored with the given hash.
     */
    default ObjectType cat_file_object_type(Hash hash)
    {
        return cat_file_object_type(hash.value);
    }


    //    void check_ignore();
    //    void commit_tree();
    //    void count_objects();
    //    void diff_index();
    //    void for_each_ref();

    /**
     * Compute object ID.
     * Computes the object ID value for an object with specified type with the contents of the content byte array.
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
     * @param type    Specify the type of object to create (default: "blob").
     * @param content The content to hash.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content.  A new hash instance is created each time this is called.
     */
    Hash hash_object(ObjectType type, byte[] content);

    /**
     * Compute object ID.
     * Computes the object ID value for an object with specified type with the contents of the content byte array.
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
     * @param repoObject The repo object to compute the hash of. The hash is recomputed from the object type and the current contents of the repo object.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content. A new hash instance is created each time this is called.
     */
    default Hash hash_object(RepoObject repoObject)
    {
        return hash_object(repoObject.getObjectType(), repoObject.getByteArray());
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
     * @param type    Specify the type of object to create (default: "blob"). If this is null then it defaults to BLOB.
     * @param content The content to hash.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     */
    Hash hash_object_write(ObjectType type, byte[] content);

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
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     */
    Hash hash_object_write_blob(byte[] content);

    /**
     * Compute object ID and write the string to the database as a UTF8 blob.
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
     * @param string The string content to write. The string is stored as UTF8.
     * @return The output from the command is a 40-character checksum hash. This is the SHA-1 hash – a checksum of the content
     */
    Hash hash_object_write_string(String string);

    //    void ls_files();
    //    void merge_base();
    //    void read_tree();
    //    void rev_list();
    //    void rev_parse();
    //    void show_ref();
    //    void symbolic_ref();


    /**
     * Register file contents in the working tree to the index (staging area).
     * Modifies the index or directory cache.
     * Each file mentioned is updated into the index and any unmerged or needs updating state is cleared.
     * <p>
     * See also git-add(1) for a more user-friendly way to do some of the most common operations on the index.
     * <p>
     * The way git update-index handles files it is told about can be modified using the various options:
     * <p>
     * https://git-scm.com/docs/git-update-index
     *
     * @param hash           The hash of the existing content to add to the staging area (index).
     * @param pathForContent The path in the staging area to add the content at.
     * @return The content that was staged from the object with the given hash.
     */
    default MutableContent update_index_add_cacheInfo(Hash hash, RepoPath pathForContent)
    {
        return update_index_add_cacheInfo(hash.value, pathForContent.toAbsolutePath().toString());
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
    MutableContent update_index_add_cacheInfo(String hashValue, String absolutePathForContent);

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
     * @param pathOfContentInWorkingArea The path of the content in the working area to add to the staging area.
     * @return The content that was staged from the object with the given hash.
     */
    default MutableContent update_index_add(RepoPath pathOfContentInWorkingArea)
    {
        return update_index_add(pathOfContentInWorkingArea.toAbsolutePath().toString());
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
    MutableContent update_index_add(String absolutePathOfContentInWorkingArea);

    //    void update_ref();
    //    void verify_pack();


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
     * <p>
     * https://git-scm.com/docs/git-write-tree
     *
     * @return The root tree object that was created. A tree object is created for each sub folder that is written, but this method only returns the root tree.
     */
    Tree write_tree();


    /**
     * Reads tree information into the index (staging area).
     * Keep the current index contents,
     * and read the contents of the named tree-ish under the directory at <prefix>.
     * The command will refuse to overwrite entries that already existed in the original index file.
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
     * @param rootTreeHash The hash value of the root of the tree to read out from the object database into the staging area.
     * @param prefixPath   The prefix to add to the path. If the prefix path is null or empty then it is placed at the root.
     * @return The entire list of mutable content that was placed in the staging area from this tree.
     */
    default List<MutableContent> read_tree(Hash rootTreeHash, RepoPath prefixPath)
    {
        return read_tree(rootTreeHash.value, prefixPath.toAbsolutePath().ensureEndsWithDelimiter().toString());
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
    List<MutableContent> read_tree(String rootTreeHashValue, String prefixPath);

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
     * <p>
     * https://git-scm.com/docs/git-commit-tree
     *
     * @param rootTreeHash       The hash value of the tree to use as the root of the commit.
     * @param commitMessage      The commit message to use.
     * @param commitParentHashes The hashes of the parent commits for this new commit. A commit object may have any number of parents. With exactly one parent, it is an ordinary commit. Having more than one parent makes the commit a merge between several lines of history. Initial (root) commits have no parents.
     * @return The commit object that was created.
     */
    default Commit commit_tree(Hash rootTreeHash, String commitMessage, Hash... commitParentHashes)
    {
        // Create the array of commit parents:
        String[] commitParentHashValues = RepoHandler.convertHashArrayToStringArray(commitParentHashes);

        // Commit the tree:
        return commit_tree(rootTreeHash.value, commitMessage, commitParentHashValues);
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
     * <p>
     * https://git-scm.com/docs/git-commit-tree
     *
     * @param rootTreeHashValue  The SHA1 hash value of the tree to use as the root of the commit.
     * @param commitMessage      The commit message to use.
     * @param commitParentHashes The hashes of the parent commits for this new commit. A commit object may have any number of parents. With exactly one parent, it is an ordinary commit. Having more than one parent makes the commit a merge between several lines of history. Initial (root) commits have no parents.
     * @return The commit object that was created.
     */
    Commit commit_tree(String rootTreeHashValue, String commitMessage, String... commitParentHashes);


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
     * <p>
     * https://git-scm.com/docs/git-rev-list
     *
     * @param commitHash The hash of the commit that we want to walk backwards down to the root.
     * @return The list of commit objects in reverse chronological order.
     */
    default List<Commit> rev_list(Hash commitHash)
    {
        return rev_list(commitHash.value);
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
     * <p>
     * https://git-scm.com/docs/git-rev-list
     *
     * @param commitHashValue The SHA-1 hash value of the commit that we want to walk backwards down to the root.
     * @return The list of commit objects in reverse chronological order.
     */
    List<Commit> rev_list(String commitHashValue);

    /**
     * Updates or creates a reference with the given name to point at the commit with the given hash.
     * <p>
     * Update the object name stored in a ref safely.
     * <p>
     * https://git-scm.com/docs/git-update-ref
     *
     * @param referenceName The name of the reference (branch) to update or create.
     * @param commitHash    The hash of the commit that the reference must point at.
     * @return The reference that was created or updated.
     */
    default HashReference update_ref_in_heads(String referenceName, Hash commitHash)
    {
        return update_ref_in_heads(referenceName, commitHash.value);
    }

    /**
     * Updates or creates a reference with the given name to point at the commit with the given hash.
     * <p>
     * Update the object name stored in a ref safely.
     * <p>
     * https://git-scm.com/docs/git-update-ref
     *
     * @param referenceName   The name of the reference (branch) to update or create.
     * @param commitHashValue The SHA-1 hash value of the commit that the reference must point at.
     * @return The reference that was created or updated.
     */
    HashReference update_ref_in_heads(String referenceName, String commitHashValue);

    /**
     * Updates or creates a reference in 'tags' with the given name to point at the commit with the given hash.
     * <p>
     * Update the object name stored in a ref safely.
     * <p>
     * https://git-scm.com/docs/git-update-ref
     *
     * @param referenceName The name of the reference (tag) to update or create.
     * @param commit        The hash of the commit that the reference must point at.
     * @return The reference that was created or updated.
     */
    default HashReference update_ref_in_tags(String referenceName, Hash commit)
    {
        return update_ref_in_tags(referenceName, commit.value);
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
    HashReference update_ref_in_tags(String referenceName, String commitHashValue);

    /**
     * Updates the HEAD symbolic reference to the given reference name.
     * <p>
     * https://git-scm.com/docs/git-symbolic-ref
     *
     * @param reference The reference to update HEAD to.
     * @return The HEAD symbolic reference for the repo.
     */
    default SymbolicReference symbolic_ref(HashReference reference)
    {
        return symbolic_ref(reference.name);
    }

    /**
     * Updates the HEAD symbolic reference to the given reference name.
     * <p>
     * https://git-scm.com/docs/git-symbolic-ref
     *
     * @param referenceName The name of the reference to update HEAD to.
     * @return The HEAD symbolic reference for the repo.
     */
    SymbolicReference symbolic_ref(String referenceName);
}
