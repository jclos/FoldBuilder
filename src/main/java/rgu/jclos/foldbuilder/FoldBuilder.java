/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rgu.jclos.foldbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Jeremie
 */
public class FoldBuilder {
    public static class Instance {
        public String content ;
        public String label;
    }
    
    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options();
        options.addOption("i", "input", true, "Input file (mandatory)");
        options.addOption("k", true, "Number of folds (defaults to 2)");
        options.addOption("o", "output", true, "Output directory (defaults to current directory)");
        options.addOption("s", "separator", true, "Separating character in the CSV file (defaults to \\t)");
        options.addOption("idx", "index", true, "Index of the label (index or first or last) (defaults to last)");
        options.addOption("v", "verbose", false, "Print messages during fold generation (defaults to true)");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        
        String inputFile;
        String outputDirectory;
        String separator;
        String indexLabel;
        int k;
        boolean speak = false;
        
        if (cmd.hasOption("input")) {
            inputFile = cmd.getOptionValue("input");
        } else {
            throw new IllegalArgumentException("You need to provide an input file.");
        }
       
        k = Integer.parseInt(cmd.getOptionValue("k"), 2);
        outputDirectory = cmd.getOptionValue("output", ".");
        separator = cmd.getOptionValue("separator", "\t");
        indexLabel = cmd.getOptionValue("index", "last");
 
        if (cmd.hasOption("verbose")) {
            speak = true;
        }
        computeAndWriteFolds(inputFile, outputDirectory, separator, indexLabel, k, speak);
    }
    
    public static void main2(String[] args) throws IOException {
        String inputFile = args[0];
        String outputFile = args[1];
        String separator = args[2];
        int indexLabel = Integer.parseInt(args[3]);
        int k = Integer.parseInt(args[4]);
        boolean speak = Boolean.parseBoolean(args[5]);
        computeAndWriteFolds(inputFile, outputFile, separator, indexLabel, k, speak);
    }
    
    /**
     * Generates K folds and writes them to disk
     * @param inputFile The CSV file from which the data comes from.
     * @param outputDirectory The directory in which the folds will be written.
     * @param separator The separating character in the CSV file.
     * @param indexLabel The index of the labels in the CSV file. Used for stratification of the folds.
     * @param k The number of folds to generates.
     * @param speak Whether to print some status messages along the way.
     * @throws IOException If something stops the program from reading or writing the files.
     */
    public static void computeAndWriteFolds(String inputFile, String outputDirectory, String separator, int indexLabel, int k, boolean speak) throws IOException {    
        Pair<List<Set<String>>, Map<String, Instance>> tmp = getFolds(inputFile, outputDirectory, separator, indexLabel, k, speak); 
        Map<String, Instance> dictionary = tmp.getRight();
        List<Set<String>> folds = tmp.getLeft();
        if (speak) System.out.println("Writing folds on disk");
        for (int i = 0 ; i < folds.size() ; i++) {
            Set<String> trainingSet = new HashSet<>();
            Set<String> testSet = new HashSet<>();
            Set<String> trainingSetIds = new HashSet<>();
            trainingSetIds.addAll(folds.get(i));
            Set<String> testSetIds = new HashSet<>();
            for (int j = 0 ; j < folds.size() ; j++) {
                if (i != j) {
                    testSetIds.addAll(folds.get(j));
                }
            }
            trainingSetIds.forEach(tid -> {
                trainingSet.add(dictionary.get(tid).content);
            });
            testSetIds.forEach(tid -> {
                testSet.add(dictionary.get(tid).content);
            });
            
            String filenameTraining = "Fold_" + (i + 1) + "_TrainingSet.csv";
            String filenameTesting = "Fold_" + (i + 1) + "_TestingSet.csv";
            File outputTraining = new File(outputDirectory + File.separator + filenameTraining);
            File outputTesting = new File(outputDirectory + File.separator + filenameTesting);
            Files.write(outputTraining.toPath(), trainingSet);
            Files.write(outputTesting.toPath(), testSet);
        }
    }
    
        /**
     * Generates K folds and writes them to disk
     * @param inputFile The CSV file from which the data comes from.
     * @param outputDirectory The directory in which the folds will be written.
     * @param separator The separating character in the CSV file.
     * @param indexLabel The index of the labels in the CSV file. Used for stratification of the folds.
     * @param k The number of folds to generates.
     * @param speak Whether to print some status messages along the way.
     * @throws IOException If something stops the program from reading or writing the files.
     */
    private static void computeAndWriteFolds(String inputFile, String outputDirectory, String separator, String indexLabel, int k, boolean speak) throws IOException {    
        Pair<List<Set<String>>, Map<String, Instance>> tmp = getFolds(inputFile, outputDirectory, separator, indexLabel, k, speak); 
        Map<String, Instance> dictionary = tmp.getRight();
        List<Set<String>> folds = tmp.getLeft();
        if (speak) System.out.println("Writing folds on disk");
        for (int i = 0 ; i < folds.size() ; i++) {
            Set<String> trainingSet = new HashSet<>();
            Set<String> testSet = new HashSet<>();
            Set<String> trainingSetIds = new HashSet<>();
            trainingSetIds.addAll(folds.get(i));
            Set<String> testSetIds = new HashSet<>();
            for (int j = 0 ; j < folds.size() ; j++) {
                if (i != j) {
                    testSetIds.addAll(folds.get(j));
                }
            }
            trainingSetIds.forEach(tid -> {
                trainingSet.add(dictionary.get(tid).content);
            });
            testSetIds.forEach(tid -> {
                testSet.add(dictionary.get(tid).content);
            });
            
            String filenameTraining = "Fold_" + (i + 1) + "_TrainingSet.csv";
            String filenameTesting = "Fold_" + (i + 1) + "_TestingSet.csv";
            File outputTraining = new File(outputDirectory + File.separator + filenameTraining);
            File outputTesting = new File(outputDirectory + File.separator + filenameTesting);
            Files.write(outputTraining.toPath(), trainingSet);
            Files.write(outputTesting.toPath(), testSet);
        }
    }
    
    /**
     * Generates K folds and writes them to disk
     * @param inputFile The CSV file from which the data comes from.
     * @param outputDirectory The directory in which the folds will be written.
     * @param separator The separating character in the CSV file.
     * @param indexLabel The index of the labels in the CSV file. Used for stratification of the folds.
     * @param k The number of folds to generates.
     * @param speak Whether to print some status messages along the way.
     * @return A pair containing a list of folds with ids of documents, and a dictionary that allows the user to retrieve aformentioned documents using the ids, in order to save space.
     * @throws IOException If something stops the program from reading or writing the files.
     */
    public static Pair<List<Set<String>>, Map<String, Instance>> getFolds(String inputFile, String outputDirectory, String separator, int indexLabel, int k, boolean speak) throws IOException {    
        Random rng = new Random();
        Map<String, Instance> dictionary = new HashMap<>();
        Map<String, Integer> classes = new HashMap<>();
        Map<String, List<String>> reversedDictionary = new HashMap<>();
        int id = 0;
        for (String line : Files.readAllLines(new File(inputFile).toPath())) {
            Instance inst = new Instance();
            String[] elements = line.split(separator);
            inst.content = line;
            inst.label = elements[indexLabel];
            String iid = "inst" + id ;
            dictionary.put(iid, inst);
            classes.put(inst.label, classes.getOrDefault(inst.label, 0) + 1);
            if (reversedDictionary.containsKey(inst.label)) {
                reversedDictionary.get(inst.label).add(iid);
            } else {
                List<String> ids = new ArrayList<>();
                ids.add(iid);
                reversedDictionary.put(inst.label, ids);
            }
            id++;
        }
        
        int numberOfInstances = id;
        int sizeOfEachFold = (int) Math.floor(numberOfInstances / k);
        Map<String, Double> classRatios = new HashMap<>();
        for(Map.Entry<String, Integer> classFrequency : classes.entrySet()) {
            classRatios.put(classFrequency.getKey(), (double)classFrequency.getValue() / (double)numberOfInstances);
        }

        List<Set<String>> folds = new ArrayList<>();
        for (int i = 0 ; i < k ; i++) {
            Set<String> fold = new HashSet<>();
            for (Map.Entry<String, List<String>> c : reversedDictionary.entrySet()) {
                int currentSize = fold.size();
                int numberRequired = (int) Math.floor(classRatios.get(c.getKey()) * sizeOfEachFold);
                while (fold.size() < currentSize + numberRequired && c.getValue().size() > 0) {
                    int nextPick = rng.nextInt(c.getValue().size());
                    fold.add(c.getValue().get(nextPick));
                    c.getValue().remove(nextPick);
                }
            }   
            folds.add(fold);
            if (speak) System.out.println("Finished computing fold " + (i + 1) + " of size " + fold.size());
        }
        
        if (speak) System.out.println("Writing folds on disk");
 
        return Pair.of(folds, dictionary);
    }
    
     /**
     * Generates K folds and writes them to disk
     * @param inputFile The CSV file from which the data comes from.
     * @param outputDirectory The directory in which the folds will be written.
     * @param separator The separating character in the CSV file.
     * @param indexLabel The index of the labels in the CSV file. Used for stratification of the folds.
     * @param k The number of folds to generates.
     * @param speak Whether to print some status messages along the way.
     * @return A pair containing a list of folds with ids of documents, and a dictionary that allows the user to retrieve aformentioned documents using the ids, in order to save space.
     * @throws IOException If something stops the program from reading or writing the files.
     */
    private static Pair<List<Set<String>>, Map<String, Instance>> getFolds(String inputFile, String outputDirectory, String separator, String indexLabel, int k, boolean speak) throws IOException {    
        Random rng = new Random();
        Map<String, Instance> dictionary = new HashMap<>();
        Map<String, Integer> classes = new HashMap<>();
        Map<String, List<String>> reversedDictionary = new HashMap<>();
        int id = 0;
        
        List<String> lines = Files.readAllLines(new File(inputFile).toPath());
        String[] elts = lines.get(0).split(separator);
        int labIndex = indexLabel.equals("first") ? 0 : indexLabel.equals("last") ? elts.length - 1 : Integer.parseInt(indexLabel);
        
        for (String line : Files.readAllLines(new File(inputFile).toPath())) {
            Instance inst = new Instance();
            String[] elements = line.split(separator);
            inst.content = line;
            inst.label = elements[labIndex];
            String iid = "inst" + id ;
            dictionary.put(iid, inst);
            classes.put(inst.label, classes.getOrDefault(inst.label, 0) + 1);
            if (reversedDictionary.containsKey(inst.label)) {
                reversedDictionary.get(inst.label).add(iid);
            } else {
                List<String> ids = new ArrayList<>();
                ids.add(iid);
                reversedDictionary.put(inst.label, ids);
            }
            id++;
        }
        
        int numberOfInstances = id;
        int sizeOfEachFold = (int) Math.floor(numberOfInstances / k);
        Map<String, Double> classRatios = new HashMap<>();
        for(Map.Entry<String, Integer> classFrequency : classes.entrySet()) {
            classRatios.put(classFrequency.getKey(), (double)classFrequency.getValue() / (double)numberOfInstances);
        }

        List<Set<String>> folds = new ArrayList<>();
        for (int i = 0 ; i < k ; i++) {
            Set<String> fold = new HashSet<>();
            for (Map.Entry<String, List<String>> c : reversedDictionary.entrySet()) {
                int currentSize = fold.size();
                int numberRequired = (int) Math.floor(classRatios.get(c.getKey()) * sizeOfEachFold);
                while (fold.size() < currentSize + numberRequired && c.getValue().size() > 0) {
                    int nextPick = rng.nextInt(c.getValue().size());
                    fold.add(c.getValue().get(nextPick));
                    c.getValue().remove(nextPick);
                }
            }   
            folds.add(fold);
            if (speak) System.out.println("Finished computing fold " + (i + 1) + " of size " + fold.size());
        }
        
        if (speak) System.out.println("Writing folds on disk");
 
        return Pair.of(folds, dictionary);
    }
}
