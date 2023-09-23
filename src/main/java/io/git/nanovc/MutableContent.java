package io.git.nanovc;

/**
 * Content at a specific path in a content area.
 * This would correspond to an actual file in a traditional git repository.
 * The Content object is designed so that you can keep a reference to it and update the byte[] content without needing
 * to go through the API.
 *
 * This class implies that the reference to the byte[] content is mutable,
 * you should never assume that you can modify the contents of the byte array unless you are in control of that.
 */
public class MutableContent extends ContentBase
{
    /**
     * The absolute path of this content in the repo.
     * The path can be changed to "move" the content to the new path.
     * This means that you can avoid the need to go through the API if you want to avoid it.
     */
    public String absolutePath;

    /**
     * The payload content to store.
     * The reference to the byte array is mutable and can be changed to point to the new content.
     * You should never modify the contents of the byte array unless you are in control of it.
     * This means that you can avoid the need to go through the API if you want to avoid it.
     */
    public byte[] content;

    /**
     * Creates content at the given absolute path.
     * @param absolutePath The absolute path in the repo where the content belongs.
     * @param content The content to store.
     */
    public MutableContent(String absolutePath, byte... content)
    {
        this.absolutePath = absolutePath;
        this.content = content;
    }

    /**
     * Creates new content.
     * You must set the path and content yourself.
     */
    public MutableContent()
    {
    }

    /**
     * The absolute path of this content in the repo.
     */
    @Override
    public String getAbsolutePath()
    {
        return this.absolutePath;
    }

    /**
     * Gets the internal byte array content.
     *
     * @return The actual content being wrapped.
     */
    @Override
    protected byte[] getContent()
    {
        return this.content;
    }
}
