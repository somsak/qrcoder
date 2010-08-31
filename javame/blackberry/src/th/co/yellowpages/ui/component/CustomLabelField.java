package th.co.yellowpages.ui.component;

import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

public class CustomLabelField extends LabelField {

	private String content;
	private String date;
	
	public CustomLabelField(String content, String date, long style) {
		super("", style);
		this.content = content;
		this.date = date;
		String text = content+"\n"+date;
		setBorder(BorderFactory.createSimpleBorder(new XYEdges(1,1,1,1)));
		setPadding(new XYEdges(2,2,2,2));
		setMargin(5, 5, 5, 5);
		super.setText(text);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
