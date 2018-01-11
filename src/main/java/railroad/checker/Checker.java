package railroad.checker;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Checker {

    private static final Logger logger = LoggerFactory.getLogger( Checker.class );

    public static final String DICTIONARY = "C:\\Users\\jalexander\\IdeaProjects\\checker\\src\\main\\resources\\US.dic";

    private static final int LEVENSHTEIN_THRESHOLD = 1;

    public static List<String> findMisspelledWords(String file) throws FileNotFoundException {
        Set<String> dictionary = readDictionary();
        List<String> misspelledWords = new LinkedList<>();
        List<String> wordsToCheck = retrieveWordsToCheck( file );
        for (String word : wordsToCheck) {
            final String lower = word.toLowerCase();
            if ( !dictionary.contains( lower )) {
                misspelledWords.add( lower );
            }
        }
        return misspelledWords;
    }

    protected static List<String> retrieveWordsToCheck( String filename ) throws FileNotFoundException {
        File f = new File( filename );
        if ( !f.exists() ) {
            logger.error( "unable to find file: {}", filename );
            throw new FileNotFoundException( filename );
        }

        List<String> wordsToCheck = new LinkedList<>();
        final Scanner s = new Scanner(f);
        while(s.hasNextLine()) {
            final String line = s.nextLine();
            String[] pieces = line.split( " +");
            if ( pieces.length > 0 ) {
                for (String piece : pieces) {
                    wordsToCheck.add( cleanUp( piece ));
                }
            }
            wordsToCheck.add( line );
        }
        return wordsToCheck;
    }

    protected static Set<String> readDictionary() throws FileNotFoundException {
        File f = new File( DICTIONARY );
        if ( !f.exists() ) {
            logger.error( "unable to find dictionary file: {}", DICTIONARY );
            throw new FileNotFoundException( DICTIONARY );
        }

        Set<String> goodWords = new HashSet<String>();
        try {
            List<String> trimmedLines = Files.readLines(f, Charsets.UTF_8,
            new LineProcessor<List<String>>() {
                List<String> result = Lists.newArrayList();

                public boolean processLine(String line) {
                    result.add(line.trim());
                    return true;
                }

                public List<String> getResult() {return result;}
            });
            goodWords.addAll( trimmedLines );
        } catch (IOException e) {
            logger.error( "error reading file", e );
        }
        return goodWords;
    }

    protected static Map<String,Set<String>> possibleCorrections( List<String> problems ) {
        Map<String,Set<String>> corrections = new HashMap<>();
        try {
            Set<String> dictionary = readDictionary();
            Set<String> possibilities = Sets.newHashSet();
            LevenshteinDistance distance = new LevenshteinDistance();
            for (String problem : problems) {
                for (String word : dictionary) {
                    if ( distance.apply( word, problem ) <= LEVENSHTEIN_THRESHOLD ) {
                        possibilities.add( word );
                    }
                }
                if ( !possibilities.isEmpty() ) {
                    corrections.put( problem, possibilities );
                }
            }
        } catch (FileNotFoundException e) {
            logger.error( "unable to load dictionary" , e );
        }
        return corrections;
    }

    protected static String cleanUp( String cleanMe ) {
        return cleanMe.replaceAll( "[\\.\\?\\!]", "" );
    }
}
