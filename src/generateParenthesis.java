import java.util.ArrayList;
import java.util.List;

public class generateParenthesis {
    public List<String> generateParenthesis(int n){

        List<String> res = new ArrayList<>();
        helper(n, "", res, 0, 0);
        return res;
    }
    void helper(int n, String path, List<String> res, int left, int right){
        if (path.length() == 2 * n){
            res.add(path);
            return;
        }

        if (left < n) {
            helper(n, path + '(', res, left + 1, right);
        }

        if (left > right){
            helper(n, path + ')', res, left, right + 1);
        }

    }

    public static void main(String[] args) {
        int[] cases = {1, 2, 3, 4, 5};

        generateParenthesis solution = new generateParenthesis();
        for (int n : cases) {
            List<String> ans = solution.generateParenthesis(n);
            System.out.println("n = " + n + " -> " + ans);
        }

    }
}
