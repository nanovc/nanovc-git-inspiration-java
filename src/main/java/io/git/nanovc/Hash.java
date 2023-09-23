package io.git.nanovc;

/**
 * A hash of data in the repository.
 * This is a 40-character checksum hash.
 * This is the SHA-1 hash – a checksum of the content you’re storing plus a header.
 */
public class Hash
{
    /**
     * The hash value for an object in the nano repository.
     * This is a 40-character checksum hash.
     */
    public String value;

    /**
     * Creates a new hash with the given hash value.
     * @param hashValue The hash value.
     */
    public Hash(String hashValue)
    {
        this.value = hashValue;
    }

    /**
     * Creates a new hash.
     * You need to set the hash value.
     */
    public Hash()
    {
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hash hash = (Hash) o;

        return value != null ? value.equals(hash.value) : hash.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
