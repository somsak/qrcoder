package th.co.yellowpages.javame;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
//#if polish.Vendor == BlackBerry
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.List;
//#endif
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

//#if polish.Vendor == BlackBerry
//# public class YPMainForm extends List implements CommandListener {
//#else
public class YPMainForm extends Form implements CommandListener {
//#endif

	private YPZXingMIDlet ypZXingMIDlet;

	private static final Command CMD_CAMERA = new Command("Camera",
			Command.OK, 0);
	private static final Command CMD_ALBUM = new Command("Album",
			Command.OK, 0);
	private static final Command CMD_GO = new Command("GO",
			Command.ITEM, 0);
	private static final Command CMD_ENCODER = new Command("Encoder",
			Command.SCREEN, 2);
	private static final Command CMD_HISTORY = new Command("History",
			Command.SCREEN, 3);
	private static final Command CMD_SETTING = new Command("Settings",
			Command.SCREEN, 4);

	private static final Command CMD_EXIT = new Command("Exit",
			Command.EXIT, 0);

	public YPMainForm(final YPZXingMIDlet ypZXingMIDlet) {
//#if polish.Vendor == BlackBerry
		super("QRCoder", Choice.IMPLICIT);
//#else
		super("QRCoder");
//#endif
		this.ypZXingMIDlet = ypZXingMIDlet;

//#if polish.Vendor == BlackBerry
//#ifdef polish.api.mmapi
	try {
		append("Camera", null);
	} catch(Exception e) {
		System.out.println("Try catch block from YPMainForm.");
		System.out.println(e.toString());
	}
//#endif
//#ifdef polish.api.fileconnectionapi
		append("Album", null);
//#endif
//#else
		try {
			ImageItem logo = new ImageItem(null, Image
					.createImage("/logo.png"), ImageItem.LAYOUT_CENTER
					| ImageItem.LAYOUT_VCENTER, null);

			append(logo);

//#ifdef polish.api.mmapi
			ImageItem camera = new ImageItem(null, Image
					.createImage("/camera-icon.png"),
					ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_VCENTER
							| ImageItem.LAYOUT_NEWLINE_BEFORE, null);

			camera.setDefaultCommand(CMD_CAMERA);
			camera.setItemCommandListener(new ItemCommandListener() {
				public void commandAction(Command command, Item item) {
					if (command == CMD_CAMERA)
						ypZXingMIDlet.showVideoCanvas();
				}
			});

			append("ScanCode");
			append(camera);
//#endif

//#ifdef polish.api.fileconnectionapi
			ImageItem album = new ImageItem(null, Image
					.createImage("/album-icon.png"),
					ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_VCENTER, null);

			album.setDefaultCommand(CMD_ALBUM);
			album.setItemCommandListener(new ItemCommandListener() {
				public void commandAction(Command command, Item item) {
					if (command == CMD_ALBUM)
						ypZXingMIDlet.showChooseImageForm();
				}
			});

			append(album);
//#endif

		} catch (IOException e) {
			e.printStackTrace();
		}
//#endif

//#if polish.Vendor == BlackBerry
		setSelectCommand(CMD_GO);
//#endif

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
//#if polish.Vendor == BlackBerry
		} else if (command == CMD_GO) {
			int index = getSelectedIndex();
			if (index == 0) {
//#ifdef polish.api.mmapi
				ypZXingMIDlet.showVideoCanvas();
//#endif
			} else if (index == 1) {
//#ifdef polish.api.fileconnectionapi
				ypZXingMIDlet.showChooseImageForm();
//#endif
			}
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
