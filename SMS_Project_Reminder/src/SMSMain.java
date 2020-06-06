import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class SMSMain {
	static String dbURL = "";
	static String dbUserName = "";
	static String dbPassword = "";
	static String dbQuerylimit = "";
	static String dbSmsTable = "";

	static String serveraddress = "";
	static String serverUserName = "";
	static String serverPassword = "";

	static String smsServer = "";

	public static void main(String[] args) {

		/* read properties file and exit if something went wrong */
		System.out.println("started reading properties file!!!");
		if (!readProperties()) {
			System.exit(1);
		}
		System.out.println("finished reading properties file!!!");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT id_text, firstName, middleName, lastName, donorNumber FROM bsis.Donor where dueToDonate=CURDATE() or datediff(CURDATE(),dueToDonate)=1 or datediff(CURDATE(),dueToDonate)=2;");
			while (rs.next()) {
				String donor_id = rs.getString("id_text");
				String firstName = rs.getString("firstName");
				String middleName = rs.getString("middleName");
				String lastName = rs.getString("lastName");
				String donorNumber = rs.getString("donorNumber");

				sendReminderSMS(donor_id, firstName, middleName, lastName, donorNumber);
			}
			con.close();
			rs.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		
		System.exit(0);

	}

	public static boolean readProperties() {
		boolean result = false;
		try (InputStream input = new FileInputStream("/var/lib/bsis_sms/SMS_Project_Reminder/config.properties")) {

			Properties prop = new Properties();

			// save properties to project root folder
			prop.load(input);

			// get the property value and print it out
			dbURL = prop.getProperty("db.url");
			dbUserName = prop.getProperty("db.user");
			dbPassword = prop.getProperty("db.password");
			dbQuerylimit = prop.getProperty("db.querylimit");
			dbSmsTable = prop.getProperty("db.smsTable");

			serveraddress = prop.getProperty("serveraddress");
			serverUserName = prop.getProperty("serverUserName");
			serverPassword = prop.getProperty("serverPassword");

			smsServer = prop.getProperty("smsServer");

			result = true;

		} catch (IOException io) {
			io.printStackTrace();
		}

		return result;
	}

	public static boolean sendReminderSMS(String donor_id, String firstName, String middleName, String lastName,
			String donorNumber) throws IOException {
		RequestUtil requestObj = new RequestUtil();
		boolean status = false;

		String donorPhoneNumber = requestObj.getDonor(donor_id, serveraddress, serverUserName, serverPassword);

		if (!donorPhoneNumber.isEmpty()) {
			System.out.println("==============Send Reminer SMS for " + firstName + " " + middleName + " " + lastName
					+ " " + ">>>" + donorPhoneNumber + "==============");

			////////////// Send SMS Code
			String smsText = "Dear " + firstName + " " + middleName + " " + lastName
					+ ", today will be 90 days since your last blood donation. Please donate again so "
					+ "that we can save more lives. " + "Your donor number is: " + donorNumber;

			System.out.println(smsText);

			boolean isSMSSent = requestObj.sendSMS(smsServer, smsText, donorPhoneNumber);

			////////////// End of Send SMS Code

		} else {
			System.out.println("No Phone Number!!!");
		}
		return status;

	}

}
