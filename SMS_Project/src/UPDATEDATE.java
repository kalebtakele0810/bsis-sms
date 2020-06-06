import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.swing.text.DateFormatter;

public class UPDATEDATE {

	public static void main(String[] args) {
		try {

			/* loading jdbc driver connection and executing select statement */
			Class.forName("com.mysql.jdbc.Driver");
			/* url,username and password values are loaded from config.properties file */
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bsis", "root", "root");
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id_text FROM bsis.donor where true;");

			long time = 1469998800000L;
			int i = 0;
			while (rs.next()) {

				String id_text = rs.getString("id_text");

				Timestamp lastUpdated = new Timestamp(time);
				Connection con1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/bsis", "root", "root");
				PreparedStatement stmt1 = (PreparedStatement) con1.prepareStatement(
						"update donor set lastUpdated='" + lastUpdated + "' where id_text='" + id_text + "';");
				stmt1.executeUpdate();
				con1.close();
				stmt1.close();
				if (i == 56) {
					time = time + 86400000;
					i=0;
				}
				i++;

			}
			con.close();
			rs.close();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
