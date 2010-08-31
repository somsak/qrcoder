/*
 * Copyright (C) 2008 ZXing authors
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

package th.co.yellowpages.zxing.client.ypandroid.encode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import th.co.yellowpages.zxing.client.ypandroid.Intents;
import th.co.yellowpages.zxing.client.ypandroid.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.text.format.Time;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class encodes data from an Intent into a QR code, and then displays it
 * full screen so that another person can scan it with their device.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class EncodeActivity extends Activity {
	private QRCodeEncoder qrCodeEncoder;
	private ProgressDialog progressDialog;
	private boolean firstLayout;

	private Bitmap qrCodeBitmap;
	public static final String qrCodeBitmapSavePath = "/sdcard/yp_saved_qr";

	/**
	 * This needs to be delayed until after the first layout so that the view
	 * dimensions will be available.
	 */
	private final OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
		public void onGlobalLayout() {
			if (firstLayout) {
				View layout = findViewById(R.id.encode_view);
				int width = layout.getWidth();
				int height = layout.getHeight();
				int smallerDimension = width < height ? width : height;
				smallerDimension = smallerDimension * 7 / 8;

				Intent intent = getIntent();
				try {
					qrCodeEncoder = new QRCodeEncoder(EncodeActivity.this, intent);
					setTitle(getString(R.string.app_name) + " - "
							+ qrCodeEncoder.getTitle());
					qrCodeEncoder.requestBarcode(handler, smallerDimension);
					progressDialog = ProgressDialog.show(EncodeActivity.this, null,
							getString(R.string.msg_encode_in_progress), true, true,
							cancelListener);
				} catch (IllegalArgumentException e) {
					showErrorMessage(R.string.msg_encode_contents_failed);
				}
				firstLayout = false;
			}
		}
	};

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case R.id.encode_succeeded:
				progressDialog.dismiss();
				progressDialog = null;
				Bitmap image = (Bitmap) message.obj;
				ImageView view = (ImageView) findViewById(R.id.image_view);
				view.setImageBitmap(image);
				TextView contents = (TextView) findViewById(R.id.contents_text_view);
				contents.setText(qrCodeEncoder.getDisplayContents());
				qrCodeEncoder = null;

				setQRBitmapForSave(image);
				break;
			case R.id.encode_failed:
				showErrorMessage(R.string.msg_encode_barcode_failed);
				qrCodeEncoder = null;
				break;
			}
		}
	};

	private void setQRBitmapForSave(Bitmap bitmap) {
		if (bitmap != null)
			this.qrCodeBitmap = bitmap;
	}

	private final OnClickListener clickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			finish();
		}
	};

	private final OnCancelListener cancelListener = new OnCancelListener() {
		public void onCancel(DialogInterface dialog) {
			finish();
		}
	};

	private final Button.OnClickListener saveQRListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(((View) v
					.getParent()).getContext());

			// Create filename from datetime
			Time time = new Time();
			time.setToNow();

			Bitmap.CompressFormat saveFormat = Bitmap.CompressFormat.PNG;
			String saveName = time.format2445() + ".png";

			String alertTitle = "";
			String alertMessage = "";
			if (saveQRCodeAs(saveName, saveFormat)) {
				alertTitle = "QR Code saved";
				alertMessage = String.format("%s saved", saveName);
				findViewById(R.id.yp_save_qr_button).setEnabled(false);
			} else {
				alertTitle = "QR Code failed to save";
				alertMessage = "FAIL to save QR Code";
			}

			alertDialog.setTitle(alertTitle);
			alertDialog.setMessage(alertMessage);
			alertDialog.show();
		}
	};

	private boolean saveQRCodeAs(String fileName, Bitmap.CompressFormat format) {
		ContentResolver contentResolver = getContentResolver();
		ContentValues values = new ContentValues(2);
		values.put(Media.DISPLAY_NAME, fileName);
		values.put(Media.MIME_TYPE, "image/png");

		Uri fileUri = contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
		// File sddir = new File(EncodeActivity.qrCodeBitmapSavePath);

		// if (sddir.exists() || !sddir.mkdirs()) {
		try {
			// String filePath = sddir.getAbsolutePath() + "/" + fileName;
			OutputStream out = contentResolver.openOutputStream(fileUri);
			this.qrCodeBitmap.compress(format, 100, out);

			out.flush();
			out.close();

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		// }

		return false;
	}

	private final Button.OnClickListener newQRListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
			finish();
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Intent intent = getIntent();
		if (intent != null) {
			String action = intent.getAction();
			if (action.equals(Intents.Encode.ACTION)
					|| action.equals(Intent.ACTION_SEND)) {
				setContentView(R.layout.encode);

				findViewById(R.id.yp_save_qr_button).setOnClickListener(saveQRListener);
				findViewById(R.id.yp_new_qr_button).setOnClickListener(newQRListener);
				return;
			}
		}
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();

		View layout = findViewById(R.id.encode_view);
		layout.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
		firstLayout = true;
	}

	private void showErrorMessage(int message) {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.button_ok, clickListener);
		builder.show();
	}
}
