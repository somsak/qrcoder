package th.co.yellowpages.javame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

public class YPRecord {

	public static String TYPE_WEB = "0";
	public static String TYPE_TEL = "1";
	public static String TYPE_EMAIL = "2";
	public static String TYPE_MESSAGE = "3";
	public static String TYPE_TEXT = "4";

	private String qrContent;
	private String type;
	private String datetime;

	public YPRecord() {

	}

	public YPRecord(String qrContent, String type) {
		try {
		this.qrContent = qrContent;
		this.type = type;

		Date date = new Date();
		this.datetime = Long.toString(date.getTime());
		} catch(Exception e) {
			System.out.println("Try catch block from YPRecord.");
			System.out.println(e.toString());
		}
	}

	public String getQRContent() {
		return qrContent;
	}

	public String getType() {
		return type;
	}

	public String getDatetime() {
		return datetime;
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(
				byteArrayOutputStream);

		dataOutputStream.writeUTF(qrContent);
		dataOutputStream.writeUTF(type);
		dataOutputStream.writeUTF(datetime);
		dataOutputStream.flush();

		return byteArrayOutputStream.toByteArray();
	}

	public void fromByteStream(byte[] data) throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				data);
		DataInputStream dataInputStream = new DataInputStream(
				byteArrayInputStream);

		qrContent = dataInputStream.readUTF();
		type = dataInputStream.readUTF();
		datetime = dataInputStream.readUTF();
	}

	public String toString() {
		return qrContent + ": " + type;
	}
}
