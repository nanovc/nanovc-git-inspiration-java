package io.git.nanovc;

/**
 * A repository of data that is being version controlled.
 * NOTE: This repo represents the usable part of the version controlled data.
 * <p>
 * The Three States
 * https://git-scm.com/book/en/v2/Getting-Started-Git-Basics#The-Three-States
 * Git has three main states that your files can reside in: committed, modified, and staged. Committed means that the
 * data is safely stored in your local database. Modified means that you have changed the file but have not committed it
 * to your database yet. Staged means that you have marked a modified file in its current version to go into your next
 * commit snapshot.
 * <p>
 * This leads us to the three main sections of a Git project: the Git directory, the working tree, and the staging
 * area.
 * The Git directory is where Git stores the metadata and object database for your project. This is the most important
 * part of Git, and it is what is copied when you clone a repository from another computer.
 * The working tree is a single checkout of one version of the project. These files are pulled out of the compressed
 * database in the Git directory and placed on disk for you to use or modify.
 * The staging area is a file, generally contained in your Git directory, that stores information about what will go
 * into your next commit. It’s sometimes referred to as the “index”, but it’s also common to refer to it as the staging
 * area.
 * The basic Git workflow goes something like this:
 * <p>
 * You modify files in your working tree.
 * 1) You stage the files, adding snapshots of them to your staging area.
 * 2) You do a commit, which takes the files as they are in the staging area and stores that snapshot permanently to
 * your Git directory.
 * 3) If a particular version of a file is in the Git directory, it’s considered committed. If it has been modified and
 * was added to the staging area, it is staged. And if it was changed since it was checked out but has not been staged,
 * it is modified
 */
public class Repo
{
    /**
     * The working area for the repository.
     * The working area where data is checked out at a certain revision.
     * Any changes to the content can be made in the working area.
     * The working area where data is checked out at a certain revision.
     * <p>
     * The "index" holds a snapshot of the content of the working tree,
     * and it is this snapshot that is taken as the contents of the next commit.
     * Thus after making any changes to the working tree,
     * and before running the commit command,
     * you must use the add command to add any new or modified files to the index.
     * <p>
     * This area is mutable because it represents what would have been the file system with real git.
     * This means that you are welcome to keep and modify the references to the mutable content without needing to use
     * this API.
     */
    public MutableContentArea workingArea = new MutableContentArea();

    /**
     * The staging area for the repository.
     * The staging area where data is prepared for being added to version control.
     * The staging area or "index" holds a snapshot of the content of the working tree,
     * and it is this snapshot that is taken as the contents of the next commit.
     * Thus after making any changes to the working tree,
     * and before running the commit command,
     * you must use the add command to add any new or modified files to the index.
     * <p>
     * This area is mutable because it represents what would have been the file system with real git.
     * This means that you are welcome to keep and modify the references to the mutable content without needing to use
     * this API.
     */
    public MutableContentArea stagingArea = new MutableContentArea();

    /**
     * An area where the content for the current checked out revision (HEAD) is checked out.
     * This area is useful for searching for differences by comparing the working or staging areas to the current
     * committed area.
     * The committed area where data is checked out at a certain revision.
     * <p>
     * This area is immutable because it represents the committed data and is not meant to be edited.
     */
    public ImmutableContentArea committedArea = new ImmutableContentArea();

    /**
     * The database of version control information.
     * This corresponds to the .git folder in a git repository.
     */
    public Database database = new Database();


    /**
     * Create a new repo.
     */
    public Repo()
    {
    }

    /**
     * Initialises a new repo with the given description.
     *
     * @param description The description for the repo. A name as the description can be useful for distinguishing
     *                    repos.
     */
    public Repo(String description)
    {
        this.database.description = description;
    }
}
