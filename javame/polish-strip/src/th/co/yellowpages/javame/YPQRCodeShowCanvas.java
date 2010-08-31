package th.co.yellowpages.javame;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class YPQRCodeShowCanvas extends Canvas implements CommandListener {

	private static final Command CMD_BACK = new Command("Back", Command.BACK, 0);

	private Image qrCode;
	private YPZXingMIDlet ypzXingMIDlet;
	private YPHistoryForm generateHistoryForm;

	public YPQRCodeShowCanvas(Image qrCode, YPZXingMIDlet ypZXingMIDlet,
			YPHistoryForm generateHistoryForm) {
		try {
		this.qrCode = qrCode;
		this.ypzXingMIDlet = ypZXingMIDlet;
		this.generateHistoryForm = generateHistoryForm;

		addCommand(CMD_BACK);
		setCommandListener(this);
		} catch(Exception e) {
			System.out.println("Try catch block from YPQRCodeShowCanvas.");
			System.out.println(e.toString());
		}
	}

	protected void paint(Graphics g) {
		g.setColor(0xFFFF00);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(qrCode, getWidth() / 2, getHeight() / 2, Graphics.VCENTER
				| Graphics.HCENTER);
	}

	public void commandAction(Command command, Displayable displayable) {
		if (command == CMD_BACK) {
			System.out.println(this.getClass().getName()
					+ " Execute BACK command");
			this.ypzXingMIDlet.getDisplay().setCurrent(generateHistoryForm);
		}
	}
}
