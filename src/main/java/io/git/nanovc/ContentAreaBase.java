package io.git.nanovc;

/**
 * An area where content resides.
 * If this was an actual git repository then this might be the file system where files would reside.
 * This is true for a working area, however it is logically true for the content of the index (staging area) too.
 * Most people would interact with a check-out of files into a working directory (working area) of the repo. That is a content area too.
 * @param <TContent> The type of content that is being stored in this content area.
 */
public abstract class ContentAreaBase<TContent extends Content> implements ContentArea<TContent>
{

}
