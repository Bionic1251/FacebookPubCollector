import com.restfb.types.Comment;
import com.restfb.types.Post;

import java.util.Date;

public class Pub {
	private String id;
	private String message;
	private Date creationDate;
	private Long author;
	private String pubId;

	public Pub(Comment comment, String pubId) {
		id = comment.getId();
		message = comment.getMessage();
		creationDate = comment.getCreatedTime();
		author = Long.valueOf(comment.getFrom().getId());
		this.pubId = pubId;
	}

	public Pub(Post post) {
		id = post.getId();
		message = post.getMessage();
		creationDate = post.getCreatedTime();
		if (post.getFrom() == null) {
			author = Long.valueOf(Settings.OWNER_ID);
		} else {
			author = Long.valueOf(post.getFrom().getId());
		}
	}

	public void setPubId(String pubId) {
		this.pubId = pubId;
	}

	public String getPubId() {
		return pubId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setAuthor(Long author) {
		this.author = author;
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Long getAuthor() {
		return author;
	}

}
