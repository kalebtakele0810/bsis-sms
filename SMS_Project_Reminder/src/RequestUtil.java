import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;

public class RequestUtil {
	public static String getDonor(String donor_id, String GET_URL, String authUser, String authPassword)
			throws IOException {
		String donorPhoneNumber = "";

		URL obj = new URL(GET_URL + "/donors/" + donor_id);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		String encoding = DatatypeConverter.printBase64Binary((authUser + ":" + authPassword).getBytes("UTF-8"));
		con.setRequestProperty("Authorization", "Basic " + encoding);

		int responseCode = con.getResponseCode();

		///////////////////////////

		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			JSONObject json = new JSONObject(response.toString());
			if (json.has("donor")) {
				JSONObject donorPhone = json.getJSONObject("donor");
				if (donorPhone.has("contact")) {
					JSONObject donorContact = donorPhone.getJSONObject("contact");
					if (donorContact.has("mobileNumber") && !donorContact.isNull("mobileNumber")) {
						donorPhoneNumber = (String) donorContact.get("mobileNumber");
					}
				}

			}
			in.close();
		}
		return donorPhoneNumber;

	}

	public static Boolean sendSMS(String smsServer, String text, String phoneNumber) throws IOException {
		URL obj = new URL(smsServer + "&text=" + URLEncoder.encode(text, "UTF-8") + "&to="
				+ URLEncoder.encode(phoneNumber, "UTF-8"));
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		boolean status = false;
		if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
			status = true;
		}
		return status;
	}

}
