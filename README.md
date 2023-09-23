# nanovc-git-inspiration-java
![NanoVC Git Inspired Java Implementation](https://github.com/nanovc/nanovc-git-inspiration-java/workflows/NanoVC%20Git%20Inspiration%20Java%20Implementation/badge.svg)

Java implementation of Nano Version Control inspired by Git.

We copied the git design as described in the Git Pro hand book, "Git Internals" chapter.
We did this so that we could learn from the git design as-is.


The web page describing Nano Version Control is here:
http://nanovc.io

To understand the basic idea, look at this blog post:
http://nanovc.io/2020/05/25/the-basic-idea/


### Getting Started Example

```@Test
 public void testHelloWorld()
 {
        // Get a new engine for a repo:
        RepoHandler nano = NanoVersionControl.newHandler();

        // Create a new repository:
        Repo repo = nano.init();
        nano.setAuthorAndCommitter("test");

        // Add content to the working area:
        ContentBase rootContent = repo.workingArea.putContent("/", (byte) 123);

        // Add the changed content to the staging area:
        nano.addAll(true);

        Commit commit = nano.commitAll("test", true);
        Assert.assertNotNull(commit);
        Assert.assertNotNull(commit.hash.value);
 }
```
