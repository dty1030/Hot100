import java.util.Arrays;

public class merge {
    public void merge(int[] nums1, int m, int[] nums2, int n) {

    }

    public static void main(String[] args) {
        int[] nums1 = {1, 2, 3, 0, 0, 0};
        int m = 3;
        int[] nums2 = {2, 5, 6};
        int n = 3;

        merge solution = new merge();
        solution.merge(nums1, m, nums2, n);

        System.out.println(Arrays.toString(nums1));
    }
}
