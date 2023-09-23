package io.git.nanovc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * A list of Hash References.
 */
public class HashReferenceCollection extends ArrayList<HashReference>
{

    // Default constructor
    public HashReferenceCollection() {
        super();
    }

    /**
     * Allows us to construct from a set
     * @param set The set to construct the list from
     */
    public HashReferenceCollection(Set set) {
        super(set);
    }

    /**
     * Allows us to constuct from an existing HashReference Collection
     * @param c The collection to create from
     */
    public HashReferenceCollection(Collection<? extends HashReference> c) {
        super(c);
    }

    /**
     * Finds the reference with the given name.
     * @param referenceName The name of the reference to find.
     * @return The reference with the given name. Null if there is not reference with the given name.
     */
    public HashReference getReference(String referenceName)
    {
        // Find the reference with the given name:
        Optional<HashReference> match = this.stream().filter(reference -> referenceName.equals(reference.name)).findAny();

        return match.orElse(null);
    }

    /**
     * Checks whether we already have a reference with the given name.
     * @param referenceName The name of the reference to find.
     * @return True if we already have a reference with the given name. False if the reference doesn't exist.
     */
    public boolean hasReference(String referenceName)
    {
        // Find the reference with the given name:
        return this.stream().filter(reference -> referenceName.equals(reference.name)).findAny().isPresent();
    }

    /**
     * Finds and removes the reference with the given name.
     * @param referenceName The name of the reference to remove.
     * @return True if a reference was removed. False if the reference was not found.
     */
    public boolean removeReference(String referenceName)
    {
        return this.removeIf(reference -> referenceName.equals(reference.name));
    }
}
