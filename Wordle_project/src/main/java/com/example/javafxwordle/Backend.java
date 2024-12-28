package com.example.javafxwordle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


/**
  com.example.wordless.Backend for a Wordle game.


 */
public class Backend {

    private static final int WORD_LENGTH = 5;
    private static final char GREEN = 'g';
    private static final char YELLOW = 'y';
    private static final char INCORRECT = 'i';
    private final Random rand;
    private final List<String> words;
    private String target;

    /**
     * Constructor for a Wordle com.example.com.example.javafxwordle.Backend.
     *
     * This constructor initializes a random target word from the words.txt word bank.
     */
    public Backend() throws IOException {

        words = getWords();
        rand = new Random();
        reset();
    }

    /**
     * Reads in the words from the words.txt word bank.
     *
     * @return a list of five letter words as strings, or a list only containing "adieu" if
     *         words.txt could not be found.
     *
     */
    List<String> getWords() {
        List<String> lines = new ArrayList<>();
        File file = new File("src/main/java/com/example/javafxwordle/words.txt");
        Scanner scan = null;
        try {
            scan = new Scanner(file);
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.length() == WORD_LENGTH) {
                    lines.add(line.toLowerCase());
                }
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println("Error in reading words.txt: " + fnfe.getMessage());
            lines.add("adieu");
        } finally {
            if (scan != null) {
                scan.close();
            }
        }
        return lines;
    }

    /**
     * This method sets the target word and is called once upon initializing a com.example.com.example.javafxwordle.Backend.
     * You should call this method whenever you need to reset a Wordle game.
     */
    public void reset() {
        target = words.get(rand.nextInt(words.size()));
    }


    public String check(String word) throws InvalidGuessException {
        if (word == null || word.length() != WORD_LENGTH || word.isBlank()) {
            throw new InvalidGuessException(word);
        }
        word = word.toLowerCase();
        char[] targetArray = target.toCharArray();
        char[] result = new char[WORD_LENGTH];

        for (int i = 0; i < WORD_LENGTH; i++) {
            result[i] = INCORRECT;
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (word.charAt(i) == targetArray[i]) {
                result[i] = GREEN;
                targetArray[i] = 0;
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (result[i] == INCORRECT) {
                for (int j = 0; j < WORD_LENGTH; j++) {
                    if (word.charAt(i) == targetArray[j]) {
                        result[i] = YELLOW;
                        targetArray[j] = 0;
                        break;
                    }
                }
            }
        }

        return String.valueOf(result);
    }


    public String getTarget() {
        return target;
    }

}