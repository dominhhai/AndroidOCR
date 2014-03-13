/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package enginewapper;

import postprocessing.Processor;
import preprocessing.AbsImagePreprocessing;
import preprocessing.ImageHelper;
import preprocessing.ImagePreprocessing;

/**
 *
 * @author dominhhai
 */
public class TessLeptWapper {

    public static final class PageSegMode {

        /**
         * Orientation and script detection only.
         */
        public static final int PSM_OSD_ONLY = 0;
        /**
         * Automatic page segmentation with orientation and script detection.
         * (OSD)
         */
        public static final int PSM_AUTO_OSD = 1;
        /**
         * Fully automatic page segmentation, but no OSD, or OCR.
         */
        public static final int PSM_AUTO_ONLY = 2;
        /**
         * Fully automatic page segmentation, but no OSD.
         */
        public static final int PSM_AUTO = 3;
        /**
         * Assume a single column of text of variable sizes.
         */
        public static final int PSM_SINGLE_COLUMN = 4;
        /**
         * Assume a single uniform block of vertically aligned text.
         */
        public static final int PSM_SINGLE_BLOCK_VERT_TEXT = 5;
        /**
         * Assume a single uniform block of text. (Default.)
         */
        public static final int PSM_SINGLE_BLOCK = 6;
        /**
         * Treat the image as a single text line.
         */
        public static final int PSM_SINGLE_LINE = 7;
        /**
         * Treat the image as a single word.
         */
        public static final int PSM_SINGLE_WORD = 8;
        /**
         * Treat the image as a single word in a circle.
         */
        public static final int PSM_CIRCLE_WORD = 9;
        /**
         * Treat the image as a single character.
         */
        public static final int PSM_SINGLE_CHAR = 10;
        /**
         * Find as much text as possible in no particular order.
         */
        public static final int PSM_SPARSE_TEXT = 11;
        /**
         * Sparse text with orientation and script detection.
         */
        public static final int PSM_SPARSE_TEXT_OSD = 12;
        /**
         * Number of enum entries.
         */
        public static final int PSM_COUNT = 13;
    }
    /**
     * Whitelist of characters to recognize.
     */
    public static final String VAR_CHAR_WHITELIST = "tessedit_char_whitelist";
    /**
     * Blacklist of characters to not recognize.
     */
    public static final String VAR_CHAR_BLACKLIST = "tessedit_char_blacklist";
    /**
     * Run Tesseract only - fastest
     */
    public static final int OEM_TESSERACT_ONLY = 0;
    /**
     * Run Cube only - better accuracy, but slower
     */
    public static final int OEM_CUBE_ONLY = 1;
    /**
     * Run both and combine results - best accuracy
     */
    public static final int OEM_TESSERACT_CUBE_COMBINED = 2;
    /**
     * Default OCR engine mode.
     */
    public static final int OEM_DEFAULT = 3;
    /**
     * Default datapath: "/usr/local/share/"
     */
    public static final String DATA_PATH = "tesseract";

    /**
     * Elements of the page hierarchy, used in {@link ResultIterator} to provide
     * functions that operate on each level without having to have 5x as many
     * functions.
     * <p>
     * NOTE: At present {@link #RIL_PARA} and {@link #RIL_BLOCK} are equivalent
     * as there is no paragraph internally yet.
     */
    public static final class PageIteratorLevel {

        /**
         * Block of text/image/separator line.
         */
        public static final int RIL_BLOCK = 0;
        /**
         * Paragraph within a block.
         */
        public static final int RIL_PARA = 1;
        /**
         * Line within a paragraph.
         */
        public static final int RIL_TEXTLINE = 2;
        /**
         * Word within a text line.
         */
        public static final int RIL_WORD = 3;
        /**
         * Symbol/character within a word.
         */
        public static final int RIL_SYMBOL = 4;
    };

    private TessLeptWapper() {
        try {
            //System.out.println("Begin load libtessleftwapper.so");
            System.load("/usr/local/lib/libtessleftwapper.so");
            //System.out.println("End load libtessleftwapper.so");
        } catch (Exception ex) {
            System.err.println("cant load lib libtessleftwapper");
            ex.printStackTrace();
        }
    }

    public native String processOCR(byte[] image, String language,
            String datapath, int ocrEngineMode,
            boolean isCallTesseractOnly);
    private static TessLeptWapper instance;

    public static TessLeptWapper getInstance() {
        if (instance == null) {
            instance = new TessLeptWapper();
        }
        return instance;
    }

    public static void freeInstance() {
        instance = null;
    }

    public static void main(String[] args) {
        TessLeptWapper test = TessLeptWapper.getInstance();

        final String imagePath = "/home/dominhhai/HaiDM-GR/test_data/";
        final String imageName = "phototest-55.png";
        final String lang = "eng";
        /**
         * pre processing
         */
//        byte[] output = ImageHelper.readImage(imagePath + imageName);
        boolean isPre = true;
        byte[] output;
        if (isPre) {
            int[][] image = ImageHelper.readImageInt(imagePath + imageName);
            int width = image.length;
            int height = image[0].length;
            int[] input = ImageHelper.conver2to1(image);

            AbsImagePreprocessing imgprocess = new ImagePreprocessing();

//        imgprocess.setDebug(true);
//        imgprocess.setFileName(imageName);

            imgprocess.setInputImage(input, width, height);
            imgprocess.process();
            output = imgprocess.getOutputImage();
        } else {
            output = ImageHelper.readImage(imagePath + imageName);
        }
        // DATA_PATH = "/usr/local/share/"
        /**
         * call engine
         */
        String result = test.processOCR(output, lang, DATA_PATH, OEM_DEFAULT, !isPre);

        /**
         * post processing
         */
        char prefix = '0';
        if (result.length() > 0) {
            prefix = result.charAt(0);
        }
        if (result.length() > 1) {
            result = result.substring(1);
        } else {
            result = "";
        }

        if (prefix == '0') {
            System.out.println("********************result********************");
            System.err.println("OCR Error!");
            System.out.println("--------------------result--------------------");
            return;
        }

        /**
         * print out before post-pro result
         */
        System.out.println("********************before-result : " + result.length() + "********************");
        System.out.println(result);
        System.out.println("--------------------before-result--------------------");

        if (isPre) {
            result = result.trim();
            int i = 0;
            while (i < result.length() && isRemoveable(result.charAt(i))) {
                i++; // delete all space, newline, tab in the head of string
            }
            if (i > 0) {
                result = result.substring(i - 1);
            }
            // call post-processing
            try {
                // TODO call post-processing
                if (result.length() > 1) {
                    result = Processor.postProcess(result, lang, "dangAmbigsPath", false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            /**
             * print out result
             */
            System.out.println("********************result : " + result.length() + "********************");
            System.out.println(result);
            System.out.println("--------------------result--------------------");
        }
    }

    private static boolean isRemoveable(char c) {
        return ((c == ' ') || (c == '\n') || (c == '\t'));
    }
}
