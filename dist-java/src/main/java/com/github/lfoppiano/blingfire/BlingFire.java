package com.github.lfoppiano.blingfire;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Java bindings for BlingFire, a high-performance finite state machine based
 * NLP tokenization library.
 *
 * <p>Provides sentence breaking, word tokenization, WordPiece/SentencePiece
 * token ID conversion, text normalization, and hyphenation.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * try (BlingFire.Model model = new BlingFire.Model("sbd.bin")) {
 *     String[] sentences = model.textToSentences(text);
 * }
 * }</pre>
 */
public class BlingFire {

    /** Unicode code point for SentencePiece delimiter (U+2581). */
    public static final int SP_DELIMITER = 0x2581;

    /** Unicode code point for default hyphen character (U+2012). */
    public static final int DEFAULT_HYPHEN = 0x2012;

    static {
        loadNativeLibrary();
    }

    private static void loadNativeLibrary() {
        try {
            System.loadLibrary("blingfirejni");
            return;
        } catch (UnsatisfiedLinkError e) {
            // Fall through to JAR extraction
        }

        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        String os;
        if (osName.contains("linux")) {
            os = "linux";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            os = "darwin";
        } else if (osName.contains("win")) {
            os = "win";
        } else {
            throw new UnsatisfiedLinkError("Unsupported OS: " + osName);
        }

        String arch;
        if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            arch = "x86_64";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            arch = "aarch64";
        } else {
            throw new UnsatisfiedLinkError("Unsupported architecture: " + osArch);
        }

        String libName;
        String libExtension;
        switch (os) {
            case "linux":
                libName = "libblingfirejni.so";
                libExtension = ".so";
                break;
            case "darwin":
                libName = "libblingfirejni.dylib";
                libExtension = ".dylib";
                break;
            case "win":
                libName = "blingfirejni.dll";
                libExtension = ".dll";
                break;
            default:
                throw new UnsatisfiedLinkError("Unsupported OS: " + os);
        }

        String resourcePath = "/native/" + os + "/" + arch + "/" + libName;
        try (InputStream is = BlingFire.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new UnsatisfiedLinkError(
                        "Native library not found in JAR: " + resourcePath
                        + ". Ensure the library is on java.library.path or bundled in the JAR.");
            }
            File tempFile = File.createTempFile("blingfirejni", libExtension);
            tempFile.deleteOnExit();
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Failed to extract native library: " + e.getMessage());
        }
    }

    // ========================================================================
    // Native method declarations
    // ========================================================================

    /** Returns the BlingFire library version as an integer. */
    public static native int GetBlingFireTokVersion();

    /**
     * Splits text into sentences using the built-in default model.
     *
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param outUtf8Str output buffer for newline-delimited sentences
     * @param maxOutUtf8StrByteCount output buffer size
     * @return number of bytes written, or -1 on error
     */
    public static native int TextToSentences(byte[] inUtf8Str, int inUtf8StrByteCount,
            byte[] outUtf8Str, int maxOutUtf8StrByteCount);

    /**
     * Tokenizes text into words using the built-in default model.
     *
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param outUtf8Str output buffer for space-delimited words
     * @param maxOutUtf8StrByteCount output buffer size
     * @return number of bytes written, or -1 on error
     */
    public static native int TextToWords(byte[] inUtf8Str, int inUtf8StrByteCount,
            byte[] outUtf8Str, int maxOutUtf8StrByteCount);

    /**
     * Splits text into sentences using the specified model.
     *
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param outUtf8Str output buffer for newline-delimited sentences
     * @param maxOutUtf8StrByteCount output buffer size
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @return number of bytes written, or -1 on error
     */
    public static native int TextToSentencesWithModel(byte[] inUtf8Str, int inUtf8StrByteCount,
            byte[] outUtf8Str, int maxOutUtf8StrByteCount, long modelHandle);

    /**
     * Splits text into sentences with byte offsets using the specified model.
     *
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param outUtf8Str output buffer for newline-delimited sentences
     * @param startOffsets array to receive start byte offsets
     * @param endOffsets array to receive end byte offsets
     * @param maxOutUtf8StrByteCount output buffer size (also max number of offsets)
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @return number of sentences, or -1 on error
     */
    public static native int TextToSentencesWithOffsetsWithModel(byte[] inUtf8Str, int inUtf8StrByteCount,
            byte[] outUtf8Str, int[] startOffsets, int[] endOffsets,
            int maxOutUtf8StrByteCount, long modelHandle);

    /**
     * Tokenizes text into words using the specified model.
     *
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param outUtf8Str output buffer for space-delimited words
     * @param maxOutUtf8StrByteCount output buffer size
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @return number of bytes written, or -1 on error
     */
    public static native int TextToWordsWithModel(byte[] inUtf8Str, int inUtf8StrByteCount,
            byte[] outUtf8Str, int maxOutUtf8StrByteCount, long modelHandle);

    /**
     * Tokenizes text into words with byte offsets using the specified model.
     *
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param outUtf8Str output buffer for space-delimited words
     * @param startOffsets array to receive start byte offsets
     * @param endOffsets array to receive end byte offsets
     * @param maxOutUtf8StrByteCount output buffer size (also max number of offsets)
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @return number of words, or -1 on error
     */
    public static native int TextToWordsWithOffsetsWithModel(byte[] inUtf8Str, int inUtf8StrByteCount,
            byte[] outUtf8Str, int[] startOffsets, int[] endOffsets,
            int maxOutUtf8StrByteCount, long modelHandle);

    /**
     * Converts text to token IDs using the specified model.
     *
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param idsArr output array for token IDs
     * @param maxIdsArrLength maximum number of IDs
     * @param unkId ID to use for unknown tokens
     * @return number of IDs produced, or -1 on error
     */
    public static native int TextToIds(long modelHandle, byte[] inUtf8Str, int inUtf8StrByteCount,
            int[] idsArr, int maxIdsArrLength, int unkId);

    /**
     * Converts text to token IDs with byte offsets using the specified model.
     *
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param idsArr output array for token IDs
     * @param startOffsets array to receive start byte offsets
     * @param endOffsets array to receive end byte offsets
     * @param maxIdsArrLength maximum number of IDs/offsets
     * @param unkId ID to use for unknown tokens
     * @return number of IDs produced, or -1 on error
     */
    public static native int TextToIdsWithOffsets(long modelHandle, byte[] inUtf8Str, int inUtf8StrByteCount,
            int[] idsArr, int[] startOffsets, int[] endOffsets, int maxIdsArrLength, int unkId);

    /**
     * Converts token IDs back to text using the specified model.
     *
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @param idsArr array of token IDs
     * @param idsCount number of IDs
     * @param outUtf8Str output buffer for UTF-8 text
     * @param maxOutUtf8StrByteCount output buffer size
     * @param skipSpecialTokens whether to skip special tokens in output
     * @return number of bytes written, or -1 on error
     */
    public static native int IdsToText(long modelHandle, int[] idsArr, int idsCount,
            byte[] outUtf8Str, int maxOutUtf8StrByteCount, boolean skipSpecialTokens);

    /**
     * Normalizes whitespace in text, replacing spaces with the given character.
     *
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param outUtf8Str output buffer
     * @param maxOutUtf8StrByteCount output buffer size
     * @param spaceChar Unicode code point to use as space replacement
     * @return number of bytes written, or -1 on error
     */
    public static native int NormalizeSpaces(byte[] inUtf8Str, int inUtf8StrByteCount,
            byte[] outUtf8Str, int maxOutUtf8StrByteCount, int spaceChar);

    /**
     * Computes word n-gram hashes for text.
     *
     * @param inUtf8Str input UTF-8 bytes
     * @param inUtf8StrByteCount input byte count
     * @param hashArr output array for hash values
     * @param maxHashArrLength maximum number of hashes
     * @param wordNgrams word n-gram size
     * @param bucketSize hash bucket size
     * @return number of hashes produced, or -1 on error
     */
    public static native int TextToHashes(byte[] inUtf8Str, int inUtf8StrByteCount,
            int[] hashArr, int maxHashArrLength, int wordNgrams, int bucketSize);

    /**
     * Hyphenates a word using the specified model.
     *
     * @param inUtf8Str input UTF-8 bytes (single word)
     * @param inUtf8StrByteCount input byte count
     * @param outUtf8Str output buffer for hyphenated word
     * @param maxOutUtf8StrByteCount output buffer size
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @param hyphenChar Unicode code point for the hyphen character
     * @return number of bytes written, or -1 on error
     */
    public static native int WordHyphenationWithModel(byte[] inUtf8Str, int inUtf8StrByteCount,
            byte[] outUtf8Str, int maxOutUtf8StrByteCount, long modelHandle, int hyphenChar);

    /**
     * Disables or enables the dummy prefix for SentencePiece models.
     *
     * @param modelHandle handle from {@link #LoadModel(String)}
     * @param noDummyPrefix true to disable dummy prefix
     * @return 0 on success, non-zero on error
     */
    public static native int SetNoDummyPrefix(long modelHandle, boolean noDummyPrefix);

    /**
     * Loads a model from a file path.
     *
     * @param modelPath path to the model file
     * @return model handle (0 on failure)
     */
    public static native long LoadModel(String modelPath);

    /**
     * Frees a previously loaded model.
     *
     * @param modelHandle handle from {@link #LoadModel(String)}
     */
    public static native void FreeModel(long modelHandle);

    // ========================================================================
    // Helper classes
    // ========================================================================

    /**
     * Represents a token with its byte offsets in the original input.
     */
    public static class TokenWithOffset {
        private final String text;
        private final int startOffset;
        private final int endOffset;

        public TokenWithOffset(String text, int startOffset, int endOffset) {
            this.text = text;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        /** Returns the token text. */
        public String getText() { return text; }

        /** Returns the start byte offset in the original UTF-8 input. */
        public int getStartOffset() { return startOffset; }

        /** Returns the end byte offset in the original UTF-8 input. */
        public int getEndOffset() { return endOffset; }

        @Override
        public String toString() {
            return "TokenWithOffset{text='" + text + "', start=" + startOffset + ", end=" + endOffset + "}";
        }
    }

    /**
     * Result of {@link #textToIdsWithOffsets}, containing token IDs with their byte offsets.
     */
    public static class TextToIdsResult {
        private final int[] ids;
        private final int[] startOffsets;
        private final int[] endOffsets;
        private final int count;

        public TextToIdsResult(int[] ids, int[] startOffsets, int[] endOffsets, int count) {
            this.ids = Arrays.copyOf(ids, count);
            this.startOffsets = Arrays.copyOf(startOffsets, count);
            this.endOffsets = Arrays.copyOf(endOffsets, count);
            this.count = count;
        }

        /** Returns the token IDs. */
        public int[] getIds() { return ids; }

        /** Returns the start byte offsets. */
        public int[] getStartOffsets() { return startOffsets; }

        /** Returns the end byte offsets. */
        public int[] getEndOffsets() { return endOffsets; }

        /** Returns the number of tokens. */
        public int getCount() { return count; }
    }

    /**
     * An {@link AutoCloseable} wrapper around a native BlingFire model handle.
     *
     * <p>Usage:</p>
     * <pre>{@code
     * try (BlingFire.Model model = new BlingFire.Model("sbd.bin")) {
     *     String[] sentences = model.textToSentences(text);
     * }
     * }</pre>
     */
    public static class Model implements AutoCloseable {
        private long handle;

        /**
         * Loads a model from the given file path.
         *
         * @param modelPath path to the model file
         * @throws IllegalStateException if the model cannot be loaded
         */
        public Model(String modelPath) {
            this.handle = LoadModel(modelPath);
            if (this.handle == 0) {
                throw new IllegalStateException("Failed to load model: " + modelPath);
            }
        }

        /**
         * Returns the native model handle.
         *
         * @return model handle
         * @throws IllegalStateException if the model has been closed
         */
        public long getHandle() {
            if (handle == 0) {
                throw new IllegalStateException("Model has been closed");
            }
            return handle;
        }

        /**
         * Splits text into sentences.
         *
         * @param text input text
         * @return array of sentences
         */
        public String[] textToSentences(String text) {
            return BlingFire.textToSentences(text, getHandle());
        }

        /**
         * Splits text into sentences with byte offsets.
         *
         * @param text input text
         * @return list of tokens with offsets
         */
        public List<TokenWithOffset> textToSentencesWithOffsets(String text) {
            return BlingFire.textToSentencesWithOffsets(text, getHandle());
        }

        /**
         * Tokenizes text into words.
         *
         * @param text input text
         * @return array of words
         */
        public String[] textToWords(String text) {
            return BlingFire.textToWords(text, getHandle());
        }

        /**
         * Tokenizes text into words with byte offsets.
         *
         * @param text input text
         * @return list of tokens with offsets
         */
        public List<TokenWithOffset> textToWordsWithOffsets(String text) {
            return BlingFire.textToWordsWithOffsets(text, getHandle());
        }

        /**
         * Converts text to token IDs.
         *
         * @param text input text
         * @param maxLen maximum number of tokens
         * @param unkId ID for unknown tokens
         * @return array of token IDs (trimmed to actual count)
         */
        public int[] textToIds(String text, int maxLen, int unkId) {
            return BlingFire.textToIds(getHandle(), text, maxLen, unkId);
        }

        /**
         * Converts text to token IDs with byte offsets.
         *
         * @param text input text
         * @param maxLen maximum number of tokens
         * @param unkId ID for unknown tokens
         * @return result containing IDs and offsets
         */
        public TextToIdsResult textToIdsWithOffsets(String text, int maxLen, int unkId) {
            return BlingFire.textToIdsWithOffsets(getHandle(), text, maxLen, unkId);
        }

        /**
         * Converts token IDs back to text.
         *
         * @param ids array of token IDs
         * @param skipSpecialTokens whether to skip special tokens
         * @return decoded text
         */
        public String idsToText(int[] ids, boolean skipSpecialTokens) {
            return BlingFire.idsToText(getHandle(), ids, skipSpecialTokens);
        }

        /**
         * Hyphenates a word.
         *
         * @param word input word
         * @return hyphenated word
         */
        public String wordHyphenation(String word) {
            return BlingFire.wordHyphenation(word, getHandle());
        }

        /**
         * Hyphenates a word with a custom hyphen character.
         *
         * @param word input word
         * @param hyphenChar Unicode code point for the hyphen
         * @return hyphenated word
         */
        public String wordHyphenation(String word, int hyphenChar) {
            return BlingFire.wordHyphenation(word, getHandle(), hyphenChar);
        }

        /**
         * Disables or enables the dummy prefix for SentencePiece models.
         *
         * @param noDummyPrefix true to disable dummy prefix
         */
        public void setNoDummyPrefix(boolean noDummyPrefix) {
            BlingFire.setNoDummyPrefix(getHandle(), noDummyPrefix);
        }

        @Override
        public void close() {
            if (handle != 0) {
                FreeModel(handle);
                handle = 0;
            }
        }
    }

    // ========================================================================
    // High-level Java API (static methods)
    // ========================================================================

    /**
     * Returns the BlingFire library version.
     *
     * @return version number
     */
    public static int getVersion() {
        return GetBlingFireTokVersion();
    }

    /**
     * Loads a model from a file path.
     *
     * @param modelPath path to the model file
     * @return model handle (0 on failure)
     */
    public static long loadModel(String modelPath) {
        return LoadModel(modelPath);
    }

    /**
     * Frees a previously loaded model.
     *
     * @param modelHandle handle from {@link #loadModel(String)}
     */
    public static void freeModel(long modelHandle) {
        FreeModel(modelHandle);
    }

    /**
     * Splits text into sentences using the specified model.
     *
     * @param text input text
     * @param modelHandle handle from {@link #loadModel(String)}
     * @return array of sentences
     */
    public static String[] textToSentences(String text, long modelHandle) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int outputBufferSize = inputBytes.length * 2 + 1024;
        byte[] outputBytes = new byte[outputBufferSize];

        int actualSize = TextToSentencesWithModel(inputBytes, inputBytes.length,
                outputBytes, outputBufferSize, modelHandle);

        if (actualSize <= 0) {
            return new String[0];
        }

        String result = new String(outputBytes, 0, actualSize, StandardCharsets.UTF_8);
        return result.split("\n");
    }

    /**
     * Tokenizes text into words using the specified model.
     *
     * @param text input text
     * @param modelHandle handle from {@link #loadModel(String)}
     * @return array of words
     */
    public static String[] textToWords(String text, long modelHandle) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int outputBufferSize = inputBytes.length * 2 + 1024;
        byte[] outputBytes = new byte[outputBufferSize];

        int actualSize = TextToWordsWithModel(inputBytes, inputBytes.length,
                outputBytes, outputBufferSize, modelHandle);

        if (actualSize <= 0) {
            return new String[0];
        }

        String result = new String(outputBytes, 0, actualSize, StandardCharsets.UTF_8);
        return result.split(" ");
    }

    /**
     * Splits text into sentences using the built-in default model (no model file needed).
     *
     * @param text input text
     * @return array of sentences
     */
    public static String[] textToSentences(String text) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int outputBufferSize = inputBytes.length * 2 + 1024;
        byte[] outputBytes = new byte[outputBufferSize];

        int actualSize = TextToSentences(inputBytes, inputBytes.length,
                outputBytes, outputBufferSize);

        if (actualSize <= 0) {
            return new String[0];
        }

        String result = new String(outputBytes, 0, actualSize, StandardCharsets.UTF_8);
        return result.split("\n");
    }

    /**
     * Tokenizes text into words using the built-in default model (no model file needed).
     *
     * @param text input text
     * @return array of words
     */
    public static String[] textToWords(String text) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int outputBufferSize = inputBytes.length * 2 + 1024;
        byte[] outputBytes = new byte[outputBufferSize];

        int actualSize = TextToWords(inputBytes, inputBytes.length,
                outputBytes, outputBufferSize);

        if (actualSize <= 0) {
            return new String[0];
        }

        String result = new String(outputBytes, 0, actualSize, StandardCharsets.UTF_8);
        return result.split(" ");
    }

    /**
     * Splits text into sentences with byte offsets.
     *
     * @param text input text
     * @param modelHandle handle from {@link #loadModel(String)}
     * @return list of tokens with byte offsets
     */
    public static List<TokenWithOffset> textToSentencesWithOffsets(String text, long modelHandle) {
        List<TokenWithOffset> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int maxItems = inputBytes.length + 1024;
        byte[] outputBytes = new byte[maxItems];
        int[] startOffsets = new int[maxItems];
        int[] endOffsets = new int[maxItems];

        int count = TextToSentencesWithOffsetsWithModel(inputBytes, inputBytes.length,
                outputBytes, startOffsets, endOffsets, maxItems, modelHandle);

        if (count <= 0) {
            return result;
        }

        String output = new String(outputBytes, 0,
                findOutputLength(outputBytes, maxItems), StandardCharsets.UTF_8);
        String[] sentences = output.split("\n");

        int sentenceCount = Math.min(count, sentences.length);
        for (int i = 0; i < sentenceCount; i++) {
            result.add(new TokenWithOffset(sentences[i], startOffsets[i], endOffsets[i]));
        }

        return result;
    }

    /**
     * Tokenizes text into words with byte offsets.
     *
     * @param text input text
     * @param modelHandle handle from {@link #loadModel(String)}
     * @return list of tokens with byte offsets
     */
    public static List<TokenWithOffset> textToWordsWithOffsets(String text, long modelHandle) {
        List<TokenWithOffset> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int maxItems = inputBytes.length + 1024;
        byte[] outputBytes = new byte[maxItems];
        int[] startOffsets = new int[maxItems];
        int[] endOffsets = new int[maxItems];

        int count = TextToWordsWithOffsetsWithModel(inputBytes, inputBytes.length,
                outputBytes, startOffsets, endOffsets, maxItems, modelHandle);

        if (count <= 0) {
            return result;
        }

        String output = new String(outputBytes, 0,
                findOutputLength(outputBytes, maxItems), StandardCharsets.UTF_8);
        String[] words = output.split(" ");

        int wordCount = Math.min(count, words.length);
        for (int i = 0; i < wordCount; i++) {
            result.add(new TokenWithOffset(words[i], startOffsets[i], endOffsets[i]));
        }

        return result;
    }

    /**
     * Converts text to token IDs using the specified model.
     *
     * @param modelHandle handle from {@link #loadModel(String)}
     * @param text input text
     * @param maxLen maximum number of tokens
     * @param unkId ID for unknown tokens
     * @return array of token IDs (trimmed to actual count)
     */
    public static int[] textToIds(long modelHandle, String text, int maxLen, int unkId) {
        if (text == null || text.isEmpty()) {
            return new int[0];
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int[] ids = new int[maxLen];

        int count = TextToIds(modelHandle, inputBytes, inputBytes.length, ids, maxLen, unkId);

        if (count <= 0) {
            return new int[0];
        }

        return Arrays.copyOf(ids, count);
    }

    /**
     * Converts text to token IDs with byte offsets.
     *
     * @param modelHandle handle from {@link #loadModel(String)}
     * @param text input text
     * @param maxLen maximum number of tokens
     * @param unkId ID for unknown tokens
     * @return result containing IDs and byte offsets, or null on error
     */
    public static TextToIdsResult textToIdsWithOffsets(long modelHandle, String text, int maxLen, int unkId) {
        if (text == null || text.isEmpty()) {
            return new TextToIdsResult(new int[0], new int[0], new int[0], 0);
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int[] ids = new int[maxLen];
        int[] startOffsets = new int[maxLen];
        int[] endOffsets = new int[maxLen];

        int count = TextToIdsWithOffsets(modelHandle, inputBytes, inputBytes.length,
                ids, startOffsets, endOffsets, maxLen, unkId);

        if (count <= 0) {
            return new TextToIdsResult(new int[0], new int[0], new int[0], 0);
        }

        return new TextToIdsResult(ids, startOffsets, endOffsets, count);
    }

    /**
     * Converts token IDs back to text.
     *
     * @param modelHandle handle from {@link #loadModel(String)}
     * @param ids array of token IDs
     * @param skipSpecialTokens whether to skip special tokens
     * @return decoded text, or empty string on error
     */
    public static String idsToText(long modelHandle, int[] ids, boolean skipSpecialTokens) {
        if (ids == null || ids.length == 0) {
            return "";
        }

        int outputBufferSize = ids.length * 64;
        byte[] outputBytes = new byte[outputBufferSize];

        int actualSize = IdsToText(modelHandle, ids, ids.length,
                outputBytes, outputBufferSize, skipSpecialTokens);

        if (actualSize <= 0) {
            return "";
        }

        return new String(outputBytes, 0, actualSize, StandardCharsets.UTF_8);
    }

    /**
     * Normalizes whitespace in text using the SentencePiece delimiter (U+2581).
     *
     * @param text input text
     * @return text with normalized spaces
     */
    public static String normalizeSpaces(String text) {
        return normalizeSpaces(text, SP_DELIMITER);
    }

    /**
     * Normalizes whitespace in text using the specified space character.
     *
     * @param text input text
     * @param spaceChar Unicode code point to use as space replacement
     * @return text with normalized spaces
     */
    public static String normalizeSpaces(String text, int spaceChar) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int outputBufferSize = inputBytes.length * 4 + 1024;
        byte[] outputBytes = new byte[outputBufferSize];

        int actualSize = NormalizeSpaces(inputBytes, inputBytes.length,
                outputBytes, outputBufferSize, spaceChar);

        if (actualSize <= 0) {
            return "";
        }

        return new String(outputBytes, 0, actualSize, StandardCharsets.UTF_8);
    }

    /**
     * Computes word n-gram hashes for text.
     *
     * @param text input text
     * @param wordNgrams word n-gram size
     * @param bucketSize hash bucket size
     * @return array of hash values (trimmed to actual count)
     */
    public static int[] textToHashes(String text, int wordNgrams, int bucketSize) {
        if (text == null || text.isEmpty()) {
            return new int[0];
        }

        byte[] inputBytes = text.getBytes(StandardCharsets.UTF_8);
        int maxHashes = inputBytes.length + 1024;
        int[] hashes = new int[maxHashes];

        int count = TextToHashes(inputBytes, inputBytes.length, hashes, maxHashes, wordNgrams, bucketSize);

        if (count <= 0) {
            return new int[0];
        }

        return Arrays.copyOf(hashes, count);
    }

    /**
     * Hyphenates a word using the default hyphen character (U+2012).
     *
     * @param word input word
     * @param modelHandle handle from {@link #loadModel(String)}
     * @return hyphenated word
     */
    public static String wordHyphenation(String word, long modelHandle) {
        return wordHyphenation(word, modelHandle, DEFAULT_HYPHEN);
    }

    /**
     * Hyphenates a word using a custom hyphen character.
     *
     * @param word input word
     * @param modelHandle handle from {@link #loadModel(String)}
     * @param hyphenChar Unicode code point for the hyphen
     * @return hyphenated word, or empty string on error
     */
    public static String wordHyphenation(String word, long modelHandle, int hyphenChar) {
        if (word == null || word.isEmpty()) {
            return "";
        }

        byte[] inputBytes = word.getBytes(StandardCharsets.UTF_8);
        int outputBufferSize = inputBytes.length * 4 + 64;
        byte[] outputBytes = new byte[outputBufferSize];

        int actualSize = WordHyphenationWithModel(inputBytes, inputBytes.length,
                outputBytes, outputBufferSize, modelHandle, hyphenChar);

        if (actualSize <= 0) {
            return "";
        }

        return new String(outputBytes, 0, actualSize, StandardCharsets.UTF_8);
    }

    /**
     * Disables or enables the dummy prefix for SentencePiece models.
     *
     * @param modelHandle handle from {@link #loadModel(String)}
     * @param noDummyPrefix true to disable dummy prefix
     */
    public static void setNoDummyPrefix(long modelHandle, boolean noDummyPrefix) {
        SetNoDummyPrefix(modelHandle, noDummyPrefix);
    }

    // ========================================================================
    // Internal helpers
    // ========================================================================

    /**
     * Finds the actual content length in a byte buffer (up to the first zero byte or buffer end).
     */
    private static int findOutputLength(byte[] buffer, int maxLen) {
        for (int i = 0; i < maxLen; i++) {
            if (buffer[i] == 0) {
                return i;
            }
        }
        return maxLen;
    }
}
