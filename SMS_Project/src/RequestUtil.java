import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;

public class RequestUtil {
	public static Donor getDonor(String din, String GET_URL, String authUser, String authPassword) throws IOException {
		Donor donor = null;

		URL obj = new URL(GET_URL + "/donors/search?donationIdentificationNumber=" + din);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		String encoding = DatatypeConverter.printBase64Binary((authUser + ":" + authPassword).getBytes("UTF-8"));
		con.setRequestProperty("Authorization", "Basic " + encoding);

		int responseCode = con.getResponseCode();
		// System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			JSONObject json = new JSONObject(response.toString());
			JSONArray donors = json.getJSONArray("donors");
			if (!donors.isEmpty()) {

				String idNum = ((JSONObject) donors.getJSONObject(0)).getString("id");
				String name = ((JSONObject) donors.getJSONObject(0)).getString("firstName");
				name = name + " " + ((JSONObject) donors.getJSONObject(0)).getString("middleName");
				name = name + " " + ((JSONObject) donors.getJSONObject(0)).getString("lastName");
				donor = new Donor(idNum, name, "", "", "", "", false);
			}
			if (donor != null) {

				obj = new URL(GET_URL + "/donors/" + donor.getId());
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				encoding = DatatypeConverter.printBase64Binary((authUser + ":" + authPassword).getBytes("UTF-8"));
				con.setRequestProperty("Authorization", "Basic " + encoding);

				responseCode = con.getResponseCode();

				///////////////////////////

				if (responseCode == HttpURLConnection.HTTP_OK) { // success
					in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					response = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}

					json = new JSONObject(response.toString());
					if (json.has("donor")) {
						JSONObject donorPhone = json.getJSONObject("donor");
						if (donorPhone.has("contact")) {
							JSONObject donorContact = donorPhone.getJSONObject("contact");
							if (donorContact.has("mobileNumber") && !donorContact.isNull("mobileNumber")) {
								donor.setPhoneNumber((String) donorContact.get("mobileNumber"));
							}
						}
						if (donorPhone.has("bloodAbo") && donorPhone.has("bloodRh") && !donorPhone.isNull("bloodAbo")
								&& !donorPhone.isNull("bloodRh")) {

							donor.setBloodAbo((String) donorPhone.get("bloodAbo"));
							donor.setBloodRh((String) donorPhone.get("bloodRh"));

						}

						if (donorPhone.has("donorNumber") && !donorPhone.isNull("donorNumber")) {

							donor.setDonorNumber((String) donorPhone.get("donorNumber"));
							donor.setDonorNumber((String) donorPhone.get("donorNumber"));

						}

					}
				}

			}

			in.close();
		} else

		{
			System.out.println("GET request not worked");
		}
		return donor;
	}

	public static Donor getDonorLabResult(String din, String GET_URL, String authUser, String authPassword)
			throws IOException {
		Donor donor = null;

		URL obj = new URL(GET_URL + "/donors/search?donationIdentificationNumber=" + din);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		String encoding = DatatypeConverter.printBase64Binary((authUser + ":" + authPassword).getBytes("UTF-8"));
		con.setRequestProperty("Authorization", "Basic " + encoding);

		int responseCode = con.getResponseCode();
		// System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			JSONObject json = new JSONObject(response.toString());
			JSONArray donors = json.getJSONArray("donors");
			if (!donors.isEmpty()) {

				String idNum = ((JSONObject) donors.getJSONObject(0)).getString("id");
				String name = ((JSONObject) donors.getJSONObject(0)).getString("firstName");
				name = name + " " + ((JSONObject) donors.getJSONObject(0)).getString("middleName");
				name = name + " " + ((JSONObject) donors.getJSONObject(0)).getString("lastName");
				donor = new Donor(idNum, name, "", "", "", "", false);
			}
			if (donor != null) {

				obj = new URL(GET_URL + "/donors/" + donor.getId());
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				encoding = DatatypeConverter.printBase64Binary((authUser + ":" + authPassword).getBytes("UTF-8"));
				con.setRequestProperty("Authorization", "Basic " + encoding);

				responseCode = con.getResponseCode();

				///////////////////////////

				if (responseCode == HttpURLConnection.HTTP_OK) { // success
					in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					response = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}

					json = new JSONObject(response.toString());
					if (json.has("donor")) {
						JSONObject donorPhone = json.getJSONObject("donor");
						if (donorPhone.has("contact")) {
							JSONObject donorContact = donorPhone.getJSONObject("contact");
							if (donorContact.has("mobileNumber") && !donorContact.isNull("mobileNumber")) {
								donor.setPhoneNumber((String) donorContact.get("mobileNumber"));
							}
						}
						if (donorPhone.has("bloodAbo") && donorPhone.has("bloodRh") && !donorPhone.isNull("bloodAbo")
								&& !donorPhone.isNull("bloodRh")) {

							donor.setBloodAbo((String) donorPhone.get("bloodAbo"));
							donor.setBloodRh((String) donorPhone.get("bloodRh"));

						}

					}
				}

				///// for loading lab result

				obj = new URL(GET_URL + "/testresults/" + din);
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				encoding = DatatypeConverter.printBase64Binary((authUser + ":" + authPassword).getBytes("UTF-8"));
				con.setRequestProperty("Authorization", "Basic " + encoding);

				responseCode = con.getResponseCode();

				///////////////////////////

				if (responseCode == HttpURLConnection.HTTP_OK) { // success
					in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					response = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}

					json = new JSONObject(response.toString());
					if (json.has("testResults")) {
						JSONObject testResults = json.getJSONObject("testResults");
						if (testResults.has("donation")) {
							JSONObject donation = testResults.getJSONObject("donation");
							if (donation.has("released") && !donation.isNull("released")) {
								donor.setReleased((boolean) donation.get("released"));
							}
						}

					}

				}

			}

			in.close();
		} else

		{
			System.out.println("GET request not worked");
		}
		return donor;
	}

	public static Order getIssue(String orderId, String GET_URL, String authUser, String authPassword)
			throws IOException {
		Order orderObj = null;

		URL obj = new URL(GET_URL + "/orderforms/" + orderId);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		String encoding = DatatypeConverter.printBase64Binary((authUser + ":" + authPassword).getBytes("UTF-8"));
		con.setRequestProperty("Authorization", "Basic " + encoding);

		int responseCode = con.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			JSONObject json = new JSONObject(response.toString());
			String status = "";
			List<String> componentsDIN = new ArrayList<String>();

			if (json.has("orderForm") && !json.isNull("orderForm")) {
				JSONObject orderForm = json.getJSONObject("orderForm");
				if (!orderForm.isEmpty()) {
					if (orderForm.has("status") && !orderForm.isNull("status")) {
						status = (String) orderForm.get("status");

					}
					if (orderForm.has("components") && !orderForm.isNull("components")) {
						JSONArray components = orderForm.getJSONArray("components");

						if (!components.isEmpty()) {
							for (int i = 0; i < components.length(); i++) {
								String din = (String) ((JSONObject) components.getJSONObject(i))
										.getString("donationIdentificationNumber");
								componentsDIN.add(din);

							}
						}
					}
				}

			}

			orderObj = new Order(componentsDIN, status);

			in.close();
		} else

		{
			System.out.println("GET request not worked");
		}
		return orderObj;
	}

	public static Boolean sendSMS(String smsServer, String text, String phoneNumber) throws IOException {
		URL obj = new URL(smsServer + "&text=" + URLEncoder.encode(text, "UTF-8") + "&to="
				+ URLEncoder.encode(phoneNumber, "UTF-8"));
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(30000);
		int responseCode = con.getResponseCode();
		boolean status = false;
		if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
			status = true;
		}
		return status;
	}

	public static int getTotalDonation(String donorID, String GET_URL, String authUser, String authPassword)
			throws IOException {
		int amount = 0;

		URL obj = new URL(GET_URL + "/donors/" + donorID + "/overview");
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		String encoding = DatatypeConverter.printBase64Binary((authUser + ":" + authPassword).getBytes("UTF-8"));
		con.setRequestProperty("Authorization", "Basic " + encoding);

		int responseCode = con.getResponseCode();
		// System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}

				JSONObject json = new JSONObject(response.toString());

				if (json.has("totalDonations") && !json.isNull("totalDonations")) {
					amount = (int) json.get("totalDonations");
				}

			}

			in.close();
		} else

		{
			System.out.println("GET request not worked");
		}
		return amount;
	}

}
