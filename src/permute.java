import java.util.ArrayList;
import java.util.List;

public class permute {
    public List<List<Integer>> permute(int[] nums) {
        if (nums == null)return null;
        List<List<Integer>> res = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        List<Integer> path = new ArrayList<>();
        helper(nums, res, path, used);


        return res;
    }
    void helper(int[] nums, List<List<Integer>> res, List<Integer> path, boolean[] used){
        if (path.size() == nums.length) {
            // 收集答案
            res.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < nums.length; i++){
            if (used[i])continue;
            path.add(nums[i]);
            used[i] = true;
            helper(nums, res, path, used);

            path.remove(path.size() - 1);
            used[i] = false;


        }
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3};

        permute solution = new permute();
        List<List<Integer>> ans = solution.permute(nums);

        System.out.println(ans);
    }
}
