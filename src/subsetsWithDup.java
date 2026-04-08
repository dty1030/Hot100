import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class subsetsWithDup {

    public List<List<Integer>> subsetsWithDup(int[] nums) {
        List<Integer> path = new ArrayList<>();
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums);
        helper(0, path, res, nums);
        return res;

    }

    void helper(int index, List<Integer> path, List<List<Integer>> res, int[] nums){

        res.add(new ArrayList<>(path));
        if (index == nums.length)return;
        for (int i = index; i < nums.length; i++){
            //不是当前层头一个候选且值相同，跳过
            if (i > index && nums[i] == nums[i - 1])continue;
            path.add(nums[i]);
            helper(i + 1, path, res, nums);
            path.remove(path.size()-1);

        }


    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 2};

        subsetsWithDup solution = new subsetsWithDup();
        List<List<Integer>> ans = solution.subsetsWithDup(nums);

        System.out.println(ans);
    }
}
