package io.git.nanovc;

/**
 * A symbolic reference to another reference.
 * eg: master
 */
public class SymbolicReference
{
    /**
     * The other reference that is being referenced.
     * eg: master
     */
    public String referenceName;

    /**
     * @param referenceName The name of the reference (branch) being referenced.
     */
    public SymbolicReference(String referenceName)
    {
        this.referenceName = referenceName;
    }

    /**
     * Creates a symbolic reference.
     * You need to set the reference name.
     */
    public SymbolicReference()
    {
    }
}
