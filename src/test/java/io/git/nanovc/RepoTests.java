package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Nano Version Control framework.
 */
public class RepoTests extends NanoVersionControlTestsBase
{
    /**
     * Tests creating, reading, updating and deleting content in the working area.
     */
    @Test
    public void WorkingAreaCRUD()
    {
        // Create a new repo and only use our nano interface for simplicity:
        NanoCommands nano = NanoVersionControl.newHandler().asNanoCommands();

        // Initialise a new repo:
        Repo repo = nano.init();

        // NOTE: The Nano Repo is very dependant on referential integrity between objects.
        // This is intentional and by design.
        // We don't stop you from breaking things by fiddling with objects
        // but we do allow you to manipulate the internals if you know what you are doing.

        // Define some content:
        byte[] helloBytesV1 = "Hello World".getBytes();
        RepoPath helloPath = RepoPath.at("hello.txt"); // Implied context is root /
        final String helloPathString = helloPath.toAbsolutePath().toString();
        Assert.assertEquals("/hello.txt", helloPathString);
        Assert.assertEquals("hello.txt", helloPath.path);
        Assert.assertEquals("/hello.txt", helloPath.toAbsolutePath().path);

        // Put some content in the working area:
        MutableContent helloContentFromFirstPut = nano.putWorkingAreaContent(helloPath, helloBytesV1);
        Assert.assertNotNull(helloContentFromFirstPut);
        Assert.assertEquals(helloPathString, helloContentFromFirstPut.absolutePath);
        Assert.assertSame(helloBytesV1, helloContentFromFirstPut.content);

        // Update the content in the working area through the API:
        byte[] helloBytesV2 = "Hello World V2".getBytes();
        MutableContent helloContentFromSecondPut = nano.putWorkingAreaContent(helloPath, helloBytesV2);
        Assert.assertNotNull(helloContentFromSecondPut);

        // We expect that one Content object is updated at a given path when updating content:
        // This means that someone with a reference to the same Content object can just modify the bytes directly.
        Assert.assertSame(helloContentFromFirstPut, helloContentFromSecondPut);

        // Just call it helloContent since it's the same instance:
        MutableContent helloContent = helloContentFromSecondPut;

        // We don't expect the byte content to be the same:
        Assert.assertNotSame(helloBytesV1, helloContent.content);
        Assert.assertSame(helloBytesV2, helloContent.content);

        // Check the path:
        Assert.assertEquals(helloPathString, helloContent.absolutePath);

        // Make sure that we only have one piece of content so far:
        Assert.assertEquals(1, repo.workingArea.contents.size());

        // Make sure that we can access the mutable content without using the API:
        Assert.assertSame(helloContent, repo.workingArea.getContent(helloPath));
        Assert.assertSame(helloContent, repo.workingArea.getContent(helloPathString));
        Assert.assertSame(helloContent, repo.workingArea.contents.get(0));
        Assert.assertSame(helloContent, repo.workingArea.getContentListSnapshot().get(0));
        Assert.assertSame(helloContent, repo.workingArea.getContentMapSnapshot().get(helloPathString));
    }



}
