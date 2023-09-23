package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Mutable Content Area in a Nano Version Control repository.
 * A mutable content area could be the working area or the staging area.
 */
public class MutableContentAreaTests extends NanoVersionControlTestsBase
{
    /**
     * Tests creating, reading, updating and deleting of content in a content area (like the working, staging or committed area).
     */
    @Test
    public void Content_Area_CRUD()
    {
        // Create the working area:
        MutableContentArea workingArea = new MutableContentArea();

        final String path = "/";

        // Make sure we have no content when we begin:
        Assert.assertEquals(0, workingArea.contents.size());

        // Put some content into the working area:
        MutableContent createdContent = workingArea.putContent(path, (byte)123);
        Assert.assertNotNull(createdContent);
        Assert.assertSame(createdContent, workingArea.getContentMapSnapshot().getContent(path));
        Assert.assertEquals(1, workingArea.contents.size());
        Assert.assertEquals(path, createdContent.absolutePath);
        Assert.assertArrayEquals(new byte[] {(byte)123}, createdContent.content);
        Assert.assertNotSame(createdContent.content, createdContent.getCloneOfContentAsByteArray());

        // Read the content:
        MutableContent readContent = workingArea.getContent(path);
        Assert.assertNotNull(readContent);
        Assert.assertSame(readContent, workingArea.getContentMapSnapshot().getContent(path));
        Assert.assertEquals(1, workingArea.contents.size());
        Assert.assertSame(createdContent, readContent);
        Assert.assertEquals(path, createdContent.absolutePath);
        Assert.assertArrayEquals(new byte[] {(byte)123}, createdContent.content);

        // Update the content:
        MutableContent replacedContent = workingArea.putContent(path, (byte)255);
        Assert.assertNotNull(replacedContent);
        Assert.assertSame(replacedContent, workingArea.getContentMapSnapshot().getContent(path));
        Assert.assertSame(readContent, replacedContent); // We expect that the existing content got mutated with the new bytes.
        Assert.assertEquals(1, workingArea.contents.size());
        Assert.assertEquals(path, replacedContent.absolutePath);
        Assert.assertArrayEquals(new byte[] {(byte)255}, replacedContent.content);

        // Delete the content:
        Assert.assertEquals(0, workingArea.contents.indexOf(replacedContent));
        workingArea.removeContent(path);
        Assert.assertEquals(-1, workingArea.contents.indexOf(replacedContent));
        Assert.assertEquals(0, workingArea.contents.size());

        // Try to get non-existent content:
        ContentBase nullContent = workingArea.getContent(path);
        Assert.assertNull(nullContent);
    }



}
