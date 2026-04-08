public class maxProfitII {
    public int maxProfitII(int[] prices) {

        int left = 0;
        int profit = 0;
        for (int i = 0; i < prices.length; i++){
            if (i > 0 && prices[i] > prices[i-1])profit += prices[i] - prices[i-1];
        }
        return profit;
    }
}
