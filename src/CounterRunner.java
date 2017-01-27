import com.restfb.types.Comment;
import com.restfb.types.Post;

import java.text.SimpleDateFormat;
import java.util.*;

public class CounterRunner {
	public static void main(String[] args) {
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 16);
		calendar.set(Calendar.MONTH, 8);
		Date date = new Date();
		System.out.println(date);
		System.out.println(date.getTime());
		System.out.println(date.getTime() < calendar.getTimeInMillis());
		System.exit(1);

		Post post = new Post();
		post.setMessage("HeyBuddy");
		Comment keyComment = new Comment();
		keyComment.setMessage("Yeah, Man");
		keyComment.setCreatedTime(new Date());
		Comment reply1 = new Comment();
		reply1.setMessage("Yep");
		reply1.setCreatedTime(new Date());
		Comment reply2 = new Comment();
		reply2.setMessage("Hey");
		reply2.setCreatedTime(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5));
		Map<Comment, List<Comment>> map = new HashMap<Comment, List<Comment>>();
		List<Comment> list = new ArrayList<Comment>();
		list.add(reply1);
		list.add(reply2);
		map.put(keyComment, list);
		FBPost fbPost = new FBPost(post, map);
		System.out.println("Counter");
	}
}
