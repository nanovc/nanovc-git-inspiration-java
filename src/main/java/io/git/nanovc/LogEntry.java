package io.git.nanovc;

import java.time.ZonedDateTime;

/**
 * An entry in a log of changes in a Nano Version Control Repository.
 */
public class LogEntry
{
    /**
     * The SHA-1 hash values of the commit.
     */
    public String commitHashValue;

    /**
     * The author is the person who originally wrote the code.
     * The committer, on the other hand, is assumed to be the person who committed the code on behalf of the original
     * author.
     * This is important in Git because Git allows you to rewrite history, or apply patches on behalf of another
     * person.
     * You may be wondering what the difference is between author and committer. The author is the person who originally
     * wrote the patch, whereas the committer is the person who last applied the patch. So, if you send in a patch to a
     * project and one of the core members applies the patch, both of you get credit — you as the author and the core
     * member as the committer.
     * http://git-scm.com/book/ch2-3.html
     * http://stackoverflow.com/questions/18750808/difference-between-author-and-committer-in-git
     */
    public String author;

    /**
     * The date, time and time-zone when the author made the change.
     */
    public ZonedDateTime authorTimeStamp;

    /**
     * The committer is assumed to be the person who committed the code on behalf of the original author.
     * The author is the person who originally wrote the code.
     * This is important in Git because Git allows you to rewrite history, or apply patches on behalf of another
     * person.
     * You may be wondering what the difference is between author and committer. The author is the person who originally
     * wrote the patch, whereas the committer is the person who last applied the patch. So, if you send in a patch to a
     * project and one of the core members applies the patch, both of you get credit — you as the author and the core
     * member as the committer.
     * http://git-scm.com/book/ch2-3.html
     * http://stackoverflow.com/questions/18750808/difference-between-author-and-committer-in-git
     */
    public String committer;

    /**
     * The date, time and time-zone when the committer made the commit.
     */
    public ZonedDateTime committerTimeStamp;

    /**
     * The commit message which describes the change.
     */
    public String message;

    @Override
    public String toString()
    {
        return String.format("COMMIT by %s : %s -> %s", this.committer, this.committerTimeStamp.toString(), this.commitHashValue);
    }
}
