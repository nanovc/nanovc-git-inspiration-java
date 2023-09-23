package io.git.nanovc;

/**
 * An entry for the status of content in the repository.
 */
public class StatusEntry
{
    /**
     * The absolute path of the content.
     */
    public String absolutePath;

    /**
     * The state of the content.
     */
    public ContentState state = ContentState.UNMODIFIED;
}
