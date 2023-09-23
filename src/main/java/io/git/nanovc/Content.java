package io.git.nanovc;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Content at a specific path in an area.
 * This would correspond to an actual file in a traditional git repository.
 */
public interface Content
{

    /**
     * The absolute path of this content in the repo.
     */
    String getAbsolutePath();

    /**
     * Gets a new clone of the content.
     * A new copy of the byte array is created each time this method is called.
     * @return A new copy of the content byte array. If the content is null then you get an empty array.
     */
    byte[] getCloneOfContentAsByteArray();

    /**
     * Gets a new clone of the content.
     * A new copy of the list is created each time this method is called.
     * @return A new list of the content. If the content is null then you get an empty list.
     */
    List<Byte> getCloneOfContentAsList();

    /**
     * Gets the content as a byte buffer.
     * @return A new byte buffer for the content. If the content is null then you get a byte buffer with 0 capacity.
     */
    ByteBuffer getContentByteBuffer();

}
