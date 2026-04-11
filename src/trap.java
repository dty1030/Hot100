public class trap {
    public int trap(int[] height) {
        int[] leftMax = new int[height.length];
        int[] rightMax = new int[height.length];
        for (int i = 0; i < height.length; i++){
            if (i > 0){
                leftMax[i] = Math.max(leftMax[i-1], height[i]);

            }
            else {
                leftMax[i] = height[i];
            }
        }
        for (int j = height.length-1; j >= 0; j--){
            if (j < height.length - 1){
                rightMax[j] = Math.max(rightMax[j+1], height[j]);
            }
            else {
                rightMax[j] = height[j];
            }
        }
        int res = 0;
        for (int i = 0;i < height.length; i++){
            res += Math.min(leftMax[i], rightMax[i]) - height[i];
        }
        return res;


    }
}
