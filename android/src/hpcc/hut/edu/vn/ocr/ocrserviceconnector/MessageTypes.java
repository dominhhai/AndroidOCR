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

package hpcc.hut.edu.vn.ocr.ocrserviceconnector;

public final class MessageTypes {
	// header
	public static final String HEADER = "ocr";
	// data
	public static final String DATA = "result";
	// data error when extracting
	public static final byte ER_DATA_ERROR = 0;
	// data process error
	public static final byte ER_PRE_PROCESSING_ERROR = 1;
	// ocr error
	public static final byte ER_OCR_ERROR = 2;
	// ocr success
	public static final byte OCR_OK = 3;
}
