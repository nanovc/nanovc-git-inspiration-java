package io.git.nanovc;

/**
 * The state of content in a repository.
 *
 * https://git-scm.com/docs/git-status#_short_format
 */
public enum ContentState
{

    /**
     * The content has not been modified.
     */
    UNMODIFIED ("Unmodified", ""),

    /**
     * The content has been modified.
     */
    MODIFIED ("Modified", "M"),

    /**
     * The content has been added.
     */
    ADDED ("Added", "A"),

    /**
     * The content has been deleted.
     */
    DELETED ("Deleted", "D"),

    /**
     * The content has been renamed.
     */
    RENAMED ("Renamed", "R"),

    /**
     * The content has been copied.
     */
    COPIED ("Copied", "C"),

    /**
     * The content has been added.
     */
    UPDATED ("Updated but Unmerged", "U");

    /**
     * The long name for the content state.
     */
    private final String longName;


    /**
     * The short name for the content state.
     */
    private final String shortName;

    ContentState(String longName, String shortName)
    {
        this.longName = longName;
        this.shortName = shortName;
    }

}
