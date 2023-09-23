package io.git.nanovc;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

/**
 * Tests for hashes.
 */
public class HashTests extends NanoVersionControlTestsBase
{

    @Test
    public void testHashingNull()
    {
        assertStringToBlobHash(null, "e69de29bb2d1d6434b8b29ae775ad8c2e48c5391");
    }

    @Test
    public void testHashingEmpty()
    {
        assertStringToBlobHash("", "e69de29bb2d1d6434b8b29ae775ad8c2e48c5391");
    }

    @Test
    public void testHashingA()
    {
        assertStringToBlobHash("A", "8c7e5a667f1b771847fe88c01c3de34413a1b220");
        assertStringToBlobHash("AA", "6c376d94dd106ae6c1ce32cf212e470489acd310");
        assertStringToBlobHash("AAA", "43d88b658623a3b06c40d318392d6c67f1e0b2f9");
        assertStringToBlobHash("a", "2e65efe2a145dda7ee51d1741299f848e5bf752e");
        assertStringToBlobHash("aa", "7ec9a4b774e2472d8e38bc18a3aa1912bacf483e");
        assertStringToBlobHash("aaa", "7c4a013e52c76442ab80ee5572399a30373600a2");
    }

    @Test
    public void test_Hashing_Performance()
    {
        long startNanos, endNanos, deltaNanos;

        // Create the repo engine that will be reused:
        RepoEngine repoEngine = new RepoEngine();

        // Define the number of hashes that we want:
        final int COUNT = 100_000;

        // Define the length of the content:
        final int CONTENT_LENGTH = 1_200;

        // Capture the starting time:
        startNanos = System.nanoTime();

        // Create a random generator for the content:
        Random random = new Random(1234);

        // Allocate a list for the inputs:
        ArrayList<byte[]> inputs = new ArrayList<>(COUNT);

        // Generate the input:
        // NOTE: We do this so that we only profile the hash performance, not the time to create the content:
        for (int i = 0; i < COUNT; i++)
        {
            // Get the random content:
            byte[] bytes = new byte[CONTENT_LENGTH];

            // Fill the bytes with random content:
            random.nextBytes(bytes);

            // Save the input:
            inputs.add(bytes);
        }

        // Capture the ending time:
        endNanos = System.nanoTime();

        // Get the duration:
        deltaNanos = endNanos - startNanos;

        System.out.printf("Content Creation Time: %1$,d ms => %2$,d Inputs/s%n", deltaNanos / 1_000_000, (long)COUNT * 1_000_000_000L / deltaNanos);


        // Create the output:
        ArrayList<Hash> hashes = new ArrayList<>(COUNT);

        // Capture the starting time:
        startNanos = System.nanoTime();

        // Go through each input and create the hash:
        for (int i = 0; i < inputs.size(); i++)
        {
            // Get the input:
            byte[] input = inputs.get(i);

            // Hash the input:
            Hash hash = repoEngine.hash_object(ObjectType.BLOB, input);

            // Save the hash:
            hashes.add(hash);
        }

        // Capture the ending time:
        endNanos = System.nanoTime();

        // Get the duration:
        deltaNanos = endNanos - startNanos;

        System.out.printf("Hashing Time: %1$,d ms => %2$,d Hashes/s%n", deltaNanos / 1_000_000, (long)COUNT * 1_000_000_000L / deltaNanos);

        // Make sure that the hashes are the expected length:
        hashes.forEach(hash -> Assert.assertEquals(40, hash.value.length()));
    }

    /**
     * Makes sure that the given string content hashes to the blob hash given by the expected value.
     * @param content The content to hash. We convert the string to UTF-8 bytes.
     * @param expectedHash The expected 40 digit hash.
     */
    public static void assertStringToBlobHash(String content, String expectedHash)
    {
        // Create an engine which is what contains the logic for hashing:
        RepoEngine repoEngine = new RepoEngine();

        // Get the bytes for the string:
        byte[] bytes = content == null ? null : content.getBytes(StandardCharsets.UTF_8);

        // Get the has from the engine:
        Hash hash = repoEngine.hash_object(ObjectType.BLOB, bytes);

        // Make sure we got a hash:
        Assert.assertNotNull(hash);

        // Make sure the hash is as expected:
        Assert.assertEquals(expectedHash, hash.value);
    }

}
