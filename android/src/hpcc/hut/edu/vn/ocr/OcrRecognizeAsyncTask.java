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

import hpcc.hut.edu.vn.ocr.imageprocessing.ImageProcessing;
import hpcc.hut.edu.vn.ocr.ocrserviceconnector.HpccOcrServiceConnector;
import hpcc.hut.edu.vn.ocr.ocrserviceconnector.MessageTypes;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Class to send OCR requests to the OCR engine in a separate thread, send a
 * success/failure message, and dismiss the indeterminate progress dialog box.
 * Used for non-continuous mode OCR only.
 */
final class OcrRecognizeAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private CaptureActivity activity;
	private TessBaseAPI baseApi;
	private byte[] data;
	private Bitmap dataBM;
	private int width;
	private int height;
	private OcrResult ocrResult = null;

	OcrRecognizeAsyncTask(CaptureActivity activity, TessBaseAPI baseApi,
			byte[] data, int width, int height) {
		this.activity = activity;
		this.baseApi = baseApi;
		this.data = data;
		this.dataBM = null;
		this.width = width;
		this.height = height;
	}

	OcrRecognizeAsyncTask(CaptureActivity activity, TessBaseAPI baseApi,
			Bitmap data) {
		this.activity = activity;
		this.baseApi = baseApi;
		this.dataBM = data;
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		ocrResult = OcrRecognizeAsyncTask.getOcrResult(
				activity,
				baseApi,
				this.dataBM,
				activity.getCameraManager().buildLuminanceSource(data, width,
						height));
		return (ocrResult != null);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		Handler handler = activity.getHandler();
		if (handler != null) {
			// Send results for single-shot mode recognition.
			Message message = Message
					.obtain(handler, result ? R.id.ocr_decode_succeeded
							: R.id.ocr_decode_failed, ocrResult);
			message.sendToTarget();
			activity.getProgressDialog().dismiss();
		}
		if (baseApi != null) {
			baseApi.clear();
		}
	}

	public static OcrResult getOcrResult(final CaptureActivity activity,
			final TessBaseAPI baseApi, Bitmap bitmap,
			PlanarYUVLuminanceSource source) {

		if ((bitmap == null && source == null)
				|| (bitmap != null && source != null)) {
			System.err.println(bitmap == null ? "OcrRecognize: bitmap is null!"
					: "OcrRecognize: source is null!");
			return null;
		}

		long start = System.currentTimeMillis();
		long timeRequired = 0;
		OcrResult ocrResult = null;
		String textResult = null;

		if (activity.isOCROnline()) { // get online ocr
			// get input information
			byte[] oriBM = null;
			int width = 0;
			int height = 0;
			if (bitmap != null) {
				oriBM = ImageProcessing.bitmapToJpegImageData(bitmap);
				width = bitmap.getWidth();
				height = bitmap.getHeight();
				System.out.println("Online OCR: get image from SD card");
			} else {
				oriBM = source.getCroppedJpegImageData();
				// NOTE: width, height must be even numbers.
				width = source.getWidth() & (~1);
				height = source.getHeight() & (~1);
				System.out.println("Online OCR: get image from Camera");
			}
			int size = oriBM.length;
			// compress data
			byte[] compressedBM = ImageProcessing.compress(oriBM);
			// send image to server
			String onlineResult = HpccOcrServiceConnector.postToOcrService(
					compressedBM, size, width, height,
					activity.getSourceLanguageCode(),
					activity.getPageSegmentationMode(),
					activity.isPrePostProcessing());
			if (onlineResult != null && !onlineResult.equals("")) {
				// handle result after receive data from server
				JSONObject jsonResult = null;
				try {
					jsonResult = (JSONObject) new JSONTokener(onlineResult)
							.nextValue();
					byte ocrCode = (byte) jsonResult
							.getInt(MessageTypes.HEADER);
					if (ocrCode == MessageTypes.OCR_OK) {
						textResult = jsonResult.getString(MessageTypes.DATA);
						ocrResult = new OcrResult();
						System.err.println("OCR OK with code: " + ocrCode
								+ ", data: " + textResult);
					} else {
						System.err.println("OCR error with code: " + ocrCode);
						return null;
					}
				} catch (JSONException e) {
					// e.printStackTrace(); ClassCastException
					try {
						baseApi.clear();
						activity.stopHandler();
					} catch (NullPointerException e1) {
						// Continue
					}
					return null;
				} catch (ClassCastException e) {
					try {
						baseApi.clear();
						activity.stopHandler();
					} catch (NullPointerException e1) {
						// Continue
					}
					return null;
				}
			} else {
				return null;
			}
		} else { // get offline ocr
			try {
				if (bitmap == null) {
					bitmap = source.renderCroppedGreyscaleBitmap();
					System.out.println("Offline OCR: get image from Camera");
				} else {
					System.out.println("Offline OCR: get image from SD card");
				}

				baseApi.setImage(ReadFile.readBitmap(bitmap));
				textResult = baseApi.getUTF8Text();
				timeRequired = System.currentTimeMillis() - start;

				// Check for failure to recognize text
				if (textResult == null || textResult.equals("")) {
					return null;
				}
				ocrResult = new OcrResult();
				ocrResult.setWordConfidences(baseApi.wordConfidences());
				ocrResult.setMeanConfidence(baseApi.meanConfidence());
				ocrResult.setRegionBoundingBoxes(baseApi.getRegions()
						.getBoxRects());
				ocrResult.setTextlineBoundingBoxes(baseApi.getTextlines()
						.getBoxRects());
				ocrResult.setStripBoundingBoxes(baseApi.getStrips()
						.getBoxRects());
				ocrResult
						.setWordBoundingBoxes(baseApi.getWords().getBoxRects());

				ocrResult.setCharacterBoundingBoxes(baseApi.getCharacters()
						.getBoxRects());

			} catch (RuntimeException e) {
				Log.e("OcrRecognizeAsyncTask",
						"Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
				e.printStackTrace();
				try {
					baseApi.clear();
					activity.stopHandler();
				} catch (NullPointerException e1) {
					// Continue
				}
				return null;
			}
		}
		timeRequired = System.currentTimeMillis() - start;
		if (!activity.isOCROnline()) {
			ocrResult.setBitmap(bitmap);
		}
		ocrResult.setText(textResult);
		ocrResult.setRecognitionTimeRequired(timeRequired);
		return ocrResult;
	}
}
