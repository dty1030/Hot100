import java.util.*;

public class levelOrder {
    List<List<Integer>> res = new ArrayList<>();
    public List<List<Integer>> levelOrder(TreeNode root){
        if (root == null)return res;
        Queue<TreeNode> queue = new LinkedList();
        queue.offer(root);
        while (queue.size() > 0){
            List<Integer> level = new ArrayList<>();
            int size = queue.size();
            for (int i = 0; i < size; i++){
                TreeNode node = queue.remove();
                if (node.left != null)queue.add(node.left);
                if (node.right != null)queue.add(node.right);
                level.add(node.val);
            }
            res.add(level);
        }
        return res;



    }
    public static void main(String[] args) {
        TreeNode root = new TreeNode(3);
        root.left = new TreeNode(9);
        root.right = new TreeNode(20);
        root.right.left = new TreeNode(15);
        root.right.right = new TreeNode(7);

        levelOrder solution = new levelOrder();
        List<List<Integer>> ans = solution.levelOrder(root);

        System.out.println(ans);
    }

}
