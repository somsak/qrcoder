package th.co.yellowpages.zxing.client.ypandroid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import th.co.yellowpages.zxing.client.ypandroid.history.HistoryManager;
import th.co.yellowpages.zxing.client.ypandroid.result.ResultButtonListener;
import th.co.yellowpages.zxing.client.ypandroid.result.ResultHandler;
import th.co.yellowpages.zxing.client.ypandroid.result.ResultHandlerFactory;
import th.co.yellowpages.zxing.client.ypandroid.share.ShareActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.common.HybridBinarizer;

public class MainActivity extends Activity {

	private static String TAG = "YP";
	private static final int SETTINGS_ID = Menu.FIRST;
	private static final int ABOUT_ID = Menu.FIRST + 1;
	private static final int MAX_RESULT_IMAGE_SIZE = 150;
	private static final String PACKAGE_NAME = "th.co.yellowpages.zxing.client.ypandroid";

	private static String versionName;
	private HistoryManager historyManager;
	private boolean playBeep;
	private boolean vibrate;
	private MediaPlayer mediaPlayer;

	private View resultView;
	private View wellcomeMenuView;

	private MainActivityHandler handler;

	private static final int PICK_IMAGE = 1;
	private static final long VIBRATE_DURATION = 200L;

	private final OnCompletionListener beepListener = new BeepListener();

	private final DialogInterface.OnClickListener aboutListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialogInterface, int i) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri
					.parse(getString(R.string.zxing_url)));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivity(intent);
		}
	};

	private final Button.OnClickListener scanListener = new Button.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(MainActivity.this, CaptureActivity.class
					.getName());
			startActivity(intent);
		}
	};

	private final Button.OnClickListener chooseImageListener = new Button.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			intent.setType("image/*");

			startActivityForResult(intent, PICK_IMAGE);
		}
	};

	private final Button.OnClickListener shareListener = new Button.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.setClassName(MainActivity.this, ShareActivity.class
					.getName());
			startActivity(intent);
		}
	};

	private final Button.OnClickListener historyListener = new Button.OnClickListener() {
		public void onClick(View v) {
			AlertDialog historyAlert = historyManager.buildAlert();
			historyAlert.show();
		}
	};

	private final Button.OnClickListener helpListener = new Button.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent
					.setClassName(MainActivity.this, HelpActivity.class
							.getName());
			startActivity(intent);
		}
	};

	public Handler getHandler() {
		return handler;
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		playBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, true);
		vibrate = prefs.getBoolean(PreferencesActivity.KEY_VIBRATE, false);

		initBeepSound();
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode) {
		historyManager.addHistoryItem(rawResult);
		handleDecodeInternally(rawResult, barcode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		// ContentResolver contentResolver = getContentResolver();

		switch (requestCode) {
		case PICK_IMAGE:
			if (resultCode == Activity.RESULT_OK) {
				// Uri imageUri = Uri.parse(intent.getAction());

				Uri imageUri = intent.getData();
				Result rawResult = null;
				MultiFormatReader qrReader = new MultiFormatReader();

				try {
					Bitmap bitmap = rescaleBitmap(imageUri);
					// bitmap = BitmapFactory.decodeStream(contentResolver
					// .openInputStream(imageUri));

					ByteBuffer buffer = ByteBuffer.allocate(bitmap
							.getRowBytes()
							* bitmap.getHeight());
					bitmap.copyPixelsToBuffer(buffer);
					BinaryBitmap binaryBitmap = new BinaryBitmap(
							new HybridBinarizer(new BitmapLuminanceSource(
									bitmap)));

					rawResult = qrReader.decode(binaryBitmap);
					if (rawResult != null) {
						handleDecode(rawResult, bitmap);
						rawResult = null;
					}

				} catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace();
				} catch (NotFoundException nfe) {
					Log.v(TAG, "QR Code not found in the image");

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(
							this);
					alertDialog.setTitle("Result");
					alertDialog.setMessage("QR Code not found in the image");
					alertDialog.show();

				} finally {
					qrReader.reset();
					// contentResolver.delete(imageUri, null, null);
				}
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// menu.add(0, SHARE_ID, 0, R.string.menu_share).setIcon(
		// android.R.drawable.ic_menu_share);
		// menu.add(0, HISTORY_ID, 0, R.string.menu_history).setIcon(
		// android.R.drawable.ic_menu_recent_history);
		menu.add(0, SETTINGS_ID, 0, R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		// menu.add(0, HELP_ID, 0, R.string.menu_help).setIcon(
		// android.R.drawable.ic_menu_help);
		menu.add(0, ABOUT_ID, 0, R.string.menu_about).setIcon(
				android.R.drawable.ic_menu_info_details);

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			resultView.setVisibility(View.GONE);
			wellcomeMenuView.setVisibility(View.VISIBLE);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_FOCUS
				|| keyCode == KeyEvent.KEYCODE_CAMERA) {
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SETTINGS_ID: {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.setClassName(this, PreferencesActivity.class.getName());
			startActivity(intent);
			break;
		}
		case ABOUT_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.title_about) + " " + versionName);
			builder.setMessage(getString(R.string.msg_about) + "\n\n"
					+ getString(R.string.zxing_url));
			builder.setIcon(R.drawable.zxing_icon);
			builder.setPositiveButton(R.string.button_open_browser,
					aboutListener);
			builder.setNegativeButton(R.string.button_cancel, null);
			builder.show();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void handleDecodeInternally(Result rawResult, Bitmap barcode) {
		wellcomeMenuView.setVisibility(View.GONE);
		resultView.setVisibility(View.VISIBLE);

		if (barcode == null) {
			barcode = ((BitmapDrawable) getResources().getDrawable(
					R.drawable.unknown_barcode)).getBitmap();
		}
		ImageView barcodeImageView = (ImageView) findViewById(R.id.yp_barcode_image_view);
		barcodeImageView.setVisibility(View.VISIBLE);
		barcodeImageView.setMaxWidth(MAX_RESULT_IMAGE_SIZE);
		barcodeImageView.setMaxHeight(MAX_RESULT_IMAGE_SIZE);
		barcodeImageView.setImageBitmap(barcode);

		// TextView formatTextView = (TextView)
		// findViewById(R.id.yp_format_text_view);
		// formatTextView.setVisibility(View.VISIBLE);
		// formatTextView.setText(getString(R.string.msg_default_format) + ": "
		// + rawResult.getBarcodeFormat().toString());

		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(
				this, rawResult);
		TextView typeTextView = (TextView) findViewById(R.id.yp_type_text_view);
		typeTextView.setText(getString(R.string.msg_default_type) + ": "
				+ resultHandler.getType().toString());

		ParsedResultType rt = resultHandler.getType();
		ImageView barcodeTypeImageView = (ImageView) findViewById(R.id.yp_barcode_type_image_view);
		barcodeTypeImageView.setVisibility(View.VISIBLE);

		if (rt == ParsedResultType.URI) {
			barcodeTypeImageView.setImageResource(R.drawable.www_icon);
		} else if (rt == ParsedResultType.SMS) {
			barcodeTypeImageView.setImageResource(R.drawable.sms_icon);
		} else if (rt == ParsedResultType.EMAIL_ADDRESS) {
			barcodeTypeImageView.setImageResource(R.drawable.email_icon);
		} else if (rt == ParsedResultType.TEL) {
			barcodeTypeImageView.setImageResource(R.drawable.tel_icon);
		}

		TextView contentsTextView = (TextView) findViewById(R.id.yp_contents_text_view);
		CharSequence title = getString(resultHandler.getDisplayTitle());
		SpannableStringBuilder styled = new SpannableStringBuilder(title
				+ "\n\n");
		styled.setSpan(new UnderlineSpan(), 0, title.length(), 0);
		CharSequence displayContents = resultHandler.getDisplayContents();
		styled.append(displayContents);
		contentsTextView.setText(styled);

		int buttonCount = resultHandler.getButtonCount();
		ViewGroup buttonView = (ViewGroup) findViewById(R.id.yp_result_button_view);
		buttonView.requestFocus();
		for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
			TextView button = (TextView) buttonView.getChildAt(x);
			if (x < buttonCount) {
				button.setVisibility(View.VISIBLE);
				button.setText(resultHandler.getButtonText(x));
				button.setOnClickListener(new ResultButtonListener(
						resultHandler, x));
			} else {
				button.setVisibility(View.GONE);
			}
		}

		playBeepSoundAndVibrate();
	}

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PackageInfo info;

		try {
			info = getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			// int currentVersion = info.versionCode;
			MainActivity.versionName = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.yp_main);

		historyManager = new HistoryManager(this);
		historyManager.trimHistory();

		handler = new MainActivityHandler(this);

		findViewById(R.id.yp_scan_button).setOnClickListener(scanListener);
		findViewById(R.id.yp_gallery_button).setOnClickListener(
				chooseImageListener);
		findViewById(R.id.yp_share_button).setOnClickListener(shareListener);
		findViewById(R.id.yp_history_button)
				.setOnClickListener(historyListener);
		findViewById(R.id.yp_help_button).setOnClickListener(helpListener);

		resultView = findViewById(R.id.result_view);
		wellcomeMenuView = findViewById(R.id.yp_wellcome_menu);
	}

	protected Bitmap rescaleBitmap(Uri uri) throws FileNotFoundException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),
				null, options);
		int origWidth = options.outWidth;
		int origHeight = options.outHeight;

		int source = Math.max(origWidth, origHeight);
		int target = 640;
		if (target >= source) {
			target = source;
		}

		int sampleScale = 1;
		while (source / (sampleScale << 1) > target) {
			sampleScale <<= 1;
		}
		// int sampleScale = (int)Math.pow(2d,
		// Math.floor(Math.log((float)source/target)/Math.log(2d)));
		float scale = (float) target / ((float) source / sampleScale);

		Log.d(TAG, "orig " + origWidth + "," + origHeight);
		Log.d(TAG, "scale " + sampleScale + " " + scale);

		/*
		 * Cursor c = managedQuery(uri, new String[]
		 * {MediaStore.Images.Media.DATA}, null, null, null); int ci =
		 * c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		 * c.moveToFirst(); String path = c.getString(ci); Log.d(TAG,
		 * "path "+path);
		 */

		options.inJustDecodeBounds = false;
		options.inSampleSize = sampleScale;
		/*
		 * Bitmap orig = BitmapFactory.decodeStream(
		 * getContentResolver().openInputStream(uri), null, options); Bitmap
		 * orig = BitmapFactory.decodeFile(path, options);
		 */
		ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(uri,
				"r");
		Bitmap orig = BitmapFactory.decodeFileDescriptor(
				fd.getFileDescriptor(), null, options);
		if (orig == null) {
			Log.e(TAG, "bitmap decode failed");
			throw new NullPointerException();
		}
		if (target == source) {
			return orig;
		}

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		Bitmap scaled = Bitmap.createBitmap(orig, 0, 0, orig.getWidth(), orig
				.getHeight(), matrix, true);
		Log.d(TAG, "result " + scaled.getWidth() + "," + scaled.getHeight());
		if (scaled != orig) {
			orig.recycle();
		}
		return scaled;
	}

	private void playBeepSoundAndVibrate() {
		initBeepSound();

		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * Creates the beep MediaPlayer in advance so that the sound can be
	 * triggered with the least latency possible.
	 */
	private void initBeepSound() {
		if (playBeep) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			int sound = Integer.parseInt(prefs.getString(
					PreferencesActivity.KEY_BEEP_SOUND, "1"));
			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.s1);

			if (sound == 1) {
				file = getResources().openRawResourceFd(R.raw.s1);
			} else if (sound == 2) {
				file = getResources().openRawResourceFd(R.raw.s2);
			} else if (sound == 3) {
				file = getResources().openRawResourceFd(R.raw.s3);
			} else if (sound == 4) {
				file = getResources().openRawResourceFd(R.raw.s4);
			}
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too
			// loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file
						.getStartOffset(), file.getLength());
				file.close();
				// mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private static class BeepListener implements OnCompletionListener {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	}
}
