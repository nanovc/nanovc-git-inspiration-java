package io.git.nanovc.examples.git_reference;

import io.git.nanovc.*;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Tests branches in {@link Repo}'s.
 *
 * A good reference is here:
 * https://git-scm.com/book/en/v2/Git-Branching-Branches-in-a-Nutshell
 */
public class Git_Branches extends NanoVersionControlTestsBase
{

    /**
     * Creates a default repo for these tests.
     * @return The handler to the default repo.
     */
    private RepoHandler createDefaultRepo()
    {
        // Create some default content for this example:
        //#region Repo Content Creation
        // Create a new manager to interact with a repo:
        RepoHandler repoHandler = NanoVersionControl.newHandler();
        PorcelainCommands git = repoHandler.asPorcelainCommands();

        // Initialise a new repo:
        Repo repo = git.init();

        // Set the author and committer on the manager:
        repoHandler.setAuthorAndCommitter("Luke Machowski");

        // Override the current time so that commits have predictable results:
        ZonedDateTime nowOverride = ZonedDateTime.of(2017, 5, 1, 8, 0, 0, 0, ZoneId.of("GMT+2"));
        repoHandler.setNowOverride(nowOverride);

        // Modify, stage  and commit the file:
        git.putWorkingAreaContent("/hello.c", bytes("hello version 1"));
        git.addAll(true);
        git.commitAll("commit 1", false);

        // Modify, stage  and commit the file:
        git.putWorkingAreaContent("/hello.c", bytes("hello version 2"));
        git.addAll(true);
        git.commitAll("commit 2", false);

        // Modify, stage and commit the file:
        git.putWorkingAreaContent("/hello.c", bytes("hello version 3"));
        git.addAll(true);
        git.commitAll("commit 3", false);

        //#endregion
        return repoHandler;
    }

    /**
     * Tests that we can create branches.
     */
    @Test
    public void testBranchCreation()
    {
        // Create the default repo:
        RepoHandler repoHandler = createDefaultRepo();
        PorcelainCommands git = repoHandler.asPorcelainCommands();

        // Create branches:
        git.branch("new branch");
        Assert.assertNotNull(repoHandler.repo.database.refs.heads.getReference("new branch"));
    }

    /**
     * Tests that we can delete branches.
     */
    @Test
    public void testBranchDeletion()
    {
        // Create the default repo:
        RepoHandler repoHandler = createDefaultRepo();
        PorcelainCommands git = repoHandler.asPorcelainCommands();

        // Create branches:
        git.branch("new branch");
        Assert.assertNotNull(repoHandler.repo.database.refs.heads.getReference("new branch"));

        // Delete Branches:
        git.branch_delete("new branch");
        Assert.assertNull(repoHandler.repo.database.refs.heads.getReference("new branch"));
    }

    /**
     * Tests that the system throws exceptions as expected when we checkout branches that don't exist.
     */
    @Test
    public void testBranchCheckoutForNonExistentBranches()
    {
        // Create the default repo:
        RepoHandler repoHandler = createDefaultRepo();
        PorcelainCommands git = repoHandler.asPorcelainCommands();

        try
        {
            // Try checkout a branch that doesn't exist:
            git.checkout("BAD BRANCH");
            Assert.fail("We expected an exception when checking out a branch that doesn't exist.");
        }
        catch (NanoRuntimeException ex)
        {
            // Make sure the message is as expected:
            Assert.assertEquals("A reference (branch) called 'BAD BRANCH' was not found. Make sure to pass in a valid reference name that already exists, a SHA1 hash of a commit or HEAD for the current checkout.", ex.getMessage());
        }
    }

    /**
     * This example demonstrates the example for git-checkout
     *
     * The original example is documented here:
     * https://git-scm.com/docs/git-checkout
     */
    @Test
    public void git_checkout_example()
    {
        // Create the default repo:
        RepoHandler repoHandler = createDefaultRepo();
        PorcelainCommands git = repoHandler.asPorcelainCommands();

        //> Start development from a known tag
        // https://git-scm.com/docs/git-branch#git-branch-Startdevelopmentfromaknowntag
        //> $ git clone git://git.kernel.org/pub/scm/.../linux-2.6 my2.6
        //> $ cd my2.6
        //> $ git branch my2.6.14 v2.6.14   (1)
        git.branch("my2.6.14");

        // Make sure that the branch is there:
        Assert.assertNotNull(repoHandler.repo.database.refs.heads.getReference("my2.6.14"));

        // Make sure that we didn't switch yet:
        Assert.assertEquals("master", repoHandler.repo.database.HEAD.referenceName);

        //> $ git checkout my2.6.14
        git.checkout("my2.6.14");

        // Make sure that we switched:
        Assert.assertEquals("my2.6.14", repoHandler.repo.database.HEAD.referenceName);

        //> 1. This step and the next one could be combined into a single step with "checkout -b my2.6.14 v2.6.14".
        // NOTE: We don't support this yet.


        //> Delete an unneeded branch
        // https://git-scm.com/docs/git-branch#git-branch-Deleteanunneededbranch
        //> $ git clone git://git.kernel.org/.../git.git my.git
        //> $ cd my.git
        git.branch("origin/todo");

        // Make sure that the branch is there:
        Assert.assertNotNull(repoHandler.repo.database.refs.heads.getReference("origin/todo"));

        //> $ git branch -d -r origin/todo origin/html origin/man   (1)
        git.branch_delete("origin/todo");

        // Make sure that the branch has been removed:
        Assert.assertNull(repoHandler.repo.database.refs.heads.getReference("origin/todo"));

        //> $ git branch -D test                                    (2)
        //> 1. Delete the remote-tracking branches "todo", "html" and "man". The next fetch or pull will create them again unless you configure them not to. See git-fetch[1].
        //>
        //> 2. Delete the "test" branch even if the "master" branch (or whichever branch is currently checked out) does not have all commits from the test branch.
        //>


    }

}
