package th.co.yellowpages.ui;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import th.co.yellowpages.util.YPLog;
import th.co.yellowpages.zxing.*;
import th.co.yellowpages.zxing.client.result.ParsedResult;
import th.co.yellowpages.zxing.client.result.ParsedResultType;
import th.co.yellowpages.zxing.client.result.ResultParser;
import th.co.yellowpages.zxing.client.rim.ZXingUiApplication;
import th.co.yellowpages.zxing.client.rim.persistence.AppSettings;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.PINAddress;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.BorderFactory;

public class ResultScreen extends MainScreen {

	private static final int TYPE_LABEL_FONT_SIZE = 16;
	private static final int FOUND_LABEL_FONT_SIZE = 19;

	private ZXingUiApplication app;
	private Result result;
	private ParsedResult resultParser;
	private Bitmap bitmap;

	public ResultScreen(Result result, String filename) {

		this.result = result;

		LabelField title = new LabelField("QRCoder", DrawStyle.ELLIPSIS
				| USE_ALL_WIDTH);
		title.setMargin(5, 0, 0, 5);
		setTitle(title);

		app = (ZXingUiApplication) UiApplication.getUiApplication();

		FileConnection fc;
		try {
			fc = (FileConnection) Connector.open(filename);

			if (fc.exists()) {
				byte[] image = new byte[(int) fc.fileSize()];
				InputStream inStream = fc.openInputStream();
				inStream.read(image);
				inStream.close();

				EncodedImage eimg = EncodedImage.createEncodedImage(image, 0,
						-1);
				bitmap = YPMainScreen.scaleImage(eimg, 60, 60);

				beepSound();

				initializeResultScreen();
			}

		} catch (IOException e) {
			showMessage(e.getMessage());
		}

	}

	public ResultScreen(Result result, Bitmap bitmap) {
		this.result = result;
		this.bitmap = bitmap;

		LabelField title = new LabelField("QRCoder", DrawStyle.ELLIPSIS
				| USE_ALL_WIDTH);
		title.setMargin(5, 0, 0, 5);
		setTitle(title);

		app = (ZXingUiApplication) UiApplication.getUiApplication();

		initializeResultScreen();

	}

	public ResultScreen(Result result, byte[] imageData) {
		this.result = result;

		LabelField title = new LabelField("QRCoder", DrawStyle.ELLIPSIS
				| USE_ALL_WIDTH);
		title.setMargin(5, 0, 0, 5);
		setTitle(title);

		app = (ZXingUiApplication) UiApplication.getUiApplication();

		EncodedImage eimg = EncodedImage.createEncodedImage(imageData, 0, -1);
		bitmap = YPMainScreen.scaleImage(eimg, 60, 60);

		beepSound();

		initializeResultScreen();

	}

	private void initializeResultScreen() {
		FontFamily fontfamily[] = FontFamily.getFontFamilies();

		BitmapField bitmapField = new BitmapField(bitmap, FIELD_HCENTER);
		bitmapField.setMargin(10, 0, 0, 0);

		Bitmap logoBitmap = null;

		LabelField typeLabel = new LabelField("", FIELD_HCENTER);
		typeLabel.setFont(fontfamily[0].getFont(FontFamily.SCALABLE_FONT,
				TYPE_LABEL_FONT_SIZE));
		typeLabel.setMargin(5, 0, 0, 0);

		LabelField foundLabel = new LabelField("", FIELD_HCENTER
				| FIELD_VCENTER);
		foundLabel.setBackground(BackgroundFactory
				.createSolidBackground(Color.WHITE));
		foundLabel.setFont(fontfamily[0].getFont(FontFamily.SCALABLE_FONT,
				FOUND_LABEL_FONT_SIZE));
		foundLabel.setBorder(BorderFactory.createSimpleBorder(new XYEdges(0, 0,
				1, 0)));
		foundLabel.setMargin(0, 5, 0, 10);

		resultParser = ResultParser.parseResult(result);
		ParsedResultType type = resultParser.getType();

		removeAllMenuItems();

		if (type.equals(ParsedResultType.URI)) {

			typeLabel.setText("Type: " + type.URI);
			foundLabel.setText("Found URL");
			logoBitmap = Bitmap.getBitmapResource("www_icon.png");
			addMenuItem(new OpenBrowserMenu());
			addMenuItem(new SendEmailMenu(ParsedResultType.URI));
			addMenuItem(new SendPINMenu());

		} else if (type.equals(ParsedResultType.EMAIL_ADDRESS)) {

			typeLabel.setText("Type: " + type.EMAIL_ADDRESS);
			foundLabel.setText("Found email address");
			logoBitmap = Bitmap.getBitmapResource("mail_icon.png");
			addMenuItem(new SendEmailMenu(ParsedResultType.EMAIL_ADDRESS));
			addMenuItem(new SendPINMenu());

		} else if (type.equals(ParsedResultType.ADDRESSBOOK)) {

			typeLabel.setText("Type: " + type.ADDRESSBOOK);
			foundLabel.setText("Found contact info");
			logoBitmap = Bitmap.getBitmapResource("tel_icon.png");
			addMenuItem(new SendEmailMenu(ParsedResultType.ADDRESSBOOK));
			addMenuItem(new SendPINMenu());

		} else if (type.equals(ParsedResultType.TEL)) {

			typeLabel.setText("Type: " + type.TEL);
			foundLabel.setText("Found phone number");
			logoBitmap = Bitmap.getBitmapResource("tel_icon.png");
			addMenuItem(new DialNumberMenu());
			addMenuItem(new SendEmailMenu(ParsedResultType.TEL));
			addMenuItem(new SendPINMenu());

		} else {

			typeLabel.setText("Type: " + type.TEXT);
			foundLabel.setText("Found plain text");
			logoBitmap = Bitmap.getBitmapResource("www_icon.png");
			addMenuItem(new WebSearchMenu());
			addMenuItem(new SendEmailMenu(ParsedResultType.TEXT));
			addMenuItem(new SendPINMenu());

		}

		BitmapField logoBitmapField = new BitmapField(logoBitmap, FIELD_HCENTER);
		logoBitmapField.setMargin(0, 5, 0, 10);

		HorizontalFieldManager hfm = new HorizontalFieldManager(FIELD_HCENTER);
		hfm.setMargin(5, 0, 5, 0);
		hfm.add(logoBitmapField);
		hfm.add(foundLabel);
		// add focusable to fix scroll bug.
		hfm.add(new LabelField("", FOCUSABLE));

		LabelField resultLabel = new LabelField(
				resultParser.getDisplayResult(), FIELD_HCENTER | FIELD_VCENTER);
		resultLabel.setMargin(10, 10, 0, 10);

		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_WIDTH);

		vfm.add(bitmapField);
		vfm.add(typeLabel);
		vfm.add(hfm);
		vfm.add(resultLabel);
		// add focusable to fix scroll bug.
		vfm.add(new LabelField("", FOCUSABLE));

		add(vfm);

		YPLog log = new YPLog(resultParser.getDisplayResult());
		Thread thread = new Thread(log);
		thread.start();
	}

	private void beepSound() {
		Boolean isSoundEnable = AppSettings.getInstance().getBooleanItem(
				AppSettings.SETTING_ENABLE_DISABLE_BEEP_SOUND);

		if (isSoundEnable != null && isSoundEnable.booleanValue() == true) {

			Integer soundInt = AppSettings.getInstance().getIntegerItem(
					AppSettings.SETTING_BEEP_SOUND);

			if (soundInt == null)
				soundInt = new Integer(0);

			YPMainScreen.playBeeb(soundInt.intValue());
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

	private class SendSMSMenu extends MenuItem {
		public SendSMSMenu() {
			super("Send SMS", 0, 100);
		}

		public void run() {

		}
	}

	private class SendEmailMenu extends MenuItem {
		private ParsedResultType type;

		public SendEmailMenu(ParsedResultType type) {
			super("Send Email", 0, 100);
			this.type = type;
		}

		public void run() {
			try {
				String sendTo = "";
				String body = "";

				if (type.equals(ParsedResultType.EMAIL_ADDRESS)) {
					sendTo = resultParser.getDisplayResult();
				} else {
					body = resultParser.getDisplayResult();
				}

				MessageArguments messageArg = new MessageArguments(
						MessageArguments.ARG_NEW, sendTo, "", body);
				Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, messageArg);
			} catch (Exception e) {
			}
		}
	}

	private class WebSearchMenu extends MenuItem {
		public WebSearchMenu() {
			super("Web search", 0, 100);
		}

		public void run() {
			String text = resultParser.getDisplayResult();
			if (text == null)
				return;
			String uri = "http://www.google.com/search?q=" + text.trim();
			BrowserSession browserSession = Browser.getDefaultSession();
			browserSession.displayPage(uri);
		}
	}

	private class OpenBrowserMenu extends MenuItem {
		public OpenBrowserMenu() {
			super("Open browser", 0, 100);
		}

		public void run() {
			String uri = resultParser.getDisplayResult();
			if (uri == null)
				return;

			BrowserSession browserSession = Browser.getDefaultSession();
			browserSession.displayPage(uri.trim());
		}
	}

	private class DialNumberMenu extends MenuItem {
		public DialNumberMenu() {
			super("Dial number", 0, 100);
		}

		public void run() {
			String phoneNumber = resultParser.getDisplayResult();
			if (phoneNumber == null)
				return;
			try {
				PhoneArguments call = new PhoneArguments(
						PhoneArguments.ARG_CALL, phoneNumber.trim());

				Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, call);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private class SendPINMenu extends MenuItem {
		public SendPINMenu() {
			super("Send PIN", 0, 100);
		}

		public void run() {
			String text = resultParser.getDisplayResult();
			if (text == null)
				return;

			try {
				MessageArguments messageArg = new MessageArguments(
						MessageArguments.ARG_NEW_PIN, "", "", text);

				Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, messageArg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
