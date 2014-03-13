/*
 * Copyright (C) 2008 ZXing authors
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

package hpcc.hut.edu.vn.ocr.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GetImageButton extends ImageView {
	/**
	 * A callback to be invoked when a GetImageButton's pressed state changes.
	 */
	public interface OnGetImageButtonListener {
		/**
		 * Called when a GetImageButton has been pressed.
		 * 
		 * @param b
		 *            The GetImageButton that was pressed.
		 */
		void onGetImageButtonClick(GetImageButton b);
	}

	private OnGetImageButtonListener mListener;

	public GetImageButton(Context context) {
		super(context);
	}

	public GetImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GetImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnGetImageButtonListener(OnGetImageButtonListener listener) {
		mListener = listener;
	}

	@Override
	public boolean performClick() {
		boolean result = super.performClick();
		if (mListener != null) {
			mListener.onGetImageButtonClick(this);
		}
		return result;
	}

}
