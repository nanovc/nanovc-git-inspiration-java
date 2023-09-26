package io.git.nanovc;

/**
 * The interface for high level porcelain commands on a Nano Version Control repository.
 * <p>
 * Because Git was initially a toolkit for a VCS rather than a full user-friendly VCS,
 * it has a bunch of verbs that do low-level work and were designed to be chained together UNIX style or called from scripts.
 * These commands are generally referred to as "plumbing" commands,
 * and the more user-friendly commands are called "porcelain" commands.
 * https://git-scm.com/book/en/v2/Git-Internals-Plumbing-and-Porcelain
 */
public interface PorcelainCommands
{
    /**
     * Create an empty Git repository or reinitialize an existing one.
     * <p>
     * https://git-scm.com/docs/git-init
     *
     * @return The repo that was initialised.
     */
    Repo init();

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     * <p>
     * https://git-scm.com/docs/git-log
     *
     * @param commitHash The hash of the commit that we want to log backwards down to the root.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    default Log log(Hash commitHash)
    {
        return log_from_commit_hash(commitHash.value);
    }

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     * <p>
     * https://git-scm.com/docs/git-log
     *
     * @param reference The reference to the commit that we want to log backwards down to the root.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    default Log log(HashReference reference)
    {
        return log_from_commit_hash(reference.hash.value);
    }

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     * <p>
     * https://git-scm.com/docs/git-log
     *
     * @param commitHashOrReference The SHA-1 hash or reference name (branch name) of the commit that we want to log backwards down to the root.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    Log log(String commitHashOrReference);

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     * <p>
     * https://git-scm.com/docs/git-log
     *
     * @param commitHashValue The SHA-1 hash of the commit that we want to log backwards down to the root.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    Log log_from_commit_hash(String commitHashValue);

    /**
     * Gets a log of all the commits leading up to the given commit.
     * The list is in reverse chronological order.
     * <p>
     * https://git-scm.com/docs/git-log
     *
     * @param referenceName The name of the reference to log from.
     * @return The log of commits from the given commit, going backwards to the root.
     */
    Log log_from_reference_name(String referenceName);


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
    void addAll(boolean createSnapshots);


    /**
     * Stages the given content at the given path in the given repo.
     * This is useful if you want to bypass the working area and stage content directly.
     * <p>
     * https://git-scm.com/docs/git-stage
     *
     * @param path    The path in the repo to stage the content in.
     * @param content The content to stage at the given path.
     * @return The content that was staged.
     */
    default MutableContent stage(RepoPath path, byte... content)
    {
        // Get the absolute path for the content because our staging area needs absolute paths:
        return stage(path.toAbsolutePath().toString(), content);
    }

    /**
     * Stages the given content at the given path in the given repo.
     * This is useful if you want to bypass the working area and stage content directly.
     * <p>
     * https://git-scm.com/docs/git-stage
     *
     * @param absolutePath The absolute path in the repo to stage the content in.
     * @param content      The content to stage at the given path.
     * @return The content that was staged.
     */
    MutableContent stage(String absolutePath, byte... content);

    /**
     * Puts the given content in the working area of the repo.
     *
     * @param absolutePath The absolute path in the working area where the content will be put.
     * @param content      The content bytes to store at the path.
     * @return The content in the in the working area that was put.
     */
    MutableContent putWorkingAreaContent(String absolutePath, byte... content);

    /**
     * Puts the given content in the working area of the repo.
     *
     * @param path    The path in the working area where the content will be put. This will be interpreted as an absolute path.
     * @param content The content bytes to store at the path.
     * @return The content in the in the working area that was put.
     */
    default MutableContent putWorkingAreaContent(RepoPath path, byte... content)
    {
        return putWorkingAreaContent(path.toAbsolutePath().toString(), content);
    }

    /**
     * Gets the content in the working area of the repo.
     *
     * @param absolutePath The absolute path in the working area where we get the content from.
     * @return The content in the working area at the given absolute path. Null if there is no content at that path.
     */
    MutableContent getWorkingAreaContent(String absolutePath);

    /**
     * Gets the content in the working area of the repo.
     *
     * @param path The path in the working area where we get the content from.
     * @return The content in the working area at the given path. Null if there is no content at that path.
     */
    default MutableContent getWorkingAreaContent(RepoPath path)
    {
        return getWorkingAreaContent(path.toAbsolutePath().toString());
    }

    /**
     * Record changes to the repository.
     * <p>
     * https://git-scm.com/docs/git-commit
     * <p>
     * Stores the current contents of the index in a new commit along with a log message from the user describing the changes.
     *
     * @param commitMessage   The commit message to use.
     * @param createSnapshots True to create a snapshot of the content when adding it to the staging area. False to pass the content by reference, thus minimizing an expensive copy operation.
     * @return The commit that was created.
     */
    Commit commitAll(String commitMessage, boolean createSnapshots);

    /**
     * Gets the commit with the given hash or reference (branch) name.
     * You can pass "HEAD" to get the currently checked out commit.
     *
     * @param commitHashOrReferenceOrHEAD The SHA-1 hash or reference name (branch name) of the commit that we want to get. Use "HEAD" to get the currently checked out commit.
     * @return The commit that is referenced. Null if it cannot be found.
     */
    Commit resolveCommit(String commitHashOrReferenceOrHEAD);

    /**
     * Gets the reference with the given reference (branch) name.
     * You can pass "HEAD" to get the currently checked out commit.
     *
     * @param referenceOrHEAD The reference name (branch name) to get. Use "HEAD" to get the currently checked out branch.
     * @return The resolved reference. Null if it cannot be found.
     */
    HashReference resolveReference(String referenceOrHEAD);

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
     */
    default void checkout(String commitHashOrReferenceOrHEAD)
    {
        checkout(commitHashOrReferenceOrHEAD, 0);
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
    void checkout(String commitHashOrReferenceOrHEAD, int revisionOffset);

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
    void checkout_path(RepoPath path);

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
    void checkout_pattern(RepoPattern pattern);

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
    void branch(String branchName);

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
    void branch_delete(String... branchNames);

    /**
     * Gets the name of the currently checked out branch.
     * <p>
     * https://git-scm.com/docs/git-branch#Documentation/git-branch.txt---show-current
     *
     * @return The name of the branch that we are currently on.
     */
    String getCurrentBranchName();
}
