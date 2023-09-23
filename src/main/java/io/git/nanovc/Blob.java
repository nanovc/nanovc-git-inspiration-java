package io.git.nanovc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The content being stored in version control.
 * This object type is called a blob.
 */
public class Blob extends RepoObject
{
    /**
     * The content for the blob.
     */
    public byte[] content;

    /**
     * Creates a new blob.
     * You need to still set the hash and content.
     */
    public Blob()
    {
    }

    /**
     * The type of the object for nano version control.
     *
     * @return The type of the object for nano version control.
     */
    @Override
    public ObjectType getObjectType()
    {
        return ObjectType.BLOB;
    }

    /**
     * Creates a new repo object with the given hash.
     *
     * @param hash    The hash for the blob.
     * @param content The content for the blob.
     */
    public Blob(Hash hash, byte... content)
    {
        super(hash);
        this.content = content;
    }

    /**
     * Creates a blob with the given content.
     * You still need to set the hash.
     *
     * @param content The content for the blob.
     */
    public Blob(byte... content)
    {
        this.content = content;
    }

    /**
     * Writes the content of this repo object into the stream.
     *
     * @param outputStream The output stream to write to.
     */
    @Override
    public void writeContentToStream(DataOutputStream outputStream) throws IOException
    {
        // Check whether we have content:
        if (this.content == null || this.content.length == 0)
        {
            // We don't have content.
            // Write the length of the content:
            outputStream.writeInt(0);
        }
        else
        {
            // We have content.
            // Write the length of the content:
            outputStream.writeInt(this.content.length);

            // Write the content:
            outputStream.write(this.content);
        }
    }

    /**
     * Reads the content of this repo object out of the stream.
     *
     * @param inputStream The input stream to read from.
     */
    @Override
    public void readContentFromStream(DataInputStream inputStream) throws IOException
    {
        // Read the length of the content:
        int length = inputStream.readInt();

        // Create an array for the content:
        this.content = new byte[length];

        // Check whether there is something to read:
        if (length > 0)
        {
            // Read out the content:
            inputStream.readFully(this.content);
        }
    }

    @Override
    public String toString()
    {
        int contentLength = this.content == null ? 0 : content.length;
        if (this.hash == null || this.hash.value == null || this.hash.value.isEmpty())
        {
            return String.format("BLOB : %,d byte%s", contentLength, contentLength == 1 ? "" : "s");
        }
        else
        {
            return String.format("BLOB : %,d byte%s -> %s", contentLength, contentLength == 1 ? "" : "s", this.hash.value);
        }
    }
}
