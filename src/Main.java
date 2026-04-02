import java.util.Scanner;

// 注意类名必须为 Main, 不要有任何 package xxx 信息
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        // 注意 hasNext 和 hasNextLine 的区别
        while (in.hasNextInt()) { // 注意 while 处理多个 case
            int a = in.nextInt();
            int b = in.nextInt();
            System.out.println(a + b);
        }
    }
    public String helper(String string){
        String res = "";
        int digits = 0;
        int chars = 0;
        int others = 0;
        int whitespace = 0;
        for(int i = 0; i < string.length(); i++){
            char c = res.charAt(i);
            string.concat(string)
            if (getClass().string.charAt(i) == Class.chars) {
                if (!res.contains("chars:")) {
                    
                    res = res + "chars:";
                }

            }
            if (string.getClass() == Class.Integer) {
                
            }
            if (string.charAt(i) == ' ') {
                if (!res.contains("whitespace:")) {
                res = res + "whitespace:";
                }

            }
        }
    }
    
}