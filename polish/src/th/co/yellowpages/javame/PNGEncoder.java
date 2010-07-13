package th.co.yellowpages.javame;

/*
 * Minimal PNG encoder to create PNG streams (and MIDP images) from RGBA arrays.
 * 
 * Copyright 2006-2009 Christian Fröschlin
 * 
 * www.chrfr.de
 * 
 * 
 * Changelog:
 * 
 * 09/22/08: Fixed Adler checksum calculation and byte order for storing length
 * of zlib deflate block. Thanks to Miloslav Ruzicka for noting this.
 * 
 * 05/12/09: Split PNG and ZLIB functionality into separate classes. Added
 * support for images > 64K by splitting the data into multiple uncompressed
 * deflate blocks.
 * 
 * 03/19/10: Re-packaged, and modified interface to be more MIDP-2 friendly,
 * using int[] rather than byte[] data.  Alpha channel is now optional, to
 * allow a smaller output size.  New toPNG() method works directly from an
 * Image object. (Graham Hughes, for Forum Nokia)
 * 
 * Terms of Use:
 * 
 * You may use the PNG encoder free of charge for any purpose you desire, as
 * long as you do not claim credit for the original sources and agree not to
 * hold me responsible for any damage arising out of its use.
 * 
 * If you have a suitable location in GUI or documentation for giving credit,
 * I'd appreciate a mention of
 * 
 * PNG encoder (C) 2006-2009 by Christian Fröschlin, www.chrfr.de
 * 
 * but that's not mandatory.
 * 
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.lcdui.Image;

public class PNGEncoder {
	private static final byte[] SIGNATURE = new byte[] { (byte) 137, (byte) 80,
			(byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10 };

	/**
	 * Generate a PNG data stream from a pixel array.
	 * <p>
	 * Setting processAlpha to false will result in a PNG file that contains no
	 * transparency information, but may be up to 25% smaller.
	 * <p>
	 * The pixel array must contain (width * height) pixels.
	 * 
	 * @param width
	 *            width of image, in pixels
	 * @param height
	 *            height of image, in pixels
	 * @param argb
	 *            pixel array, as populated from Image.getRGB()
	 * @param processAlpha
	 *            true if you want to keep alpha channel data
	 * @return PNG data in a byte[]
	 * @throws IllegalArgumentException
	 *             if the size of the pixel array does not match the specified
	 *             width and height
	 */
	public static byte[] toPNG(int width, int height, int[] argb,
			boolean processAlpha) throws IllegalArgumentException {
		ByteArrayOutputStream png;
		try {
			byte[] header = createHeaderChunk(width, height, processAlpha);
			byte[] data = createDataChunk(width, height, argb, processAlpha);
			byte[] trailer = createTrailerChunk();

			png = new ByteArrayOutputStream(SIGNATURE.length + header.length
					+ data.length + trailer.length);
			png.write(SIGNATURE);
			png.write(header);
			png.write(data);
			png.write(trailer);
		} catch (IOException ioe) {
			// none of the code should ever throw an IOException
			throw new IllegalStateException("Unexpected " + ioe);
		}
		return png.toByteArray();
	}

	/**
	 * Generate a PNG data stream from an Image object.
	 * 
	 * @param img
	 *            source Image
	 * @param processAlpha
	 *            true if you want to keep the alpha channel data
	 * @return PNG data in a byte[]
	 */
	public static byte[] toPNG(Image img, boolean processAlpha) {
		int width = img.getWidth();
		int height = img.getHeight();
		int[] argb = new int[width * height];
		img.getRGB(argb, 0, width, 0, 0, width, height);
		// allow garbage collection, if this is the only reference
		img = null;
		return toPNG(width, height, argb, processAlpha);
	}

	private static byte[] createHeaderChunk(int width, int height,
			boolean processAlpha) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(13);
		DataOutputStream chunk = new DataOutputStream(baos);
		chunk.writeInt(width);
		chunk.writeInt(height);
		chunk.writeByte(8); // Bitdepth
		chunk.writeByte(processAlpha ? 6 : 2); // Colortype ARGB or RGB
		chunk.writeByte(0); // Compression
		chunk.writeByte(0); // Filter
		chunk.writeByte(0); // Interlace
		return toChunk("IHDR", baos.toByteArray());
	}

	private static byte[] createDataChunk(int width, int height, int[] argb,
			boolean processAlpha) throws IOException, IllegalArgumentException {
		if (argb.length != (width * height)) {
			throw new IllegalArgumentException(
					"array size does not match image dimensions");
		}
		int source = 0;
		int dest = 0;
		byte[] raw = new byte[(processAlpha ? 4 : 3) * (width * height)
				+ height];
		for (int y = 0; y < height; y++) {
			raw[dest++] = 0; // No filter
			for (int x = 0; x < width; x++) {
				int pixel = argb[source++];
				raw[dest++] = (byte) (pixel >> 16); // red
				raw[dest++] = (byte) (pixel >> 8); // green
				raw[dest++] = (byte) (pixel); // blue
				if (processAlpha) {
					raw[dest++] = (byte) (pixel >> 24); // alpha
				}
			}
		}
		return toChunk("IDAT", toZLIB(raw));
	}

	private static byte[] createTrailerChunk() throws IOException {
		return toChunk("IEND", new byte[] {});
	}

	private static byte[] toChunk(String id, byte[] raw) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(raw.length + 12);
		DataOutputStream chunk = new DataOutputStream(baos);

		chunk.writeInt(raw.length);

		byte[] bid = new byte[4];
		for (int i = 0; i < 4; i++) {
			bid[i] = (byte) id.charAt(i);
		}

		chunk.write(bid);

		chunk.write(raw);

		int crc = 0xFFFFFFFF;
		crc = updateCRC(crc, bid);
		crc = updateCRC(crc, raw);
		chunk.writeInt(~crc);

		return baos.toByteArray();
	}

	private static int[] crcTable = null;

	private static void createCRCTable() {
		crcTable = new int[256];

		for (int i = 0; i < 256; i++) {
			int c = i;
			for (int k = 0; k < 8; k++) {
				c = ((c & 1) > 0) ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
			}
			crcTable[i] = c;
		}
	}

	private static int updateCRC(int crc, byte[] raw) {
		if (crcTable == null) {
			createCRCTable();
		}

		for (int i = 0; i < raw.length; i++) {
			crc = crcTable[(crc ^ raw[i]) & 0xFF] ^ (crc >>> 8);
		}

		return crc;
	}

	/*
	 * This method is called to encode the image data as a zlib block as
	 * required by the PNG specification. This file comes with a minimal ZLIB
	 * encoder which uses uncompressed deflate blocks (fast, short, easy, but no
	 * compression). If you want compression, call another encoder (such as
	 * JZLib?) here.
	 */
	private static byte[] toZLIB(byte[] raw) throws IOException {
		return ZLIB.toZLIB(raw);
	}
}

class ZLIB {
	private static final int BLOCK_SIZE = 32000;

	public static byte[] toZLIB(byte[] raw) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(raw.length + 6
				+ (raw.length / BLOCK_SIZE) * 5);
		DataOutputStream zlib = new DataOutputStream(baos);

		byte tmp = (byte) 8;
		zlib.writeByte(tmp); // CM = 8, CMINFO = 0
		zlib.writeByte((31 - ((tmp << 8) % 31)) % 31); // FCHECK(FDICT/FLEVEL=0)

		int pos = 0;
		while (raw.length - pos > BLOCK_SIZE) {
			writeUncompressedDeflateBlock(zlib, false, raw, pos,
					(char) BLOCK_SIZE);
			pos += BLOCK_SIZE;
		}

		writeUncompressedDeflateBlock(zlib, true, raw, pos,
				(char) (raw.length - pos));

		// zlib check sum of uncompressed data
		zlib.writeInt(calcADLER32(raw));

		return baos.toByteArray();
	}

	private static void writeUncompressedDeflateBlock(DataOutputStream zlib,
			boolean last, byte[] raw, int off, char len) throws IOException {
		zlib.writeByte((byte) (last ? 1 : 0)); // Final flag, Compression type 0
		zlib.writeByte((byte) (len & 0xFF)); // Length LSB
		zlib.writeByte((byte) ((len & 0xFF00) >> 8)); // Length MSB
		zlib.writeByte((byte) (~len & 0xFF)); // Length 1st complement LSB
		zlib.writeByte((byte) ((~len & 0xFF00) >> 8)); // Length 1st complement
														// MSB
		zlib.write(raw, off, len); // Data
	}

	private static int calcADLER32(byte[] raw) {
		int s1 = 1;
		int s2 = 0;
		for (int i = 0; i < raw.length; i++) {
			int abs = raw[i] >= 0 ? raw[i] : (raw[i] + 256);
			s1 = (s1 + abs) % 65521;
			s2 = (s2 + s1) % 65521;
		}
		return (s2 << 16) + s1;
	}
}