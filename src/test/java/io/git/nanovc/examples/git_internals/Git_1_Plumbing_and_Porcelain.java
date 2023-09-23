package io.git.nanovc.examples.git_internals;

import io.git.nanovc.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * 10.1 Git Internals - Plumbing and Porcelain
 * https://git-scm.com/book/en/v2/Git-Internals-Plumbing-and-Porcelain
 * <p>
 * This set of tests follows the examples explained in the official Git documentation.
 * <p>
 * It demonstrates how the same structure can be replicated with Nano Version Control.
 * <p>
 * NOTE: We follow the convention where all text that comes from the official Git documentation is prefixed with //>
 */
public class Git_1_Plumbing_and_Porcelain extends NanoVersionControlTestsBase
{
    /**
     * Plumbing and Porcelain asd
     */
    @Test
    public void Plumbing_and_Porcelain()
    {

        //>
        //> Plumbing and Porcelain
        //>
        //> This book covers how to use Git with 30 or so verbs such as checkout, branch, remote, and so on.
        //> But because Git was initially a toolkit for a VCS rather than a full user-friendly VCS,
        //> it has a bunch of verbs that do low-level work and were designed to be chained together UNIX style or
        //> called from scripts.
        //>
        //> These commands are generally referred to as “plumbing” commands, and the more
        //> user-friendly commands are called “porcelain” commands.
        //>
        //> The book’s first nine chapters deal almost exclusively with porcelain commands.
        //> But in this chapter, you’ll be dealing mostly with the lower-level plumbing commands,
        //> because they give you access to the inner workings of Git,
        //> and help demonstrate how and why Git does what it does.
        //>
        //> Many of these commands aren’t meant to be used manually on the command line,
        //> but rather to be used as building blocks for new tools and custom scripts.
        //>
        //> When you run git init in a new or existing directory,
        //> Git creates the .git directory, which is where almost everything that Git stores and manipulates is located.
        //> If you want to back up or clone your repository, copying this single directory elsewhere gives you nearly everything you need.
        //> This entire chapter basically deals with the stuff in this directory.
        //>
        //> Here’s what it looks like:
        //>
        /*>
            $ ls -F1
            HEAD
            config*
            description
            hooks/
            info/
            objects/
            refs/
         */

        // The Nano Engine can be thought of as the Git Command Line Interface (CLI):
        RepoHandler git = NanoVersionControl.newHandler();

        // Call git init:
        Repo repo = git.init();

        // The repo.database represents the .git folder in a git repository.
        Database dotGit = repo.database;

        // Inspect the structure of the .git database:
        Assert.assertNotNull(dotGit.HEAD); // HEAD points to master after an init.
        Assert.assertNotNull(dotGit.config);
        Assert.assertNotNull(dotGit.description);
        Assert.assertNotNull(dotGit.hooks);
        Assert.assertNotNull(dotGit.info);
        Assert.assertNotNull(dotGit.objects);
        Assert.assertNotNull(dotGit.refs);

        // You may see some other files in there,
        // but this is a fresh git init repository
        // – it’s what you see by default.
        //
        // The description file is only used by the GitWeb program, so don’t worry about it.
        // The config file contains your project-specific configuration options,
        // and the info directory keeps a global exclude file for ignored patterns that you don’t want to track in a .gitignore file.
        // The hooks directory contains your client- or server-side hook scripts, which are discussed in detail in Git Hooks.

        // This leaves four important entries:
        //      the HEAD and
        //      (yet to be created) index files,
        //      and the objects
        //      and refs directories.
        // These are the core parts of Git.
        //
        // The objects directory stores all the content for your database,
        // the refs directory stores pointers into commit objects in that data (branches),
        // the HEAD file points to the branch you currently have checked out,
        // and the index file is where Git stores your staging area information.
        // You’ll now look at each of these sections in detail to see how Git operates.
    }

}
