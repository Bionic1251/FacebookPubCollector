import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.json.JsonObject;
import com.restfb.types.Comment;
import com.restfb.types.Post;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class Collector {
	private static FacebookClient facebookClient;
	private Map<Date, Integer> commentMap = new HashMap<Date, Integer>();
	private Map<Date, Integer> replyMap = new HashMap<Date, Integer>();
	private Map<Date, Integer> ownerCommentMap = new HashMap<Date, Integer>();
	private Map<Date, Integer> ownerReplyMap = new HashMap<Date, Integer>();
	private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

	private PrintWriter commentWriter = null;
	private PrintWriter postWriter = null;

	public Collector() {
		updateConnection();
		try {
			commentWriter = new PrintWriter(new File(Settings.GROUP_NAME + "_comment.txt"));
			commentWriter.println("[");
			postWriter = new PrintWriter(new File(Settings.GROUP_NAME + "_post.txt"));
			postWriter.println("[");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void collect() {
		String uri = "https://graph.facebook.com/v2.7/" + Settings.GROUP_NAME + "/posts?format=json&summary=1&order=chronological&access_token=" + Settings.ACCESS_TOKEN;
		Connection<Post> postFeed = facebookClient.fetchConnectionPage(uri, Post.class);
		do {
			boolean exit = isOutOfRangeAndProcess(postFeed.getData());
			if (exit) {
				break;
			}
			postFeed = getConnection(Post.class, postFeed);
		} while (postFeed != null);
		closeWriters();
	}

	private void updateConnection() {
		facebookClient = new DefaultFacebookClient(Settings.ACCESS_TOKEN, Settings.APP_SECRET, Version.VERSION_2_7);
	}

	private <T> Connection<T> getConnection(Class<T> connectionType, Connection<T> connection) {
		if (connection.hasNext()) {
			sleep();

			try {
				return facebookClient.fetchConnectionPage(connection.getNextPageUrl(), connectionType);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	private boolean isOutOfRangeAndProcess(List<Post> postList) {
		for (Post post : postList) {
			System.out.println(format.format(post.getCreatedTime()));
			if (post.getCreatedTime().getTime() > Settings.MAX_DATE.getTime()) {
				continue;
			}
			if (post.getCreatedTime().getTime() < Settings.MIN_DATE.getTime()) {
				return true;
			}
			System.out.println(format.format(post.getCreatedTime()) + ", " + post.getMessage());
			FBPost fbPost = collectPost(post);
			if (fbPost == null) {
				continue;
			}
		}
		return false;
	}

	private void closeWriters() {
		commentWriter.close();
		postWriter.close();
	}

	private FBPost collectPost(Post post) {
		postWriter.println(postToJSON(post).toString() + ",");
		savePostToDB(post);
		int count = 0;
		Map<Comment, List<Comment>> commentMap = new HashMap<>();
		sleep();
		Connection<Comment> commentFeed;
		try {
			commentFeed = facebookClient.fetchConnection(post.getId() + "/comments", Comment.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		do {
			count += processComments(commentFeed.getData(), commentMap, post.getId());
			commentFeed = getConnection(Comment.class, commentFeed);
		} while (commentFeed != null);
		System.out.println(count);
		return new FBPost(post, commentMap);
	}

	private void savePostToDB(Post post) {
		DBManager manager = DBManager.getInstance();
		Pub pub = new Pub(post);
		manager.insertPub(pub);
	}

	private int processComments(List<Comment> commentList, Map<Comment, List<Comment>> commentMap, String postId) {
		int count = 0;
		for (Comment comment : commentList) {
			count++;
			saveCommentToDB(comment, postId);
			updateMap(this.commentMap, comment.getCreatedTime());
			updateOwnerCommentMapIfNecessary(comment);
			//System.out.println("COMMENT " + comment.getMessage());
			List<Comment> replyList = collectReplies(comment, comment.getId());
			if (replyList == null) {
				continue;
			}
			if (replyList != null && !replyList.isEmpty()) {
				commentMap.put(comment, replyList);
			}
		}
		return count;
	}

	private void saveCommentToDB(Comment comment, String postId) {
		DBManager manager = DBManager.getInstance();
		Pub pub = new Pub(comment, postId);
		manager.insertPub(pub);
	}

	private void updateOwnerCommentMapIfNecessary(Comment comment) {
		if (comment != null && comment.getFrom() != null && comment.getFrom().getId().equals(Settings.OWNER_ID)) {
			updateMap(ownerCommentMap, comment.getCreatedTime());
		}
	}

	private JsonObject postToJSON(Post post) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("id", post.getId());
		jsonObject.put("created_time", getDate(post.getCreatedTime()));
		jsonObject.put("message", post.getMessage());
		return jsonObject;
	}

	private JsonObject commentToJSON(Comment comment, String replyTo) {
		JsonObject object = new JsonObject();
		object.put("id", comment.getId());
		object.put("message", comment.getMessage());
		object.put("from", comment.getFrom().getId());
		object.put("reply", replyTo);
		object.put("likes", comment.getLikes());

		object.put("created_time", getDate(comment.getCreatedTime()));
		return object;
	}

	private String getDate(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		String month = String.format("%02d", calendar.get(Calendar.MONTH));
		String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
		return calendar.get(Calendar.YEAR) + "-" + month + "-" + day;
	}

	private List<Comment> collectReplies(Comment comment, String commentId) {
		if (comment != null && comment.getMessage() != null && comment.getFrom() != null) {
			commentWriter.println(commentToJSON(comment, commentId).toString() + ",");
		}
		boolean valid = false;
		List<Comment> commentList = new ArrayList<Comment>();
		sleep();
		Connection<Comment> replyFeed;
		try {
			replyFeed = facebookClient.fetchConnection(comment.getId() + "/comments", Comment.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		do {
			valid = valid || processReplies(replyFeed.getData(), commentList, commentId);
			replyFeed = getConnection(Comment.class, replyFeed);
		} while (replyFeed != null);
		return valid ? commentList : new ArrayList<Comment>();
	}

	private boolean processReplies(List<Comment> comments, List<Comment> commentList, String commentId) {
		boolean valid = false;
		for (Comment reply : comments) {
			commentWriter.println(commentToJSON(reply, commentId) + ",");
			updateMap(replyMap, reply.getCreatedTime());
			if (reply.getFrom() != null && reply.getFrom().getId().equals(Settings.OWNER_ID)) {
				valid = true;
				updateMap(ownerReplyMap, reply.getCreatedTime());
			}
			commentList.add(reply);
			saveCommentToDB(reply, commentId);
			//System.out.println(reply.getMessage());
		}
		return valid;
	}

	private void sleep() {
		try {
			Thread.sleep(Settings.SLEEP_TIME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateMap(Map<Date, Integer> map, Date date) {
		date = roundDate(date);
		Integer count = 0;
		if (map.containsKey(date)) {
			count = map.get(date);
		}
		map.put(date, ++count);
	}

	private long getMaxDate(Map<Date, Integer> map) {
		long maxDate = Long.MIN_VALUE;
		Set<Date> dates = map.keySet();
		for (Date date : dates) {
			maxDate = Math.max(date.getTime(), maxDate);
		}
		return maxDate;
	}

	private long getMinDate(Map<Date, Integer> map) {
		long minDate = Long.MAX_VALUE;
		Set<Date> dates = map.keySet();
		for (Date date : dates) {
			minDate = Math.min(date.getTime(), minDate);
		}
		return minDate;
	}

	private Date roundDate(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		return calendar.getTime();
	}
}
