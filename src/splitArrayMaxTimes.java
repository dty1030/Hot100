/**
 * 题目：
 * 把数组划分为两个和相同的子数组，并且可以继续递归划分下去，
 * 求最多能划分多少次。
 *
 * 提示：
 * 重点想：
 * 1. 什么情况下当前区间可以继续划分
 * 2. 划分后的子问题怎么定义
 * 3. 返回值表示什么
 */
public class splitArrayMaxTimes {
    public int splitArrayMaxTimes(int[] nums) {
        return -1;
    }

    public static void main(String[] args) {
        int[][] cases = {
                {1, 1, 1, 1},
                {2, 2, 2, 2, 2, 2},
                {3, 3, 6, 6},
                {1, 2, 3, 3, 2, 1}
        };

        splitArrayMaxTimes solution = new splitArrayMaxTimes();
        for (int[] nums : cases) {
            int ans = solution.splitArrayMaxTimes(nums);
            System.out.println(ans);
        }
    }
}
