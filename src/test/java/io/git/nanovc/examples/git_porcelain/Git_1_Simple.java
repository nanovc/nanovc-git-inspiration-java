package io.git.nanovc.examples.git_porcelain;

import io.git.nanovc.*;
import io.git.nanovc.examples.git_internals.Git_3_References;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This is a set of example where we use the porcelain commands to create meaningful repos in Nano Version Control.
 */
public class Git_1_Simple extends NanoVersionControlTestsBase
{


    /**
     * This example hopes to replicate the example structure from the advanced git documentation.
     * Specifically, it replicates the end state of the {@link Git_3_References#Tags()} unit test.
     *
     * The original example is documented here:
     * 10.3 Git Internals - Git References
     * https://git-scm.com/book/en/v2/Git-Internals-Git-References
     */
    @Test
    public void ReplicaOfTheGitInternalsExample()
    {


        // This is the repo structure that we hope to replicate using the porcelain commands.
        // https://git-scm.com/book/en/v2/Git-Internals-Git-References
        /*>
            NOTE: The top hash is from their original example,
                  The bottom hash is from our actual values in the unit test.

                                                                            /-----------------bak-------------_
                                         -----------------      ---------- /                                   |
              ---------------------      |    1a014e     |      | 3c4e9c |/                                    |
              | refs/heads/master |----->|    96eaef     |----->| 870152 |---new.txt----------------------_    |
              ---------------------      |  third commit |      |  tree  |\                                |   |
                                         -----------------      ---------- \---\                           |   |
                                                 |                           test.txt    ---------------   |   |
                                                 |                               \------>| 1f7a7a      |   |   |
                                                 |                                       | cde016      |   |   |
                                                 |                                __---->| "version 2" |   |   |
                                                 |                               /       ---------------   |   |
                                                 v                           test.txt                      |   |
                                         -----------------      ---------- ____/                           |   |
              ---------------------      |    cac0ca     |      | 0155eb |/                                |   |
              |  refs/heads/test  |----->|    d08016     |----->| 11450f |                                 |   |
              ---------------------      | second commit |      |  tree  |\                                |   |
                                         -----------------      ---------- \---\                           |   |
                                                 |                           new.txt     ---------------   |   |
                                                 |                               \------>| fa49b0      |   |   |
                                                 |                                       | 634a39      |<--/   |
                                                 |                                       | "new file"  |       |
                                                 |                                       ---------------       |
                                                 v                                                             |
                                         -----------------      ----------               ---------------       |
                                         |    fdf4fc     |      | d8329f |               | 83baae      |       |
                                         |    ff7337     |----->| 19b794 |---test.txt--->| d5353c      |       |
                                         | first commit  |      |  tree  |               | "version 1" |       |
                                         -----------------      ----------               ---------------       |
                                                                     ^                                         /
                                                                     |-----------------------------------------

           Figure 152. Git directory objects with branch head references included.
        */

        // Create a new manager to interact with a repo:
        RepoHandler repoManager = NanoVersionControl.newHandler();
        PorcelainCommands git = repoManager.asPorcelainCommands();

        // Initialise a new repo:
        Repo repo = git.init();

        // Set the author and committer on the manager:
        repoManager.setAuthorAndCommitter("Luke Machowski");

        // Override the current time so that commits have predictable results:
        ZonedDateTime nowOverride = ZonedDateTime.of(2017, 5, 1, 8, 0, 0, 0, ZoneId.of("GMT+2"));
        repoManager.setNowOverride(nowOverride);

        // Start adding content to the working area:
        Assert.assertEquals(0, repo.workingArea.contents.size());
        git.putWorkingAreaContent("test.txt", bytes("version 1"));
        Assert.assertEquals(1, repo.workingArea.contents.size());

        // Add the file to the staging area so that it is tracked:
        Assert.assertEquals(0, repo.stagingArea.contents.size());
        git.addAll(true);
        Assert.assertEquals(1, repo.stagingArea.contents.size());

        // Confirm that the content in the staging area is a snapshot of what is in the working area:
        Assert.assertArrayEquals(repo.workingArea.getContent("test.txt").content, repo.stagingArea.getContent("test.txt").content);
        Assert.assertNotSame(repo.workingArea.getContent("test.txt").content, repo.stagingArea.getContent("test.txt").content);

        // Commit the changes:
        Commit firstCommit = git.commitAll("first commit", true);
        Assert.assertEquals("bc8284884aba8ec5dffab515898cd3cd957e5695", firstCommit.hash.value);
        Assert.assertEquals("91f91dfa30a202f88d959213deb8dfa1e81e3fdd", firstCommit.treeHashValue);
        Assert.assertEquals("d5353cdaa0518db00beb7dd5c334860f1e58f1f5", ((Tree) repoManager.cat_file(firstCommit.treeHashValue)).entries.get(0).hashValue);

        /*>
                                         -----------------      ----------               ---------------
              ---------------------      |    fdf4fc     |      | d8329f |               | 83baae      |
              | refs/heads/master |----->|    bc8284     |----->| 91f91d |---test.txt--->| d5353c      |
              ---------------------      | first commit  |      |  tree  |               | "version 1" |
                                         -----------------      ----------               ---------------
        */
    }


}
