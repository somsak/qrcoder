package th.co.yellowpages.ui;

import java.io.*;
import java.util.Date;

import javax.microedition.io.*;
import javax.microedition.io.file.*;

import th.co.yellowpages.javame.PNGEncoder;
import th.co.yellowpages.ui.component.*;
import th.co.yellowpages.zxing.BarcodeFormat;
import th.co.yellowpages.zxing.MultiFormatWriter;
import th.co.yellowpages.zxing.Result;
import th.co.yellowpages.zxing.WriterException;
import th.co.yellowpages.zxing.client.result.ParsedResult;
import th.co.yellowpages.zxing.client.result.ParsedResultType;
import th.co.yellowpages.zxing.client.result.ResultParser;
import th.co.yellowpages.zxing.common.BitMatrix;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class EncoderScreen extends MainScreen {

	private static final String[] encodeType = { "Text", "Email address",
			"Phone number", "URL" };
	private static final int BACK = 0xFF000000;
	private static final int WHTIE = 0xFFFFFFFF;

	private ObjectChoiceField encodingTypeChoiceField;
	private VerticalFieldManager bodyVfm;

	private byte[] pngByte = null;
	private Bitmap bitmap;
	private LabelField title;

	public EncoderScreen() {
		LabelField title = new LabelField("QRCoder", DrawStyle.ELLIPSIS
				| USE_ALL_WIDTH);
		title.setMargin(5, 0, 0, 5);
		setTitle(title);

		initializeEncoderScreen();
		initializeText();
	}

	private void initializeEncoderScreen() {
		encodingTypeChoiceField = new ObjectChoiceField("Select encode type:",
				encodeType);
		encodingTypeChoiceField.setChangeListener(new ButtonListener(this));
		add(encodingTypeChoiceField);

		bodyVfm = new VerticalFieldManager(FIELD_HCENTER | USE_ALL_WIDTH);
		bodyVfm.setMargin(5, 5, 5, 5);
		add(bodyVfm);

		ButtonField createQRCodeButton = new ButtonField("Create QR Code",
				FIELD_HCENTER | USE_ALL_WIDTH | ButtonField.CONSUME_CLICK);
		createQRCodeButton.setChangeListener(new ButtonListener(this));
		add(createQRCodeButton);
	}

	private void ClearBodyVFM() {
		if (bodyVfm != null)
			bodyVfm.deleteAll();
	}

	private void initializeText() {
		BorderedEditField textField = new BorderedEditField(
				BasicEditField.FILTER_DEFAULT | USE_ALL_WIDTH | EDITABLE);
		bodyVfm.add(textField);
	}

	private void initializeEmail() {
		BorderedEditField emailField = new BorderedEditField(
				BasicEditField.FILTER_EMAIL | USE_ALL_WIDTH | EDITABLE);
		bodyVfm.add(emailField);
	}

	private void initializePhone() {
		BorderedEditField phoneField = new BorderedEditField(
				BasicEditField.FILTER_PHONE | USE_ALL_WIDTH | EDITABLE);
		bodyVfm.add(phoneField);
	}

	private void initializeURL() {
		BorderedEditField urlField = new BorderedEditField(
				BasicEditField.FILTER_URL | USE_ALL_WIDTH | EDITABLE);
		bodyVfm.add(urlField);
	}

	private void writeFile(String filePath) {

		FileConnection fileConnection = null;
		OutputStream outputStream = null;
		try {
			fileConnection = (FileConnection) Connector.open(filePath,
					Connector.READ_WRITE);
			if (!fileConnection.exists()) {
				fileConnection.create();
			}
			outputStream = fileConnection.openOutputStream();

			outputStream.write(pngByte);
			outputStream.close();
			fileConnection.close();

			Dialog alert = new Dialog(Dialog.D_OK, "QR Code saved", Dialog.OK,
					null, Manager.VERTICAL_SCROLL);
			alert.show();

		} catch (Exception e) {
			Dialog alert = new Dialog(Dialog.D_OK, e.getMessage(), Dialog.OK,
					Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),
					Manager.VERTICAL_SCROLL);
			alert.show();
		} finally {
			try {
				if (fileConnection != null)
					fileConnection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (outputStream != null)
					outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Listens for button clicks and executes the appropriate action.
	 */
	private final class ButtonListener implements FieldChangeListener {

		private final Screen screen;

		private ButtonListener(Screen screen) {
			this.screen = screen;
		}

		public void fieldChanged(Field field, int context) {

			if (field instanceof ObjectChoiceField) {
				int selected = encodingTypeChoiceField.getSelectedIndex();
				switch (selected) {
				case 0:
					ClearBodyVFM();
					initializeText();
					break;
				case 1:
					ClearBodyVFM();
					initializeEmail();
					break;
				case 2:
					ClearBodyVFM();
					initializePhone();
					break;
				case 3:
					ClearBodyVFM();
					initializeURL();
					break;
				}
			} else if (field instanceof ButtonField) {

				Field temp = bodyVfm.getField(0);

				if (temp instanceof BorderedEditField) {
					BorderedEditField editField = (BorderedEditField) temp;
					String content = editField.getText().trim();

					if (content.equals("")) {
						Dialog alert = new Dialog(
								Dialog.D_OK,
								"Could not encode a barcode from the data provided.",
								Dialog.OK,
								Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION),
								Manager.VERTICAL_SCROLL);
						alert.show();
						return;
					} else {
						int selected = encodingTypeChoiceField
								.getSelectedIndex();
						// if select phone add tel: to content
						if (selected == 2)
							content = "tel:" + content;

						Result result = new Result(content, null, null, null);
						ParsedResult resultParser = ResultParser
								.parseResult(result);
						ParsedResultType type = resultParser.getType();

						boolean isInvalid = false;
						String errorText = null;
						// check invalid syntax
						if (selected == 1
								&& !type.equals(ParsedResultType.EMAIL_ADDRESS)) {
							isInvalid = true;
							errorText = "Invalid Email address syntax.";
						} else if (selected == 2
								&& !type.equals(ParsedResultType.TEL)) {
							isInvalid = true;
							errorText = "Invalid Phone number syntax.";
						} else if (selected == 3
								&& !type.equals(ParsedResultType.URI)) {
							isInvalid = true;
							errorText = "Invalid URL syntax.";
						}

						if (isInvalid == true && errorText != null) {
							Dialog alert = new Dialog(
									Dialog.D_OK,
									errorText,
									Dialog.OK,
									Bitmap
											.getPredefinedBitmap(Bitmap.EXCLAMATION),
									Manager.VERTICAL_SCROLL);
							alert.show();
							return;
						}

						GenerateQRCode(content);
					}
				}

			}
		}

		private void GenerateQRCode(String content) {
			MultiFormatWriter writer = new MultiFormatWriter();
			try {
				int target = Math.min(getHeight(), getWidth());
				int qrWidth = target;
				int qrHeigth = qrWidth;

				BitMatrix qrBitMatrix = writer.encode(content,
						BarcodeFormat.QR_CODE, qrWidth, qrHeigth);

				int[] rgb = new int[qrWidth * qrHeigth];

				for (int y = 0; y < qrBitMatrix.getHeight(); y++) {
					for (int x = 0; x < qrWidth; x++) {
						int offset = y * qrHeigth;
						rgb[offset + x] = qrBitMatrix.get(x, y) ? BACK : WHTIE;
					}
				}
				pngByte = PNGEncoder.toPNG(qrWidth, qrHeigth, rgb, true);

				if (pngByte == null)
					return;

				bitmap = Bitmap.createBitmapFromPNG(pngByte, 0, -1);
				BitmapField bitmapField = new BitmapField(bitmap, FIELD_HCENTER|FOCUSABLE);

				screen.deleteAll();
				
				VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_WIDTH);
				
				// add focusable to fix scroll bug.
				//vfm.add(new LabelField("", FOCUSABLE));
				vfm.add(bitmapField);
				// add focusable to fix scroll bug.
				vfm.add(new LabelField("", FOCUSABLE));
				
				screen.add(vfm);
				
				addMenuItem(new SaveMenu());
				addMenuItem(new NewMenu());

			} catch (WriterException e) {
				e.printStackTrace();
			}
		}
	}

	private class SaveMenu extends MenuItem {
		public SaveMenu() {
			super("Save", 0, 100);
		}

		public void run() {
			// Save file to SDcard
			String filePath = System
					.getProperty("fileconn.dir.memorycard.photos")
					+ Long.toString((new Date()).getTime()) + ".png";
			writeFile(filePath);
		}
	}

	private class NewMenu extends MenuItem {
		public NewMenu() {
			super("New", 0, 100);
		}

		public void run() {
			deleteAll();
			removeAllMenuItems();
			initializeEncoderScreen();
			initializeText();
		}
	}

}
