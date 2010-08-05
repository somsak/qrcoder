package th.co.yellowpages.javame;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.microedition.io.Connector;
//#ifdef polish.api.fileconnectionapi
import javax.microedition.io.file.FileConnection;
//#endif
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

final class YPCreateQRForm extends Form implements CommandListener {

	private static final int BACK = 0xFF000000;
	private static final int WHTIE = 0xFFFFFFFF;

	private static final Command CMD_CREATE = new Command("Create",
			Command.ITEM, 0);
//#ifdef polish.api.fileconnectionapi
	private static final Command CMD_SAVE = new Command("Save", Command.ITEM, 1);
//#endif
	private static final Command CMD_NEW = new Command("New", Command.ITEM, 2);
	private static final Command CMD_BACK = new Command("Back", Command.BACK, 0);

	private YPZXingMIDlet ypZXingMIDlet;
//#if polish.JavaPlatform == "BlackBerry/5.0"
	private javax.microedition.lcdui.TextField textField;
//#else
	private TextField textField;
//#endif

	private Image qrCodeImage;

	private String filename;
	private String qrContent;

	private ChoiceGroup ecnodeRadioButtons;
	private int defaultIndex;

	public YPCreateQRForm(YPZXingMIDlet ypZXingMIDlet) {
		super("Encoder");
		try {


		this.ypZXingMIDlet = ypZXingMIDlet;

		addCommand(CMD_CREATE);
		addCommand(CMD_BACK);
		setCommandListener(this);

		initForm();
		} catch(Exception e) {
			System.out.println("Try catch block from YPCreateQRForm.");
			System.out.println(e.toString());
		}
	}

	public void commandAction(Command command, Displayable displayable) {
		int type = command.getCommandType();

		if (command == CMD_CREATE) {
			qrContent = textField.getString();

			if (qrContent.length() != 0) {
				qrCodeImage = encode(qrContent);
				ImageItem imageItem = new ImageItem(null, qrCodeImage,
						ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_VCENTER,
						null);

				deleteAll();
				append(imageItem);
				removeCommand(CMD_CREATE);
//#ifdef polish.api.fileconnectionapi
				addCommand(CMD_SAVE);
//#endif
				addCommand(CMD_NEW);
			} else {

			}
//#ifdef polish.api.fileconnectionapi
		} else if (command == CMD_SAVE) {
			saveQRCodeImage();
//#endif
		} else if (command == CMD_NEW) {
			ypZXingMIDlet.getDisplay().setCurrent(
					new YPCreateQRForm(ypZXingMIDlet));
		} else if (type == Command.BACK) {
			ypZXingMIDlet.showMainForm();
		}
	}

	private Image encode(String content) {
		try {
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			int target = Math.min(getHeight(), getWidth());
			int qrWidth = target;
			int qrHeigth = qrWidth;

			BitMatrix qrBitMatrix = qrCodeWriter.encode(content,
					BarcodeFormat.QR_CODE, qrWidth, qrHeigth);

			System.out.println("w: " + qrBitMatrix.getWidth());
			System.out.println("h: " + qrBitMatrix.getHeight());

			int[] rgb = new int[qrWidth * qrHeigth];

			for (int y = 0; y < qrBitMatrix.getHeight(); y++) {
				for (int x = 0; x < qrWidth; x++) {
					int offset = y * qrHeigth;
					rgb[offset + x] = qrBitMatrix.get(x, y) ? BACK : WHTIE;
				}
			}

			return Image.createRGBImage(rgb, qrWidth, qrHeigth, false);
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void initForm() {
//#if polish.JavaPlatform == "BlackBerry/5.0"
		textField = new javax.microedition.lcdui.TextField("Message: ", null, 256, javax.microedition.lcdui.TextField.ANY);
//#else
		textField = new TextField("Message: ", null, 256, TextField.ANY);
//#endif
		textField.setPreferredSize(getWidth(), 65);
		append(textField);

		ecnodeRadioButtons = new ChoiceGroup(null, Choice.EXCLUSIVE);
		defaultIndex = ecnodeRadioButtons.append("website(url)", null);
		ecnodeRadioButtons.append("phone", null);
		ecnodeRadioButtons.append("e-mail", null);
		ecnodeRadioButtons.append("message", null);
		ecnodeRadioButtons.setSelectedIndex(defaultIndex, true);

		append(ecnodeRadioButtons);
	}

//#ifdef polish.api.fileconnectionapi
	private void saveQRCodeImage() {
		byte[] pngData = PNGEncoder.toPNG(qrCodeImage, false);

		filename = Long.toString((new Date()).getTime()) + ".png";

		Thread t = new Thread(new SaveThread(pngData, filename, this));
		t.start();
	}
//#endif

	void showAlert(String message) {
		Alert alert = new Alert("Result");

		alert.setString(message);
		ypZXingMIDlet.getDisplay().setCurrent(alert);
	}

//#ifdef polish.api.fileconnectionapi
	private class SaveThread implements Runnable {

		private byte[] data;
		private String saveName;
		private YPCreateQRForm createQRFrom;

		public SaveThread(byte[] data, String saveName,
				YPCreateQRForm createQRFrom) {
			this.data = data;
			this.saveName = saveName;
			this.createQRFrom = createQRFrom;
		}

		public void run() {
			System.out.println("Start saving QR Code image");

			try {
				String url = YPZXingMIDlet.QRCODE_IMAGE_SAVE_PATH + saveName;
				createQRFrom.showAlert(url);

				FileConnection fileConnection = (FileConnection) Connector
						.open(url, Connector.READ_WRITE);
				if (!fileConnection.exists()) {
					fileConnection.create();
				}
				OutputStream outputStream = fileConnection.openOutputStream();

				outputStream.write(this.data);
				outputStream.close();
				fileConnection.close();

				System.out.println("QR Code image saved successfully");
			} catch (IOException ioe) {
				System.out.println("IOException: " + ioe.getMessage());
				createQRFrom.showAlert("Save failed!!");
			} catch (SecurityException se) {
				System.out.println("Security exception:" + se.getMessage());
				createQRFrom.showAlert("Save  failed!!");
			}
		}
	}
//#endif
}
