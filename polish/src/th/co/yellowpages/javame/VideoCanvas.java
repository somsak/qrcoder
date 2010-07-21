//#condition polish.api.mmapi
package th.co.yellowpages.javame;

/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

/**
 * The main {@link Canvas} onto which the camera's field of view is painted.
 * This class manages decoding via {@link SnapshotThread}.
 * 
 * @author Sean Owen
 * @author Simon Flannery
 */
final class VideoCanvas extends Canvas implements CommandListener {

	// private static final Command history = new Command("History",
	// Command.ITEM,
	// 0);
	private static final Command back = new Command("Back", Command.BACK, 0);

	private final YPZXingMIDlet ypZXingMIDlet;
	private final SnapshotThread snapshotThread;

	VideoCanvas(YPZXingMIDlet ypZXingMIDlet) {
		this.ypZXingMIDlet = ypZXingMIDlet;
		addCommand(back);
		// addCommand(history);
		setCommandListener(this);
		snapshotThread = new SnapshotThread(ypZXingMIDlet);
		new Thread(snapshotThread).start();
	}

	protected void paint(Graphics graphics) {
		// do nothing
	}

	protected void keyPressed(int keyCode) {
//#if polish.Vendor == BlackBerry
		if (keyCode == -8 || keyCode == -19 || keyCode == -21) {
//#else
		if (keyCode == -5) {
//#endif
			snapshotThread.continueRun();
		}
	}

	public void commandAction(Command command, Displayable displayable) {
		int type = command.getCommandType();

		if (type == Command.OK) {
			snapshotThread.continueRun();
		} else if (type == Command.BACK) {
			snapshotThread.stop();
			ypZXingMIDlet.showMainForm();
		}
	}
}
