import java.util.ArrayList;

class Kette {
    //Vars
    private String kette;
    private ArrayList<Node> kette2d = new ArrayList<>();
    private Random2DGenerator random2DGenerator = new Random2DGenerator();

    //Constructor
    Kette(String new_string) {
        kette = new_string;
    }

    Kette(String new_string, ArrayList<Node> kette2d, ArrayList<Integer> chromosome) {
        kette = new_string;
        this.kette2d = kette2d;
        forceChromosome(chromosome);
    }

    private void forceChromosome(ArrayList<Integer> chromosome){
        for (int gene = 0; gene < chromosome.size(); gene ++){
            kette2d.get(gene).setGene(chromosome.get(gene));
        }
    }


    ArrayList<Node> getKette2d() {
        return kette2d;
    }

    void generateByRngNoOverlap() {
        while (kette2d.size() == 0) // 0 means returned graph is invalid
            kette2d = random2DGenerator.generateRandomGraphNoOverlap(kette);
    }

    void generateByRng(){
        kette2d = random2DGenerator.generateRandomGraph(kette);
    }

    void generateByIntelligentRng(){
        kette2d = random2DGenerator.generateIntelligentRandomGraph(kette);
    }

    private int calcMinEnergie() {
        int counter = 0;
        for (int i = 0; i < kette2d.size(); i++) {
            if (kette2d.get(i).getValue() == 1) {

                int i_x = kette2d.get(i).getX();
                int i_y = kette2d.get(i).getY();

                for (int j = 0; j < kette2d.size(); j++) {

                    if (Math.abs(i - j) > 1 && kette2d.get(j).getValue() == 1) { //not the same and both 1

                        int j_x = kette2d.get(j).getX();
                        int j_y = kette2d.get(j).getY();

                        if (Math.abs(i_x - j_x) == 1 && i_y == j_y) { // x is +-1 y the same
                            counter++;
                        } else if (Math.abs(i_y - j_y) == 1 && i_x == j_x) { // y is +-1 x the same
                            counter++;
                        }
                    }
                }
            }
        }
        return counter / 2; //every connection is listed 2 times (a to b and b to a)
    }

    private int calcOverlap(){
        int counter = 0;
        for (Node nodeA : kette2d){
            int a_x = nodeA.getX();
            int a_y = nodeA.getY();

            for (Node nodeB : kette2d){
                int b_x = nodeB.getX();
                int b_y = nodeB.getY();

                if (a_x == b_x && a_y == b_y && nodeA != nodeB){
                    counter ++;
                }
            }
        }
        return counter / 2;
    }

    double calcFitness (){
        double countOfPairs = calcMinEnergie();
        double countOfOverlap = calcOverlap();
        return ((1 + countOfPairs) / ((1 + countOfOverlap) * 100));
    }

    void printValues(){
        System.out.println();
        System.out.println("Minimale Energie: " + calcMinEnergie());
        System.out.println("Overlap: " + calcOverlap());
        System.out.println("Fitness: " + calcFitness());
    }

}
