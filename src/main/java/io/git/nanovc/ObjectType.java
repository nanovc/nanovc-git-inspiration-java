package io.git.nanovc;

import java.nio.charset.StandardCharsets;

/**
 * Describes the types of objects that can be stored in the objects database.
 * https://git-scm.com/book/en/v2/Git-Internals-Git-Objects
 */
public enum ObjectType
{
    /**
     * The content of a file being stored in the repo.
     * {@link Blob}
     */
    BLOB,

    /**
     * A commit object stores information about who saved the snapshots,
     * when they were saved, and why they were saved.
     * {@link Commit}
     */
    COMMIT,

    /**
     * The tree solves the problem of storing the filename and also allows you to store a group of files together.
     * {@link Tree}
     */
    TREE;

    /**
     * The tag used in Hashes for this object type.
     */
    public final String hashTag;

    /**
     * The bytes used for
     */
    public final byte[] hashBytes;

    /**
     * Creates a new object type definition.
     */
    ObjectType()
    {
        this.hashTag = this.name().toLowerCase();
        this.hashBytes = this.hashTag.getBytes(StandardCharsets.US_ASCII);
    }
}
