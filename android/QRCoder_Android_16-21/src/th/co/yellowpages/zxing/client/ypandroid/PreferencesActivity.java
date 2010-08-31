/*
 * Copyright (C) 2008 ZXing authors
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

package th.co.yellowpages.zxing.client.ypandroid;

import java.io.IOException;

import th.co.yellowpages.zxing.client.ypandroid.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * The main settings activity.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class PreferencesActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	static final String KEY_DECODE_1D = "preferences_decode_1D";
	static final String KEY_DECODE_QR = "preferences_decode_QR";
	static final String KEY_USE_FRONT_LIGHT = "preferences_use_front_light";
	public static final String KEY_CUSTOM_PRODUCT_SEARCH = "preferences_custom_product_search";

	static final String KEY_PLAY_BEEP = "preferences_play_beep";
	static final String KEY_BEEP_SOUND = "preferences_beep_sound";
	static final String KEY_VIBRATE = "preferences_vibrate";
	static final String KEY_COPY_TO_CLIPBOARD = "preferences_copy_to_clipboard";

	static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";
	public static final String KEY_NOT_OUR_RESULTS_SHOWN = "preferences_not_out_results_shown";

	private CheckBoxPreference decode1D;
	private CheckBoxPreference decodeQR;
	private ListPreference beebSound;
	private MediaPlayer mediaPlayer;

	private final OnCompletionListener beepListener = new BeepListener();

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.preferences);

		PreferenceScreen preferences = getPreferenceScreen();
		preferences.getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		decode1D = (CheckBoxPreference) preferences
				.findPreference(KEY_DECODE_1D);
		decodeQR = (CheckBoxPreference) preferences
				.findPreference(KEY_DECODE_QR);
		beebSound = (ListPreference) preferences.findPreference(KEY_BEEP_SOUND);

		String[] sounds = new String[4];
		sounds[0] = "Sound 1";
		sounds[1] = "Sound 2";
		sounds[2] = "Sound 3";
		sounds[3] = "Sound 4";

		String[] soundValues = new String[4];
		soundValues[0] = "1";
		soundValues[1] = "2";
		soundValues[2] = "3";
		soundValues[3] = "4";

		beebSound.setEntries(sounds);
		beebSound.setEntryValues(soundValues);
		beebSound.setDefaultValue(sounds[0]);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	// Prevent the user from turning off both decode options
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_DECODE_1D)) {
			decodeQR.setEnabled(decode1D.isChecked());
			decodeQR.setChecked(true);
		} else if (key.equals(KEY_DECODE_QR)) {
			decode1D.setEnabled(decodeQR.isChecked());
			decode1D.setChecked(true);
		}

		if (key.equals(KEY_BEEP_SOUND)) {
			String sound = sharedPreferences.getString(KEY_BEEP_SOUND, "1");
			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.s1);

			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			try {
				if (sound == "1") {
					file = getResources().openRawResourceFd(R.raw.s1);
				} else if (sound == "2") {
					file = getResources().openRawResourceFd(R.raw.s2);
				} else if (sound == "3") {
					file = getResources().openRawResourceFd(R.raw.s3);
				} else if (sound == "4") {
					file = getResources().openRawResourceFd(R.raw.s4);
				}

				mediaPlayer.setDataSource(file.getFileDescriptor(), file
						.getStartOffset(), file.getLength());
				file.close();
				// mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
				mediaPlayer.start();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private static class BeepListener implements OnCompletionListener {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.stop();
			mediaPlayer = null;
		}
	}
}
