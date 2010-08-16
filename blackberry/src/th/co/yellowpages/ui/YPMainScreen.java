package th.co.yellowpages.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

import th.co.yellowpages.zxing.BinaryBitmap;
import th.co.yellowpages.zxing.DecodeHintType;
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
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class YPMainScreen extends MainScreen {
	public static final int BACKGROUND_COLOR = 32215;
	public static final int HEADER_BACKGROUND_COLOR = 4041982;

	private static final String HEADER_ICON = "yp_icon.png";
	private static final String LOGO = "logo.png";
	private static final String ENCODER_ICON_ONFOCUS = "encoder_icon_onFocus.png";
	private static final String ENCODER_ICON_ONUNFOCUS = "encoder_icon_onUnfocus.png";
	private static final String HELP_ICON_ONFOCUS = "help_icon_onFocus.png";
	private static final String HELP_ICON_ONUNFOCUS = "help_icon_onUnfocus.png";
	private static final String HISTORY_ICON_ONFOCUS = "history_icon_onFocus.png";
	private static final String HISTORY_ICON_ONUNFOCUS = "history_icon_onUnfocus.png";
	private static final String CAMERA_BUTTON_LABEL = "Camera";
	private static final String ALBUM_BUTTON_LABEL = "Album";
	private static final String ENCODER_BUTTON_LABEL = "Encoder";
	private static final String HELP_BUTTON_LABEL = "Help";
	private static final String HISTORY_BUTTON_LABEL = "History";
	private static final int LABEL_FONT_SIZE = 16;

	private final ZXingUiApplication app;
	private final QRCapturedJournalListener imageListener;
	private PopupScreen popup;
	private final Reader reader;
	private final Hashtable readerHints;

	public YPMainScreen() {
		super(DEFAULT_MENU | DEFAULT_CLOSE);

		// Set background color
		Manager manager = this.getMainManager();
		manager.setBackground(BackgroundFactory
				.createSolidBackground(BACKGROUND_COLOR));

		initializeTitle();
		initializeMenu();
		initializeMainScreen();

		app = (ZXingUiApplication) UiApplication.getUiApplication();
		imageListener = new QRCapturedJournalListener(this);

		reader = new MultiFormatReader();
		readerHints = new Hashtable(1);
		readerHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
	}

	/**
	 * Construct the title of screen.
	 */
	private void initializeTitle() {
		VerticalFieldManager titleVerticalField = new VerticalFieldManager(
				USE_ALL_WIDTH);

		titleVerticalField.setBackground(BackgroundFactory
				.createSolidBackground(HEADER_BACKGROUND_COLOR));

		BitmapField headerIcon = new BitmapField(Bitmap
				.getBitmapResource(HEADER_ICON), FIELD_RIGHT);
		headerIcon.setBackground(BackgroundFactory
				.createSolidBackground(BACKGROUND_COLOR));

		LabelField ypLabel = new LabelField("YP", FIELD_RIGHT);

		HorizontalFieldManager titleHorizontalField = new HorizontalFieldManager(
				FIELD_RIGHT);
		titleHorizontalField.setBackground(BackgroundFactory
				.createSolidBackground(BACKGROUND_COLOR));

		XYEdges xyEdgesObj = new XYEdges(5, 5, 5, 5);
		titleHorizontalField.setPadding(xyEdgesObj);
		titleHorizontalField.setMargin(xyEdgesObj);

		titleHorizontalField.add(headerIcon);
		titleHorizontalField.add(ypLabel);

		titleVerticalField.add(titleHorizontalField);
		setTitle(titleVerticalField);
		setChangeListener(null);
	}

	/**
	 * Construct the main screen include custom button.
	 */
	private void initializeMainScreen() {
		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_WIDTH);
		FieldChangeListener buttonListener = new ButtonListener();

		BitmapField logo = new BitmapField(Bitmap.getBitmapResource(LOGO),
				FIELD_HCENTER);
		logo.setMargin(30, 0, 0, 0);
		vfm.add(logo);

		LabelField scanCodeLabel = new LabelField("ScanCode", FIELD_HCENTER) {
			public void paint(Graphics graphics) {
				graphics.setColor(Color.YELLOW);
				super.paint(graphics);
			}
		};

		scanCodeLabel.setMargin(30, 0, 0, 0);
		vfm.add(scanCodeLabel);

		HorizontalFieldManager hfm = new HorizontalFieldManager(FIELD_HCENTER);

		// 0 Camera Button
		ButtonField cameraButton = new ButtonField(CAMERA_BUTTON_LABEL,
				ButtonField.CONSUME_CLICK);
		cameraButton.setChangeListener(buttonListener);

		// 1 Album Button
		ButtonField albumButton = new ButtonField(ALBUM_BUTTON_LABEL,
				ButtonField.CONSUME_CLICK);
		albumButton.setChangeListener(buttonListener);

		hfm.add(cameraButton);
		hfm.add(albumButton);
		vfm.add(hfm);

		FontFamily fontfamily[] = FontFamily.getFontFamilies();
		Font font = fontfamily[0].getFont(FontFamily.SCALABLE_FONT,
				LABEL_FONT_SIZE);
		XYEdges buttonMargin = new XYEdges(10, 10, 5, 10);

		// 2 Encoder Button
		CustomButton encoderButton = new CustomButton(ENCODER_BUTTON_LABEL,
				ENCODER_ICON_ONFOCUS, ENCODER_ICON_ONUNFOCUS,
				ButtonField.CONSUME_CLICK);
		encoderButton.setMargin(buttonMargin);
		LabelField encodeLabel = new LabelField("Encoder", FIELD_HCENTER) {
			public void paint(Graphics graphics) {
				graphics.setColor(Color.WHITE);
				super.paint(graphics);
			}
		};
		encoderButton.setChangeListener(buttonListener);
		encodeLabel.setFont(font);
		VerticalFieldManager vfm1 = new VerticalFieldManager();
		vfm1.add(encoderButton);
		vfm1.add(encodeLabel);

		// 3 History Button
		CustomButton historyButton = new CustomButton(HISTORY_BUTTON_LABEL,
				HISTORY_ICON_ONFOCUS, HISTORY_ICON_ONUNFOCUS,
				ButtonField.CONSUME_CLICK);
		historyButton.setMargin(buttonMargin);
		LabelField historyLabel = new LabelField("History", FIELD_HCENTER) {
			public void paint(Graphics graphics) {
				graphics.setColor(Color.WHITE);
				super.paint(graphics);
			}
		};
		historyButton.setChangeListener(buttonListener);
		historyLabel.setFont(font);
		VerticalFieldManager vfm2 = new VerticalFieldManager();
		vfm2.add(historyButton);
		vfm2.add(historyLabel);

		// 4 Help Button
		CustomButton helpButton = new CustomButton(HELP_BUTTON_LABEL,
				HELP_ICON_ONFOCUS, HELP_ICON_ONUNFOCUS,
				ButtonField.CONSUME_CLICK);
		helpButton.setMargin(buttonMargin);
		LabelField helpLabel = new LabelField("Help", FIELD_HCENTER) {
			public void paint(Graphics graphics) {
				graphics.setColor(Color.WHITE);
				super.paint(graphics);
			}
		};
		helpButton.setChangeListener(buttonListener);
		helpLabel.setFont(font);
		VerticalFieldManager vfm3 = new VerticalFieldManager();
		vfm3.add(helpButton);
		vfm3.add(helpLabel);

		HorizontalFieldManager hfm1 = new HorizontalFieldManager(FIELD_HCENTER);
		hfm1.setMargin(20, 0, 0, 0);

		hfm1.add(vfm1);
		hfm1.add(vfm2);
		hfm1.add(vfm3);

		vfm.add(hfm1);

		vfm.setChangeListener(null);

		add(vfm);
	}

	private void initializeMenu() {
		MenuItem settingMenu = new MenuItem("Setting", 0, 0) {

			public void run() {
				app.pushScreen(new SettingsScreen());
			}
		};

		MenuItem aboutMenu = new MenuItem("About", 1, 0) {

			public void run() {
				app.pushScreen(new AboutScreen());
			}
		};

		addMenuItem(settingMenu);
		addMenuItem(aboutMenu);
	}

	/**
	 * Handles the newly created file. If the file is a jpg image, from the
	 * camera, the images is assumed to be a qrcode and decoding is attempted.
	 */
	void imageSaved(String imagePath) {
		Log.info("Image saved: " + imagePath);
		app.removeFileSystemJournalListener(imageListener);
		if (imagePath.endsWith(".jpg") && imagePath.indexOf("IMG") >= 0) // a
		// blackberry
		// camera
		// image
		// file
		{
			Log.info("imageSaved - Got file: " + imagePath);
			Camera.getInstance().exit();
			Log.info("camera exit finished");
			app.requestForeground();

			DialogFieldManager manager = new DialogFieldManager();
			popup = new PopupScreen(manager);
			manager.addCustomField(new LabelField("Decoding image..."));

			app.pushScreen(popup); // original
			Log.info("started progress screen.");

			Runnable fct = new FileConnectionThread(imagePath);
			Log.info("Starting file connection thread.");
			app.invokeLater(fct);
			Log.info("Finished file connection thread.");
		} else {
			Log.error("Failed to locate camera image.");
		}
	}

	/**
	 * Closes the application and persists all required data.
	 */
	public void close() {
		app.removeFileSystemJournalListener(imageListener);
		DecodeHistory.getInstance().persist();
		super.close();
	}

	/**
	 * This method is overriden to remove the 'save changes' dialog when
	 * exiting.
	 */
	public boolean onSavePrompt() {
		setDirty(false);
		return true;
	}

	/**
	 * Listens for selected buttons and starts the required screen.
	 */
	private final class ButtonListener implements FieldChangeListener {
		public void fieldChanged(Field field, int context) {
			String buttonName = "";
			if (field instanceof ButtonField)
				buttonName = ((ButtonField) field).getLabel();
			else if (field instanceof CustomButton)
				buttonName = ((CustomButton) field).getLabel();
			Log.debug("*** fieldChanged: " + field.getIndex());

			if (buttonName.equals(CAMERA_BUTTON_LABEL)) {
				try {
					app.addFileSystemJournalListener(imageListener);
					Camera.getInstance().invoke(); // start camera
					return;
				} catch (Exception e) {
					Log.error("!!! Problem invoking camera.!!!: " + e);
				}
			} else if (buttonName.equals(ALBUM_BUTTON_LABEL)) {
				// TODO push album screen
			} else if (buttonName.equals(ENCODER_BUTTON_LABEL)) {
				app.pushScreen(new EncoderScreen());
			} else if (buttonName.equals(HISTORY_BUTTON_LABEL)) {
				app.pushScreen(new HistoryScreen());
			} else if (buttonName.equals(HELP_BUTTON_LABEL)) {
				app.pushScreen(new HelpScreen());
			}
		}

	}

	/**
	 * Thread that decodes the newly created image. If the image is successfully
	 * decoded and the data is a URL, the browser is invoked and pointed to the
	 * given URL.
	 */
	private final class FileConnectionThread implements Runnable {

		private final String imagePath;

		private FileConnectionThread(String imagePath) {
			this.imagePath = imagePath;
		}

		public void run() {
			FileConnection file = null;
			InputStream is = null;
			Image capturedImage = null;
			try {
				file = (FileConnection) Connector.open("file://" + imagePath,
						Connector.READ_WRITE);
				is = file.openInputStream();
				capturedImage = Image.createImage(is);
			} catch (IOException e) {
				Log.error("Problem creating image: " + e);
				removeProgressBar();
				invalidate();
				showMessage("An error occured processing the image.");
				return;
			} finally {
				try {
					if (is != null) {
						is.close();
					}
					if (file != null && file.exists()) {
						file.delete();
						if (file.isOpen()) {
							file.close();
						}
						Log.info("Deleted image file.");
					}
				} catch (IOException ioe) {
					Log.error("Error while closing file: " + ioe);
				}
			}

			if (capturedImage != null) {
				Log.info("Got image...");
				LuminanceSource source = new LCDUIImageLuminanceSource(
						capturedImage);
				BinaryBitmap bitmap = new BinaryBitmap(
						new GlobalHistogramBinarizer(source));
				Result result;
				ReasonableTimer decodingTimer = null;
				try {
					decodingTimer = new ReasonableTimer();
					Log.info("Attempting to decode image...");
					result = reader.decode(bitmap, readerHints);
					decodingTimer.finished();
				} catch (ReaderException e) {
					Log.error("Could not decode image: " + e);
					decodingTimer.finished();
					removeProgressBar();
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
					if (isURI(resultText)) {
						resultText = URLDecoder.decode(resultText);
						removeProgressBar();
						invalidate();
						if (!decodingTimer.wasResonableTime()
								&& !AppSettings.getInstance().getBooleanItem(
										AppSettings.SETTING_CAM_RES_MSG)
										.booleanValue()) {
							showMessage("We detected that the decoding process took quite a while. "
									+ "It will be much faster if you decrease your camera's resolution (640x480).");
						}
						DecodeHistory.getInstance().addHistoryItem(
								new DecodeHistoryItem(resultText));
						invokeBrowser(resultText);
						return;
					}
				} else {
					removeProgressBar();
					invalidate();
					showMessage("A QR Code was not found in the image.");
					return;
				}

			}

			removeProgressBar();
			invalidate();
		}

		/**
		 * Quick check to see if the result of decoding the qr code was a valid
		 * uri.
		 */
		private boolean isURI(String uri) {
			return uri.startsWith("http://");
		}

		/**
		 * Invokes the web browser and browses to the given uri.
		 */
		private void invokeBrowser(String uri) {
			BrowserSession browserSession = Browser.getDefaultSession();
			browserSession.displayPage(uri);
		}

		/**
		 * Syncronized version of removing progress dialog. NOTE: All methods
		 * accessing the gui that are in seperate threads should syncronize on
		 * app.getEventLock()
		 */
		private void removeProgressBar() {
			synchronized (app.getAppEventLock()) {
				if (popup != null) {
					app.popScreen(popup);
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
	}
}
