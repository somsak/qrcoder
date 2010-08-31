package th.co.yellowpages.javame;

import java.io.IOException;
import java.util.Date;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class YPHistoryForm extends Form implements CommandListener {

	private static final Command CMD_OPEN_WEB = new Command("Open Web",
			Command.ITEM, 0);
	private static final Command CMD_OPEN_TEL = new Command("Dial",
			Command.ITEM, 0);
	private static final Command CMD_OPEN_EMAIL = new Command("Email",
			Command.ITEM, 0);
	private static final Command CMD_CLEAR_HISTORY = new Command(
			"Clear history", Command.ITEM, 1);

	private static final Command CMD_BACK = new Command("Back", Command.BACK, 0);

	private YPZXingMIDlet ypZXingMIDlet;
	private YPHistoryForm generateHistoryForm;

	public YPHistoryForm(YPZXingMIDlet ypZXingMIDlet) {
		super("History");
		try {


		this.ypZXingMIDlet = ypZXingMIDlet;
		this.generateHistoryForm = this;

		addCommand(CMD_BACK);
		addCommand(CMD_CLEAR_HISTORY);
		setCommandListener(this);

		Thread t = new Thread(new RecordListThread());
		t.start();
		} catch(Exception e) {
			System.out.println("Try catch block from YPHistoryForm.");
			System.out.println(e.toString());
		}
	}

	public void commandAction(Command command, Displayable displayable) {
		int type = command.getCommandType();

		if (type == Command.BACK) {
			System.out.println(this.getClass().getName() + ": " + "Back");

			ypZXingMIDlet.showMainForm();
		} else if (command == CMD_CLEAR_HISTORY) {
			try {
				RecordStore rs = RecordStore.openRecordStore(
						YPZXingMIDlet.QRCODER_HISTORY_RECORD_STORE, true);
				RecordEnumeration re = rs.enumerateRecords(null, null, false);

				while (re.hasNextElement()) {
					rs.deleteRecord(re.nextRecordId());
				}

				ypZXingMIDlet.getDisplay().setCurrent(
						new YPHistoryForm(ypZXingMIDlet));
			} catch (RecordStoreNotFoundException e) {
				e.printStackTrace();
			} catch (RecordStoreException e) {
				e.printStackTrace();
			}
		}
	}

	private class RecordListThread implements Runnable {

		public void run() {
			try {
				RecordStore rs = RecordStore.openRecordStore(
						YPZXingMIDlet.QRCODER_HISTORY_RECORD_STORE, true);
				RecordEnumeration re = rs.enumerateRecords(null, null, false);

				while (re.hasNextElement()) {
					final YPRecord r = new YPRecord();
					r.fromByteStream(re.nextRecord());

					Date d = new Date((Long.parseLong(r.getDatetime())));
					String ds = d.toString().substring(0, 20);

					StringItem item = new StringItem(ds, r.getQRContent());
					item.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);

					if (r.getType().compareTo(YPRecord.TYPE_EMAIL) == 0) {
						item.setDefaultCommand(CMD_OPEN_EMAIL);
					} else if (r.getType().compareTo(YPRecord.TYPE_WEB) == 0) {
						item.setDefaultCommand(CMD_OPEN_WEB);
					} else if (r.getType().compareTo(YPRecord.TYPE_TEL) == 0) {
						item.setDefaultCommand(CMD_OPEN_TEL);
					}

					item.setItemCommandListener(new ItemCommandListener() {
						public void commandAction(Command command, Item item) {
							try {
								String uri = null;

								if (command == CMD_OPEN_WEB) {
									uri = r.getQRContent();
								} else if (command == CMD_OPEN_TEL) {
									uri = "tel:" + r.getQRContent();
								} else if (command == CMD_OPEN_EMAIL) {
									uri = "mailto:" + r.getQRContent();
								}

								if (uri != null)
									ypZXingMIDlet.platformRequest(uri);
							} catch (ConnectionNotFoundException e) {
								e.printStackTrace();
							}
						}
					});

					generateHistoryForm.append(item);
					System.out.println(r.toString());
				}
			} catch (RecordStoreFullException e) {
				e.printStackTrace();
			} catch (RecordStoreNotFoundException e) {
				e.printStackTrace();
			} catch (RecordStoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
