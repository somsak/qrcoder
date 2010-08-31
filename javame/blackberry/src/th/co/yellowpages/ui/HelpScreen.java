/*
 * Copyright 2008 ZXing authors
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

package th.co.yellowpages.ui;

import javax.microedition.io.file.FileConnection;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

/**
 * The screen used to display the application help information.
 * 
 * This code was contributed by LifeMarks.
 * 
 * @author Matt York (matt@lifemarks.mobi)
 */
public class HelpScreen extends MainScreen {

	private static final String DEFAULT_URL = "local:///html/index.html";
	private static final String BACK_LABEL = "Back";
	private static final String DONE_LABEL = "Done";
	private static BrowserField browserField;

	HelpScreen() {
		LabelField title = new LabelField("QRCoder - Help", DrawStyle.ELLIPSIS
				| USE_ALL_WIDTH);
		title.setMargin(5, 0, 0, 5);
		setTitle(title);
		Manager vfm = new VerticalFieldManager(VERTICAL_SCROLL);

		browserField = new BrowserField();
		browserField.requestContent(DEFAULT_URL);
		vfm.add(browserField);
		add(vfm);
		
		addMenuItem(new BackMenu());
		addMenuItem(new ForwardMenu());
		addMenuItem(new DoneMenu(this));
	}

	private class DoneMenu extends MenuItem {
		private final Screen screen;
		
		public DoneMenu(Screen screen) {
			super("Done", 0, 100);
			this.screen = screen;
		}

		public void run() {
			UiEngine ui = Ui.getUiEngine();
			ui.popScreen(screen);
		}

	}

	private class BackMenu extends MenuItem {
		public BackMenu() {
			super("Back", 0, 100);
		}

		public void run() {
			browserField.setFocus();
			browserField.back();
		}

	}

	private class ForwardMenu extends MenuItem {
		public ForwardMenu() {
			super("Forward", 0, 100);
		}

		public void run() {
			browserField.setFocus();
			browserField.forward();
		}

	}

	/**
	 * Closes the screen when the OK button is pressed.
	 */
	private static class ButtonListener implements FieldChangeListener {
		private final Screen screen;

		private ButtonListener(Screen screen) {
			this.screen = screen;
		}

		public void fieldChanged(Field field, int context) {
			if (field instanceof ButtonField) {
				ButtonField button = (ButtonField) field;

				if (button.getLabel().equals(BACK_LABEL)) {
					browserField.setFocus();
					browserField.back();
					browserField.forward();
				} else if (button.getLabel().equals(DONE_LABEL)) {
					UiEngine ui = Ui.getUiEngine();
					ui.popScreen(screen);
				}
			}
		}
	}

}
