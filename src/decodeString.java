import java.util.Stack;

public class decodeString {
    public String decodeString(String s) {
        String cur = "";
        int num = 0;
        Stack<Integer> numStack = new Stack<>();
        Stack<String> strStack = new Stack<String>();
        for (int i = 0; i < s.length(); i++){
            if (Character.isDigit(s.charAt(i))) {
                num = num * 10 + (s.charAt(i) - '0');
            }
            else if (s.charAt(i) == '[') {
                numStack.push(num);
                strStack.push(cur);
                num = 0;
                cur = "";
            } else if (s.charAt(i) == ']') {
                int k = numStack.pop();
                String temp = cur;
                cur = "";
                for (int j = 0; j < k; j++){
                    cur += temp;
                }
                cur = strStack.pop() + cur;
            }
            else {
                cur += s.charAt(i);
            }
        }
        return cur;

    }

    public static void main(String[] args) {
        decodeString solution = new decodeString();

        String[] testCases = {
                "3[a]",              // "aaa"
                "3[a]2[bc]",         // "aaabcbc"
                "3[a2[c]]",          // "accaccacc"（嵌套）
                "2[abc]3[cd]ef",     // "abcabccdcdcdef"
                "100[leetcode]",     // 多位数字
                "abc",               // 没有方括号
        };

        for (String s : testCases) {
            System.out.println("输入: \"" + s + "\" -> " + solution.decodeString(s));
        }
    }

}
