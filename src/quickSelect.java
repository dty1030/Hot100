import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 给定整数数组 nums 和整数 k，请返回数组中第 k 个最大的元素。
 * 请注意，你需要找的是数组排序后的第 k 个最大的元素，而不是第 k 个不同的元素。
 * 你必须设计并实现时间复杂度为 O(n) 的算法解决此问题。
 *
 * ============================================================
 * 常用数据结构时间复杂度速查
 * ============================================================
 *
 * ArrayList (动态数组)
 *   get(i)              O(1)
 *   add(末尾)           O(1) 均摊
 *   add(中间/头部)      O(n)
 *   remove(末尾)        O(1)
 *   remove(中间/头部)   O(n)
 *   contains            O(n)
 *
 * LinkedList (双向链表)
 *   get(i)              O(n)
 *   addFirst/addLast    O(1)
 *   removeFirst/Last    O(1)
 *   remove(已知节点)    O(1)
 *   contains            O(n)
 *
 * HashMap / HashSet
 *   get/put/remove      O(1) 均摊
 *   containsKey         O(1) 均摊
 *   遍历                O(n)
 *
 * TreeMap / TreeSet (红黑树)
 *   get/put/remove      O(log n)
 *   first/last          O(log n)
 *   遍历(有序)          O(n)
 *
 * PriorityQueue (堆)
 *   offer               O(log n)
 *   poll                O(log n)
 *   peek                O(1)
 *
 * Stack / Deque (ArrayDeque)
 *   push/pop/peek       O(1)
 *
 * Queue (LinkedList)
 *   offer/poll/peek     O(1)
 *
 * 数组 Array
 *   access              O(1)
 *   Arrays.sort()       O(n log n)
 *   Arrays.binarySearch O(log n)
 *
 * ============================================================
 */
public class quickSelect {

    private int quickSelect(List<Integer> nums, int k) {
        int pivot = nums.get(nums.size()-1);
        List<Integer> big = new ArrayList<>();
        List<Integer> small = new ArrayList<>();
        List<Integer> equal = new ArrayList<>();
        for (int num: nums){
            if (num == pivot)equal.add(num);
            if (num > pivot)big.add(num);
            if (num < pivot)small.add(num);
        }
        if (k <= big.size())return quickSelect(big, k);
        if (k <= big.size() + equal.size())return equal.get(0);
        return quickSelect(small, k-big.size()-equal.size()); // TODO
    }

    public static void main(String[] args) {
        quickSelect sol = new quickSelect();

        // 测试1: 普通情况，第2大 = 5
        List<Integer> nums1 = new ArrayList<>(Arrays.asList(3, 2, 1, 5, 6, 4));
        System.out.println("Test1: " + sol.quickSelect(nums1, 2)); // 期望: 5

        // 测试2: 有重复元素，第4大 = 4
        List<Integer> nums2 = new ArrayList<>(Arrays.asList(3, 2, 3, 1, 2, 4, 5, 5, 6));
        System.out.println("Test2: " + sol.quickSelect(nums2, 4)); // 期望: 4

        // 测试3: 只有一个元素
        List<Integer> nums3 = new ArrayList<>(Arrays.asList(1));
        System.out.println("Test3: " + sol.quickSelect(nums3, 1)); // 期望: 1

        // 测试4: 第1大 = 最大值
        List<Integer> nums4 = new ArrayList<>(Arrays.asList(7, 3, 5, 1));
        System.out.println("Test4: " + sol.quickSelect(nums4, 1)); // 期望: 7
    }
}

/*
public class Solution {
    private int quickSelect(List<Integer> nums, int k) {
        // 随机选择基准数
        Random rand = new Random();
        int pivot = nums.get(rand.nextInt(nums.size()));
        // 将大于、小于、等于 pivot 的元素划分至 big, small, equal 中
        List<Integer> big = new ArrayList<>();
        List<Integer> equal = new ArrayList<>();
        List<Integer> small = new ArrayList<>();
        for (int num : nums) {
            if (num > pivot)
                big.add(num);
            else if (num < pivot)
                small.add(num);
            else
                equal.add(num);
        }
        // 第 k 大元素在 big 中，递归划分
        if (k <= big.size())
            return quickSelect(big, k);
        // 第 k 大元素在 small 中，递归划分
        if (nums.size() - small.size() < k)
            return quickSelect(small, k - nums.size() + small.size());
        // 第 k 大元素在 equal 中，直接返回 pivot
        return pivot;
    }

    public int findKthLargest(int[] nums, int k) {
        List<Integer> numList = new ArrayList<>();
        for (int num : nums) {
            numList.add(num);
        }
        return quickSelect(numList, k);
    }
}


* */