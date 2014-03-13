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

import hpcc.hut.edu.vn.ocr.CaptureActivity;

import com.googlecode.tesseract.android.TessBaseAPI;

import hpcc.hut.edu.vn.ocr.R;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Class to send bitmap data for OCR.
 * 
 * The code for this class was adapted from the ZXing project:
 * http://code.google.com/p/zxing/
 */
final class DecodeHandler extends Handler {

	private final CaptureActivity activity;
	private boolean running = true;
	private final TessBaseAPI baseApi;
	private static boolean isDecodePending;

	DecodeHandler(CaptureActivity activity) {
		this.activity = activity;
		baseApi = activity.getBaseApi();
	}

	@Override
	public void handleMessage(Message message) {
		if (!running) {
			return;
		}
		switch (message.what) {
		case R.id.ocr_continuous_decode:
			// Only request a decode if a request is not already pending.
			if (!isDecodePending) {
				isDecodePending = true;
				ocrContinuousDecode((byte[]) message.obj, message.arg1,
						message.arg2);
			}
			break;
		case R.id.ocr_decode:
			ocrDecode((byte[]) message.obj, message.arg1, message.arg2);
			break;
		case R.id.quit:
			running = false;
			Looper.myLooper().quit();
			break;
		}
	}

	static void resetDecodeState() {
		isDecodePending = false;
	}

	/**
	 * Launch an AsyncTask to perform an OCR decode for single-shot mode.
	 * 
	 * @param data
	 *            Image data
	 * @param width
	 *            Image width
	 * @param height
	 *            Image height
	 */
	private void ocrDecode(byte[] data, int width, int height) {
		activity.displayProgressDialog();

		// Launch OCR asynchronously, so we get the dialog box displayed
		// immediately
		new OcrRecognizeAsyncTask(activity, baseApi, data, width, height)
				.execute();
	}

	/**
	 * Perform an OCR decode for realtime recognition mode.
	 * 
	 * @param data
	 *            Image data
	 * @param width
	 *            Image width
	 * @param height
	 *            Image height
	 */
	private void ocrContinuousDecode(byte[] data, int width, int height) {
		PlanarYUVLuminanceSource source = activity.getCameraManager()
				.buildLuminanceSource(data, width, height);
		if (source == null) {
			sendContinuousOcrFailMessage();
			return;
		}

		OcrResult ocrResult = OcrRecognizeAsyncTask.getOcrResult(activity,
				baseApi, null, source);
		Handler handler = activity.getHandler();
		if (handler == null) {
			return;
		}

		if (ocrResult == null) {
			try {
				sendContinuousOcrFailMessage();
			} catch (NullPointerException e) {
				activity.stopHandler();
			} finally {
				baseApi.clear();
			}
			return;
		}

		try {
			Message message = Message.obtain(handler,
					R.id.ocr_continuous_decode_succeeded, ocrResult);
			message.sendToTarget();
		} catch (NullPointerException e) {
			activity.stopHandler();
		} finally {
			baseApi.clear();
		}
	}

	private void sendContinuousOcrFailMessage() {
		Handler handler = activity.getHandler();
		if (handler != null) {
			Message message = Message.obtain(handler,
					R.id.ocr_continuous_decode_failed, new OcrResult(null,
							null, null, 0, null, null, null, null, null, 0));
			message.sendToTarget();
		}
	}

}
