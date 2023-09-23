package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests repository patterns.
 * Patterns are used to define a range of paths in a {@link Repo}.
 */
public class RepoPatternTests extends NanoVersionControlTestsBase
{
    /**
     * Tests creation of repository patterns.
     */
    @Test
    public void PatternCreation()
    {
        RepoPattern repoPattern = RepoPattern.matching("**/*.json");
        Assert.assertNotNull(repoPattern);
    }

    /**
     * Tests matching of repo patterns against content.
     */
    @Test
    public void PatternMatching()
    {
        // Create the content:
        List<Content> content = new ArrayList<>();
        content.add(new MutableContent("/"));
        content.add(new MutableContent("/a"));
        content.add(new MutableContent("/a/1.json"));
        content.add(new MutableContent("/a/2.json"));
        content.add(new MutableContent("/a/b/3.json"));
        content.add(new MutableContent("/4.json"));
        content.add(new MutableContent("/5.json"));

        assertContentMatches(content, "/", "/");
        assertContentMatches(content, "*", "/,/a,/4.json,/5.json");
        assertContentMatches(content, "/*", "/,/a,/4.json,/5.json");
        assertContentMatches(content, "**", "/,/a,/a/1.json,/a/2.json,/a/b/3.json,/4.json,/5.json");
        assertContentMatches(content, "/**", "/,/a,/a/1.json,/a/2.json,/a/b/3.json,/4.json,/5.json");
        assertContentMatches(content, "*.json", "/4.json,/5.json");
        assertContentMatches(content, "/*.json", "/4.json,/5.json");
        assertContentMatches(content, "**.json", "/a/1.json,/a/2.json,/a/b/3.json,/4.json,/5.json");
        assertContentMatches(content, "**/*.json", "/a/1.json,/a/2.json,/a/b/3.json");
        assertContentMatches(content, "**/**.json", "/a/1.json,/a/2.json,/a/b/3.json");
        assertContentMatches(content, "/**/*.json", "/a/1.json,/a/2.json,/a/b/3.json");
        assertContentMatches(content, "/a/*.json", "/a/1.json,/a/2.json");
        assertContentMatches(content, "/a/**.json", "/a/1.json,/a/2.json,/a/b/3.json");
        assertContentMatches(content, "/a/b/*.json", "/a/b/3.json");
        assertContentMatches(content, "/*/b/*.json", "/a/b/3.json");
        assertContentMatches(content, "/**/b/*.json", "/a/b/3.json");
        assertContentMatches(content, "**/b/*.json", "/a/b/3.json");
        assertContentMatches(content, "**3.json", "/a/b/3.json");
        assertContentMatches(content, "**b**", "/a/b/3.json");
    }

    /**
     * Tests that the given repo pattern matches the content.
     * @param content The content to match.
     * @param globPattern The repo pattern to test.
     * @param expectedMatch The comma separated paths of the expected matches.
     */
    private void assertContentMatches(List<Content> content, String globPattern, String expectedMatch)
    {
        // Create the pattern:
        RepoPattern repoPattern = RepoPattern.matching(globPattern);

        // Get the matches:
        List<Content> matches = repoPattern.match(content);

        // Make sure the matches are as expected:
        Assert.assertEquals(expectedMatch, matches.stream().map(c -> c.getAbsolutePath()).collect(Collectors.joining(",")));
    }


}
