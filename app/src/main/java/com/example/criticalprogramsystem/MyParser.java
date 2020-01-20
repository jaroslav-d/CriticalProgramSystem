package com.example.criticalprogramsystem;

import android.util.Log;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import opennlp.tools.lemmatizer.SimpleLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

public class MyParser {

    private static final String TAG = "MyParser";
    private static String FILE_NAME = "/home/jaroslav/Downloads/1.txt";
    private static String PATH_DATA = "/home/jaroslav/Documents/CriticalProgramSystem/Data/";
    private static String PATH_MAIN = "/home/jaroslav/Documents/CriticalProgramSystem/";
    private static String PATH_MODELS = "/home/jaroslav/Documents/CriticalProgramSystem/Models/";
    private static String FILE_FORMAT = ".pdf";

    public static void main(String[] args) throws IOException {
        /* Read .txt file */
//        Files.lines(Paths.get(FILE_NAME), StandardCharsets.UTF_8).forEach(System.out::println);
//        Set<String> stopwords = new HashSet<>(
//                Files.readAllLines(Paths.get(PATH_MAIN + "stop_word.txt"), StandardCharsets.UTF_8)
//        );
        Set<String> stopwords = new HashSet<>();
        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(PATH_MAIN + "stop_word.txt"),
                                     StandardCharsets.UTF_8
                             )
                     )
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopwords.add(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "This file don't read or don't find");
        }

        /* Read .pdf files */
        File dir = new File(PATH_DATA); //path указывает на директорию
        File[] listFiles = dir.listFiles();
        ArrayList<PDDocument> listDocs = new ArrayList<>();
        for (File file : listFiles) {
            listDocs.add(PDDocument.load(file));
        }

        ArrayList<String> wordBag = new ArrayList<>();
        ArrayList<String[]> updateTokens = new ArrayList<>();

        for (PDDocument document : listDocs) {

            /* Text preparation */
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String fulltext = pdfStripper.getText(document);
            String regex = "Introduction|References|Abstract|INTRODUCTION|REFERENCES|ABSTRACT";
            String[] split = fulltext.split(regex);
            String text = split.length == 3 ? split[1] : split[0];
            String cleartext = Pattern
                    .compile("[^a-zA-Z_]|(?<=\\W|\\d)\\w{1,3}(?=\\W|\\d)")
                    .matcher(text)
                    .replaceAll(" ");
            for (String substring : stopwords) {
                cleartext = Pattern
                        .compile("(?<=\\s)" + substring + "(?=\\s)")
                        .matcher(cleartext)
                        .replaceAll(" ");
            }
            String[] tokens = cleartext.split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = tokens[i].toLowerCase();
            }

            TreeSet<String> setTokens = new TreeSet<>(Arrays.asList(tokens));

            String[] lemmas = new String[tokens.length];
            ArrayList<String> setLemmas = new ArrayList<>();
            ArrayList<Integer> idxLemmas = new ArrayList<>();

            /* Get properties from document */
//            PDDocumentInformation pdd = document.getDocumentInformation();
//            System.out.println(
//                    "Author: " + pdd.getAuthor() + "\n" +
//                            "Creator: " + pdd.getCreator() + "\n" +
//                            "Title: " + pdd.getTitle() + "\n" +
//                            "Subject: " + pdd.getSubject() + "\n" +
//                            "Keywords: " + pdd.getKeywords() + "\n" +
//                            "Producer: " + pdd.getProducer() + "\n"
//            );

            /* Show text */
//            System.out.println(fulltext);

            try {

                InputStream posModelIn = new FileInputStream(PATH_MODELS + "en-pos-maxent.bin");
                // loading the parts-of-speech model from stream
                POSModel posModel = new POSModel(posModelIn);
                // initializing the parts-of-speech tagger with model
                POSTaggerME posTagger = new POSTaggerME(posModel);
                // Tagger tagging the tokens
                String tags[] = posTagger.tag(tokens);

                InputStream dictLemmatizer = new FileInputStream(PATH_MODELS + "en-lemmatizer.txt");
                // loading the lemmatizer with dictionary
                SimpleLemmatizer lemmatizer = new SimpleLemmatizer(dictLemmatizer);

                for (int i = 0; i < tokens.length; i++) {
                    lemmas[i] = lemmatizer.lemmatize(tokens[i], tags[i]);
                    if (!setLemmas.contains(lemmas[i])) {
                        idxLemmas.add(i);
                        setLemmas.add(lemmas[i]);
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            SnowballStemmer sm = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
            String[] stemms = new String[tokens.length];
            ArrayList<String> setStemms = new ArrayList<>();
            ArrayList<Integer> idxStemms = new ArrayList<>();
            for (int i = 0; i < tokens.length; i++) {
                stemms[i] = (String) sm.stem(tokens[i]);
                if (!setStemms.contains(stemms[i])) {
                    idxStemms.add(i);
                    setStemms.add(stemms[i]);
                }
            }

//            ArrayList<String> different = new ArrayList<>();
//            for (Integer idx : idxLemmas) {
//                if (!idxStemms.contains(idx)) {
//                    different.add(setLemmas.get(idxLemmas.indexOf(idx)));
//                }
//                if (setLemmas.get(idxLemmas.indexOf(idx)).equals("better")) {
//                    System.out.println(setLemmas.indexOf("better"));
//                    System.out.println(idx.intValue());
//                }
//                if (setLemmas.get(idxLemmas.indexOf(idx)).equals("branch")) {
//                    System.out.println(setLemmas.indexOf("branch"));
//                    System.out.println(idx.intValue());
//                }
//            }
//            for (Integer idx : idxStemms) {
//                if (setStemms.get(idxStemms.indexOf(idx)).equals("better")) {
//                    System.out.println(setStemms.indexOf("better"));
//                    System.out.println(idx.intValue());
//                }
//                if (setStemms.get(idxStemms.indexOf(idx)).equals("branch")) {
//                    System.out.println(setStemms.indexOf("branch"));
//                    System.out.println(idx.intValue());
//                }
//            }
//            Collections.sort(different);
//            Collections.sort(setStemms);
//            Collections.sort(setLemmas);

            wordBag.addAll(setLemmas);
            updateTokens.add(lemmas);
            document.close();
        }

        /* HAL - method */
        ArrayList<String> dictionary = new ArrayList<>(new TreeSet<>(wordBag));
        int[][] hal = new int[dictionary.size()][dictionary.size()];
        int windowSize = 10;
        for (String[] lemmas : updateTokens) {
            for (int i = 0; i < lemmas.length; i++) {
                int indexLemmaFrom = dictionary.indexOf(lemmas[i]);
                for (int j = 1; j < windowSize + 1; j++) {
                    if (i + j < lemmas.length) {
                        int indexLemmaTo = dictionary.indexOf(lemmas[i + j]);
                        hal[indexLemmaFrom][indexLemmaTo] = hal[indexLemmaFrom][indexLemmaTo] + windowSize - j + 1;
                    }
                }
            }
        }

        /* TF-IDF */
        ArrayList<Integer> tf = new ArrayList<>();
        for (String[] lemmas : updateTokens) {
            for (String word : dictionary) {
                int numIn = 0;
                for (String lemma : lemmas) {
                    numIn = lemma.equals(word) ? numIn + 1 : numIn;
                }
                tf.add((int) numIn/dictionary.size());
            }
        }

        ArrayList<Integer> idf = new ArrayList<>();
        for (String word : dictionary) {
            int numDoc = 0;
            for (String[] lemmasArray : updateTokens) {
                List<String> lemmasList = Arrays.asList(lemmasArray);
                numDoc = lemmasList.contains(word) ? numDoc + 1 : numDoc;
            }
            idf.add((int) Math.log(updateTokens.size()/numDoc));
        }

        int oldMax = 1000;
        for (int i = 0; i < 20; i++) {
            int idxFirst = 0;
            int idxLast = 0;
            int newMax = 0;
            for (int row = 0; row < hal.length; row++) {
                for (int col = 0; col < hal.length; col++) {
                    if (hal[row][col] > newMax && oldMax > hal[row][col]) {
                        newMax = hal[row][col];
                        idxFirst = row;
                        idxLast = col;
                    }
                }
            }
            oldMax = newMax;
            System.out.println(dictionary.get(idxFirst));
            System.out.println(dictionary.get(idxLast));
        }

        FileWriter fw = new FileWriter(PATH_DATA + "HAL.csv");
        for (String str: dictionary) {
            fw.write(str + "; ");
        }
        for (int[] row : hal) {
            for (int element : row) {
                fw.write("" + element + "; ");
            }
            fw.write("\n");
        }
        fw.close();
    }
}
