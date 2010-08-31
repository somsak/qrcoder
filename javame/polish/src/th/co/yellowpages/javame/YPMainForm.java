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

import de.enough.polish.util.ImageUtil;

public class YPMainForm extends Form implements CommandListener {

	private YPZXingMIDlet ypZXingMIDlet;

	private static final Command CMD_CAMERA = new Command("Camera",
			Command.OK, 0);
	private static final Command CMD_ALBUM = new Command("Album",
			Command.OK, 0);
	private static final Command CMD_ENCODER = new Command("Encoder",
			Command.SCREEN, 1);
	private static final Command CMD_HISTORY = new Command("History",
			Command.SCREEN, 2);
	private static final Command CMD_SETTING = new Command("Settings",
			Command.SCREEN, 3);
	private static final Command CMD_EXIT = new Command("Exit",
			Command.SCREEN, 4);

	public YPMainForm(final YPZXingMIDlet ypZXingMIDlet) {
		super("QRCoder");
		
		this.ypZXingMIDlet = ypZXingMIDlet;

		try {
			Image logoImage = Image.createImage("/logo.png");
			logoImage = ImageUtil.scaleToFit(logoImage, getWidth() - 60, getHeight());
			ImageItem logo = new ImageItem(null, logoImage, ImageItem.LAYOUT_CENTER
					| ImageItem.LAYOUT_VCENTER, null);
			append(logo);

//#ifdef polish.api.mmapi
			Image cameraIcon = Image.createImage("/camera-icon.png");
			ImageItem camera = new ImageItem(null, cameraIcon,
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
			Image albumIcon = Image.createImage("/album-icon.png");	
			ImageItem album = new ImageItem(null, albumIcon,
					ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_VCENTER, null);

			album.setDefaultCommand(CMD_ALBUM);
			album.setItemCommandListener(new ItemCommandListener() {
				public void commandAction(Command command, Item item) {
					if (command == CMD_ALBUM)
						ypZXingMIDlet.showChooseImageForm();
				}
			});

			append(album);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		} else if (command == CMD_HISTORY) {
			ypZXingMIDlet.showGenerateHistoryForm();
		} else if (command == CMD_ENCODER) {
			ypZXingMIDlet.showCreateQRForm();
		} else if (command == CMD_SETTING) {
			ypZXingMIDlet.showSetting();
		}
	}
}
