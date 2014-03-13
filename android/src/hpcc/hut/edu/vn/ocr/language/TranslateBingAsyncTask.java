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
package hpcc.hut.edu.vn.ocr.language;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import hpcc.hut.edu.vn.ocr.CaptureActivity;
import hpcc.hut.edu.vn.ocr.R;
import hpcc.hut.edu.vn.ocr.ResultActivity;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Class to perform translations in the background.
 */
public final class TranslateBingAsyncTask extends
		AsyncTask<String, String, Boolean> {

	// account on Microsoft Translator - free package
	private static final String CLIENT_ID = "HaiDM";
	private static final String CLIENT_PASS = "/t955NfncQ5ZhYsFTdRCsBSY9ha4gA4WK9X0Iq+w5Cs=";

	private static final String TAG = TranslateBingAsyncTask.class
			.getSimpleName();

	// data for CaputureActivity
	private CaptureActivity activity;
	private TextView textView;
	// data for result
	private ResultActivity result_activity;
	private EditText editText;
	// common data
	private boolean isResult;
	private View progressView;
	private TextView targetLanguageTextView;
	private String sourceLanguageCode;
	private String targetLanguageCode;
	private String sourceText;
	private String translatedText = "";

	public TranslateBingAsyncTask(CaptureActivity activity,
			String sourceLanguageCode, String targetLanguageCode,
			String sourceText) {
		this.isResult = false;
		this.activity = activity;
		this.sourceLanguageCode = sourceLanguageCode;
		this.targetLanguageCode = targetLanguageCode;
		this.sourceText = sourceText;
		textView = (TextView) activity.findViewById(R.id.translation_text_view);
		progressView = (View) activity
				.findViewById(R.id.indeterminate_progress_indicator_view);
		targetLanguageTextView = (TextView) activity
				.findViewById(R.id.translation_language_text_view);
		this.initDataConnector();
	}

	public TranslateBingAsyncTask(ResultActivity activity, View progressView,
			EditText editText, String sourceLanguageCode,
			String targetLanguageCode, String sourceText) {
		this.isResult = true;
		this.result_activity = activity;
		this.progressView = progressView;
		if (this.progressView.getVisibility() != View.VISIBLE) {
			this.progressView.setVisibility(View.VISIBLE);
		}
		this.editText = editText;
		this.sourceLanguageCode = sourceLanguageCode;
		this.targetLanguageCode = targetLanguageCode;
		this.sourceText = sourceText;
		this.initDataConnector();
	}

	private void initDataConnector() {
		// init translate key
		Translate.setClientId(CLIENT_ID);
		Translate.setClientSecret(CLIENT_PASS);
	}

	@Override
	protected Boolean doInBackground(String... arg0) {
		sourceLanguageCode = TranslateBingAsyncTask
				.toLanguage(LanguageCodeHelper.getTranslationLanguageName(
						(this.isResult ? result_activity : activity)
								.getBaseContext(), sourceLanguageCode));
		translatedText = null;
		translatedText = TranslateBingAsyncTask.translate(sourceLanguageCode,
				targetLanguageCode, sourceText);

		return translatedText != null;
	}

	@Override
	protected synchronized void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		try {
			if (result) {
				// Log.i(TAG, "SUCCESS");
				if (this.isResult) {
					this.editText.setText(translatedText);
					this.editText.setEnabled(true);
					// this.result_activity.translate_result = translatedText;
				} else {
					if (targetLanguageTextView != null) {
						targetLanguageTextView.setTypeface(
								Typeface.defaultFromStyle(Typeface.NORMAL),
								Typeface.NORMAL);
					}
					textView.setText(translatedText);
					textView.setVisibility(View.VISIBLE);
					textView.setTextColor(activity.getResources().getColor(
							R.color.translation_text));

					// Crudely scale betweeen 22 and 32 -- bigger font for
					// shorter
					// text
					int scaledSize = Math.max(22,
							32 - translatedText.length() / 4);
					textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);
				}

			} else {
				Log.e(TAG, "FAILURE");
				if (this.isResult) {
					this.editText.setText("Unavailable");
				} else {
					targetLanguageTextView.setTypeface(
							Typeface.defaultFromStyle(Typeface.ITALIC),
							Typeface.ITALIC);
					targetLanguageTextView.setText("Unavailable");
				}
			}
		} catch (Exception e) {
			// continue implement
		}
		// Turn off the indeterminate progress indicator
		if (progressView != null) {
			progressView.setVisibility(View.GONE);
		}
	}

	/**
	 * Translate using Microsoft Translate API
	 * 
	 * @param sourceLanguageCode
	 *            Source language code, for example, "en"
	 * @param targetLanguageCode
	 *            Target language code, for example, "es"
	 * @param sourceText
	 *            Text to send for translation
	 * @return Translated text
	 */
	static String translate(String sourceLanguageCode,
			String targetLanguageCode, String sourceText) {
		try {
			Log.d(TAG, sourceLanguageCode + " -> " + targetLanguageCode);
			return Translate.execute(sourceText,
					Language.fromString(sourceLanguageCode),
					Language.fromString(targetLanguageCode));
		} catch (Exception e) {
			Log.e(TAG, "Caught exeption in translation request.");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Convert the given name of a natural language into a Language from the
	 * enum of Languages supported by this translation service.
	 * 
	 * @param languageName
	 *            The name of the language, for example, "English"
	 * @return code representing this language, for example, "en", for this
	 *         translation API
	 * @throws IllegalArgumentException
	 */
	public static String toLanguage(String languageName)
			throws IllegalArgumentException {
		// Convert string to all caps
		String standardizedName = languageName.toUpperCase();

		// Replace spaces with underscores
		standardizedName = standardizedName.replace(' ', '_');

		// Remove parentheses
		standardizedName = standardizedName.replace("(", "");
		standardizedName = standardizedName.replace(")", "");

		// Map Norwegian-Bokmal to Norwegian
		if (standardizedName.equals("NORWEGIAN_BOKMAL")) {
			standardizedName = "NORWEGIAN";
		}

		try {
			return Language.valueOf(standardizedName).toString();
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Not found--returning default language code");
			return CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE;
		}
	}
}
