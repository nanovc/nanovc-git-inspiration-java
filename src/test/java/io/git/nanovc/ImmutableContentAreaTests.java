package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that the immutable content area behaves as expected.
 * An immutable content area would usually be the committed area in a git repository.
 */
public class ImmutableContentAreaTests extends NanoVersionControlTestsBase
{
    /**
     * Tests creating, reading, updating and deleting of content in an immutable content area (like the committed area).
     */
    @Test
    public void ImmutableContentArea_CRUD()
    {
        // Create the immutable content area:
        ImmutableContentArea area = new ImmutableContentArea();

        // Make sure the content area is not frozen initially:
        Assert.assertFalse(area.isFrozen());

        final String path = "/";

        // Make sure we have no content when we begin:
        Assert.assertEquals(0, area.getContentMapSnapshot().size());

        // Put some content into the working area:
        ImmutableContent createdContent = area.putContent(path, (byte)123);
        Assert.assertNotNull(createdContent);
        Assert.assertSame(createdContent, area.getContentMapSnapshot().getContent(path));
        Assert.assertEquals(1, area.getContentMapSnapshot().size());
        Assert.assertEquals(path, createdContent.getAbsolutePath());
        Assert.assertArrayEquals(new byte[] {(byte)123}, createdContent.getCloneOfContentAsByteArray());

        // Read the content:
        ImmutableContent readContent = area.getContent(path);
        Assert.assertNotNull(readContent);
        Assert.assertSame(readContent, area.getContentMapSnapshot().getContent(path));
        Assert.assertEquals(1, area.getContentMapSnapshot().size());
        Assert.assertSame(createdContent, readContent);
        Assert.assertEquals(path, createdContent.getAbsolutePath());
        Assert.assertArrayEquals(new byte[] {(byte)123}, createdContent.getCloneOfContentAsByteArray());

        // Update the content:
        ImmutableContent replacedContent = area.putContent(path, (byte)255);
        Assert.assertNotNull(replacedContent);
        Assert.assertSame(replacedContent, area.getContentMapSnapshot().getContent(path));
        Assert.assertNotSame(readContent, replacedContent);
        Assert.assertEquals(1, area.getContentMapSnapshot().size());
        Assert.assertEquals(path, replacedContent.getAbsolutePath());
        Assert.assertArrayEquals(new byte[] {(byte)255}, replacedContent.getCloneOfContentAsByteArray());

        // Delete the content:
        area.removeContent(path);
        Assert.assertEquals(0, area.getContentMapSnapshot().size());

        // Try to get non-existent content:
        ImmutableContent nullContent = area.getContent(path);
        Assert.assertNull(nullContent);

        // Freeze the content area:
        area.freeze();

        // Make sure the content area is frozen:
        Assert.assertTrue(area.isFrozen());

        // Try to modify the content:


        try
        {
            area.putContent(path, (byte)69);
            Assert.fail("Putting content in a frozen immutable store should have thrown an ImmutableContentModifiedException.");
        }
        catch (ImmutableContentModifiedException nanoException)
        {
            // We expect an exception telling us that we can't edit the content.
        }
        catch (Exception ex)
        {
            Assert.fail("Putting content in a frozen immutable store should have thrown an ImmutableContentModifiedException.");
        }



        try
        {
            area.putContent(createdContent);
            Assert.fail("Putting content in a frozen immutable store should have thrown an ImmutableContentModifiedException.");
        }
        catch (ImmutableContentModifiedException nanoException)
        {
            // We expect an exception telling us that we can't edit the content.
        }
        catch (Exception ex)
        {
            Assert.fail("Putting content in a frozen immutable store should have thrown an ImmutableContentModifiedException.");
        }



        try
        {
            area.removeContent(path);
            Assert.fail("Removing content from a frozen immutable store should have thrown an ImmutableContentModifiedException.");
        }
        catch (ImmutableContentModifiedException nanoException)
        {
            // We expect an exception telling us that we can't edit the content.
        }
        catch (Exception ex)
        {
            Assert.fail("Removing content from a frozen immutable store should have thrown an ImmutableContentModifiedException.");
        }

    }



}
