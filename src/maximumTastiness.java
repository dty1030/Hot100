import java.util.Arrays;

public class maximumTastiness {
    public int maximumTastiness(int[] price, int k) {
        int tastiness = 0;
        Arrays.sort(price);
        int leftIndex = 0;
        int rightIndex = price[price.length-1] - price[0];
        while (leftIndex < rightIndex){
            int count = 0;
            int mid = (leftIndex + rightIndex + 1)/2;
            if (canSelect(price, mid, k))leftIndex = mid;
            else {
                rightIndex = mid - 1;
            }
        }
        return leftIndex;


    }
    boolean canSelect(int[] price, int k, int mid){
        int last = price[0];
        int count = 1;
        for (int i = 1; i < price.length; i++) {
            if (price[i] >= last + mid) {
                count++;
                last = price[i];  // 更新上一个选的价格
            }
        }
        return count >= k;
    }
}
