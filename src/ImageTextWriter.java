import java.awt.*;

class ImageTextWriter {
    private ImageTextWriter() { //Static class
        //Do not call!!!
    }

    static void writeDataToImage(Graphics2D g2, double fitness, int overlap, int minEnergy, int imageWidth, int fontSize){
        Font font = new Font("Serif", Font.PLAIN, fontSize);
        g2.setFont(font);
        int xMargin = 25;
        int yMarginTop = 40+fontSize/4; // just works ...
        g2.setColor(Color.BLACK);

        String combinedString = getCombinedString(fitness, overlap, minEnergy);

        if (StringFits(combinedString, g2, imageWidth, xMargin)){
            g2.drawString(combinedString, xMargin, yMarginTop);
        }else {//recursion with reduced font size
            int updatedFontSize = fontSize - fontSize / 10; //subtract one tenth
            writeDataToImage(g2,fitness,overlap,minEnergy,imageWidth, updatedFontSize);
        }
    }

    private static String getCombinedString(double fitness, int overlap, int minEnergy) {
        String fitnessString = "Fitness: " + roundedFitnessString(fitness);
        String overlapString = "Overlap: " + Integer.toString(overlap);
        String minEnergyString = "Energy: " + Integer.toString(minEnergy);
        return fitnessString+" | "+overlapString+" | "+minEnergyString;
    }

    private static boolean StringFits(String combinedString, Graphics2D g2, int imageWidth, int xMargin) {
        return g2.getFontMetrics().stringWidth(combinedString)+xMargin*2 < imageWidth;
    }

    private static String roundedFitnessString(double fitness){ //Format is #.##
        int value = Character.getNumericValue(Double.toString(fitness).charAt(5));
        if (value > 5){ //round up
            fitness += 0.01;
        }
        return Double.toString(fitness).substring(0,4);
    }
}