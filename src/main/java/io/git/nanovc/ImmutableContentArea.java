package io.git.nanovc;

import java.util.stream.Stream;

/**
 * An area where immutable content resides.
 * If this was an actual git repository then this might be the file system where files would reside.
 * This is true for a working area, however it is logically true for the content of the index (staging area) too.
 * Most people would interact with a check-out of files into a working directory (working area) of the repo. That is a content area too.
 *
 * When the content area is initially created, it is editable.
 * Once the content must be frozen from further edits, call {@link #freeze()}.
 */
public class ImmutableContentArea extends ContentAreaBase<ImmutableContent>
{
    /**
     * This flags whether the content area is frozen.
     * If it is frozen then you can no longer put or remove content from this content area.
     */
    private boolean frozen = false;

    /**
     * The map of content for this area, indexed by the absolute path of the content in the repo.
     */
    private ContentMap<ImmutableContent> contents = new ContentMap<>();

    /**
     * Gets whether this content area is frozen.
     * If it is frozen then you can no longer put or remove content from this content area.
     * Call {@link #freeze()} to freeze this content area from any further modifications.
     * @return True if the content area is frozen and can no longer be modified. False if you can still add or remove content from this content area.
     */
    public boolean isFrozen()
    {
        return frozen;
    }

    /**
     * Freezes this content area from any further modifications.
     * Call {@link #isFrozen()} to check whether this content area is frozen or not.
     */
    public void freeze()
    {
        frozen = true;
    }

    /**
     * Creates and puts the given content into this map.
     * If content at this path already exists, it is replaced with a new {@link ImmutableContent} instance.
     *
     * @param absolutePath The absolute path in the repo where the content must be put.
     * @param content      The content to put.
     * @return The content that was created.
     */
    @Override
    public ImmutableContent putContent(String absolutePath, byte... content)
    {
        // Make sure that we are not frozen:
        if (this.frozen) throw new ImmutableContentModifiedException("Cannot put content into an immutable area once it has been frozen. Place the content before calling freeze() or use a MutableContentArea.");

        // Create the immutable content wrapper:
        ImmutableContent wrapper = new ImmutableContent(absolutePath, content);

        // Put the content in the content area:
        putContent(wrapper);

        return wrapper;
    }

    /**
     * Puts the given content into this map.
     * It replaces any content at the given path if it already exists.
     *
     * @param content The content to put into this map. It gets indexed by the content absolute path.
     * @return This content area, so that it can be chained in a fluent manner.
     */
    @Override
    public ContentArea<ImmutableContent> putContent(ImmutableContent content)
    {
        // Make sure that we are not frozen:
        if (this.frozen) throw new ImmutableContentModifiedException("Cannot put content into an immutable area once it has been frozen. Place the content before calling freeze() or use a MutableContentArea.");

        // Put the content in the content area at the given path:
        // NOTE: This will replace any existing content at the given path.
        this.contents.putContent(content);

        return this;
    }

    /**
     * Gets the content at the given path.
     *
     * @param absolutePath The absolute path in the repo of the content to get.
     * @return The content at the given path. Null if there is no content at the path.
     */
    @Override
    public ImmutableContent getContent(String absolutePath)
    {
        return this.contents.getContent(absolutePath);
    }

    /**
     * Removes content at the given path if there is content.
     *
     * @param absolutePath The absolute path in the repo of the content to remove.
     * @return This content area, so that it can be chained in a fluent manner.
     */
    @Override
    public ContentArea<ImmutableContent> removeContent(String absolutePath)
    {
        // Make sure that we are not frozen:
        if (this.frozen) throw new ImmutableContentModifiedException("Cannot remove content from an immutable area once it has been frozen. Remove the content before calling freeze() or use a MutableContentArea.");

        this.contents.removeContent(absolutePath);
        return this;
    }

    /**
     * Gets a snapshot of the content as a map.
     * The key is the absolute path of the content.
     *
     * @return A snapshot of the current content area, indexed by the absolute path of the content.
     */
    @Override
    public ContentMap<ImmutableContent> getContentMapSnapshot()
    {
        // Create a new map:
        ContentMap<ImmutableContent> snapshot = new ContentMap<>();

        // Copy the content across:
        snapshot.putAll(this.contents);

        return snapshot;
    }

    /**
     * Gets a snapshot of the content as a list.
     *
     * @return A snapshot of the current content area.
     */
    @Override
    public ContentList<ImmutableContent> getContentListSnapshot()
    {
        // Create a new list:
        ContentList<ImmutableContent> snapshot = new ContentList<>();

        // Copy the content across:
        snapshot.addAll(this.contents.values());

        return snapshot;
    }

    public Stream<ImmutableContent> contentStream() {
        return this.contents.values().stream();
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

        // Unfreeze the content
        this.frozen = false;

        // Remove the content
        this.contents.clear();
    }

}
