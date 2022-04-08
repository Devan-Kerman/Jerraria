package school;

public class TreeNode {
	TreeNode left;
	String keyword;
	TreeNode right;
	ArticleNode head;

	public TreeNode(String keyword, ArticleNode head) {
		this.left = null;
		this.keyword = keyword;
		this.right = null;
		this.head = head;
	}
}
