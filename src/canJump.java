public class canJump {
    public boolean canJump(int[] nums) {
        int index = nums[0];
        int maxReach = nums[0];
        for (int i = 0; i < nums.length; i++){
            if (i > maxReach)return false;
            maxReach = Math.max(nums[i] + i, maxReach);
        }

        return maxReach >= nums.length - 1;
    }
}
