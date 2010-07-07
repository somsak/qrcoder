package th.co.yellowpages.javame;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import com.google.zxing.Result;

public class YPImageItem extends CustomItem implements ItemCommandListener {
	private static final Command CMD_DECODE = new Command("Decode",
			Command.ITEM, 0);

	private Image image;
	private YPZXingMIDlet ypZXingMIDlet;
	private YPAlbumForm chooseImageForm;

	protected YPImageItem(Image image, YPZXingMIDlet ypZXingMIDlet,
			YPAlbumForm chooseImageForm) {
		super(null);

		this.image = image;
		this.ypZXingMIDlet = ypZXingMIDlet;
		this.chooseImageForm = chooseImageForm;

		setDefaultCommand(CMD_DECODE);
		setItemCommandListener(this);
	}

	protected int getMinContentHeight() {
		return 0;
	}

	protected int getMinContentWidth() {
		return 0;
	}

	protected int getPrefContentHeight(int width) {
		return 70;
	}

	protected int getPrefContentWidth(int height) {
		return 70;
	}

	protected void paint(Graphics g, int w, int h) {
		Image i = YPZXingMIDlet.resizeImage(image, 70, 70);
		g.drawImage(i, 2, 2, Graphics.TOP | Graphics.LEFT);
	}

	public void commandAction(Command c, Item item) {
		if (c == CMD_DECODE) {
			Result result = YPZXingMIDlet.decode(image);
			ypZXingMIDlet.handleDecodedText(result, chooseImageForm);
		}
	}
}
