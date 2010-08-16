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

import java.util.Date;

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
    long lookUSN = FileSystemJournal.getNextUSN() - 1; // the last file added to the filesystem
    Log.debug("lookUSN: " + lookUSN);
    FileSystemJournalEntry entry = FileSystemJournal.getEntry(lookUSN);
    if (entry != null && entry.getEvent() == FileSystemJournalEntry.FILE_ADDED) {
      Log.info("Got file: " + entry.getPath() + " @: " + new Date());
      screen.imageSaved(entry.getPath());
    }
  }

}
