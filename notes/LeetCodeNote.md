# 刷题知识点总结

## 一、常见思维误区

### 1. 下标 vs 值
```java
int left = nums[0];   // ❌ 存的是值
int left = 0;         // ✅ 存的是下标
```
双指针、滑动窗口里，指针存的是**下标**，不是值。

### 2. 指针方向
- 从中心往外扩展：`left--`，`right++`
- 从外往中间收缩：`left++`，`right--`
- 搞混方向会导致逻辑完全反转

### 3. 条件判断方向
找最小值更新：`if (prices[i] < prices[left]) left = i`
找最大值更新：`if (prices[i] > maxVal) maxVal = prices[i]`
找最大利润：`if (profit < cur) profit = cur`，用 `<` 不是 `>`

---

## 二、String 常用操作

### substring 左闭右开
```java
s.substring(left + 1, right)  // 取 [left+1, right-1] 这段
```
循环退出时指针已经多走一步，截取时需要 +1 修正。

---

## 三、算法思路

### 快速排序
```
1. 选 pivot（通常取最右边元素）
2. 双指针 partition：小于 pivot 的换到左边
3. pivot 归位（swap pointer+1 和 right）
4. 递归左右两段
```
**关键**：partition 返回 pivot 的最终下标。

---

### 中心扩展（回文子串）
```java
// 奇数：left = i-1, right = i+1
// 偶数：left = i, right = i+1
while (left >= 0 && right < s.length()) {
    if (s.charAt(left) == s.charAt(right)) { left--; right++; }
    else break;
}
String res = s.substring(left + 1, right);
```

---

### 贪心
- **买卖股票 I**：维护最低买入点，每天计算当天卖出的利润
- **买卖股票 II**：只要今天比昨天贵就累加差值
- **跳跃游戏 I**：维护 `maxReach`，`i > maxReach` 就返回 false
- **跳跃游戏 II**：维护 `currentEnd`，到达边界时 `jumps++`

```
遍历每天 i：
    maxReach = Math.max(maxReach, i + nums[i])  // i + 步数 = 最远落点
    if (i == currentEnd) { jumps++; currentEnd = maxReach; }
```

---

### DFS 递归模板
```java
ReturnType dfs(Node root, ...) {
    if (root == null) return ...; // base case：空节点
    if (满足条件) return root;    // base case：找到目标

    left = dfs(root.left, ...);
    right = dfs(root.right, ...);

    // 根据左右结果决定返回什么
    if (left != null && right != null) return root;
    return left != null ? left : right;
}
```
**常见问题**：
- 忘记 null 的 base case → 空指针异常
- 忘记 return 值 → 编译错误
- 没有标记 visited → 无限递归

---

### DP（动态规划）
**买卖股票 III**（最多2笔）：
```
leftMax[i]  = [0..i] 一笔交易最大利润（从左往右）
rightMax[i] = [i..n-1] 一笔交易最大利润（从右往左）
答案 = max(leftMax[i] + rightMax[i])
```

---

## 四、奇偶判断

```java
n % 2 == 0   // n 是偶数
n % 2 == 1   // n 是奇数
n % 2 == x % 2  // n 和 x 奇偶相同
```
不要用除法或其他方式判断奇偶。

---

## 五、Math 工具方法

```java
Math.max(a, b)   // 取较大值
Math.min(a, b)   // 取较小值
Integer.MIN_VALUE  // DP 中表示"不可达"状态的初始值
```

---

## 六、回溯（Backtracking）

**模板**：每一步做选择，递归处理剩余，不合法就跳过。

```java
void helper(List<String> res, int count, String cur, int index, String s) {
    // base case：到达目标状态
    if (count == 目标 && index == s.length()) {
        res.add(cur);
        return;
    }
    // 枚举所有可能的选择
    for (选择) {
        if (合法) helper(...继续递归...);
    }
}
```

**常见坑**：
- 不合法时应该 **skip**（跳过这次选择），不是 `return`（会终止其他分支）
- base case 要**加入结果**再 return，不是直接 return
- 越界检查要在 substring 之前

---

## 七、DP 边界 +1 技巧

**LCS（最长公共子序列）**：
```java
int[][] dp = new int[text1.length() + 1][text2.length() + 1];
// dp[0][j] 表示空串和 text2 前 j 个字符 → 全是 0
// 多出来的一行一列代表"空串"，避免 dp[i-1][j-1] 越界
```

**状态转移**：
```java
if (text1.charAt(i - 1) == text2.charAt(j - 1))  // 注意 -1
    dp[i][j] = dp[i-1][j-1] + 1;
else
    dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
```

**关键理解**：
- `dp[i][j]` 的 `i` 表示"前 i 个字符"，对应字符串下标是 `i-1`
- 所以 `charAt(i-1)` 才是第 i 个字符

---

## 八、问题转化技巧

**最长回文子序列 → LCS**：
> `s` 的最长回文子序列 = `s` 和 `reverse(s)` 的最长公共子序列

原理：回文正反读一样，所以 s 里的回文子序列一定也是 reverse(s) 的子序列。

**见到陌生题要想：能不能转化成已知问题？**

---

## 九、二分查找模板（求最大满足条件值）

```java
int left = 最小可能值, right = 最大可能值;
while (left < right) {
    int mid = (left + right + 1) / 2;  // 注意 +1 防死循环
    if (check(mid)) left = mid;         // mid 可行，答案可能是 mid 或更大
    else right = mid - 1;                // mid 不可行，答案一定更小
}
return left;
```

**关键**：
- 满足单调性（小的满足大的不满足，或相反）才能二分
- `check(mid)` 是判断"mid 是否可行"的函数
- `mid = (left + right + 1) / 2` 防止 left=right-1 时死循环

---

## 十、字符串处理常用

**split 遇到特殊字符要转义**：
```java
"1.2.3".split("\\.")  // ✅ . 是正则特殊字符
"1.2.3".split(".")     // ❌ . 匹配所有字符
```

**parseInt 自动去前导零**：
```java
Integer.parseInt("001")  // → 1
Integer.parseInt("01")   // → 1
```

**三元运算符处理边界**（比较两个长度不同的数组）：
```java
int a = i < v1.length ? Integer.parseInt(v1[i]) : 0;
int b = i < v2.length ? Integer.parseInt(v2[i]) : 0;
```
越界时补 0，避免特判。

---

## 十一、二叉搜索树（BST）LCA

**利用 BST 大小规律，不需要递归左右两边**：
- p、q 都比 root 小 → LCA 在左子树
- p、q 都比 root 大 → LCA 在右子树
- 否则 root 就是 LCA

和普通二叉树 LCA 的区别：BST 每次只走一个方向，更快。

---

## 十二、常见翻译："中文想法 → 代码"

**站在位置 i，最多跳 nums[i] 步** → `i + nums[i]`
**这一跳的边界** → `currentEnd`
**已经访问过的** → `boolean[] visited`
**上一个选的值** → `int last`
**当前最优解** → `int res` 或 `int maxXxx`

做题难不是写不出代码，而是**想不清楚要维护什么信息**。先把"追踪什么"用中文写出来，再翻译成变量。
