import java.util.Scanner;

// 注意类名必须为 Main, 不要有任何 package xxx 信息
public class zuoyebangTest {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        // 注意 hasNext 和 hasNextLine 的区别
        while (in.hasNextLine()) { // 注意 while 处理多个 case
            String s= in.nextLine();
            System.out.println(helper(s));
        }
    }
    public static String helper(String string) {
        String res = "";
        int digits = 0;
        int chars = 0;
        int others = 0;
        int whitespace = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isLetter(c)) {
                chars++;
            }
            else if (Character.isDigit(c)) {
                digits++;
            }
            else if (c == ' ') {
                whitespace++;
            } else {
                others++;
            }
        }
        res = res + "chars:"+ chars + "\n" + "digits:" + digits + "\n" + "whitespace:" + whitespace
         + "\n" + "others:" + others;
        return res;
    }
}