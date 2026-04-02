import java.util.ArrayList;
import java.util.List;

public class subsets {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        return null;
    }
    void helper(List<Integer> arr, int pointer, List<List<Integer>> res, int[] nums){

        if (pointer == nums.length){
            res.add(arr);
            return;
        }
        arr.add(nums[pointer]);
        helper(arr, pointer + 1, res, nums);
        arr.remove(arr.size()-1);

        helper(arr, pointer++, res, nums);



    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3};

        subsets solution = new subsets();
        List<List<Integer>> ans = solution.subsets(nums);

        System.out.println(ans);
    }
}
