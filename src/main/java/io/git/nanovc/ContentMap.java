package io.git.nanovc;

import java.util.HashMap;

/**
 * A map of content indexed by the absolute path of the content in the repo.
 * @param <TContent> The type of content that is being stored in this content map.
 */
public class ContentMap<TContent extends Content> extends HashMap<String, TContent>
{
    /**
     * Puts the given content into this map.
     * @param content The content to put into this map. It gets indexed by the content absolute path.
     */
    public void putContent(TContent content)
    {
        // Add it to our map:
        this.put(content.getAbsolutePath(), content);
    }

    /**
     * Gets the content at the given path.
     * @param absolutePath The absolute path in the repo of the content to get.
     * @return The content at the given path. Null if there is no content at the path.
     */
    public TContent getContent(String absolutePath)
    {
        return this.get(absolutePath);
    }

    /**
     * Removes content at the given path if there is content.
     * @param absolutePath The absolute path in the repo of the content to remove.
     */
    public void removeContent(String absolutePath)
    {
        this.remove(absolutePath);
    }
}
