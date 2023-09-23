package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the plumbing commands for the Nano Version Control framework.
 */
public class PlumbingCommandsTests extends NanoVersionControlTestsBase
{
    /**
     * Tests creating, reading, updating and deleting content in the working area.
     */
    @Test
    public void WorkingAreaCRUD()
    {
        // Create a new repo:
        RepoHandler repoManager = NanoVersionControl.newHandler();

        // Initialise the repository:
        repoManager.init();

        // Get access to its plumbing commands:
        PlumbingCommands plumbingCommands = repoManager.asPlumbingCommands();

        // Define some content:
        byte[] helloBytesV1 = "Hello World".getBytes();

        // Add some content:
        Hash helloHashV1 = plumbingCommands.hash_object_write_blob(helloBytesV1);
        Assert.assertNotNull(helloHashV1);
        Assert.assertEquals("ff31809acd7e6662279e013511a68523e9ae27dd", helloHashV1.value);

        // Get the content back:
        RepoObject helloRepoObjectV1 = plumbingCommands.cat_file(helloHashV1);
        Assert.assertNotNull(helloRepoObjectV1);
        Assert.assertEquals(helloHashV1.value, helloRepoObjectV1.hash.value);
        Assert.assertTrue(helloRepoObjectV1 instanceof Blob);
        Blob helloBlobV1 = (Blob)helloRepoObjectV1;
        Assert.assertArrayEquals(helloBytesV1, helloBlobV1.content);

        // Try hashing an existing blob:
        Hash helloHashV1FromBlob = plumbingCommands.hash_object(helloBlobV1);
        Assert.assertNotSame(helloHashV1, helloHashV1FromBlob);
        Assert.assertSame(helloHashV1, helloBlobV1.hash); // We don't expect the hash to have been updated.
    }



}
