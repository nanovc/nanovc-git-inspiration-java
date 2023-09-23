package io.git.nanovc;

import java.nio.ByteBuffer;

/**
 * Content at a specific path in a content area.
 * This would correspond to an actual file in a traditional git repository.
 * The Content object is designed so that you cannot modify it because it represents content in a content area
 * that must not be modified externally. For example, this is used in {@link MutableContentArea} and {@link Repo#committedArea}
 *
 * This class implies that the reference to the byte[] content is mutable,
 * you should never assume that you can modify the contents of the byte array unless you are in control of that.
 */
public class ImmutableContent extends ContentBase
{
    /**
     * The absolute path of this content in the repo.
     * The path can be changed to "move" the content to the new path.
     * This means that you can avoid the need to go through the API if you want to avoid it.
     */
    private final String absolutePath;

    /**
     * The payload content to store.
     * The reference to the byte array is mutable and can be changed to point to the new content.
     * You should never modify the contents of the byte array unless you are in control of it.
     * This means that you can avoid the need to go through the API if you want to avoid it.
     */
    private final byte[] content;

    /**
     * Creates content at the given absolute path.
     * @param absolutePath The absolute path in the repo where the content belongs.
     * @param content The content to store.
     */
    public ImmutableContent(String absolutePath, byte... content)
    {
        this.absolutePath = absolutePath;
        this.content = content;
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

    /**
     * Gets the content as a byte buffer.
     * @return A new byte buffer for the content. If the content is null then you get a byte buffer with 0 capacity.
     */
    @Override
    public ByteBuffer getContentByteBuffer()
    {
        // Get a read only byte buffer:
        // http://stackoverflow.com/questions/5480070/immutable-container-for-a-byte-that-supports-subsequences-like-string-substri
        // http://stackoverflow.com/a/5480109/231860
        return super.getContentByteBuffer().asReadOnlyBuffer();
    }
}
