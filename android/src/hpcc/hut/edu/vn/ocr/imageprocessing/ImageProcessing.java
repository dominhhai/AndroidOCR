package hpcc.hut.edu.vn.ocr.imageprocessing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;

public class ImageProcessing {
	// private static Bitmap preProcessing(Bitmap input) {
	// return sauvolaAlgorithm(input, 0.5, 15);
	// }

	public static Bitmap sauvolaAlgorithm(Bitmap input, double k, int wsite) {
		int height = input.getHeight();
		int width = input.getWidth();
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int off = 0xFF000000;
		int on = 0xFFFFFFFF;

		int whalf = wsite >> 1; // Half of window size

		// tinh toan truoc cac gia tri tong , tong binh phuong
		// cua hinh chu nhat co trai - tren la (0,0), phai - duoi la (x,y)
		long[][] integral_image, integral_sqimg;
		integral_image = new long[width][height];
		integral_sqimg = new long[width][height];

		for (int j = 0; j < height; j++) {
			integral_image[0][j] = input.getPixel(0, j);
			integral_sqimg[0][j] = input.getPixel(0, j) * input.getPixel(0, j);
		}

		for (int i = 1; i < width; i++) {
			for (int j = 0; j < height; j++) {
				integral_image[i][j] = integral_image[i - 1][j]
						+ input.getPixel(i, j);
				integral_sqimg[i][j] = integral_sqimg[i - 1][j]
						+ input.getPixel(i, j) * input.getPixel(i, j);
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
				output.setPixel(i, j, (input.getPixel(i, j) < threshold) ? off
						: on);
			}
		}

		return output;
	}

	public static final byte[] bitmapToJpegImageData(Bitmap bitmap) {
		byte[] output = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, baos);
		output = baos.toByteArray();
		try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	// Deflater
	public static final byte[] compress(byte input[]) {
		// System.out.println ("size input: " + input.length);
		Deflater def = new Deflater();
		def.setInput(input);
		def.finish();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte buf[] = new byte[1024];
		while (!def.finished()) {
			int byteCount = def.deflate(buf);
			os.write(buf, 0, byteCount);
		}
		def.end();
		byte[] output = os.toByteArray();
		return output;
	}

	// 8bit -> 1 byte
	public static final byte[] combineByte(byte orgByte[]) {
		int length = orgByte.length;
		byte[] combinedByte = new byte[length / 8 + 1];
		for (int i = 0; i < length; i++) {
			int j = i / 8;
			int k = i % 8;
			if (orgByte[i] == 0)
				combinedByte[j] = (byte) (combinedByte[j] & ~(1 << k));
			else
				combinedByte[j] = (byte) (combinedByte[j] | (1 << k));
		}
		return combinedByte;
	}
}
