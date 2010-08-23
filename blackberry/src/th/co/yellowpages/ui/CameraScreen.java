package th.co.yellowpages.ui;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import th.co.yellowpages.zxing.Result;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;

public class CameraScreen extends MainScreen {

	private Player _p;
	private Field _videoField;
	private VideoControl _videoControl;

	public CameraScreen() {
		try {
			_p = javax.microedition.media.Manager
					.createPlayer("capture://video?encoding=jpeg&width=1024&height=768");
			_p.realize();
			_videoControl = (VideoControl) _p.getControl("VideoControl");

			if (_videoControl != null) {
				_videoField = (Field) _videoControl.initDisplayMode(
						VideoControl.USE_GUI_PRIMITIVE,
						"net.rim.device.api.ui.Field");
				_videoControl.setDisplayFullScreen(true);
				_videoControl.setVisible(true);
				_p.start();

				if (_videoField != null) {
					add(_videoField);
				}
			}
		} catch (Exception e) {
			Dialog.alert(e.toString());
		}

	}

	protected boolean invokeAction(int action) {
		boolean handled = super.invokeAction(action);

		if (!handled) {
			if (action == ACTION_INVOKE) {
				try {
					byte[] rawImage = _videoControl.getSnapshot(null);
					System.out.println("ACTION: "+rawImage.length);
				} catch (Exception e) {
					_videoField.setFocus();
					Dialog.alert(e.toString());
				}
			}
		}
		return handled;
	}
}
