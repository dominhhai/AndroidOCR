/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package preprocessing;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author minhhai
 */
public class ImagePreprocessing extends AbsImagePreprocessing {

    @Override
    public int[][] RGBtoGreyScale(final int[][] input) {
//        int[][] gray_image = new int[super.width][super.height];
//        for (int i = 0; i < super.width; i++) {
//            for (int j = 0; j < super.height; j++) {
//                Color color = new Color(input[i][j]);
//                int red = color.getRed();
//                int green = color.getGreen();
//                int blue = color.getBlue();
//                
////                int greyscale = ((int) Math.round(0.2989 * red + 0.587 * green + 0.114 * blue));
////                greyscale = greyscale & 0xFF;
////
////                gray_image[i][j] = ImageHelper.rgbToColor(greyscale, greyscale, greyscale);
//                gray_image[i][j] = ((int) Math.round(0.2989 * red + 0.587 * green + 0.114 * blue));
//            }
//        }
//        return gray_image;
        return ImageHelper.to8BitImage(input, super.width, super.height);
    }

    @Override
    public int[][] sauvolaBinarizationAlgorithm(final int[][] input, final double k, final int wsite) {
        int[][] output = new int[super.width][super.height];
        int off = 0x00;
        int on = 0xFF;

        int whalf = wsite >> 1; // Half of window size

        // tinh toan truoc cac gia tri tong , tong binh phuong
        // cua hinh chu nhat co trai - tren la (0,0), phai - duoi la (x,y)
        long[][] integral_image, integral_sqimg;
        integral_image = new long[super.width][super.height];
        integral_sqimg = new long[super.width][super.height];

        for (int j = 0; j < height; j++) {
            integral_image[0][j] = input[0][j];
            integral_sqimg[0][j] = input[0][j] * input[0][j];
        }

        for (int i = 1; i < width; i++) {
            for (int j = 0; j < height; j++) {
                integral_image[i][j] = integral_image[i - 1][j]
                        + input[i][j];
                integral_sqimg[i][j] = integral_sqimg[i - 1][j]
                        + input[i][j] * input[i][j];
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 1; j < height; j++) {
                integral_image[i][j] = integral_image[i][j - 1]
                        + integral_image[i][j];
                integral_sqimg[i][j] = integral_sqimg[i][j - 1]
                        + integral_sqimg[i][j];
            }
        }

        // tinh toan tim nguong cho tung diem anh

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int xmin = Math.max(0, i - whalf);
                int ymin = Math.max(0, j - whalf);
                int xmax = Math.min(width - 1, i + whalf);
                int ymax = Math.min(height - 1, j + whalf);
                int area = (xmax - xmin + 1) * (ymax - ymin + 1);

                long diff, sqdiff;

                if ((xmin == 0) && (ymin == 0)) { // diem goc (0,0)
                    diff = integral_image[xmax][ymax];
                    sqdiff = integral_sqimg[xmax][ymax];
                } else if ((xmin == 0) && (ymin > 0)) { // cot dau tien
                    diff = integral_image[xmax][ymax]
                            - integral_image[xmax][ymin - 1];
                    sqdiff = integral_sqimg[xmax][ymax]
                            - integral_sqimg[xmax][ymin - 1];
                } else if ((xmin > 0) && (ymin == 0)) { // dong dau tien
                    diff = integral_image[xmax][ymax]
                            - integral_image[xmin - 1][ymax];
                    sqdiff = integral_sqimg[xmax][ymax]
                            - integral_sqimg[xmin - 1][ymax];
                } else { // con lai
                    long diagsum = integral_image[xmax][ymax]
                            + integral_image[xmin - 1][ymin - 1];
                    long idiagsum = integral_image[xmax][ymin - 1]
                            + integral_image[xmin - 1][ymax];
                    diff = diagsum - idiagsum;
                    long sqdiagsum = integral_sqimg[xmax][ymax]
                            + integral_sqimg[xmin - 1][ymin - 1];
                    long sqidiagsum = integral_sqimg[xmax][ymin - 1]
                            + integral_sqimg[xmin - 1][ymax];
                    sqdiff = sqdiagsum - sqidiagsum;
                }

                double mean = (double) diff / area;
                double std = Math.sqrt((sqdiff - (double) diff * diff / area)
                        / (area - 1));
                int threshold = (int) (mean * (1 + k * ((std / 128) - 1)));
                output[i][j] = (input[i][j] < threshold) ? off : on;
            }
        }

        return output;
    }

    @Override
    public int[][] medianFilter(final int[][] input, final int wsize) {
        int edge = wsize >> 1;
        int[][] output = new int[super.width][super.height];
        int[] colorArray = new int[wsize * wsize];

        int count;

        for (int x = edge; x < super.width - edge; x++) {
            for (int y = edge; y < super.height - edge; y++) {
                count = 0;
                for (int fx = 0; fx < wsize; fx++) {
                    for (int fy = 0; fy < wsize; fy++) {
                        colorArray[count++] = input[x + fx - edge][y + fy - edge];
                    }
                }
                output[x][y] = ImageHelper.getMedian(colorArray);
            }
        }

        return input;
    }

    @Override
    public int[][] increaseDPI(final int[][] input) {
        byte[] imageInput = ImageHelper.intotbyte(ImageHelper.conver2to1(input), super.width, super.height);
        byte[] imageOutput;
        int DPI = 300;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageInput);
            BufferedImage buf = ImageIO.read(bais);
            InputStream is = new ByteArrayInputStream(imageInput);
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            Iterator it = ImageIO.getImageReaders(iis);
            if (!it.hasNext()) {
                return null;
            }
            ImageReader reader = (ImageReader) it.next();
            reader.setInput(iis);
            IIOMetadata meta = reader.getImageMetadata(0);

            Map<String, String> metadata = ImageHelper.readImageData(meta);
            float dpiX = Float.parseFloat(metadata.get("dpiX"));

            if (dpiX < DPI) {
                float scale = DPI / dpiX;
                int wsize = (int) (buf.getWidth() * scale);
                int hsize = (int) (buf.getHeight() * scale);
                int type = (buf.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
                BufferedImage tmp = new BufferedImage(wsize, hsize, type);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(buf, 0, 0, wsize, hsize, null);
                g2.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(tmp, "png", baos);
                baos.flush();
                imageOutput = imageInput;//baos.toByteArray();
                baos.close();
            } else {
                imageOutput = imageInput;
            }
            return ImageHelper.convert1to2(ImageHelper.bytetoint(imageOutput), super.width, super.height);
        } catch (Exception ex) {
            Logger.getLogger(ImagePreprocessing.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
