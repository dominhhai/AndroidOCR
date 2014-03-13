/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package preprocessing;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NodeList;

/**
 *
 * @author minhhai
 */
public class ImageHelper {

    public static int[][] convert1to2(int[] input, int width, int height) {
        int[][] output = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int k = i * height + j;
                output[i][j] = input[k];
            }
        }
        return output;
    }

    public static int[] conver2to1(int[][] input) {
        int width = input.length;
        int height = input[0].length;
        int[] output = new int[width * height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int k = i * height + j;
                output[k] = input[i][j];
            }
        }
        return output;
    }

    public static int[] bytetoint(byte[] input) {
        try {
            BufferedImage buf = ImageIO.read(new ByteArrayInputStream(input));
            return buf.getRGB(0, 0, buf.getWidth(), buf.getHeight(), null, 0, buf.getWidth());
        } catch (IOException ex) {
            Logger.getLogger(ImageHelper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static byte[] intotbyte(int[] input, int width, int height) {
        BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        buf.setRGB(0, 0, width, height, input, 0, width);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(buf, "png", baos);
            baos.flush();
            byte[] output = baos.toByteArray();
            baos.close();
            return output;
        } catch (IOException ex) {
            Logger.getLogger(ImageHelper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static void writeImageInt(int[][] input, String file) {
        int width = input.length;
        int height = input[0].length;
        int[] pixels = ImageHelper.conver2to1(input);
//        System.out.println("width: " + width);
//        System.out.println("height: " + height);
//        System.out.println("length: " + pixels.length);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        try {
            // retrieve image            
            File outputfile = new File(file);
            ImageIO.write(image, "png", outputfile);
//            System.out.println ("Write Image done: " + file);

            Logger.getLogger(ImageHelper.class.getName()).log(Level.SEVERE, "Write Image done: " + file);
        } catch (IOException ex) {
            Logger.getLogger(ImageHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static byte[] readImage(String part) {
        byte[] image = null;
        try {
            BufferedImage buf = ImageIO.read(new File(part));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(buf, "png", baos);
            baos.flush();
            image = baos.toByteArray();
            baos.close();
        } catch (IOException ex) {
            Logger.getLogger(ImageHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    public static int[][] readImageInt(String part) {
        int[][] image = null;
        try {
            BufferedImage buf = ImageIO.read(new File(part));
            int[] bufInt = buf.getRGB(0, 0, buf.getWidth(), buf.getHeight(), null, 0, buf.getWidth());
            image = ImageHelper.convert1to2(bufInt, buf.getWidth(), buf.getHeight());
        } catch (IOException ex) {
            Logger.getLogger(ImageHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    public static Map<String, String> readImageData(IIOMetadata imageMetadata) {
        Map<String, String> dict = new HashMap<String, String>();
        if (imageMetadata != null) {
            IIOMetadataNode dimNode = (IIOMetadataNode) imageMetadata.getAsTree("javax_imageio_1.0");
            NodeList nodes = dimNode.getElementsByTagName("HorizontalPixelSize");
            int dpiX;
            if (nodes.getLength() > 0) {
                float dpcWidth = Float.parseFloat(nodes.item(0).getAttributes().item(0).getNodeValue());
                dpiX = (int) Math.round(25.4f / dpcWidth);
            } else {
                dpiX = Toolkit.getDefaultToolkit().getScreenResolution();
            }
            dict.put("dpiX", String.valueOf(dpiX));

            nodes = dimNode.getElementsByTagName("VerticalPixelSize");
            int dpiY;
            if (nodes.getLength() > 0) {
                float dpcHeight = Float.parseFloat(nodes.item(0).getAttributes().item(0).getNodeValue());
                dpiY = (int) Math.round(25.4f / dpcHeight);
            } else {
                dpiY = Toolkit.getDefaultToolkit().getScreenResolution();
            }
            dict.put("dpiY", String.valueOf(dpiY));
        }

        return dict;
    }

    public static void sort(int[] array) {
        Arrays.sort(array);
    }

    public static int getMedian(int[] array) {
        int middle;
        if (array.length == 3) {
            int a = array[0];
            int b = array[1];
            int c = array[2];
            if ((a <= b) && (a <= c)) {
                middle = (b <= c) ? b : c;
            } else if ((b <= a) && (b <= c)) {
                middle = (a <= c) ? a : c;
            } else {
                middle = (a <= b) ? a : b;
            }
        } else {
            ImageHelper.sort(array);
            middle = array[array.length >> 1];
        }
        return middle;
    }

    public static int getAlpha(int color) {
        return ((color >> 24) & 0xFF);
    }

    public static int getRed(int color) {
        return ((color >> 16) & 0xFF);
    }

    public static int getGreen(int color) {
        return ((color >> 8) & 0xFF);
    }

    public static int getBlue(int color) {
        return (color & 0xFF);
    }

    public static int rgbToColor(int r, int g, int b) {
        return argbToColor(255, r, g, b);
    }

    public static int argbToColor(int a, int r, int g, int b) {
        return (((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public static int[][] to32BitImage(final int[][] source, int width, int height) {
        int[][] output = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = source[i][j];
                output[i][j] = rgbToColor(color, color, color);
            }
        }
        return output;
    }

    public static int[][] to8BitImage(final int[][] source, int width, int height) {
        int[][] output = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int color = source[i][j];
                int red = getRed(color);
                int green = getGreen(color);
                int blue = getBlue(color);
                output[i][j] = ((int) Math.round(0.2989 * red + 0.587 * green + 0.114 * blue));
            }
        }
        return output;
    }
}
