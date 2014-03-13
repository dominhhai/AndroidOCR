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

import java.io.FileNotFoundException;
import java.io.IOException;

import hpcc.hut.edu.vn.ocr.CaptureActivity;
import hpcc.hut.edu.vn.ocr.OcrResult;
import hpcc.hut.edu.vn.ocr.camera.CameraManager;
import hpcc.hut.edu.vn.ocr.R;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * The code for this class was adapted from the ZXing project:
 * http://code.google.com/p/zxing/
 */
final class CaptureActivityHandler extends Handler {

	private static final String TAG = CaptureActivityHandler.class
			.getSimpleName();

	private final CaptureActivity activity;
	private final DecodeThread decodeThread;
	private static State state;
	private final CameraManager cameraManager;

	private enum State {
		PREVIEW, PREVIEW_PAUSED, CONTINUOUS, CONTINUOUS_PAUSED, SUCCESS, DONE
	}

	CaptureActivityHandler(CaptureActivity activity,
			CameraManager cameraManager, boolean isContinuousModeActive) {
		this.activity = activity;
		this.cameraManager = cameraManager;

		// Start ourselves capturing previews (and decoding if using continuous
		// recognition mode).
		cameraManager.startPreview();

		decodeThread = new DecodeThread(activity);
		decodeThread.start();

		if (isContinuousModeActive) {
			state = State.CONTINUOUS;

			// Show the shutter and torch buttons
			activity.setButtonVisibility(true);

			// Display a "be patient" message while first recognition request is
			// running
			activity.setStatusViewForContinuous();

			restartOcrPreviewAndDecode();
		} else {
			state = State.SUCCESS;

			// Show the shutter and torch buttons
			activity.setButtonVisibility(true);

			restartOcrPreview();
		}
	}

	@Override
	public void handleMessage(Message message) {

		switch (message.what) {
		case R.id.restart_preview:
			restartOcrPreview();
			break;
		case R.id.ocr_continuous_decode_failed:
			DecodeHandler.resetDecodeState();
			try {
				activity.handleOcrContinuousDecode(message.obj == null ? new OcrResult(
						null, null, null, 0, null, null, null, null, null, 0)
						: (OcrResult) message.obj);
			} catch (NullPointerException e) {
				Log.w(TAG, "got bad OcrResultFailure", e);
			}
			if (state == State.CONTINUOUS) {
				restartOcrPreviewAndDecode();
			}
			break;
		case R.id.ocr_continuous_decode_succeeded:
			DecodeHandler.resetDecodeState();
			try {
				activity.handleOcrContinuousDecode((OcrResult) message.obj);
			} catch (NullPointerException e) {
				// Continue
			}
			if (state == State.CONTINUOUS) {
				restartOcrPreviewAndDecode();
			}
			break;
		case R.id.ocr_decode_succeeded:
			state = State.SUCCESS;
			activity.setShutterButtonClickable(true);
			activity.setGetimgButtonClickable(true);
			activity.handleOcrDecode((OcrResult) message.obj);
			break;
		case R.id.ocr_decode_failed:
			state = State.PREVIEW;
			activity.setShutterButtonClickable(true);
			activity.setGetimgButtonClickable(true);
			// OcrResult ocrResult = (OcrResult) message.obj;
			// TODO here
			Toast toast = Toast.makeText(activity.getBaseContext(),
					"OCR failed. Please try again.", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP, 0, 0);
			toast.show();
			break;
		}
	}

	void stop() {
		// TODO See if this should be done by sending a quit message to
		// decodeHandler as is done
		// below in quitSynchronously().

		Log.d(TAG, "Setting state to CONTINUOUS_PAUSED.");
		state = State.CONTINUOUS_PAUSED;
		removeMessages(R.id.ocr_continuous_decode);
		removeMessages(R.id.ocr_decode);
		removeMessages(R.id.ocr_continuous_decode_failed);
		removeMessages(R.id.ocr_continuous_decode_succeeded); // TODO are these
																// removeMessages()
																// calls doing
																// anything?

		// Freeze the view displayed to the user.
		// CameraManager.get().stopPreview();
	}

	void resetState() {
		// Log.d(TAG, "in restart()");
		if (state == State.CONTINUOUS_PAUSED) {
			Log.d(TAG, "Setting state to CONTINUOUS");
			state = State.CONTINUOUS;
			restartOcrPreviewAndDecode();
		}
	}

	void quitSynchronously() {
		state = State.DONE;
		if (cameraManager != null) {
			cameraManager.stopPreview();
		}
		// Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		try {
			// quit.sendToTarget(); // This always gives
			// "sending message to a Handler on a dead thread"

			// Wait at most half a second; should be enough time, and onPause()
			// will timeout quickly
			decodeThread.join(500L);
		} catch (InterruptedException e) {
			Log.w(TAG, "Caught InterruptedException in quitSyncronously()", e);
			// continue
		} catch (RuntimeException e) {
			Log.w(TAG, "Caught RuntimeException in quitSyncronously()", e);
			// continue
		} catch (Exception e) {
			Log.w(TAG, "Caught unknown Exception in quitSynchronously()", e);
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(R.id.ocr_continuous_decode);
		removeMessages(R.id.ocr_decode);

	}

	/**
	 * Start the preview, but don't try to OCR anything until the user presses
	 * the shutter button.
	 */
	private void restartOcrPreview() {
		// Display the shutter and torch buttons
		activity.setButtonVisibility(true);

		if (state == State.SUCCESS) {
			state = State.PREVIEW;

			// Draw the viewfinder.
			activity.drawViewfinder();
		}
	}

	/**
	 * Send a decode request for realtime OCR mode
	 */
	private void restartOcrPreviewAndDecode() {
		// Continue capturing camera frames
		cameraManager.startPreview();

		// Continue requesting decode of images
		cameraManager.requestOcrDecode(decodeThread.getHandler(),
				R.id.ocr_continuous_decode);
		activity.drawViewfinder();
	}

	/**
	 * Request OCR on the current preview frame.
	 */
	private void ocrDecode() {
		state = State.PREVIEW_PAUSED;
		cameraManager.requestOcrDecode(decodeThread.getHandler(),
				R.id.ocr_decode);
	}

	/**
	 * Request OCR when the hardware shutter button is clicked.
	 */
	void hardwareShutterButtonClick() {
		// Ensure that we're not in continuous recognition mode
		if (state == State.PREVIEW) {
			ocrDecode();
		}
	}

	/**
	 * Request OCR when the on-screen shutter button is clicked.
	 */
	void shutterButtonClick() {
		// Disable further clicks on this button until OCR request is finished
		activity.setShutterButtonClickable(false);
		ocrDecode();
	}

	/**
	 * Get Image from SD card to OCR
	 */

	public static final int PICK_FROM_FILE = 0;
	public static final int AFTER_IMG_HANDLING = 1;

	void getimageButtonClick() {
		// Disable further clicks on this button until OCR request is finished
		activity.setGetimgButtonClickable(false);
		// do get image
		state = State.PREVIEW_PAUSED;
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		activity.startActivityForResult(
				Intent.createChooser(intent, "Complete action using"),
				PICK_FROM_FILE);
	}

	void handleGettedImage(Uri uri) {
		try {
			Bitmap ocrBitmap = MediaStore.Images.Media.getBitmap(
					activity.getContentResolver(), uri);
			// System.out.println("getted bitmap: " + ocrBitmap.getWidth() +
			// ", "
			// + ocrBitmap.getHeight());

			// activity.displayProgressDialog();
			// // convert bitmap to ARGB_8888
			// int width = ocrBitmap.getWidth();
			// int height = ocrBitmap.getHeight();
			// int[] pixels = new int[width * height];
			// ocrBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
			// // to greyscale
			// for (int i = 0; i < pixels.length; i ++) {
			// int red = (pixels[i] >> 16) & 0xFF;
			// int green = (pixels[i] >> 8) & 0xFF;
			// int blue = pixels[i] & 0xFF;
			// pixels[i] = (int) (0.2989 * red + 0.587 * green + 0.114 * blue);
			// }
			// ocrBitmap = Bitmap.createBitmap(width, height,
			// Bitmap.Config.ARGB_8888);
			// ocrBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			// // Launch OCR asynchronously, so we get the dialog box displayed
			// // immediately
			// new OcrRecognizeAsyncTask(activity, activity.getBaseApi(),
			// ocrBitmap).execute();
			Database.bitmapSource = ocrBitmap;
			Database.uriSource = uri;
			Intent intent = new Intent().setClass(activity,
					ImageViewerActivity.class);
			intent.putExtra(Database.HEADER_SOURCE,
					Database.HEADER_SOURCE_BITMAP);
			activity.startActivity(intent);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void handleAfterProcessingImage(Bitmap bitmap) {
		new OcrRecognizeAsyncTask(activity, activity.getBaseApi(), bitmap)
				.execute();
	}
}
