public class maxProfit {
    public int maxProfit(int[] prices) {
        int left = 0;
        int right = prices.length-1;
        int profit = 0;
        for (int i = 0; i < prices.length; i++){
            if (profit < prices[i] - prices[left])profit = prices[i] - prices[left];
            if(prices[left] > prices[i])left = i;
        }
        return profit;
    }

}
