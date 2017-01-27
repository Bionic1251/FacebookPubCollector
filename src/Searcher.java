import com.restfb.*;
import com.restfb.types.Page;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Searcher {
	private static FacebookClient facebookClient;
	private PrintWriter textWriter = null;
	private String query;

	public Searcher(String fileName) {
		query = fileName;
		facebookClient = new DefaultFacebookClient(Settings.ACCESS_TOKEN, Settings.APP_SECRET, Version.VERSION_2_5);
		try {
			textWriter = new PrintWriter(new File(fileName + "_search.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void search() {
		textWriter.println("Query " + query);
		Connection<Page> publicSearch = facebookClient.fetchConnection("search", Page.class,
				Parameter.with("q", query), Parameter.with("type", "page"));
		do {
			for (Page page : publicSearch.getData()) {
				textWriter.println(page.getName());
				System.out.println(page.getName());
			}
			if (publicSearch.hasNext()) {
				sleep();
				publicSearch = facebookClient.fetchConnectionPage(publicSearch.getNextPageUrl(), Page.class);
			} else {
				publicSearch = null;
			}
		} while (publicSearch != null);
		textWriter.close();
	}

	private void sleep() {
		try {
			Thread.sleep(Settings.SLEEP_TIME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
