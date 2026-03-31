public class longestPalindrome{

    /*
     * Java String 常用方法速查：
     * 1. String.valueOf(x)
     *    把其他类型转成字符串。
     *    例：String.valueOf(123) -> "123"
     *
     * 2. s.charAt(index)
     *    取指定下标的字符。
     *    例：s.charAt(0)
     *
     * 3. s.length()
     *    返回字符串长度。
     *
     * 4. s.isEmpty()
     *    判断字符串是否为空串 ""。
     *
     * 5. s.substring(begin)
     *    从 begin 截取到结尾。
     *
     * 6. s.substring(begin, end)
     *    截取 [begin, end) 这一段，左闭右开。
     *
     * 7. s.equals(other)
     *    判断两个字符串内容是否相同。
     *
     * 8. s.equalsIgnoreCase(other)
     *    忽略大小写比较。
     *
     * 9. s.indexOf(ch)
     *    返回字符/字符串第一次出现的位置，不存在返回 -1。
     *
     * 10. s.lastIndexOf(ch)
     *     返回最后一次出现的位置。
     *
     * 11. s.contains(str)
     *     判断是否包含某个子串。
     *
     * 12. s.startsWith(prefix)
     *     判断是否以某个前缀开头。
     *
     * 13. s.endsWith(suffix)
     *     判断是否以某个后缀结尾。
     *
     * 14. s.toCharArray()
     *     把字符串转成字符数组。
     *
     * 15. s.toLowerCase() / s.toUpperCase()
     *     转小写 / 大写。
     *
     * 16. s.trim()
     *     去掉首尾空白字符。
     *
     * 17. s.replace(oldChar, newChar)
     *     替换字符。
     *
     * 18. s.replace(oldStr, newStr)
     *     替换字符串。
     *
     * 19. s.split(regex)
     *     按规则切分字符串。
     *
     * 20. Integer.parseInt(str)
     *     把数字字符串转成 int，例如 "123" -> 123。
     */

    public String longestPalindrome(String s) {

        if (s.isEmpty())return "";

        String res = "";
        int index = s.length()/2;
        int left = 0;
        int right = s.length()-1;

        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            left = i - 1;
            right = i + 1;
            String leftSubString = "";
            String rightSubString = "";
            while (left >= 0 && right < s.length()){
                char leftChar = s.charAt(left);
                char rightChar = s.charAt(right);
                if (leftChar==rightChar) {
                    leftSubString = leftSubString + leftChar;
                    rightSubString = rightSubString + rightChar;
                }
                else{break;}
                left--;
                right++;

            }
            //String temp = leftSubString + s.charAt(i) + rightSubString;
            String temp = s.substring(left + 1, right);
            if (temp.length() > res.length())res = temp;

        }
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            left = i;
            right = i + 1;
            String leftSubString = "";
            String rightSubString = "";
            while (left >= 0 && right < s.length()){
                char leftChar = s.charAt(left);
                char rightChar = s.charAt(right);
                if (leftChar==rightChar) {
                    leftSubString = leftSubString + leftChar;
                    rightSubString = rightSubString + rightChar;
                }
                else{break;}
                left--;
                right++;

            }
            //String temp = leftSubString + s.charAt(i) + rightSubString;
            String temp = s.substring(left + 1, right);
            if (temp.length() > res.length())res = temp;

        }
        return res;


    }


    public static void main(String[] args) {
        longestPalindrome solution = new longestPalindrome();

        String[] testCases = {
                "",
                "abcabcbb",
                "bbbbb",
                "pwwkew",
                "dvdf",
                "abba"
        };

        for (String s : testCases) {
            String result = solution.longestPalindrome(s);
            System.out.println("输入: \"" + s + "\" -> 最长无重复子串长度: " + result);
        }
    }
}
