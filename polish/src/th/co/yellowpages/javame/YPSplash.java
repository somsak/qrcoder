package th.co.yellowpages.javame;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class YPSplash extends Canvas implements Runnable {

	private static final int BG_COLOR = 0x007DD7;
	private static final String LOGO = "/logo.png";
	private static final String FOOTER = "/footer.png";

	private YPZXingMIDlet ypZXingMIDlet;

	public YPSplash(YPZXingMIDlet ypZXingMIDlet) {
		this.ypZXingMIDlet = ypZXingMIDlet;
	}

	protected void paint(Graphics g) {
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());

		try {
			Image logo = Image.createImage(LOGO);
			Image footer = Image.createImage(FOOTER);

			// Logo
			g.drawImage(logo, getWidth() / 2, getHeight() / 2, Graphics.VCENTER
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
	}
}
