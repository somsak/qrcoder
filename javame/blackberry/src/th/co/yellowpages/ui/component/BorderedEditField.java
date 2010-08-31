package th.co.yellowpages.ui.component;

import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.decor.BorderFactory;

public class BorderedEditField extends EditField {
	public BorderedEditField() {
		super();
		setBorder(BorderFactory.createSimpleBorder(new XYEdges(1, 1, 1, 1)));
	}

	public BorderedEditField(long style) {
		super(style);
		setBorder(BorderFactory.createSimpleBorder(new XYEdges(1, 1, 1, 1)));
	}

	public BorderedEditField(String label, String initialValue) {
		super(label, initialValue);
		setBorder(BorderFactory.createSimpleBorder(new XYEdges(1, 1, 1, 1)));
	}

	public BorderedEditField(String label, String initialValue, int maxNumChars,
			long style) {
		super(label, initialValue, maxNumChars, style);
		setBorder(BorderFactory.createSimpleBorder(new XYEdges(1, 1, 1, 1)));
	}

	public int getPreferredHeight() {
		return Font.getDefault().getHeight() * 3;
	}

	public void layout(int width, int height) {
		super.layout(width, height);
		if (getExtent().height < getPreferredHeight())
			setExtent(width, getPreferredHeight());
	}
}
