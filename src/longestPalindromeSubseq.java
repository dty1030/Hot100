public class longestPalindromeSubseq {
    public int longestPalindromeSubseq(String s) {
        int[][] dp = new int[s.length() + 1][s.length() + 1];
        int count = 0;
        String reverse = "";
        for (int i = s.length() - 1; i >=0; i--){
            reverse = reverse + s.charAt(i);
        }
        for (int j = 1; j <= s.length(); j++){
            for (int k = 1; k <= reverse.length(); k++){
                if (s.charAt(j - 1) == reverse.charAt(k - 1)){
                    dp[j][k] = dp[j-1][k-1] + 1;}
                else dp[j][k] = Math.max(dp[j-1][k], dp[j][k-1]);
            }
        }
        return dp[s.length()][reverse.length()];

    }
}
