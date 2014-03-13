/*
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

import hpcc.hut.edu.vn.ocr.imageprocessing.CropOption;
import hpcc.hut.edu.vn.ocr.imageprocessing.CropOptionAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ImageViewerActivity extends Activity {

	private static final int CROP_IMAGE = 1;
	private static final int OCR = 1;

	private Button btnOcr;
	private Button btnCrop;
	private Button btnRotate;
	private ImageView imgView;
	private String dirPath;
	private Uri imgUri;
	private ProgressBar progressBar;
	private Bitmap source = null;

	private FileOutputStream fileOutputStream;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.imageview);

		dirPath = Environment.getExternalStorageDirectory().toString()
				+ "/BKDroiOCR/";
		// File dir = new File(dirPath);
		// if (!dir.exists()) {
		// dir.mkdir();
		// }

		this.btnOcr = (Button) this.findViewById(R.id.btn_ocr);
		this.btnCrop = (Button) this.findViewById(R.id.btn_crop);
		this.btnRotate = (Button) this.findViewById(R.id.btn_rotate);
		this.imgView = (ImageView) this.findViewById(R.id.img_view);
		this.progressBar = (ProgressBar) this
				.findViewById(R.id.imgview_progressbar);
		// CHECK LAYOUT
		// int btn_width = this.btnRotate.getWidth();
		// this.btnCrop.setWidth(btn_width);
		// this.btnOcr.setWidth(btn_width);
		this.progressBar.setVisibility(View.GONE);
		// get data
		Intent intent = this.getIntent();
		byte header = intent.getByteExtra(Database.HEADER_SOURCE, (byte) -1);
		if (header == Database.HEADER_SOURCE_URI) {
			this.imgUri = Database.uriSource;
			this.imgView.setImageURI(this.imgUri);
		} else {
			this.source = Database.bitmapSource;
			this.imgView.setImageBitmap(source);
		}
		// event handling
		this.btnCrop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// doCrop();
				performCrop();
			}
		});
		this.btnOcr.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sentData4CaptureActivity ();
			}
		});
	}

	protected void sentData4CaptureActivity() {
		Intent returnIntent = new Intent();
		Bundle extras = new Bundle ();
		extras.putParcelable("data", this.source);
		returnIntent.putExtras(extras);
		this.setResult(Activity.RESULT_OK, returnIntent);
		this.finish();
	}

	protected void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(this, "Can not find image crop app",
					Toast.LENGTH_SHORT).show();

			return;
		} else {
			intent.setData(imgUri);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, CROP_IMAGE);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(
										cropOptions.get(item).appIntent,
										CROP_IMAGE);
							}
						});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (imgUri != null) {
							getContentResolver().delete(imgUri, null, null);
							imgUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}

	}

	protected void bmToUri(boolean isUriExits) {
		if (isUriExits) {
			imgUri = Database.uriSource;
		} else {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				source.compress(CompressFormat.JPEG, 100, baos);
				File temp = new File(dirPath, "temp.jpg");
				temp.createNewFile();
				fileOutputStream = new FileOutputStream(temp);
				fileOutputStream.write(baos.toByteArray());
				imgUri = Uri.fromFile(temp);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Helper method to carry out crop operation
	 */
	private void performCrop() {
		// take care of exceptions
		try {
			// call the standard crop action intent (the user device may not
			// support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(this.imgUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			cropIntent.putExtra("scale", true);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, CROP_IMAGE);
		}
		// respond to users whose devices do not support the crop action
		catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case CROP_IMAGE:
			// get the returned data
			Bundle extras = data.getExtras();
//			System.out.println("result extras");
			if (extras != null) {
				// retrieve a reference to the ImageView
				this.source = extras.getParcelable("data");
				Database.bitmapSource = this.source;
				// display the returned cropped image
				this.imgView.setImageBitmap(source);
			}
			break;
		}
	}
}
