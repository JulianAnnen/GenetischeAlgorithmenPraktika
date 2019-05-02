import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class GenerationHandler {

    private String sequence;
    private ArrayList<Kette> individuals = new ArrayList<>();
    private ImageCreator imageCreator = new ImageCreator();
    private RandomCollection<Kette> randomCollection = new RandomCollection<>();
    private int maxGenerations = 0;
    private int generationSize = 0;
    private int newBloodAmount = 0;
    private Kette bestIndividual = new Kette("");
    private DefaultCategoryDataset dataset = new DefaultCategoryDataset( );


    public GenerationHandler(String sequence) {
        this.sequence = sequence;
        LineChart chart = new LineChart(
                "Fitness Graph" ,
                "Live line graph showing the current progress",
                dataset);

        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }


    void initializeGeneration(int generationSize) {
        for (int i = 0; i < generationSize; i++) {
            individuals.add(new Kette(sequence));
            individuals.get(i).generateByRng();
        }
    }

    void evolve(int maxGenerations, int generationSize, int newBloodAmount){
        this.maxGenerations = maxGenerations;
        this.generationSize = generationSize;
        this.newBloodAmount = newBloodAmount;


        for (int generation = 0; generation < maxGenerations; generation++){
            //fitnessBiasedSelection(generationSize/2);
            tournamentSelection(5,500);
            //individuals.sort((Kette ketteA, Kette ketteB) -> Double.compare(ketteB.calcFitness(),ketteA.calcFitness()));


            printLogTxt(generation);if (generation != maxGenerations -1) { // if not the last generation
                //makeSomeBabys();
                makeSomeMutants(generation);
                //makeSomeNewBlood(generation);
            }else{ //if its the last generation
                //individuals.subList(5, individuals.size()).clear(); // kill all but the x best
            }
        }
    }

    private void fitnessBiasedSelection(int selectionSize){ //Programm freezes if selection is bigger than generation Size
        generateRandomCollection();

        individuals.clear();

        for (int i = 0; i < selectionSize; i++){
            individuals.add(randomCollection.next());
        }
    }

    private void tournamentSelection(int tournamentSize, int numberOfTournaments){
        ArrayList<Kette> champions = new ArrayList<>();
        Kette champion = new Kette("");
        for (int i = 0; i < numberOfTournaments; i++) {
            double bestFoundFitness = 0;
            for (int j = 0; j < tournamentSize; j++) {
                int random = getRandomIntInRange(0,individuals.size()-1);
                Kette challenger = individuals.get(random);
                if (challenger.calcFitness() > bestFoundFitness){
                    bestFoundFitness = challenger.calcFitness();
                    champion = challenger;
                }
            }
            champions.add(champion);
        }
        individuals.clear();

        individuals.addAll(champions);
    }

    private void generateRandomCollection() {
        double overallFitness = calcOverallFitness();
        for (Kette individual : individuals){
            double weight = (individual.calcFitness()/overallFitness);
            weight = weight*100;
            randomCollection.add(weight,individual);
        }
    }

    private void makeSomeBabys(){ //Todo: remake function with crossover chance value
        //create 2 offspring's
        ArrayList<Integer> chromosomeA = ChromosomeHandler.extractChromosome(individuals.get(0).getPhenotype());
        ArrayList<Integer> chromosomeB = ChromosomeHandler.extractChromosome(individuals.get(1).getPhenotype());

        ArrayList<Integer> childA = ChromosomeHandler.crossoverChromosome(chromosomeA,chromosomeB);
        ArrayList<Integer> childB = ChromosomeHandler.crossoverChromosome(chromosomeB,chromosomeA);

        individuals.add(ChromosomeHandler.chromosome2phenotype(childA, sequence));
        individuals.add(ChromosomeHandler.chromosome2phenotype(childB, sequence));
    }

    //Todo: make altering the mutation rate somewhat convenient
    private void makeSomeMutants(int generation){
        int initialPop = individuals.size();
        // fill the generationSize while leaving space for newBlood also no need to do that in the last gen
        while (individuals.size() < generationSize - newBloodAmount){
            int randomNum = ThreadLocalRandom.current().nextInt(0, initialPop);
            ArrayList<Integer> chromosomeMutant = ChromosomeHandler.extractChromosome(individuals.get(randomNum).getPhenotype());
            ArrayList<Integer> mutant = ChromosomeHandler.mutateChromosome(chromosomeMutant, 0.1);
            individuals.add(ChromosomeHandler.chromosome2phenotype(mutant, sequence));
        }
    }

    private void makeSomeNewBlood(int generation){
        while (individuals.size() < generationSize){
            individuals.add(new Kette(sequence));
            individuals.get(individuals.size()-1).generateByRng();
        }
    }

    private double calcOverallFitness(){
        double avr = 0;
        for (Kette kette : individuals){
            avr += kette.calcFitness();
        }
        //avr = avr/individuals.size();
        return avr;
    }

    void drawResult(int top) { // top defines the best x you want the image of
        individuals.sort((Kette ketteA, Kette ketteB) -> Double.compare(ketteB.calcFitness(),ketteA.calcFitness()));
        for (int i = 0; i < top; i++){
            imageCreator.createImage(individuals.get(i).getPhenotype(), Integer.toString(i)+ ".png");
            System.out.println();
            individuals.get(i).printValues();
        }
    }

    private void printLogTxt(int generation){
        individuals.sort((Kette ketteA, Kette ketteB) -> Double.compare(ketteB.calcFitness(),ketteA.calcFitness()));
        if(bestIndividual.calcFitness() < individuals.get(0).calcFitness())
            bestIndividual = individuals.get(0);
        //create folder
        String folder = "/ga";
        if (!new File(folder).exists()) {
            //noinspection ResultOfMethodCallIgnored
            new File(folder).mkdirs();
        }
        try (PrintWriter out = new PrintWriter(new FileWriter(new File("/ga" + File.separator +"Log.txt"),true))) {
            out.print((Integer.toString(generation) + "," + calcOverallFitness() / generationSize) + "," +
                    individuals.get(0).calcFitness() + "," + bestIndividual.calcFitness() + "," +
                    bestIndividual.calcMinEnergy() + "," + bestIndividual.calcOverlap());
            out.print("\n");

            dataset.addValue( individuals.get(0).calcFitness() , "current best" , Integer.toString(generation) );
            dataset.addValue( bestIndividual.calcFitness() , "overall best" , Integer.toString(generation) );
            dataset.addValue( calcOverallFitness() / generationSize , "other" , Integer.toString(generation) );

        }catch (java.io.IOException e){
            System.out.println("Log file not found");
        }
    }

    private static int getRandomIntInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }


}
