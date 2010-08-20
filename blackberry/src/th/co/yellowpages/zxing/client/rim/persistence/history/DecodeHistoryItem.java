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

package th.co.yellowpages.zxing.client.rim.persistence.history;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.util.Persistable;

import java.util.Date;

/**
 * A single decoded history item that is stored by the decode history.
 * 
 * This code was contributed by LifeMarks.
 * 
 * @author Matt York (matt@lifemarks.mobi)
 */
public final class DecodeHistoryItem implements Persistable {

	private String date;
	private String content;

	private DecodeHistoryItem() {
		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MMM-dd HH:mm:ss");

		Date temp = new Date();
		date = format.format(temp);
	}

	public DecodeHistoryItem(String content) {
		this();
		this.content = content;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return date;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

}
