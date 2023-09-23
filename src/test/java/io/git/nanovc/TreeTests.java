package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the creation of trees from repo paths.
 */
public class TreeTests extends NanoVersionControlTestsBase
{
    /**
     * Tests the creation of trees for specific paths of content.
     */
    @Test
    public void testTrees()
    {
        RepoHandler repoHandler = NanoVersionControl.newHandler();
        repoHandler.init();


        repoHandler.putWorkingAreaContent("/record/Reporting Financial Institution/2017-ICIS.content", "1".getBytes());
        repoHandler.putWorkingAreaContent("/record/Reporting Financial Institution/2017-ICIS/1.content", "1".getBytes());
        repoHandler.putWorkingAreaContent("/record/Reporting Financial Institution/2017-ICIS/1.json", "1".getBytes());

        repoHandler.addAll(false);
        Tree rootTree = repoHandler.write_tree();

        Repo repo = repoHandler.repo;

        // record
        Assert.assertEquals(1, rootTree.entries.size());
        TreeEntry recordEntry = rootTree.entries.get(0);
        Assert.assertEquals("record", recordEntry.name);
        Assert.assertEquals(ObjectType.TREE, recordEntry.objectType);
        Tree recordTree = (Tree) repo.database.objects.map.get(recordEntry.hashValue);

        // Reporting Financial Institution:
        Assert.assertEquals(1, recordTree.entries.size());
        TreeEntry rfiEntry = recordTree.entries.get(0);
        Assert.assertEquals("Reporting Financial Institution", rfiEntry.name);
        Assert.assertEquals(ObjectType.TREE, rfiEntry.objectType);
        Tree rfiTree = (Tree) repo.database.objects.map.get(rfiEntry.hashValue);


        // 2017-ICIS:
        Assert.assertEquals(2, rfiTree.entries.size());
        TreeEntry icisBlobEntry = rfiTree.entries.get(0);
        Assert.assertEquals("2017-ICIS.content", icisBlobEntry.name);
        Assert.assertEquals(ObjectType.BLOB, icisBlobEntry.objectType);
        TreeEntry icisTreeEntry = rfiTree.entries.get(1);
        Assert.assertEquals("2017-ICIS", icisTreeEntry.name);
        Assert.assertEquals(ObjectType.TREE, icisTreeEntry.objectType);
        Tree icisTree = (Tree) repo.database.objects.map.get(icisTreeEntry.hashValue);


        // 1:
        Assert.assertEquals(2, icisTree.entries.size());
        TreeEntry oneContentEntry = icisTree.entries.get(0);
        TreeEntry oneJsonEntry = icisTree.entries.get(1);
        Assert.assertEquals("1.content", oneContentEntry.name);
        Assert.assertEquals("1.json", oneJsonEntry.name);
        Assert.assertEquals(ObjectType.BLOB, oneContentEntry.objectType);
        Assert.assertEquals(ObjectType.BLOB, oneJsonEntry.objectType);

    }



}
