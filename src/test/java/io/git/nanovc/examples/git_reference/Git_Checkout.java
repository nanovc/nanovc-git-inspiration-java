package io.git.nanovc.examples.git_reference;

import io.git.nanovc.*;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This is a set of examples derived from the Git Reference Manual for the checkout command.
 *
 */
public class Git_Checkout extends NanoVersionControlTestsBase
{

    /**
     * This example demonstrates the example for git-checkout
     *
     * The original example is documented here:
     * https://git-scm.com/docs/git-checkout
     */
    @Test
    public void git_checkout_example()
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


        //> 1. The following sequence checks out the master branch,
        //> reverts the Makefile to two revisions back,
        //> deletes hello.c by mistake,
        //> and gets it back from the index.
        //>
        //> $ git checkout master             (1)
        //> $ git checkout master~2 Makefile  (2)
        //> $ rm -f hello.c
        //> $ git checkout hello.c            (3)
        //>

        //> 1.1. switch branch
        git.checkout("master");
        Assert.assertEquals("master", git.getCurrentBranchName());
        Assert.assertEquals("hello version 3", git.getWorkingAreaContent(RepoPath.at("hello.c")).getContentAsString());

        //> 1.2. take a file out of another commit
        git.checkout("master", -2);
        Assert.assertEquals("master", git.getCurrentBranchName());
        Assert.assertEquals("hello version 1", git.getWorkingAreaContent(RepoPath.at("hello.c")).getContentAsString());

        //> 1.3. restore hello.c from the index
        git.checkout_path(RepoPath.at("hello.c"));
        Assert.assertEquals("master", git.getCurrentBranchName());
        Assert.assertEquals("hello version 1", git.getWorkingAreaContent(RepoPath.at("hello.c")).getContentAsString());


        //> If you want to check out all C source files out of the index, you can say
        //>
        //> $ git checkout -- '*.c'
        //> Note the quotes around *.c. The file hello.c will also be checked out,
        // even though it is no longer in the working tree,
        // because the file globbing is used to match entries in the index
        // (not in the working tree by the shell).

        // Modify the contents of the file:
        git.putWorkingAreaContent(RepoPath.at("hello.c"), bytes("CHANGED"));
        Assert.assertEquals("CHANGED", git.getWorkingAreaContent(RepoPath.at("hello.c")).getContentAsString());

        // Revert the changes:
        git.checkout_pattern(RepoPattern.matching("*.c"));

        // Make sure the changes were reverted:
        Assert.assertEquals("hello version 1", git.getWorkingAreaContent(RepoPath.at("hello.c")).getContentAsString());


        //> If you have an unfortunate branch that is named hello.c,
        //> this step would be confused as an instruction to switch to that branch.
        //> You should instead write:
        //>
        //> $ git checkout -- hello.c

        // Modify the contents of the file:
        git.putWorkingAreaContent(RepoPath.at("hello.c"), bytes("CHANGED AGAIN"));
        Assert.assertEquals("CHANGED AGAIN", git.getWorkingAreaContent(RepoPath.at("hello.c")).getContentAsString());

        // Revert the changes:
        git.checkout_path(RepoPath.at("hello.c"));

        // Make sure the changes were reverted:
        Assert.assertEquals("hello version 1", git.getWorkingAreaContent(RepoPath.at("hello.c")).getContentAsString());


        //> 2. After working in the wrong branch, switching to the correct branch would be done using:
        //>
        //> $ git checkout mytopic
        try
        {
            git.checkout("mytopic");
            Assert.fail("We should have thrown an exception if the branch doesn't exist.");
        }
        catch (NanoRuntimeException ex)
        {
        }
        // Make sure that we are still on the previous branch:
        Assert.assertEquals("master", git.getCurrentBranchName());

        // Create the branch:
        git.branch("mytopic");
        git.checkout("mytopic");
        Assert.assertEquals("mytopic", git.getCurrentBranchName());


        //> However, your "wrong" branch and correct "mytopic" branch may differ in files that you have modified locally,
        // in which case the above checkout would fail like this:
        //>
        //> $ git checkout mytopic
        //> error: You have local changes to 'frotz'; not switching branches.
        //> You can give the -m flag to the command, which would try a three-way merge:
        //>
        //> $ git checkout -m mytopic
        //> Auto-merging frotz
        //> After this three-way merge, the local modifications are not registered in your index file, so git diff would show you what changes you made since the tip of the new branch.
        // TODO: merge changes


        //> 3. When a merge conflict happens during switching branches with the -m option, you would see something like this:
        //>
        //> $ git checkout -m mytopic
        //> Auto-merging frotz
        //> ERROR: Merge conflict in frotz
        //> fatal: merge program failed
        //>
        //> At this point, git diff shows the changes cleanly merged as in the previous example, as well as the changes in the conflicted files. Edit and resolve the conflict and mark it resolved with git add as usual:
        //>
        //> $ edit frotz
        //> $ git add frotz
        // TODO: detect merge conflicts

    }

}
