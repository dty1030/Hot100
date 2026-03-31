/*
* 912, 手撕快排
* */


import java.util.ArrayList;
import java.util.List;

class sortArray {
    public int[] sortArray(int[] nums) {

        //int pivot = nums[nums.length/2+1];
        int left = 0;
        int right = nums.length-1;
        quickSort(nums, left, right);
        return nums;


    }
    void quickSort(int[] nums, int left, int right) {
        if (left >= right) return;  // base case

        int p = partition(nums, left, right); // partition 后 pivot 归位，返回其下标
        quickSort(nums, left, p - 1);
        quickSort(nums, p + 1, right);
    }
    int partition(int[] nums, int left, int right){
        int pivot = nums[right];       // 选最右边的元素作为基准值
        int pointer = left - 1;        // pointer = "比pivot小的区域" 的右边界，初始为空（left-1 表示区域还不存在）

        for (int j = left; j < right; j++){   // j 逐个探索每个元素（不包括right，那是pivot）
            if (nums[j] < pivot){             // 发现一个比pivot小的元素
                pointer++;                    // 小区域向右扩一格
                swap(nums, pointer, j);

            }
            // 如果 nums[j] >= pivot，什么都不做，j继续往右走
        }
        pointer++;
        swap(nums, pointer, right);



        // 循环结束后：
        // nums[left .. pointer]       全是 < pivot 的元素
        // nums[pointer+1 .. right-1]  全是 >= pivot 的元素
        // nums[right] 还是 pivot，它还没放到正确位置！

        // ❌ 下面这行是错的：只是把数字赋给了 pivot 变量，数组没有任何变化
        // pivot = pointer + 1;

        // ✅ 应该做的是：
        // 1. 把 nums[right]（pivot）和 nums[pointer+1] 交换，让pivot归位
        // 2. return pointer+1（pivot最终所在的下标）
        return pointer;

    }

    void swap(int[] nums, int pointer, int index){
        int temp = nums[pointer];     // 把这个小元素换到小区域最右边（pointer位置）
        nums[pointer] = nums[index];
        nums[index] = temp;
    }
}