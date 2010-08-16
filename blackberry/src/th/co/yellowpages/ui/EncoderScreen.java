package th.co.yellowpages.ui;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.component.ObjectChoiceField;

public class EncoderScreen extends MainScreen {

	private static final String[] encodeType = { "Text", "Contact information",
			"Email address", "Phone number", "URL" };

	private ObjectChoiceField encodingTypeChoiceField;
	private VerticalFieldManager bodyVfm;

	public EncoderScreen() {
		LabelField title = new LabelField("QRCoder", DrawStyle.ELLIPSIS
				| USE_ALL_WIDTH);
		title.setMargin(5, 0, 0, 5);
		setTitle(title);

		Manager vfm = new VerticalFieldManager(FIELD_HCENTER | USE_ALL_WIDTH);

		encodingTypeChoiceField = new ObjectChoiceField("Select encode type:",
				encodeType);
		encodingTypeChoiceField.setChangeListener(new ButtonListener(this));
		vfm.add(encodingTypeChoiceField);

		bodyVfm = new VerticalFieldManager(FIELD_HCENTER | USE_ALL_WIDTH);
		ClearBodyVFM();
		initializeText();

		ButtonField createQRCodeButton = new ButtonField("Create QR Code",
				FIELD_HCENTER | USE_ALL_WIDTH | ButtonField.CONSUME_CLICK);
		createQRCodeButton.setChangeListener(null);
		
		add(vfm);
		add(bodyVfm);
		add(createQRCodeButton);
	}

	private void ClearBodyVFM() {
		if (bodyVfm != null)
			bodyVfm.deleteAll();
	}

	private void initializeText() {

	}

	private void initializeContactInfo() {

	}

	private void initializeEmail() {

	}

	private void initializePhone() {

	}

	private void initializeURL() {

	}

	/**
	 * Listens for button clicks and executes the appropriate action.
	 */
	private final class ButtonListener implements FieldChangeListener {
		private final Screen screen;

		private ButtonListener(Screen screen) {
			this.screen = screen;
		}

		public void fieldChanged(Field field, int context) {

		}
	}
}
