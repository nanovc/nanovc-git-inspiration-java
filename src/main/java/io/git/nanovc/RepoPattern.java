package io.git.nanovc;

/**
 * A base class for specific types of patterns in a {@link Repo}.
 * We use glob like syntax eg: *.json or **.json (to cross path boundaries).
 * Absolute paths start with a / ({@link PathBase#DELIMITER})
 * Relative paths do not start with a / ({@link PathBase#DELIMITER})
 */
public class RepoPattern extends PatternBase<RepoPattern, RepoPath>
{

    /**
     * Creates a pattern that matches the given paths.
     * @param globPattern The pattern of paths to match. We use glob like syntax eg: *.json or **.json (to cross path boundaries).
     */
    public RepoPattern(String globPattern)
    {
        super(globPattern);
    }

    /**
     * A factory method to create a new instance of the specific path.
     *
     * @param globPattern The pattern of paths to match. We use glob like syntax eg: *.json or **.json (to cross path boundaries).
     * @return The new instance at the given path.
     */
    @Override
    protected RepoPattern createInstance(String globPattern)
    {
        return new RepoPattern(globPattern);
    }

    /**
     * Creates a repo pattern that matches the given {@link RepoPath}'s.
     * @param globPattern The pattern of paths to match. We use glob like syntax eg: *.json or **.json (to cross path boundaries).
     * @return The pattern that matches the given {@link RepoPath}'s.
     */
    public static RepoPattern matching(String globPattern)
    {
        return new RepoPattern(globPattern);
    }
}
