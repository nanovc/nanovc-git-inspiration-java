package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the store of repo objects.
 * This corresponds to the objects directory structure in the .git database.
 */
public class RepoObjectStoreTests extends NanoVersionControlTestsBase
{
    /**
     * Tests creating, reading, updating and deleting repo objects objects directory.
     */
    @Test
    public void RepoObjectStoreCRUD()
    {
        // Create the collection:
        RepoObjectStore store = new RepoObjectStore();

        // Create a Repo Manager so we can hash objects:
        RepoEngine repoEngine = new RepoEngine();

        // Define some content:
        byte[] helloBytesV1 = "Hello World".getBytes();

        // Get the hash for the object:
        final Hash helloHashV1 = repoEngine.hash_object(ObjectType.BLOB, helloBytesV1);
        String helloHashStringV1 = helloHashV1.value;

        // Create a Blob:
        Blob helloBlobV1 = new Blob(helloHashV1, helloBytesV1);

        // Store the blob:
        store.put(helloBlobV1);
        Assert.assertSame(helloBlobV1, store.get(helloHashV1));
        Assert.assertSame(helloBlobV1, store.get(helloHashStringV1));
        Assert.assertSame(helloBlobV1, store.map.get(helloHashStringV1));
        Assert.assertSame(helloBlobV1, store.index.get(helloHashStringV1.substring(0,2)).get(helloHashStringV1.substring(2)));

        // Make sure we only have one item in the store:
        Assert.assertEquals(1, store.map.size());
        Assert.assertEquals(1, store.index.size());
        Assert.assertEquals(1, store.index.get(helloHashStringV1.substring(0,2)).size());



        // Define some content:
        byte[] helloBytesV2 = "Hello World V2".getBytes();

        // Get the hash for the object:
        final Hash helloHashV2 = repoEngine.hash_object(ObjectType.BLOB, helloBytesV2);
        String helloHashStringV2 = helloHashV2.value;

        // Create a Blob:
        Blob helloBlobV2 = new Blob(helloHashV2, helloBytesV2);

        // Store the blob:
        store.put(helloBlobV2);
        Assert.assertSame(helloBlobV2, store.get(helloHashV2));
        Assert.assertSame(helloBlobV2, store.get(helloHashStringV2));
        Assert.assertSame(helloBlobV2, store.map.get(helloHashStringV2));
        Assert.assertSame(helloBlobV2, store.index.get(helloHashStringV2.substring(0,2)).get(helloHashStringV2.substring(2)));

        // Make sure we only have two items in the store:
        Assert.assertEquals(2, store.map.size());
        Assert.assertEquals(2, store.index.size());
        Assert.assertEquals(1, store.index.get(helloHashStringV1.substring(0,2)).size());
        Assert.assertEquals(1, store.index.get(helloHashStringV2.substring(0,2)).size());


        // Remove some content:
        store.remove(helloBlobV2);
        Assert.assertNull(store.get(helloHashV2));
        Assert.assertNull(store.get(helloHashStringV2));
        Assert.assertNull(store.map.get(helloHashStringV2));
        Assert.assertNull(store.index.get(helloHashStringV2.substring(0,2)));

        // Make sure we only have one item in the store:
        Assert.assertEquals(1, store.map.size());
        Assert.assertEquals(1, store.index.size());
        Assert.assertEquals(1, store.index.get(helloHashStringV1.substring(0,2)).size());
    }



}
