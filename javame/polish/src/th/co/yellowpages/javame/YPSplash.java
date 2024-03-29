package th.co.yellowpages.javame;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import de.enough.polish.util.ImageUtil;

public class YPSplash extends Canvas implements Runnable {

	private static final int SPLASH_TIME = 2000;
	private static final int BG_COLOR = 0x007DD7;
	private static final String LOGO = "/logo.png";
	private static final String FOOTER = "/footer.png";

	private YPZXingMIDlet ypZXingMIDlet;

	public YPSplash(YPZXingMIDlet ypZXingMIDlet) {
		try {
		this.ypZXingMIDlet = ypZXingMIDlet;
		} catch(Exception e) {
			System.out.println("Try catch block from YPSplash.");
			System.out.println(e.toString());
		}
	}

	protected void paint(Graphics g) {
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());

		try {
			Image logoImage = Image.createImage(LOGO);
			logoImage = ImageUtil.scaleToFit(logoImage, getWidth() - 30, getHeight());
			Image footer = Image.createImage(FOOTER);
			footer = ImageUtil.scaleToFit(footer, getWidth() - 50, getHeight());
			
			// Logo
			g.drawImage(logoImage, getWidth() / 2, getHeight() / 2, Graphics.VCENTER
					| Graphics.HCENTER);

			// Footer
			g.drawImage(footer, getWidth() / 2, getHeight(), Graphics.BOTTOM
					| Graphics.HCENTER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		ypZXingMIDlet.getDisplay().setCurrent(this);
		try {
			Thread.sleep(SPLASH_TIME);
		} catch (InterruptedException e) {
		}
		ypZXingMIDlet.showMainForm();
	}
}
