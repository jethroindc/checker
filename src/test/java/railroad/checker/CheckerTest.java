package railroad.checker;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CheckerTest {

    private static final String TEST_FILENAME = "./test.txt";
    private static final String DICTIONARY = "./US.dic";
    private static final String MISSPELLED = "cherker";
    private static final String CORRECTED = "checked";

    @BeforeClass
    public static void init() {
        BasicConfigurator.configure();
    }

    @Test
    public void findMisspelledWords() {
        try {
            Set<String> dictionary = getDictionary();
            List<String> words = wordsToCheck();
            List<String> misspelled = Checker.findMisspelledWords(dictionary, words);
            assertNotNull(misspelled);
            assertTrue(!misspelled.isEmpty());
            assertTrue(misspelled.contains(MISSPELLED));
        } catch (IOException | URISyntaxException e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void testPossibleCorrections() {
        try {
            Set<String> dictionary = getDictionary();
            List<String> words = wordsToCheck();
            List<String> misspelled = Checker.findMisspelledWords(dictionary, words);
            Map<String, Set<String>> corrections = Checker.possibleCorrections(dictionary, misspelled, 1);
            assertNotNull(corrections);
            assertTrue(!corrections.isEmpty());
            assertTrue(corrections.containsKey(MISSPELLED));
            assertTrue(corrections.get( MISSPELLED ).contains(CORRECTED));
        } catch (IOException | URISyntaxException e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void testRetrieveWordsToCheck() {
        try {
            List<String> wordsToCheck = wordsToCheck();
            assertNotNull(wordsToCheck);
            assertTrue(!wordsToCheck.isEmpty());
            assertTrue(wordsToCheck.contains(MISSPELLED));
        } catch (IOException | URISyntaxException e) {
            fail(e.getLocalizedMessage());
        }
    }

    private File getResourceFile(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }

    private Set<String> getDictionary() throws IOException, URISyntaxException {
        return Checker.readDictionary(getResourceFile(DICTIONARY));
    }

    private List<String> wordsToCheck() throws IOException, URISyntaxException {
        return Checker.retrieveWordsToCheck( getResourceFile( TEST_FILENAME ));
    }

    @Test
    public void testCommandLine() {
        String[] args = new String[] { "-dict", getResourceFile( DICTIONARY ).toString(), getResourceFile( TEST_FILENAME ).toString() };
        Checker.main( args );
    }

    @Test
    public void testReadDictionary() {
        try {
            Set<String> dictionary = getDictionary();
            assertNotNull(dictionary);
            assertTrue(!dictionary.isEmpty());
            assertTrue(dictionary.contains("pseudohypoparathyroidism"));
            assertEquals(118619, dictionary.size());
        } catch (IOException | URISyntaxException e) {
            fail(e.getLocalizedMessage());
        }
    }
}