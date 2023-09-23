package io.git.nanovc;

import java.util.HashMap;

/**
 * A collection of repository objects.
 * They could be blobs, trees or commits.
 * The repo objects are indexed by the SHA-1 hash.
 */
public class RepoObjectStore
{
    /**
     * The map of ALL the Repo Objects.
     * The SHA-1 hash is the key.
     */
    public final HashMap<String, RepoObject> map = new HashMap<>();

    /**
     * The index of Repo Objects.
     * The first key is the first 2 characters of the hash.
     * The second key is the remaining 38 characters of the hash.
     * The value is the Repo Object with that hash.
     */
    public final HashMap<String, HashMap<String, RepoObject>> index = new HashMap<>();

    /**
     * Puts the given repo object in the collection.
     * The repo object is indexed by its SHA-1 hash.
     *
     * If the object database already has content with the given hash then it is not updated and the given repoObject instance is ignored.
     *
     * @param repoObject The repo object to put into the collection. Any existing object is NOT replaced if it has the same hash.
     * @return The repo object store so that it can have chained calls.
     */
    public RepoObjectStore put(RepoObject repoObject)
    {
        // Get the hash:
        final Hash hash = repoObject.hash;

        // Get the string value of the hash:
        final String hashValue = hash.value;

        // Check whether we already have this content:
        if (!map.containsKey(hashValue))
        {
            // We don't have this content yet.

            // Get the first 2 characters of the hash:
            String indexKey2 = hashValue.substring(0, 2);

            // Get the last 38 characters of the hash:
            String indexKey38 = hashValue.substring(2);

            // Get the first layer:
            HashMap<String, RepoObject> firstLayer = index.computeIfAbsent(indexKey2, s -> new HashMap<>());

            // Index the repo object in the first layer:
            firstLayer.put(indexKey38, repoObject);

            // Index the repo object in the map:
            map.put(hashValue, repoObject);
        }

        return this;
    }

    /**
     * Gets the repo object with the given hash.
     * @param hash The hash of the object to get.
     * @return The repo object with the given hash. Null if there is none with this hash.
     */
    public RepoObject get(Hash hash)
    {
        return this.map.get(hash.value);
    }

    /**
     * Gets the repo object with the given hash.
     * @param hashValue The SHA-1 hash of the object to get.
     * @return The repo object with the given hash. Null if there is none with this hash.
     */
    public RepoObject get(String hashValue)
    {
        return this.map.get(hashValue);
    }

    /**
     * Removes the given repo object from the collection.
     * The repo object is indexed by its SHA-1 hash.
     *
     * @param repoObject The repo object to put into the collection.
     * @return The repo object store so that it can have chained calls.
     */
    public RepoObjectStore remove(RepoObject repoObject)
    {
        // Get the hash:
        final Hash hash = repoObject.hash;
        return remove(hash);
    }

    /**
     * Removes the given repo object from the collection.
     * The repo object is indexed by its SHA-1 hash.
     *
     * @param hash The hash of the object to remove.
     * @return The repo object store so that it can have chained calls.
     */
    public RepoObjectStore remove(Hash hash)
    {
        // Get the string value of the hash:
        final String hashValue = hash.value;
        return remove(hashValue);
    }

    /**
     * Removes the given repo object from the collection.
     * The repo object is indexed by its SHA-1 hash.
     *
     * @param hashValue The SHA-1 hash value of the repo object to remove.
     * @return The repo object store so that it can have chained calls.
     */
    public RepoObjectStore remove(String hashValue)
    {
        // Check whether we even have this object:
        RepoObject existingObject = this.map.remove(hashValue);
        if (existingObject != null)
        {
            // We had an existing repo object.

            // Get the first 2 characters of the hash:
            String indexKey2 = hashValue.substring(0, 2);

            // Get the last 38 characters of the hash:
            String indexKey38 = hashValue.substring(2);

            // Get the first layer:
            HashMap<String, RepoObject> firstLayer = index.computeIfAbsent(indexKey2, s -> new HashMap<>());

            // Remove the repo object from the first layer:
            firstLayer.remove(indexKey38);

            // Check whether we can clean up this layer:
            if (firstLayer.size() == 0)
            {
                // We no longer have any repo objects in this layer.
                // Remove this layer:
                this.index.remove(indexKey2);
            }
        }

        return this;
    }

    /**
     * Clears the Object Store of all it's objects
     * This will clear the map and the index of all their entries
     */
    public void clear() {
        map.clear();
        index.clear();
    }
}
