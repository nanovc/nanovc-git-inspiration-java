package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Nano Version Control framework.
 */
public class NanoVersionControlTests extends NanoVersionControlTestsBase
{
    /**
     * Tests creation of the nano version control engine.
     */
    @Test
    public void NVCEngineCreation()
    {
        // Create a new repository:
        Repo repo = new Repo();

        // Make sure the repo has the basic repository structure:
        Assert.assertNotNull(repo.database);
        Assert.assertNotNull(repo.stagingArea);
        Assert.assertNotNull(repo.workingArea);
    }

    /**
     * Tests a simple commit.
     */
    @Test
    public void testSimpleCommitContent()
    {
        // Get a new engine for a repo:
        RepoHandler nano = NanoVersionControl.newHandler();

        // Create a new repository:
        Repo repo = nano.init();
        nano.setAuthorAndCommitter("test");

        // Add content to the working area:
        ContentBase rootContent = repo.workingArea.putContent("/", (byte) 123);

        // Add the changed content to the staging area:
        nano.addAll(true);

        Commit commit = nano.commitAll("test", true);
        Assert.assertNotNull(commit);
        Assert.assertNotNull(commit.hash.value);

    }

    /**
     * Tests a simple commit with no content
     */
    @Test
    public void testSimpleCommitNoContent()
    {
        // Get a new engine for a repo:
        RepoHandler nano = NanoVersionControl.newHandler();

        // Create a new repository:
        Repo repo = nano.init();
        nano.setAuthorAndCommitter("test");

        // Add the changed content to the staging area:
        nano.addAll(true);

        Commit commit = nano.commitAll("test", true);
        Assert.assertNotNull(commit);
        Assert.assertNotNull(commit.hash.value);

    }

}
