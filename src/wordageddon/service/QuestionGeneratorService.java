package wordageddon.service;

import wordageddon.model.DocumentTermMatrix;
import wordageddon.model.Question;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating various types of questions about document text analysis.
 * 
 * This class handles the logic for creating diverse question types:
 * - Absolute frequency: how many times a word appears in a document
 * - Relative frequency comparison: comparing frequencies of different words
 * - Document-specific word association: which document contains a specific word
 * - Exclusion questions: which word never appears in a document
 * 
 * The service also tracks previously asked questions to avoid repetition.
 *
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class QuestionGeneratorService {
    
    /** The Document-Term Matrix containing the analyzed document data */
    private final DocumentTermMatrix dtm;
    
    /** List of document names available for the current game session */
    private final List<String> documents;
    
    /** Set of keys representing already used absolute frequency questions */
    private final Set<String> usedFrequencyQuestions = new HashSet<>();
    
    /** Set of keys representing already used document comparison questions */
    private final Set<String> usedComparisonQuestions = new HashSet<>();
    
    /** Set of keys representing already used document-specific questions */
    private final Set<String> usedDocumentSpecificQuestions = new HashSet<>();
    
    /** Set of keys representing already used exclusion questions */
    private final Set<String> usedExclusionQuestions = new HashSet<>();
    
    /** Random number generator for consistent randomization */
    private final Random random = new Random();
    
    /** Maximum attempts to generate a unique question before giving up */
    private static final int MAX_ATTEMPTS = 50;
    
    /**
     * Constructs a new QuestionGeneratorService with the specified document data.
     *
     * @param dtm the Document-Term Matrix containing processed document data
     * @param documents list of document names that should be used in the game
     */
    public QuestionGeneratorService(DocumentTermMatrix dtm, List<String> documents) {
        this.dtm = dtm;
        this.documents = new ArrayList<>(documents);
    }
    
    /**
     * Resets all tracking sets to allow reusing previously used questions.
     * Call this when starting a new game session.
     */
    public void resetTracking() {
        usedFrequencyQuestions.clear();
        usedComparisonQuestions.clear();
        usedDocumentSpecificQuestions.clear();
        usedExclusionQuestions.clear();
    }
    
    /**
     * Generates a question of a random type.
     * Tries all question types in a random order until one succeeds.
     *
     * @param questionNumber The sequence number of the question in the current game
     * @return the generated Question object, or a fallback question if generation fails
     */
    public Question generateRandomQuestion(int questionNumber) {
        // Create a list of question generation methods to try in random order
        List<QuestionGenerator> generators = Arrays.asList(
            () -> generateAbsoluteFrequencyQuestion(questionNumber),
            () -> generateRelativeFrequencyQuestion(questionNumber),
            () -> generateDocumentSpecificQuestion(questionNumber),
            () -> generateExclusionQuestion(questionNumber)
        );
        
        // Try generators in a random order
        Collections.shuffle(generators);
        
        for (QuestionGenerator generator : generators) {
            Question question = generator.generate();
            if (question != null) {
                return question;
            }
        }
        
        // If all generators fail, return a fallback question
        return generateFallbackQuestion(questionNumber);
    }
    
    /**
     * Generates a question about the absolute frequency of a word in a document.
     *
     * @param questionNumber The sequence number of the question in the current game
     * @return the generated Question object, or null if generation fails
     */
    public Question generateAbsoluteFrequencyQuestion(int questionNumber) {
        if (documents.isEmpty()) {
            return null;
        }
        
        int attempts = 0;
        
        while (attempts < MAX_ATTEMPTS) {
            String doc = getRandomDocument();
            Map<String, Integer> termFreq = dtm.getTermsForDocument(doc);
            
            if (termFreq == null || termFreq.isEmpty()) {
                attempts++;
                continue;
            }
            
            List<String> words = new ArrayList<>(termFreq.keySet());
            if (words.isEmpty()) {
                attempts++;
                continue;
            }
            
            String word = words.get(random.nextInt(words.size()));
            String questionKey = doc + "|" + word;
            
            if (!usedFrequencyQuestions.contains(questionKey) || usedFrequencyQuestions.size() >= documents.size() * 5) {
                // Allow reuse if we've used a lot of combinations already
                usedFrequencyQuestions.add(questionKey);
                
                int correctFreq = termFreq.get(word);
                String genericDocName = getGenericDocumentName(doc);
                
                String questionText = String.format("Quante volte compare la parola \"%s\" nel %s?", word, genericDocName);
                
                Set<Integer> options = new HashSet<>();
                options.add(correctFreq);
                
                // Ensure options are distinct and plausible
                int optionRange = Math.max(5, correctFreq / 2);
                while (options.size() < 4) {
                    int variation = random.nextInt(optionRange) + 1;
                    int optionValue = random.nextBoolean() ? 
                        correctFreq + variation : Math.max(0, correctFreq - variation);
                    
                    if (optionValue != correctFreq) { 
                        options.add(optionValue);
                    } else if (options.size() < 2 && correctFreq == 0) { 
                        options.add(optionValue + 1);
                    } else if (options.size() < 2) {
                        options.add(optionValue + (options.size() % 2 == 0 ? 1 : -1));
                    }
                }
                
                List<String> optionStrings = new ArrayList<>();
                List<Integer> optionsList = new ArrayList<>(options);
                Collections.shuffle(optionsList);
                
                int correctIndex = -1;
                for (int i = 0; i < optionsList.size(); i++) {
                    optionStrings.add(String.valueOf(optionsList.get(i)));
                    if (optionsList.get(i) == correctFreq) {
                        correctIndex = i;
                    }
                }
                
                if (correctIndex == -1) {
                    attempts++;
                    usedFrequencyQuestions.remove(questionKey);
                    continue;
                }
                
                return new Question(questionNumber, questionText, optionStrings, correctIndex);
            }
            
            attempts++;
        }
        
        return null;
    }
    
    /**
     * Generates a question comparing the relative frequencies of two words.
     *
     * @param questionNumber The sequence number of the question in the current game
     * @return the generated Question object, or null if generation fails
     */
    public Question generateRelativeFrequencyQuestion(int questionNumber) {
        if (documents.isEmpty()) {
            return null;
        }
        
        int attempts = 0;
        
        while (attempts < MAX_ATTEMPTS) {
            String doc = getRandomDocument();
            Map<String, Integer> termFreq = dtm.getTermsForDocument(doc);
            
            if (termFreq == null || termFreq.size() < 5) {
                attempts++;
                continue;
            }
            
            // Get all words with frequencies and sort by frequency
            List<Map.Entry<String, Integer>> wordsByFreq = new ArrayList<>(termFreq.entrySet());
            Collections.shuffle(wordsByFreq);
            
            // Try to find two distinct words with different frequencies
            if (wordsByFreq.size() < 2) {
                attempts++;
                continue;
            }
            
            String word1 = wordsByFreq.get(0).getKey();
            String word2 = wordsByFreq.get(1).getKey();
            int freq1 = wordsByFreq.get(0).getValue();
            int freq2 = wordsByFreq.get(1).getValue();
            
            // Skip if frequencies are equal - not interesting for comparison
            if (freq1 == freq2) {
                attempts++;
                continue;
            }
            
            String questionKey = doc + "|" + word1 + "|" + word2;
            String reverseKey = doc + "|" + word2 + "|" + word1;
            
            if ((!usedComparisonQuestions.contains(questionKey) && 
                 !usedComparisonQuestions.contains(reverseKey)) || 
                 usedComparisonQuestions.size() >= documents.size() * 5) {
                
                usedComparisonQuestions.add(questionKey);
                
                String genericDocName = getGenericDocumentName(doc);
                String questionText = String.format("Nel %s, quale parola compare più frequentemente?", genericDocName);
                
                List<String> options = Arrays.asList(word1, word2);
                int correctIndex = freq1 > freq2 ? 0 : 1;
                
                // Add two distractors from the same document
                List<String> distractors = wordsByFreq.stream()
                    .filter(e -> !e.getKey().equals(word1) && !e.getKey().equals(word2))
                    .limit(2)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
                
                List<String> allOptions = new ArrayList<>(options);
                allOptions.addAll(distractors);
                Collections.shuffle(allOptions);
                
                correctIndex = allOptions.indexOf(options.get(correctIndex));
                
                return new Question(questionNumber, questionText, allOptions, correctIndex);
            }
            
            attempts++;
        }
        
        return null;
    }
    
    /**
     * Generates a question asking which document contains a specific word.
     *
     * @param questionNumber The sequence number of the question in the current game
     * @return the generated Question object, or null if generation fails
     */
    public Question generateDocumentSpecificQuestion(int questionNumber) {
        if (documents.size() < 2) { // Need at least 2 documents for this type
            return null;
        }
        
        int attempts = 0;
        
        while (attempts < MAX_ATTEMPTS) {
            // Find a word that appears in one document but not all
            String targetDoc = getRandomDocument();
            Map<String, Integer> targetTerms = dtm.getTermsForDocument(targetDoc);
            
            if (targetTerms == null || targetTerms.isEmpty()) {
                attempts++;
                continue;
            }
            
            // Filter for words that appear only in some documents
            List<String> candidates = new ArrayList<>();
            for (String word : targetTerms.keySet()) {
                // Check if this word doesn't appear in at least one other document
                boolean isExclusive = false;
                for (String otherDoc : documents) {
                    if (!otherDoc.equals(targetDoc)) {
                        Map<String, Integer> otherTerms = dtm.getTermsForDocument(otherDoc);
                        if (otherTerms == null || !otherTerms.containsKey(word)) {
                            isExclusive = true;
                            break;
                        }
                    }
                }
                
                if (isExclusive) {
                    candidates.add(word);
                }
            }
            
            if (candidates.isEmpty()) {
                attempts++;
                continue;
            }
            
            String word = candidates.get(random.nextInt(candidates.size()));
            String questionKey = word;
            
            if (!usedDocumentSpecificQuestions.contains(questionKey) || 
                usedDocumentSpecificQuestions.size() >= Math.min(50, documents.size() * 10)) {
                
                usedDocumentSpecificQuestions.add(questionKey);
                
                String questionText = String.format("In quale documento compare la parola \"%s\"?", word);
                
                List<String> docOptions = new ArrayList<>();
                docOptions.add(getGenericDocumentName(targetDoc));
                
                // Add other document names as distractors
                List<String> otherDocs = new ArrayList<>(documents);
                otherDocs.remove(targetDoc);
                Collections.shuffle(otherDocs);
                
                for (String doc : otherDocs) {
                    if (docOptions.size() < 4) {
                        docOptions.add(getGenericDocumentName(doc));
                    }
                }
                
                Collections.shuffle(docOptions);
                int correctIndex = docOptions.indexOf(getGenericDocumentName(targetDoc));
                
                return new Question(questionNumber, questionText, docOptions, correctIndex);
            }
            
            attempts++;
        }
        
        return null;
    }
    
    /**
     * Generates a question asking which word never appears in a specific document.
     *
     * @param questionNumber The sequence number of the question in the current game
     * @return the generated Question object, or null if generation fails
     */
    public Question generateExclusionQuestion(int questionNumber) {
        if (documents.isEmpty()) {
            return null;
        }
        
        int attempts = 0;
        
        while (attempts < MAX_ATTEMPTS) {
            String doc = getRandomDocument();
            Map<String, Integer> docTerms = dtm.getTermsForDocument(doc);
            
            if (docTerms == null || docTerms.isEmpty()) {
                attempts++;
                continue;
            }
            
            // Get words from other documents that don't appear in this one
            Set<String> allWords = dtm.getAllTerms();
            Set<String> docWords = docTerms.keySet();
            
            List<String> missingWords = allWords.stream()
                .filter(word -> !docWords.contains(word))
                .collect(Collectors.toList());
            
            if (missingWords.isEmpty()) {
                attempts++;
                continue;
            }
            
            String correctWord = missingWords.get(random.nextInt(missingWords.size()));
            String questionKey = doc + "|" + correctWord;
            
            if (!usedExclusionQuestions.contains(questionKey) ||
                usedExclusionQuestions.size() >= documents.size() * 5) {
                
                usedExclusionQuestions.add(questionKey);
                
                String genericDocName = getGenericDocumentName(doc);
                String questionText = String.format("Quale delle seguenti parole NON compare mai nel %s?", genericDocName);
                
                // Get some words that do appear in the document as distractors
                List<String> distractors = new ArrayList<>(docWords);
                Collections.shuffle(distractors);
                distractors = distractors.subList(0, Math.min(distractors.size(), 3));
                
                List<String> options = new ArrayList<>();
                options.add(correctWord);
                options.addAll(distractors);
                
                Collections.shuffle(options);
                int correctIndex = options.indexOf(correctWord);
                
                return new Question(questionNumber, questionText, options, correctIndex);
            }
            
            attempts++;
        }
        
        return null;
    }
    
    /**
     * Generates a simple fallback question when all other generation attempts fail.
     *
     * @param questionNumber The sequence number of the question in the current game
     * @return a basic fallback Question object
     */
    public Question generateFallbackQuestion(int questionNumber) {
        String doc = !documents.isEmpty() ? documents.get(0) : "documento generico";
        String genericDocName = getGenericDocumentName(doc);
        
        Map<String, Integer> termFreq = !documents.isEmpty() ? dtm.getTermsForDocument(doc) : null;
        
        if (termFreq != null && !termFreq.isEmpty()) {
            // Get the first available word
            String word = termFreq.keySet().iterator().next();
            int correctFreq = termFreq.get(word);
            
            String questionText = String.format("Quante volte compare la parola \"%s\" nel %s? (Fallback)", 
                word, genericDocName);
            
            // Generate simple options
            List<String> options = Arrays.asList(
                String.valueOf(correctFreq),
                String.valueOf(Math.max(0, correctFreq - 1)),
                String.valueOf(correctFreq + 1),
                String.valueOf(correctFreq + 2)
            );
            Collections.shuffle(options);
            
            int correctIndex = options.indexOf(String.valueOf(correctFreq));
            return new Question(questionNumber, questionText, options, correctIndex);
        } else {
            // Ultimate fallback if no words available
            List<String> options = Arrays.asList("0", "1", "2", "3");
            return new Question(questionNumber, 
                "Quante parole ci sono in questo documento? (Fallback)", options, 1);
        }
    }
    
    /**
     * Returns a random document from the available documents list.
     *
     * @return a randomly selected document name
     */
    private String getRandomDocument() {
        if (documents.isEmpty()) {
            throw new IllegalStateException("No documents available");
        }
        return documents.get(random.nextInt(documents.size()));
    }
    
    /**
     * Converts a document filename to a generic name for display.
     * Maps documents to "Documento 1", "Documento 2", etc. based on their order.
     *
     * @param filename the actual filename of the document
     * @return a generic document name for display
     */
    private String getGenericDocumentName(String filename) {
        int index = documents.indexOf(filename);
        if (index >= 0) {
            return "Documento " + (index + 1);
        }
        
        // Fallback if document not found in list
        return "Documento X";
    }
    
    /**
     * Functional interface for question generation strategies.
     */
    @FunctionalInterface
    private interface QuestionGenerator {
        Question generate();
    }
}
