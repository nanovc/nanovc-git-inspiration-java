package io.git.nanovc;

/**
 * The interface for high level commands on a Nano Version Control repository.
 * <p>
 * This API is designed to make using Nano Version Control intuitive and simple.
 * It strays away from the git API in favour of simplicity and achieving the nano version control objectives.
 *
 * The Nano Repo is designed to leverage references in memory for performance reasons.
 * The referential integrity of the objects that are passed around is important.
 * It is very possible for you to modify the state of objects and break the entire repo.
 * HOWEVER, we deliberately allow this so that if you know what you are doing,
 * you are able to manipulate the repo at a very low level yourself.
 * This allows for very powerful and efficient reuse scenarios in higher level API's.
 * This follows the lead from Git where they provide the Porcelain commands
 * but definitely still expose the plumbing commands in cases that you want to extend the system in a higher level scenario.
 *
 * Since we are basing the Nano Version Control idea on a stable and well known model as described by Git,
 * we feel that it is therefore appropriate for us to be exposing the inner workings because once
 * the core idea is implemented we don't expect much change in the core idea.
 * Therefore we deliberately forgo the traditional OO encapsulation rules in favour of the feeling
 * of a lightweight framework.
 *
 * In short: the references matter.
 */
public interface NanoCommands extends PorcelainCommands
{

}
