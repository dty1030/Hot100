import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class threeSum {
    public List<List<Integer>> threeSum(int[] nums) {

        int x, y, z;
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums);
        for (int i = 0; i < nums.length; i++){

            int left = i + 1;
            int right = nums.length-1;
            if (i > 0 && nums[i] == nums[i-1])continue;

            while (left < right){
                int sum = nums[i] + nums[left] + nums[right];
                if (sum < 0) left++;
                else if (sum > 0)right--;
                else {
                    List<Integer> arr = new ArrayList<>();

                    arr.add(nums[i]);
                    arr.add(nums[left]);
                    arr.add(nums[right]);
                    res.add(arr);
                    //跳过重复
                    while (left < right && nums[left] == nums[left+1])
                    {
                        left++;
                    }
                    left++;
                    while (right > left && nums[right] == nums[right-1]){
                        right--;
                    }
                    right--;
                }
            }


        }
        return res;


    }
}