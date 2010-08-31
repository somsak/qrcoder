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

//#ifdef polish.api.mmapi
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import javax.microedition.media.MediaException;
//#endif


/**
 * The main {@link Canvas} onto which the camera's field of view is painted.
 * This class manages decoding via {@link SnapshotThread}.
 * 
 * @author Sean Owen
 * @author Simon Flannery
 */
final class VideoCanvas extends Canvas implements CommandListener {

	private static final Command back = new Command("Back", Command.BACK, 0);

	private final YPZXingMIDlet ypZXingMIDlet;
	private final SnapshotThread snapshotThread;
	
	VideoCanvas(YPZXingMIDlet ypZXingMIDlet) {
		this.ypZXingMIDlet = ypZXingMIDlet;
		
		try {
			setCommandListener(this);
		} catch(Exception e) {
			e.toString();
		}
	
		snapshotThread = new SnapshotThread(ypZXingMIDlet);
		new Thread(snapshotThread).start();
		
		addCommand(back);
	}

	protected void paint(Graphics g) {
		// Do nothing
	}
	
	public int getWidth() {
		return getWidth();
	}
	
	public int getHeight() {
		return getHeight();
	}

	protected void keyPressed(int keyCode) {
		if (keyCode == -5) {
			snapshotThread.continueRun();
		}
	}

	public void commandAction(Command command, Displayable displayable) {
		int type = command.getCommandType();

		if (type == Command.OK) {
			snapshotThread.continueRun();
		} else if (type == Command.BACK) {
			snapshotThread.stop();
			
			try {
				Player player = ypZXingMIDlet.getPlayer();
				player.stop();
				player.deallocate();
				player.close();
			} catch(Exception e) {
				System.out.println(e.toString());
			}
			
			ypZXingMIDlet.showMainForm();
		}
	}
}
