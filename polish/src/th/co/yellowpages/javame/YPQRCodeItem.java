//#condition polish.api.fileconnectionapi
package th.co.yellowpages.javame;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

public class YPQRCodeItem extends CustomItem implements ItemCommandListener {

	private final static Command CMD_SELECT = new Command("Select",
			Command.ITEM, 0);
	private final static Command CMD_INFO = new Command("Info", Command.ITEM, 1);

	private String qrContent;
	private Image qrImage;
	private int width, height;
	private String filename;
	private YPZXingMIDlet ypZXingMIDlet;
	private YPHistoryForm generateHistoryForm;

	protected YPQRCodeItem(String qrContent, Image qrImage, int width,
			int height, String filename, YPZXingMIDlet ypZXingMIDlet,
			YPHistoryForm generateHistoryForm) {
		super(null);
		try {


		this.qrContent = qrContent;
		this.qrImage = qrImage;
		this.width = width;
		this.height = height;
		this.filename = filename;
		this.ypZXingMIDlet = ypZXingMIDlet;
		this.generateHistoryForm = generateHistoryForm;

		setDefaultCommand(CMD_SELECT);
		addCommand(CMD_INFO);
		this.setItemCommandListener(this);
		} catch(Exception e) {
			System.out.println("Try catch block from YPQRCodeItem.");
			System.out.println(e.toString());
		}
	}

	protected int getMinContentHeight() {
		return height;
	}

	protected int getMinContentWidth() {
		return width;
	}

	protected int getPrefContentHeight(int width) {
		return this.height;
	}

	protected int getPrefContentWidth(int height) {
		return this.width;
	}

	protected void paint(Graphics g, int w, int h) {
		g.drawRect(1, 1, w - 2, h - 2);
		g.setColor(0xffff00);
		g.fillRect(1, 1, w - 2, h - 2);
		g.drawImage(qrImage, 2, 2, Graphics.TOP | Graphics.LEFT);

		g.setColor(0x000000);
		if (qrContent.length() >= 25) {
			g.drawSubstring(qrContent, 0, 25, qrImage.getWidth() + 3, qrImage
					.getHeight() / 2, Graphics.BASELINE | Graphics.LEFT);
		} else {
			g.drawString(qrContent, qrImage.getWidth() + 3,
					qrImage.getHeight() / 2, Graphics.BASELINE | Graphics.LEFT);
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == CMD_SELECT) {
			System.out.println("Command select");
			Thread t = new Thread(new ShowQRThread());
			t.start();
		} else if (c == CMD_INFO) {
			Alert a = new Alert("Info");
			a.setString(qrContent);
			a.setTimeout(Alert.FOREVER);
			ypZXingMIDlet.getDisplay().setCurrent(a);
		}
	}

	private class ShowQRThread implements Runnable {

		public void run() {
			try {
				String url = YPZXingMIDlet.QRCODE_IMAGE_SAVE_PATH + filename;
				FileConnection fc = (FileConnection) Connector.open(url,
						Connector.READ);
				Image i = Image.createImage(fc.openInputStream());
				ypZXingMIDlet.getDisplay().setCurrent(
						new YPQRCodeShowCanvas(i, ypZXingMIDlet,
								generateHistoryForm));
				fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
