package io.git.nanovc;

/**
 * An area where content resides.
 * If this was an actual git repository then this might be the file system where files would reside.
 * This is true for a working area, however it is logically true for the content of the index (staging area) too.
 * Most people would interact with a check-out of files into a working directory (working area) of the repo. That is a
 * content area too.
 *
 * @param <TContent> The type of content that is being stored in this content area.
 */
public interface ContentArea<TContent extends Content>
{

    /**
     * Creates and puts the given content into this map.
     * If content at this path already exists, it is replaced.
     *
     * @param absolutePath The absolute path in the repo where the content must be put.
     * @param content      The content to put.
     * @return The content that was created.
     */
    TContent putContent(String absolutePath, byte... content);

    /**
     * Creates and puts the given content into this map.
     * If content at this path already exists, it is replaced.
     *
     * @param path    The path in the repo where the content must be put.
     * @param content The content to put.
     * @return The content that was created.
     */
    default TContent putContent(RepoPath path, byte... content)
    {
        return putContent(path.toAbsolutePath().toString(), content);
    }

    /**
     * Puts the given content into this map.
     *
     * @param content The content to put into this map. It gets indexed by the content absolute path.
     * @return This content area, so that it can be chained in a fluent manner.
     */
    ContentArea<TContent> putContent(TContent content);

    /**
     * Gets the content at the given path.
     *
     * @param absolutePath The absolute path in the repo of the content to get.
     * @return The content at the given path. Null if there is no content at the path.
     */
    TContent getContent(String absolutePath);

    /**
     * Gets the content at the given path.
     *
     * @param path The path in the repo of the content to get.
     * @return The content at the given path. Null if there is no content at the path.
     */
    default TContent getContent(RepoPath path)
    {
        return getContent(path.toAbsolutePath().toString());
    }

    /**
     * Removes content at the given path if there is content.
     *
     * @param absolutePath The absolute path in the repo of the content to remove.
     * @return This content area, so that it can be chained in a fluent manner.
     */
    ContentArea<TContent> removeContent(String absolutePath);

    /**
     * Removes content at the given path if there is content.
     *
     * @param path The path in the repo of the content to remove.
     * @return This content area, so that it can be chained in a fluent manner.
     */
    default ContentArea<TContent> removeContent(RepoPath path)
    {
        return removeContent(path.toAbsolutePath().toString());
    }

    /**
     * Gets a snapshot of the content as a map.
     * The key is the absolute path of the content.
     * The map is a snapshot.
     * The references to the content are live.
     *
     * @return A snapshot of the current content area, indexed by the absolute path of the content.
     */
    ContentMap<TContent> getContentMapSnapshot();

    /**
     * Gets a snapshot of the content as a list.
     * The list is a snapshot.
     * The references to the content are live.
     *
     * @return A snapshot of the current content area.
     */
    ContentList<TContent> getContentListSnapshot();

    /**
     * Returns true if the content area has any content or false if it is empty
     * @return true if any content
     */
    boolean hasContent();

    /**
     * Clears all the content from the content area
     */
    public void clear();
}
