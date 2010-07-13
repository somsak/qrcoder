package th.co.yellowpages.javame;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.StringItem;

import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.SMSParsedResult;
import com.google.zxing.client.result.TelParsedResult;
import com.google.zxing.client.result.URIParsedResult;

public class YPQRCodeDetectHandlerForm extends Form implements CommandListener {

	private static final Command CMD_OPEN_WEB = new Command("Open Web",
			Command.ITEM, 0);
	private static final Command CMD_OPEN_TEL = new Command("Dial",
			Command.ITEM, 0);
	private static final Command CMD_OPEN_EMAIL = new Command("Email",
			Command.ITEM, 0);
	private static final Command CMD_OPEN_SMS = new Command("SMS",
			Command.ITEM, 0);
	private static final Command CMD_BACK = new Command("Back", Command.BACK, 0);

	private ParsedResult result;
	private YPZXingMIDlet ypZXingMIDlet;
	private Displayable backDisplayable;

	public YPQRCodeDetectHandlerForm(ParsedResult result,
			YPZXingMIDlet ypZXingMIDlet, Displayable backDisplayable) {
		super("Decode result");
		this.result = result;
		this.ypZXingMIDlet = ypZXingMIDlet;
		this.backDisplayable = backDisplayable;

		addCommand(CMD_BACK);
		setCommandListener(this);

		ParsedResultType type = result.getType();

		if (type.equals(ParsedResultType.URI)) {
			addCommand(CMD_OPEN_WEB);
		} else if (type.equals(ParsedResultType.EMAIL_ADDRESS)) {
			addCommand(CMD_OPEN_EMAIL);
		} else if (type.equals(ParsedResultType.SMS)) {
			addCommand(CMD_OPEN_SMS);
		} else if (type.equals(ParsedResultType.TEL)) {
			addCommand(CMD_OPEN_TEL);
		} else {

		}

		StringItem text = new StringItem(null, result.getDisplayResult());

		append(text);
		YPSetting ypSetting = new YPSetting(ypZXingMIDlet);
		this.ypZXingMIDlet.playBeeb(ypSetting.loadSoundSetting());
	}

	protected void paint(Graphics g) {
		g.setColor(0xFFFF00);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(0x000000);
		g.drawString(result.getDisplayResult(), getWidth() / 2,
				getHeight() / 2, Graphics.BASELINE | Graphics.HCENTER);
	}

	public void commandAction(Command command, Displayable displayable) {
		try {
			String uri = null;

			if (command == CMD_BACK) {
				this.ypZXingMIDlet.getDisplay().setCurrent(backDisplayable);
			} else if (command == CMD_OPEN_WEB) {
				uri = ((URIParsedResult) result).getURI();
			} else if (command == CMD_OPEN_TEL) {
				uri = ((TelParsedResult) result).getTelURI();
			} else if (command == CMD_OPEN_EMAIL) {
				uri = ((EmailAddressParsedResult) result).getMailtoURI();
			} else if (command == CMD_OPEN_SMS) {
				uri = ((SMSParsedResult) result).getSMSURI();
			}

			if (uri != null)
				ypZXingMIDlet.platformRequest(uri);
		} catch (ConnectionNotFoundException e) {
			e.printStackTrace();
		}
	}
}
