/**
 * 题目：
 * 求 n! 的个位数。
 *
 * 提示：
 * 这是找规律题，通常是签到题。
 */
public class factorialLastDigit {
    public int factorialLastDigit(int n) {
        if ( n == 1 )return 1;
        else if (n == 2)return  2;
        else if (n == 3)return 6;
        else if (n == 4)return 4;
        else return 0;

    }

    public static void main(String[] args) {
        int[] cases = {1, 2, 3, 4, 5, 10};

        factorialLastDigit solution = new factorialLastDigit();
        for (int n : cases) {
            int ans = solution.factorialLastDigit(n);
            System.out.println("n = " + n + " -> " + ans);
        }
    }
}
