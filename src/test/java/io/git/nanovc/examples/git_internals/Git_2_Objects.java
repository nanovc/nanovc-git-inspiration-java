package io.git.nanovc.examples.git_internals;

import io.git.nanovc.*;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 10.2 Git Internals - Git Objects
 * https://git-scm.com/book/en/v2/Git-Internals-Git-Objects
 *
 * This set of tests follows the examples explained in the official Git documentation.
 *
 * It demonstrates how the same structure can be replicated with Nano Version Control.
 *
 * NOTE: We follow the convention where all text that comes from the official Git documentation is prefixed with //>
 */
public class Git_2_Objects extends NanoVersionControlTestsBase
{
    @Test
    public void Git_Objects()
    {

        //> Git is a content-addressable filesystem.
        //>
        //> Great.
        //>
        //> What does that mean?
        //>
        //> It means that at the core of Git is a simple key-value data store.
        //> You can insert any kind of content into it,
        //> and it will give you back a key that you can use to retrieve the content again at any time.
        //>
        //> To demonstrate, you can use the plumbing command hash-object, which takes some data, stores it in your .git directory,
        //> and gives you back the key the data is stored as.
        //> First, you initialize a new Git repository and verify that there is nothing in the objects directory:
        /*>
            $ git init test
            Initialized empty Git repository in /tmp/test/.git/
                $ cd test
            $ find .git/objects
                .git/objects
                .git/objects/info
                .git/objects/pack
            $ find .git/objects -type f
        */




        // The RepoEngine can be thought of as te Git Command Line Interface (CLI).
        // Therefore we can use that to initialise the repo too:
        //> EXAMPLE: $ git init test:
        RepoHandler git = NanoVersionControl.newHandler();
        Repo test = git.init();

        //> EXAMPLE: Initialized empty Git repository in /tmp/test/.git/


        // The repo.database represents the .git folder.
        // Make sure it exists:
        Assert.assertNotNull(test.database);

        //> EXAMPLE: $ cd test

        //> EXAMPLE: $ find .git/objects
        //> EXAMPLE:   .git/objects
        //> EXAMPLE:   .git/objects/info
        //> EXAMPLE:   .git/objects/pack
        Assert.assertNotNull(test.database.objects);
        Assert.assertEquals(0, test.database.objects.map.size());
        // NOTE: We are not interested in the info and pack structures since it's all in memory anyway.
        // We leave it up to the storage layer to decide how to optimise the structure.
        // Notice how we are drawing the line between "Storage" and "Version Control".
        // Nano Version control is interested in the "Version Control" and not the "Storage".

        //> Git has initialized the objects directory and created pack and info subdirectories in it,
        //> but there are no regular files.
        //> Now, store some text in your Git database:
        /*>
            $ echo 'test content' | git hash-object -w --stdin
            d670460b4b4aece5915caf5c68d12f560a9fe3e4
         */

        Hash hash = git.hash_object_write_string("test content");
        Assert.assertEquals("04d7fa923aad00484743abdb0170ce0064f1ab15", hash.toString());

        //> The -w tells hash-object to store the object;
        //> otherwise, the command simply tells you what the key would be.
        //> --stdin tells the command to read the content from stdin;
        // if you don’t specify this, hash-object expects a file path at the end.
        // The output from the command is a 40-character checksum hash.
        // This is the SHA-1 hash – a checksum of the content you’re storing plus a header, which you’ll learn about in a bit.

        // Now you can see how Git has stored your data:
        /*
            $ find .git/objects -type f
            .git/objects/d6/70460b4b4aece5915caf5c68d12f560a9fe3e4
         */
        Assert.assertNotNull(git.repo.database.objects.index.get("04"));
        Assert.assertNotNull(git.repo.database.objects.index.get("04").get("d7fa923aad00484743abdb0170ce0064f1ab15"));
        Assert.assertNotNull(git.repo.database.objects.get(hash));


        //> You can see a file in the objects directory.
        //> This is how Git stores the content initially
        //> – as a single file per piece of content,
        //> named with the SHA-1 checksum of the content and its header.
        //> The subdirectory is named with the first 2 characters of the SHA-1,
        //> and the filename is the remaining 38 characters.
        //>
        //> You can pull the content back out of Git with the cat-file command.
        //> This command is sort of a Swiss army knife for inspecting Git objects.
        //> Passing -p to it instructs the cat-file command to figure out the type of content and display it nicely for you:
        //>
        //> $ git cat-file -p d670460b4b4aece5915caf5c68d12f560a9fe3e4
        //>     test content
        RepoObject repoObject = git.cat_file(hash);
        Assert.assertNotNull(repoObject);
        Assert.assertEquals(ObjectType.BLOB, git.cat_file_object_type(hash));
    }

    @Test
    public void Tree_Objects()
    {
        //> The next type we’ll look at is the tree,
        //> which solves the problem of storing the filename and also allows you to store a group of files together.
        //> Git stores content in a manner similar to a UNIX filesystem, but a bit simplified.
        //> All the content is stored as tree and blob objects,
        //> with trees corresponding to UNIX directory entries and blobs corresponding more or less to inodes or file contents.
        //> A single tree object contains one or more tree entries,
        //> each of which contains a SHA-1 pointer to a blob or subtree with its associated mode, type, and filename.
        //>
        //> For example, the most recent tree in a project may look something like this:

        //>  $ git cat-file -p master^{tree}
        //>      100644 blob a906cb2a4a904a152e80877d4088654daad0c859      README
        //>      100644 blob 8f94139338f9404f26296befa88755fc2598c289      Rakefile
        //>      040000 tree 99f1a6d12cb4b6f19c8655fca46c3ecf317074e0      lib

        //> The master^{tree} syntax specifies the tree object that is pointed to by the last commit on your master branch.
        //> Notice that the lib subdirectory isn’t a blob but a pointer to another tree:

        //>  $ git cat-file -p 99f1a6d12cb4b6f19c8655fca46c3ecf317074e0
        //>      100644 blob 47c6340d6459e05787f644c2447d2595f5d3a54b      simplegit.rb

        //> Conceptually, the data that Git is storing is something like this:
        /*>
                          _______
                          |tree |
                          -------
                     ___/    |    \___
                    /        |        \
                 README   Rakefile    lib
                 /           |           \
             -------      -------      -------
             | blob|      |blob |      |tree |
             -------      -------      -------
                                          |
                                      simplegit.rb
                                          |
                                       -------
                                       |blob |
                                       -------

           Figure 149. Simple version of the Git data model.
         */

        // Initialise a new repo:
        RepoHandler git = NanoVersionControl.newHandler();
        Repo repo = git.init();

        // Create state in the repo that was created in Git_Objects():
        //region Initial State Creation

        // Create the first version of the test.txt file:
        Hash test_txt_version_1_hash = git.hash_object_write_string("version 1");
        Assert.assertEquals("d5353cdaa0518db00beb7dd5c334860f1e58f1f5", test_txt_version_1_hash.value);

        // Create the second version of the test.txt file:
        Hash test_txt_version_2_hash = git.hash_object_write_string("version 2");
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", test_txt_version_2_hash.value);

        //endregion
        // Now the initial stat of the repo matches where the story below begins.



        //> You can fairly easily create your own tree.
        //> Git normally creates a tree by taking the state of your staging area or index and writing a series of tree objects from it.
        //> So, to create a tree object, you first have to set up an index by staging some files.
        //> To create an index with a single entry – the first version of your test.txt file –
        //> you can use the plumbing command update-index.
        //> You use this command to artificially add the earlier version of the test.txt file to a new staging area.
        //> You must pass it the --add option because the file doesn’t yet exist in your staging area
        //> (you don’t even have a staging area set up yet) and --cacheinfo because the file you’re adding isn’t in your directory but is in your database.
        //> Then, you specify the mode, SHA-1, and filename:

        //>  $ git update-index --add --cacheinfo 100644 \
        //>     83baae61804e65cc73a7201a7252750c76066a30 test.txt

        // Add the existing object from the object database into the staging area:
        MutableContent test_txt_stagingContent = git.update_index_add_cacheInfo(test_txt_version_1_hash, RepoPath.at("test.txt"));
        Assert.assertSame(test_txt_stagingContent, git.repo.stagingArea.getContent("/test.txt"));

        //> In this case, you’re specifying a mode of 100644, which means it’s a normal file.
        //> Other options are 100755, which means it’s an executable file; and 120000, which specifies a symbolic link.
        //> The mode is taken from normal UNIX modes but is much less flexible –
        //> these three modes are the only ones that are valid for files (blobs) in Git (although other modes are used for directories and submodules).

        // NOTE: We ignore the file-mode concept with Nano Version Control because we are not using a real file system and the concept doesn't make sense.
        // It's up to the higher-level layer to define the meaning and usage of the content.

        //> Now, you can use the write-tree command to write the staging area out to a tree object.
        //> No -w option is needed – calling write-tree automatically creates a tree object from the state of the index if that tree doesn’t yet exist:

        //>  $ git write-tree
        //>      d8329fc1cc938780ffdd9f94e0d364e0ea74f579
        // Write the current staging area into a set of tree objects into the object database:
        Tree version1Tree = git.write_tree();
        Assert.assertNotNull(version1Tree);
        Assert.assertNotNull(version1Tree.hash);
        Assert.assertNotNull(version1Tree.hash.value);
        Assert.assertEquals("91f91dfa30a202f88d959213deb8dfa1e81e3fdd", version1Tree.hash.value);


        //>  $ git cat-file -p d8329fc1cc938780ffdd9f94e0d364e0ea74f579
        //>     100644 blob 83baae61804e65cc73a7201a7252750c76066a30      test.txt
        Assert.assertSame(version1Tree, git.cat_file(version1Tree.hash));
        Assert.assertNotNull(version1Tree.entries);
        Assert.assertEquals(1, version1Tree.entries.size());
        Assert.assertEquals("test.txt", version1Tree.entries.get(0).name);
        Assert.assertEquals(ObjectType.BLOB, version1Tree.entries.get(0).objectType);
        Assert.assertEquals("d5353cdaa0518db00beb7dd5c334860f1e58f1f5", version1Tree.entries.get(0).hashValue);
        Assert.assertEquals(test_txt_version_1_hash.value, version1Tree.entries.get(0).hashValue);

        //> You can also verify that this is a tree object:

        //>  $ git cat-file -t d8329fc1cc938780ffdd9f94e0d364e0ea74f579
        //>     tree
        Assert.assertEquals(ObjectType.TREE, git.cat_file_object_type(version1Tree.hash));

        // Make sure that the staging area is not cleared after the tree is written:
        Assert.assertEquals(1, repo.stagingArea.contents.size());

        //> You’ll now create a new tree with the second version of test.txt and a new file as well:

        //> $ echo 'new file' > new.txt
        // Create a new file in the working directory:
        MutableContent new_txt_WorkingDirectoryContent = git.putWorkingAreaContent(RepoPath.at("new.txt"), "new file".getBytes(StandardCharsets.UTF_8));

        //> $ git update-index --cacheinfo 100644 \
        //>     1f7a7a472abf3dd9643fd615f6da379c4acb3e3a test.txt
        //> $ git update-index test.txt
        // Update the staging area with version 2 of our file which is already in the object database:
        git.update_index_add_cacheInfo(test_txt_version_2_hash, RepoPath.at("test.txt"));

        //> $ git update-index --add new.txt
        MutableContent new_txt_stagingContent = git.update_index_add(RepoPath.at("new.txt"));
        Assert.assertSame(new_txt_stagingContent, git.repo.stagingArea.getContent("/new.txt"));

        //> Your staging area now has the new version of test.txt as well as the new file new.txt.
        //> Write out that tree (recording the state of the staging area or index to a tree object) and see what it looks like:

        //> $ git write-tree
        //>     0155eb4229851634a0f03eb265b69f5a2d56f341
        // Write the current staging area into a set of tree objects into the object database:
        Tree version2Tree = git.write_tree();
        Assert.assertNotNull(version2Tree);
        Assert.assertNotNull(version2Tree.hash);
        Assert.assertNotNull(version2Tree.hash.value);
        Assert.assertEquals("654725b11e4186d9807c3396c2c651b927acb68f", version2Tree.hash.value);

        //> $ git cat-file -p 0155eb4229851634a0f03eb265b69f5a2d56f341
        //>     100644 blob fa49b077972391ad58037050f2a75f74e3671e92      new.txt
        //>     100644 blob 1f7a7a472abf3dd9643fd615f6da379c4acb3e3a      test.txt
        Assert.assertSame(version2Tree, git.cat_file(version2Tree.hash));
        Assert.assertNotNull(version2Tree.entries);
        Assert.assertEquals(2, version2Tree.entries.size());
        Assert.assertEquals("test.txt", version2Tree.entries.get(0).name);
        Assert.assertEquals(ObjectType.BLOB, version2Tree.entries.get(0).objectType);
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", version2Tree.entries.get(0).hashValue);
        Assert.assertEquals(test_txt_version_2_hash.value, version2Tree.entries.get(0).hashValue);
        Assert.assertEquals("new.txt", version2Tree.entries.get(1).name);
        Assert.assertEquals(ObjectType.BLOB, version2Tree.entries.get(1).objectType);
        Assert.assertEquals("634a399f5c7cb5b2c22aeedb32e7b49cd2fb9623", version2Tree.entries.get(1).hashValue);


        //> Notice that this tree has both file entries and also that the test.txt SHA-1 is the “version 2” SHA-1 from earlier (1f7a7a)
        // [cde016cb] in our example.





        //> Just for fun, you’ll add the first tree as a subdirectory into this one.
        //> You can read trees into your staging area by calling read-tree.
        //> In this case, you can read an existing tree into your staging area as a subtree by using the --prefix option to read-tree:

        //> $ git read-tree --prefix=bak d8329fc1cc938780ffdd9f94e0d364e0ea74f579
        List<MutableContent> version1TreeContentAtBak = git.read_tree(version1Tree.hash, RepoPath.at("bak"));
        Assert.assertEquals(1, version1TreeContentAtBak.size());
        Assert.assertEquals("/bak/test.txt", version1TreeContentAtBak.get(0).getAbsolutePath());

        //> $ git write-tree
        //>     3c4e9cd789d88d8d89c1073707c3585e41b0e614
        Tree version3Tree = git.write_tree();
        Assert.assertNotNull(version3Tree);
        Assert.assertNotNull(version3Tree.hash);
        Assert.assertNotNull(version3Tree.hash.value);
        Assert.assertEquals("17aa869614d4876f6646bcea558496d5a720f728", version3Tree.hash.value);


        //> $ git cat-file -p 3c4e9cd789d88d8d89c1073707c3585e41b0e614
        //>     040000 tree d8329fc1cc938780ffdd9f94e0d364e0ea74f579      bak
        //>     100644 blob fa49b077972391ad58037050f2a75f74e3671e92      new.txt
        //>     100644 blob 1f7a7a472abf3dd9643fd615f6da379c4acb3e3a      test.txt
        Assert.assertSame(version3Tree, git.cat_file(version3Tree.hash));
        Assert.assertNotNull(version3Tree.entries);
        Assert.assertEquals(3, version3Tree.entries.size());
        Assert.assertEquals("test.txt", version3Tree.entries.get(0).name);
        Assert.assertEquals(ObjectType.BLOB, version3Tree.entries.get(0).objectType);
        Assert.assertEquals("cde016cb1660f6922680085eb1047d6823e7ab06", version3Tree.entries.get(0).hashValue);
        Assert.assertEquals(test_txt_version_2_hash.value, version3Tree.entries.get(0).hashValue);
        Assert.assertEquals("new.txt", version3Tree.entries.get(1).name);
        Assert.assertEquals(ObjectType.BLOB, version3Tree.entries.get(1).objectType);
        Assert.assertEquals("634a399f5c7cb5b2c22aeedb32e7b49cd2fb9623", version3Tree.entries.get(1).hashValue);
        Assert.assertEquals("bak", version3Tree.entries.get(2).name);
        Assert.assertEquals(ObjectType.TREE, version3Tree.entries.get(2).objectType);
        Assert.assertEquals("91f91dfa30a202f88d959213deb8dfa1e81e3fdd", version3Tree.entries.get(2).hashValue);


        //> If you created a working directory from the new tree you just wrote,
        //> you would get the two files in the top level of the working directory
        //> and a subdirectory named bak that contained the first version of the test.txt file.
        //> You can think of the data that Git contains for these structures as being like this:

        /*>
            NOTE: The top hash is from their original example,
                  The bottom hash is from our actual values in the unit test.


                          ________
                          |3c4e9c|
                          |870152|
                          | tree |
                          --------
                     ___/    |    \___
                    /        |        \
                new.txt   test.txt     bak
                 /           |           \
            ----------   -----------  ----------
            | fa49b0 |   | 1f7a7a  |  | d8329f |
            | cde016 |   | 634a39  |  | 91f91d |
            |new file|   |version 2|  |  tree  |
            ----------   -----------  ----------
                                          |
                                       test.txt
                                          |
                                     -----------
                                     | 83baae  |
                                     | d5353c  |
                                     |version 1|
                                     -----------

           Figure 150. The content structure of your current Git data.
         */
    }

    @Test
    public void Commit_Objects()
    {
        // Initialise a new repo:
        RepoHandler git = NanoVersionControl.newHandler();
        Repo repo = git.init();

        // Create state in the repo that was created in Tree_Objects():
        //region Initial State Creation

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


        //endregion
        // Now the initial stat of the repo matches where the story below begins.

        /*>
            NOTE: The top hash is from their original example,
                  The bottom hash is from our actual values in the unit test.


                          ________
                          |3c4e9c|
                          |17aa86|
                          | tree |
                          --------
                     ___/    |    \___
                    /        |        \
                new.txt   test.txt     bak
                 /           |           \
            ----------   -----------  ----------
            | fa49b0 |   | 1f7a7a  |  | d8329f |
            | cde016 |   | 634a39  |  | 91f91d |
            |new file|   |version 2|  |  tree  |
            ----------   -----------  ----------
                                          |
                                       test.txt
                                          |
                                     -----------
                                     | 83baae  |
                                     | d5353c  |
                                     |version 1|
                                     -----------

           Figure 150. The content structure of your current Git data.
         */

        //> You have three trees that specify the different snapshots of your project that you want to track,
        //> but the earlier problem remains: you must remember all three SHA-1 values in order to recall the snapshots.
        //> You also don’t have any information about who saved the snapshots, when they were saved, or why they were saved.
        //> This is the basic information that the commit object stores for you.
        //>
        //> To create a commit object, you call commit-tree and specify a single tree SHA-1 and which commit objects,
        //> if any, directly preceded it.
        //>
        //> Start with the first tree you wrote:

        //> $ echo 'first commit' | git commit-tree d8329f
        //>     fdf4fc3344e67ab068f836878b6c4951e3b15f3d

        // Set the author and committer on the manager:
        git.setAuthorAndCommitter("Luke Machowski");

        // Override the current time so that commits have predictable results:
        ZonedDateTime nowOverride = ZonedDateTime.of(2017, 5, 1, 8, 0, 0, 0, ZoneId.of("GMT+2"));
        git.setNowOverride(nowOverride);

        // Commit the first tree:
        Commit firstCommit = git.commit_tree(version1Tree.hash, "first commit");
        Assert.assertNotNull(firstCommit);
        Assert.assertEquals("bc8284884aba8ec5dffab515898cd3cd957e5695", firstCommit.hash.value);


        //> You will get a different hash value because of different creation time and author data.
        //> Replace commit and tag hashes with your own checksums further in this chapter.

        //> Now you can look at your new commit object with cat-file:
        //> $ git cat-file -p fdf4fc3
        //>     tree d8329fc1cc938780ffdd9f94e0d364e0ea74f579
        //>     author Scott Chacon <schacon@gmail.com> 1243040974 -0700
        //>     committer Scott Chacon <schacon@gmail.com> 1243040974 -0700
        //>
        //>     first commit
        Assert.assertSame(firstCommit, git.cat_file(firstCommit.hash));
        Assert.assertEquals("first commit", firstCommit.message);
        Assert.assertEquals("Luke Machowski", firstCommit.author);
        Assert.assertEquals(nowOverride, firstCommit.authorTimeStamp);
        Assert.assertEquals("Luke Machowski", firstCommit.committer);
        Assert.assertEquals(nowOverride, firstCommit.committerTimeStamp);
        Assert.assertEquals("91f91dfa30a202f88d959213deb8dfa1e81e3fdd", firstCommit.treeHashValue);
        Assert.assertEquals(version1Tree.hash.value, firstCommit.treeHashValue);


        //> The format for a commit object is simple:
        //> it specifies the top-level tree for the snapshot of the project at that point;
        //> the author/committer information (which uses your user.name and user.email configuration settings and a timestamp);
        //> a blank line,
        //> and then the commit message.

        //> Next, you’ll write the other two commit objects, each referencing the commit that came directly before it:

        //> $ echo 'second commit' | git commit-tree 0155eb -p fdf4fc3
        //>     cac0cab538b970a37ea1e769cbbde608743bc96d

        // Commit the second tree:
        Commit secondCommit = git.commit_tree(version2Tree.hash, "second commit", firstCommit.hash);
        Assert.assertNotNull(secondCommit);
        Assert.assertEquals("c1e4cf563ad74162a7fc050fbd7e866750b918c3", secondCommit.hash.value);

        // Make sure the parent commit was referenced:
        Assert.assertEquals(1, secondCommit.parentCommitHashValues.length);
        Assert.assertEquals("bc8284884aba8ec5dffab515898cd3cd957e5695", secondCommit.parentCommitHashValues[0]);

        //> $ echo 'third commit'  | git commit-tree 3c4e9c -p cac0cab
        //>     1a410efbd13591db07496601ebc7a059dd55cfe9

        // Commit the third tree:
        Commit thirdCommit = git.commit_tree(version3Tree.hash, "third commit", secondCommit.hash);
        Assert.assertNotNull(thirdCommit);
        Assert.assertEquals("a93eb7a3da86e4e938505bb95b0f9a716d4657f2", thirdCommit.hash.value);

        // Make sure the parent commit was referenced:
        Assert.assertEquals(1, thirdCommit.parentCommitHashValues.length);
        Assert.assertEquals("c1e4cf563ad74162a7fc050fbd7e866750b918c3", thirdCommit.parentCommitHashValues[0]);


        //> Each of the three commit objects points to one of the three snapshot trees you created.

        //> Oddly enough, you have a real Git history now that you can view with the git log command,
        //> if you run it on the last commit SHA-1:

        //> $ git log --stat 1a410e
        //>     commit 1a410efbd13591db07496601ebc7a059dd55cfe9
        //>     Author: Scott Chacon <schacon@gmail.com>
        //>     Date:   Fri May 22 18:15:24 2009 -0700
        //>
        //>     	third commit
        //>
        //>      bak/test.txt | 1 +
        //>      1 file changed, 1 insertion(+)
        //>
        //>     commit cac0cab538b970a37ea1e769cbbde608743bc96d
        //>     Author: Scott Chacon <schacon@gmail.com>
        //>     Date:   Fri May 22 18:14:29 2009 -0700
        //>
        //>     	second commit
        //>
        //>      new.txt  | 1 +
        //>      test.txt | 2 +-
        //>      2 files changed, 2 insertions(+), 1 deletion(-)
        //>
        //>     commit fdf4fc3344e67ab068f836878b6c4951e3b15f3d
        //>     Author: Scott Chacon <schacon@gmail.com>
        //>     Date:   Fri May 22 18:09:34 2009 -0700
        //>
        //>         first commit
        //>
        //>      test.txt | 1 +
        //>      1 file changed, 1 insertion(+)

        // Get the log of changes:
        Log log = git.asPorcelainCommands().log(thirdCommit.hash);
        Assert.assertNotNull(log);
        Assert.assertEquals(3, log.size());

        Assert.assertEquals(thirdCommit.hash.value, log.get(0).commitHashValue);
        Assert.assertEquals(thirdCommit.author, log.get(0).author);
        Assert.assertEquals(thirdCommit.authorTimeStamp, log.get(0).authorTimeStamp);
        Assert.assertEquals(thirdCommit.committer, log.get(0).committer);
        Assert.assertEquals(thirdCommit.committerTimeStamp, log.get(0).committerTimeStamp);
        Assert.assertEquals(thirdCommit.message, log.get(0).message);

        Assert.assertEquals(secondCommit.hash.value, log.get(1).commitHashValue);
        Assert.assertEquals(secondCommit.author, log.get(1).author);
        Assert.assertEquals(secondCommit.authorTimeStamp, log.get(1).authorTimeStamp);
        Assert.assertEquals(secondCommit.committer, log.get(1).committer);
        Assert.assertEquals(secondCommit.committerTimeStamp, log.get(1).committerTimeStamp);
        Assert.assertEquals(secondCommit.message, log.get(1).message);

        Assert.assertEquals(firstCommit.hash.value, log.get(2).commitHashValue);
        Assert.assertEquals(firstCommit.author, log.get(2).author);
        Assert.assertEquals(firstCommit.authorTimeStamp, log.get(2).authorTimeStamp);
        Assert.assertEquals(firstCommit.committer, log.get(2).committer);
        Assert.assertEquals(firstCommit.committerTimeStamp, log.get(2).committerTimeStamp);
        Assert.assertEquals(firstCommit.message, log.get(2).message);

        //> Amazing. You’ve just done the low-level operations to build up a Git history without using any of the front end commands.
        //> This is essentially what Git does when you run the git add and git commit commands –
        //> it stores blobs for the files that have changed,
        //> updates the index,
        //> writes out trees,
        //> and writes commit objects that reference the top-level trees and the commits that came immediately before them.
        //> These three main Git objects – the blob, the tree, and the commit – are initially stored as separate files in your .git/objects directory.

        //> Here are all the objects in the example directory now, commented with what they store:

        //> $ find .git/objects -type f
        //>     .git/objects/01/55eb4229851634a0f03eb265b69f5a2d56f341 # tree 2
        //>     .git/objects/1a/410efbd13591db07496601ebc7a059dd55cfe9 # commit 3
        //>     .git/objects/1f/7a7a472abf3dd9643fd615f6da379c4acb3e3a # test.txt v2
        //>     .git/objects/3c/4e9cd789d88d8d89c1073707c3585e41b0e614 # tree 3
        //>     .git/objects/83/baae61804e65cc73a7201a7252750c76066a30 # test.txt v1
        //>     .git/objects/ca/c0cab538b970a37ea1e769cbbde608743bc96d # commit 2
        //>     .git/objects/d6/70460b4b4aece5915caf5c68d12f560a9fe3e4 # 'test content'
        //>     .git/objects/d8/329fc1cc938780ffdd9f94e0d364e0ea74f579 # tree 1
        //>     .git/objects/fa/49b077972391ad58037050f2a75f74e3671e92 # new.txt
        //>     .git/objects/fd/f4fc3344e67ab068f836878b6c4951e3b15f3d # commit 1

        Assert.assertSame(thirdCommit, repo.database.objects.get("a93eb7a3da86e4e938505bb95b0f9a716d4657f2"));
        Assert.assertSame(secondCommit, repo.database.objects.get("c1e4cf563ad74162a7fc050fbd7e866750b918c3"));
        Assert.assertSame(firstCommit, repo.database.objects.get("bc8284884aba8ec5dffab515898cd3cd957e5695"));
        Assert.assertSame(version3Tree, repo.database.objects.get("17aa869614d4876f6646bcea558496d5a720f728"));
        Assert.assertSame(version2Tree, repo.database.objects.get("654725b11e4186d9807c3396c2c651b927acb68f"));
        Assert.assertSame(version1Tree, repo.database.objects.get("91f91dfa30a202f88d959213deb8dfa1e81e3fdd"));
        Assert.assertSame(git.cat_file(test_txt_version_2_hash), repo.database.objects.get("cde016cb1660f6922680085eb1047d6823e7ab06"));
        Assert.assertSame(git.cat_file(test_txt_version_1_hash), repo.database.objects.get("d5353cdaa0518db00beb7dd5c334860f1e58f1f5"));
        Assert.assertSame(git.cat_file(git.hash_object(new Blob("new file".getBytes(StandardCharsets.UTF_8)))), repo.database.objects.get("634a399f5c7cb5b2c22aeedb32e7b49cd2fb9623"));

        //> If you follow all the internal pointers, you get an object graph something like this:

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
    }

    @Test
    public void Hash_Examples()
    {
        RepoHandler git = NanoVersionControl.newHandler();
        git.init();

        Hash hash;

        hash = git.hash_object_write_string("Hello World");
        Assert.assertEquals("ff31809acd7e6662279e013511a68523e9ae27dd", hash.value);

        hash = git.hash_object_write_string("hello world");
        Assert.assertEquals("3c2c784a800a17999a0d6ea1e86075a9a1169df2", hash.value);

        hash = git.hash_object_write_string("");
        Assert.assertEquals("593f4708db84ac8fd0f5cc47c634f38c013fe9e4", hash.value);

        hash = git.hash_object_write_string(null);
        Assert.assertEquals("593f4708db84ac8fd0f5cc47c634f38c013fe9e4", hash.value);

        hash = git.hash_object_write_string(" ");
        Assert.assertEquals("48028baf4dfbecbaf40405079b129ca1fd355b44", hash.value);

        hash = git.hash_object_write_string("test content");
        Assert.assertEquals("04d7fa923aad00484743abdb0170ce0064f1ab15", hash.value);

        hash = git.hash_object_write_string("what is up, doc?");
        Assert.assertEquals("2e3bccfbdc592d426467a0014587edfd4e3212af", hash.value);

        hash = git.hash_object_write_string("what is up, doc?\n");
        Assert.assertEquals("35ac7c46910090c31c6e243d5a794407bf270142", hash.value);

        hash = git.hash_object_write_string("what is up, doc?\r\n");
        Assert.assertEquals("a620f692fded9a873c2f8185631943a1522c574a", hash.value);

        hash = git.hash_object_write_string("Nano Version Control");
        Assert.assertEquals("5a52951985a0b0f5d2762abdc01569cc75e1eea4", hash.value);

        hash = git.hash_object_write_string("nano version control");
        Assert.assertEquals("38d8aa0d6ee8dd49ee73a6af74612812dd540a33", hash.value);

        hash = git.hash_object_write_string("NanoVersionControl");
        Assert.assertEquals("f57267fcdfe6056c8dd6c86b76fb2deaae588d94", hash.value);

        hash = git.hash_object_write_string("Lukasz Machowski");
        Assert.assertEquals("ed76db8f2b729af0e6b5417f4f6b144835981037", hash.value);

        hash = git.hash_object_write_string("Lukasz Antoni Machowski");
        Assert.assertEquals("d1c1d54e0b5f486269fd3ac477b3a1f15d4e1161", hash.value);
    }

}
