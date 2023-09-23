package io.git.nanovc;

/**
 * Helper class for hexadecimal conversions.
 */
public class Hex
{

    /**
     * The characters for hexadecimal representation.
     */
    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Gets a hexadecimal representation of the given bytes.
     * https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     * https://stackoverflow.com/a/9855338/231860
     * @param bytes The bytes to convert.
     * @return A hexadecimal representation of the given bytes.
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
