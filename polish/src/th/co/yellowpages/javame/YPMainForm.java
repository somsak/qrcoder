package th.co.yellowpages.javame;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

public class YPMainForm extends Form implements CommandListener {

	private final YPZXingMIDlet ypZXingMIDlet;

	private static final Command CMD_CAMERA = new Command("Camera",
			Command.ITEM, 0);
	private static final Command CMD_ALBUM = new Command("Album", Command.ITEM,
			1);
	private static final Command CMD_ENCODER = new Command("Encoder",
			Command.ITEM, 2);
	private static final Command CMD_HISTORY = new Command("History",
			Command.ITEM, 3);
	private static final Command CMD_SETTING = new Command("Settings",
			Command.ITEM, 4);

	private static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 0);

	public YPMainForm(final YPZXingMIDlet ypZXingMIDlet) {
		super("QRCoder");

		this.ypZXingMIDlet = ypZXingMIDlet;

		try {
			ImageItem logo = new ImageItem(null, Image
					.createImage("/res/logo.png"), ImageItem.LAYOUT_CENTER
					| ImageItem.LAYOUT_VCENTER, null);

//#ifdef polish.api.mmapi
			ImageItem camera = new ImageItem(null, Image
					.createImage("/res/camera-icon.png"),
					ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_VCENTER
							| ImageItem.LAYOUT_NEWLINE_BEFORE, null);

			camera.setDefaultCommand(CMD_CAMERA);
			camera.setItemCommandListener(new ItemCommandListener() {
				public void commandAction(Command command, Item item) {
					if (command == CMD_CAMERA)
						ypZXingMIDlet.showVideoCanvas();
				}
			});
//#endif

//#ifdef polish.api.fileconnectionapi
			ImageItem album = new ImageItem(null, Image
					.createImage("/res/album-icon.png"),
					ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_VCENTER, null);

			album.setDefaultCommand(CMD_ALBUM);
			album.setItemCommandListener(new ItemCommandListener() {
				public void commandAction(Command command, Item item) {
					if (command == CMD_ALBUM)
						ypZXingMIDlet.showChooseImageForm();
				}
			});
//#endif

			append(logo);
//#ifdef polish.api.mmapi
			append("ScanCode");
			append(camera);
//#endif
//#ifdef polish.api.fileconnectionapi
			append(album);
//#endif
		} catch (IOException e) {
			e.printStackTrace();
		}

		addCommand(CMD_ENCODER);
		addCommand(CMD_HISTORY);
		addCommand(CMD_SETTING);
		addCommand(CMD_EXIT);

		setCommandListener(this);
	}

	public void commandAction(Command command, Displayable displayable) {
		if (command == CMD_EXIT) {
			ypZXingMIDlet.stop();
//#ifdef polish.api.mmapi
		} else if (command == CMD_CAMERA) {
			ypZXingMIDlet.showVideoCanvas();
//#endif
//#ifdef polish.api.fileconnectionapi
		} else if (command == CMD_ALBUM) {
			ypZXingMIDlet.showChooseImageForm();
//#endif
		} else if (command == CMD_HISTORY) {
			ypZXingMIDlet.showGenerateHistoryForm();
		} else if (command == CMD_ENCODER) {
			ypZXingMIDlet.showCreateQRCanvas();
		} else if (command == CMD_SETTING) {
			ypZXingMIDlet.showSetting();
		}
	}
}
