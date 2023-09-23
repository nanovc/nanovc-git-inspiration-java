package io.git.nanovc;

import java.util.Comparator;

public class TreeEntryComparator implements Comparator<TreeEntry> {

    /**
     * Compares its two tree entries for order.
     * Will order blobs over tree
     */
    @Override
    public int compare(TreeEntry treeEntry1, TreeEntry treeEntry2) {
        if (treeEntry1.objectType.equals(ObjectType.BLOB) && !treeEntry2.objectType.equals(ObjectType.BLOB)) return 1;
        return 0;
    }
}
