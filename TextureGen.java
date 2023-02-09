// Tim Norwood Computer Science Projects
//
// Generates noise textures from algorithms to any resolution, and can make them tilable.

import java.awt.*;
import javax.swing.JFrame;
import java.util.*;
import java.lang.*;
import java.io.*;

public class TextureGen {
   
   public static final int RESOLUTION = 512;
   
   public static void main(String[] args) {
      DrawingPanel panel = new DrawingPanel(RESOLUTION, RESOLUTION);
      Graphics g = panel.getGraphics();
      Random rand = new Random();
      //double[][] ones = new double[1080][1920];
      //for (int row = 0; row < 1080; row++) {
         //for (int column = 0; column < 1920; column++) {
            //ones[row][column] = 1.0;
         //}
      //}
      double[][] noise = remap(extremaRemap(colorCutoff(remap(smooth(generateNoise(rand), 20)), 0.2, 0.5)));
      drawFour(noise);
      //File frame1 = new File("F:PROJECT.2/FX/henryframestobeinterp/h_005.png");
      //File frame2 = new File("F:PROJECT.2/FX/henryframestobeinterp/h_006.png");
      //DrawingPanel image1 = new DrawingPanel(frame1);
      //DrawingPanel image2 = new DrawingPanel(frame2);
      //double[][] noise = subtract(ones, imageToMask(image1), false);
      //double[][] noise2 = subtract(ones, colorCutoff(imageToMask(image2), 0, 1.0), false);
      //double [][] print = interpolateMask(noise, noise2);
      //drawNoise(panel, rand, noise2);
      //drawNoise(panel, rand, print);
   }
   
   // Generates a random value at each pixel
   // Returns an array of values of the pixels
   // Parameters:
   //    Random rand - The random number generator used in the method
   public static double[][] generateNoise(Random rand) {
      double[][] noise = new double[RESOLUTION][RESOLUTION];
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            noise[row][column] = ((double) rand.nextInt(256)) / 256.0;
         }
      }
      return noise;
   }
   
   // Generates cell noise of a size according to the resolution constant
   // Returns an array of values for each pixel
   // Parameters:
   //    Random rand - The random number generator the method uses
   //    int points - The threshold for determining if a given pixel will be the center of a cell
   public static double[][] generateCells(Random rand, int points) {
      double[][] noise = new double[RESOLUTION][RESOLUTION];
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            noise[row][column] = 1.0;
            if (rand.nextInt(RESOLUTION) < Math.sqrt(points)) {
               noise[row][column] = 0.0;
            }
         }
      }
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            int radius = 1;
            while (noise[row][column] == 1.0) {
               for (int row2 = 0 - (radius); row2 < (radius); row2++) {
                  for (int column2 = 0 - (radius); column2 < (radius); column2++) {
                     if (row + row2 >= 0 && column + column2 >= 0 && row + row2 < RESOLUTION && column + column2 < RESOLUTION) {
                        if (noise[row + row2][column + column2] == 0.0) {
                           if (noise[row][column] == 1.0) {
                              noise[row][column] -= (1.0 / radius);// WHEN ONE PIXEL IS BLACK, ALL AFTER IT IN THE ROW ARE BLACK BECAUSE THE NEXT PIXEL CHECKS THE PREVIOUS ONE AND SEES IT AS A VORONOI POINT.
                           }
                           if (noise[row][column] == 0.0) {
                              noise[row][column] = 0.05;
                           }
                        }
                     }
                  }
               }
               radius++;
            }
         }
      }
      return noise;
   }
   
   // Generates a texture that repeatedly fades between black and white
   // Returns an array of values for each pixel
   // Parameters:
   //    int period - The width of the waves in pixels
   //    boolean vert - If true, the waves will be vertical rather than horizontal
   public static double[][] generateWaves(int period, boolean vert) {
      double[][] noise = new double[RESOLUTION][RESOLUTION];
      double[][] gradient = new double[RESOLUTION][RESOLUTION];
      for (int i = 0; i < RESOLUTION / period; i++) {
         if (i % 2 == 0) {
            gradient = generateGradient(i * period, (i * period) + period, 0, 1, vert);
         }
         else {
            gradient = generateGradient((i * period) - 1, (i * period) + period, 1, 0, vert);
         }
         for (int row = 0; row < RESOLUTION; row++) {
            for (int column = 0; column < RESOLUTION; column++) {
               if (gradient[row][column] == 1) {
                  gradient[row][column] = 0;
               }
               noise[row][column] += gradient[row][column];
            }
         }
      }
      return noise;
   }
   
   // Generates a gradient from one value to another across a given gap
   // Returns an array of values for each pixel
   // Parameters:
   //    int start - The row/column that the gradient starts from
   //    int end - The row/column that the gradient ends at
   //    double fromValue - The starting value
   //    double toValue - The ending value
   //    boolean vert - If true, the gradient will be vertical instead of horizontal
   public static double[][] generateGradient(int start, int end, double fromValue, double toValue, boolean vert) {
      double[][] noise = new double[RESOLUTION][RESOLUTION];
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            if (row < start && (vert)) {
               noise[row][column] = fromValue;
            }
            if (row > end && (vert)) {
               noise[row][column] = toValue;
            }
            if (row >= start && row <= end && (vert)) {
               int distance = end - start;
               double increment = (toValue - fromValue) / (double) distance;
               noise[row][column] = ((double) fromValue + (increment * (row - start)));
            }
            if (column < start && !(vert)) {
               noise[row][column] = fromValue;
            }
            if (column > end && !(vert)) {
               noise[row][column] = toValue;
            }
            if (column >= start && column <= end && !(vert)) {
               int distance = end - start;
               double increment = (toValue - fromValue) / (double) distance;
               noise[row][column] = ((double) fromValue + (increment * (column - start)));
            }
            if (noise[row][column] > 1.0) {
               noise[row][column] = 1.0;
            }
            if (noise[row][column] < 0.0) {
               noise[row][column] = 0.0;
            }
         }
      }
      return noise;
   }
   
   // Generates voronoi noise of a size according to the resolution constant
   // Returns an array of values for each pixel
   // Parameters:
   //    Random rand - The random number generator the method uses
   //    int points - The threshold for determining if a given pixel will be the center of a cell
   public static double[][] generateVoronoi(Random rand, int points) {
      double[][] noise = new double[RESOLUTION][RESOLUTION];
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            noise[row][column] = 1.0;
            if (rand.nextInt(RESOLUTION) < Math.sqrt(points)) {
               noise[row][column] = 0.0;
            }
         }
      }
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            int radius = 1;
            while (noise[row][column] == 1.0) {
               for (int row2 = 0 - (radius); row2 < (radius); row2++) {
                  for (int column2 = 0 - (radius); column2 < (radius); column2++) {
                     if (Math.sqrt((Math.abs(row2) * Math.abs(row2)) + (Math.abs(column2) * Math.abs(column2))) <= radius) { 
                        if (row + row2 >= 0 && column + column2 >= 0 && row + row2 < RESOLUTION && column + column2 < RESOLUTION) {
                           if (noise[row + row2][column + column2] == 0.0) {
                              if (noise[row][column] == 1.0) {
                                 noise[row][column] -= (1.0 / radius);// WHEN ONE PIXEL IS BLACK, ALL AFTER IT IN THE ROW ARE BLACK BECAUSE THE NEXT PIXEL CHECKS THE PREVIOUS ONE AND SEES IT AS A VORONOI POINT.
                              }
                              if (noise[row][column] == 0.0) {
                                 noise[row][column] = 0.05;
                              }
                           }
                        }
                     }
                  }
               }
               radius++;
            }
         }
      }
      return noise;
   }
   
   // Draws an image in black and white according to the values in a 2D array
   // Parameters:
   //    DrawingPanel panel - The place the image is visible
   //    Random rand - Unused???
   //    double[][] noise - The array to be used to compose the image
   public static void drawNoise(DrawingPanel panel, Random rand, double[][] noise) {
      for (int row = 0; row < noise.length; row++) {
         for (int column = 0; column < noise[1].length; column++) {
            Color c = new Color(((float) noise[row][column]), ((float)noise[row][column]), ((float)noise[row][column]), ((float)1));
            if (row < 1079) {
               panel.setPixel(column, row, c);
            }
         }
      }
   }
   
   // Clips the black and white points of a given set of values
   // Returns an array of values for each pixel
   // Parameters:
   //    double[][] noise - The array of pixels to be edited
   //    double black - A value between 0 and 1 to clip to black below
   //    double white - A value between 0 and 1 to clip to white above
   public static double[][] colorCutoff(double[][] noise, double black, double white) {
      for (int row = 0; row < noise.length; row++) {
         for (int column = 0; column < noise[1].length; column++) {
            if (noise[row][column] < black) {
               noise[row][column] = 0;
            }
            if (noise[row][column] > white) {
               noise[row][column] = 1;
            }
         }
      }
      return noise;
   }
   
   public static double[][] extremaRemap(double[][] noise) {
      double max = 0.0;
      double min = 1.0;
      for (int row = 0; row < noise.length; row++) {
         for (int column = 0; column < noise[1].length; column++) {
            if (noise[row][column] > max && noise[row][column] < 1.0) {
               max = noise[row][column];
            }
            if (noise[row][column] < min && noise[row][column] > 0.0) {
               min = noise[row][column];
            }
         }
      }
      for (int row = 0; row < noise.length; row++) {
         for (int column = 0; column < noise[1].length; column++) {
            if (noise[row][column] == 1.0) {
               noise[row][column] = max;
            }
            if (noise[row][column] == 0.0) {
               noise[row][column] = min;
            }
         }
      }
      return noise;
   }
   
   public static double[][] smooth(double[][] noise, int margin) {
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            double average = 0;
            int iterations = 0;
            for (int row2 = 0 - (margin / 2); row2 < (margin / 2); row2++) {
               for (int column2 = 0 - (margin / 2); column2 < (margin / 2); column2++) {
                  if (row + row2 >= 0 && column + column2 >= 0) {
                     average += noise[(row + row2) % RESOLUTION][(column + column2) % RESOLUTION];
                     iterations++;
                  }
               }
            }
            average = average / iterations;
            if (noise[row][column] < average) {
               noise[row][column] += (average - noise[row][column]);
            }
            if (noise[row][column] > average) {
               noise[row][column] -= (noise[row][column] - average);
            }
            while (noise[row][column] > 1) {
               noise[row][column] = 1;
            }
            while (noise[row][column] < 0) {
               noise[row][column] = 0;
            }
         }
      }
      return noise;
   }
   
   public static double[][] add(double[][] noise, double[][] noise2) {
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            noise[row][column] += noise2[row][column];
         }
      }
      return noise;
   }
   
   public static double[][] subtract(double[][] noise, double[][] noise2, boolean absolute) {
      for (int row = 0; row < noise.length; row++) {
         for (int column = 0; column < noise[1].length; column++) {
            noise[row][column] -= noise2[row][column];
            while (noise[row][column] < 0) {
               if (absolute) {
                  noise[row][column] += 1;
               }
               else {
                  noise[row][column] = 0; // changing this = 0 to a += 1 could be what makes the weird wave subtraction diamon pattern
               }
            }
         }
      }
      return noise;
   }
   
   public static double[][] multiply(double[][] noise, double[][] noise2) {
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            noise[row][column] = noise[row][column] * noise2[row][column];
         }
      }
      return noise;
   }
   
   public static double[][] divide(double[][] noise, double[][] noise2) {
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            noise[row][column] = noise[row][column] / noise2[row][column];
            while (noise[row][column] > 1) {
               noise[row][column] -= 1;
            }
         }
      }
      return noise;
   }
   
   public static double[][] remap(double[][] noise) {
      double lowest = 1;
      double highest = 0;
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            if (noise[row][column] < lowest) {
               lowest = noise[row][column];
            }
         }
      }
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            if (noise[row][column] >= lowest) {
               noise[row][column] -= lowest;
            }
            if (noise[row][column] > highest) {
               highest = noise[row][column];
            }
         }
      }
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            noise[row][column] = noise[row][column] * (1.0 / highest);
         }
      }
      return noise;
   }
   
   public static double[][] imageToMask(DrawingPanel image3) {
      double mask[][] = new double[image3.getHeight()][image3.getWidth()];
      for (int row = 0; row < image3.getHeight(); row++) {
         for (int column = 0; column < image3.getWidth(); column++) {
            Color p = image3.getPixel(column, row);
            mask[row][column] = (p.getRed() + p.getBlue() + p.getGreen()) / 3;
            mask[row][column] = mask[row][column] / 256.0;
         }
      }
      return mask;
   }
   
   public static double[][] interpolateMask(double[][] frame1, double[][] frame2) {
      double newFrame[][] = new double[frame1.length][frame1[1].length];
      ArrayList<int[]> frame1WhiteCoords = new ArrayList<int[]>();
      ArrayList<int[]> frame2WhiteCoords = new ArrayList<int[]>();
      for (int row = 0; row < newFrame.length; row++) {
         for (int column = 0; column < newFrame[1].length; column++) {
            int[] coords = new int[2];
            coords[0] = row;
            coords[1] = column;
            newFrame[row][column] = 0.0;
            if (frame1[row][column] == 1.0) {
               frame1WhiteCoords.add(coords);
            }
            if (frame2[row][column] == 1.0) {
               frame2WhiteCoords.add(coords);
            }
         }
      }
      boolean forward = true;
      double ratio = frame1WhiteCoords.size() / frame2WhiteCoords.size();
      if (ratio > 1) {
         ratio = frame2WhiteCoords.size() / frame1WhiteCoords.size();
         forward = false;
      }
      int loops = 0;
      for (int row = 0; row < newFrame.length; row++) {
         for (int column = 0; column < newFrame[1].length; column++) {
            int[] coords = new int[2];
            coords[0] = row;
            coords[1] = column;
            if (forward && frame1WhiteCoords.indexOf(coords) != -1) {
               int[] oldCoords = new int[2];
               oldCoords = frame1WhiteCoords.get(frame1WhiteCoords.indexOf(coords));
               int[] newCoords = new int[2];
               newCoords = frame2WhiteCoords.get(loops);
               loops++;
               if (loops <= frame2WhiteCoords.size()) {
                  loops = 0;
               }
               newFrame[row + ((newCoords[0] - oldCoords[0]) / 2)][column + ((newCoords[1] - oldCoords[1]) / 2)] = 1.0;
            }
         }
      }
      return newFrame;
   }
   
   public static double[][] makeSeamless(double[][] noise) {
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            //Top left, REMEMBER TO FLIP VERTICAL LINES AT INCREMENTS OF RESOLUTION OVER 4 and the horizontal in bottom left
            if (row < RESOLUTION / 2 && column < (RESOLUTION / 4)) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION / 2) - row - 1][(RESOLUTION / 2) - column];
               noise[row][column] = newValue;
               noise[(RESOLUTION / 2) - row - 1][(RESOLUTION / 2) - column] = oldValue;
            }
            //top right
            if (row < RESOLUTION / 2 && column > (3 * RESOLUTION / 4)) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION / 2) - row - 1][(RESOLUTION - column) + (RESOLUTION / 2)];
               noise[row][column] = newValue;
               noise[(RESOLUTION / 2) - row - 1][(RESOLUTION - column) + (RESOLUTION / 2)] = oldValue;
            }
            //bottom left
            if (row > (3 * RESOLUTION / 4) && column < RESOLUTION / 2) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION - row - 1) + (RESOLUTION / 2)][(RESOLUTION / 2) - column - 1];
               noise[row][column] = newValue;
               noise[(RESOLUTION - row - 1) + (RESOLUTION / 2)][(RESOLUTION / 2) - column - 1] = oldValue;
            }
            //bottom right
            if (row >= RESOLUTION / 2 && column > (3 * RESOLUTION / 4)) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION - row - 1) + (RESOLUTION / 2)][(RESOLUTION - column - 1) + (RESOLUTION / 2)];
               noise[row][column] = newValue;
               noise[(RESOLUTION - row - 1) + (RESOLUTION / 2)][(RESOLUTION - column - 1) + (RESOLUTION / 2)] = oldValue;
            }
            if (row < RESOLUTION / 4 && column == RESOLUTION / 4) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION / 2) - row][column];
               noise[row][column] = newValue;
               noise[(RESOLUTION / 2) - row][column] = oldValue;
            }
            if (row < RESOLUTION / 4 && column == 3 * RESOLUTION / 4) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION / 2) - row][column];
               noise[row][column] = newValue;
               noise[(RESOLUTION / 2) - row][column] = oldValue;
            }
            if (row > (3 * RESOLUTION / 4) && (column == 3 * RESOLUTION / 4 || column == (3 * RESOLUTION / 4) - 1)) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION) - row + (RESOLUTION / 2)][column];
               noise[row][column] = newValue;
               noise[(RESOLUTION) - row + (RESOLUTION / 2)][column] = oldValue;
            }
            if ((row == (3 * RESOLUTION / 4) - 1 || row == (3 * RESOLUTION / 4)) && column < RESOLUTION / 4) {
               double oldValue = noise[row][column];
               double newValue = noise[row][(RESOLUTION / 2) - column];
               noise[row][column] = newValue;
               noise[row][(RESOLUTION / 2) - column] = oldValue;
            }
         }
      }
      /*for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            if ((row < RESOLUTION / 2) && (column < RESOLUTION / 2)) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION) - (row + 1)][(RESOLUTION) - (column + 1)];
               noise[row][column] = newValue;
               noise[(RESOLUTION) - (row + 1)][(RESOLUTION) - (column + 1)] = oldValue;
            }
            if ((row > RESOLUTION / 2) && (column < RESOLUTION / 2)) {
               double oldValue = noise[row][column];
               double newValue = noise[(RESOLUTION / 2) - ((row - 1) - (RESOLUTION / 2))][(column - 1) + (RESOLUTION / 2)];
               noise[row][column] = newValue;
               noise[(RESOLUTION / 2) - ((row + 1) - (RESOLUTION / 2))][(column) + (RESOLUTION / 2)] = oldValue;
            }
         }
      } // othermethod
      double[][] topLeft = new double[(RESOLUTION/2)][(RESOLUTION/2)];
      double[][] topRight = new double[(RESOLUTION/2)][(RESOLUTION/2)];
      double[][] bottomLeft = new double[(RESOLUTION/2)][(RESOLUTION/2)];
      double[][] bottomRight = new double[(RESOLUTION/2)][(RESOLUTION/2)];
      for (int row = 0; row < RESOLUTION / 2; row++) {
         for (int column = 0; column < RESOLUTION / 2; column++) {
            topLeft[row][column] = noise[row][column];
         }
      }
      for (int row = RESOLUTION / 2; row < RESOLUTION; row++) {
         for (int column = RESOLUTION / 2; column < RESOLUTION; column++) {
            bottomRight[row - (RESOLUTION / 2)][column - (RESOLUTION / 2)] = noise[row][column];
         }
      }
      for (int row = 0; row < RESOLUTION / 2; row++) {
         for (int column = RESOLUTION / 2; column < RESOLUTION; column++) {
            topRight[row][column - (RESOLUTION / 2)] = noise[row][column];
         }
      }
      for (int row = RESOLUTION / 2; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION / 2; column++) {
            bottomLeft[row - (RESOLUTION / 2)][column] = noise[row][column];
         }
      }
      // Below is swap
      for (int row = 0; row < RESOLUTION / 2; row++) {
         for (int column = 0; column < RESOLUTION / 2; column++) {
            noise[row][column] = bottomRight[row][column];
         }
      }
      for (int row = RESOLUTION / 2; row < RESOLUTION; row++) {
         for (int column = RESOLUTION / 2; column < RESOLUTION; column++) {
            noise[row][column] = topLeft[row - (RESOLUTION / 2)][column - (RESOLUTION / 2)];
         }
      }
      for (int row = 0; row < RESOLUTION / 2; row++) {
         for (int column = RESOLUTION / 2; column < RESOLUTION; column++) {
            noise[row][column] = bottomLeft[row][column - (RESOLUTION / 2)];
         }
      }
      for (int row = RESOLUTION / 2; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION / 2; column++) {
            noise[row][column] = topLeft[row - (RESOLUTION / 2)][column];
         }
      }*/
      return noise;
   }
   
   public static void drawFour(double[][] noise) {
      DrawingPanel panel2 = new DrawingPanel(RESOLUTION * 2, RESOLUTION * 2);
      for (int row = 0; row < RESOLUTION * 2; row++) {
         for (int column = 0; column < RESOLUTION * 2; column++) {
            Color c = new Color(((float) noise[row % RESOLUTION][column % RESOLUTION]), ((float)noise[row % RESOLUTION][column % RESOLUTION]), ((float)noise[row % RESOLUTION][column % RESOLUTION]), ((float)1));
            panel2.setPixel(column, row, c);
         }
      }
   }
   
   public static double[][] exclusiveSmooth(double[][] noise, int margin, int border) {
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            if ((row < (RESOLUTION / 2) + border && row > (RESOLUTION / 2) - border) || (column < (RESOLUTION / 2) + border && column > (RESOLUTION / 2) - border)) {
               double average = 0;
               int iterations = 0;
               for (int row2 = 0 - (margin / 2) + (row - (RESOLUTION - border)); row2 < (margin / 2); row2++) {
                  for (int column2 = 0 - (margin / 2) + (column - (RESOLUTION - border)); column2 < (margin / 2); column2++) {
                     if (row + row2 >= 0 && column + column2 >= 0) {
                        average += noise[(row + row2) % RESOLUTION][(column + column2) % RESOLUTION];
                        iterations++;
                     }
                  }
               }
               average = average / iterations;
               if (noise[row][column] < average) {
                  noise[row][column] += (average - noise[row][column]);
               }
               if (noise[row][column] > average) {
                  noise[row][column] -= (noise[row][column] - average);
               }
               while (noise[row][column] > 1) {
                  noise[row][column] = 1;
               }
               while (noise[row][column] < 0) {
                  noise[row][column] = 0;
               }
            }
         }
      }
      return noise;
   }
   
   public static double[][] makeTrueSeamless(double[][]noise, int margin) {
      double[][] nineNoise = new double[RESOLUTION * 3][RESOLUTION * 3];
      for (int row = 0; row < RESOLUTION * 3; row++) {
         for (int column = 0; column < RESOLUTION * 3; column++) {
            nineNoise[row][column] = noise[row % RESOLUTION][column % RESOLUTION];
         }
      }
      for (int row = 0; row < RESOLUTION * 3; row++) {
         for (int column = 0; column < RESOLUTION * 3; column++) {
            double average = 0;
            int iterations = 0;
            for (int row2 = 0 - (margin / 2); row2 < (margin / 2); row2++) {
               for (int column2 = 0 - (margin / 2); column2 < (margin / 2); column2++) {
                  if (row + row2 >= 0 && column + column2 >= 0) {
                     average += nineNoise[(row + row2) % RESOLUTION][(column + column2) % RESOLUTION];
                     iterations++;
                  }
               }
            }
            average = average / iterations;
            if (nineNoise[row][column] < average) {
               nineNoise[row][column] += (average - nineNoise[row][column]);
            }
            if (nineNoise[row][column] > average) {
               nineNoise[row][column] -= (nineNoise[row][column] - average);
            }
            while (nineNoise[row][column] > 1) {
               nineNoise[row][column] = 1;
            }
            while (nineNoise[row][column] < 0) {
               nineNoise[row][column] = 0;
            }
         }
      }
      for (int row = 0; row < RESOLUTION; row++) {
         for (int column = 0; column < RESOLUTION; column++) {
            noise[row][column] = nineNoise[row + RESOLUTION][column + RESOLUTION];
         }
      }
      return noise;
   }
   
}