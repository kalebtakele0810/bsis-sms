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
	static String issueTable = "";

	static String smsServer = "";

	public static void main(String[] args) {

		/* read properties file and exit if something went wrong */
		System.out.println("started reading properties file!!!");
		if (!readProperties()) {
			System.exit(1);
		}
		System.out.println("finished reading properties file!!!");
		boolean bloodGroupConfig = getBloodGroupConfiguration();
		try {

			/* loading jdbc driver connection and executing select statement */
			Class.forName("com.mysql.jdbc.Driver");
			/* url,username and password values are loaded from config.properties file */
			Connection con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from " + dbSmsTable + " "
					+ "where  is_thanks_sent=false or is_labresult_sent=false or is_bloodgroup_sent=false;");
			while (rs.next()) {

				/*
				 * retrieving
				 * tableId,donationIdentificationNumber,is_thanks_sent,is_labresult_sent and
				 * is_bloodgroup_sent from database result set
				 * 
				 * tableId is later used as a primary key for modifying other values such as
				 * is_thanks_sent,is_labresult_sent,etc,...
				 * 
				 * is_thanks_sent is a boolean value to check whether thankyou sms message is
				 * sent or not
				 * 
				 * is_labresult_sent is a boolean value to check whether lab result sms message
				 * is sent or not
				 * 
				 * is_bloodgroup_sent is a boolean value to check whether bloodgroup sms message
				 * is sent or not
				 */
				String tableId = rs.getString("id");
				String din = rs.getString("donationIdentificationNumber");
				boolean isThanksSent = rs.getBoolean("is_thanks_sent");
				boolean is_labresult_sent = rs.getBoolean("is_labresult_sent");
				boolean is_bloodgroup_sent = rs.getBoolean("is_bloodgroup_sent");
				if (!isThanksSent) {
					sendThankyouSMS(din, tableId);
				} else {
					if (!is_bloodgroup_sent) {
						sendBloodGroupSMS(din, tableId, bloodGroupConfig);
					} else {
						if (!is_labresult_sent) {
							sendLabResultSMS(din, tableId);
						}
					}
				}

			}
			con.close();
			rs.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		/*
		 * SMS for Donors after the blood they donated is issued for a certain health
		 * facility
		 */

		try {
			/* loading jdbc driver connection and executing select statement */
			Class.forName("com.mysql.jdbc.Driver");
			/* url,username and password values are loaded from config.properties file */
			Connection con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("select * from " + issueTable + " " + "where is_sms_sent=false;");
			while (rs.next()) {
				/*
				 * retrieving tableId,orderId
				 * 
				 * tableId is later used as a primary key for modifying is_sms_sent
				 * 
				 * orderID will be later used to get the order information, from which the DIN
				 * is retrieved, this DIN is then used to get the the Donor information with its
				 * telephone number.
				 * 
				 */
				String tableId = rs.getString("id");
				String orderID = rs.getString("orderId");
				sendIssueNotificationSMS(orderID, tableId);

			}
			con.close();
			rs.close();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public static boolean readProperties() {
		boolean result = false;
		try (InputStream input = new FileInputStream("/var/lib/bsis_sms/SMS_Project/config.properties")) {
			Properties prop = new Properties();

			// save properties to project root folder
			prop.load(input);

			/* get the property value and print it out */
			/* get database URL */
			dbURL = prop.getProperty("db.url");
			/* get database user */
			dbUserName = prop.getProperty("db.user");
			/* get database password */
			dbPassword = prop.getProperty("db.password");
			/* get database query limit */
			dbQuerylimit = prop.getProperty("db.querylimit");
			/*
			 * get database table on which the sms information is stored, this information
			 * includes DIN number, if or not thankyou sms is sent,if or not bloodgroup sms
			 * is sent,if or not labresult sms is sent,etc...
			 */
			dbSmsTable = prop.getProperty("db.smsTable");
			/*
			 * get database table for the issued orders, this includes orderID and whether
			 * or not sms is sent for that order
			 */
			issueTable = prop.getProperty("db.issueTable");
			/*
			 * load the address for the bsis server
			 */
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

	public static boolean sendThankyouSMS(String DIN, String tableId) throws IOException {
		/* Initialize request utility class to donor order object */
		RequestUtil requestObj = new RequestUtil();
		boolean status = false;
		Donor donor = requestObj.getDonor(DIN, serveraddress, serverUserName, serverPassword);
		/* check if the donor object is not null */
		if (donor != null) {
			/* check if the donor has phone number */
			if (!donor.getPhoneNumber().isEmpty()) {
				System.out.println("==============Send Thankyou SMS for " + donor.getFullName() + ">>>"
						+ donor.getPhoneNumber() + "==============");

				/* Send SMS for the donor */
				String smsText = "Dear " + donor.getFullName() + ", thank you for donating blood. "
						+ "Your donor number is:" + donor.getDonorNumber();
				/* storing results of sms status to the isSMSSent boolean */
				boolean isSMSSent = requestObj.sendSMS(smsServer, smsText, donor.getPhoneNumber());
				/*
				 * if sending failed for the donor sms send status will be updated to false. so
				 * that sending will be repeated again later.
				 */
				if (isSMSSent) {
					/*
					 * update is_thanks_sent to true so that donor doesn't receive repetitive thank
					 * you sms to one donation
					 */
					String qry = "update " + dbSmsTable + " set is_thanks_sent=true where id=" + tableId;
					status = new DatabaseAction().executeUpdate(qry, dbURL, dbUserName, dbPassword);
				}
			} else {
				/*
				 * update is_thanks_sent to true so that donor didn't have phone number, so that
				 * future operations doesn't try to send sms again.
				 */
				String qry = "update " + dbSmsTable + " set is_thanks_sent=true where id=" + tableId;
				status = new DatabaseAction().executeUpdate(qry, dbURL, dbUserName, dbPassword);
			}
		}
		return status;

	}

	public static boolean sendBloodGroupSMS(String DIN, String tableId, boolean bloodGroupConfig) throws IOException {
		/* Initialize request utility class to obtain donor object */
		RequestUtil requestObj = new RequestUtil();
		boolean status = false;
		Donor donor = requestObj.getDonor(DIN, serveraddress, serverUserName, serverPassword);
		/* check if the donor object is not null */
		if (donor != null) {
			/* check if the donor has phone number */
			if (!donor.getPhoneNumber().isEmpty()) {
				/* check if the donor blood group is entered */
				if (!donor.getBloodAbo().equals("")) {
					int totalDonations = requestObj.getTotalDonation(donor.getId(), serveraddress, serverUserName,
							serverPassword);
					System.out.println("==============Send BloodGroup SMS for " + donor.getFullName() + "=="
							+ donor.getPhoneNumber() + "=" + donor.getBloodAbo() + donor.getBloodRh()
							+ "\n Total Donation=" + totalDonations + "==============");

					boolean isSMSSent = false;
					/*
					 * when the configuration is set to false for donations other than first
					 * donation
					 */
					if (bloodGroupConfig || ((!bloodGroupConfig) && (totalDonations <= 1))) {
						// send

						/* Send SMS for the donor */
						String smsText = "Dear " + donor.getFullName() + ", thank you for donating blood. "
								+ "Your blood group is: " + donor.getBloodAbo() + donor.getBloodRh();
						/* storing results of sms status to the isSMSSent boolean */
						isSMSSent = requestObj.sendSMS(smsServer, smsText, donor.getPhoneNumber());

					}
					if (isSMSSent) {
						/*
						 * update is_bloodgroup_sent to true so that donor doesn't receive repetitive
						 * blood group sms to one donation
						 */
						String qry = "update " + dbSmsTable + " set is_bloodgroup_sent=true where id=" + tableId;
						status = new DatabaseAction().executeUpdate(qry, dbURL, dbUserName, dbPassword);
					}
				}
			} else {
				/*
				 * update is_thanks_sent to true so that donor didn't have phone number, so that
				 * future operations doesn't try to send sms again.
				 */
				String qry = "update " + dbSmsTable + " set is_bloodgroup_sent=true where id=" + tableId;
				status = new DatabaseAction().executeUpdate(qry, dbURL, dbUserName, dbPassword);
			}
		}
		return status;

	}

	public static boolean sendLabResultSMS(String DIN, String tableId) throws IOException {
		/* Initialize request utility class to obtain donor object */
		RequestUtil requestObj = new RequestUtil();
		boolean status = false;
		Donor donor = requestObj.getDonorLabResult(DIN, serveraddress, serverUserName, serverPassword);
		/* check if the donor object is not null */
		if (donor != null) {
			/* check if the donor has phone number */
			if (!donor.getPhoneNumber().isEmpty()) {
				/* check if lab result is done and the blood is released */
				if (donor.isReleased()) {
					System.out.println("==============Send LabResult SMS for " + donor.getFullName() + "=="
							+ donor.getPhoneNumber() + "=" + donor.getBloodAbo() + donor.getBloodRh()
							+ "==============");

					/* Send SMS for the donor */
					String smsText = "Dear " + donor.getFullName() + ", lab results from your latest donation is ready."
							+ " You may collect it from the nearest blood bank branch.";

					/* storing results of sms status to the isSMSSent boolean */
					boolean isSMSSent = requestObj.sendSMS(smsServer, smsText, donor.getPhoneNumber());
					if (isSMSSent) {
						/*
						 * update is_labresult_sent to true so that donor doesn't receive repetitive lab
						 * result notification sms to one donation
						 */
						String qry = "update " + dbSmsTable + " set is_labresult_sent=true where id=" + tableId;
						status = new DatabaseAction().executeUpdate(qry, dbURL, dbUserName, dbPassword);
					}
				}
			} else {
				/*
				 * update is_thanks_sent to true so that donor didn't have phone number, so that
				 * future operations doesn't try to send sms again.
				 */
				String qry = "update " + dbSmsTable + " set is_labresult_sent=true where id=" + tableId;
				status = new DatabaseAction().executeUpdate(qry, dbURL, dbUserName, dbPassword);
			}
		}
		return status;

	}

	public static boolean sendIssueNotificationSMS(String orderId, String tableId) throws IOException {
		/* Initialize request utility class to obtain order object */
		RequestUtil requestObj = new RequestUtil();
		boolean status = false;
		Order order = requestObj.getIssue(orderId, serveraddress, serverUserName, serverPassword);

		/* check if the order object is not null */
		if (order != null) {
			/*
			 * check if the order is dispatched, which means the blood is given for the
			 * requested body. If the status is 'CREATED' then it'll be skipped
			 */
			if (order.getDispatched().equals("DISPATCHED")) {
				/* check if the list of donation components is not null */
				if (order.getComponentsDIN() != null) {
					/*
					 * Assume that sms is sent for every donation components in this order, then if
					 * anything fails it'll be set to false. This variable will be later used to
					 * update the values in the database.
					 */
					boolean isSMSSent = true;

					for (String DIN : order.getComponentsDIN()) {
						/* get donor information from the DIN number and send sms for that donor */
						boolean is_bloodissue_sent = true;
						try {

							/* loading jdbc driver connection and executing select statement */
							Class.forName("com.mysql.jdbc.Driver");
							/* url,username and password values are loaded from config.properties file */
							Connection con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
							Statement stmt = con.createStatement();
							ResultSet rs = stmt.executeQuery("select is_bloodissue_sent from " + dbSmsTable
									+ " where  donationIdentificationNumber=" + DIN + ";");
							while (rs.next()) {
								is_bloodissue_sent = rs.getBoolean("is_bloodissue_sent");
							}
						} catch (Exception e) {
							System.out.println(e);
						}
						if (!is_bloodissue_sent) {
							Donor donor = requestObj.getDonor(DIN, serveraddress, serverUserName, serverPassword);
							/* check if the donor is not null */
							if (donor != null) {
								/* check if the donor has phone number */
								if (!donor.getPhoneNumber().isEmpty()) {
									System.out.println("==============Send Order Notification SMS for "
											+ donor.getFullName() + ">>>" + donor.getPhoneNumber() + "==============");

									/* Send SMS for the donor */
									String smsText = "Dear " + donor.getFullName() + ", the blood you donated is now"
											+ " given for a patient. Thank you for saving a life!! ";
									/* storing results of sms status to the success boolean */
									boolean success = requestObj.sendSMS(smsServer, smsText, donor.getPhoneNumber());
									/*
									 * if sending failed for any one of the donors in this 'Order', sms send status
									 * will be updated to false.
									 */
									if (!success) {
										isSMSSent = false;
									}
									if (success) {
										/*
										 * update is_bloodissue_sent to true so that donor doesn't receive repetitive
										 * issuing sms to one donation
										 */
										String qry = "update " + dbSmsTable
												+ " set is_bloodissue_sent=true where donationIdentificationNumber="
												+ DIN;
										status = new DatabaseAction().executeUpdate(qry, dbURL, dbUserName, dbPassword);
									}
								}
							}
						}

					}
					/*
					 * If sms is sent to every donors in this 'Order', then is_sms_sent attribute
					 * will be modified to true.
					 */
					if (isSMSSent) {
						/* update is_sms_sent to true so that one sms is sent per issue */
						String qry = "update " + issueTable + " set is_sms_sent=true where id=" + tableId;
						status = new DatabaseAction().executeUpdate(qry, dbURL, dbUserName, dbPassword);
					}

				}

			}
		}
		return status;

	}


	public static boolean getBloodGroupConfiguration() {
		/*
		 * Initialize response value to true, so that in case the configuration is not
		 * added, It'll still send blood group SMS
		 */
		boolean value = true;
		try {

			/* loading jdbc driver connection and executing select statement */
			Class.forName("com.mysql.jdbc.Driver");
			/* url,username and password values are loaded from config.properties file */
			Connection con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
			Statement stmt = con.createStatement();
			/* select the value of the configuration from database */
			ResultSet rs = stmt.executeQuery("SELECT value FROM GeneralConfig where name='sendBloodGroupSMS';");
			while (rs.next()) {
				value = Boolean.parseBoolean(rs.getString("value"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return value;

	}

}
