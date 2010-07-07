package th.co.yellowpages.zxing.client.ypandroid;

import th.co.yellowpages.zxing.client.ypandroid.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {

	protected boolean _active = true;
	protected int _splashTime = 4000; // time to display the splash screen in ms

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yp_splash);

		// thread for displaying the SplashScreen
		Thread splashTread = new Thread() {
			@Override
			public void run() {
				try {
					int waited = 0;
					while (_active && (waited < _splashTime)) {
						sleep(100);
						if (_active) {
							waited += 100;
						}
					}
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					finish();
					Intent intent = new Intent();
					intent.setClassName(SplashActivity.this, MainActivity.class
							.getName());
					startActivity(intent);
				}
			}
		};
		splashTread.start();
	}
}
