package th.co.yellowpages.javame;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

public class YPSetting extends Form implements CommandListener,
		ItemStateListener {

	private static final Command CMD_BACK = new Command("Back", Command.BACK, 0);
	private static final Command CMD_SAVE = new Command("Save", Command.OK, 0);

	private YPZXingMIDlet ypZXingMIDlet;
	private RecordStore rs;
	private ChoiceGroup sound;

	public YPSetting(final YPZXingMIDlet ypZXingMIDlet) {
		super("Setting");

		this.ypZXingMIDlet = ypZXingMIDlet;

		try {
			rs = RecordStore.openRecordStore(
					YPZXingMIDlet.QRCODER_SETTING_RECORD_STORE, true);
		} catch (RecordStoreFullException e) {
			e.printStackTrace();
		} catch (RecordStoreNotFoundException e) {
			e.printStackTrace();
		} catch (RecordStoreException e) {
			e.printStackTrace();
		}

		sound = new ChoiceGroup("Beeb sound", ChoiceGroup.EXCLUSIVE,
				new String[] { "Sound 1", "Sound 2", "Sound 3", "Sound 4",
						"Disable" }, null);

		append(sound);

		// boolean[] selectedFlag = new boolean[] { false, false, false, false
		// };
		// selectedFlag[loadSoundSetting()] = true;
		//
		// sound.setSelectedFlags(selectedFlag);

		sound.setSelectedIndex(loadSoundSetting(), true);

		addCommand(CMD_SAVE);
		addCommand(CMD_BACK);

		setCommandListener(this);
		setItemStateListener(this);
	}

	private void saveSoundSetting(String soundId) {
		byte[] sound = soundId.getBytes();

		try {
			rs.setRecord(1, sound, 0, sound.length);
		} catch (RecordStoreNotOpenException e) {
			e.printStackTrace();
		} catch (InvalidRecordIDException e) {
			try {
				rs.addRecord(sound, 0, sound.length);
			} catch (RecordStoreNotOpenException e1) {
				e1.printStackTrace();
			} catch (RecordStoreFullException e1) {
				e1.printStackTrace();
			} catch (RecordStoreException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (RecordStoreFullException e) {
			e.printStackTrace();
		} catch (RecordStoreException e) {
			e.printStackTrace();
		}
	}

	protected int loadSoundSetting() {
		int soundId = 0;

		if (rs != null) {
			try {
				byte[] sound = rs.getRecord(1);
				soundId = Integer.parseInt(new String(sound));
			} catch (RecordStoreNotOpenException e) {
				e.printStackTrace();
			} catch (InvalidRecordIDException e) {
				saveSoundSetting("0");
			} catch (RecordStoreException e) {
				e.printStackTrace();
			}
		}

		return soundId;
	}

	public void commandAction(Command command, Displayable displayable) {
		if (command == CMD_BACK) {
			ypZXingMIDlet.showMainForm();
		} else if (command == CMD_SAVE) {
			String soundId = Integer.toString(sound.getSelectedIndex());
			saveSoundSetting(soundId);
			ypZXingMIDlet.showMainForm();
		}
	}

	public void itemStateChanged(Item item) {
		String soundId = Integer.toString(sound.getSelectedIndex());
		ypZXingMIDlet.playBeeb(Integer.parseInt(soundId));
	}
}
