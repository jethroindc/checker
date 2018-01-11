package railroad.checker;

import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Checker {

    private static final Logger logger = LoggerFactory.getLogger(Checker.class);

    private static final int LEVENSHTEIN_THRESHOLD = 1;

    public static final String THRESHOLD_OPTION = "threshold";
    public static final String DICT_OPTION = "dict";

    public static void main(String[] args) {
        Options options = new Options();
        options.addRequiredOption( "d", DICT_OPTION, true, "filename of the dictionary file" );
        options.addOption( "t", THRESHOLD_OPTION, true, "threshold to use when getting suggestions, default: 1" );
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse( options, args );
            int threshold = 1;
            if ( commandLine.hasOption(THRESHOLD_OPTION)) {
                String value = commandLine.getOptionValue(THRESHOLD_OPTION);
                threshold = Integer.parseInt( value );
            }
            String dictFile = commandLine.getOptionValue( DICT_OPTION );
            if ( ! new File(dictFile).exists()) {
                System.err.println( "dictionary file: " + dictFile + " does not exist.  Please check and try again" );
                System.exit(1 );
            }
            String[] extras = commandLine.getArgs();
            if ( extras.length == 0 ) {
                System.err.println( "Please provide a file for processing" );
                System.exit(1 );
            }
            Set<String> dict = null;
            try {
                dict = readDictionary( new File( dictFile ));
            } catch (IOException | URISyntaxException e) {
                System.err.println( "Error reading dictionary file: " + dictFile );
                System.err.println( e.getLocalizedMessage() );
                System.exit(1 );
            }
            Map<String,Set<String>> fixes = new HashMap<>();
            for (String extra : extras) {
                // make sure the file exists
                File f = new File( extra );
                if ( !f.exists()) {
                    System.err.println( "file provided: " + extra + " does not exist" );
                    System.exit(1 );
                }
                List<String> checkMe = null;
                try {
                    checkMe = retrieveWordsToCheck( f );
                } catch (IOException | URISyntaxException e) {
                    System.err.println( "Error reading file: " + dictFile );
                    System.err.println( e.getLocalizedMessage() );
                    System.exit(1 );
                }
                List<String> errors = findMisspelledWords( dict, checkMe );
                Map<String,Set<String>> myFixes = possibleCorrections( dict, errors );
                fixes.putAll( myFixes );
            }
            Gson gson = new Gson();
            String json = gson.toJson( fixes );
            System.out.println( json );
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "checked", options, true );
            System.err.println( e.getLocalizedMessage() );
            System.exit(1 );
        }
    }

    public static List<String> findMisspelledWords(Set<String> dictionary, List<String> words)  {
        List<String> misspelledWords = new LinkedList<>();
        for (String word : words) {
            final String lower = word.toLowerCase();
            if (!dictionary.contains(lower)) {
                misspelledWords.add(lower);
            }
        }
        return misspelledWords;
    }

    public static List<String> retrieveWordsToCheck(File filename) throws IOException, URISyntaxException {
        List<String> lines = readLines(filename);
        List<String> wordsToCheck = new LinkedList<>();
        for (String line : lines) {
            String[] pieces = line.split(" +");
            if (pieces.length > 0) {
                for (String piece : pieces) {
                    wordsToCheck.add(cleanUp(piece));
                }
            }
        }
        return wordsToCheck;
    }

    protected static List<String> readLines(File file ) throws IOException {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            String[] pieces = content.split( "\\n" );
            return Arrays.stream(pieces).map(String::trim).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error( "error reading file", e );
            throw e;
        }
    }


    protected static Set<String> readDictionary(File dictionary) throws IOException, URISyntaxException {
        return new HashSet<>(readLines( dictionary ));
    }


    protected static Map<String, Set<String>> possibleCorrections(Set<String> dictionary, List<String> problems) {
        Map<String, Set<String>> corrections = new HashMap<>();
        LevenshteinDistance distance = new LevenshteinDistance();
        for (String problem : problems) {
            Set<String> possibilities = new HashSet<>();
            for (String word : dictionary) {
                if (distance.apply(word, problem) <= LEVENSHTEIN_THRESHOLD) {
                    possibilities.add(word);
                }
            }
            if (!possibilities.isEmpty()) {
                corrections.put(problem, possibilities);
            }
        }
        return corrections;
    }

    protected static String cleanUp(String cleanMe) {
        return cleanMe.replaceAll("[\\.\\?\\!,;\\-]", "");
    }
}
