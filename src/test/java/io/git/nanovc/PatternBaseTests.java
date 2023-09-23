package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the base pattern functionality.
 */
public class PatternBaseTests extends NanoVersionControlTestsBase
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
    public void test_createRegex()
    {
        Assert.assertEquals("", PatternBase.createRegex("").pattern());

        Assert.assertEquals("/a", PatternBase.createRegex("a").pattern());
        Assert.assertEquals("/[^\\/]*", PatternBase.createRegex("*").pattern());
        Assert.assertEquals("/.*", PatternBase.createRegex("**").pattern());
        Assert.assertEquals("/a[^\\/]*", PatternBase.createRegex("a*").pattern());
        Assert.assertEquals("/a.*", PatternBase.createRegex("a**").pattern());
        Assert.assertEquals("/[^\\/]*a", PatternBase.createRegex("*a").pattern());
        Assert.assertEquals("/.*a", PatternBase.createRegex("**a").pattern());

        Assert.assertEquals("/a/", PatternBase.createRegex("a/").pattern());
        Assert.assertEquals("/a/[^\\/]*", PatternBase.createRegex("a/*").pattern());
        Assert.assertEquals("/a/.*", PatternBase.createRegex("a/**").pattern());
        Assert.assertEquals("/[^\\/]*/.*", PatternBase.createRegex("*/**").pattern());
        Assert.assertEquals("/.*/.*", PatternBase.createRegex("**/**").pattern());
        Assert.assertEquals("/.*[^\\/]*", PatternBase.createRegex("***").pattern());
        Assert.assertEquals("/.*.*", PatternBase.createRegex("****").pattern());

        Assert.assertEquals("/a/", PatternBase.createRegex("/a/").pattern());
        Assert.assertEquals("/a/[^\\/]*", PatternBase.createRegex("/a/*").pattern());
        Assert.assertEquals("/a/.*", PatternBase.createRegex("/a/**").pattern());
        Assert.assertEquals("/[^\\/]*/.*", PatternBase.createRegex("/*/**").pattern());
        Assert.assertEquals("/.*/.*", PatternBase.createRegex("/**/**").pattern());
        Assert.assertEquals("/.*[^\\/]*", PatternBase.createRegex("/***").pattern());
        Assert.assertEquals("/.*.*", PatternBase.createRegex("/****").pattern());

        Assert.assertEquals("/a\\.txt", PatternBase.createRegex("a.txt").pattern());
        Assert.assertEquals("/a\\.txt", PatternBase.createRegex("/a.txt").pattern());
        Assert.assertEquals("/a\\.txt/", PatternBase.createRegex("a.txt/").pattern());
        Assert.assertEquals("/[^\\/]*/a\\.txt", PatternBase.createRegex("*/a.txt").pattern());
        Assert.assertEquals("/.*/a\\.txt", PatternBase.createRegex("**/a.txt").pattern());
        Assert.assertEquals("/.*/.*\\.txt", PatternBase.createRegex("**/**.txt").pattern());
        Assert.assertEquals("/.*/[^\\/]*\\.txt", PatternBase.createRegex("**/*.txt").pattern());
    }
}
