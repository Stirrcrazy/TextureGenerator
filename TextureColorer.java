import java.awt.*;
import javax.swing.JFrame;
import java.util.*;
import java.lang.*;

public class TextureColorer {

   // remember you can use this class to assign values to bottom and top of colorcutoff, making it a proper colorramp
   // Make sure you can use one noise texture for each color channel, and thus paint on canvases using noise independent of the value of the resulting image
   
   public static void main(String args[]) {
      Random rand = new Random();
      double[][] Rmask = TextureGen.remap(TextureGen.smooth(TextureGen.generateNoise(rand), 50));
      double[][] Gmask = TextureGen.subtract(TextureGen.generateWaves(50, false), TextureGen.generateWaves(50, true), true);
      double[][] Bmask = TextureGen.remap(TextureGen.smooth(TextureGen.generateNoise(rand), 50));
      DrawingPanel panel = new DrawingPanel(Rmask.length, Rmask.length);
      double[][][] image  = assignColor(Gmask, 1, Gmask, 1.0, Gmask, 1);
      drawImage(image, panel);
   }
   
   public static void drawImage(double[][][] image, DrawingPanel panel) {
      for (int row = 0; row < image.length; row++) {
         for (int column = 0; column < image.length; column++) {
            Color c = new Color(((float) image[row][column][0]), ((float)image[row][column][1]), ((float)image[row][column][2]), ((float)1));
            panel.setPixel(column, row, c);
         }
      }
   }
   
   public static double[][][] assignColor(double[][] Rmask, double r, double[][] Gmask, double g, double[][] Bmask, double b) {
      double[][][] image = new double[Rmask.length][Rmask.length][3];
      for (int row = 0; row < Rmask.length; row++) {
         for (int column = 0; column < Rmask.length; column++) {
            image[row][column][0] = r * Rmask[row][column];
            image[row][column][1] = g * Gmask[row][column];
            image[row][column][2] = b * Bmask[row][column];
         }
      }
      return image;
   }
   
}