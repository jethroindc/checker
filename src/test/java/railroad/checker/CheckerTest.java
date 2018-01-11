package railroad.checker;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CheckerTest {

    private static final String TEST_FILENAME = "C:\\Users\\jalexander\\IdeaProjects\\checker\\src\\main\\resources\\test.txt";

    @BeforeClass
    public static void init() {
        BasicConfigurator.configure();
    }

    @Test
    public void findMisspelledWords() {
        try {
            List<String> misspelled = Checker.findMisspelledWords(TEST_FILENAME);
            assertNotNull( misspelled );
            assertTrue( !misspelled.isEmpty() );
            assertTrue( misspelled.contains( "cherker" ));
        } catch (FileNotFoundException e) {
            fail( e.getLocalizedMessage() );
        }
    }

    @Test
    public void possibleCorrections() {
        try {
            List<String> misspelled = Checker.findMisspelledWords(TEST_FILENAME);
            Map<String,Set<String>> corrections = Checker.possibleCorrections( misspelled );
            assertNotNull( corrections );
            assertTrue( !corrections.isEmpty() );
            assertTrue( corrections.containsKey( "cherker" ));
        } catch (FileNotFoundException e) {
            fail( e.getLocalizedMessage() );
        }
    }

    @Test
    public void retrieveWordsToCheck() {
        try {
            List<String> wordsToCheck = Checker.retrieveWordsToCheck(TEST_FILENAME);
            assertNotNull( wordsToCheck );
            assertTrue( !wordsToCheck.isEmpty() );
            assertTrue( wordsToCheck.contains( "cherker" ));
        } catch (FileNotFoundException e) {
            fail( e.getLocalizedMessage() );
        }
    }

    @Test
    public void readDictionary() {
        try {
            Set<String> dictionary = Checker.readDictionary();
            assertNotNull( dictionary );
            assertTrue( !dictionary.isEmpty() );
            assertTrue( dictionary.contains( "buys" ));
            assertTrue( dictionary.contains( "buzz" ));
            assertTrue( dictionary.contains( "cafe" ));
            assertTrue( dictionary.contains( "imam" ));
            assertEquals( 118619, dictionary.size() );
            assertTrue( dictionary.contains( "pseudohypoparathyroidism" ));
        } catch (FileNotFoundException e) {
            fail( e.getLocalizedMessage() );
        }
    }
}