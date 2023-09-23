package io.git.nanovc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

/**
 * A base class for specific types of patterns in a {@link Repo}.
 * We use glob like syntax eg: *.json or **.json (to cross path boundaries).
 * Absolute paths start with a / ({@link PathBase#DELIMITER})
 * Relative paths do not start with a / ({@link PathBase#DELIMITER})
 * @param <TSelf> The specific type of pattern being implemented. This is needed so that we can get chained calls with the specific type of pattern.
 * @param <TPath> The specific type of path being matched. This is needed so that we can get chained calls with the specific type of pattern and path.
 */
public abstract class PatternBase<TSelf extends Pattern<TSelf, TPath>, TPath extends Path<TPath>> implements Pattern<TSelf, TPath>
{
    /**
     * The original glob pattern that was used to define this pattern.
     */
    protected final String globPattern;

    /**
     * The regular expression used to match the paths.
     */
    protected final java.util.regex.Pattern regex;

    /**
     * Creates a pattern that matches the given paths.
     * @param globPattern The pattern of paths to match. We use glob like syntax eg: *.json or **.json (to cross path boundaries).
     */
    public PatternBase(String globPattern)
    {
        this.globPattern = globPattern;
        this.regex = createRegex(globPattern);
    }

    /**
     * Finds all paths that match the pattern.
     *
     * @param contentToSearch The content to search through
     * @return The content that matched the pattern.
     */
    @Override
    public <T extends Content> List<T> match(Collection<T> contentToSearch)
    {
        // Create the result:
        List<T> matchedContent = new ArrayList<>();

        // Search through the content:
        for (T content : contentToSearch)
        {
            // Check whether the content matches the pattern:
            Matcher matcher = regex.matcher(content.getAbsolutePath());
            if (matcher.matches())
            {
                // We have a match.
                matchedContent.add(content);
            }
        }
        return matchedContent;
    }

    /**
     * Checks whether the path of the content matches the pattern.
     *
     * @param contentToCheck The content to check for a match.
     * @return True if the content matches the pattern.
     */
    @Override
    public <T extends Content> boolean matches(T contentToCheck)
    {
        // Check whether the content matches the pattern:
        Matcher matcher = regex.matcher(contentToCheck.getAbsolutePath());
        return matcher.matches();
    }

    /**
     * Checks whether the path matches this pattern.
     *
     * @param path The path to check against this pattern.
     * @return True if the path  matches the pattern.
     */
    @Override
    public boolean matches(TPath path)
    {
        // Check whether the content matches the pattern:
        Matcher matcher = regex.matcher(path.toAbsolutePath().toString());
        return matcher.matches();
    }

    /**
     * Checks whether the path matches this pattern.
     *
     * @param absolutePath The absolute path (starting with /) to check against this pattern.
     * @return True if the path matches the pattern.
     */
    @Override
    public boolean matches(String absolutePath)
    {
        // Check whether the content matches the pattern:
        Matcher matcher = regex.matcher(absolutePath);
        return matcher.matches();
    }

    /**
     * Finds all paths that match the pattern.
     *
     * @param contentToSearch The content to search through
     * @return The stream of content that matched the pattern.
     */
    @Override
    public <T extends Content> Stream<T> matchStream(Stream<T> contentToSearch)
    {
        return contentToSearch.filter(content ->
        {
            // Check whether the content matches the pattern:
            Matcher matcher = regex.matcher(content.getAbsolutePath());
            return matcher.matches();
        });
    }

    /**
     * Gets another pattern that matches this pattern OR the other pattern.
     *
     * @param otherPattern The other pattern that we also want to match optionally.
     * @return A new pattern that matches this pattern OR the other pattern.
     */
    @Override
    public TSelf or(String otherPattern)
    {
        return createInstance(String.format("(%s)|(%s)", this.globPattern, otherPattern));
    }

    /**
     * Gets another pattern that matches this pattern AND the other pattern.
     *
     * @param otherPattern The other pattern that we must match.
     * @return A new pattern that matches this pattern AND the other pattern.
     */
    @Override
    public TSelf and(String otherPattern)
    {
        return createInstance(String.format("(%s)(%s)", this.globPattern, otherPattern));
    }

    /**
     * Gets the regex that can be used to match on the absolute paths of the content.
     *
     * @return The pattern that the content paths are matched with.
     */
    @Override
    public java.util.regex.Pattern asRegex()
    {
        return regex;
    }

    /**
     * A factory method to create a new instance of the specific path.
     * @param globPattern The pattern of paths to match. We use glob like syntax eg: *.json or **.json (to cross path boundaries).
     * @return The new instance at the given path.
     */
    protected abstract TSelf createInstance(String globPattern);

    /**
     * Creates a regex that can be used for matching paths against their absolute values.
     * We use glob like syntax eg: *.json or **.json (to cross path boundaries).
     * @param globPattern The pattern of paths to match. We use glob like syntax eg: *.json or **.json (to cross path boundaries).
     * @return The regular expression for this glob pattern.
     */
    public static java.util.regex.Pattern createRegex(String globPattern)
    {
        // Short circuit for empty glob patterns:
        if (globPattern == null || globPattern.isEmpty()) return java.util.regex.Pattern.compile("");

        // Replace wild cards:
        StringBuilder stringBuilder = new StringBuilder();

        // We need a scanning solution because we can't do a two stage replacement with * and **.
        // NOTE: The Java Scanner class turned out to be awkward because of needing to switch delimiters.
        // Went with a hand-rolled scanner instead.
        for (int index = 0, length = globPattern.length(), lastIndex = length - 1; index < length; index++)
        {
            // Get the current character:
            char currentChar = globPattern.charAt(index);

            // Check whether the next character is a *:
            if (currentChar == '*')
            {
                // We have a star.

                // Look at the next character:
                if (index < lastIndex)
                {
                    // We are not at the end yet.

                    // Get the next character:
                    char nextChar = globPattern.charAt(index + 1);

                    // Look for another star:
                    if (nextChar == '*')
                    {
                        // We got a **.

                        // Consume the extra *:
                        index++;

                        // Append the pattern:
                        stringBuilder.append(".*");

                        // Go to the next character:
                        continue;
                    }

                }
                // If we get here then we got a single *.

                // Append the pattern:
                // NOT the delimiter:
                stringBuilder.append("[^\\");
                stringBuilder.append(PathBase.DELIMITER);
                stringBuilder.append("]");
                stringBuilder.append("*");
            }
            else if (currentChar == '.')
            {
                // We got a dot.

                // Append the pattern:
                stringBuilder.append("\\.");
            }
            else
            {
                // This is not a star.
                // Append it to the output:
                stringBuilder.append(currentChar);
            }
        }
        // Now we have processed the glob pattern.

        // Always match the absolute starting delimiter:
        if (!stringBuilder.substring(0, PathBase.DELIMITER.length()).equals(PathBase.DELIMITER))
        {
            // We don't have the path delimiter at the beginning.
            // Always start with the path delimiter for absolute paths.
            stringBuilder.insert(0, PathBase.DELIMITER);
        }
        // Now we have the regex that we want.

        return java.util.regex.Pattern.compile(stringBuilder.toString());
    }

}
