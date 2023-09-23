package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Nano Version Control repository engine.
 */
public class RepoEngineTests extends NanoVersionControlTestsBase
{

    /**
     * Tests the ability to stage content directly without using the working area.
     * This is useful when you don't want to preserve the content in the working area.
     */
    @Test
    public void DirectStaging()
    {
        // Create the engine:
        RepoHandler manager = NanoVersionControl.newHandler();

        // Create a new repository:
        Repo repo = manager.init();

        // Stage some content:
        manager.stage("/ahdd", (byte)123);

        // Make sure the content was staged:
        Assert.assertEquals(1, repo.stagingArea.contents.size());
        Assert.assertArrayEquals(new byte[] {123}, repo.stagingArea.getContent("/ahdd").content);
    }



}
