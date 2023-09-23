package io.git.nanovc;

import java.util.Set;

/**
 * Info about the repo.
 * The info directory keeps a global exclude file for ignored patterns that you don’t want to track in a .gitignore file.
 */
public class Info
{
    /**
     * The info directory keeps a global exclude file for ignored patterns that you don’t want to track in a .gitignore file.
     */
    public Set<String> exclude;
}
