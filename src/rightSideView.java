import java.util.ArrayList;
import java.util.List;

public class rightSideView {
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        helper(root, res, 0);
        return res;


    }
    void helper(TreeNode node, List<Integer> res, int depth){
        if (node == null)return;
        if (res.size() == depth)res.add(node.val);
        if (node.right != null){
        helper(node.right, res, depth + 1);}
        if (node.left != null){
            helper(node.left, res, depth + 1);
        }
    }
}
