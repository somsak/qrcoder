package th.co.yellowpages.javame;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
//#ifdef polish.api.fileconnectionapi
import javax.microedition.io.file.FileConnection;
//#endif
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;

public class YPAlbumForm extends Form implements CommandListener {

	private static final Command CMD_BACK = new Command("Back", Command.BACK, 0);

	private YPZXingMIDlet ypZXingMIDlet;
	private YPAlbumForm chooseImageForm;

	public YPAlbumForm(YPZXingMIDlet ypZXingMIDlet) {
		super("Album");
		try {


		this.ypZXingMIDlet = ypZXingMIDlet;
		chooseImageForm = this;

//#ifdef polish.api.fileconnectionapi
		Thread t = new Thread(new LoadImageThread());
		t.start();
//#endif

		addCommand(CMD_BACK);
		setCommandListener(this);
		} catch(Exception e) {
			System.out.println("Try catch block from YPZXingMIDlet.");
			System.out.println(e.toString());
		}
	}

//#ifdef polish.api.fileconnectionapi
	private class LoadImageThread implements Runnable {

		public void run() {
			try {
				String url = YPZXingMIDlet.QRCODE_IMAGE_SAVE_PATH;
				
				FileConnection fc = (FileConnection) Connector.open(url,
						Connector.READ);
				Enumeration files = fc.list();
				fc.close();
				while (files.hasMoreElements()) {
					String imageURL = YPZXingMIDlet.QRCODE_IMAGE_SAVE_PATH
							+ (String) files.nextElement();
					fc = (FileConnection) Connector.open(imageURL,
							Connector.READ);
					Image image = Image.createImage(fc.openInputStream());
					chooseImageForm.append(new YPImageItem(image,
							ypZXingMIDlet, chooseImageForm));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
//#endif

	public void commandAction(Command command, Displayable d) {
		int type = command.getCommandType();

		if (type == Command.BACK) {
			ypZXingMIDlet.showMainForm();
		}
	}
}
