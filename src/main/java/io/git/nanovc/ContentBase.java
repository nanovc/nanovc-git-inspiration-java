package io.git.nanovc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Content at a specific path in an area.
 * This would correspond to an actual file in a traditional git repository.
 * This is the base class for {@link MutableContent} and
 */
public abstract class ContentBase implements Content
{

    /**
     * The absolute path of this content in the repo.
     */
    public abstract String getAbsolutePath();

    /**
     * Gets the internal byte array content.
     * @return The actual content being wrapped.
     */
    protected abstract byte[] getContent();

    public String getContentAsString()
    {
        return new String(getContent());
    }

    /**
     * Gets a new clone of the content.
     * A new copy of the byte array is created each time this method is called.
     * @return A new copy of the content byte array. If the content is null then you get an empty array.
     */
    public byte[] getCloneOfContentAsByteArray()
    {
        // Make sure we have content:
        byte[] content = getContent();
        if (content == null || content.length == 0)
        {
            // We do not have any content.
            return new byte[0];
        }
        else
        {
            // We have content.

            // Copy the array:
            return Arrays.copyOf(content, content.length);
        }
    }

    /**
     * Gets a new clone of the content.
     * A new copy of the list is created each time this method is called.
     * @return A new list of the content. If the content is null then you get an empty list.
     */
    public List<Byte> getCloneOfContentAsList()
    {
        // Make sure we have content:
        byte[] content = getContent();
        if (content == null || content.length == 0)
        {
            // We do not have any content.
            return Collections.EMPTY_LIST;
        }
        else
        {
            // We have content.

            // Create the list:
            List<Byte> byteList = new ArrayList<>();
            for (int i = 0; i < content.length; i++)
            {
                byteList.add(content[i]);
            }
            return byteList;
        }
    }

    /**
     * Gets the content as a byte buffer.
     * @return A new byte buffer for the content. If the content is null then you get a byte buffer with 0 capacity.
     */
    public ByteBuffer getContentByteBuffer()
    {
        // Make sure we have content:
        byte[] content = getContent();
        if (content == null || content.length == 0)
        {
            // We do not have any content.
            return ByteBuffer.allocate(0);
        }
        else
        {
            // We have content.

            return ByteBuffer.wrap(content);
        }
    }

    /**
     * The string value of this content.
     * @return The string for debugging this content.
     */
    @Override
    public String toString()
    {
        // Get the content:
        byte[] content = this.getContent();

        // Check whether we have content:
        if (content == null)
        {
            // We don't have content.
            return String.format("%s -> 0 bytes", this.getAbsolutePath());
        }
        else
        {
            // We do have content.
            int contentLength = content == null ? 0 : content.length;

            // Check if the content is very long:
            String contentString;
            if (contentLength > 1000)
            {
                // We have a very long string.

                // Get a smaller copy of the content:
                // https://stackoverflow.com/a/19237414/231860
                byte[] shortContent = Arrays.copyOf(content, 1000);

                // Get the string to display:
                contentString = new String(shortContent);

            }
            else
            {
                // Our content is short enough to display.

                // Get the string to display:
                contentString = new String(content);
            }
            // Now we have the string of content to display.
            return String.format("%s -> %,d byte%s:\n%s", this.getAbsolutePath(), contentLength, contentLength == 1 ? "" : "s", contentString);
        }
    }
}
