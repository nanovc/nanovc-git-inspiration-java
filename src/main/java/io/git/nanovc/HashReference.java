package io.git.nanovc;

/**
 * A reference to an object with the given hash.
 */
public class HashReference
{
    /**
     * The name of the reference.
     * eg: master
     */
    public String name;

    /**
     * The hash of the object that this reference is referencing.
     * It usually references a commit.
     */
    public Hash hash;

    /**
     * Creates a new reference to an object with the given hash.
     * @param name The name of the reference. eg: master
     * @param hash The hash of the object being referenced, usually a commit.
     */
    public HashReference(String name, String hash)
    {
        this.name = name;
        this.hash = new Hash();
        this.hash.value = hash;
    }

    /**
     * Creates a new reference.
     * You need to set the name and hash.
     */
    public HashReference()
    {
    }

    @Override
    public String toString()
    {
        return String.format("%s -> %s", name == null ? "?" : name, (hash == null || hash.value == null) ? "?" : hash.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashReference that = (HashReference) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return hash != null ? hash.equals(that.hash) : that.hash == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
    }
}
