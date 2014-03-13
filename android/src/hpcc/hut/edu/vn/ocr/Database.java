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

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Save data between activities
 * 
 * @author minhhai
 * 
 */
public class Database {
	public static final String HEADER_SOURCE = "HEADER_SOURCE_PLANAR";
	public static final byte HEADER_SOURCE_PLANAR = 0;
	public static final byte HEADER_SOURCE_BITMAP = 1;
	public static final byte HEADER_SOURCE_URI = 2;
	public static final String HEADER_RESULT = "HEADER_RESULT";

	public static final String HEADER_OCR_LANGUAGE = "HEADER_OCR_LANGUAGE_4_RESULT";
	public static final String HEADER_OCR_LANGUAGE_CODE = "HEADER_OCR_LANGUAGE_CODE_4_RESULT";
	public static final String HEADER_OCR_RESULT = "HEADER_OCR_RESULT_4_RESULT";
	public static final String HEADER_TRANSLATE_INDEX = "HEADER_TRANSLATE_INDEX_4_RESULT";
	
	public static PlanarYUVLuminanceSource planarSource;
	public static Bitmap bitmapSource;
	public static Uri uriSource;
	public static OcrResult ocrResult;
}
