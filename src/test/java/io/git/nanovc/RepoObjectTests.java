package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Tests that repo objects return the expected content and object types.
 */
public class RepoObjectTests extends NanoVersionControlTestsBase
{
    /**
     * Tests that blobs return the expected object type and content.
     */
    @Test
    public void BlobTests() throws IOException
    {
        // Create a blob:
        Blob blob = new Blob();

        // Make sure it returns the expected object type:
        Assert.assertEquals(ObjectType.BLOB, blob.getObjectType());

        // Set null content:
        blob.content = null;

        // Make sure that we get an empty byte array when the content is null:
        byte[] nullBytes = blob.getByteArray();
        Assert.assertNotNull(nullBytes);
        Assert.assertEquals(4, nullBytes.length);


        // Set blank content:
        blob.content = new byte[0];

        // Make sure that we get an empty byte array when the content is blank:
        byte[] blankBytes = blob.getByteArray();
        Assert.assertNotNull(blankBytes);
        Assert.assertNotSame(blob.content, blankBytes); // We don't expect the bytes to match the content because of serialization concerns.
        Assert.assertEquals(4, blankBytes.length);
        Assert.assertNotSame(nullBytes, blankBytes);


        // Set some content:
        blob.content = new byte[]{(byte) 1, (byte) 2, (byte) 3};

        // Make sure that we get some content:
        byte[] threeBytes = blob.getByteArray();
        Assert.assertNotNull(blankBytes);
        Assert.assertNotSame(blob.content, threeBytes); // We don't expect the bytes to match the content because of serialization concerns.
        Assert.assertEquals(7, threeBytes.length);
        Assert.assertNotSame(nullBytes, threeBytes);
        Assert.assertNotSame(blankBytes, threeBytes);

        // Create a stream of the content:
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(threeBytes);
                DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)
        )
        {
            // Create a new blob to read into:
            Blob streamBlob = new Blob();
            streamBlob.readContentFromStream(dataInputStream);

            // Make sure the structure is as expected:
            Assert.assertArrayEquals(blob.content, streamBlob.content);
        }
    }


    /**
     * Tests that trees return the expected object type and content.
     */
    @Test
    public void TreeTests() throws IOException
    {
        // Create a blob:
        Tree tree = new Tree();

        // Make sure it returns the expected object type:
        Assert.assertEquals(ObjectType.TREE, tree.getObjectType());

        // Make sure that there are no tree entries by default:
        Assert.assertNotNull(tree.entries);
        Assert.assertEquals(0, tree.entries.size());

        // Create a blob tree entry:
        TreeEntry entryBlob = new TreeEntry();
        entryBlob.hashValue = "1111111111222222222233333333334444444444";
        entryBlob.name = "entryBlob";
        entryBlob.objectType = ObjectType.BLOB;
        tree.entries.add(entryBlob);

        // Create a commit tree entry:
        TreeEntry entryCommit = new TreeEntry();
        entryCommit.hashValue = "2222222222333333333344444444445555555555";
        entryCommit.name = "entryCommit";
        entryCommit.objectType = ObjectType.COMMIT;
        tree.entries.add(entryCommit);

        // Create a tree tree entry:
        TreeEntry entryTree = new TreeEntry();
        entryTree.hashValue = "3333333333444444444455555555556666666666";
        entryTree.name = "entryTree";
        entryTree.objectType = ObjectType.TREE;
        tree.entries.add(entryTree);

        // Get the bytes for the tree:
        byte[] treeBytes = tree.getByteArray();
        Assert.assertNotNull(treeBytes);

        // Create a stream of the content:
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(treeBytes);
                DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)
        )
        {
            // Create a new tree to read into:
            Tree streamTree = new Tree();
            streamTree.readContentFromStream(dataInputStream);

            // Make sure the structure is as expected:
            Assert.assertNotNull(streamTree.entries);
            Assert.assertEquals(tree.entries.size(), streamTree.entries.size());
            for (int i = 0; i < streamTree.entries.size(); i++)
            {
                // Get both entries:
                TreeEntry expectedEntry = tree.entries.get(i);
                TreeEntry streamEntry = streamTree.entries.get(i);

                // Make sure the entries are as expected:
                Assert.assertEquals(expectedEntry.objectType, streamEntry.objectType);
                Assert.assertEquals(expectedEntry.name, streamEntry.name);
                Assert.assertEquals(expectedEntry.hashValue, streamEntry.hashValue);
            }
        }

    }

    /**
     * Tests that commits return the expected object type and content.
     */
    @Test
    public void CommitTests() throws IOException
    {
        // Create a blob:
        Commit commit = new Commit();

        // Make sure it returns the expected object type:
        Assert.assertEquals(ObjectType.COMMIT, commit.getObjectType());

        // Create a commit:
        commit.treeHashValue = "1111111111222222222233333333334444444444";
        commit.author = "Luke";
        commit.authorTimeStamp = ZonedDateTime.of(2017,4,29, 8,0,0, 0, ZoneId.of("GMT+2"));
        commit.committer = "admin";
        commit.committerTimeStamp = ZonedDateTime.of(2017,4,29, 9,0,0, 0, ZoneId.of("GMT+2"));
        commit.message = "First Commit";

        // Get the bytes for the tree:
        byte[] commitBytes = commit.getByteArray();
        Assert.assertNotNull(commitBytes);

        // Create a stream of the content:
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(commitBytes);
                DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)
        )
        {
            // Create a new commit to read into:
            Commit streamCommit = new Commit();
            streamCommit.readContentFromStream(dataInputStream);

            // Make sure the structure is as expected:
            Assert.assertEquals(commit.author, streamCommit.author);
            Assert.assertEquals(commit.authorTimeStamp, streamCommit.authorTimeStamp);
            Assert.assertEquals(commit.committer, streamCommit.committer);
            Assert.assertEquals(commit.committerTimeStamp, streamCommit.committerTimeStamp);
            Assert.assertEquals(commit.treeHashValue, streamCommit.treeHashValue);
            Assert.assertEquals(commit.message, streamCommit.message);
        }


        // Create another commit:
        Commit commit2 = new Commit();

        // Make sure it returns the expected object type:
        Assert.assertEquals(ObjectType.COMMIT, commit2.getObjectType());

        // Create a commit:
        commit2.parentCommitHashValues = new String[] {"0000000000111111111122222222223333333333"};
        commit2.treeHashValue = "2222222222333333333344444444445555555555";
        commit2.author = "Luke";
        commit2.authorTimeStamp = ZonedDateTime.of(2017,4,30, 8,0,0, 0, ZoneId.of("GMT+2"));
        commit2.committer = "admin";
        commit2.committerTimeStamp = ZonedDateTime.of(2017,4,30, 9,0,0, 0, ZoneId.of("GMT+2"));
        commit2.message = "Second Commit";

        // Get the bytes for the tree:
        byte[] commitBytes2 = commit2.getByteArray();
        Assert.assertNotNull(commitBytes2);

        // Create a stream of the content:
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(commitBytes2);
                DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)
        )
        {
            // Create a new commit to read into:
            Commit streamCommit2 = new Commit();
            streamCommit2.readContentFromStream(dataInputStream);

            // Make sure the structure is as expected:
            Assert.assertEquals(commit2.author, streamCommit2.author);
            Assert.assertEquals(commit2.authorTimeStamp, streamCommit2.authorTimeStamp);
            Assert.assertEquals(commit2.committer, streamCommit2.committer);
            Assert.assertEquals(commit2.committerTimeStamp, streamCommit2.committerTimeStamp);
            Assert.assertEquals(commit2.treeHashValue, streamCommit2.treeHashValue);
            Assert.assertEquals(commit2.message, streamCommit2.message);
            Assert.assertArrayEquals(commit2.parentCommitHashValues, streamCommit2.parentCommitHashValues);
        }
    }

}
