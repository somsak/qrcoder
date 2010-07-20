package th.co.yellowpages.javame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
//#ifdef polish.api.mmapi
import javax.microedition.media.control.VideoControl;
//#endif
import javax.microedition.media.control.VolumeControl;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.GlobalHistogramBinarizer;

public final class YPZXingMIDlet extends MIDlet implements PlayerListener {

	protected static final String QRCODE_IMAGE_SAVE_PATH = System
			.getProperty("fileconn.dir.photos");
	protected static final String QRCODER_HISTORY_RECORD_STORE = "QRCodeRecordStore";
	protected static final String QRCODER_SETTING_RECORD_STORE = "QRCodeSettingRecordStore";

	private static final int ALERT_TIMEOUT_MS = 5 * 1000;

	public static final int COMMAND_SAVE = 0;

	private Display display;
//#ifdef polish.api.mmapi
	private Player player;
	private VideoControl videoControl;
//#endif
	private Canvas canvas;
	private Alert confirmation;
	private Alert alert;
	private Menu history;
	private Vector resultHistory;

	public static String PLATFORM = System.getProperty("microedition.platform");

	private static final int SPLASH_TIME = 3000;

	static MultimediaManager buildMultimediaManager() {
//#ifdef polish.api.jsr234
		return new AdvancedMultimediaManager();
//#else
		// Comment line above / uncomment below to make the basic version
		return new DefaultMultimediaManager();
//#endif
	}

//#ifdef polish.api.mmapi
	private static Player createPlayer() throws IOException, MediaException {
		// Try a workaround for Nokias, which want to use capture://image in
		// some cases
		Player player = null;

		if (PLATFORM != null && PLATFORM.indexOf("Nokia") >= 0) {
			try {
				player = Manager.createPlayer("capture://image");
			} catch (MediaException me) {
				// if this fails, just continue with capture://video
			} catch (NullPointerException npe) { // Thanks webblaz... for this
				// improvement:
				// The Nokia 2630 throws this if image/video capture is not
				// supported
				// We should still try to continue
			} catch (Error e) {
				// Ugly, but, it seems the Nokia N70 throws
				// "java.lang.Error: 136" here
				// We should still try to continue
			}
		}
		if (player == null) {
			try {
				player = Manager.createPlayer("capture://video");
			} catch (NullPointerException npe) {
				// The Nokia 2630 throws this if image/video capture is not
				// supported
				throw new MediaException(
						"Image/video capture not supported on this phone");
			}
		}
		return player;
	}
//#endif

	void barcodeAction(ParsedResult result, Displayable backDisplayable) {
		display.setCurrent(new YPQRCodeDetectHandlerForm(result, this,
				backDisplayable));
	}

	protected void destroyApp(boolean unconditional) {
//#ifdef polish.api.mmapi
		if (player != null) {
			videoControl = null;
			try {
				player.stop();
			} catch (MediaException me) {
				// continue
			}
			player.deallocate();
			player.close();
			player = null;
		}
//#endif
	}

	Canvas getCanvas() {
		return canvas;
	}

	Display getDisplay() {
		return display;
	}

//#ifdef polish.api.mmapi
	Player getPlayer() {
		return player;
	}

	VideoControl getVideoControl() {
		return videoControl;
	}
//#endif

	void handleDecodedText(Result theResult, Displayable backDisplayable) {
		ParsedResult result = ResultParser.parseResult(theResult);
		String resultString = result.toString();

		if (result.getType() == ParsedResultType.TEL) {
			addToHistory(resultString, YPRecord.TYPE_TEL);
		} else if (result.getType() == ParsedResultType.EMAIL_ADDRESS) {
			addToHistory(resultString, YPRecord.TYPE_EMAIL);
		} else if (result.getType() == ParsedResultType.SMS) {
			addToHistory(resultString, YPRecord.TYPE_MESSAGE);
		} else if (result.getType() == ParsedResultType.URI) {
			addToHistory(resultString, YPRecord.TYPE_WEB);
		} else {
			addToHistory(resultString, YPRecord.TYPE_TEXT);
		}

		barcodeAction(result, backDisplayable);
	}

	void historyRequest() {
		Display.getDisplay(this).setCurrent(history);
	}

	void itemRequest(Displayable backDisplayable) {
		ParsedResult result = (ParsedResult) resultHistory.elementAt(history
				.getSelectedIndex());
		barcodeAction(result, backDisplayable);
	}

	protected void pauseApp() {
//#ifdef polish.api.mmapi
		if (player != null) {
			try {
				player.stop();
			} catch (MediaException me) {
				// continue?
				showError(me);
			}
		}
//#endif
	}

	private void showAlert(Alert alert) {
		Display display = Display.getDisplay(this);
		display.setCurrent(alert);
	}

	void showGenerateHistoryForm() {
		display.setCurrent(new YPHistoryForm(this));
	}

	void showCreateQRCanvas() {
		display.setCurrent(new YPCreateQRForm(this));
	}

	void showError(String message) {
		alert.setTitle("Error");
		alert.setString(message);
		alert.setType(AlertType.ERROR);
		showAlert(alert);
	}

	void showError(Throwable t) {
		String message = t.getMessage();
		if (message != null && message.length() > 0) {
			showError(message);
		} else {
			showError(t.toString());
		}
	}

	void showHistoryCanvas() {
	}

	void showMainCanvas() {
		display.setCurrent(new MainCanvas(this));
	}

	void showMainForm() {
		display.setCurrent(new YPMainForm(this));
	}

	void showSetting() {
		display.setCurrent(new YPSetting(this));
	}

//#ifdef polish.api.mmapi
	void showVideoCanvas() {
		try {
			player = createPlayer();
			player.realize();

			MultimediaManager multimediaManager = buildMultimediaManager();
			multimediaManager.setZoom(player);
			multimediaManager.setExposure(player);
			multimediaManager.setFlash(player);
			videoControl = (VideoControl) player.getControl("VideoControl");
			canvas = new VideoCanvas(this);
			canvas.setFullScreenMode(true);
			videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, canvas);
			videoControl.setDisplayLocation(0, 0);
			videoControl.setDisplaySize(canvas.getWidth(), canvas.getHeight());
			player.start();
			videoControl.setVisible(true);
			display.setCurrent(canvas);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MediaException e) {
			e.printStackTrace();
		}
	}
//#endif

	protected void startApp() throws MIDletStateChangeException {
		System.out.println("Platform: " + PLATFORM);

		display = Display.getDisplay(this);

		Thread splashThread = new Thread(new YPSplash(this));
		splashThread.start();

		try {
			splashThread.sleep(SPLASH_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		display.setCurrent(new YPMainForm(this));

		resultHistory = new Vector(5);
		history = new Menu(this, "Scan History", "Use");

		// Set up one confirmation and alert object to re-use
		confirmation = new Alert(null);
		confirmation.setType(AlertType.CONFIRMATION);
		confirmation.setTimeout(ALERT_TIMEOUT_MS);
		Command yes = new Command("Yes", Command.OK, 1);
		confirmation.addCommand(yes);
		Command no = new Command("No", Command.CANCEL, 1);
		confirmation.addCommand(no);
		alert = new Alert(null);
		alert.setTimeout(ALERT_TIMEOUT_MS);
	}

	void stop() {
		destroyApp(false);
		notifyDestroyed();
	}

	static Result decode(Image image) {
		Result result = null;
		try {
			LuminanceSource source = new LCDUIImageLuminanceSource(image);
			BinaryBitmap bitmap = new BinaryBitmap(
					new GlobalHistogramBinarizer(source));
			Reader reader = new MultiFormatReader();
			result = reader.decode(bitmap);

			System.out.println("Decode result: " + result.toString());
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ChecksumException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * This methog resizes an image by resampling its pixels
	 * 
	 * @param src
	 *            The image to be resized
	 * @return The resized image
	 */
	public static Image resizeImage(Image src, int width, int height) {
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();
		Image tmp = Image.createImage(width, srcHeight);
		Graphics g = tmp.getGraphics();
		int ratio = (srcWidth << 16) / width;
		int pos = ratio / 2;

		// Horizontal Resize
		for (int x = 0; x < width; x++) {
			g.setClip(x, 0, 1, srcHeight);
			g.drawImage(src, x - (pos >> 16), 0, Graphics.LEFT | Graphics.TOP);
			pos += ratio;
		}

		Image resizedImage = Image.createImage(width, height);
		g = resizedImage.getGraphics();
		ratio = (srcHeight << 16) / height;
		pos = ratio / 2;

		// Vertical resize
		for (int y = 0; y < height; y++) {
			g.setClip(0, y, width, 1);
			g.drawImage(tmp, 0, y - (pos >> 16), Graphics.LEFT | Graphics.TOP);
			pos += ratio;
		}

		return resizedImage;
	}

	public void showChooseImageForm() {
		display.setCurrent(new YPAlbumForm(this));
	}

	public void addToHistory(String qrContent, String type) {
		try {
			RecordStore rs = RecordStore.openRecordStore(
					YPZXingMIDlet.QRCODER_HISTORY_RECORD_STORE, true);
			YPRecord record = new YPRecord(qrContent, type);

			rs.addRecord(record.toByteArray(), 0, record.toByteArray().length);
			rs.closeRecordStore();
		} catch (RecordStoreFullException e) {
			e.printStackTrace();
		} catch (RecordStoreNotFoundException e) {
			e.printStackTrace();
		} catch (RecordStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void playBeeb(int soundId) {
		int curVolume = 100;

		Player musicPlayer;

		try {
			InputStream is = null;
			System.out.println("SoundId = " + soundId);

			if (soundId == 0) {
				is = getClass().getResourceAsStream("/1.wav");
			} else if (soundId == 1) {
				is = getClass().getResourceAsStream("/2.wav");
			} else if (soundId == 2) {
				is = getClass().getResourceAsStream("/3.wav");
			} else if (soundId == 3) {
				is = getClass().getResourceAsStream("/4.wav");
			} else {
				return;
			}

			musicPlayer = Manager.createPlayer(is, "audio/X-wav");
			musicPlayer.realize();

			// add player listener to access sound events
			musicPlayer.addPlayerListener(this);

			// The set occurs twice to prevent sound spikes at the very
			// beginning of the sound.
			VolumeControl volumeControl = (VolumeControl) musicPlayer
					.getControl("VolumeControl");
			volumeControl.setLevel(curVolume);

			// finally start the piece of music
			musicPlayer.realize();
			musicPlayer.start();

			// set the volume once more
			volumeControl = (VolumeControl) musicPlayer
					.getControl("VolumeControl");
			volumeControl.setLevel(curVolume);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MediaException e) {
			e.printStackTrace();
		}
	}

	public void playerUpdate(Player arg0, String arg1, Object arg2) {

	}
}
