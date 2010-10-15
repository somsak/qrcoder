package th.co.yellowpages.util;

import javax.microedition.io.HttpConnection;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;

public class YPLog implements Runnable {

	private LocationProvider locationProvider;
	private String msg;

	public YPLog(String msg) {
		this.msg = msg;
	}

	public void run() {
		try {
			String url = "http://qrcode.yellowpages.co.th/api/strlogapi.php?";
			// String url =
			// "http://garnet.cpe.ku.ac.th/~b4954113/bb/strlogapi.php?";
			initializeGPS();
			String wifi = getWiFiAddress();
			String nw = getNetwork();
			String vs = getFirmwareVersion();
			String sn = getSerialNumber();
			String lat = getLatitude();
			String lon = getLatitude();
			String ag = getUserAgent();
			String ip = getIPAddress();

			String temp = "";
			if (wifi != null)
				temp = addParameter(temp, "wifi", wifi);
			if (nw != null)
				temp = addParameter(temp, "nw", nw);
			if (vs != null)
				temp = addParameter(temp, "vs", vs);
			if (sn != null)
				temp = addParameter(temp, "sn", sn);
			if (lat != null)
				temp = addParameter(temp, "lat", lat);
			if (lon != null)
				temp = addParameter(temp, "lon", lon);
			if (ag != null)
				temp = addParameter(temp, "ag", ag);
			if (ip != null)
				temp = addParameter(temp, "ip", ip);
			if (msg != null)
				temp = addParameter(temp, "up", msg);
			url += temp.substring(1);
			System.out.println(url);
			ConnectionFactory connFact = new ConnectionFactory();
			ConnectionDescriptor connDesc = connFact.getConnection(url);

			if (connDesc != null) {
				HttpConnection httpConn = (HttpConnection) connDesc
						.getConnection();
				httpConn.setRequestMethod(HttpConnection.GET);
				int code = httpConn.getResponseCode();
				System.out.println("CODE: " + code);
			}

		} catch (Exception e) {
		}
	}

	public String urlEncode(String s) {
		StringBuffer sbuf = new StringBuffer();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int ch = s.charAt(i);
			if ('A' <= ch && ch <= 'Z') { // 'A'..'Z'
				sbuf.append((char) ch);
			} else if ('a' <= ch && ch <= 'z') { // 'a'..'z'
				sbuf.append((char) ch);
			} else if ('0' <= ch && ch <= '9') { // '0'..'9'
				sbuf.append((char) ch);
			} else if (ch == ' ') { // space
				sbuf.append('+');
			} else if (ch == '-' || ch == '_' // these characters don't need
					// encoding
					|| ch == '.' || ch == '*') {
				sbuf.append((char) ch);
			} else if (ch <= 0x007f) { // other ASCII
				sbuf.append(hex(ch));
			} else if (ch <= 0x07FF) { // non-ASCII <= 0x7FF
				sbuf.append(hex(0xc0 | (ch >> 6)));
				sbuf.append(hex(0x80 | (ch & 0x3F)));
			} else { // 0x7FF < ch <= 0xFFFF
				sbuf.append(hex(0xe0 | (ch >> 12)));
				sbuf.append(hex(0x80 | ((ch >> 6) & 0x3F)));
				sbuf.append(hex(0x80 | (ch & 0x3F)));
			}
		}
		return sbuf.toString();
	}

	// get the encoded value of a single symbol, each return value is 3
	// characters long
	static String hex(int sym) {
		return (hex.substring(sym * 3, sym * 3 + 3));
	}

	// Hex constants concatenated into a string, messy but efficient
	final static String hex = "%00%01%02%03%04%05%06%07%08%09%0a%0b%0c%0d%0e%0f%10%11%12%13%14%15%16%17%18%19%1a%1b%1c%1d%1e%1f"
			+ "%20%21%22%23%24%25%26%27%28%29%2a%2b%2c%2d%2e%2f%30%31%32%33%34%35%36%37%38%39%3a%3b%3c%3d%3e%3f"
			+ "%40%41%42%43%44%45%46%47%48%49%4a%4b%4c%4d%4e%4f%50%51%52%53%54%55%56%57%58%59%5a%5b%5c%5d%5e%5f"
			+ "%60%61%62%63%64%65%66%67%68%69%6a%6b%6c%6d%6e%6f%70%71%72%73%74%75%76%77%78%79%7a%7b%7c%7d%7e%7f"
			+ "%80%81%82%83%84%85%86%87%88%89%8a%8b%8c%8d%8e%8f%90%91%92%93%94%95%96%97%98%99%9a%9b%9c%9d%9e%9f"
			+ "%a0%a1%a2%a3%a4%a5%a6%a7%a8%a9%aa%ab%ac%ad%ae%af%b0%b1%b2%b3%b4%b5%b6%b7%b8%b9%ba%bb%bc%bd%be%bf"
			+ "%c0%c1%c2%c3%c4%c5%c6%c7%c8%c9%ca%cb%cc%cd%ce%cf%d0%d1%d2%d3%d4%d5%d6%d7%d8%d9%da%db%dc%dd%de%df"
			+ "%e0%e1%e2%e3%e4%e5%e6%e7%e8%e9%ea%eb%ec%ed%ee%ef%f0%f1%f2%f3%f4%f5%f6%f7%f8%f9%fa%fb%fc%fd%fe%ff";

	public String addParameter(String URL, String name, String value) {
		int qpos = URL.indexOf('?');
		int hpos = URL.indexOf('#');
		char sep = qpos == -1 ? '?' : '&';
		String seg = sep + urlEncode(name) + '=' + urlEncode(value);
		return hpos == -1 ? URL + seg : URL.substring(0, hpos) + seg
				+ URL.substring(hpos);
	}

	private void initializeGPS() {
		try {
			Criteria criteria = new Criteria();
			criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
			criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
			criteria.setCostAllowed(true);
			criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_MEDIUM);
			locationProvider = LocationProvider.getInstance(criteria);

			// if GPS chip is not available or if there is no satellite coverage
			// (i.e. inside a building) use Cellsite
			if (locationProvider.getState() == LocationProvider.OUT_OF_SERVICE
					|| locationProvider.getState() == LocationProvider.TEMPORARILY_UNAVAILABLE) {
				criteria.setHorizontalAccuracy(Criteria.NO_REQUIREMENT);
				criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
				criteria.setCostAllowed(true);
				criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_LOW);
				locationProvider = LocationProvider.getInstance(criteria);
			}
		} catch (Exception e) {

		}
	}

	private String getWiFiAddress() {
		try {
			return WLANInfo.getAPInfo().getSSID();
		} catch (Exception e) {
			return null;
		}
	}

	private String getNetwork() {
		try {
			return RadioInfo.getCurrentNetworkName();
		} catch (Exception e) {
			return null;
		}
	}

	private String getFirmwareVersion() {
		try {
			return DeviceInfo.getSoftwareVersion();
		} catch (Exception e) {
			return null;
		}
	}

	private String getSerialNumber() {
		try {
			return DeviceInfo.getDeviceId() + "";
		} catch (Exception e) {
			return null;
		}
	}

	private String getLatitude() {
		try {
			if (locationProvider == null)
				return null;
			Location location = locationProvider.getLastKnownLocation();
			QualifiedCoordinates coordinates = location
					.getQualifiedCoordinates();
			return coordinates.getLatitude() + "";
		} catch (Exception e) {
			return null;
		}
	}

	private String getLongitude() {
		try {
			if (locationProvider == null)
				return null;

			Location location = locationProvider.getLastKnownLocation();
			QualifiedCoordinates coordinates = location
					.getQualifiedCoordinates();
			return coordinates.getLongitude() + "";
		} catch (Exception e) {
			return null;
		}
	}

	private String getUserAgent() {
		return "BlackBerry";
	}

	/*
	 * private String getUserAgent() { String version = "";
	 * ApplicationDescriptor[] ad = ApplicationManager.getApplicationManager()
	 * .getVisibleApplications(); for (int i = 0; i < ad.length; i++) { if
	 * (ad[i].getModuleName().trim().equalsIgnoreCase( "net_rim_bb_ribbon_app"))
	 * { version = ad[i].getVersion(); break; } }
	 * 
	 * String userAgent = "Blackberry" + DeviceInfo.getDeviceName() + "/" +
	 * version + " Profile/" + System.getProperty("microedition.profiles") +
	 * " Configuration/" + System.getProperty("microedition.configuration") +
	 * " VendorID/" + Branding.getVendorId(); return userAgent;//
	 * URLencode(userAgent);
	 * 
	 * }
	 */

	private String getIPAddress() {
		String ip = new String("");

		try {
			int cni = RadioInfo.getCurrentNetworkIndex();
			int apnId = cni + 1; // cni is zero based
			byte[] ipaddr = RadioInfo.getIPAddress(apnId);
			for (int i = 0; i < ipaddr.length; i++) {
				int temp = (ipaddr[i] & 0xff);
				if (i < 3) {
					ip = ip.concat("" + temp + ".");
				} else {
					ip = ip.concat("" + temp);
				}
			}
			System.out.println("IPAddress " + ip);
		} catch (Exception e) {
			ip = null;
		}

		return ip;
	}

}
