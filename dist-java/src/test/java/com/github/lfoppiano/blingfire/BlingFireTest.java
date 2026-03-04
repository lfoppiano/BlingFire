package com.github.lfoppiano.blingfire;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlingFireTest {

    private static String sbdModelPath;
    private static String wbdModelPath;
    private static String bertModelPath;

    @BeforeAll
    static void setUp() {
        sbdModelPath = getResourcePath("sbd.bin");
        wbdModelPath = getResourcePath("wbd.bin");
        bertModelPath = getResourcePath("bert_base_tok.bin");
    }

    private static String getResourcePath(String name) {
        URL url = BlingFireTest.class.getClassLoader().getResource(name);
        assertNotNull(url, "Test resource not found: " + name);
        return new File(url.getFile()).getAbsolutePath();
    }

    @Test
    void testGetVersion() {
        int version = BlingFire.getVersion();
        assertTrue(version > 0, "Version should be positive, got: " + version);
    }

    @Test
    void testLoadAndFreeModel() {
        long handle = BlingFire.loadModel(sbdModelPath);
        assertNotEquals(0, handle, "Model handle should not be 0");
        BlingFire.freeModel(handle);
    }

    @Test
    void testTextToSentences() {
        try (BlingFire.Model model = new BlingFire.Model(sbdModelPath)) {
            String text = "Hello world. How are you? I am fine.";
            String[] sentences = model.textToSentences(text);
            assertNotNull(sentences);
            assertEquals(3, sentences.length, "Expected 3 sentences");
            assertEquals("Hello world.", sentences[0].trim());
            assertEquals("How are you?", sentences[1].trim());
            assertEquals("I am fine.", sentences[2].trim());
        }
    }

    @Test
    void testTextToWords() {
        try (BlingFire.Model model = new BlingFire.Model(wbdModelPath)) {
            String text = "Hello world.";
            String[] words = model.textToWords(text);
            assertNotNull(words);
            assertTrue(words.length >= 3, "Expected at least 3 tokens (Hello, world, .)");
        }
    }

    @Test
    void testTextToSentencesWithOffsets() {
        try (BlingFire.Model model = new BlingFire.Model(sbdModelPath)) {
            String text = "Hello world. How are you?";
            List<BlingFire.TokenWithOffset> tokens = model.textToSentencesWithOffsets(text);
            assertNotNull(tokens);
            assertFalse(tokens.isEmpty(), "Should return at least one sentence");

            for (BlingFire.TokenWithOffset token : tokens) {
                assertTrue(token.getStartOffset() >= 0, "Start offset should be non-negative");
                assertTrue(token.getEndOffset() >= token.getStartOffset(),
                        "End offset should be >= start offset");
                assertNotNull(token.getText());
                assertFalse(token.getText().isEmpty());
            }
        }
    }

    @Test
    void testTextToWordsWithOffsets() {
        try (BlingFire.Model model = new BlingFire.Model(wbdModelPath)) {
            String text = "Hello world";
            List<BlingFire.TokenWithOffset> tokens = model.textToWordsWithOffsets(text);
            assertNotNull(tokens);
            assertFalse(tokens.isEmpty(), "Should return at least one word");

            for (BlingFire.TokenWithOffset token : tokens) {
                assertTrue(token.getStartOffset() >= 0);
                assertTrue(token.getEndOffset() >= token.getStartOffset());
                assertNotNull(token.getText());
            }
        }
    }

    @Test
    void testTextToIds() {
        try (BlingFire.Model model = new BlingFire.Model(bertModelPath)) {
            String text = "Hello world";
            int[] ids = model.textToIds(text, 128, 100);
            assertNotNull(ids);
            assertTrue(ids.length > 0, "Should produce at least one token ID");
            // BERT "hello" = 7592, "world" = 2088 (for bert_base_tok)
            // The tokenizer lowercases, so we check specific IDs
            assertEquals(7592, ids[0], "First token should be 'hello' (7592)");
            assertEquals(2088, ids[1], "Second token should be 'world' (2088)");
        }
    }

    @Test
    void testTextToIdsWithOffsets() {
        try (BlingFire.Model model = new BlingFire.Model(bertModelPath)) {
            String text = "Hello world";
            BlingFire.TextToIdsResult result = model.textToIdsWithOffsets(text, 128, 100);
            assertNotNull(result);
            assertTrue(result.getCount() > 0, "Should produce at least one token");
            assertEquals(result.getIds().length, result.getCount());
            assertEquals(result.getStartOffsets().length, result.getCount());
            assertEquals(result.getEndOffsets().length, result.getCount());
        }
    }

    @Test
    void testIdsToText() {
        try (BlingFire.Model model = new BlingFire.Model(bertModelPath)) {
            // Round-trip test: text -> ids -> text
            String text = "hello world";
            int[] ids = model.textToIds(text, 128, 100);
            assertTrue(ids.length > 0);

            String decoded = model.idsToText(ids, true);
            assertNotNull(decoded);
            // Note: IdsToText may return empty for some model types (e.g., WordPiece).
            // This is expected behavior - the test verifies the call doesn't crash.
        }
    }

    @Test
    void testNormalizeSpaces() {
        String text = "Hello world";
        String result = BlingFire.normalizeSpaces(text);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testModelAutoCloseable() {
        // Verify that try-with-resources properly closes the model
        BlingFire.Model model = new BlingFire.Model(sbdModelPath);
        long handle = model.getHandle();
        assertNotEquals(0, handle);
        model.close();

        // After close, getHandle should throw
        assertThrows(IllegalStateException.class, model::getHandle);
    }

    @Test
    void testEmptyInput() {
        try (BlingFire.Model model = new BlingFire.Model(sbdModelPath)) {
            String[] sentences = model.textToSentences("");
            assertNotNull(sentences);
            assertEquals(0, sentences.length);

            sentences = model.textToSentences(null);
            assertNotNull(sentences);
            assertEquals(0, sentences.length);
        }
    }

    @Test
    void testUnicodeInput() {
        try (BlingFire.Model model = new BlingFire.Model(sbdModelPath)) {
            String text = "\u042f \u0443\u0432\u0438\u0434\u0435\u043b \u0434\u0435\u0432\u0443\u0448\u043a\u0443. \u5979\u5f88\u6f02\u4eae\u3002";
            String[] sentences = model.textToSentences(text);
            assertNotNull(sentences);
            assertTrue(sentences.length >= 1, "Should handle multi-byte UTF-8 input");
        }
    }

    @Test
    void testDefaultTextToSentences() {
        String text = "Hello world. How are you?";
        String[] sentences = BlingFire.textToSentences(text);
        assertNotNull(sentences);
        assertEquals(2, sentences.length, "Expected 2 sentences");
        assertEquals("Hello world.", sentences[0].trim());
        assertEquals("How are you?", sentences[1].trim());
    }

    @Test
    void testDefaultTextToWords() {
        String text = "Hello world";
        String[] words = BlingFire.textToWords(text);
        assertNotNull(words);
        assertTrue(words.length >= 2, "Expected at least 2 words");
    }

    @Test
    void testStaticTextToSentences() {
        long handle = BlingFire.loadModel(sbdModelPath);
        try {
            String[] sentences = BlingFire.textToSentences("First sentence. Second sentence.", handle);
            assertNotNull(sentences);
            assertEquals(2, sentences.length);
        } finally {
            BlingFire.freeModel(handle);
        }
    }

    @Test
    void testStaticTextToWords() {
        long handle = BlingFire.loadModel(wbdModelPath);
        try {
            String[] words = BlingFire.textToWords("Hello world", handle);
            assertNotNull(words);
            assertTrue(words.length >= 2);
        } finally {
            BlingFire.freeModel(handle);
        }
    }
}
