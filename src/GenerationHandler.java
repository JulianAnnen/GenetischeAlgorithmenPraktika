import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class GenerationHandler {

    private String sequence;
    private ArrayList<Kette> individuals = new ArrayList<>();
    private ImageCreator imageCreator = new ImageCreator();
    RandomCollection<Kette> randomCollection = new RandomCollection<>();
    private int maxGenerations = 0;
    private int generationSize = 0;
    private int newBloodAmount = 0;

    public GenerationHandler(String sequence) {
        this.sequence = sequence;
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

            //sort the list so the best individuals are on top (0 and 1)

            selectLuckyFew(generationSize/2);

            individuals.sort((Kette ketteA, Kette ketteB) -> Double.compare(ketteB.calcFitness(),ketteA.calcFitness()));

            if (generation != maxGenerations -1) { // if not the last generation
                //makeSomeBabys();
                makeSomeMutants(generation);
                //makeSomeNewBlood(generation);
            }else{
                individuals.subList(5, individuals.size()).clear(); // kill all but the 2 best
            }
            printLogTxt();
        }
    }

    private void selectLuckyFew(int selectionSize){ //Programm freezes if selection is bigger than generation Size
        generateRandomCollection();

        individuals.clear();

        for (int i = 0; i < selectionSize; i++){
            individuals.add(randomCollection.next());
        }
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

    void printResult() { //todo: move image creation somewhere else
        for (int i = 0; i < individuals.size(); i++){
            imageCreator.createImage(individuals.get(i).getPhenotype(), Integer.toString(i)+ ".png");
            System.out.println();
            individuals.get(i).printValues();
        }
    }

    void printLogTxt(){
        //create folder
        String folder = "/ga";
        if (!new File(folder).exists()) {
            //noinspection ResultOfMethodCallIgnored
            new File(folder).mkdirs();
        }
        try (PrintWriter out = new PrintWriter(new FileWriter(new File("/ga" + File.separator +"Log.txt"),true))) {
            for(Kette kette: individuals){
                out.print(kette.calcFitness() + ",");
            }
            out.print("\n");
        }catch (java.io.IOException e){
            System.out.println("Log file not found");
        }
    }

}
