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


---

## 字符串
## 1. 比较字符串内容，优先用 equals

对，平时比较字符串“值是否相同”，用：

a.equals(b)

或者更稳一点：

Objects.equals(a, b)

区别是：

- a.equals(b)：如果 a 是 null，会空指针
- Objects.equals(a, b)：可以安全处理 null

比如：

Objects.equals(digits, "")

这是合法的，意思是“digits 的内容是不是空串”。

但如果只是判断空串，更常见的是：

digits == null || digits.isEmpty()

———

## 2. == 比较的是“是不是同一个对象引用”

对，== 对对象来说比较的是：

- 两个引用是不是指向同一个对象

不是比较内容。

比如：

String a = new String("hello");
String b = new String("hello");
System.out.println(a == b); // false
System.out.println(a.equals(b)); // true

因为：

- 内容一样
- 但不是同一个对象

———

## 3. equals 比较的是字符串内容

对。

String a = "hello";
String b = "hello";
System.out.println(a.equals(b)); // true

因为内容相同。

———

## 4. String a = "hello" 会不会每次都 new 一个对象？

不会直接理解成“每次都 new 一个新的 String 对象”。

字符串字面量比如：

"hello"

通常会放进 字符串常量池 里。

所以：

String a = "hello";
String b = "hello";

很多情况下，a 和 b 会指向常量池里的同一个字符串对象。

因此：

a == b

这里通常是 true。

———

## 5. 你这个例子里，a == b 是什么？

String a = "hello";
String b = "hello";

这里通常：

a == b   // true

因为两个都引用了字符串常量池里的同一个 "hello"。

但你不要拿这个现象去做字符串内容比较，因为这只是字面量池带来的结果，不是 == 适合比较字符串。

你应该养成固定习惯：

- 比内容：equals
- 比是不是同一个对象：==

———

## 6. 一个最容易记住的对比例子

String a = "hello";
String b = "hello";
String c = new String("hello");

System.out.println(a == b);      // true
System.out.println(a == c);      // false
System.out.println(a.equals(c)); // true

这样就很清楚了：

- a 和 b：都指向常量池同一个对象
- c：显式 new 了一个新对象
- 内容一样，但引用不同

———

## 7. 你这题里怎么写更合适

如果你想判断空输入，推荐直接写成这种思路：

if (digits == null || digits.isEmpty()) {
return new ArrayList<>();
}

比 return null 更合适。

———

一句话总结：

- ==：比引用
- equals：比内容
- 字符串字面量通常进常量池，所以 "hello" == "hello" 常常是 true
- 但比较字符串内容时，永远优先用 equals
---