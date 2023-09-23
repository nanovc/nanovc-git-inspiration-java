package io.git.nanovc;

import java.nio.charset.StandardCharsets;

/**
 * A test base class for the Nano Version Control framework.
 */
public class NanoVersionControlTestsBase
{
    /**
     * A helper method to convert a string to bytes.
     * This keeps tests concise.
     * It abstracts the encoding away.
     * @param string The string to convert to bytes.
     * @return The bytes for the string.
     */
    public static byte[] bytes(String string)
    {
        return string.getBytes(StandardCharsets.UTF_8);
    }
}
