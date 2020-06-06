import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseAction {

	public static boolean executeUpdate(String qry, String dbURL, String dbUserName, String dbPassword)
			throws IOException {
		boolean status = false;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
			PreparedStatement stmt = (PreparedStatement) con.prepareStatement(qry);
			stmt.executeUpdate();
			status = true;
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return status;
	}

	public static ResultSet executeSelect(String qry,Connection con , String dbURL, String dbUserName, String dbPassword)
			throws IOException {
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(qry);

			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return rs;

	}

}
