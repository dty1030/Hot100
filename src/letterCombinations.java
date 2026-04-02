import java.util.*;

public class letterCombinations {
    public List<String> letterCombinations(String digits){
        if (Objects.equals(digits, ""))return null;

        List<String> res = new ArrayList<>();

        String[] mapping = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        helper(res, digits, "", 0, mapping);

        return res;
    }

    void helper(List<String> res, String digits, String temp, int index, String[] mmapping){
        if (index == digits.length()){
            res.add(temp);
            return;
        }
        char c = digits.charAt(index);
        String letters = mmapping[c - '0'];
        for (char ch: letters.toCharArray()){
            helper(res, digits, temp + ch, index + 1, mmapping);
        }
    }

    public static void main(String[] args) {
        String digits = "23";

        letterCombinations solution = new letterCombinations();
        List<String> ans = solution.letterCombinations(digits);

        System.out.println(ans);
    }
}
