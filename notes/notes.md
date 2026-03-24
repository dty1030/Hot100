# 查漏补缺笔记

## 1. 数组/循环基础

### 向右移动腾出位置（从右往左遍历）
插入到 index=0 前，必须从右往左移：
```java
for (int i = cachePointer; i >= 1; i--) {
    cache[i] = cache[i - 1];
}
cache[0] = newElement;
```
- 终止条件是 `i >= 1`，不是 `i >= 0`（否则 cache[-1] 越界）
- 从左往右移会覆盖还没处理的数据

### for-each vs 下标循环
```java
for (int num : nums)       // num 是值
for (int i = 0; i < n; i++) // i 是下标，nums[i] 才是值
```
混用会导致把下标当值比较，是常见 bug。

---

## 2. Java 语法

### List 存值不存下标
```java
arr.add(nums[i]);   // 正确：存值
arr.add(i);         // 错误：存下标
```

### List 初始化
```java
List<Integer> res = new ArrayList<>();           // 空列表
List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3)); // 带初始值
```

### Integer.MAX_VALUE
```java
int min = Integer.MAX_VALUE;  // 正确
int min = Math.Max;           // 不存在，编译报错
```

---

## 3. 链表反转

### 迭代法（三指针）
```
每步：保存 next → 反转 cur.next → 移动 prev 和 cur
终止：cur == null
返回：prev（新头节点）
```

### 递归法
```java
ListNode newHead = reverseList(head.next);
head.next.next = head;  // 让后一个节点指回自己
head.next = null;       // 断开原来的指向
return newHead;         // 一直往上传新头节点
```

---

## 4. 快速选择（第K大）

### 核心思路
选 pivot，把数组分成 big / equal / small 三组，然后：
- `k <= big.size()` → 递归 `quickSelect(big, k)`
- `k <= big.size() + equal.size()` → 返回 `pivot`
- 否则 → 递归 `quickSelect(small, k - big.size() - equal.size())`

### 注意
- LeetCode 入口是 `findKthLargest(int[] nums, int k)`，需要把 int[] 转成 List

---

## 5. 三数之和（双指针）

### 框架
1. 先 `Arrays.sort(nums)`
2. 外层固定 i，内层 left=i+1, right=n-1 双指针
3. sum < 0 → left++；sum > 0 → right--；sum == 0 → 记录

### 去重（3处）
```java
// 1. 跳过重复的 i
if (i > 0 && nums[i] == nums[i-1]) continue;

// 2. 找到答案后跳过重复的 left
while (left < right && nums[left] == nums[left+1]) left++;
left++;

// 3. 找到答案后跳过重复的 right
while (right > left && nums[right] == nums[right-1]) right--;
right--;
```
while 跳过中间重复，while 后的 ++ / -- 才是真正走出最后一个重复位置。

---

## 6. LFU Cache（待完成）

### 当前方案（数组模拟）复杂度 O(n)，会 TLE
### 正确方案：HashMap + 双向链表 + minFreq 变量
- `keyMap`: key → Node（存 value 和 freq）
- `freqMap`: freq → 双向链表（头=最新，尾=最旧）
- get/put 时升频：从旧桶摘出，插入新桶头部
- 淘汰：删 `freqMap[minFreq]` 的尾节点