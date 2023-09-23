package io.git.nanovc;

import java.util.Optional;

/**
 * An area where mutable content resides.
 * If this was an actual git repository then this might be the file system where files would reside.
 * This is true for a working area, however it is logically true for the content of the index (staging area) too.
 * Most people would interact with a check-out of files into a working directory (working area) of the repo. That is a
 * content area too.
 * <p>
 * Since this is mutable, you are able to modify the state of the content area without going through the API.
 * This makes it easy to work with the content area in high performance scenarios
 * or when you want to build up the content area programmatically.
 */
public class MutableContentArea extends ContentAreaBase<MutableContent>
{
    /**
     * The list of content for this area.
     * It is not indexed because the paths are mutable.
     * Therefore each access by path needs to search through the list to find the content at the given path.
     */
    public ContentList<MutableContent> contents = new ContentList<>();

    /**
     * Creates and puts the given content into this map.
     * If content at this path already exists, it is replaced by mutating the existing content.
     *
     * @param absolutePath The absolute path in the repo where the content must be put.
     * @param content      The content to put.
     * @return The content that was created.
     */
    @Override
    public MutableContent putContent(String absolutePath, byte... content)
    {
        // Check whether we already have content there:
        MutableContent existingContent = getContent(absolutePath);
        if (existingContent == null)
        {
            // We do not have existing content there yet.

            // Create the mutable content:
            existingContent = new MutableContent(absolutePath, content);

            // Add it to the content area:
            this.contents.add(existingContent);
        }
        else
        {
            // We already have existing content at the given path.

            // Update the existing content:
            existingContent.content = content;
        }

        return existingContent;
    }

    /**
     * Puts the given content into this map.
     * If content at the given path already exists then it is mutated to match the inputs content.
     *
     * @param content The content to put into this map. It gets indexed by the content absolute path.
     * @return This content area, so that it can be chained in a fluent manner.
     */
    @Override
    public ContentArea<MutableContent> putContent(MutableContent content)
    {
        // Check whether we already have content there:
        MutableContent existingContent = getContent(content.getAbsolutePath());
        if (existingContent == null)
        {
            // We do not have existing content there yet.

            // Add it to the content area:
            this.contents.add(content);
        }
        else
        {
            // We already have existing content at the given path.

            // Update the existing content:
            existingContent.content = content.content;
        }

        return this;
    }

    /**
     * Gets the content at the given path.
     *
     * @param absolutePath The absolute path in the repo of the content to get.
     * @return The content at the given path. Null if there is no content at the path.
     */
    @Override
    public MutableContent getContent(String absolutePath)
    {
        // Search for content with the given path:
        Optional<MutableContent> match = this.contents.stream().filter(mutableContent -> absolutePath.equals(mutableContent.getAbsolutePath())).findAny();

        return match.isPresent() ? match.get() : null;
    }

    /**
     * Removes content at the given path if there is content.
     *
     * @param absolutePath The absolute path in the repo of the content to remove.
     * @return This content area, so that it can be chained in a fluent manner.
     */
    @Override
    public ContentArea<MutableContent> removeContent(String absolutePath)
    {
        // Remove any content at the given path:
        this.contents.removeIf(mutableContent -> absolutePath.equals(mutableContent.getAbsolutePath()));
        return this;
    }

    /**
     * Gets a snapshot of the content as a map.
     * The key is the absolute path of the content.
     * The map is a snapshot.
     * The references to the content are live.
     *
     * @return A snapshot of the current content area, indexed by the absolute path of the content.
     */
    @Override
    public ContentMap<MutableContent> getContentMapSnapshot()
    {
        // Create a new map:
        ContentMap<MutableContent> snapshot = new ContentMap<>();

        // Copy the content across:
        this.contents.forEach(snapshot::putContent);

        return snapshot;
    }

    /**
     * Gets a snapshot of the content as a list.
     * The list is a snapshot.
     * The references to the content are live.
     *
     * @return A snapshot of the current content area.
     */
    @Override
    public ContentList<MutableContent> getContentListSnapshot()
    {
        // Create a new list:
        ContentList<MutableContent> snapshot = new ContentList<>();

        // Copy the content across:
        snapshot.addAll(this.contents);

        return snapshot;
    }

    @Override
    public boolean hasContent() {
        return !contents.isEmpty();
    }

    /**
     * Clears all the content from the content area
     */
    @Override
    public void clear() {
        this.contents.clear();
    }
}
