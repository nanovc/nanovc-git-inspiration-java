package io.git.nanovc;

/**
 * A path in the repository.
 * This is lighter weight version of Java Paths.
 * We don't need the file system functionality.
 * Absolute paths start with a /
 * Relative paths do not start with a /.
 */
public class RepoPath extends PathBase<RepoPath>
{
    /**
     * Creates the given repo path.
     * @param relativeOrAbsolutePath The relative or absolute path to resolve. Absolute paths start with a / {@link #DELIMITER}. Relative paths don't.
     */
    public RepoPath(String relativeOrAbsolutePath)
    {
        super(relativeOrAbsolutePath);
    }

    /**
     * A factory method to create a new instance of the specific path.
     *
     * @param relativeOrAbsolutePath The relative or absolute path to resolve. Absolute paths start with a / {@link #DELIMITER}. Relative paths don't.
     * @return The new instance at the given path.
     */
    @Override
    protected RepoPath createInstance(String relativeOrAbsolutePath)
    {
        return new RepoPath(relativeOrAbsolutePath);
    }

    /**
     * Creates a repo path at the given location.
     * This is a convenience factory method to construct repo paths.
     * @param relativeOrAbsolutePath The relative or absolute path for the repository. Absolute paths start with /. Relative paths don't.
     * @return A repo path at the given location.
     */
    public static RepoPath at(String relativeOrAbsolutePath)
    {
        return new RepoPath(relativeOrAbsolutePath);
    }

    /**
     * Creates a repo path at the root.
     * This is a convenience factory method to construct repo paths.
     * @return A repo path at the root.
     */
    public static RepoPath atRoot()
    {
        return new RepoPath(DELIMITER);
    }

}
