import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class SearchRunner {
	public static void main(String[] args) {
		setParameters();

		Searcher searcher = new Searcher("airline");
		searcher.search();
	}

	private static void setParameters() {
		SimpleDateFormat parserSDF = new SimpleDateFormat("dd.MM.yyyy");
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("config.properties");
			prop.load(input);

			Settings.ACCESS_TOKEN = (String) prop.get("access_token");
			System.out.println("ACCESS_TOKEN " + Settings.ACCESS_TOKEN);

			Settings.APP_SECRET = (String) prop.get("app_secret");
			System.out.println("APP_SECRET " + Settings.APP_SECRET);

			Settings.GROUP_NAME = (String) prop.get("group");
			System.out.println("GROUP_NAME " + Settings.GROUP_NAME);

			Settings.OWNER_ID = (String) prop.get("owner_id");
			System.out.println("OWNER_ID " + Settings.OWNER_ID);

			Settings.SLEEP_TIME = Integer.valueOf((String) prop.get("sleep_time"));
			System.out.println("SLEEP_TIME " + Settings.SLEEP_TIME);

			String maxDateStr = (String) prop.get("max_date");
			Settings.MAX_DATE = parserSDF.parse(maxDateStr, new ParsePosition(0));
			System.out.println("MAX_DATE " + Settings.MAX_DATE);

			String minDateStr = (String) prop.get("min_date");
			Settings.MIN_DATE = parserSDF.parse(minDateStr, new ParsePosition(0));
			System.out.println("MIN_DATE " + Settings.MIN_DATE);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
