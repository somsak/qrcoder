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

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

/**
 * The screen used to display the application 'about' information.
 * 
 * This code was contributed by LifeMarks.
 * 
 * @author Matt York (matt@lifemarks.mobi)
 */
public class AboutScreen extends MainScreen {

	private static final String OPEN_BROWSER_BUTTON_LABEL = "Open browser";
	private static final String CANCEL_BUTON_LABEL = "Cancel";
	private static final String ZXING_URL = "http://code.google.com/p/zxing";
	private static final String ZXING_ICON = "zxing-icon.png";

	public AboutScreen() {
		LabelField title = new LabelField("QRCoder v1.0", DrawStyle.ELLIPSIS
				| USE_ALL_WIDTH);
		title.setMargin(5,0,0,5);
		setTitle(title);
		Manager vfm = new VerticalFieldManager(FIELD_HCENTER | USE_ALL_WIDTH);

		BitmapField icon = new BitmapField(
				Bitmap.getBitmapResource(ZXING_ICON), FIELD_HCENTER);
		icon.setMargin(0, 10, 0, 20);

		Field content = new LabelField(
				"Based on the open source ZXing Barcode Library", FIELD_HCENTER);

		HorizontalFieldManager hfm1 = new HorizontalFieldManager(FIELD_HCENTER);
		hfm1.setMargin(20, 20, 20, 20);
		hfm1.add(icon);
		hfm1.add(content);
		vfm.add(hfm1);

		Field uri = new LabelField(ZXING_URL, FIELD_HCENTER);

		vfm.add(uri);

		Field openBrowser = new ButtonField(OPEN_BROWSER_BUTTON_LABEL,
				ButtonField.CONSUME_CLICK);
		openBrowser.setChangeListener(new ButtonListener(this));

		Field cancelButton = new ButtonField(CANCEL_BUTON_LABEL,
				ButtonField.CONSUME_CLICK);
		cancelButton.setChangeListener(new ButtonListener(this));

		HorizontalFieldManager hfm2 = new HorizontalFieldManager(FIELD_HCENTER);
		hfm2.setMargin(10, 0, 0, 0);
		hfm2.add(openBrowser);
		hfm2.add(cancelButton);
		vfm.add(hfm2);
		add(vfm);
	}

	/**
	 * Used to close the screen when the ok button is pressed.
	 */
	private static class ButtonListener implements FieldChangeListener {
		private final Screen screen;

		private ButtonListener(Screen screen) {
			this.screen = screen;
		}

		public void fieldChanged(Field field, int context) {
			if (field instanceof ButtonField) {
				ButtonField button = (ButtonField) field;

				if (button.getLabel().equals(OPEN_BROWSER_BUTTON_LABEL)) {
					BrowserSession bSession = Browser.getDefaultSession();
					bSession.displayPage(ZXING_URL);
				} else if (button.getLabel().equals(CANCEL_BUTON_LABEL)) {
					UiEngine ui = Ui.getUiEngine();
					ui.popScreen(screen);
				}
			}
		}
	}

}
