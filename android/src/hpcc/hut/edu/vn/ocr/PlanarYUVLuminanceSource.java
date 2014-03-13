/*
 * Copyright (C) 2008 ZXing authors
 * Copyright 2011 Robert Theis
 * Copyright 2012 Hai Do Minh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hpcc.hut.edu.vn.ocr;

import hpcc.hut.edu.vn.ocr.imageprocessing.JpegImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

/**
 * This object extends LuminanceSource around an array of YUV data returned from
 * the camera driver, with the option to crop to a rectangle within the full
 * data. This can be used to exclude superfluous pixels around the perimeter and
 * speed up decoding.
 * 
 * It works for any pixel format where the Y channel is planar and appears
 * first, including YCbCr_420_SP and YCbCr_422_SP.
 * 
 * The code for this class was adapted from the ZXing project:
 * http://code.google.com/p/zxing
 */
public final class PlanarYUVLuminanceSource {

	private final byte[] yuvData;
	private final int dataWidth;
	private final int dataHeight;
	private final int width;
	private final int height;
	private final int left;
	private final int top;

	public PlanarYUVLuminanceSource(byte[] yuvData, int dataWidth,
			int dataHeight, int left, int top, int width, int height) {

		if (left + width > dataWidth || top + height > dataHeight) {
			throw new IllegalArgumentException(
					"Crop rectangle does not fit within image data.");
		}

		this.yuvData = yuvData;
		this.dataWidth = dataWidth;
		this.dataHeight = dataHeight;
		this.width = width;
		this.height = height;
		this.left = left;
		this.top = top;
	}

	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= height) {
			throw new IllegalArgumentException(
					"Requested row is outside the image: " + y);
		}
		if (row == null || row.length < width) {
			row = new byte[width];
		}
		int offset = (y + top) * dataWidth + left;
		System.arraycopy(yuvData, offset, row, 0, width);
		return row;
	}

	public byte[] getMatrix() {

		// If the caller asks for the entire underlying image, save the copy and
		// give them the
		// original data. The docs specifically warn that result.length must be
		// ignored.
		if (width == dataWidth && height == dataHeight) {
			return yuvData;
		}

		int area = width * height;
		byte[] matrix = new byte[area];
		int inputOffset = top * dataWidth + left;

		// If the width matches the full width of the underlying data, perform a
		// single copy.
		if (width == dataWidth) {
			System.arraycopy(yuvData, inputOffset, matrix, 0, area);
			return matrix;
		}

		// Otherwise copy one cropped row at a time.
		byte[] yuv = yuvData;
		for (int y = 0; y < height; y++) {
			int outputOffset = y * width;
			System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
			inputOffset += dataWidth;
		}
		return matrix;
	}

	public boolean isCropSupported() {
		return true;
	}

	public boolean isRotateSupported() {
		return false;
	}

	public PlanarYUVLuminanceSource crop(int left, int top, int width,
			int height) {
		return new PlanarYUVLuminanceSource(yuvData, dataWidth, dataHeight,
				this.left + left, this.top + top, width, height);
	}

	public Bitmap renderCroppedGreyscaleBitmap() {
		int[] pixels = new int[width * height];
		byte[] yuv = yuvData;
		int inputOffset = top * dataWidth + left;

		for (int y = 0; y < height; y++) {
			int outputOffset = y * width;
			for (int x = 0; x < width; x++) {
				int grey = yuv[inputOffset + x] & 0xff;
				pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
			}
			inputOffset += dataWidth;
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	public JpegImage getCroppedJpegImage() {
		return new JpegImage(this.getCroppedJpegImageData(), width, height);
	}

	public byte[] getCroppedJpegImageData() {
		YuvImage yuvImage = new YuvImage(this.getMatrix(), ImageFormat.NV21,
				width, height, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
		byte[] jdata = baos.toByteArray();

		// System.out.println ("imgw: " + width + ", imgh: " + height);
		// System.out.println ("baos leng: " + baos.size() + ", jdata size: " +
		// jdata.length);
		// Bitmap bitmap = BitmapFactory.decodeByteArray( baos.toByteArray(), 0,
		// baos.size());
		// System.out.println ("bitw: " + bitmap.getWidth() + ", bith: " +
		// bitmap.getHeight());
		try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jdata;
	}

	public byte[] getYuvData() {
		return yuvData;
	}

	public int getDataWidth() {
		return dataWidth;
	}

	public int getDataHeight() {
		return dataHeight;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLeft() {
		return left;
	}

	public int getTop() {
		return top;
	}

}
