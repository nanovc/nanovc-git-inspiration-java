package io.git.nanovc;

/**
 * The interface for all paths in Nano Version Control.
 * @param <TSelf> The specific type of path being implemented. This is needed so that we can get chained calls with the specific type of path.
 */
public interface Path<TSelf extends Path<TSelf>>
{

    /**
     * Resolves the relative path from the current path.
     * It supports relative and absolute paths.
     *
     * @param relativeOrAbsolutePath The relative or absolute path to resolve. If it is relative then it is appended to the current path. If it is absolute then the absolute path is returned and the current path is ignored.
     * @return The resolved path.
     */
    TSelf resolve(String relativeOrAbsolutePath);

    /**
     * This ensures that the path has a delimiter at the end.
     * If the path already has a delimiter then the same instance is returned.
     *
     * @return The path with a delimiter at the end. This existing instance is returned if it already has a delimiter at the end.
     */
    TSelf ensureEndsWithDelimiter();

    /**
     * Returns an absolute path from this path.
     * If it already is an absolute path it returns itself without making a new instance.
     *
     * @return An absolute path for the current path.
     */
    TSelf toAbsolutePath();

    /**
     * Splits the path into separate parts, broken up by the path delimiter '/'.
     * If it's an absolute path then the first part is the entry straight under the root (the blank string for the root is stripped off).
     * @return The separate parts of the path.
     */
    String[] splitIntoParts();
}
