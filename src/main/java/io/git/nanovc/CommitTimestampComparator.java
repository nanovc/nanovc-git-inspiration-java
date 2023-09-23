package io.git.nanovc;

import java.util.Comparator;

/**
 * Used to compare commits to find which commit came first
 * This implementation requires the committerTimeStamp to be set
 */
public class CommitTimestampComparator implements Comparator<Commit> {

    @Override
    public int compare(Commit commit1, Commit commit2) {
        if (commit1.committerTimeStamp.isAfter(commit2.committerTimeStamp)) return -1;
        else if (commit1.committerTimeStamp.equals(commit2.committerTimeStamp)) return 0;
        else return 1;
    }

}
