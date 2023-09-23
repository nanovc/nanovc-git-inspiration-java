package io.git.nanovc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * This is a tree, which solves the problem of storing the blob name and path and also allows you to store a group of
 * blobs together.
 * The repository stores content in a manner similar to a UNIX filesystem, but a bit simplified.
 * All the content is stored as tree and blob objects, with trees corresponding to UNIX directory entries and blobs
 * corresponding more or less to inodes or file contents.
 * A single tree object contains one or more tree entries, each of which contains a SHA-1 pointer to a blob or subtree
 * with its associated mode, type, and filename.
 * <p>
 * https://git-scm.com/book/en/v2/Git-Internals-Git-Objects
 */
public class Tree extends RepoObject
{

    /**
     * The entries in this tree.
     */
    public TreeEntryCollection entries = new TreeEntryCollection();

    /**
     * The type of the object for nano version control.
     *
     * @return The type of the object for nano version control.
     */
    @Override
    public ObjectType getObjectType()
    {
        return ObjectType.TREE;
    }

    /**
     * Writes the content of this repo object into the stream.
     *
     * @param outputStream The output stream to write to.
     */
    @Override
    public void writeContentToStream(DataOutputStream outputStream) throws IOException
    {
        // Write the number of entries:
        outputStream.writeInt(this.entries.size());

        // Write each entry:
        for (TreeEntry entry : this.entries)
        {
            // Write the entry type:
            switch (entry.objectType)
            {
                case BLOB:
                    outputStream.writeByte('b');
                    break;
                case COMMIT:
                    outputStream.writeByte((byte) 'c');
                    break;
                case TREE:
                    outputStream.writeByte((byte) 't');
                    break;
                default:
                    outputStream.writeByte((byte) '?');
            }

            // Write the name:
            outputStream.writeUTF(entry.name);

            // Write the hash:
            outputStream.writeUTF(entry.hashValue);
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
        // Read the number of entries:
        int entryCount = inputStream.readInt();

        // Clear the current entries:
        this.entries.clear();

        // Read out the entries:
        for (int i = 0; i < entryCount; i++)
        {
            // Read the object type:
            byte objectTypeByte = inputStream.readByte();

            // Determine the object type:
            ObjectType objectType;
            switch (objectTypeByte)
            {
                case (byte) 'b':
                    objectType = ObjectType.BLOB;
                    break;

                case (byte) 'c':
                    objectType = ObjectType.COMMIT;
                    break;

                case (byte) 't':
                    objectType = ObjectType.TREE;
                    break;

                default:
                    objectType = null;
            }

            // Read the name:
            String name = inputStream.readUTF();

            // Read out the hash value:
            String hashValue = inputStream.readUTF();

            // Create the tree entry:
            TreeEntry entry = new TreeEntry();
            entry.objectType = objectType;
            entry.name = name;
            entry.hashValue = hashValue;

            // Save the entry:
            this.entries.add(entry);
        }
    }

    @Override
    public String toString()
    {
        int entryCount = this.entries == null ? 0 : this.entries.size();
        String initial = "";
        if (this.hash == null || this.hash.value == null || this.hash.value.isEmpty())
        {
            initial = String.format("TREE with %,d Entr%s", entryCount, entryCount == 1 ? "y" : "ies");
        }
        else
        {
            initial = String.format("TREE with %,d Entr%s -> %s", entryCount, entryCount == 1 ? "y" : "ies", this.hash.value);
        }
        if (entryCount != 0) {
            String entriesString = entries.stream()
                    .map(entry -> entry.toString())
                    .collect(Collectors.joining(System.lineSeparator(), "["+ System.lineSeparator(),  System.lineSeparator() + "]"));
            return initial.concat(" - ").concat(entriesString);
        } else {
            return initial;
        }
    }
}
