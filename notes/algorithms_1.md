# 🧮 算法与手撕题汇总

> 整合所有面试中出现的算法题、刷题笔记、回溯/DFS 框架。
> 面试八股知识点请参考 Interview.md。

---

## 一、排序算法

### 1. 快速排序

```java
public void quickSort(int[] nums, int left, int right) {
    if (left >= right) return;
    
    int pivot = nums[left + (right - left) / 2]; // 选中间元素做 pivot
    int i = left, j = right;
    
    while (i <= j) {
        while (nums[i] < pivot) i++;
        while (nums[j] > pivot) j--;
        if (i <= j) {
            int temp = nums[i];
            nums[i] = nums[j];
            nums[j] = temp;
            i++;
            j--;
        }
    }
    
    quickSort(nums, left, j);
    quickSort(nums, i, right);
}
// 时间：平均 O(n log n)，最坏 O(n²)
// 空间：O(log n)（递归栈）
// 不稳定排序
```

### 2. 冒泡排序

```java
public void bubbleSort(int[] nums) {
    int n = nums.length;
    for (int i = 0; i < n - 1; i++) {
        boolean swapped = false; // 优化：如果一轮没交换说明已有序
        for (int j = 0; j < n - 1 - i; j++) {
            if (nums[j] > nums[j + 1]) {
                int temp = nums[j];
                nums[j] = nums[j + 1];
                nums[j + 1] = temp;
                swapped = true;
            }
        }
        if (!swapped) break;
    }
}
// 时间：平均 O(n²)，最好 O(n)
// 稳定排序
```

---

## 二、经典手撕题

### 3. 最长回文子串（中心扩展）

```java
public String longestPalindrome(String s) {
    if (s == null || s.length() < 1) return "";
    int start = 0, end = 0;
    for (int i = 0; i < s.length(); i++) {
        int len1 = expandAroundCenter(s, i, i);     // 奇数中心 "aba"
        int len2 = expandAroundCenter(s, i, i + 1); // 偶数中心 "abba"
        int maxLen = Math.max(len1, len2);
        if (maxLen > end - start) {
            start = i - (maxLen - 1) / 2;
            end = i + maxLen / 2;
        }
    }
    return s.substring(start, end + 1);
}

private int expandAroundCenter(String s, int left, int right) {
    while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
        left--;
        right++;
    }
    return right - left - 1; // 退出循环时 left 和 right 多挪了一步
}
// 时间 O(n²)，空间 O(1)
```

### 4. N 个线程交替打印 1~m

```java
import java.util.concurrent.locks.*;

public class AlternatePrinting {
    private static int count = 1;
    private static int turn = 0;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition condition = lock.newCondition();

    public static void print(int n, int m) {
        for (int i = 0; i < n; i++) {
            final int id = i;
            new Thread(() -> {
                while (true) {
                    lock.lock();
                    try {
                        while (turn != id && count <= m) {
                            condition.await(); // 必须用 while 防止虚假唤醒
                        }
                        if (count > m) {
                            condition.signalAll();
                            break;
                        }
                        System.out.println(Thread.currentThread().getName() + " : " + count++);
                        turn = (turn + 1) % n;
                        condition.signalAll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();
                    }
                }
            }, "线程" + (i + 1)).start();
        }
    }
    public static void main(String[] args) { print(3, 10); }
}
```

### 5. 判断完全二叉树（BFS）

```java
public boolean isCompleteTree(TreeNode root) {
    if (root == null) return true;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    boolean hasNull = false;
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        if (node == null) {
            hasNull = true;
        } else {
            if (hasNull) return false; // 之前遇到 null，现在又有非 null → 不完全
            queue.offer(node.left);
            queue.offer(node.right);
        }
    }
    return true;
}
// 时间 O(n)，空间 O(n)
```

### 6. TopK 问题（小顶堆）

```java
public int findKthLargest(int[] nums, int k) {
    PriorityQueue<Integer> heap = new PriorityQueue<>(); // 小顶堆
    for (int num : nums) {
        heap.offer(num);
        if (heap.size() > k) heap.poll(); // 保持堆大小为 k
    }
    return heap.peek(); // 堆顶就是第 k 大
}
// 时间 O(n log k)，空间 O(k)
// 为什么用小顶堆？堆里维护最大的 k 个，堆顶是 k 个中最小的 = 第 k 大
```

### 7. 有序数组转平衡 BST

```java
public TreeNode sortedArrayToBST(int[] nums) {
    return buildTree(nums, 0, nums.length - 1);
}

private TreeNode buildTree(int[] nums, int left, int right) {
    if (left > right) return null;
    int mid = left + (right - left) / 2;
    TreeNode root = new TreeNode(nums[mid]);
    root.left = buildTree(nums, left, mid - 1);
    root.right = buildTree(nums, mid + 1, right);
    return root;
}
// 时间 O(n)，空间 O(log n)
```

### 8. 三数之和（排序 + 双指针）

```java
public List<List<Integer>> threeSum(int[] nums) {
    List<List<Integer>> res = new ArrayList<>();
    Arrays.sort(nums);
    
    for (int i = 0; i < nums.length - 2; i++) {
        if (nums[i] > 0) break; // 排序后第一个数 > 0，不可能凑成 0
        if (i > 0 && nums[i] == nums[i - 1]) continue; // 去重1：跳过重复的 i
        
        int left = i + 1, right = nums.length - 1;
        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];
            if (sum < 0) {
                left++;
            } else if (sum > 0) {
                right--;
            } else {
                res.add(Arrays.asList(nums[i], nums[left], nums[right]));
                // 去重2、3：跳过重复的 left 和 right
                while (left < right && nums[left] == nums[left + 1]) left++;
                while (left < right && nums[right] == nums[right - 1]) right--;
                left++;
                right--;
            }
        }
    }
    return res;
}
// 时间 O(n²)，空间 O(1)（不算输出）
// 关键：3 处去重 + 排序是前提
```

### 9. 路径总和 II（回溯）

给定二叉树和目标和 `targetSum`，找出所有根节点到叶子节点路径总和等于 targetSum 的路径。

```java
public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
    List<List<Integer>> res = new ArrayList<>();
    dfs(root, targetSum, new ArrayList<>(), res);
    return res;
}

private void dfs(TreeNode node, int remain, List<Integer> path, List<List<Integer>> res) {
    if (node == null) return;
    
    path.add(node.val);
    
    // 到达叶子节点且剩余值为 0 → 找到一条路径
    if (node.left == null && node.right == null && remain == node.val) {
        res.add(new ArrayList<>(path)); // 存快照！不是存引用
    }
    
    dfs(node.left, remain - node.val, path, res);
    dfs(node.right, remain - node.val, path, res);
    
    path.remove(path.size() - 1); // 回溯：撤销选择
}
// 时间 O(n²)（最坏每个节点都在路径上），空间 O(n)（递归深度）
```

### 10. 链表反转

```java
// 迭代法（三指针）
public ListNode reverseList(ListNode head) {
    ListNode prev = null, cur = head;
    while (cur != null) {
        ListNode next = cur.next; // 保存下一个
        cur.next = prev;          // 反转指向
        prev = cur;               // prev 前进
        cur = next;               // cur 前进
    }
    return prev; // prev 是新头节点
}

// 递归法
public ListNode reverseListRecursive(ListNode head) {
    if (head == null || head.next == null) return head;
    ListNode newHead = reverseListRecursive(head.next);
    head.next.next = head; // 让后一个节点指回自己
    head.next = null;       // 断开原来的指向
    return newHead;         // 一直往上传新头节点
}
```

### 10.5 链表冒泡排序

链表不能像数组那样用下标随机访问，所以冒泡排序需要**用指针遍历 + 交换节点值**（不交换节点本身，更简单）。

```java
public ListNode bubbleSortList(ListNode head) {
    if (head == null || head.next == null) return head;
    
    ListNode end = null; // 每轮冒泡的终止位置（已排好的部分）
    
    while (end != head.next) { // 直到所有节点都排好
        ListNode cur = head;
        while (cur.next != end) {
            if (cur.val > cur.next.val) {
                // 交换值（不交换节点，比改指针简单得多）
                int temp = cur.val;
                cur.val = cur.next.val;
                cur.next.val = temp;
            }
            cur = cur.next;
        }
        end = cur; // 最后一个被比较的节点已经是最大的了
    }
    return head;
}
// 时间 O(n²)，空间 O(1)
// 注意：链表排序更推荐用归并排序 O(n log n)（LeetCode 148）
```

### 11. LeetCode 3260 变体——最大回文数字（数字可换序，不能 0 开头）

```java
public String largestPalindrome(String s) {
    int[] freq = new int[10];
    for (char c : s.toCharArray()) freq[c - '0']++;
    
    StringBuilder half = new StringBuilder();
    String middle = "";
    
    // 从 9 到 0 贪心，尽量用大数字
    for (int d = 9; d >= 0; d--) {
        int pairs = freq[d] / 2;
        for (int i = 0; i < pairs; i++) half.append(d);
        if (freq[d] % 2 == 1 && middle.isEmpty()) {
            middle = String.valueOf(d);
        }
    }
    
    // 处理前导 0
    if (half.length() > 0 && half.charAt(0) == '0') {
        return middle.isEmpty() ? "0" : middle;
    }
    
    String result = half.toString() + middle + half.reverse().toString();
    return result.isEmpty() ? "" : result;
}
```

### 12. 数组按 k 分组求积分和最小

```java
// 允许重排：排序后连续 k 个一组
public int minCost(int[] nums, int k) {
    Arrays.sort(nums); // 排序让差值最小的数字在一组
    int totalCost = 0;
    for (int i = 0; i < nums.length; i += k) {
        totalCost += nums[Math.min(i + k - 1, nums.length - 1)] - nums[i];
    }
    return totalCost;
}

// 不能重排（连续子数组）：DP
public int minCostDP(int[] nums, int k) {
    int n = nums.length;
    int[] dp = new int[n + 1];
    Arrays.fill(dp, Integer.MAX_VALUE);
    dp[0] = 0;
    for (int i = k; i <= n; i += k) {
        for (int j = i - k; j >= 0; j -= k) {
            int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
            for (int x = j; x < i; x++) {
                max = Math.max(max, nums[x]);
                min = Math.min(min, nums[x]);
            }
            if (dp[j] != Integer.MAX_VALUE)
                dp[i] = Math.min(dp[i], dp[j] + max - min);
        }
    }
    return dp[n];
}
```

### 13. 区间映射——输入一个值，找到它所在区间对应的数值

**典型场景**：成绩分段（0~59→不及格，60~79→良好，80~100→优秀）。

```java
// 方案：TreeMap + floorEntry（O(log n) 查找）
TreeMap<Integer, String> rangeMap = new TreeMap<>();
rangeMap.put(0, "不及格");   // [0, 60)
rangeMap.put(60, "及格");    // [60, 70)
rangeMap.put(70, "良好");    // [70, 85)
rangeMap.put(85, "优秀");    // [85, 100]

// 查询：输入一个分数，返回等级
public String getLevel(int score) {
    // floorEntry：找到 <= score 的最大 key
    Map.Entry<Integer, String> entry = rangeMap.floorEntry(score);
    return entry == null ? "无效分数" : entry.getValue();
}

getLevel(72);  // → "良好"（floorEntry 找到 key=70）
getLevel(59);  // → "不及格"（floorEntry 找到 key=0）
getLevel(90);  // → "优秀"（floorEntry 找到 key=85）

// 底层原理：TreeMap 基于红黑树，floorEntry 就是在树中找 <= 目标值的最大节点
// 时间复杂度 O(log n)，比遍历所有区间 O(n) 高效得多
```

### 14. 反射获取成员变量——字段名为 city_编号，根据编号获取城市名

```java
public class CityConfig {
    public String city_001 = "北京";
    public String city_002 = "上海";
    public String city_003 = "深圳";
}

// 通过反射，根据编号动态获取字段值
public String getCityByCode(String code) throws Exception {
    CityConfig config = new CityConfig();
    String fieldName = "city_" + code; // 拼出字段名
    Field field = CityConfig.class.getDeclaredField(fieldName);
    field.setAccessible(true); // 如果是 private 字段需要这一步
    return (String) field.get(config);
}

getCityByCode("002"); // → "上海"

// 更好的做法：别用反射，用 Map
Map<String, String> cityMap = Map.of("001", "北京", "002", "上海", "003", "深圳");
cityMap.get("002"); // → "上海"
// 面试时先说 Map 方案（简单高效），再补充反射方案（展示你知道反射）
```

---

## 三、回溯算法框架

### 核心骨架（所有回溯题的变体）

```java
void backtrack(当前状态, 选择列表, 路径path, 结果集res) {
    if (满足收集条件) {
        res.add(new ArrayList<>(path)); // 存快照！
        return;
    }
    for (选择 : 当前层的候选列表) {
        path.add(选择);                 // 做选择
        backtrack(下一层状态, ...);      // 递归
        path.remove(path.size() - 1);   // 撤销选择
    }
}
```

### 两种回溯树

```
类型1：二叉决策树（当前元素选/不选）→ subsets
类型2：for 枚举树（当前层谁做下一个元素）→ permute, combinationSum

区分口诀：
"当前这个元素要不要" → 二叉树
"这一层谁来做下一个元素" → for 枚举
```

### 同层去重（combinationSum2、subsetsWithDup）

```java
Arrays.sort(nums); // 必须先排序！
for (int i = start; i < nums.length; i++) {
    if (i > start && nums[i] == nums[i - 1]) continue; // 同层后续重复值跳过
    path.add(nums[i]);
    backtrack(i + 1, ...);
    path.remove(path.size() - 1);
}
// i > start（不是 i > 0）才是"本层后续候选"
```

### 常见错误清单

1. `res.add(path)` → 错！path 后面还会变，要 `res.add(new ArrayList<>(path))`
2. `add` 和 `remove` 不成对 → 状态恢复错误
3. 用错主角：`for (int i = start; ...)` 里选中的是 `nums[i]`，不是 `nums[start]`
4. 忘记下一层从 `i + 1` 开始（每个位置只能用一次的题）
5. 把"同层去重"写成"只要重复就跳过" → 会误伤不同层的合法选择

---

## 四、网格 DFS 框架（岛屿类题目）

### 通用模板

```java
void dfs(char[][] grid, int i, int j) {
    if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length) return;
    if (grid[i][j] != '1') return;
    grid[i][j] = '0'; // 标记已访问
    dfs(grid, i - 1, j);
    dfs(grid, i + 1, j);
    dfs(grid, i, j - 1);
    dfs(grid, i, j + 1);
}
```

### 做题先问 4 个问题

| 问题 | numIslands | solve (被围绕的区域) | maxAreaOfIsland |
|------|-----------|---------------------|----------------|
| 目标格子？ | `'1'` | `'O'` | `1` |
| 从哪启动？ | 全图扫描遇 `'1'` | **只从边界** `'O'` | 全图扫描遇 `1` |
| 标记方式？ | `'1'→'0'` | `'O'→'P'`（保护） | `1→0` |
| 结果？ | 启动几次=几个岛 | `O→X`, `P→O` | 取最大面积 |

### 常见坑

- 把"连通区域"想成矩形 → DFS 处理的是不规则的连通性
- `helper` 里塞太多逻辑 → 主函数决定从哪启动，helper 只负责处理连通块
- `'O'`（字母O）和 `'0'`（数字0）混淆

---

## 五、查漏补缺（做题高频易错点）

### for-each vs 下标循环

```java
for (int num : nums)       // num 是值
for (int i = 0; i < n; i++) // i 是下标，nums[i] 才是值
// 混用会导致把下标当值比较
```

### List 存值不存下标

```java
arr.add(nums[i]);   // 正确：存值
arr.add(i);         // 错误：存下标
```

### c - '0' 字符转数字

```java
char c = '7';
int x = c - '0'; // → 7
// 不是"去掉引号"，是用 ASCII 码差值转换
```

### String 不可变

```java
String s = "abc";
s.concat("d");         // s 还是 "abc"！concat 返回新字符串
s = s.concat("d");     // s 变成 "abcd"（接住返回值）
```

---

## 六、笔试/OJ 真题

### 15. 找字符 'a' 的位置（签到题）

**题意**：给一个长度为 5 的字符串（由 'a'~'e' 各一个组成，下标从 1 开始），输出 'a' 的位置。

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        // indexOf 返回的是从 0 开始的下标，题目要求从 1 开始
        System.out.println(s.indexOf('a') + 1);
    }
}
// 思路：直接用 String.indexOf()，别忘了 +1 转换为 1-based 下标
// 也可以遍历：for (int i = 0; i < 5; i++) if (s.charAt(i) == 'a') return i + 1;
```

### 16. IOI 赛制排名（模拟 + HashMap）

**题意**：n 次提交记录 (user, problem, score)。每个用户每道题取最高分，总分 = 所有题目最高分之和。每次提交后输出用户 1 的排名（并列排名规则：分数相同占相同名次，如 100,100,80 → 排名 1,1,3）。

**思路**：

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        
        // userScores[u][p] = 用户 u 在题目 p 上的最高分
        // 用 Map<Integer, Map<Integer, Integer>> 存储
        Map<Integer, Map<Integer, Integer>> userBest = new HashMap<>();
        // totalScore[u] = 用户 u 的总分
        Map<Integer, Integer> totalScore = new HashMap<>();
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < n; i++) {
            int user = in.nextInt();
            int problem = in.nextInt();
            int score = in.nextInt();
            
            // 更新该用户在该题的最高分
            Map<Integer, Integer> problems = userBest.computeIfAbsent(user, k -> new HashMap<>());
            int oldBest = problems.getOrDefault(problem, 0);
            
            if (score > oldBest) {
                problems.put(problem, score);
                // 总分增加差值
                totalScore.merge(user, score - oldBest, Integer::sum);
            }
            
            // 计算用户 1 的排名
            int myScore = totalScore.getOrDefault(1, 0);
            int rank = 1;
            for (int otherScore : totalScore.values()) {
                if (otherScore > myScore) rank++; // 分数比我高的人数 + 1 = 我的排名
            }
            sb.append(rank).append('\n');
        }
        System.out.print(sb);
    }
}
// 时间复杂度：O(n * U)，U 是用户数。如果 U 很大需要用 TreeMap 优化
// 关键点：
// 1. 每题取最高分 → HashMap 存每题 best
// 2. 总分 = 所有题 best 之和 → 用差值增量更新，不用每次重新算
// 3. 排名 = 分数严格比我高的人数 + 1（并列排名）
```

### 17. 重排数组使拼接字符串字典序最小（贪心）

**题意**：字符串 s 长度为 n，数组 a 长度为 n。重排数组 a，然后按 1~n 的顺序，第 i 次操作在字符串 t 的末尾拼接 a'[i] 个 s[i]。要求最终 t 的字典序最小。

**思路**：

```
核心观察：
- 第 i 次操作往 t 末尾追加 a'[i] 个字符 s[i]
- t 最终 = s[1] 重复 a'[1] 次 + s[2] 重复 a'[2] 次 + ... + s[n] 重复 a'[n] 次
- 要 t 字典序最小 → 让"重要的位置"（前面的字符）尽量小

贪心策略：
- s 的每个位置的字符是固定的，我们只能决定每个位置重复几次
- 如果 s[i] 是较小的字符（如 'a'），我们希望它重复多次（大的 a 值分给它）
- 如果 s[i] 是较大的字符（如 'z'），我们希望它重复少次（小的 a 值分给它）

但要注意位置的优先级：靠前的位置更重要！

更精确的贪心：
- 将数组 a 排序
- 对 s 中每个位置，按照"该位置的字符越小、位置越靠前"的优先级
- 分配较大的 a 值给字典序贡献最大的位置（小字符+靠前位置）
```

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        String s = in.next();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = in.nextInt();
        
        // 创建索引数组，按字符排序（字符小的排前面，字符相同按位置排）
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        
        // 字符越小 → 应该分配越大的重复次数（让小字符多出现）
        // 字符相同时，位置越靠前 → 也应该分配越大的重复次数
        Arrays.sort(indices, (x, y) -> {
            if (s.charAt(x) != s.charAt(y)) return s.charAt(x) - s.charAt(y);
            return x - y;
        });
        
        Arrays.sort(a); // a 从小到大
        
        // 最大的 a 值分给最小的字符（贪心）
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[indices[i]] = a[n - 1 - i]; // 倒着分配
        }
        
        // 构造结果字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < result[i]; j++) {
                sb.append(s.charAt(i));
            }
        }
        System.out.println(Arrays.toString(result)); // 输出重排后的数组
    }
}
// 注意：这题的贪心策略需要根据具体约束调整
// 核心思想：小字符多重复、大字符少重复 → 字典序最小
```

### 18. 迭代函数 f(x) = min(x,b) + gcd(x,b)，求 f^(n)(a)

**题意**：给定 a, b, n。f(x) = min(x, b) + gcd(x, b)。f^(1)(x) = f(x)，f^(k+1)(x) = f(f^(k)(x))。求 f^(n)(a)。

**思路**：

```
分析 f(x) 的行为：
- 如果 x <= b：f(x) = x + gcd(x, b)，结果 > x（单调递增）
- 如果 x > b：f(x) = b + gcd(x, b)，结果 <= b + b = 2b

关键观察：
- x 从 a 开始不断迭代，如果 x <= b 阶段会不断增长
- 一旦 x >= b，f(x) = b + gcd(x, b) ≤ 2b
- 之后 f(x) 的值会在 [b, 2b] 范围内
- gcd(x, b) 的值只有有限种可能（b 的因子数有限）
- 所以 f 一定会进入循环！

算法：
1. 暴力模拟直到发现循环（用 Set 记录出现过的值）
2. 找到循环起点和周期长度
3. 用 (n - 已迭代次数) % 周期 跳过重复循环

n 可达 10^18，必须找循环，不能暴力迭代 n 次
```

```java
import java.util.*;

public class Main {
    static long gcd(long a, long b) {
        while (b != 0) { long t = b; b = a % b; a = t; }
        return a;
    }

    static long f(long x, long b) {
        return Math.min(x, b) + gcd(x, b);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int T = in.nextInt();
        while (T-- > 0) {
            long a = in.nextLong(), b = in.nextLong(), n = in.nextLong();
            
            // 记录迭代过程中的值序列，检测循环
            List<Long> history = new ArrayList<>();
            Map<Long, Integer> seen = new HashMap<>();
            
            long x = a;
            for (long step = 0; step < n; step++) {
                if (seen.containsKey(x)) {
                    // 发现循环：从 seen[x] 到当前 step
                    int cycleStart = seen.get(x);
                    int cycleLen = (int)(step - cycleStart);
                    long remaining = (n - step) % cycleLen;
                    // 从循环起点再走 remaining 步
                    x = history.get((int)(cycleStart + remaining));
                    break;
                }
                seen.put(x, (int)step);
                history.add(x);
                x = f(x, b);
            }
            
            System.out.println(x);
        }
    }
}
// 时间复杂度：O(b 的因子数)，因为循环长度 ≤ b 的因子数（有限）
// 空间复杂度：O(循环长度)
// 关键：n 很大（10^18），必须检测循环用取模跳过，不能暴力迭代
```

---

> **文档版本**：v2.0
> **内容来源**：面试手撕题 + LeetCode 刷题笔记 + 笔试 OJ 真题 + 回溯/DFS 框架总结
