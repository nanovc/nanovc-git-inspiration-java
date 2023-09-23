package io.git.nanovc.examples.git_internals;

import io.git.nanovc.*;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 10.3 Git Internals - Git References
 * https://git-scm.com/book/en/v2/Git-Internals-Git-References
 * <p>
 * This set of tests follows the examples explained in the official Git documentation.
 * <p>
 * It demonstrates how the same structure can be replicated with Nano Version Control.
 * <p>
 * NOTE: We follow the convention where all text that comes from the official Git documentation is prefixed with //>
 */
public class Git_3_References extends NanoVersionControlTestsBase
{
    @Test
    public void Git_References()
    {
        // Initialise a new repo:
        RepoHandler git = NanoVersionControl.newHandler();
        Repo repo = git.init();

        // Create state in the repo that was created in Tree_Objects():
        //region Initial State Creation

        // Set the author and committer on the manager:
        git.setAuthorAndCommitter("Luke Machowski");

        // Override the current time so that commits have predictable results:
        ZonedDateTime nowOverride = ZonedDateTime.of(2017, 5, 1, 8, 0, 0, 0, ZoneId.of("GMT+2"));
        git.setNowOverride(nowOverride);

        // Create the first version of the test.txt file:
        Hash test_txt_version_1_hash = git.hash_object_write_string("version 1");
        Assert.assertEquals("d5353cdaa0518db00beb7dd5c334860f1e58f1f5", test_txt_version_1_hash.value);

        // Create the second version of the test.txt file:
        Hash test_txt_version_2_hash = git.hash_object_write_string("version 2");
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", test_txt_version_2_hash.value);

        // Add the existing object from the object database into the staging area:
        git.update_index_add_cacheInfo(test_txt_version_1_hash, RepoPath.at("test.txt"));

        // Write the current staging area into a set of tree objects into the object database:
        Tree version1Tree = git.write_tree();
        Assert.assertEquals("91f91dfa30a202f88d959213deb8dfa1e81e3fdd", version1Tree.hash.value);
        Assert.assertEquals("d5353cdaa0518db00beb7dd5c334860f1e58f1f5", version1Tree.entries.get(0).hashValue);

        // Create a new file in the working directory:
        git.putWorkingAreaContent(RepoPath.at("new.txt"), "new file".getBytes(StandardCharsets.UTF_8));

        // Update the staging area with version 2 of our file which is already in the object database:
        git.update_index_add_cacheInfo(test_txt_version_2_hash, RepoPath.at("test.txt"));

        // Add it to the staging area:
        git.update_index_add(RepoPath.at("new.txt"));

        // Write the current staging area into a set of tree objects into the object database:
        Tree version2Tree = git.write_tree();
        Assert.assertEquals("654725b11e4186d9807c3396c2c651b927acb68f", version2Tree.hash.value);
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", version2Tree.entries.get(0).hashValue);
        Assert.assertEquals("634a399f5c7cb5b2c22aeedb32e7b49cd2fb9623", version2Tree.entries.get(1).hashValue);

        // Add the first version as a backup under the 'bak' directory:
        git.read_tree(version1Tree.hash, RepoPath.at("bak"));

        // Write the current staging area into a set of tree objects into the object database:
        Tree version3Tree = git.write_tree();
        Assert.assertEquals("17aa869614d4876f6646bcea558496d5a720f728", version3Tree.hash.value);
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", version3Tree.entries.get(0).hashValue);
        Assert.assertEquals("634a399f5c7cb5b2c22aeedb32e7b49cd2fb9623", version3Tree.entries.get(1).hashValue);
        Assert.assertEquals("91f91dfa30a202f88d959213deb8dfa1e81e3fdd", version3Tree.entries.get(2).hashValue);

        // Commit the first tree:
        Commit firstCommit = git.commit_tree(version1Tree.hash, "first commit");
        Assert.assertEquals("bc8284884aba8ec5dffab515898cd3cd957e5695", firstCommit.hash.value);

        // Commit the second tree:
        Commit secondCommit = git.commit_tree(version2Tree.hash, "second commit", firstCommit.hash);
        Assert.assertEquals("c1e4cf563ad74162a7fc050fbd7e866750b918c3", secondCommit.hash.value);

        // Commit the third tree:
        Commit thirdCommit = git.commit_tree(version3Tree.hash, "third commit", secondCommit.hash);
        Assert.assertEquals("a93eb7a3da86e4e938505bb95b0f9a716d4657f2", thirdCommit.hash.value);

        //endregion
        // Now the initial stat of the repo matches where the story below begins.

        /*>
            NOTE: The top hash is from their original example,
                  The bottom hash is from our actual values in the unit test.

                                               /-----------------bak-------------_
            -----------------      ---------- /                                   |
            | 1a0140e       |      | 3c4e9c |/                                    |
            | a93eb7a       |----->| 17aa86 |---new.txt----------------------_    |
            | third commit  |      |  tree  |\                                |   |
            -----------------      ---------- \---\                           |   |
                    |                           test.txt    ---------------   |   |
                    |                               \------>| 1f7a7a      |   |   |
                    |                                       | cde016      |   |   |
                    |                                __---->| "version 2" |   |   |
                    |                               /       ---------------   |   |
                    v                           test.txt                      |   |
            -----------------      ---------- ____/                           |   |
            | cac0ca        |      | 0155eb |/                                |   |
            | c1e4cf        |----->| 654725 |                                 |   |
            | second commit |      |  tree  |\                                |   |
            -----------------      ---------- \---\                           |   |
                    |                           new.txt     ---------------   |   |
                    |                               \------>| fa49b0      |   |   |
                    |                                       | 634a39      |<--/   |
                    |                                       | "new file"  |       |
                    |                                       ---------------       |
                    v                                                             |
            -----------------      ----------               ---------------       |
            | fdf4fc        |      | d8329f |               | 83baae      |       |
            | bc8284        |----->| 91f91d |---test.txt--->| d5353c      |       |
            | first commit  |      |  tree  |               | "version 1" |       |
            -----------------      ----------               ---------------       |
                                        ^                                         /
                                        |-----------------------------------------

           Figure 151. All the reachable objects in your Git directory.
        */


        //> You can run something like git log 1a410e to look through your whole history,
        //> but you still have to remember that 1a410e is the last commit in order to walk that history to find all those objects.
        //> You need a file in which you can store the SHA-1 value under a simple name
        //> so you can use that pointer rather than the raw SHA-1 value.

        //> In Git, these are called “references” or “refs”;
        //> you can find the files that contain the SHA-1 values in the .git/refs directory.
        //> In the current project, this directory contains no files, but it does contain a simple structure:

        //> $ find .git/refs
        //>     .git/refs
        //>     .git/refs/heads
        //>     .git/refs/tags
        //> $ find .git/refs -type f
        Assert.assertNotNull(repo.database.refs);
        Assert.assertNotNull(repo.database.refs.heads);
        Assert.assertNotNull(repo.database.refs.tags);
        Assert.assertEquals(0, repo.database.refs.heads.size());

        //> To create a new reference that will help you remember where your latest commit is,
        //> you can technically do something as simple as this:

        //> $ echo "1a410efbd13591db07496601ebc7a059dd55cfe9" > .git/refs/heads/master
        repo.database.refs.heads.add(new HashReference("master", "a93eb7a3da86e4e938505bb95b0f9a716d4657f2"));

        //> Now, you can use the head reference you just created instead of the SHA-1 value in your Git commands:

        //> $ git log --pretty=oneline master
        //>     1a410efbd13591db07496601ebc7a059dd55cfe9 third commit
        //>     cac0cab538b970a37ea1e769cbbde608743bc96d second commit
        //>     fdf4fc3344e67ab068f836878b6c4951e3b15f3d first commit
        Log masterLog = git.log("master");
        Assert.assertNotNull(masterLog);
        Assert.assertEquals(3, masterLog.size());
        Assert.assertEquals(thirdCommit.hash.value, masterLog.get(0).commitHashValue);
        Assert.assertEquals(secondCommit.hash.value, masterLog.get(1).commitHashValue);
        Assert.assertEquals(firstCommit.hash.value, masterLog.get(2).commitHashValue);

        //> You aren’t encouraged to directly edit the reference files.
        //> Git provides a safer command to do this if you want to update a reference called update-ref:

        // $ git update-ref refs/heads/master 1a410efbd13591db07496601ebc7a059dd55cfe9
        HashReference masterReference = git.update_ref_in_heads("master", "96eae0f8ccca09d7eb6f3bf1b46331acea6332c4");
        Assert.assertNotNull(masterReference);
        Assert.assertEquals("master", masterReference.name);
        Assert.assertEquals("96eae0f8ccca09d7eb6f3bf1b46331acea6332c4", masterReference.hash.value);

        //> That’s basically what a branch in Git is:
        //> a simple pointer or reference to the head of a line of work.

        //> To create a branch back at the second commit, you can do this:

        //> $ git update-ref refs/heads/test cac0ca
        HashReference testReference = git.update_ref_in_heads("test", secondCommit.hash);
        Assert.assertNotNull(testReference);
        Assert.assertEquals("test", testReference.name);
        Assert.assertEquals("c1e4cf563ad74162a7fc050fbd7e866750b918c3", testReference.hash.value);

        //> Your branch will contain only work from that commit down:

        //> $ git log --pretty=oneline test
        //>     cac0cab538b970a37ea1e769cbbde608743bc96d second commit
        //>     fdf4fc3344e67ab068f836878b6c4951e3b15f3d first commit
        Log testLog = git.log("test");
        Assert.assertNotNull(testLog);
        Assert.assertEquals(2, testLog.size());
        Assert.assertEquals(secondCommit.hash.value, testLog.get(0).commitHashValue);
        Assert.assertEquals(firstCommit.hash.value, testLog.get(1).commitHashValue);

        //> Now, your Git database conceptually looks something like this:
        /*>
            NOTE: The top hash is from their original example,
                  The bottom hash is from our actual values in the unit test.

                                                                            /-----------------bak-------------_
                                         -----------------      ---------- /                                   |
              ---------------------      |    1a014e     |      | 3c4e9c |/                                    |
              | refs/heads/master |----->|    a93eb7     |----->| 17aa86 |---new.txt----------------------_    |
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
              |  refs/heads/test  |----->|    c1e4cf     |----->| 654725 |                                 |   |
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
                                         |    bc8284     |----->| 91f91d |---test.txt--->| d5353c      |       |
                                         | first commit  |      |  tree  |               | "version 1" |       |
                                         -----------------      ----------               ---------------       |
                                                                     ^                                         /
                                                                     |-----------------------------------------

           Figure 152. Git directory objects with branch head references included.
        */

        //> When you run commands like git branch (branchname),
        //> Git basically runs that update-ref command to add the SHA-1 of the last commit of the branch you’re on
        //> into whatever new reference you want to create.
    }

    @Test
    public void The_HEAD()
    {
        // Initialise a new repo:
        RepoHandler git = NanoVersionControl.newHandler();
        Repo repo = git.init();

        // Create state in the repo to suite the example:
        //region Initial State Creation

        git.update_ref_in_heads("master", "1111111111222222222233333333334444444444");
        repo.database.HEAD = new SymbolicReference("master");

        //endregion
        // Now the initial stat of the repo matches where the story below begins.

        //> The question now is, when you run git branch (branchname), how does Git know the SHA-1 of the last commit?
        //> The answer is the HEAD file.

        //> The HEAD file is a symbolic reference to the branch you’re currently on.
        //> By symbolic reference, we mean that unlike a normal reference,
        //> it doesn’t generally contain a SHA-1 value but rather a pointer to another reference.
        //> If you look at the file, you’ll normally see something like this:

        //> $ cat .git/HEAD
        //>     ref: refs/heads/master
        Assert.assertEquals("master", repo.database.HEAD.referenceName);

        //> If you run git checkout test, Git updates the file to look like this:

        //> $ cat .git/HEAD
        //>     ref: refs/heads/test


        //> When you run git commit, it creates the commit object,
        //> specifying the parent of that commit object to be whatever SHA-1 value the reference in HEAD points to.

        //> You can also manually edit this file, but again a safer command exists to do so: symbolic-ref.
        //> You can read the value of your HEAD via this command:

        //> $ git symbolic-ref HEAD
        //>     refs/heads/master

        //> You can also set the value of HEAD:

        //> $ git symbolic-ref HEAD refs/heads/test
        //> $ cat .git/HEAD
        //>     ref: refs/heads/test
        SymbolicReference headReference = git.symbolic_ref("test");
        Assert.assertNotNull(headReference);
        Assert.assertSame(headReference, repo.database.HEAD);
        Assert.assertEquals("test", headReference.referenceName);

        //> You can’t set a symbolic reference outside of the refs style:

        //> $ git symbolic-ref HEAD test
        //>     fatal: Refusing to point HEAD outside of refs/
    }

    @Test
    public void Tags()
    {
        // Initialise a new repo:
        RepoHandler git = NanoVersionControl.newHandler();
        Repo repo = git.init();

        // Create state in the repo that was created in Git_References():
        //region Initial State Creation

        // Set the author and committer on the manager:
        git.setAuthorAndCommitter("Luke Machowski");

        // Override the current time so that commits have predictable results:
        ZonedDateTime nowOverride = ZonedDateTime.of(2017, 5, 1, 8, 0, 0, 0, ZoneId.of("GMT+2"));
        git.setNowOverride(nowOverride);

        // Create the first version of the test.txt file:
        Hash test_txt_version_1_hash = git.hash_object_write_string("version 1");
        Assert.assertEquals("d5353cdaa0518db00beb7dd5c334860f1e58f1f5", test_txt_version_1_hash.value);

        // Create the second version of the test.txt file:
        Hash test_txt_version_2_hash = git.hash_object_write_string("version 2");
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", test_txt_version_2_hash.value);

        // Add the existing object from the object database into the staging area:
        git.update_index_add_cacheInfo(test_txt_version_1_hash, RepoPath.at("test.txt"));

        // Write the current staging area into a set of tree objects into the object database:
        Tree version1Tree = git.write_tree();
        Assert.assertEquals("91f91dfa30a202f88d959213deb8dfa1e81e3fdd", version1Tree.hash.value);
        Assert.assertEquals("d5353cdaa0518db00beb7dd5c334860f1e58f1f5", version1Tree.entries.get(0).hashValue);

        // Create a new file in the working directory:
        git.putWorkingAreaContent(RepoPath.at("new.txt"), "new file".getBytes(StandardCharsets.UTF_8));

        // Update the staging area with version 2 of our file which is already in the object database:
        git.update_index_add_cacheInfo(test_txt_version_2_hash, RepoPath.at("test.txt"));

        // Add it to the staging area:
        git.update_index_add(RepoPath.at("new.txt"));

        // Write the current staging area into a set of tree objects into the object database:
        Tree version2Tree = git.write_tree();
        Assert.assertEquals("654725b11e4186d9807c3396c2c651b927acb68f", version2Tree.hash.value);
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", version2Tree.entries.get(0).hashValue);
        Assert.assertEquals("634a399f5c7cb5b2c22aeedb32e7b49cd2fb9623", version2Tree.entries.get(1).hashValue);

        // Add the first version as a backup under the 'bak' directory:
        git.read_tree(version1Tree.hash, RepoPath.at("bak"));

        // Write the current staging area into a set of tree objects into the object database:
        Tree version3Tree = git.write_tree();
        Assert.assertEquals("17aa869614d4876f6646bcea558496d5a720f728", version3Tree.hash.value);
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", version3Tree.entries.get(0).hashValue);
        Assert.assertEquals("634a399f5c7cb5b2c22aeedb32e7b49cd2fb9623", version3Tree.entries.get(1).hashValue);
        Assert.assertEquals("91f91dfa30a202f88d959213deb8dfa1e81e3fdd", version3Tree.entries.get(2).hashValue);

        // Commit the first tree:
        Commit firstCommit = git.commit_tree(version1Tree.hash, "first commit");
        Assert.assertEquals("bc8284884aba8ec5dffab515898cd3cd957e5695", firstCommit.hash.value);

        // Commit the second tree:
        Commit secondCommit = git.commit_tree(version2Tree.hash, "second commit", firstCommit.hash);
        Assert.assertEquals("c1e4cf563ad74162a7fc050fbd7e866750b918c3", secondCommit.hash.value);

        // Commit the third tree:
        Commit thirdCommit = git.commit_tree(version3Tree.hash, "third commit", secondCommit.hash);
        Assert.assertEquals("a93eb7a3da86e4e938505bb95b0f9a716d4657f2", thirdCommit.hash.value);

        // Create the master reference to the third commit:
        HashReference masterReference = git.update_ref_in_heads("master", thirdCommit.hash);
        Assert.assertEquals("master", masterReference.name);
        Assert.assertEquals("a93eb7a3da86e4e938505bb95b0f9a716d4657f2", masterReference.hash.value);

        // Create the test reference to the second commit:
        HashReference testReference = git.update_ref_in_heads("test", secondCommit.hash);
        Assert.assertEquals("test", testReference.name);
        Assert.assertEquals("c1e4cf563ad74162a7fc050fbd7e866750b918c3", testReference.hash.value);

        //endregion
        // Now the initial stat of the repo matches where the story below begins.

        /*>
            NOTE: The top hash is from their original example,
                  The bottom hash is from our actual values in the unit test.

                                                                            /-----------------bak-------------_
                                         -----------------      ---------- /                                   |
              ---------------------      |    1a014e     |      | 3c4e9c |/                                    |
              | refs/heads/master |----->|    a93eb7a     |----->| 17aa86 |---new.txt----------------------_    |
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
              |  refs/heads/test  |----->|    c1e4cf     |----->| 654725 |                                 |   |
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
                                         |    bc8284     |----->| 91f91d |---test.txt--->| d5353c      |       |
                                         | first commit  |      |  tree  |               | "version 1" |       |
                                         -----------------      ----------               ---------------       |
                                                                     ^                                         /
                                                                     |-----------------------------------------

           Figure 152. Git directory objects with branch head references included.
        */

        //> We just finished discussing Git’s three main object types, but there is a fourth.
        //> The tag object is very much like a commit object – it contains a tagger, a date, a message, and a pointer.
        //> The main difference is that a tag object generally points to a commit rather than a tree.
        //> It’s like a branch reference, but it never moves – it always points to the same commit but gives it a friendlier name.

        //> As discussed in Git Basics, there are two types of tags:
        //> annotated and lightweight.
        //> You can make a lightweight tag by running something like this:

        //> $ git update-ref refs/tags/v1.0 cac0cab538b970a37ea1e769cbbde608743bc96d
        HashReference tagV1Reference = git.update_ref_in_tags("v1.0", secondCommit.hash);
        Assert.assertNotNull(tagV1Reference);
        Assert.assertEquals("v1.0", tagV1Reference.name);
        Assert.assertEquals("c1e4cf563ad74162a7fc050fbd7e866750b918c3", tagV1Reference.hash.value);
        Assert.assertEquals(1, repo.database.refs.tags.size());
        Assert.assertSame(tagV1Reference, repo.database.refs.tags.getReference("v1.0"));

        //> That is all a lightweight tag is – a reference that never moves.
        //> An annotated tag is more complex, however.
        //> If you create an annotated tag, Git creates a tag object and then writes a reference to point to it rather than directly to the commit.
        //> You can see this by creating an annotated tag (-a specifies that it’s an annotated tag):

        //> $ git tag -a v1.1 1a410efbd13591db07496601ebc7a059dd55cfe9 -m 'test tag'

        // NOTE: This is a tag on the the third commit.




        // WARNING: We have not implemented annotated tags in Nano Version Control Yet!





        //> Here’s the object SHA-1 value it created:

        //> $ cat .git/refs/tags/v1.1
        //>     9585191f37f7b0fb9444f35a9bf50de191beadc2

        //> Now, run the cat-file command on that SHA-1 value:

        //> $ git cat-file -p 9585191f37f7b0fb9444f35a9bf50de191beadc2
        //>     object 1a410efbd13591db07496601ebc7a059dd55cfe9
        //>     type commit
        //>     tag v1.1
        //>     tagger Scott Chacon <schacon@gmail.com> Sat May 23 16:48:58 2009 -0700
        //>
        //>     test tag

        //> Notice that the object entry points to the commit SHA-1 value that you tagged.
        //> Also notice that it doesn’t need to point to a commit; you can tag any Git object.
        //> In the Git source code, for example,
        //> the maintainer has added their GPG public key as a blob object and then tagged it.

        //> You can view the public key by running this in a clone of the Git repository:

        //> $ git cat-file blob junio-gpg-pub

        //> The Linux kernel repository also has a non-commit-pointing tag object
        //> – the first tag created points to the initial tree of the import of the source code.
    }
}
