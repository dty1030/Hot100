public class maxProfitIII {
    /*
    * 如何确定第一笔交易结束点？
    * */
    public int maxProfitIII(int[] prices) {
        int leftProfit = 0;
        int rightProfit = 0;
        int profit = 0;
        int left= 0;
        int right = prices.length - 1;

        int[] leftMax = new int[prices.length];
        int[] rightMax = new int[prices.length];
        for (int i = 1; i < prices.length; i++){
            leftMax[i] = Math.max(leftMax[i-1], prices[i] - prices[left]);
            if (prices[left] > prices[i])left = i;
        }
        for (int j = prices.length - 2; j >= 0; j--){
            rightMax[j] = Math.max(rightMax[j+1], prices[right] - prices[j]);
            if (prices[right] < prices[j])right = j;
        }

        for (int i = 0;i < prices.length; i++){
            if (leftMax[i] + rightMax[i] > profit){
                profit = leftMax[i] + rightMax[i];
            }
        }
        return profit;








    }
}
