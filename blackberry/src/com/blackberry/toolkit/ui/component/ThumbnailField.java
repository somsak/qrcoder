package com.blackberry.toolkit.ui.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.MIMETypeAssociations;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.util.Arrays;

import com.blackberry.toolkit.ui.container.TableLayoutManager;
import com.blackberry.toolkit.ui.images.ImageManipulator;

/**
 * Generate and display a set of Thumbnails in a table.
 * 
 * @author twindsor
 * @version 1.2 (January 2010)
 */
public class ThumbnailField extends TableLayoutManager {

	// presentation settings
	public static final int DEFAULT_COLUMNS = 4;
	public static final int DEFAULT_PADDING = 4;

	private int _columns = DEFAULT_COLUMNS;
	private int _padding = DEFAULT_PADDING;
	private int _width;
	// Is this a picker type of field or just display
	private boolean _selectable;

	// image sources
	private Bitmap[] _images;
	private String _directory;
	private boolean _recursive;
	private String[] _files;
	// default to using the array
	private boolean _useArray = true;

	// Listener
	private Hashtable _fieldMapping = new Hashtable();
	private Listener _listener = null;

	// resources
	private ImageManipulator _manipulator = new ImageManipulator(null);
	private ImageIterator _iterator = new ImageIterator();
	private static Bitmap _loadingImage = Bitmap.getBitmapResource("clock.PNG");
	private static BitmapField _loading = new BitmapField(_loadingImage, Field.FIELD_HCENTER | Field.FIELD_VCENTER);

	/**
	 * Create a ThumbnailField from a file system directory.
	 * 
	 * @param style
	 *            Manager style settings.
	 * @param columns
	 *            Number of columns to fit in space.
	 * @param padding
	 *            the padding to put around the images. Recommend > 0 for
	 *            selectable displays to show highlight.
	 * @param width
	 *            the width that should be used.
	 * @param selectable
	 *            true makes this a focusable and selectable display.
	 * @param directory
	 *            the file root to load images from.
	 * @param recursive
	 *            true to recurse the directory or false to only load files in
	 *            the immediate root.
	 * @throws Exception
	 *             If an error occurs while loading images.
	 * @return ThumbnailField of thumbnails created from images in the
	 *         directory.
	 * @since 1.0
	 */
	public static ThumbnailField createFromDirectory(long style, int columns, int padding, int width, boolean selectable, String directory,
			boolean recursive) throws Exception {
		ThumbnailField table = createTable(columns, padding, width, style);
		table._selectable = selectable;
		table._useArray = false;
		table.setImages(directory, recursive);
		return table;

	}

	/**
	 * Create a ThumbnailField from an array of images.
	 * 
	 * @param style
	 *            Manager style settings.
	 * @param columns
	 *            Number of columns to fit in space.
	 * @param padding
	 *            the padding to put around the images. Recommend > 0 for
	 *            selectable displays to show highlight.
	 * @param width
	 *            the width that should be used.
	 * @param selectable
	 *            true makes this a focusable and selectable display.
	 * @param images
	 *            array of Bitmaps to thumbnail.
	 * @return ThumbnailField of thumbnails created from images in the array.
	 * @since 1.0
	 */
	public static ThumbnailField createFromArray(long style, int columns, int padding, int width, boolean selectable, Bitmap[] images) {
		ThumbnailField table = createTable(columns, padding, width, style);
		table._selectable = selectable;
		table._useArray = true;
		table.setImages(images);
		return table;
	}

	/**
	 * Internal Constructor.
	 * 
	 * @param columnstyles
	 * @param columnwidths
	 * @param horizontalpadding
	 * @param style
	 * @since 1.0
	 */
	protected ThumbnailField(int[] columnstyles, int[] columnwidths, int horizontalpadding, long style) {
		super(columnstyles, columnwidths, horizontalpadding, style);
	}

	/**
	 * Internal Constructor.
	 * 
	 * @param columnstyles
	 * @param style
	 * @since 1.0
	 */
	protected ThumbnailField(int[] columnstyles, long style) {
		super(columnstyles, style);
	}

	/**
	 * Set the images to those from this array. Replaces the current images.
	 * 
	 * @param bitmaps
	 *            array of Bitmaps.
	 * @since 1.0
	 */
	public void setImages(Bitmap[] bitmaps) {
		_images = bitmaps;
		_useArray = true;
		replaceImages();
	}

	/**
	 * Set the images to those from this directory. Replaces the current images.
	 * 
	 * @param directory
	 *            file system directory. Absolute path.
	 * @param recursive
	 *            true to recurse.
	 * @throws Exception
	 *             if an error occurs reading files.
	 * @since 1.0
	 */
	public void setImages(String directory, boolean recursive) throws Exception {
		_directory = directory;
		_recursive = recursive;
		_useArray = false;
		_iterator.gatherFiles();
		replaceImages();
	}

	/**
	 * Clear the images and add them again. Used by the setImages methods.
	 * 
	 * @since 1.0
	 */
	private void replaceImages() {
		deleteAll();
		_fieldMapping.clear();
		addImagesToTable();
	}

	/**
	 * Calculate the size of each thumbnail. Based on padding, and number of
	 * columns.
	 * 
	 * @return width of each column in pixels.
	 * @since 1.0
	 */
	private int calculateThumbnailSize() {
		int totalPadding = _padding * _columns;
		int singleColumn = (_width - totalPadding) / _columns;
		return singleColumn;
	}

	/**
	 * Internal construction of the table.
	 * 
	 * @param columns
	 *            number of columns.
	 * @param padding
	 *            padding between images.
	 * @param width
	 *            width of the table.
	 * @param style
	 *            Manager style settings.
	 * @return Empty table for filling with images.
	 * @since 1.0
	 */
	protected static ThumbnailField createTable(int columns, int padding, int width, long style) {
		int[] columnStyles = new int[columns];
		int[] columnWidths = new int[columns];
		// First column will be zero width but that adds padding.
		Arrays.fill(columnStyles, USE_PREFERRED_SIZE);
		ThumbnailField table = new ThumbnailField(columnStyles, columnWidths, 0, style);
		table._columns = columns;
		table._padding = padding;
		table._width = width;
		return table;
	}

	/**
	 * Add images to the table iterating over the list. Starts a separate thread
	 * to do this as loading can take some time.
	 * 
	 * @since 1.0
	 */
	private void addImagesToTable() {
		new Thread() {
			public void run() {
				try {
					_iterator.reset();
					Bitmap current = null;
					int bitmapSize = calculateThumbnailSize();
					int item = 0;
					_loading.setSpace((bitmapSize - _loadingImage.getHeight()) / 2, (bitmapSize - _loadingImage.getWidth()) / 2);
					while (_iterator.hasMoreImages()) {
						synchronized (UiApplication.getEventLock()) {
							ThumbnailField.this.add(_loading);
						}
						current = _iterator.getNextImage();
						// resize and add to the table in a BitmapField
						Bitmap thumbnail = new Bitmap(bitmapSize, bitmapSize);
						_manipulator.setBitmap(current);
						_manipulator.scaleInto(thumbnail, 0, ImageManipulator.SCALE_TO_FILL);
						BitmapField field;
						if (isSelectable()) {
							field = new BitmapField(thumbnail, Field.FOCUSABLE);
						} else {
							field = new BitmapField(thumbnail);
						}
						field.setSpace(getPaddingHalf(), getPaddingHalf());
						synchronized (UiApplication.getEventLock()) {
							ThumbnailField.this.replace(_loading, field);
						}
						// add to Mapping
						_fieldMapping.put(field, new FieldMapData(thumbnail, item, _iterator.getCurrentFilename()));
						item++;
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}.start();

	}

	public int getPadding() {
		return _padding;
	}

	/**
	 * Get the rounded down padding for one side of an image.
	 * 
	 * @since 1.0
	 * @return 1/2 of padding as an integer, floored.
	 */
	public int getPaddingHalf() {
		int pad = 0;
		try {
			pad = (int) (_padding / 2);
		} catch (Exception e) {
		}
		return pad;
	}

	public void setPadding(int padding) {
		_padding = padding;
	}

	public void setWidth(int width) {
		_width = width;
	}

	public boolean isSelectable() {
		return _selectable;
	}

	public void setSelectable(boolean selectable) {
		_selectable = selectable;
	}

	public Listener getListener() {
		return _listener;
	}

	/**
	 * Replaces the listner and will set selectable true if the listener exists.
	 * 
	 * @param listener
	 *            Listener to use.
	 * @since 1.0
	 */
	public void setListener(Listener listener) {
		if (listener != null) {
			_selectable = true;
		}
		_listener = listener;
	}

	/**
	 * Override to call the listener if this ThumbnailField has one.
	 * 
	 * @since 1.0
	 */
	protected boolean invokeAction(int action) {
		if (isSelectable() && _listener != null) {
			Field field = getLeafFieldWithFocus();
			if (field instanceof BitmapField) {
				BitmapField bitmapField = (BitmapField) field;
				FieldMapData data = (FieldMapData) _fieldMapping.get(bitmapField);
				_listener.thumbnailSelected(data.thumbnail, data.index, data.filename);
				return true;
			}
		}
		return super.invokeAction(action);
	}

	/**
	 * Internal class for abstracting file system vs Bitmap array use.
	 * 
	 * @author twindsor
	 * @since 1.0
	 */
	private class ImageIterator {
		private int _index;

		private String FILE_SEPARATOR = System.getProperty("file.separator");
		private String PARENT_DIRECTORY = "..";
		private String FILE_PROTOCOL = "file://";

		public void reset() {
			_index = 0;
		}

		/**
		 * Get the next image from the list.
		 * 
		 * @return the next Bitmap from the list of images. Will read from
		 *         storage if necessary.
		 * @throws Exception
		 *             if a file system error occurs.
		 * @since 1.0
		 */
		public Bitmap getNextImage() throws Exception {
			Bitmap nextImage = null;
			if (_useArray) {
				if (_images != null && _index < _images.length) {
					nextImage = _images[_index];
					_index++;
				}
			} else {
				if (_files != null && _index < _files.length) {
					nextImage = readBitmapFromFile(_files[_index]);
					_index++;
				}
			}
			return nextImage;
		}

		/**
		 * Get Current filename of the iterator.
		 * 
		 * @return filename from the index, null if an array is used.
		 * @since 1.0
		 */
		public String getCurrentFilename() {
			if (!_useArray && _files != null) {
				if (_index == 0) {
					return _files[0];
				} else {
					return _files[_index - 1];
				}
			}
			return null;
		}

		/**
		 * Check for more images in the list.
		 * 
		 * @return true if more images remain.
		 * @since 1.0
		 */
		public boolean hasMoreImages() {
			if (_useArray) {
				if (_images != null && _index < _images.length) {
					return true;
				}
			} else {
				if (_files != null && _index < _files.length) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Gather the file names from the directory.
		 * 
		 * @throws IOException
		 *             if a file system error occurs.
		 * @since 1.0
		 */
		public void gatherFiles() throws IOException {
			Vector files = new Vector();
			if (_directory != null) {
				FileConnection dir = (FileConnection) Connector.open(_directory, Connector.READ);
				if (dir.isDirectory()) {
					recurseDirectory(dir, files);
				}
				dir.close();
			}
			_files = new String[files.size()];
			files.copyInto(_files);
		}

		/**
		 * Read a directory (not necessarily recursively).
		 * 
		 * @param dir
		 *            the FileConnection open to the directory.
		 * @param files
		 *            the list of files already found.
		 * @throws IOException
		 *             if a file system error occurs.
		 * @since 1.0
		 */
		private void recurseDirectory(FileConnection dir, Vector files) throws IOException {
			Enumeration list = dir.list();
			while (list.hasMoreElements()) {
				String file = (String) list.nextElement();
				if (file.endsWith(FILE_SEPARATOR)) {
					if (_recursive) {
						// set the fileConnection, recurse and return
						// connection.
						dir.setFileConnection(file);
						recurseDirectory(dir, files);
						dir.setFileConnection(PARENT_DIRECTORY);
					}
				} else {
					if (MIMETypeAssociations.getMediaType(file) == MIMETypeAssociations.MEDIA_TYPE_IMAGE) {
						// Image file, add to list
						StringBuffer filename = new StringBuffer();
						filename.append(FILE_PROTOCOL).append(dir.getPath()).append(dir.getName()).append(file);
						files.addElement(filename.toString());

					}
				}
			}
		}

		/**
		 * Read a Bitmap given the filesystem name.
		 * 
		 * @param filename
		 *            filename to open and load.
		 * @return image read from the file.
		 * @throws IOException
		 *             if a file system error occurs.
		 * @since 1.0
		 */
		private Bitmap readBitmapFromFile(String filename) throws IOException {
			Bitmap bitmap = null;
			byte[] data;
			if (filename != null) {
				FileConnection file = (FileConnection) Connector.open(filename, Connector.READ);
				int fileSize = (int) file.fileSize();
				if (fileSize > 0) {
					data = new byte[fileSize];
					InputStream input = file.openInputStream();
					input.read(data);
					Thread.yield();
					EncodedImage image = EncodedImage.createEncodedImage(data, 0, data.length);
					bitmap = image.getBitmap();
					input.close();
				}
				file.close();
			}
			return bitmap;
		}

	}

	/**
	 * Class to store thumbnail details.
	 * 
	 * @author twindsor
	 * @since 1.0
	 */
	private class FieldMapData {
		/**
		 * Simple Contstructor.
		 * 
		 * @param thumbnail
		 *            the scaled Bitmap.
		 * @param item
		 *            the index within the ThumbnailField.
		 * @param filename
		 *            the filename if a directory was used, should remain null
		 *            otherwise.
		 * @since 1.0
		 */
		public FieldMapData(Bitmap thumbnail, int item, String filename) {
			this.thumbnail = thumbnail;
			this.index = item;
			this.filename = filename;
		}

		public Bitmap thumbnail;
		public int index;
		public String filename;
	}

	/**
	 * Interface to implement for handling selection of images within a
	 * Thumbnail field.
	 * 
	 * @author twindsor
	 * @since 1.0
	 */
	public static interface Listener {
		/**
		 * Method called when a user selects a thumbnail.
		 * 
		 * @param thumbnail
		 *            the thumbnail sized bitmap.
		 * @param index
		 *            within the list of images or files passed.
		 * @param filename
		 *            the filename of the image. Will be null if the
		 *            {@link ThumbnailField} was created using an array of
		 *            Bitmaps.
		 * @since 1.0
		 */
		public void thumbnailSelected(Bitmap thumbnail, int index, String filename);

		/**
		 * Called when the Picker is closed without selecting a thumbnail. Never
		 * called by the basic ThumbnailField.
		 * 
		 * @since 1.1
		 */
		public void selectionCanceled();
	}

}
