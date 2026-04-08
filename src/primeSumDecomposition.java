/**
 * 题目：
 * 判断能否把 n 分解为 k 个素数之和，
 * 并且其中奇数素数的个数是 m 的倍数。
 *
 * 提示：
 * 这是找规律题，重点分析：
 * 1. n 的奇偶性
 * 2. k 的数量约束
 * 3. 奇数素数个数与 m 的倍数关系
 */
public class primeSumDecomposition {
    public boolean canDecompose(int n, int k, int m) {
        for (int x = 0; x <= k; x += m){
            if (x % 2 == 0 && n % 2 == 0)return true;
            if (x % 2 != 0 && n % 2 != 0)return true;
        }

        return false;
    }

    public static void main(String[] args) {
        int[][] cases = {
                {10, 3, 1},
                {11, 3, 2},
                {20, 4, 2},
                {7, 2, 1}
        };

        primeSumDecomposition solution = new primeSumDecomposition();
        for (int[] testCase : cases) {
            int n = testCase[0];
            int k = testCase[1];
            int m = testCase[2];
            boolean ans = solution.canDecompose(n, k, m);
            System.out.println("n = " + n + ", k = " + k + ", m = " + m + " -> " + ans);
        }
    }
}
