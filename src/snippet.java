import java.util.Scanner; 
import java.util.ArrayList;
import
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
    public ArrayList<Integer> helper(int n, ArrayList<Integer> arrayList){
        //1<= n <= 10

        int temp;
        ArrayList arr = new ArrayList();
        if(arr.size() == n)return arr;
        int temp = n - 2;
        if (temp >= 1){
            
        }
        if ()

    }
}