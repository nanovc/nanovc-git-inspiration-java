package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests repository paths.
 */
public class RepoPathTests extends NanoVersionControlTestsBase
{
    /**
     * Tests creation of repository paths.
     */
    @Test
    public void PathCreation()
    {
        RepoPath repoPath = new RepoPath("/");
        Assert.assertEquals("/", repoPath.path);
    }

    /**
     * Tests that we can resolve one path from another.
     */
    @Test
    public void ResolvingPaths()
    {
        RepoPath rootPath = new RepoPath("/");
        RepoPath pathR1 = rootPath.resolve("R1");
        Assert.assertEquals("/R1", pathR1.path);

        RepoPath pathR1S1 = pathR1.resolve("S1");
        Assert.assertEquals("/R1/S1", pathR1S1.path);

        RepoPath pathR2 = pathR1.resolve("/R2");
        Assert.assertEquals("/R2", pathR2.path);
    }

    /**
     * Tests that we can convert paths to strings.
     */
    @Test
    public void PathsToString()
    {
        RepoPath rootPath = new RepoPath("/");
        Assert.assertEquals("/", rootPath.path);
        Assert.assertEquals("/", rootPath.toString());
        Assert.assertEquals("/", rootPath.toAbsolutePath().toString());

        RepoPath pathR1 = rootPath.resolve("R1");
        Assert.assertEquals("/R1", pathR1.path);
        Assert.assertEquals("/R1", pathR1.toString());
        Assert.assertEquals("/R1", pathR1.toAbsolutePath().toString());

        RepoPath pathR1S1 = pathR1.resolve("S1");
        Assert.assertEquals("/R1/S1", pathR1S1.path);
        Assert.assertEquals("/R1/S1", pathR1S1.toString());
        Assert.assertEquals("/R1/S1", pathR1S1.toAbsolutePath().toString());

        RepoPath pathR2 = pathR1.resolve("/R2");
        Assert.assertEquals("/R2", pathR2.path);
        Assert.assertEquals("/R2", pathR2.toString());
        Assert.assertEquals("/R2", pathR2.toAbsolutePath().toString());
    }
}
