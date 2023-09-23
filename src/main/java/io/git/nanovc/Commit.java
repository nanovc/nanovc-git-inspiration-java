package io.git.nanovc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * A commit object stores information about who saved the snapshots,
 * when they were saved, and why they were saved.
 * <p>
 * The format for a commit object is simple:
 * it specifies the top-level tree for the snapshot of the project at that point;
 * the author/committer information (which uses your user.name and user.email configuration settings and a timestamp);
 * a blank line,
 * and then the commit message.
 */
public class Commit extends RepoObject
{

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
     * The SHA-1 hash value for the root of the tree for this commit.
     */
    public String treeHashValue;

    /**
     * The commit message which describes the change.
     */
    public String message;

    /**
     * The SHA-1 hash values of the parent commits.
     * If this is null or empty then this represents the root commit.
     * <p>
     * A commit object may have any number of parents.
     * With exactly one parent, it is an ordinary commit.
     * Having more than one parent makes the commit a merge between several lines of history.
     * Initial (root) commits have no parents.
     */
    public String[] parentCommitHashValues;

    /**
     * Creates a new commit.
     * You still need to set the hash and commit details.
     */
    public Commit()
    {
    }

    /**
     * Creates a new commit.
     * You still need to set the hash.
     *
     * @param author             The author of this commit. The author is the person who originally wrote the code.
     * @param authorTimeStamp    The timestamp when the author made the change.
     * @param committer          The committer. The committer is assumed to be the person who committed the code on behalf of the original author.
     * @param committerTimeStamp The timestamp when the committer made the commit.
     * @param treeHashValue      The SHA-1 hash value for the root of the tree for this commit.
     * @param message            The commit message which describes the change.
     */
    public Commit(String author, ZonedDateTime authorTimeStamp, String committer, ZonedDateTime committerTimeStamp, String treeHashValue, String message)
    {
        this.author = author;
        this.authorTimeStamp = authorTimeStamp;
        this.committer = committer;
        this.committerTimeStamp = committerTimeStamp;
        this.treeHashValue = treeHashValue;
        this.message = message;
    }

    /**
     * Creates a new commit.
     * You still need to set the hash.
     *
     * @param author                 The author of this commit. The author is the person who originally wrote the code.
     * @param authorTimeStamp        The timestamp when the author made the change.
     * @param committer              The committer. The committer is assumed to be the person who committed the code on behalf of the original author.
     * @param committerTimeStamp     The timestamp when the committer made the commit.
     * @param treeHashValue          The SHA-1 hash value for the root of the tree for this commit.
     * @param message                The commit message which describes the change.
     * @param parentCommitHashValues The hashes of the parent commits for this new commit. A commit object may have any number of parents. With exactly one parent, it is an ordinary commit. Having more than one parent makes the commit a merge between several lines of history. Initial (root) commits have no parents.
     */
    public Commit(String author, ZonedDateTime authorTimeStamp, String committer, ZonedDateTime committerTimeStamp, String treeHashValue, String message, String... parentCommitHashValues)
    {
        this(author, authorTimeStamp, committer, committerTimeStamp, treeHashValue, message);
        this.parentCommitHashValues = parentCommitHashValues;
    }

    /**
     * Creates a new commit with the given values.
     *
     * @param hash               The hash for the commit.
     * @param author             The author of the commit. The author is the person who originally wrote the code.
     * @param authorTimeStamp    The timestamp when the author made the change.
     * @param committer          The committer. The committer is assumed to be the person who committed the code on behalf of the original author.
     * @param committerTimeStamp The timestamp when the committer made the commit.
     * @param treeHashValue      The SHA-1 hash value for the root of the tree for this commit.
     * @param message            The commit message which describes the change.
     */
    public Commit(Hash hash, String author, ZonedDateTime authorTimeStamp, String committer, ZonedDateTime committerTimeStamp, String treeHashValue, String message)
    {
        super(hash);
        this.author = author;
        this.authorTimeStamp = authorTimeStamp;
        this.committer = committer;
        this.committerTimeStamp = committerTimeStamp;
        this.treeHashValue = treeHashValue;
        this.message = message;
    }

    /**
     * Creates a new commit with the given values.
     *
     * @param hash                  The hash for the commit.
     * @param author                The author of the commit. The author is the person who originally wrote the code.
     * @param authorTimeStamp       The timestamp when the author made the change.
     * @param committer             The committer. The committer is assumed to be the person who committed the code on behalf of the original author.
     * @param committerTimeStamp    The timestamp when the committer made the commit.
     * @param treeHashValue         The SHA-1 hash value for the root of the tree for this commit.
     * @param message               The commit message which describes the change.
     * @param parentCommitHashValues The hashes of the parent commits for this new commit. A commit object may have any number of parents. With exactly one parent, it is an ordinary commit. Having more than one parent makes the commit a merge between several lines of history. Initial (root) commits have no parents.
     */
    public Commit(Hash hash, String author, ZonedDateTime authorTimeStamp, String committer, ZonedDateTime committerTimeStamp, String treeHashValue, String message, String... parentCommitHashValues)
    {
        this(hash, author, authorTimeStamp, committer, committerTimeStamp, treeHashValue, message);
        this.parentCommitHashValues = parentCommitHashValues;
    }

    /**
     * The type of the object for nano version control.
     *
     * @return The type of the object for nano version control.
     */
    @Override
    public ObjectType getObjectType()
    {
        return ObjectType.COMMIT;
    }

    /**
     * Writes the content of this repo object into the stream.
     *
     * @param outputStream The output stream to write to.
     */
    @Override
    public void writeContentToStream(DataOutputStream outputStream) throws IOException
    {
        // Write out the number of parent commits that we have:
        int parentCommitCount = this.parentCommitHashValues == null ? 0 : this.parentCommitHashValues.length;
        outputStream.writeInt(parentCommitCount);

        // Check whether we have any parent commit hashes to write out:
        if (parentCommitCount > 0)
        {
            // We have parent commit hashes to write out.
            // Write out each parent commit:
            for (int i = 0; i < parentCommitCount; i++)
            {
                // Get the parent commit hash value:
                String parentCommitHashValue = this.parentCommitHashValues[i];

                // Write the hash of the parent commit:
                outputStream.writeUTF(parentCommitHashValue == null ? "" : parentCommitHashValue);
            }
        }
        // Now we have written the parent commit hashes.

        outputStream.writeUTF(this.treeHashValue);
        outputStream.writeUTF(this.author);
        outputStream.writeUTF(this.authorTimeStamp.toString());
        outputStream.writeUTF(this.committer);
        outputStream.writeUTF(this.committerTimeStamp.toString());
        outputStream.writeUTF(this.message);
    }

    /**
     * Reads the content of this repo object out of the stream.
     *
     * @param inputStream The input stream to read from.
     */
    @Override
    public void readContentFromStream(DataInputStream inputStream) throws IOException
    {
        // Read in how many parent commit hash values we have:
        int parentCommitCount = inputStream.readInt();

        // Create the array for parent commit hash values:
        this.parentCommitHashValues = new String[parentCommitCount];

        // Read out each parent commit:
        for (int i = 0; i < parentCommitCount; i++)
        {
            // Get the parent commit hash value:
            String parentCommitHashValue = inputStream.readUTF();

            // Write the hash of the parent commit:
            this.parentCommitHashValues[i] = parentCommitHashValue;
        }
        // Now we have read in the parent commit hash values.

        this.treeHashValue = inputStream.readUTF();
        this.author = inputStream.readUTF();
        this.authorTimeStamp = ZonedDateTime.parse(inputStream.readUTF());
        this.committer = inputStream.readUTF();
        this.committerTimeStamp = ZonedDateTime.parse(inputStream.readUTF());
        this.message = inputStream.readUTF();
    }

    @Override
    public String toString()
    {
        if (this.hash == null || this.hash.value == null || this.hash.value.isEmpty())
        {
            if (this.message != null)
            {
                return String.format("%s -> COMMIT by %s : %s", this.message, this.committer, this.committerTimeStamp);
            }
            else
            {
                return String.format("COMMIT by %s : %s", this.committer, this.committerTimeStamp);
            }
        }
        else
        {
            if (this.message != null)
            {
                return String.format("%s -> COMMIT by %s : %s -> Hash: %s, Tree Hash: %s", this.message, this.committer, this.committerTimeStamp, this.hash.value, this.treeHashValue);
            }
            else
            {
                return String.format("COMMIT by %s : %s -> Hash: %s, Tree Hash: %s", this.committer, this.committerTimeStamp, this.hash.value, this.treeHashValue);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        return hash.value != null ? hash.value.equals(commit.hash.value) : commit.hash.value == null;
    }

    @Override
    public int hashCode() {
        return hash.value != null ? hash.value.hashCode() : 0;
    }
}
