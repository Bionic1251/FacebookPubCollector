import com.restfb.types.Comment;
import com.restfb.types.Post;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class FBPost {
	private Post post;
	private Map<Comment, List<Comment>> commentMap;
	private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

	public FBPost(Post post, Map<Comment, List<Comment>> commentMap) {
		this.post = post;
		this.commentMap = commentMap;
	}

	public Post getPost() {
		return post;
	}

	public Map<Comment, List<Comment>> getCommentMap() {
		return commentMap;
	}

	@Override
	public String toString() {
		if (post == null) {
			return "null";
		}
		String out = "\"" + inlineString(post.getMessage()) + "\"," + format.format(post.getCreatedTime());
		for (Map.Entry<Comment, List<Comment>> entry : commentMap.entrySet()) {
			out += "\r\n\t\"" + inlineString(entry.getKey().getMessage()) + "\",";
			out += format.format(entry.getKey().getCreatedTime()) + ",";
			out += entry.getKey().getFrom().getName();
			for (Comment comment : entry.getValue()) {
 				if (comment.getFrom() != null && comment.getFrom().getId().equals(Settings.OWNER_ID)) {
					out += "\r\n\t\t\"~" + inlineString(comment.getMessage()) + "\",";
				} else {
					out += "\r\n\t\t\"" + inlineString(comment.getMessage()) + "\",";
				}
				out += format.format(comment.getCreatedTime()) + ",";
				out += comment.getFrom().getName() + ",";
				out += comment.getFrom().getId();
			}
		}
		return out;
	}

	private String inlineString(String str) {
		if (str == null) {
			return str;
		}
		str = str.replace("\r\n", " ");
		str = str.replace("\r", " ");
		str = str.replace("\n", " ");
		return str;
	}
}
