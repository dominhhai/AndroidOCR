/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package preprocessing;

/**
 *
 * @author minhhai
 */
public abstract class AbsImagePreprocessing {

    public static final double BT709[] = new double[]{0.2125, 0.7154, 0.0721};
    public static final double RMY[] = new double[]{0.5, 0.419, 0.081};
    public static final double YIQ[] = new double[]{0.299, 0.587, 0.114};
    private int[] _InputImage;
    protected int width;
    protected int height;
    private byte[] _OutputImage;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    private boolean debug = false;

    // firstly set an image
    public void setInputImage(int[] input, int width, int height) {
        this._InputImage = input;
        this.width = width;
        this.height = height;
    }

    // call after calling process method
    public byte[] getOutputImage() {
        return this._OutputImage;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

    // implement pre-processing
    public void process() {
//        long startTime = System.currentTimeMillis();
//        System.out.println("Begin processing: " + startTime);

        int[][] tmpBuff = ImageHelper.convert1to2(_InputImage, width, height);
        if (this.debug) {
            ImageHelper.writeImageInt(tmpBuff, fileName + "-ori.png");
        }

        tmpBuff = this.RGBtoGreyScale(tmpBuff);
        if (this.debug) {
            ImageHelper.writeImageInt(ImageHelper.to32BitImage(tmpBuff, width, height), fileName + "-grey.png");
        }
        tmpBuff = this.sauvolaBinarizationAlgorithm(tmpBuff, 0.2, 15);
        if (this.debug) {
            ImageHelper.writeImageInt(ImageHelper.to32BitImage(tmpBuff, width, height), fileName + "-binary.png");
        }
        tmpBuff = this.medianFilter(tmpBuff, 3);
        if (this.debug) {
            ImageHelper.writeImageInt(ImageHelper.to32BitImage(tmpBuff, width, height), fileName + "-filter.png");
        }
        tmpBuff = this.increaseDPI(ImageHelper.to32BitImage(tmpBuff, width, height));
        if (this.debug) {
            ImageHelper.writeImageInt(ImageHelper.to32BitImage(tmpBuff, width, height), fileName + "-dpi.png");
        }
        this._OutputImage = ImageHelper.intotbyte(ImageHelper.conver2to1(ImageHelper.to32BitImage(tmpBuff, width, height)), width, height);

//        long endTime = System.currentTimeMillis();
//        System.out.println("End processing: " + endTime);
//        System.out.println("Total time: " + (endTime - startTime));
    }

    public abstract int[][] RGBtoGreyScale(final int[][] input);

    public abstract int[][] sauvolaBinarizationAlgorithm(final int[][] input, final double k, final int wsite);

    public abstract int[][] medianFilter(final int[][] input, final int wsize);

    public abstract int[][] increaseDPI(final int[][] input);
}
