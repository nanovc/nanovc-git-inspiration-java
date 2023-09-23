package io.git.nanovc;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An object in the repository.
 */
public abstract class RepoObject
{
    /**
     * The hash for this object.
     * This hash is used to access this object in the database.
     */
    public Hash hash;

    /**
     * Creates a new repo object.
     * You still need to set the hash.
     */
    public RepoObject()
    {
    }

    /**
     * Creates a new repo object with the given hash.
     *
     * @param hash The hash for the repo object.
     */
    public RepoObject(Hash hash)
    {
        this.hash = hash;
    }

    /**
     * The type of the object for nano version control.
     *
     * @return The type of the object for nano version control.
     */
    public abstract ObjectType getObjectType();

    /**
     * Writes the content of this repo object into the stream.
     *
     * @param outputStream The output stream to write to.
     */
    public abstract void writeContentToStream(DataOutputStream outputStream) throws IOException;

    /**
     * Reads the content of this repo object out of the stream.
     *
     * @param inputStream The input stream to read from.
     */
    public abstract void readContentFromStream(DataInputStream inputStream) throws IOException;

    /**
     * Gets a byte array of the content for this repo object.
     * It is assumed that the returned byte array should not be modified externally.
     *
     * @return The byte array for the content of this repo object.
     */
    public byte[] getByteArray()
    {
        // Create a stream in memory:
        // http://stackoverflow.com/questions/664389/byte-array-of-unknown-length-in-java
        // http://stackoverflow.com/a/664394/231860
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)
        )
        {
            // Write out the content:
            writeContentToStream(dataOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
