package io.git.nanovc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * The interface for all patterns in Nano Version Control.
 * We use glob like syntax eg: *.json or **.json (to cross path boundaries).
 * A pattern lets you match many {@link Path}'s.
 * @param <TSelf> The specific type of pattern being implemented. This is needed so that we can get chained calls with the specific type of pattern.
 * @param <TPath> The specific type of path being matched. This is needed so that we can get chained calls with the specific type of pattern and path.
 */
public interface Pattern<TSelf extends Pattern<TSelf, TPath>, TPath extends Path<TPath>>
{

    /**
     * Finds all paths that match the pattern.
     * @param contentToSearch The content to search through
     * @return The content that matched the pattern.
     */
    <T extends Content> List<T> match(Collection<T> contentToSearch);

    /**
     * Finds all paths that match the pattern.
     * @param contentToSearch The content to search through
     * @return The stream of content that matched the pattern.
     */
    <T extends Content> Stream<T> matchStream(Stream<T> contentToSearch);

    /**
     * Checks whether the path of the content matches the pattern.
     *
     * @param contentToCheck The content to check for a match.
     * @return True if the content matches the pattern.
     */
    <T extends Content> boolean matches(T contentToCheck);

    /**
     * Checks whether the path matches this pattern.
     *
     * @param path The path to check against this pattern.
     * @return True if the path  matches the pattern.
     */
    boolean matches(TPath path);

    /**
     * Checks whether the path matches this pattern.
     *
     * @param absolutePath The absolute path (starting with /) to check against this pattern.
     * @return True if the path matches the pattern.
     */
    boolean matches(String absolutePath);

    /**
     * Gets another pattern that matches this pattern OR the other pattern.
     * @param otherPattern The other pattern that we also want to match optionally.
     * @return A new pattern that matches this pattern OR the other pattern.
     */
    TSelf or(String otherPattern);

    /**
     * Gets another pattern that matches this pattern AND the other pattern.
     * @param otherPattern The other pattern that we must match.
     * @return A new pattern that matches this pattern AND the other pattern.
     */
    TSelf and(String otherPattern);

    /**
     * Gets the regex that can be used to match on the absolute paths of the content.
     * @return The pattern that the content paths are matched with.
     */
    java.util.regex.Pattern asRegex();
}
