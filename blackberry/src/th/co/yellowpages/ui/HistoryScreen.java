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

import th.co.yellowpages.ui.component.CustomLabelField;
import th.co.yellowpages.zxing.Result;
import th.co.yellowpages.zxing.client.rim.ZXingUiApplication;
import th.co.yellowpages.zxing.client.rim.persistence.history.DecodeHistory;
import th.co.yellowpages.zxing.client.rim.persistence.history.DecodeHistoryItem;
import th.co.yellowpages.zxing.client.rim.util.Log;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

/**
 * The screen used to display the qrcode decoding history.
 * 
 * This code was contributed by LifeMarks.
 * 
 * @author Matt York (matt@lifemarks.mobi)
 */
public class HistoryScreen extends MainScreen {

	private ZXingUiApplication app;

	HistoryScreen() {
		LabelField title = new LabelField("QRCoder - History",
				DrawStyle.ELLIPSIS | USE_ALL_WIDTH);
		title.setMargin(5, 0, 0, 5);
		setTitle(title);

		app = (ZXingUiApplication) UiApplication.getUiApplication();

		Manager vfm = new VerticalFieldManager(FIELD_HCENTER | VERTICAL_SCROLL);
		Log.debug("Num history items: "
				+ DecodeHistory.getInstance().getNumItems());
		DecodeHistory history = DecodeHistory.getInstance();
		for (int i = 0; i < history.getNumItems(); i++) {
			final DecodeHistoryItem item = history.getItemAt(i);

			CustomLabelField labelButton = new CustomLabelField(item
					.getContent(), item.getDate(), USE_ALL_WIDTH | FOCUSABLE) {
				protected boolean navigationClick(int status, int time) {
					Result result = new Result(item.getContent(), null, null, null);
					Bitmap bitmap = Bitmap.getBitmapResource("unknown_barcode.png");
					app.pushScreen(new ResultScreen(result, bitmap));
					return true;
				}
			};

			vfm.add(labelButton);
		}

		add(vfm);
	}

}