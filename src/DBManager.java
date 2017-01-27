import java.io.PrintWriter;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DBManager {

	private Connection connection;
	private static DBManager instance;

	private DBManager() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connect() {
		if (connection != null) {
			return;
		}
		try {
			connection = DriverManager.getConnection(Settings.DB_ADDRESS, Settings.USER_NAME, Settings.PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		if (connection == null) {
			return;
		}
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}

	public void insertPub(Pub pub) {
		String sql = "INSERT INTO pub(id, message, creation_date, author, pub_id, page)" +
				"select ?,?,?,?,?,?"+
				"where not exists(select 1 from pub where id=?)";
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, pub.getId());
			preparedStatement.setString(2, pub.getMessage());
			preparedStatement.setTimestamp(3, new Timestamp(pub.getCreationDate().getTime()));
			preparedStatement.setLong(4, pub.getAuthor());
			preparedStatement.setString(5, pub.getPubId());
			preparedStatement.setString(6, Settings.GROUP_NAME);
			preparedStatement.setString(7, pub.getId());
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeStatement(preparedStatement);
		}
	}

	private void closeStatement(PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}

