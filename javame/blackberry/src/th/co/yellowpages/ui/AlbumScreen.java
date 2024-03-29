package th.co.yellowpages.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import th.co.yellowpages.zxing.BinaryBitmap;
import th.co.yellowpages.zxing.DecodeHintType;
import th.co.yellowpages.zxing.LuminanceSource;
import th.co.yellowpages.zxing.MultiFormatReader;
import th.co.yellowpages.zxing.NotFoundException;
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

import com.blackberry.toolkit.ui.component.ThumbnailField;
import com.blackberry.toolkit.ui.component.ThumbnailField.Listener;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;

public class AlbumScreen extends MainScreen {

	private ThumbnailField thumbs = null;
	private boolean selectable = true;
	private ZXingUiApplication app;

	public AlbumScreen() {
		LabelField title = new LabelField("QRCoder", DrawStyle.ELLIPSIS
				| USE_ALL_WIDTH);
		title.setMargin(5, 0, 0, 5);
		setTitle(title);

		addMenuItem(new InternalMenu());
		addMenuItem(new SDCardMenu());

		app = (ZXingUiApplication) UiApplication.getUiApplication();

		try {

			FileConnection dir = (FileConnection) Connector.open(System
					.getProperty("fileconn.dir.memorycard.photos"),
					Connector.READ);
			if (dir.exists()) {
				System.out.println("SDCARD");
				thumbs = ThumbnailField.createFromDirectory(
						Manager.VERTICAL_SCROLL | Manager.USE_ALL_WIDTH
								| Manager.USE_ALL_HEIGHT,
						ThumbnailField.DEFAULT_COLUMNS,
						ThumbnailField.DEFAULT_PADDING, Display.getWidth(),
						selectable, System
								.getProperty("fileconn.dir.memorycard.photos"),
						true);
				setupListener(thumbs, selectable);
				add(thumbs);
			}
			dir.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: No SDcard");
			try {
				thumbs = ThumbnailField.createFromDirectory(
						Manager.VERTICAL_SCROLL | Manager.USE_ALL_WIDTH
								| Manager.USE_ALL_HEIGHT,
						ThumbnailField.DEFAULT_COLUMNS,
						ThumbnailField.DEFAULT_PADDING, Display.getWidth(),
						selectable, System.getProperty("fileconn.dir.photos"),
						true);
				setupListener(thumbs, selectable);
				add(thumbs);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
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

	private void removeAlbumScreen() {
		UiEngine ui = Ui.getUiEngine();
		ui.popScreen(this);
	}

	private class SDCardMenu extends MenuItem {
		public SDCardMenu() {
			super("SDCard", 0, 100);
		}

		public void run() {
			try {
				AlbumScreen.this.deleteAll();
				thumbs = ThumbnailField.createFromDirectory(
						Manager.VERTICAL_SCROLL | Manager.USE_ALL_WIDTH
								| Manager.USE_ALL_HEIGHT,
						ThumbnailField.DEFAULT_COLUMNS,
						ThumbnailField.DEFAULT_PADDING, Display.getWidth(),
						selectable, System
								.getProperty("fileconn.dir.memorycard.photos"),
						true);
				setupListener(thumbs, selectable);
				AlbumScreen.this.add(thumbs);
			} catch (Throwable t) {
				t.printStackTrace();
			}

		}
	}

	private class InternalMenu extends MenuItem {
		public InternalMenu() {
			super("Internal", 0, 100);
		}

		public void run() {
			try {
				AlbumScreen.this.deleteAll();
				thumbs = ThumbnailField.createFromDirectory(
						Manager.VERTICAL_SCROLL | Manager.USE_ALL_WIDTH
								| Manager.USE_ALL_HEIGHT,
						ThumbnailField.DEFAULT_COLUMNS,
						ThumbnailField.DEFAULT_PADDING, Display.getWidth(),
						selectable, System.getProperty("fileconn.dir.photos"),
						true);
				setupListener(thumbs, selectable);
				AlbumScreen.this.add(thumbs);
			} catch (Throwable t) {
				t.printStackTrace();
			}

		}
	}

	private class ThumbnailListener implements Listener {
		public void thumbnailSelected(final Bitmap thumbnail, int index,
				String filename) {

			FileConnection file = null;
			InputStream is = null;
			Image selectedImage = null;
			Result result = null;

			try {
				file = (FileConnection) Connector
						.open(filename, Connector.READ);
				is = file.openInputStream();
				selectedImage = Image.createImage(is);
			} catch (IOException e) {
				invalidate();
				showMessage("An error occured processing the image.");
				return;
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException ioe) {
				}
			}

			if (selectedImage != null) {
				Log.info("Got image...");
				MultiFormatReader reader = new MultiFormatReader();
				LuminanceSource source = new LCDUIImageLuminanceSource(
						selectedImage);
				BinaryBitmap bitmap = new BinaryBitmap(
						new GlobalHistogramBinarizer(source));

				ReasonableTimer decodingTimer = null;
				try {
					decodingTimer = new ReasonableTimer();
					Log.info("Attempting to decode image...");

					Hashtable hints = new Hashtable(1);
					hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

					result = reader.decode(bitmap, hints);
					decodingTimer.finished();
				} catch (ReaderException e) {
					Log.error("Could not decode image: " + e);
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
					invalidate();

					if (!decodingTimer.wasResonableTime()
							&& !AppSettings.getInstance().getBooleanItem(
									AppSettings.SETTING_CAM_RES_MSG)
									.booleanValue()) {
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
						DecodeHistoryItem decodeHistoryItem = new DecodeHistoryItem(
								resultText);
						DecodeHistory.getInstance().addHistoryItem(
								decodeHistoryItem);
					}

					app.pushScreen(new ResultScreen(result, filename));

					invalidate();
				}
			}

		}

		public void selectionCanceled() {

		}

	}

	private void setupListener(ThumbnailField thumbs, boolean selectable) {
		if (selectable) {
			thumbs.setListener(new ThumbnailListener());
		}
	}
}
