public class maxProfitIV {
    public int maxProfitIV(int k, int[] prices) {

        int profit  = 0;
        int count = 0;
        int index = 0;
        int[] hold = new int[prices.length + 1];
        int[] notHold = new int[prices.length + 1];
        for (int j = 0; j < prices.length; j++ ){
            hold[j] = Integer.MIN_VALUE;
            notHold[j] = 0;
        }
        int[][] dp = new int[prices.length][];
        for (int i = 0; i < prices.length; i++){
            for (int j = 0; j < prices.length; j++){
                hold[j] = Math.max(hold[j], notHold[j] - prices[i]);
                notHold[j] = Math.max(notHold[j], hold[j-1] + prices[i]);
            }
        }

    }
}
