# Java 基础查漏补缺

这份笔记只整理最近做题时反复暴露出来、容易卡住写题节奏的基础点。

---

## 1. `static` 和对象方法

### `main` 为什么总是 `static`

```java
public static void main(String[] args)
```

这是程序入口，JVM 启动时不先创建对象，直接通过类来调用它。

所以：

- `main` 属于类
- 不是某个对象的方法

---

### `main` 里为什么不能直接调普通方法

如果有：

```java
public static void main(String[] args) {
    helper("abc"); // 不一定能直接调
}

public String helper(String s) {
    return s;
}
```

这里会出问题，因为：

- `main` 是 `static`
- `helper` 是非 `static`

非 `static` 方法属于对象，不属于类。

---

### 两种解决办法

#### 方法 1：把 `helper` 也写成 `static`

```java
public static String helper(String s) {
    return s;
}
```

这样 `main` 里就能直接调。

#### 方法 2：先创建对象，再调

```java
Main t = new Main();
System.out.println(t.helper("abc"));
```

---

### 面试 / ACM 里更省事的做法

很多手撕题里，如果只是工具函数，通常直接写成：

```java
public static ...
```

这样最省事。

---

## 2. `Scanner` 的常用输入方式

```java
Scanner in = new Scanner(System.in);
```

### `System.in` 是什么

- 标准输入
- 平台或终端把输入喂给程序
- `Scanner` 从里面读数据

---

### 常见方法

#### `nextInt()`

- 读一个整数

```java
int x = in.nextInt();
```

#### `next()`

- 读一个字符串 token
- 遇到空格就断开

例如输入：

```text
hello world
```

第一次 `next()` 只会读到：

```text
hello
```

#### `nextLine()`

- 读整行
- 包括空格

例如输入：

```text
aA 1$ b
```

`nextLine()` 会读到整串：

```java
"aA 1$ b"
```

---

### 常见配套判断

#### `hasNextInt()`

- 看看还有没有下一个整数

#### `hasNextLine()`

- 看看还有没有下一行

---

### 最重要的区别

- `hasNextLine()`：只是判断“还有吗”，返回 `boolean`
- `nextLine()`：真正把这一行读出来，返回 `String`

一句话：

> `hasNextLine()` 负责问“还有没有”，`nextLine()` 负责拿“这一行”。

---

## 3. `String` 和 `char`

### 字符串长度

```java
s.length()
```

表示字符串总长度，空格也算字符。

---

### 取第 `i` 个字符

```java
char c = s.charAt(i);
```

返回的是：

- `char`

不是：

- `String`

---

### 遍历字符串最常用骨架

```java
for (int i = 0; i < s.length(); i++) {
    char c = s.charAt(i);
}
```

---

## 4. `String` 不可变

这是一个非常重要的点。

### 错误直觉

很多时候会以为：

```java
String s = "abc";
s.concat("d");
```

这样会把 `s` 变成 `"abcd"`。

其实不会。

---

### 为什么

`String` 是不可变对象。

也就是说：

- 原字符串不会被原地修改
- 会生成一个新字符串

例如：

```java
String s = "abc";
s.concat("d");
System.out.println(s); // 还是 abc
```

---

### 如果要真的改结果，要接住新值

```java
String s = "abc";
s = s + "d";
System.out.println(s); // abcd
```

或者：

```java
s = s.concat("d");
```

---

### 一句话记忆

> `String` 不会原地改，必须用新字符串接住结果。

---

## 5. 判断字符类型：`Character`

做字符串题时很常用。

---

### 判断是不是字母

```java
Character.isLetter(c)
```

---

### 判断是不是数字

```java
Character.isDigit(c)
```

---

### 判断是不是空白字符

```java
Character.isWhitespace(c)
```

注意：

它不只是空格 `' '`，还可能包括：

- `\t`
- `\n`
- 其他空白字符

---

### 如果题目明确只统计“空格”

更精确的写法是：

```java
c == ' '
```

不要误以为：

```java
Character.isWhitespace(c)
```

只会统计普通空格。

---

## 6. 字符数字转整数：`c - '0'`

例如：

```java
char c = '7';
int x = c - '0'; // 7
```

这不是“去掉引号”，而是：

- 字符 `'7'`
- 转成整数 `7`

常用于：

- 数字字符转下标
- 字符数字转真正数值

例如：

```java
String[] mapping = {"", "", "abc", "def", ...};
String letters = mapping[c - '0'];
```

---

## 7. `==` 和 `equals`

### 对对象来说，`==` 比较什么

- 比较是不是同一个引用
- 不是比较内容

---

### `equals` 比较什么

- 比较内容

对于字符串，平时比较值是否相同，优先用：

```java
a.equals(b)
```

或者更稳一点：

```java
Objects.equals(a, b)
```

---

### 字符串字面量为什么有时 `==` 也是 true

例如：

```java
String a = "hello";
String b = "hello";
System.out.println(a == b); // 常常是 true
```

这是因为字符串字面量通常在常量池里复用。

但不要因此养成用 `==` 比字符串内容的习惯。

一句话：

> 比字符串内容，用 `equals`；`==` 只是比是不是同一个对象。

---

## 8. `List` 和 `ArrayList`

### `List` 是什么

- 接口

### `ArrayList` 是什么

- `List` 的一种实现类

---

### 常见写法

```java
List<Integer> list = new ArrayList<>();
```

意思是：

- 左边按接口来声明
- 右边用具体实现类来创建对象

---

### 为什么方法参数通常写 `List`，不写 `ArrayList`

因为方法如果只需要“列表的通用能力”，就不应该写死成某一种实现。

更推荐：

```java
void helper(List<Integer> path)
```

不推荐：

```java
void helper(ArrayList<Integer> path)
```

---

### 为什么会报这种错

```text
Required type:
ArrayList<Integer>
Provided:
List<Integer>
```

因为：

- 方法参数要求的是具体实现类 `ArrayList`
- 但你传的是声明成接口类型的变量 `List`

解决思路：

- 参数也改成 `List<Integer>`

一句话：

> 变量和参数优先写接口 `List`，不是具体实现类 `ArrayList`。

---

## 9. `if` / `else if` / `else` 的互斥分类

统计字符类别时，很适合写成：

```java
if (Character.isLetter(c)) {
    chars++;
} else if (Character.isDigit(c)) {
    digits++;
} else if (c == ' ') {
    whitespace++;
} else {
    others++;
}
```

这样一个字符只会被分到一种类别。

---

### 常见错误

```java
if (...) chars++;
if (...) digits++;
if (...) whitespace++;
else others++;
```

这里最后的 `else` 只会和最后一个 `if` 配对，导致：

- 字母也可能被算到 `others`
- 数字也可能被算到 `others`

所以如果要做“互斥分类”，优先用：

```java
if / else if / else
```

---

## 10. 函数设计：先统计，后拼输出

如果题目要求输出：

```text
chars:3
digits:2
whitespace:1
others:4
```

更推荐的做法是：

1. 先定义四个 `int`
2. 遍历字符串，分别统计
3. 循环结束后统一拼结果字符串

不推荐：

- 一边遍历一边拼结果字符串

因为会更乱，也更容易写错。

一句话：

> 先统计，后格式化。

---

## 11. ACM / 面试手撕常见思路

### 主函数做什么

- 读输入
- 调 `helper`
- 打印输出

### `helper` 做什么

- 处理具体逻辑
- 返回结果

---

### 典型结构

```java
public static void main(String[] args) {
    Scanner in = new Scanner(System.in);
    while (in.hasNextLine()) {
        String s = in.nextLine();
        System.out.println(helper(s));
    }
}
```

---

## 12. 最近最容易混淆的点速记

### `hasNextLine()` vs `nextLine()`

- `hasNextLine()`：判断
- `nextLine()`：读取

### `String` vs `char`

- `String`：字符串
- `char`：单个字符

### `res.charAt(i)` vs `string.charAt(i)`

遍历输入串时，应该看：

```java
string.charAt(i)
```

不是看结果字符串 `res`。

### `String.concat(...)`

- 不会改原字符串
- 要接住返回值

### `==` vs `equals`

- `==`：比引用
- `equals`：比内容

### `List` vs `ArrayList`

- 参数优先写 `List`

---

## 13. 最后一句总结

你最近暴露出来的薄弱点，不是“不会编程”，而是：

- Java API 生疏
- 模板写法忘了
- 一写题就把“判断”“读取”“遍历”“分类”“格式化”搅在一起

以后做这类题时，强制按这 4 步拆：

1. 输入怎么读
2. 每个字符怎么遍历
3. 每个字符怎么分类
4. 最后怎么输出

这样会顺很多。
