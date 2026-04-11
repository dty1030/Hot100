public class maxArea {
    public int maxArea(int[] height) {
        int leftBound = 0;
        int rightBound = height.length - 1;
        int[] leftMax = new int[height.length];
        int[] rightMax = new int[height.length];
        int maxArea = Math.min(height[leftBound], height[rightBound]) * (rightBound - leftBound);

        while (leftBound < rightBound){
            maxArea = Math.max(maxArea, Math.min(height[leftBound], height[rightBound]) * (rightBound - leftBound));
            if (height[rightBound] > height[leftBound])leftBound++;
            else rightBound--;
        }
        return maxArea;

    }
}
