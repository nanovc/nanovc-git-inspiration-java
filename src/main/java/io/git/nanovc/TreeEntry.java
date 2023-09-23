package io.git.nanovc;

/**
 * An Entry in a {@link Tree}.
 * This is what associates a name and path with a blob or another tree.
 */
public class TreeEntry
{

    public TreeEntry() {
    }

    public TreeEntry(ObjectType objectType, String hashValue, String name) {
        this.objectType = objectType;
        this.hashValue = hashValue;
        this.name = name;
    }

    /**
     * The type of repo object being referenced by this tree entry.
     */
    public ObjectType objectType;

    /**
     * The SHA-1 hash value of the repo object that is being referenced by this tree entry.
     */
    public String hashValue;

    /**
     * The name of the tree entry.
     * If the {@link #objectType} is {@link ObjectType#BLOB} then the name represents the name of the file for that blob.
     * If the {@link #objectType} is {@link ObjectType#TREE} then the name represents the name of the directory for that sub tree.
     */
    public String name;

    @Override
    public String toString()
    {
        return String.format("%s : %s -> %s", name, objectType, hashValue);
    }
}
