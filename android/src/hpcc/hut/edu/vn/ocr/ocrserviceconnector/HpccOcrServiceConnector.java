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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class HpccOcrServiceConnector {
	private static final String HOST = "http://bkluster.hust.edu.vn:8080/TessOCRWebServices/ocrservice";

	/*
	 * FormDataParam("image") InputStream uploadedInputStream,
	 * 
	 * @FormDataParam("size") int imagesize,
	 * 
	 * @FormDataParam("width") int width, @FormDataParam("height") int height,
	 * 
	 * @DefaultValue("eng") @FormDataParam("lang") String language,
	 * 
	 * @DefaultValue("3") @FormDataParam("psm") int pagesegmode,
	 * 
	 * @DefaultValue("true") @FormDataParam("process") boolean isPrePostProcess
	 */
	public static String postToOcrService(byte[] data, int size, int imgw,
			int imgh, String lang, int psm, boolean isPrePostProcess) {
		System.out.println("Sent data: w = " + imgw + ", h = " + imgh
				+ ", psm = " + psm + ", process: " + isPrePostProcess + ", with lang: " + lang);
		String result = "";

		try {
			HttpParams httpParamenters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParamenters, 30000);
			DefaultHttpClient httpClient = new DefaultHttpClient(
					httpParamenters);
			HttpPost postRequest = new HttpPost(HOST);

			ByteArrayBody bab = new ByteArrayBody(data, "input.jpg");
			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("image", bab);
			reqEntity.addPart("size", new StringBody("" + size)); // size to
																	// check if
																	// decompress
																	// fail
			reqEntity.addPart("width", new StringBody("" + imgw));
			reqEntity.addPart("height", new StringBody("" + imgh));
			reqEntity.addPart("lang", new StringBody(lang));
			reqEntity.addPart("psm", new StringBody("" + psm));
			reqEntity.addPart("process", new StringBody("" + isPrePostProcess));

			postRequest.setEntity(reqEntity);

			HttpResponse response = httpClient.execute(postRequest);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();
			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
			result = s.toString();
			System.out.println("result in Json: " + result);
		} catch (Exception e) {
			// handle exception here
			Log.e(e.getClass().getName(), e.getMessage());
			return null;
		}
		return result;

	}
}
