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

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.system.PNGEncodedImage;
import net.rim.device.api.ui.Graphics;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import th.co.yellowpages.javame.PNGEncoder;
import th.co.yellowpages.zxing.client.rim.util.Log;

/**
 * The listener that is fired when an image file is added to the file system.
 * 
 * This code was contributed by LifeMarks.
 * 
 * @author Matt York (matt@lifemarks.mobi)
 */
public class QRCapturedJournalListener implements FileSystemJournalListener {

	private final YPMainScreen screen;

	QRCapturedJournalListener(YPMainScreen screen) {
		this.screen = screen;
	}

	public void fileJournalChanged() {
		long lookUSN = FileSystemJournal.getNextUSN() - 1; // the last file
		// added to the
		// filesystem
		Log.debug("lookUSN: " + lookUSN);
		FileSystemJournalEntry entry = FileSystemJournal.getEntry(lookUSN);
		if (entry != null
				&& entry.getEvent() == FileSystemJournalEntry.FILE_ADDED) {
			Log.info("Got file: " + entry.getPath() + " @: " + new Date());
			String imagePath = entry.getPath();
			System.out.println(">> " + imagePath);
			resizeImage(imagePath);
			screen.imageSaved(imagePath);
		}
	}

	private void resizeImage(String filename) {
		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open("file://" + filename,
					Connector.READ_WRITE);

			if (fc.exists()) {
				byte[] image = new byte[(int) fc.fileSize()];
				InputStream inStream = fc.openInputStream();
				inStream.read(image);
				inStream.close();

				EncodedImage eimg = EncodedImage.createEncodedImage(image, 0,
						-1);
				
				if (fc != null && fc.exists()) {
					if(eimg.getHeight() > 640 && eimg.getWidth() > 480)
						fc.delete();
					if (fc.isOpen()) {
						fc.close();
					}
				}

				
				if (eimg.getWidth() > 640 && eimg.getHeight() > 480) {
					fc = (FileConnection) Connector.open("file://" + filename,
							Connector.READ_WRITE);
					if (!fc.exists())
						fc.create();
					OutputStream outStream = fc.openOutputStream();
					
					Bitmap tempBitmap = eimg.getBitmap();
					Bitmap bitmap = new Bitmap(640, 480);
					tempBitmap.scaleInto(bitmap, Bitmap.FILTER_LANCZOS);
					JPEGEncodedImage jpegImg = JPEGEncodedImage.encode(bitmap, 100);
					byte[] data = jpegImg.getData();
					outStream.write(data);
					outStream.close();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fc != null && fc.exists()) {
					if (fc.isOpen())
						fc.close();

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] getBytesFromBitmap(Bitmap bmp) {
		try {
			int height = bmp.getHeight();
			int width = bmp.getWidth();
			int[] rgbdata = new int[width * height];
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			Graphics g = new Graphics(bmp);
			bmp.getARGB(rgbdata, 0, width, 0, 0, width, height);
			for (int i = 0; i < rgbdata.length; i++) {
				if (rgbdata[i] != -1) {
					dos.writeInt(i);
					dos.flush();
					// l++;
				}
			}
			bos.flush();
			return bos.toByteArray();
		} catch (Exception ex) {
			return null;
		}
	}

}
