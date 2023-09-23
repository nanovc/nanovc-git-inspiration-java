package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the base path functionality.
 */
public class PathBaseTests extends NanoVersionControlTestsBase
{
    /**
     * Tests that the delimiter is as expected.
     */
    @Test
    public void testPathDelimiter()
    {
        Assert.assertEquals("/", PathBase.DELIMITER);
    }

    /**
     * Tests that we detect absolute paths correctly.
     */
    @Test
    public void test_isAbsolute()
    {
        // Absolute Paths:
        Assert.assertTrue(PathBase.isAbsolute("/"));
        Assert.assertTrue(PathBase.isAbsolute("/ "));
        Assert.assertTrue(PathBase.isAbsolute("/a"));
        Assert.assertTrue(PathBase.isAbsolute("/a/b"));

        // Relative Paths:
        Assert.assertFalse(PathBase.isAbsolute(""));
        Assert.assertFalse(PathBase.isAbsolute(" /"));
    }
}
