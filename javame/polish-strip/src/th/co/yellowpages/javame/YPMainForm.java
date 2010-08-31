package th.co.yellowpages.javame;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import de.enough.polish.util.ImageUtil;

public class YPMainForm extends Form implements CommandListener {

	private YPZXingMIDlet ypZXingMIDlet;

	private static final Command CMD_CAMERA = new Command("Camera",	Command.OK, 0);
	private static final Command CMD_HISTORY = new Command("History",	Command.SCREEN, 1);

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
			// Image cameraIcon = Image.createImage("/camera-icon.png");
			// ImageItem camera = new ImageItem(null, cameraIcon, ImageItem.LAYOUT_CENTER |ImageItem.LAYOUT_VCENTER | ImageItem.LAYOUT_NEWLINE_BEFORE, null);
			StringItem camera = new StringItem(null, "Camera\n", Item.BUTTON);
			camera.setDefaultCommand(CMD_CAMERA);
			camera.setItemCommandListener(new ItemCommandListener() {
				public void commandAction(Command command, Item item) {
					if (command == CMD_CAMERA)
						ypZXingMIDlet.showVideoCanvas();
				}
			});
			append(camera);
			
			StringItem history = new StringItem(null, "\nHistory\n", Item.BUTTON);
			history.setDefaultCommand(CMD_HISTORY);
			history.setItemCommandListener(new ItemCommandListener() {
				public void commandAction(Command command, Item item) {
					if (command == CMD_HISTORY)
						ypZXingMIDlet.showGenerateHistoryForm();
				}
			});
			append(history);
//#endif
      Image footImage = Image.createImage("/footer.png");
			footImage = ImageUtil.scaleToFit(footImage, getWidth() - 60, getHeight());
			ImageItem footer = new ImageItem(null, footImage, ImageItem.LAYOUT_CENTER
					| ImageItem.LAYOUT_VCENTER, null);
			append(footer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		setCommandListener(this);
	}

	public void commandAction(Command command, Displayable displayable) {
//#ifdef polish.api.mmapi
		if (command == CMD_CAMERA) {
			ypZXingMIDlet.showVideoCanvas();
//#endif
		}  else if (command == CMD_HISTORY) {
			ypZXingMIDlet.showGenerateHistoryForm();
		}
	}
}
