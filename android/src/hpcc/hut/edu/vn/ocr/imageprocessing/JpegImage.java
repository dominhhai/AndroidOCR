package hpcc.hut.edu.vn.ocr.imageprocessing;

public class JpegImage {

	private byte[] jdata;
	private int width;
	private int height;

	public byte[] getJdata() {
		return jdata;
	}

	public void setJdata(byte[] jdata) {
		this.jdata = jdata;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public JpegImage() {
	}

	public JpegImage(byte[] jdata, int width, int height) {
		this.jdata = jdata;
		this.width = width;
		this.height = height;
	}

}
