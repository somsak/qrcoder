package th.co.yellowpages.ui;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Image;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import th.co.yellowpages.zxing.BinaryBitmap;
import th.co.yellowpages.zxing.DecodeHintType;
import th.co.yellowpages.zxing.EncodeHintType;
import th.co.yellowpages.zxing.LuminanceSource;
import th.co.yellowpages.zxing.MultiFormatReader;
import th.co.yellowpages.zxing.Reader;
import th.co.yellowpages.zxing.ReaderException;
import th.co.yellowpages.zxing.Result;
import th.co.yellowpages.zxing.client.rim.ZXingUiApplication;
import th.co.yellowpages.zxing.client.rim.persistence.AppSettings;
import th.co.yellowpages.zxing.client.rim.persistence.history.DecodeHistory;
import th.co.yellowpages.zxing.client.rim.persistence.history.DecodeHistoryItem;
import th.co.yellowpages.zxing.client.rim.util.Log;
import th.co.yellowpages.zxing.client.rim.util.ReasonableTimer;
import th.co.yellowpages.zxing.client.rim.util.URLDecoder;
import th.co.yellowpages.zxing.common.GlobalHistogramBinarizer;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;

public class CameraScreen extends MainScreen {

	private Player _p;
	private Field _videoField;
	private VideoControl _videoControl;
	private ZXingUiApplication app;

	public CameraScreen() {

		app = (ZXingUiApplication) UiApplication.getUiApplication();

		try {
			_p = javax.microedition.media.Manager
					.createPlayer("capture://video?encoding=jpeg&width=640&height=480");

			_p.realize();
			_videoControl = (VideoControl) _p.getControl("VideoControl");

			if (_videoControl != null) {
				_videoField = (Field) _videoControl.initDisplayMode(
						VideoControl.USE_GUI_PRIMITIVE,
						"net.rim.device.api.ui.Field");
				_videoControl.setDisplayFullScreen(true);
				_videoControl.setVisible(true);
				_p.start();
			}

			if (_videoField != null) {
				add(_videoField);
			}

		} catch (IOException e) {
			Dialog.alert(e.toString());
		} catch (MediaException e) {
			Dialog.alert(e.toString());
		}

	}

	protected boolean invokeAction(int action) {
		boolean handled = super.invokeAction(action);

		if (!handled) {
			if (action == ACTION_INVOKE) {
				try {
					byte[] rawImage = _videoControl.getSnapshot(null);
					System.out.println("ACTION: " + rawImage.length);
					decodeImage(rawImage);
				} catch (Exception e) {
					_videoField.setFocus();
					Dialog.alert(e.toString());
				}
			}
		}
		return true;
	}

	private void decodeImage(byte[] imageData) {
		if (imageData == null)
			return;

		Log.info("Got image...");

		Image image = Image.createImage(imageData, 0, imageData.length);
		LuminanceSource source = new LCDUIImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(
				source));
		Result result;
		ReasonableTimer decodingTimer = null;

		try {
			decodingTimer = new ReasonableTimer();
			Log.info("Attempting to decode image...");
			Reader reader = new MultiFormatReader();

			Hashtable hints = new Hashtable(1);
			hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

			result = reader.decode(bitmap, hints);
			decodingTimer.finished();
		} catch (ReaderException e) {
			Log.error("Could not decode image: " + e);
			System.out.println("Could not decode image: " + e.getMessage());
			decodingTimer.finished();
			invalidate();

			boolean showResolutionMsg = !AppSettings.getInstance()
					.getBooleanItem(AppSettings.SETTING_CAM_RES_MSG)
					.booleanValue();
			if (showResolutionMsg) {
				showMessage("A QR Code was not found in the image. "
						+ "We detected that the decoding process took quite a while. "
						+ "It will be much faster if you decrease your camera's resolution (640x480).");
			} else {
				showMessage("A QR Code was not found in the image.");
			}

			return;
		}

		if (result != null) {
			String resultText = result.getText();
			Log.info("result: " + resultText);
			resultText = URLDecoder.decode(resultText);
			invalidate();
			if (!decodingTimer.wasResonableTime()
					&& !AppSettings.getInstance().getBooleanItem(
							AppSettings.SETTING_CAM_RES_MSG).booleanValue()) {
				showMessage("We detected that the decoding process took quite a while. "
						+ "It will be much faster if you decrease your camera's resolution (640x480).");
			}

			boolean isDuplicate = false;

			DecodeHistory history = DecodeHistory.getInstance();
			for (int i = 0; i < history.getNumItems(); i++) {
				DecodeHistoryItem item = history.getItemAt(i);
				if (item.getContent().equals(resultText)) {
					isDuplicate = true;
					break;
				}
			}

			if (isDuplicate == false) {
				DecodeHistory.getInstance().addHistoryItem(
						new DecodeHistoryItem(resultText));
			}

			try {
				_p.stop();
				_p.deallocate();
			} catch (Exception e) {
				Dialog.alert(e.toString());
			}

			app.popScreen(this);
			app.pushScreen(new ResultScreen(result, imageData));

			return;
		} else {
			invalidate();
			showMessage("A QR Code was not found in the image.");
			return;
		}
	}

	/**
	 * Syncronized version of showing a message dialog. NOTE: All methods
	 * accessing the gui that are in seperate threads should syncronize on
	 * app.getEventLock()
	 */
	private void showMessage(String message) {
		synchronized (app.getAppEventLock()) {
			Dialog.alert(message);
		}
	}
}
