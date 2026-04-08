import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class combinationSum2 {

    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        Arrays.sort(candidates);
        helper(candidates, target, 0, path, res);
        return res;
    }


    void helper(int[] candidates, int target, int start,  List<Integer> path, List<List<Integer>> res){

        if (target == 0){
            res.add(new ArrayList<>(path));
            return;
        }

        for (int i = start; i < candidates.length; i++){
            if (candidates[i] > target)return;
            if (start < i && candidates[i] == candidates[i-1])continue;
            path.add(candidates[i]);
            helper(candidates, target - candidates[i], i + 1, path, res);
            path.remove(path.size()-1);
        }
    }
}
