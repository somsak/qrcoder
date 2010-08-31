package th.co.yellowpages.zxing.client.ypandroid;

import com.google.zxing.Result;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public final class MainActivityHandler extends Handler {
	private final MainActivity activity;

	MainActivityHandler(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		Bundle bundle = message.getData();
		Bitmap barcode = bundle == null ? null : (Bitmap) bundle
				.getParcelable(DecodeThread.BARCODE_BITMAP);
		activity.handleDecode((Result) message.obj, barcode);
	}
}
