package io.git.nanovc;

/**
 * The status of a repository.
 *
 * https://git-scm.com/docs/git-status
 */
public class Status
{
    /**
     * The status entries for the content when comparing the working area to the committed area.
     */
    public StatusEntryCollection workingAreaEntries = new StatusEntryCollection();

    /**
     * The status entries for the content when comparing the staging area to the committed area.
     */
    public StatusEntryCollection stagingAreaEntries = new StatusEntryCollection();
}
