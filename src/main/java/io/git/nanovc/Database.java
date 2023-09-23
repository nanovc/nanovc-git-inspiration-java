package io.git.nanovc;

/**
 * The database of version control information.
 * This corresponds to the .git folder in a git repository.
 */
public class Database
{

    /**
     * Points to the branch you currently have checked out.
     * If this is null then we don't have any revision checked out yet.
     *
     * The HEAD file is a symbolic reference to the branch you’re currently on.
     * By symbolic reference, we mean that unlike a normal reference,
     * it doesn’t generally contain a SHA-1 value but rather a pointer to another reference.
     *
     * HOWEVER, if we are not on a branch but rather have a detached head,
     * this will contain the SHA1 hash of the commit that we checked out.
     */
    public SymbolicReference HEAD;

    /**
     * The project specific configuration options.
     */
    public Config config = new Config();

    /**
     * A free-form description of the repo that can be used to explain what the repo represents.
     */
    public String description;

    /**
     * The name of the repo.
     * This can be useful for distinguishing repos.
     */
    public String name;

    /**
     * The client or server side hooks
     */
    public Hooks hooks = new Hooks();

    /**
     * Info about the repo.
     * This holds information about excludes.
     */
    public Info info = new Info();

    /**
     * The objects directory stores all the content for your database.
     */
    public RepoObjectStore objects = new RepoObjectStore();

    /**
     * The refs directory stores pointers into commit objects in that data (branches).
     */
    public Refs refs = new Refs();
}
