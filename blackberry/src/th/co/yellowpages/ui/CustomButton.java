package th.co.yellowpages.ui;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;

public class CustomButton extends Field implements DrawStyle {

	private Bitmap _currentPicture;
	private Bitmap _onPicture; // image for "in focus"
	private Bitmap _offPicture; // image for "not in focus"
	private String _label;
	private int _width;
	private int _height;

	public CustomButton(String label, String onImage, String offImage, long style) {
		super(style);
		_offPicture = Bitmap.getBitmapResource(offImage);
		_onPicture = Bitmap.getBitmapResource(onImage);
		_currentPicture = _offPicture;
		_label = label;
		_width = _currentPicture.getWidth();
		_height = _currentPicture.getHeight();
	}

	public String getLabel(){
		return _label;
	}
	
	public int getPreferredHeight() {
		return _height;
	}

	public int getPreferredWidth() {
		return _width;
	}

	public boolean isFocusable() {
		return true;
	}

	// Override function to switch picture
	protected void onFocus(int direction) {
		_currentPicture = _onPicture;
		invalidate();
	}

	// Override function to switch picture
	protected void onUnfocus() {
		_currentPicture = _offPicture;
		invalidate();
	}

	protected void layout(int width, int height) {
		setExtent(Math.min(width, getPreferredWidth()), Math.min(height,
				getPreferredHeight()));
	}

	// update the fieldchange
	protected void fieldChangeNotify(int context) {
		try {
			this.getChangeListener().fieldChanged(this, context);
		} catch (Exception exception) {
		}
	}

	// Since button is rounded we need to fill corners with dark color to match
	protected void paint(Graphics graphics) {
		graphics.setColor(YPMainScreen.BACKGROUND_COLOR);
		graphics.fillRect(0, 0, getWidth(), getHeight());
		graphics.drawBitmap(0, 0, getWidth(), getHeight(), _currentPicture, 0,
				0);
	}

	// Listen for navigation Click
	protected boolean navigationClick(int status, int time) {
		fieldChangeNotify(1);
		return true;
	}

}
