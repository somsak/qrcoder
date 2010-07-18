package th.co.yellowpages.javame;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;

class MainCanvas extends Canvas {

	public static final int ACTION_SCAN = 0;
	public static final int ACTION_CREATE = 1;
	public static final int ACTION_HISTORY = 2;
	public static final int ACTION_CHOOSE = 3;

	private YPZXingMIDlet ypZXingMIDlet;
	private Display display;

	private Image ypLogo;
	private Image createQRIcon;
	private Image historyIcon;
	private Image scanIcon;
	private Image createQRIconSelected;
	private Image historyIconSelected;
	private Image scanIconSelected;
	private Image createQRIconPaint;
	private Image historyIconPaint;
	private Image scanIconPaint;
	private Image chooseIcon;
	private Image chooseIconSelected;

	private int currentAction = 0;
	private Image chooseIconPaint;

	MainCanvas(YPZXingMIDlet yPZXingMIDlet) {
		this.ypZXingMIDlet = yPZXingMIDlet;
		display = yPZXingMIDlet.getDisplay();

		try {
			ypLogo = Image.createImage("/res/zxing-icon.png");

			scanIcon = Image.createImage("/res/zxing-icon.png");
			scanIconSelected = Image.createImage("/res/face-smile.png");

			createQRIcon = Image.createImage("/res/zxing-icon.png");
			createQRIconSelected = Image.createImage("/res/face-smile.png");

			historyIcon = Image.createImage("/res/zxing-icon.png");
			historyIconSelected = Image.createImage("/res/face-smile.png");

			chooseIcon = Image.createImage("/res/zxing-icon.png");
			chooseIconSelected = Image.createImage("/res/face-smile.png");
		} catch (IOException e) {
			throw new RuntimeException("Unable to load Image: " + e);
		}

		updateSelectedIcon();
	}

	public int getCurrentAction() {
		return currentAction;
	}

	public void keyPressed(int key) {
		if (key == -3 && currentAction > 0) {
			currentAction--;
		} else if (key == -4 && currentAction < 3) {
			currentAction++;
		} else if (key == -5) {
			switch (currentAction) {
//#ifdef polish.api.mmapi
			case ACTION_SCAN:
				ypZXingMIDlet.showVideoCanvas();
				break;
//#endif
			case ACTION_CREATE:
				ypZXingMIDlet.showCreateQRCanvas();
				break;
			case ACTION_HISTORY:
				ypZXingMIDlet.showGenerateHistoryForm();
				break;
			case ACTION_CHOOSE:
				ypZXingMIDlet.showChooseImageForm();
				break;
			}

		}
		System.out.println("Key " + key);
		System.out.println("Current selected action " + currentAction);

		updateSelectedIcon();
	}

	public void paint(Graphics g) {
		int topMargin = 3;
		int bottomMargin = 3;
		int canvasHeigth = getHeight();
		int canvasWidth = getWidth();

		g.setGrayScale(255);
		g.fillRect(0, 0, canvasWidth, canvasHeigth);

		g.setColor(0, 0, 0);
		g.fillRect(0, 0, canvasWidth, 70);

		g.setColor(255, 0, 0);
		g.fillRect(0, canvasHeigth - 70, canvasWidth, 70);

		// Logo
		g.drawImage(ypLogo, getWidth() / 2, topMargin, Graphics.TOP
				| Graphics.HCENTER);

		// Menu
//#ifdef polish.api.mmapi
		g.drawImage(scanIconPaint, canvasWidth / 2 - 70, canvasHeigth
				- bottomMargin, Graphics.BOTTOM | Graphics.HCENTER);
//#endif
		g.drawImage(createQRIconPaint, canvasWidth / 2, canvasHeigth
				- bottomMargin, Graphics.BOTTOM | Graphics.HCENTER);
		g.drawImage(historyIconPaint, canvasWidth / 2 + 70, canvasHeigth
				- bottomMargin, Graphics.BOTTOM | Graphics.HCENTER);
		g.drawImage(chooseIconPaint, canvasWidth / 2 + 80, canvasHeigth
				- bottomMargin, Graphics.BOTTOM | Graphics.HCENTER);
	}

	public void setCurrentAction(int action) {
		this.currentAction = action;
	}

	public void stop() {
		display.setCurrent(this);
	}

	private void updateSelectedIcon() {
		scanIconPaint = scanIcon;
		createQRIconPaint = createQRIcon;
		historyIconPaint = historyIcon;
		chooseIconPaint = chooseIcon;

		switch (this.currentAction) {
//#ifdef polish.api.mmapi
		case MainCanvas.ACTION_SCAN:
			scanIconPaint = scanIconSelected;
			break;
//#endif
		case MainCanvas.ACTION_CREATE:
			createQRIconPaint = createQRIconSelected;
			break;
		case MainCanvas.ACTION_HISTORY:
			historyIconPaint = historyIconSelected;
			break;
		case MainCanvas.ACTION_CHOOSE:
			chooseIconPaint = chooseIconSelected;
			break;
		}
		repaint();
	}
}
