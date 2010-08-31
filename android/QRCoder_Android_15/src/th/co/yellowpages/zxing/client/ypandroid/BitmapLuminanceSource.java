package th.co.yellowpages.zxing.client.ypandroid;

import android.graphics.Bitmap;

import com.google.zxing.LuminanceSource;

public class BitmapLuminanceSource extends LuminanceSource {
	private byte[] matrix;

	protected BitmapLuminanceSource(Bitmap bitmap) {
		this(bitmap, bitmap.getWidth(), bitmap.getHeight());
	}

	protected BitmapLuminanceSource(Bitmap bitmap, int width, int height) {
		super(width, height);
		int area = width * height;

		this.matrix = new byte[area];
		int[] rgb = new int[area];

		bitmap.getPixels(rgb, 0, width, 0, 0, width, height);

		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = rgb[offset + x];
				int luminance = (306 * ((pixel >> 16) & 0xFF) + 601
						* ((pixel >> 8) & 0xFF) + 117 * (pixel & 0xFF)) >> 10;
				matrix[offset + x] = (byte) luminance;
			}
		}

		rgb = null;
	}

	@Override
	public byte[] getMatrix() {
		return matrix;
	}

	@Override
	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= getHeight()) {
			throw new IllegalArgumentException(
					"Requested row is outside the image: " + y);
		}

		int width = getWidth();
		if (row == null || row.length < width) {
			row = new byte[width];
		}

		int offset = y * width;
		System.arraycopy(this.matrix, offset, row, 0, width);

		return row;
	}
}
