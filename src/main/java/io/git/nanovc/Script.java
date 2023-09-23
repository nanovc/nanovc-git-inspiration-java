package io.git.nanovc;

/**
 * A script that can be used to execute interesting actions when something occurs in the repo.
 * Scripts are usually used by {@link Hooks}.
 */
public class Script
{
    /**
     * The code for the Script.
     * This is considered to be JavaScript.
     */
    public String code;

    /**
     * Flags whether the script is enabled or not.
     */
    public boolean enabled;
}
