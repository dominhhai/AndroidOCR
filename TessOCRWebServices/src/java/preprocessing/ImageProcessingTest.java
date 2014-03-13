/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package preprocessing;

/**
 *
 * @author dominhhai
 */
public class ImageProcessingTest {


    public static void main(String[] args) {
        final String part = "/Users/minhhai/Desktop/";
        final String name = "bluredimage1.jpg";
        int[][] image = ImageHelper.readImageInt(part + name);
        int width = image.length;
        int height = image[0].length;
        int[] input = ImageHelper.conver2to1(image);
        
        AbsImagePreprocessing imgprocess = new ImagePreprocessing ();
 
        imgprocess.setFileName(name); 
        imgprocess.setDebug(true);
        imgprocess.setInputImage(input, width, height);
        imgprocess.process();
        byte[] output = imgprocess.getOutputImage();
//        int n = 8;
//        System.out.println("2^" + n + ":= " + (2 << (n - 1)));
    }
}
