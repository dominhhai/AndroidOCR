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

import hpcc.hut.edu.vn.ocr.language.TranslateBingAsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends Activity {

	private TextView tv_ocrlang;
	private EditText et_ocrResult;
	private EditText et_translateResult;
	private ProgressBar translateProgressBar;
	private CheckBox translateCheckbox;
	private Spinner translateSpinner;

	private String ocr_lang;
	private String ocr_lang_code;
	private String tran_lang_code;

	private String[] list_translate_lang;

	private String dirPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.result);
		dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/BKDroidOCR/";
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		// get GUI components
		this.tv_ocrlang = (TextView) this.findViewById(R.id.tv_orig_lang);
		this.et_ocrResult = (EditText) this.findViewById(R.id.et_result);
		this.et_translateResult = (EditText) this
				.findViewById(R.id.et_translate);
		this.translateCheckbox = (CheckBox) this
				.findViewById(R.id.cb_translate);
		this.translateSpinner = (Spinner) this
				.findViewById(R.id.spinner_translate);
		this.translateProgressBar = (ProgressBar) this
				.findViewById(R.id.result_progressbar);
		// get data
		Intent intent = this.getIntent();
		String ocr_result = intent.getStringExtra(Database.HEADER_OCR_RESULT);
		this.ocr_lang = intent.getStringExtra(Database.HEADER_OCR_LANGUAGE);
		this.ocr_lang_code = intent
				.getStringExtra(Database.HEADER_OCR_LANGUAGE_CODE);
		int currentTranslateIndex = intent.getIntExtra(
				Database.HEADER_TRANSLATE_INDEX, 8);
		// set GUI
		this.registerForContextMenu(this.et_ocrResult);
		this.registerForContextMenu(this.et_translateResult);
		this.tv_ocrlang.setText("OCR: " + ocr_lang);
		this.et_ocrResult.setText(ocr_result);
		this.translateSpinner.setSelection(currentTranslateIndex);
		if (this.translateCheckbox.isChecked()) {
			this.enableTranslateView();
		} else {
			this.disableTranslateView();
		}
		this.list_translate_lang = this.getResources().getStringArray(
				R.array.translationtargetiso6391_microsoft);
		// event handler
		this.translateCheckbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {
						if (isChecked) {
							ResultActivity.this.enableTranslateView();
						} else {
							ResultActivity.this.disableTranslateView();
						}
					}

				});
		this.translateSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						tran_lang_code = ResultActivity.this.list_translate_lang[pos];
						ResultActivity.this.et_translateResult
								.setEnabled(false);
						// Get the translation asynchronously
						new TranslateBingAsyncTask(ResultActivity.this,
								ResultActivity.this.translateProgressBar,
								ResultActivity.this.et_translateResult,
								ocr_lang_code, tran_lang_code, et_ocrResult
										.getText().toString()).execute();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}

				});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.equals(this.et_ocrResult)) {
			menu.add(Menu.NONE,
					CaptureActivity.OPTIONS_COPY_RECOGNIZED_TEXT_ID, Menu.NONE,
					"Copy recognized text");
			menu.add(Menu.NONE,
					CaptureActivity.OPTIONS_SHARE_RECOGNIZED_TEXT_ID,
					Menu.NONE, "Share recognized text");
			menu.add(Menu.NONE,
					CaptureActivity.OPTIONS_SAVE_RECOGNIZED_TEXT_ID, Menu.NONE,
					"Save recognized text");
		} else if (v.equals(this.et_translateResult)) {
			menu.add(Menu.NONE,
					CaptureActivity.OPTIONS_COPY_TRANSLATED_TEXT_ID, Menu.NONE,
					"Copy translated text");
			menu.add(Menu.NONE,
					CaptureActivity.OPTIONS_SHARE_TRANSLATED_TEXT_ID,
					Menu.NONE, "Share translated text");
			menu.add(Menu.NONE,
					CaptureActivity.OPTIONS_SAVE_TRANSLATED_TEXT_ID, Menu.NONE,
					"Save translated text");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CaptureActivity.OPTIONS_COPY_RECOGNIZED_TEXT_ID:
		case CaptureActivity.OPTIONS_COPY_TRANSLATED_TEXT_ID:
			CharSequence text = (item.getItemId() == CaptureActivity.OPTIONS_COPY_RECOGNIZED_TEXT_ID) ? this.et_ocrResult
					.getText() : this.et_translateResult.getText();
			this.doOptionsCopyText(text);
			return true;
		case CaptureActivity.OPTIONS_SHARE_RECOGNIZED_TEXT_ID:
		case CaptureActivity.OPTIONS_SHARE_TRANSLATED_TEXT_ID:
			text = (item.getItemId() == CaptureActivity.OPTIONS_SHARE_RECOGNIZED_TEXT_ID) ? this.et_ocrResult
					.getText() : this.et_translateResult.getText();
			this.doOptionsShareText(
					item.getItemId() == CaptureActivity.OPTIONS_SAVE_RECOGNIZED_TEXT_ID,
					text);
			return true;
		case CaptureActivity.OPTIONS_SAVE_RECOGNIZED_TEXT_ID:
		case CaptureActivity.OPTIONS_SAVE_TRANSLATED_TEXT_ID:
			text = (item.getItemId() == CaptureActivity.OPTIONS_SAVE_RECOGNIZED_TEXT_ID) ? this.et_ocrResult
					.getText() : this.et_translateResult.getText();
			this.doOptionsSaveText(
					item.getItemId() == CaptureActivity.OPTIONS_SHARE_RECOGNIZED_TEXT_ID,
					text);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void disableTranslateView() {
		this.translateSpinner.setVisibility(View.GONE);
		this.et_translateResult.setVisibility(View.GONE);
		this.translateProgressBar.setVisibility(View.GONE);
	}

	private void enableTranslateView() {
		this.translateSpinner.setVisibility(View.VISIBLE);
		this.et_translateResult.setVisibility(View.VISIBLE);
		// this.translateProgressBar.setVisibility(View.VISIBLE);
		this.et_translateResult.setText("Translating...");
		tran_lang_code = this.list_translate_lang[this.translateSpinner
				.getSelectedItemPosition()];
		this.et_translateResult.setEnabled(false);
		// Get the translation asynchronously
		new TranslateBingAsyncTask(ResultActivity.this,
				ResultActivity.this.translateProgressBar,
				ResultActivity.this.et_translateResult, ocr_lang_code,
				tran_lang_code, et_ocrResult.getText().toString()).execute();
	}

	public void doOptionsCopyText(final CharSequence text) {
		ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clipboardManager.setText(text);
		if (clipboardManager.hasText()) {
			Toast toast = Toast.makeText(this, "Text copied.",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		}
	}

	public void doOptionsShareText(final boolean isOCR, final CharSequence text) {
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Share "
				+ (isOCR ? "OCR Result" : "Translate Result"));
		sharingIntent.putExtra(Intent.EXTRA_TEXT, text.toString());
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}

	public void doOptionsSaveText(final boolean isOCR, final CharSequence text) {
		String filename = (isOCR ? "ocr_" : "translate_")
				+ System.currentTimeMillis() + ".txt";
		File file = new File(this.dirPath, filename);
		FileOutputStream fos;
		byte[] bytedata = text.toString().getBytes();
		try {
			fos = new FileOutputStream(file);
			fos.write(bytedata);
			fos.flush();
			fos.close();
			Toast toast = Toast.makeText(this, "Text save as " + this.dirPath
					+ filename + ".", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		} catch (FileNotFoundException e) {
			Toast toast = Toast.makeText(this,
					"Not found dir path. Can't save text.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		} catch (IOException e) {
			Toast toast = Toast.makeText(this, "Write error! Can't save text.",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		}
	}

}
