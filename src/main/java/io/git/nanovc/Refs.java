package io.git.nanovc;

/**
 * The refs directory stores pointers into commit objects in that data (branches).
 */
public class Refs
{
    /**
     * The list of heads in the repo.
     */
    public HashReferenceCollection heads = new HashReferenceCollection();

    /**
     * The list of tags in the repo.
     */
    public HashReferenceCollection tags = new HashReferenceCollection();
}
