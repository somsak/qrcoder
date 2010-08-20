package th.co.yellowpages.ui;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import th.co.yellowpages.zxing.*;
import th.co.yellowpages.zxing.client.result.ParsedResult;
import th.co.yellowpages.zxing.client.result.ParsedResultType;
import th.co.yellowpages.zxing.client.result.ResultParser;
import th.co.yellowpages.zxing.client.rim.ZXingUiApplication;
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.blackberry.api.mail.Message;
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
				bitmap = YPMainScreen.SizePic(eimg, 60, 60);

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

		ParsedResult resultParser = ResultParser.parseResult(result);
		ParsedResultType type = resultParser.getType();

		String resultText = result.getText();

		removeAllMenuItems();

		if (type.equals(ParsedResultType.URI)) {

			typeLabel.setText("Type: " + type.URI);
			foundLabel.setText("Found URL");
			logoBitmap = Bitmap.getBitmapResource("www_icon.png");
			addMenuItem(new OpenBrowserMenu());
			addMenuItem(new SendEmailMenu(ParsedResultType.URI));
			// addMenuItem(new SendSMSMenu());

		} else if (type.equals(ParsedResultType.EMAIL_ADDRESS)) {

			typeLabel.setText("Type: " + type.EMAIL_ADDRESS);
			foundLabel.setText("Found email address");
			logoBitmap = Bitmap.getBitmapResource("mail_icon.png");
			addMenuItem(new SendEmailMenu(ParsedResultType.EMAIL_ADDRESS));

		} else if (type.equals(ParsedResultType.SMS)) {

		} else if (type.equals(ParsedResultType.TEL)) {

			typeLabel.setText("Type: " + type.TEL);
			foundLabel.setText("Found phone number");
			logoBitmap = Bitmap.getBitmapResource("tel_icon.png");
			resultText = resultText.substring(4, resultText.length());
			addMenuItem(new DialNumberMenu());

		} else {

			typeLabel.setText("Type: " + type.TEXT);
			foundLabel.setText("Found plain text");
			logoBitmap = Bitmap.getBitmapResource("www_icon.png");
			addMenuItem(new WebSearchMenu());
			addMenuItem(new SendEmailMenu(ParsedResultType.TEXT));
			// addMenuItem(new SendSMSMenu());

		}

		BitmapField logoBitmapField = new BitmapField(logoBitmap, FIELD_HCENTER);
		logoBitmapField.setMargin(0, 5, 0, 10);

		HorizontalFieldManager hfm = new HorizontalFieldManager(FIELD_HCENTER);
		hfm.setMargin(5, 0, 5, 0);
		hfm.add(logoBitmapField);
		hfm.add(foundLabel);
		// add focusable to fix scroll bug.
		hfm.add(new LabelField("", FOCUSABLE));

		LabelField resultLabel = new LabelField(resultText, FIELD_HCENTER);
		resultLabel.setMargin(10, 10, 0, 10);

		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_WIDTH);

		vfm.add(bitmapField);
		vfm.add(typeLabel);
		vfm.add(hfm);
		vfm.add(resultLabel);
		// add focusable to fix scroll bug.
		vfm.add(new LabelField("", FOCUSABLE));

		add(vfm);
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
				if (type.equals(ParsedResultType.URI)
						|| type.equals(ParsedResultType.TEXT)) {
					body = result.getText();
				} else if (type.equals(ParsedResultType.EMAIL_ADDRESS)) {
					sendTo = result.getText();
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
			String text = result.getText();
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
			String uri = result.getText();
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
			String phoneNumber = result.getText();
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
}
