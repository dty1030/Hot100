import java.util.ArrayList;
import java.util.List;

public class combinationSum {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        helper(candidates, res, path, 0, target);
        return res;


    }

    void helper(int[] candidates, List<List<Integer>> res, List<Integer> path, int index, int target){
        if (target == 0){
            res.add(new ArrayList(path));
            return;
        }
        if (index >= candidates.length)return;
        if (candidates[index] <= target){
            path.add(candidates[index]);

            helper(candidates, res, path, index, target - candidates[index]);
            //如果没法得到结果，返回
            path.remove(path.size() - 1);
        }
        //下一个candidate
        helper(candidates, res, path, index + 1, target);


    }
}
