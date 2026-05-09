# 🚀 Java 后端开发高频面试题汇总

---

## 一、 Java 基础与面向对象

### 1. Java 有哪几种基础数据类型？其中哪些有对应的包装类？

Java 有 8 种基础数据类型：

- **整数型**：byte (1字节)、short (2字节)、int (4字节)、long (8字节)。
- **浮点型**：float (4字节)、double (8字节)。
- **字符型**：char (2字节)。
- **布尔型**：boolean (1位，但在内存中通常按字节分配)。

所有基础数据类型都有对应的包装类（如 Integer、Double、Character、Boolean 等），用于支持面向对象特性和泛型。

### 2. Integer a = 1 存放在内存的哪部分？Integer a = 1 和 Integer a = 100 有什么区别吗？

局部变量引用 a 存放在**虚拟机栈**中，而对象实例存放在**堆内存**中。

**底层原理与避坑**：Java 为了优化性能，在 Integer 类加载时预先缓存了 [-128, 127] 之间的对象。

- `Integer a = 1` 触发自动装箱调用 `Integer.valueOf(1)`，直接从缓存池 IntegerCache 中返回预先创建好的对象。
- 如果使用 `Integer a = 200`，超出缓存范围，底层会 `new Integer(200)`，在堆中分配新对象。

```java
// Integer.valueOf 底层源码
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)]; // 命中缓存
    return new Integer(i); // 避坑：超范围则每次 new 新对象，== 比较会失效，必须用 equals
}
```

### 3. 面向对象的三大特性是什么？请分别介绍一下。

1. **封装**：将类的状态信息（属性）隐藏在内部，不允许外部程序直接访问，而是通过该类提供的方法（getter/setter）来实现对隐藏信息的操作和访问。
2. **继承**：子类继承父类的特征和行为，使得子类对象具有父类的实例域和方法。极大地提高了代码复用性。
3. **多态**：同一个行为具有多个不同表现形式。表现为父类引用指向子类对象，在运行时根据具体对象的类型执行不同的方法实现。

### 4. 继承和实现接口的两个最大区别是什么？它们有什么共同点？

- **区别**：1. 类只支持**单继承**（避免多继承带来的菱形继承问题），但可以**多实现**（实现多个接口）；2. 语义不同，继承表示强耦合的 "is-a"（是一种）关系，接口表示解耦的 "has-a" 或 "can-do"（具备某能力）契约关系。
- **共同点**：都能作为多态的基础；都不能被直接实例化（抽象类/接口）。

### 5. 介绍一下 Java 8 接口中的 default 方法。

Java 8 引入 `default` 关键字允许在接口中编写具有默认实现的方法。

**底层意图**：主要是为了**向后兼容**。例如 Java 8 要在 List 接口中增加 `sort()` 方法，如果不使用默认方法，所有实现 List 的旧代码都会编译报错。有了 default，实现类可以直接继承该默认逻辑而不必强制重写。

### 6. 多态的底层实现原理是什么？虚拟机是怎么运行多态这个过程的？

多态的底层核心机制是**虚方法表 (Virtual Method Table, vtable)**。

当类被 JVM 加载时，会在方法区为每个类生成一张虚方法表。表中记录了该类各个方法的实际内存入口地址。如果子类重写了父类的方法，子类虚方法表中的指针就会指向子类自己重写后的方法地址；如果没有重写，则指向父类的方法地址。运行时，JVM 遇到 `invokevirtual` 字节码指令，会提取对象的实际类型，查这张表并执行对应地址的代码。

### 7. 什么是动态分派？

动态分派是在**运行期**根据对象的实际类型（Receiver）来确定方法执行版本的分配过程。Java 中的方法重写（Override）就是依靠动态分派实现的，这也是多态的具体体现。与之对应的是静态分派（编译期确定），如方法重载（Overload）。

### 8. 介绍一下 static 和 final 关键字。

- **static**：表示"全局共享"。修饰变量（类加载时分配在方法区）、修饰方法（无需实例即可调用）、修饰代码块（类加载时执行一次）。静态方法内部不能使用 `this` 关键字。
- **final**：表示"不可改变"。修饰类（不能被继承，如 String）、修饰方法（不能被子类重写，防止继承体系破坏核心逻辑）、修饰变量（必须初始化且只能赋值一次，即常量）。

### 9. 什么是泛型？什么是泛型擦除？

泛型（Generics）是参数化类型机制，允许在编译时进行严格的类型检查。**泛型擦除**是指在编译期间，所有的泛型信息（如 `<String>`）都会被编译器抹除，替换为原始类型（通常是 Object，如果定义了 `<T extends Number>` 则擦除为 Number）。

### 10. 泛型擦除之后，虚拟机是怎么运行的？

JVM 运行时其实不知道泛型的存在，它处理的都是 Object。

**底层原理**：编译器在擦除泛型后，会在调用的字节码处自动插入 `checkcast` 指令（强制类型转换）。同时，为了解决泛型擦除导致的子类重写多态失效问题，编译器会自动在子类中生成**桥接方法 (Bridge Method)**。

### 11. 介绍一下 Java 的异常体系。

顶层父类是 `java.lang.Throwable`，分为两派：

1. **Error**：JVM 内部的严重系统错误，如 `OutOfMemoryError`，应用程序不应捕获，也无力恢复。
2. **Exception**：程序可处理的异常。细分为：
   - **受检异常 (Checked Exception)**：如 `IOException`，编译器强制要求 try-catch 或 throws 声明，否则编译不通过。
   - **非受检异常 (RuntimeException)**：如 `NullPointerException`、`IndexOutOfBoundsException`，编译期不检查，通常由代码逻辑错误引起。

### 12. 你知道的 Error 有哪些类型？在运行时会不会出现 Error？

常见的有 `OutOfMemoryError`（堆内存耗尽）、`StackOverflowError`（递归太深导致虚拟机栈满）、MetaspaceOOM（元空间满）。在运行时**会**出现，出现后会导致所在线程直接崩溃，如果不加干预可能导致整个 JVM 进程退出。

### 13. String 类可以被继承吗？String、StringBuilder、StringBuffer 的区别？

String 类被 `final` 修饰，**不可被继承**。

| 对比项 | String | StringBuilder | StringBuffer |
|--------|--------|---------------|-------------|
| 可变性 | **不可变**（每次修改产生新对象） | 可变 | 可变 |
| 线程安全 | 安全（不可变天然安全） | **不安全** | 安全（方法加了 synchronized） |
| 性能 | 频繁拼接最差 | **最快** | 比 StringBuilder 慢 |
| 适用场景 | 常量、少量拼接 | **单线程大量拼接**（最常用） | 多线程大量拼接 |

**避坑**：循环中用 `String +=` 拼接字符串，每次都会 new 一个新的 String 对象，产生大量垃圾。应该用 `StringBuilder.append()`。

### 14. equals 与 == 的区别（附字符串常量池原理）

- **`==`**：对基本类型比较**值**，对引用类型比较**内存地址**（是不是同一个对象）。
- **`equals()`**：比较**内容**（String 重写了 equals，按字符序列逐个比较）。

```java
String a = "hello";       // 字面量，进入字符串常量池
String b = "hello";       // 常量池已有，直接复用同一个对象
String c = new String("hello"); // 显式 new，在堆中创建新对象

System.out.println(a == b);      // true（同一个常量池对象）
System.out.println(a == c);      // false（不同对象）
System.out.println(a.equals(c)); // true（内容相同）
```

**铁律**：比较字符串内容永远用 `equals()`，不要用 `==`。

### 15. 反射的优缺点有哪些？

**反射**是 Java 在运行时动态获取类信息、调用方法、修改属性的能力（通过 `Class` 对象）。

- **优点**：极大提高代码灵活性和扩展性。Spring 的 IoC 容器（反射创建 Bean）、MyBatis 的 ORM 映射、JDK 动态代理全都依赖反射。
- **缺点**：性能比直接调用慢（需要额外的类型检查和安全检查）；破坏封装性（可以访问 private 字段）；编译期无法检查类型安全，运行时才报错。

### 16. 介绍你用过的设计模式。

1. **单例模式**：Spring 中 Bean 默认就是单例。双重检查锁 + volatile 是经典实现。
2. **工厂模式**：Spring 的 `BeanFactory` 就是工厂模式，根据配置动态创建对象。
3. **代理模式**：Spring AOP 底层使用 JDK 动态代理（接口）或 CGLIB（类）生成代理对象。
4. **策略模式**：线程池的拒绝策略 `RejectedExecutionHandler` 就是策略模式的经典应用。
5. **观察者模式**：Spring 的事件机制 `ApplicationEvent` + `ApplicationListener`。
6. **模板方法模式**：`AbstractQueuedSynchronizer`（AQS）定义了获取/释放锁的骨架流程，子类只需实现 `tryAcquire` / `tryRelease`。

### 17. 接口和抽象类的区别

| 对比项 | 接口 (Interface) | 抽象类 (Abstract Class) |
|--------|-----------------|----------------------|
| 多继承 | 可以实现**多个**接口 | 只能继承**一个**抽象类 |
| 方法实现 | Java 8 前只有抽象方法；Java 8 后可以有 `default` 和 `static` 方法 | 可以有抽象方法和普通方法 |
| 成员变量 | 只能有 `public static final` 常量 | 可以有各种类型的成员变量 |
| 构造方法 | 没有构造方法 | 有构造方法（供子类调用） |
| 设计语义 | "**能做什么**"（can-do），定义能力契约 | "**是什么**"（is-a），抽取共性模板 |

**代码感受两者的区别**：

```java
// 接口：定义一种能力（can-do）
// 鸟能飞，飞机也能飞，它们不是同一种东西，但都有"飞"的能力
interface Flyable {
    void fly();
    default void land() { System.out.println("降落"); } // Java 8 默认实现
}

// 抽象类：定义一类事物的公共模板（is-a）
// 猫和狗都是动物，有共同的 eat() 逻辑可以复用
abstract class Animal {
    String name;  // 可以有普通成员变量
    
    public void eat() {
        System.out.println(name + "在吃东西"); // 共性逻辑直接写好
    }
    
    abstract void speak(); // 差异部分让子类实现
}

// 一个类只能继承一个抽象类，但可以实现多个接口
class Bird extends Animal implements Flyable {
    @Override
    void speak() { System.out.println("叽叽喳喳"); }
    
    @Override
    public void fly() { System.out.println("扑棱翅膀飞"); }
}
```

**选型口诀**：多个不相关的类需要同一种能力 → 用接口。多个相关的类有公共代码需要复用 → 用抽象类。

### 18. public、private、protected 的区别

| 修饰符 | 同一个类 | 同一个包 | 子类 | 不同包 |
|--------|---------|---------|------|--------|
| public | ✓ | ✓ | ✓ | ✓ |
| protected | ✓ | ✓ | ✓ | ✗ |
| (default/包访问) | ✓ | ✓ | ✗ | ✗ |
| private | ✓ | ✗ | ✗ | ✗ |

**记忆口诀**：public 全开放，private 全封闭，protected 比 default 多了一个"子类可见"。

```java
public class User {
    public String name;     // 谁都能访问
    protected int age;      // 同包 + 子类可访问
    String city;            // default：只有同包可访问
    private String password; // 只有 User 类内部可访问
}
```

### 19. 什么是方法重载（Overload）？和重写（Override）的区别？

| 对比项 | 重载 (Overload) | 重写 (Override) |
|--------|---------------|----------------|
| 发生位置 | **同一个类**中 | 子类重新定义**父类**的方法 |
| 方法签名 | 方法名相同，**参数列表不同** | 方法名、参数列表、返回类型**完全相同** |
| 决定时机 | **编译期**确定调用版本（静态分派） | **运行期**根据对象实际类型确定（动态分派，多态基础） |
| 返回值 | 可以不同 | 必须相同（或子类型） |

```java
// 重载（Overload）：同一个类，参数不同
public class Calculator {
    public int add(int a, int b) { return a + b; }
    public double add(double a, double b) { return a + b; }  // 参数类型不同 → 重载
    public int add(int a, int b, int c) { return a + b + c; } // 参数个数不同 → 重载
}

// 重写（Override）：子类覆盖父类方法
class Animal {
    public void speak() { System.out.println("..."); }
}
class Dog extends Animal {
    @Override
    public void speak() { System.out.println("汪汪汪"); } // 重写，运行时多态
}

Animal a = new Dog();
a.speak(); // 输出"汪汪汪"（运行时看实际类型 Dog，动态分派）
```

### 20. ArrayList 实现循环遍历有几种方式？

```java
List<String> list = Arrays.asList("A", "B", "C");

// 1. 普通 for 循环（按下标访问，ArrayList 随机访问 O(1)，最友好）
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}

// 2. 增强 for-each（底层是 Iterator，语法糖）
for (String s : list) {
    System.out.println(s);
}

// 3. Iterator 迭代器（唯一支持遍历中安全删除的方式）
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if ("B".equals(s)) it.remove(); // 安全删除
}

// 4. Lambda + forEach（Java 8+）
list.forEach(s -> System.out.println(s));

// 5. Stream 流（Java 8+，支持链式操作 filter/map/collect）
list.stream().filter(s -> !"B".equals(s)).forEach(System.out::println);

// 6. ListIterator（支持双向遍历和修改）
ListIterator<String> lit = list.listIterator();
while (lit.hasNext()) {
    System.out.println(lit.next());
}
```

**避坑**：在遍历中删除元素，只能用方式 3 的 `it.remove()`。使用方式 2 的 for-each 直接 `list.remove()` 会抛 `ConcurrentModificationException`，因为 for-each 底层用 Iterator，Iterator 的 `expectedModCount` 和 List 的 `modCount` 对不上了。

---

## 二、 Java 集合框架

### 1. 你熟悉的数据结构有哪些？

数组（连续内存查询快）、单/双向链表（插入删除快）、栈（LIFO 先进后出）、队列（FIFO 先进先出）、哈希表（基于 Hash 散列，查询 O(1)）、树（如二叉搜索树、红黑树，查询 O(log N)）。

### 2. 请阐述二叉树和红黑树各自的特点。

**二叉搜索树 (BST)**：每个节点最多两个孩子，左子树所有值 < 当前节点 < 右子树所有值。查询、插入平均 O(log N)，但在极端情况下（如按升序插入 1,2,3,4,5）会退化成链表，性能降至 O(N)。

**红黑树 (Red-Black Tree)**：一种**自平衡**的二叉搜索树。通过节点染色（红/黑）和旋转操作，保证了树的高度始终维持在 O(log N)，不会退化。它有 5 条约束规则：

1. 每个节点是红色或黑色。
2. 根节点是黑色。
3. 所有叶子节点（NIL 空节点）是黑色。
4. **红色节点的两个子节点必须是黑色**（即不能出现连续两个红色节点）。
5. **从任一节点到其所有后代叶子节点的路径上，黑色节点数量相同**（黑高一致）。

**实战联系**：Java 中 HashMap 在链表长度超过 8 且数组长度 ≥ 64 时，链表转为红黑树；TreeMap 和 TreeSet 底层也是红黑树。

### 3. 请详细说明红黑树中红色节点的含义及其作用。

红色节点的本质含义是**"这个节点是跟它的父节点合并在一起的"**。

红黑树其实可以等价理解为一棵 **2-3-4 树**（B 树的一种特例）：一个黑色父节点和它下面挂着的红色子节点，在 2-3-4 树里就是合并成了一个大节点。红色节点的存在提供了一种**弹性机制**——它让红黑树在不破坏黑高平衡的前提下，能够容纳"临时多出来"的节点，然后通过旋转和变色来重新达到平衡。

简单来说：黑色节点决定了树的"骨架高度"（黑高一致保证平衡），红色节点是夹在骨架中的"弹性关节"，让插入和删除操作只需要少量的局部旋转和变色就能恢复平衡，而不需要像 AVL 树那样频繁地做严格的高度调整。

### 4. 在现实生活或软件系统中，哪些场景会用到队列（Queue）？

队列的核心特性是 **FIFO（先进先出）**，在需要"排队处理"和"解耦生产者与消费者"的场景中大量使用：

1. **消息队列 / 异步削峰**：秒杀场景下订单请求先进入 Redis Stream / RabbitMQ / Kafka 排队，消费者按顺序消费，避免数据库被瞬时流量击穿。
2. **线程池的任务队列**：`ThreadPoolExecutor` 的 `workQueue` 就是一个 `BlockingQueue`。核心线程忙不过来时，新任务排队等候。
3. **BFS 广度优先搜索**：算法题中层序遍历二叉树、最短路径搜索都使用队列。
4. **操作系统的进程调度**：CPU 就绪队列。
5. **打印机任务队列、食堂排队窗口**：生活中的先来先服务。

### 5. 请结合代码示例，说明 ArrayList 的扩容机制。

ArrayList 底层是一个 `Object[]` 数组。初始容量默认为 **10**（首次 add 时才真正分配）。

**扩容时机**：当 `size + 1 > 当前数组容量` 时触发扩容。

**扩容策略**：新容量 = 旧容量 × 1.5（右移一位实现：`oldCapacity + (oldCapacity >> 1)`），然后用 `Arrays.copyOf()` 把旧数组数据拷贝到新数组。

```java
// ArrayList 核心扩容源码（JDK 8 简化版）
private void grow(int minCapacity) {
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + (oldCapacity >> 1); // 1.5 倍扩容
    if (newCapacity < minCapacity)
        newCapacity = minCapacity;
    elementData = Arrays.copyOf(elementData, newCapacity); // 拷贝到新数组
}
```

**避坑**：扩容是 O(N) 操作（数组拷贝）。如果提前知道要存 1 万条数据，最好在构造时 `new ArrayList<>(10000)` 预设容量，避免反复扩容拷贝的性能浪费。

### 6. 在 Java 中，对 List 进行并发写操作（如在循环中添加元素）时可能出现什么异常？

会抛出 **`ConcurrentModificationException`**（并发修改异常）。

**注意**：此异常**不一定需要多线程**，单线程在用 `for-each` 或 `Iterator` 遍历 List 时，如果在循环体内直接调用 `list.add()` / `list.remove()`，同样会触发。

**底层原因（fail-fast 快速失败机制）**：ArrayList 内部维护了一个 `modCount`（修改计数器），每次 add/remove 都会 `modCount++`。Iterator 在创建时会记住当时的 `expectedModCount = modCount`。每次调用 `iterator.next()` 时，会检查 `modCount != expectedModCount`，如果不一致说明在遍历期间集合被结构性修改了，立即抛异常。

```java
// 触发异常的经典写法
List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
for (String s : list) {
    if ("B".equals(s)) {
        list.remove(s); // 💥 ConcurrentModificationException
    }
}

// ✅ 正确做法 1：使用 Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if ("B".equals(it.next())) {
        it.remove(); // 安全，会同步更新 expectedModCount
    }
}

// ✅ 正确做法 2：使用 CopyOnWriteArrayList（多线程场景）
// ✅ 正确做法 3：使用 removeIf()（Java 8+）
list.removeIf("B"::equals);
```

### 7. HashSet 是怎么保证元素不重复的？

```java
// HashSet 的底层其实只维护了一个 HashMap
private transient HashMap<E,Object> map;

// 所有的 Value 都用这个叫 PRESENT 的空枕头（常量对象）来占位
private static final Object PRESENT = new Object();

// 当你调用 HashSet.add() 时，它自己不干活，直接扔给 HashMap
public boolean add(E e) {
    // 把你要存的元素 e 当作 HashMap 的 Key
    // 利用 HashMap 的 Key 天生不可重复的特性，完美实现去重！
    return map.put(e, PRESENT) == null;
}
```

### 3. HashMap 的 Key 可以为空吗？

结论：可以，但只能有一个。

```java
// HashMap 底层计算 Hash 值的核心源码
static final int hash(Object key) {
    int h;
    // 核心魔法：如果 key 是 null，直接强行返回 0！
    // Key 为 null 的数据，永远固定存在 Node 数组的第 0 个桶里。
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

**对比拓展**：`ConcurrentHashMap` 严禁 Key 或 Value 为 null。

### 4. HashMap 能用自定义对象当 Key 吗？（天坑警告）

结论：可以，但**必须重写 `hashCode()` 和 `equals()` 方法**。

```java
import java.util.HashMap;
import java.util.Objects;

class Order {
    int orderId;
    String name;

    public Order(int orderId, String name) {
        this.orderId = orderId;
        this.name = name;
    }

    // 如果下面这段重写代码被注释掉，必定发生数据丢失！
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId && Objects.equals(name, order.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, name);
    }
}

public class HashMapPitfallDemo {
    public static void main(String[] args) {
        HashMap<Order, String> cache = new HashMap<>();

        // 1. 存入订单 order1
        Order order1 = new Order(1001, "毛肉外卖");
        cache.put(order1, "加麻加辣");

        // 2. 业务流转，前端又传来一个相同内容的订单 order2
        Order order2 = new Order(1001, "毛肉外卖");

        // 3. 尝试获取！
        // 如果 Order 类没有重写那两个方法，这里拿到的 info 是 null！
        String info = cache.get(order2);
        System.out.println("获取到的订单备注：" + info);
    }
}
```

### 8. 详细讲讲 HashMap 的扩容原理。

HashMap 底层是 `Node<K,V>[] table` 数组。默认初始容量 **16**，负载因子 **0.75**。

**扩容时机**：当元素个数 > 容量 × 负载因子（即 16 × 0.75 = 12 个元素）时触发扩容。

**扩容过程**：
1. 创建一个**容量翻倍**（16 → 32）的新数组。
2. 遍历旧数组的每个桶，把每个 Node 重新计算在新数组中的位置（`hash & (newCap - 1)`）。
3. JDK 8 优化：扩容后节点要么在**原位置**，要么在**原位置 + 旧容量**的位置（利用高位 bit 判断），避免了 JDK 7 中的链表反转导致多线程下的死循环问题。

**避坑**：如果提前知道要存大量数据，应在构造时指定初始容量 `new HashMap<>(expectedSize / 0.75 + 1)`，避免反复扩容的性能浪费。

### 9. HashMap 和 ConcurrentHashMap 实现区别？

| 对比维度 | HashMap | ConcurrentHashMap |
|---------|---------|-------------------|
| 线程安全 | 不安全 | 线程安全 |
| 底层结构 | 数组 + 链表 + 红黑树 | 数组 + 链表 + 红黑树 |
| JDK7锁机制 | 无锁 | 分段锁（Segment，16个） |
| JDK8锁机制 | 无锁 | CAS + synchronized（锁桶） |
| null支持 | key和value都可为null | 都不允许null |
| 扩容 | 单线程扩容 | 多线程协助扩容 |

**JDK8 ConcurrentHashMap 核心改进**：取消 Segment 分段锁，降低锁粒度。put 操作时，如果桶位置为空，直接使用 CAS 无锁插入头节点；只有当发生哈希冲突时，才会 `synchronized` 仅锁住该桶的头节点。锁粒度细化到了极致，只要多个线程不向同一个桶里塞数据，就可以完全并行。

### 6. CopyOnWriteArrayList 的底层实现原理是什么？底层一般用的是什么锁？

它的原理是**写时复制**。底层使用 `ReentrantLock` 保证并发写安全。

内部数组被 `volatile` 修饰，保证读线程立刻看到最新数组。读操作完全无锁，性能极高。写操作时，先加锁，拷贝出一个长度 +1 的新数组，在新数组上修改，修改完后将引用指向新数组，最后释放锁。

**避坑**：如果频繁执行写操作，会引发高频的数组拷贝和堆内存垃圾产生，导致频繁 GC（甚至 Full GC），因此它**只适合读多写少**的场景。

```java
private transient volatile Object[] array; // volatile 保证可见性

public boolean add(E e) {
    final ReentrantLock lock = this.lock;
    lock.lock(); // 独占锁保证只有一个线程能拷贝
    try {
        Object[] elements = getArray();
        int len = elements.length;
        Object[] newElements = Arrays.copyOf(elements, len + 1); // 写时复制坑点：产生大量短期对象
        newElements[len] = e;
        setArray(newElements);
        return true;
    } finally {
        lock.unlock();
    }
}
```

---

## 三、 多线程与并发编程 (JUC)

### 1. 什么是多线程？多线程有哪几种实现方式？

多线程是操作系统调度的最小单元，能在同一进程内并发执行多个任务。实现方式有 4 种：

1. 继承 Thread 类，重写 `run()`。
2. 实现 Runnable 接口（最常用，避免单继承限制）。
3. 实现 Callable 接口，配合 FutureTask 使用（支持获取返回值和抛出受检异常）。
4. 使用 ExecutorService 线程池（实战中唯一推荐的方式，避免频繁创建销毁线程的开销）。

### 2. 进程与线程的物理隔离

- **进程（工厂）**：系统分配资源的基本单位。进程崩溃互不影响。
- **线程（工人）**：CPU 执行的基本单位。线程共享进程的堆内存，但拥有私有的虚拟机栈。

### 3. Runnable 与 Callable 的核心差异

- **Runnable（跑腿小弟）**：`void run()`。**无返回值**，且无法向外抛出受检异常。
- **Callable（商务代表）**：`V call()`。**有返回值**（需配合 FutureTask 或线程池获取），且允许向外抛出异常。

```java
import java.util.concurrent.*;

public class ThreadDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        // 1. 派【跑腿小弟】Runnable 去干活
        Runnable errandBoy = () -> {
            try {
                System.out.println("跑腿小弟：我只管去送文件，不带回结果...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        threadPool.submit(errandBoy);

        // 2. 派【商务代表】Callable 去谈合同
        Callable<String> businessRep = () -> {
            System.out.println("商务代表：我去谈千万合同了...");
            Thread.sleep(2000);
            return "【千万级合同书】";
        };
        Future<String> futureResult = threadPool.submit(businessRep);
        System.out.println("老板：代表去谈合同了，我先喝口茶...");
        String contract = futureResult.get(); // 阻塞死等
        System.out.println("老板拿到结果：" + contract);
        threadPool.shutdown();
    }
}
```

### 4. 线程有几种状态？

`java.lang.Thread.State` 定义了 6 种状态：NEW (新建)、RUNNABLE (可运行)、BLOCKED (阻塞，专指等锁)、WAITING (无限期等待)、TIMED_WAITING (限期等待)、TERMINATED (终止)。

在操作系统层面，Java 的 RUNNABLE 状态实际上涵盖了操作系统的**就绪态 (Ready)** 和**运行态 (Running)**。

### 5. 介绍一下 wait() 和 notify() 方法。

这俩是 Object 的底层 native 方法，用于线程间的等待/通知机制。调用 `wait()` 时，当前线程必须已持有该对象的 Monitor 锁。调用后，当前线程立刻**释放锁**，并进入该 Monitor 的等待队列（WaitSet）挂起。直到另一个线程获取同一把锁，调用 `notify()` 或 `notifyAll()`，被挂起的线程才会被移入同步队列（EntryList）重新竞争锁。

### 6. 线程的哪个状态是不释放锁的？

线程调用 `Thread.sleep()` 进入 TIMED_WAITING 状态时不会释放锁。此外，线程在尝试获取 synchronized 锁失败进入 BLOCKED 状态时，它原先已持有的其他锁也不会被释放（这也是产生死锁的温床）。

### 7. Java 都有哪些锁？synchronized 是怎么实现的？

锁分类有：乐观/悲观锁、公平/非公平锁、可重入锁、自旋锁、独占/共享锁等。

`synchronized` 属于悲观锁、非公平锁、可重入锁。它可以修饰代码块、实例方法和静态方法。

**底层原理**：编译后，会在代码块前后插入 `monitorenter` 和 `monitorexit` 指令。每个对象头中都有一个 Mark Word，指向一个 ObjectMonitor（C++实现）。线程执行到 monitorenter 时，会尝试将 Monitor 的 `_owner` 字段设置为自己。JDK 1.6 引入锁升级机制：无锁 → 偏向锁（记录线程ID） → 轻量级锁（CAS自旋） → 重量级锁（OS互斥量）。

### 8. 如何保证多个线程安全地访问同一资源？联系 JMM 说明为何多个线程能获取到"旧"资源值。

**线程不安全的根本原因**：Java 内存模型（JMM）规定，每个线程都有自己的**工作内存**（可以理解为 CPU 缓存），它是主内存（堆）中共享变量的副本。线程对变量的读写操作都在工作内存中进行，**不直接操作主内存**。

这就导致了三大问题：

1. **可见性问题**：线程 A 修改了变量 x，写回了主内存，但线程 B 的工作内存里还是 x 的旧值（还没来得及从主内存刷新）。
2. **原子性问题**：`i++` 实际上是三步操作（读取 → 加1 → 写回），多线程交叉执行可能导致结果丢失。
3. **有序性问题**：JVM 和 CPU 为了性能优化会对指令进行**重排序**，在单线程中语义不变，但多线程下可能产生错误。

**解决方案**：

| 问题 | 解决手段 |
|------|----------|
| 可见性 | `volatile`（强制每次从主内存读/写）、`synchronized`、`Lock` |
| 原子性 | `synchronized`、`Lock`、`Atomic` 原子类 (CAS) |
| 有序性 | `volatile`（禁止指令重排序）、`synchronized`（同一时刻只有一个线程执行） |

```java
// volatile 的经典用法：双重检查锁的单例模式
public class Singleton {
    // 避坑：不加 volatile，可能因指令重排导致其他线程拿到半初始化的对象
    private static volatile Singleton instance;
    
    public static Singleton getInstance() {
        if (instance == null) {                    // 第一次检查（无锁，快速路径）
            synchronized (Singleton.class) {
                if (instance == null) {            // 第二次检查（持锁，防止重复创建）
                    instance = new Singleton();    // 可能重排序：1.分配内存 2.初始化 3.赋引用
                }                                  // volatile 保证 2 在 3 之前完成
            }
        }
        return instance;
    }
}
```

### 9. 高并发状态下，通常应该怎么加锁？

核心思路是**降低锁粒度和锁竞争时间**：

1. **无锁化**：使用 LongAdder、AtomicInteger (底层 CAS+volatile)。
2. **分段锁**：如 JDK 1.7 的 ConcurrentHashMap（Segment分段锁）或 JDK 1.8 的锁桶首节点。
3. **读写分离锁**：使用 ReentrantReadWriteLock，读读不互斥，大幅提升读多写少场景的吞吐量。
4. **缩短锁内代码**：synchronized 只包围真正存在竞态条件的关键代码，不要锁住耗时的网络 IO 或数据库操作。

### 9. synchronized 和 ReentrantLock 的异同

- **出生维度**：`synchronized` 是 JVM 层面的关键字（全自动）；`ReentrantLock` 是 API 层面的类（纯手动）。
- **公平性与唤醒**：`synchronized` 只能非公平，只能盲目唤醒。`ReentrantLock` 可公平可非公平，支持绑定多个 Condition 精准唤醒。
- **🚨 释放机制铁律**：`synchronized` 自动释放；**ReentrantLock 必须在 `finally` 块中纯手动调用 `unlock()`**。

```java
import java.util.concurrent.locks.ReentrantLock;

public class LockDemo {
    private int balance = 1000;
    private final ReentrantLock manualLock = new ReentrantLock();

    // 方式一：synchronized (全自动挡)
    public synchronized void syncWithdraw(int amount) {
        if (balance >= amount) {
            balance -= amount;
            System.out.println("全自动取钱成功，余额：" + balance);
        }
        // 方法结束，系统【自动释放锁】
    }

    // 方式二：ReentrantLock (纯手动挡)
    public void reentrantWithdraw(int amount) {
        manualLock.lock();
        try {
            if (balance >= amount) {
                balance -= amount;
                System.out.println("纯手动取钱成功，余额：" + balance);
            }
        } finally {
            manualLock.unlock(); // 铁律：必须在 finally 中纯手动解锁！
        }
    }
}
```

### 10. ReentrantLock 的原理和核心机制是什么？

ReentrantLock 是基于 API 层面实现的独占、可重入悲观锁。它的核心机制完全依赖内部组件 AQS。它支持公平锁（按排队顺序拿锁）和非公平锁（先通过 CAS 抢一把试试，抢不到再去排队，性能更高，是默认策略）。

### 11. 什么是 AQS？

AQS (AbstractQueuedSynchronizer) 是 Java 并发包的基石。

**底层原理与核心结构**：它维护了一个 `volatile int state`（代表同步资源）和一个基于双向链表的 CHL 等待队列。

当线程尝试抢锁时，会调用 `tryAcquire()`，底层使用 Unsafe 类的 CAS 操作（`compareAndSetState`）尝试将 state 从 0 改为 1。如果成功，则抢锁成功并将锁的持有者设为自己。如果失败，AQS 会将当前线程包装成一个 Node 节点，加入双向队列的尾部，并调用 `LockSupport.park()` 将线程挂起。释放锁时，调用 `tryRelease()` 将 state 减 1，归零则唤醒队列中头节点的下一个节点。

```java
// AQS 核心底层代码
private volatile int state; // volatile 保证多线程可见性

protected final boolean compareAndSetState(int expect, int update) {
    // 利用底层 CPU 硬件指令 CMPXCHG 保证抢锁时的原子性
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```

### 12. CAS (Compare And Swap) 原理与 ABA 漏洞

- **原理**：一种无锁的乐观锁机制。修改数据时，对比当前内存值与预期的旧值，若一致才进行修改更新；若不一致则自旋重试。
- **🔥 ABA 漏洞**：线程 1 准备修改 A 为 B，期间线程 2 将 A 改为 C 又改回 A。线程 1 醒来比较发现还是 A，误以为无人修改。
- **终极解法**：使用 `AtomicStampedReference` 增加版本号。

```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

public class ABADemo {
    public static void main(String[] args) {
        // 1. 灾难现场：普通的 AtomicInteger 引发 ABA 问题
        AtomicInteger balance = new AtomicInteger(100);
        new Thread(() -> {
            balance.compareAndSet(100, 150);
            balance.compareAndSet(150, 100);
        }, "小偷线程").start();
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            boolean success = balance.compareAndSet(100, 50);
            System.out.println("普通CAS扣款结果：" + success + "，当前余额：" + balance.get());
        }, "提现线程").start();

        // 2. 满分解法：AtomicStampedReference
        AtomicStampedReference<Integer> safeBalance = new AtomicStampedReference<>(100, 1);
        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            safeBalance.compareAndSet(100, 150,
                safeBalance.getStamp(), safeBalance.getStamp() + 1);
            safeBalance.compareAndSet(150, 100,
                safeBalance.getStamp(), safeBalance.getStamp() + 1);
        }, "安全的小偷线程").start();
        new Thread(() -> {
            int stamp = safeBalance.getStamp(); // 拿到版本号 1
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            boolean success = safeBalance.compareAndSet(100, 50, stamp, stamp + 1);
            System.out.println("加版本号CAS扣款结果：" + success
                + "，当前版本号：" + safeBalance.getStamp());
        }, "安全的提现线程").start();
    }
}
```

### 13. 高频手撕代码：死锁案发现场

```java
public class DeadLockDemo {
    private static final Object key = new Object();
    private static final Object codebook = new Object();

    public static void main(String[] args) {
        Thread threadA = new Thread(() -> {
            synchronized (key) {
                System.out.println("玩家A：我拿到了【钥匙】，准备去抢密码本...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (codebook) {
                    System.out.println("玩家A：拿到了密码本，成功开箱！");
                }
            }
        });
        Thread threadB = new Thread(() -> {
            synchronized (codebook) {
                System.out.println("玩家B：我拿到了【密码本】，准备去抢钥匙...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (key) {
                    System.out.println("玩家B：拿到了钥匙，成功开箱！");
                }
            }
        });
        threadA.start();
        threadB.start();
        // 运行结果：互相死等，程序永远不会结束！
    }
}
```

### 14. ConcurrentHashMap 的神级锁优化 (JDK 8)

JDK 8 采用 `Node 数组 + CAS + synchronized`。put 逻辑核心：如果桶位置为空，直接使用 CAS 无锁插入头节点。局部加锁：只有当发生哈希冲突时，才会 `synchronized` 仅锁住该桶的头节点。锁粒度细化到了极致。

### 15. 线程池有哪几种类型？

JDK 提供的 Executors 工厂类包含几种预设类型（**阿里巴巴规约禁止直接使用**）：

1. FixedThreadPool（核心线程=最大线程，无界队列：容易 OOM）。
2. CachedThreadPool（核心线程=0，最大线程=无穷大：任务爆发会创建海量线程压垮 CPU）。
3. SingleThreadExecutor（单线程排队执行，无界队列：容易 OOM）。
4. ScheduledThreadPool（定时/周期任务执行器）。

**正确做法**：必须通过 `ThreadPoolExecutor` 的构造函数手动定义参数。

### 16. 线程池的核心参数有哪些？

ThreadPoolExecutor 构造函数有 7 个核心参数：

1. **corePoolSize**：核心线程数（平时养着不回收的常驻工人）。
2. **maximumPoolSize**：最大线程数（忙不过来时最多允许多少工人）。
3. **keepAliveTime**：空闲存活时间（临时工人闲置多久被辞退）。
4. **unit**：存活时间的单位。
5. **workQueue**：任务阻塞队列（活儿太多接不过来时，先放这里排队）。
6. **threadFactory**：线程工厂（用于给线程起个有意义的业务名字，方便排查问题）。
7. **handler**：拒绝策略（队列满了且工人也满编时，怎么拒绝新活儿）。

### 17. 设置线程池大小的时候需要考虑哪些条件？

- **CPU 密集型（如复杂计算、加解密）**：线程过多会导致 CPU 频繁切换上下文反而变慢。配置为 N(cpu) + 1。
- **I/O 密集型（如数据库请求、远程 API 调用）**：线程大部分时间在阻塞等数据，CPU 空闲。公式一般为 N(cpu) × 2，或更精确的：N(cpu) × (1 + WaitTime / ComputeTime)。

### 18. 线程池的淘汰（拒绝）策略有哪些？

1. **AbortPolicy**（默认）：直接抛出 RejectedExecutionException 异常。
2. **CallerRunsPolicy**：不抛异常，直接由提交任务的线程亲自来执行。这会阻塞提交者，起到流量控制作用。
3. **DiscardPolicy**：静默丢弃最新提交的任务，什么也不做。
4. **DiscardOldestPolicy**：丢弃队列中最老的任务，然后尝试把新任务塞进队列。

### 19. 常见的阻塞队列 (workQueue) 有哪些？

- **`ArrayBlockingQueue`**：基于数组的**有界**队列，按 FIFO 排序，创建时必须指定容量。
- **`LinkedBlockingQueue`**：基于链表的有界队列（默认容量为 Integer.MAX_VALUE，近似无界）。FixedThreadPool 和 SingleThreadExecutor 默认使用它（**这就是它们容易 OOM 的根源**）。
- **`SynchronousQueue`**：不存储元素的特殊队列，每个插入操作必须等到另一个线程调用移除操作。CachedThreadPool 使用它。

**线程池代码示例**：

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5,                             // corePoolSize（正式工）
    10,                            // maximumPoolSize（总柜台数）
    60L,                           // keepAliveTime（临时工闲置60秒被辞退）
    TimeUnit.SECONDS,              // unit
    new LinkedBlockingQueue<>(100), // workQueue（等候区100把椅子）
    Executors.defaultThreadFactory(),
    new ThreadPoolExecutor.CallerRunsPolicy() // handler（让提交者亲自执行）
);
```

### 21. ThreadLocal 的作用与底层原理

ThreadLocal 为每个线程提供一个**独立的变量副本**，实现线程隔离，避免了多线程共享变量的线程安全问题。

**底层原理**：每个 Thread 对象内部有一个 `ThreadLocalMap`（类似 HashMap，但用线性探测法解决冲突）。调用 `threadLocal.set(value)` 时，以这个 ThreadLocal 实例本身作为 Key，value 作为 Value，存入**当前线程自己的** ThreadLocalMap 中。不同线程各存各的，互不干扰。

```java
// Thread 源码中可以看到：
public class Thread {
    ThreadLocal.ThreadLocalMap threadLocals = null; // 每个线程一份
}

// ThreadLocal.set() 底层：
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = t.threadLocals; // 拿到当前线程自己的 Map
    if (map != null)
        map.set(this, value); // key 是 ThreadLocal 实例本身
    else
        createMap(t, value);
}
```

**典型使用场景**：

```java
// 实战：存储当前登录用户（拦截器 + ThreadLocal 配合）
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user) { tl.set(user); }
    public static UserDTO getUser() { return tl.get(); }
    public static void removeUser() { tl.remove(); } // 必须 remove！
}

// 拦截器中使用：
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        // 校验 Token → 解析出用户信息
        UserDTO user = parseToken(request.getHeader("Authorization"));
        UserHolder.saveUser(user); // 存入当前线程的 ThreadLocal
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, ...) {
        UserHolder.removeUser(); // 请求结束必须清理！
    }
}

// Controller/Service 任何地方直接拿用户，不用层层传参：
UserDTO currentUser = UserHolder.getUser();
```

**避坑（内存泄漏原理）**：ThreadLocalMap 的 Key 是**弱引用**（WeakReference），GC 后 Key 变成 null，但 Value 是**强引用**不会被回收。如果线程池中线程长期存活（线程复用不销毁），这些"Key 为 null 的 Value"就成了永远无法访问但占着内存的僵尸数据。所以**必须在请求结束时调用 `remove()`**。

### 22. 线程池中的核心线程是通过什么具体方式创建的？线程是在 new ThreadPoolExecutor 时创建，还是在提交任务时创建？

**核心答案**：线程是在**提交任务（submit/execute）时懒加载创建**的，不是在 `new ThreadPoolExecutor()` 时创建。

当调用 `execute()` 提交任务时，线程池会通过内部的 `addWorker()` 方法创建一个 **Worker 对象**。Worker 本身实现了 Runnable 接口，内部持有一个 `Thread` 成员变量。这个 Thread 是通过线程池构造参数中指定的 **ThreadFactory**（如 `Executors.defaultThreadFactory()`）调用 `newThread(worker)` 创建出来的。

```java
// ThreadPoolExecutor 内部简化逻辑
private boolean addWorker(Runnable firstTask, boolean core) {
    Worker w = new Worker(firstTask);    // 创建 Worker
    Thread t = w.thread;                  // Worker 构造函数中：this.thread = threadFactory.newThread(this)
    t.start();                            // 启动线程
    workers.add(w);                       // 加入工作线程集合
    return true;
}
```

**补充**：如果想在创建线程池时就预热核心线程，可以调用 `prestartAllCoreThreads()` 方法。

### 20. 请描述向线程池提交任务（submit/execute）后的完整执行流程。

这是一道面试必背的流程题，核心是**三级判断**：

1. **核心线程还有空位吗？** → 当前线程数 < `corePoolSize`，直接创建新的核心线程来执行任务。
2. **核心线程满了，队列还能放吗？** → 核心线程满了，尝试把任务放入 `workQueue` 阻塞队列排队。
3. **队列也满了，还能加临时工吗？** → 队列也满了，尝试创建非核心线程（总线程数 < `maximumPoolSize`）来执行。
4. **临时工也满编了** → 触发**拒绝策略**（四种 handler 之一）。

```
                    ┌─ 核心线程有空位 ──→ 创建核心线程执行
                    │
submit(task) ───→  ├─ 核心满，队列有空 ──→ 放入 workQueue 排队
                    │
                    ├─ 队列满，可加线程 ──→ 创建非核心线程执行
                    │
                    └─ 都满了 ──────────→ 执行拒绝策略
```

**避坑**：当非核心线程空闲时间超过 `keepAliveTime` 后会被销毁回收；但核心线程默认永远存活（除非设置了 `allowCoreThreadTimeOut(true)`）。

---

## 四、 JVM 底层原理

### 1. JVM 内存结构（五大区域）

**一、线程共享区（GC 的主战场）**

- **堆（Heap）——"大仓库"**：JVM 中最大的一块内存。所有的对象实例（如 `new Student()`）以及 JDK 8 之后的静态变量和字符串常量池都存放在这里。是垃圾回收器的核心区域。
- **方法区 / 元空间（Metaspace）——"档案室"**：存储被 JVM 加载的类信息、常量（如 `static final` 修饰的常量）、以及所有方法的字节码指令。Java 8 中，实质实现变为位于直接内存的**元空间**，不再受 JVM 堆大小限制。

**二、线程私有区（不涉及 GC）**

- **虚拟机栈（VM Stack）——"员工办公桌"**：每执行一个方法，就会在栈里压入一个"栈帧"（存放局部变量表、操作数栈等）。方法执行完毕，栈帧出栈，内存自动释放。
- **本地方法栈（Native Method Stack）**：专为 JVM 执行底层的 C/C++（Native）方法服务的。
- **程序计数器（PC Register）——"书签/进度条"**：当前线程所执行的字节码的行号指示器。唯一不会发生 OOM 的区域。

### 2. 类组件的存储与初始化原理解析

以一段经典代码为例，拆解其在 JVM 中的流转：

```java
class Student {
    static final int ClassNum = 7;       // 1. 静态常量
    String sex = "male";                 // 2. 实例变量
    String getSex() { return this.sex; } // 3. 实例方法
    static int getClassNum() { return ClassNum; } // 4. 静态方法
}
```

- **类的图纸与方法逻辑（3和4）**：`Student` 类的结构信息、`getSex()` 和 `getClassNum()` 的字节码指令，在**类加载时**被放入**方法区（元空间）**。
- **静态常量（1）**：`static final` 是编译期常量，存储在**方法区（元空间）的常量池**中，类加载时直接初始化完毕。
- **实例变量（2）**：存储在**堆（Heap）**中。在类加载时它**不存在**，只有在执行 `new Student()` 实例化对象时，才会随着对象的创建而在堆中分配内存并赋值。
- **方法的执行**：无论是调用实例方法还是静态方法，只要方法开始运行，就会在当前线程的**虚拟机栈（VM Stack）**中压入栈帧，方法结束即销毁。

### 3. 静态代码块 static { ... } 深度解析

**执行时机铁律**：在类被 JVM 加载到内存时立刻自动执行，有且仅有一次。

**经典面试题：初始化执行顺序**：记住口诀——**静态老大，普通老二，构造老三**。

```java
class Student {
    static { System.out.println("1. 静态代码块执行"); }
    { System.out.println("2. 普通代码块执行"); }
    public Student() { System.out.println("3. 构造方法执行"); }
}
// 第一次 new Student() 打印顺序：1 -> 2 -> 3
// 第二次 new Student() 打印顺序：2 -> 3（静态块绝对不会再执行）
```

### 3. 如何判断对象是垃圾？

Java 使用：**可达性分析算法**（顺藤摸瓜找葡萄）。

通过一系列称为 GC Roots 的根节点（如栈中的局部变量、方法区中的静态变量等）作为起始点向下搜索。如果一个对象到 GC Roots 没有任何引用链相连（不可达），则视为垃圾。它完美解决了"引用计数法"中循环引用的致命缺陷。

**GC Roots 包括**：虚拟机栈中引用的对象（局部变量）、方法区中静态属性引用的对象、方法区中常量引用的对象、本地方法栈中引用的对象、被 synchronized 持有的对象。

### 4. 三大垃圾回收算法

1. **标记-清除算法 (Mark-Sweep)**：先标记垃圾，然后就地清除。**缺点**：产生大量内存碎片。
2. **标记-复制算法 (Copying)**：内存一分为二，把存活的对象紧凑地搬到另一半，然后瞬间清空当前一半。**优点**：无碎片。**缺点**：空间利用率仅为 50%。
3. **标记-整理算法 (Mark-Compact)**：把存活对象全部推到内存一端，紧密排列，然后清理边界外的垃圾。**优点**：无碎片，不浪费空间。**缺点**：移动对象开销极大。

### 5. 分代收集理论（JVM 的真实做法）

- **新生代（朝生夕死）**：对象存活率极低，采用**标记-复制算法**。JVM 新生代的 Eden 与两个 Survivor (8:1:1) 就是优化的复制算法。
- **老年代（老顽固）**：对象存活率高且很难死，采用**标记-清除** 或 **标记-整理算法**。

**工作流程**：新对象在 Eden 区创建 → Eden 满了触发 Minor GC → 存活对象复制到 Survivor → 对象在 Survivor 区来回复制（From ↔ To） → 存活超过 15 次晋升到老年代 → 老年代满了触发 Full GC。

### 6. 常见的垃圾回收器有哪些？

- **CMS (Concurrent Mark Sweep)**：以最短停顿时间为目标的**老年代**收集器，基于"标记-清除"。并发阶段不阻塞用户线程。
- **G1 (Garbage-First)**：现代 JVM 主流收集器。将堆划分为一个个大小相等的 Region。后台维护一个优先列表，每次根据允许的停顿时间，优先回收价值最大的 Region。
- **ZGC / Shenandoah**：极低延迟收集器（停顿时间不超过 10ms），支持 TB 级堆内存。

### 7. GC 调优

常用 JVM 参数：

```bash
-Xms4g              # 初始堆大小
-Xmx4g              # 最大堆大小（建议与Xms相同，避免动态扩容）
-Xmn2g              # 新生代大小
-XX:SurvivorRatio=8  # Eden:Survivor = 8:2
-XX:+UseG1GC         # 使用G1收集器
-XX:MaxGCPauseMillis=200  # 最大停顿时间200ms
```

GC 调优步骤：监控 GC 情况（`jstat -gc`） → 分析问题（Full GC 频繁？Minor GC 频繁？） → 调整参数 → 压测验证效果。

### 8. JVM 双亲委派模型

**类加载器层级**（从上到下）：

1. **启动类加载器 (Bootstrap ClassLoader)**：加载 `jre/lib` 下的核心类（如 `java.lang.String`），C++ 实现。
2. **扩展类加载器 (Extension ClassLoader)**：加载 `jre/lib/ext` 下的扩展类。
3. **应用类加载器 (Application ClassLoader)**：加载我们自己写的类（classpath 下的类）。

**双亲委派机制**：当一个类加载器收到加载请求时，**先委托给父类加载器去加载**，父类也继续向上委托。只有当父类加载器在自己的搜索范围内找不到该类，子类加载器才自己动手。

**为什么要这么做？** 保证 Java 核心类的安全性和唯一性。即使你自己写了一个 `java.lang.String` 类，双亲委派保证最终加载的一定是 JDK 自带的 String，防止核心 API 被篡改。

---

## 五、 Spring 全家桶

### 1. 什么是 AOP（面向切面编程）？

AOP 就是把那些与核心业务无关、但在整个系统里到处都要用的"通用逻辑"（横切关注点），抽离出来，统一处理。

**实战亮点**：自定义一个注解叫 `@AutoFill`，然后写一个 AOP 切面类，规定：只要哪个方法头上顶着 `@AutoFill`，就在方法执行之前，偷偷把 `update_time` 设为当前时间，`update_user` 设为当前登录人。

**Spring 框架里哪些核心用到了 AOP？**

1. `@Transactional`（声明式事务）：AOP 在方法执行前帮你开启数据库事务，执行完提交；捕捉到异常立刻 `rollback()` 回滚。
2. `@Cacheable`（Spring Cache 缓存）：AOP 在方法执行前先去 Redis 里看有没有数据，有就直接返回。
3. Spring Security（权限控制）：如 `@PreAuthorize("hasRole('ADMIN')")`。

### 2. Spring Boot 自动装配是怎么实现的？

核心在启动类注解 `@SpringBootApplication`，它包含 `@EnableAutoConfiguration`。

**底层源码级流程**：

1. 该注解引入了 `AutoConfigurationImportSelector` 类。
2. 其内部 `selectImports` 方法会使用 SpringFactoriesLoader 去扫描当前类路径下所有依赖 jar 包中 `META-INF/spring.factories` 文件。
3. 提取文件中配置的所有全限定类名。
4. 结合 `@ConditionalOnClass`、`@ConditionalOnMissingBean` 等条件注解进行过滤，只有当项目里引入了相关 jar 包且没有自定义相关 Bean 时，才会把这些配置类的 Bean 注册到 Spring 的 IoC 容器中。

### 3. Spring Boot 启动的时候会加载什么？

1. 初始化 SpringApplication，推断应用类型。
2. 加载系统环境变量、命令行参数和 application.yml/properties 配置文件，封装成 Environment 对象。
3. 创建对应的 ApplicationContext 容器。
4. 执行 `refreshContext`（Spring 核心生命周期，注册 BeanFactoryPostProcessor，扫描 @Component 并实例化 Bean）。
5. 启动内嵌的 Web 服务器（如 Tomcat）。
6. 触发各种 CommandLineRunner 或 ApplicationRunner 回调。

### 4. 详细讲讲 Bean 的生命周期。

Bean 的生命周期由 BeanFactory 精确控制，主要分四大步：

1. **实例化 (Instantiation)**：调用构造方法或工厂方法在内存中开辟空间（此时 Bean 是个只有默认值的空壳）。
2. **属性赋值 (Populate)**：处理 @Autowired 等依赖注入，调用 setter 方法赋值。
3. **初始化 (Initialization)**：执行各种 Aware 接口的回调 → BeanPostProcessor 前置处理 → 自定义初始化方法（@PostConstruct） → BeanPostProcessor 后置处理（**Spring AOP 生成代理对象就是在这里完成的**）。
4. **销毁 (Destruction)**：容器关闭时，调用 @PreDestroy 或 DisposableBean.destroy 清理资源。

**记忆口诀**：**new出来 → 填依赖 → 做准备 → 用完收拾**

### 5. 介绍一下 IoC，它是怎么实现的？

IoC（Inversion of Control，控制反转）是一种设计思想。传统开发中，对象自己内部去 new 它所依赖的其他对象。IoC 将创建对象的控制权转移给了 Spring 容器。

**底层实现**：主要依靠**解析 XML/注解蓝图 + Java 反射机制 + 工厂模式 + 内部 ConcurrentHashMap 缓存**实现对象的动态创建与统一存放。

### 6. 什么是 DI？和 IoC 有没有关系？

DI（Dependency Injection，依赖注入）是 IoC 思想的具体实现手段。IoC 是目标，DI 是手段。容器在创建某个对象时，发现它依赖另一个对象，容器会自动从内部缓存里找到并动态地将其赋值。底层通过 Java 反射调用 setter 方法或带参构造函数。

### 7. Spring 是怎么解决循环依赖的？

针对**单例作用域**的**Setter/属性注入**，Spring 采用**三级缓存**机制解决。

**三级缓存的具体分工**：

1. **一级缓存 (`singletonObjects`)**：**成品库**。存放已经完全实例化、属性赋值完成的最终完整 Bean。
2. **二级缓存 (`earlySingletonObjects`)**：**半成品库**。存放刚实例化完成，但**还没有进行属性注入**的 Bean 引用（提前暴露出来应急用的）。
3. **三级缓存 (`singletonFactories`)**：**工厂库**。存放一个 ObjectFactory 对象工厂，用于生成半成品 Bean 的引用，或者在必要时**提前生成 AOP 代理对象**。

假设 A 依赖 B，B 依赖 A：

1. A 实例化后（刚 new 出来，没赋值），先将一个能获取 A 的匿名工厂函数（ObjectFactory）放入**第三级缓存** `singletonFactories` 中。
2. A 开始属性注入，发现需要 B，于是去实例化 B。
3. B 实例化后注入属性发现需要 A，B 从三级缓存拿到了 A 的 ObjectFactory，调用 `getObject()` 获得 A 的早期引用，并把 A 移入**第二级缓存** `earlySingletonObjects`。
4. B 成功注入 A，B 完成初始化放入一级缓存（单例池）。
5. A 拿到 B 进行注入，最终 A 也完成初始化放入一级缓存。

**避坑**：构造器注入产生的循环依赖 Spring 无法解决（因为实例化都过不去，没法提前暴露）。

**必考点：为什么一定要三级缓存？只用两级不行吗？** 如果项目中没有任何 AOP（切面代理），两级缓存完全够用。但三级缓存的本质是为了**处理 AOP**。通过 ObjectFactory，Spring 可以保证：如果 A 被循环依赖了，且 A 需要被切面代理，那么注入给 B 的 A 的引用，**一定是通过工厂提前生成的代理对象**，而不是原始对象。

```java
// Spring 底层暴露第三级缓存的核心代码
protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
    if (!this.singletonObjects.containsKey(beanName)) {
        this.singletonFactories.put(beanName, singletonFactory); // 放入三级缓存
        this.earlySingletonObjects.remove(beanName);
        this.registeredSingletons.add(beanName);
    }
}
```

### 8. Java 里的事务是怎么用的？

通常使用声明式事务注解 `@Transactional`。

**底层机制**：基于 Spring AOP。Spring 会动态生成代理类，在执行业务方法前执行 `Connection.setAutoCommit(false)` 开启事务。正常执行完毕则 commit()；抛出异常则 rollback()。

**避坑指南**：默认情况下，`@Transactional` **只对非受检异常（RuntimeException 或 Error）回滚**。必须写明 `@Transactional(rollbackFor = Exception.class)` 才能万无一失。如果在同类中非事务方法调用了事务方法，也会因为绕过了 AOP 代理导致事务失效。

### 9. 场景题：如何监控一个对外提供的 HTTP 接口（第三方调用）的返回值，以检查返回结果是否正常？

这是一道综合性的**AOP + 日志 + 健康检查**场景题，有几种思路：

**思路一：AOP 切面统一拦截（最优雅）**

自定义一个切面，`@Around` 环绕通知拦截所有对外接口方法。在方法执行后，检查返回值是否符合预期（如 HTTP 状态码 200、业务 code 为 0），如果异常则记录日志并触发告警（企业微信/钉钉通知）。

**思路二：拦截器 / Filter 层面**

通过 `HandlerInterceptor` 的 `postHandle` 或 `afterCompletion` 方法，统一拦截 Controller 层的返回值进行校验。

**思路三：定时健康探针**

用 `@Scheduled` 定时任务主动发 HTTP 请求调用该接口（类似心跳检测），检查返回值，异常时告警。配合 Spring Boot Actuator 的 `/health` 端点暴露给 Prometheus + Grafana 做可视化监控。

**思路四：日志 + ELK**

在统一返回包装类 `Result<T>` 中记录每次响应日志，通过 ELK（Elasticsearch + Logstash + Kibana）收集日志，设置异常关键词告警规则。

### 10. MyBatis 核心安全：#{} 与 ${} 的区别

- `#{}` **(预编译占位符)**：底层使用 JDBC 的 `PreparedStatement`。先预编译 SQL 语句，再将参数作为纯文本安全注入。**有效防止 SQL 注入攻击**。
- `${}` **(字符串拼接)**：将参数原封不动地直接拼接到 SQL 语句中。**极易引发 SQL 注入漏洞**。仅在动态传入表名或动态指定 ORDER BY 排序字段等无法使用预编译的场景下使用。

### 11. @Autowired 和 @Resource 的区别是什么？

| 对比项 | @Autowired | @Resource |
|--------|-----------|-----------|
| 来源 | Spring 框架 | JDK 标准（JSR-250） |
| 注入方式 | **按类型**匹配。如果同类型有多个 Bean，需配合 `@Qualifier("beanName")` 指定。 | 默认**按名称**匹配（`name` 属性），找不到再按类型匹配。 |
| 支持构造器注入 | 支持 | 不支持 |

**实战选型**：如果项目纯 Spring 生态，用 `@Autowired` 更地道；如果追求与框架解耦（万一换框架），用 `@Resource`。

### 12. 过滤器（Filter）和拦截器（Interceptor）的区别是什么？

| 对比项 | Filter | Interceptor |
|--------|--------|-------------|
| 所属规范 | Servlet 规范（Java EE） | Spring MVC 框架 |
| 作用范围 | 所有请求（包括静态资源） | 只拦截 Controller 方法 |
| 实现方式 | 实现 `javax.servlet.Filter` 接口 | 实现 `HandlerInterceptor` 接口 |
| 能否获取 Spring Bean | 不能直接获取（需特殊处理） | 可以（它本身就在 Spring 容器中） |
| 执行顺序 | Filter **先于** Interceptor 执行 | 在 DispatcherServlet 之后执行 |

**实战**：通用的编码设置、跨域处理用 Filter；登录校验、权限拦截、日志记录用 Interceptor（能拿到 Handler 方法信息）。

### 13. MyBatis 动态 SQL 遇到传入参数都为空，如何处理？

使用 `<where>` 标签替代手写 `WHERE`。`<where>` 标签会自动判断：如果所有内部 `<if>` 条件都不满足，它不会生成 `WHERE` 关键字；如果有条件成立，它还会自动去掉最前面多余的 `AND` 或 `OR`。

```xml
<select id="selectUser" resultType="User">
    SELECT * FROM user
    <where>
        <if test="name != null and name != ''">
            AND name = #{name}
        </if>
        <if test="age != null">
            AND age = #{age}
        </if>
    </where>
</select>
<!-- 如果 name 和 age 都为空，生成的 SQL 是：SELECT * FROM user（没有 WHERE） -->
```

### 14. MyBatis 和 MyBatis-Plus 在使用上有什么区别？

MyBatis-Plus 是 MyBatis 的**增强工具**，在不改变 MyBatis 原有功能的基础上，提供了大量便捷功能：

1. **通用 CRUD 无需写 XML**：继承 `BaseMapper<T>` 后，`selectById`、`insert`、`updateById`、`deleteById` 等方法直接可用。
2. **条件构造器 `LambdaQueryWrapper`**：用链式 API 替代手写 XML 中的 `<if>` 动态 SQL。
3. **自动填充**：配合 `MetaObjectHandler` 实现 `create_time`、`update_time` 的自动填充。
4. **分页插件**：内置 `PaginationInnerInterceptor`，一行配置搞定物理分页。

### 15. Spring Boot 实战规范：RESTful 接口

将网络上的一切视为"资源"。URL 路径中只包含名词（如 `/user`），通过 HTTP 的请求动词（GET/POST/PUT/DELETE）来区分操作。

- 查询（查）：`GET /user/1` → `@GetMapping("/user/{id}")`
- 新增（增）：`POST /user` → `@PostMapping("/user")`
- 修改（改）：`PUT /user` → `@PutMapping("/user")`
- 删除（删）：`DELETE /user/1` → `@DeleteMapping("/user/{id}")`

### 11. Validation 参数校验

```java
public class User {
    @NotBlank(message = "用户名绝对不能是空的！")
    private String username;
    @Length(min = 6, max = 20, message = "密码必须在6-20位之间！")
    private String password;
}

@PostMapping("/register")
public String register(@Validated @RequestBody User user) {
    // 一行 if-else 都不用写！
    userService.register(user);
}
```

### 18. 登录和校验流程（Session vs Token/JWT）

**传统 Session 方案**：

```java
// 服务端登录成功后，创建 Session
HttpSession session = request.getSession();
session.setAttribute("user", userInfo);
// 自动通过 Set-Cookie: JSESSIONID=xxx 返回给浏览器
// 后续请求浏览器自动带 Cookie → 服务端根据 JSESSIONID 查 Session

// 致命问题：集群部署时 Session 不共享
// A 服务器创建的 Session，请求被负载均衡到 B 服务器 → 找不到 → 用户被踢出
```

**现代 Token/JWT 方案**（项目中常用）：

```java
// 1. 登录成功 → 签发 JWT
String token = Jwts.builder()
    .setClaims(Map.of("userId", user.getId()))
    .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1小时过期
    .signWith(SignatureAlgorithm.HS256, secretKey) // 用密钥签名
    .compact();
// 返回给前端，前端存在 localStorage

// 2. 前端后续请求在 Header 中携带：
// Authorization: Bearer eyJhbGciOi...

// 3. 服务端拦截器校验 Token
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        String token = request.getHeader("Authorization").substring(7); // 去掉 "Bearer "
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody(); // 解析出用户信息
        UserHolder.saveUser(claims.get("userId")); // 存入 ThreadLocal
        return true;
    }
}
```

**JWT 的优势**：Token 本身携带用户信息，服务端**无状态**（不需要存 Session），天然支持集群部署。

---

## 六、 MySQL 数据库

### 1. 索引的本质与 B+ 树底层揭秘

- **为什么不用 Hash？** Hash 不支持范围查询（`WHERE age > 18`）。
- **为什么不用普通 B 树？** B 树每个节点都存真实数据，导致树很高。而 **B+ 树只在最底层叶子节点存放真实数据**，非叶子节点全放纯目录。通常 3 层就能存放千万级数据。
- **B+ 树的杀手锏**：所有叶子节点之间用**双向链表**连接！范围查询极其丝滑。

### 2. 聚簇索引 vs 非聚簇索引

- **聚簇索引（主键索引）**：叶子节点包含**完整行数据**。找到索引就找到全部数据。一张表只有一个。
- **非聚簇索引（二级索引）**：叶子节点只存**主键 ID**。找到后还得拿主键 ID 回到聚簇索引再查，这叫**回表**。可以有多个。
- **🚨 如果没有主键**：InnoDB 会找第一个不含 NULL 的唯一索引；都没有就偷偷生成隐藏的 `ROW_ID`。

**如何避免回表？ → 覆盖索引**

```sql
-- 建联合索引：(name, age)
CREATE INDEX idx_name_age ON user(name, age);
SELECT id, name, age FROM user WHERE name = '张三';
-- 索引包含：name、age、id（主键自动包含）→ 不需要回表！
```

### 3. 索引设计 5 大原则

1. WHERE 子句中的字段
2. JOIN 的关联字段
3. ORDER BY / GROUP BY 的字段
4. 区分度高的字段（手机号 ✓，性别 ✗）
5. 覆盖索引（避免回表）

**🚨 为什么不给每个字段都建索引？**

1. **磁盘撑爆**：每棵索引树就是一份物理文件。建 10 个索引，数据量直接膨胀好几倍。
2. **写操作（增删改）极慢**：INSERT 一条数据，不仅要改主键树，还要跑去 age 树、phone 树等所有索引树全部更新一遍。**索引越多，查询越快，但写入越慢！**

### 4. 联合索引的最左前缀匹配与查询优化器重排

联合索引 `(a, b, c)` 是严格按声明顺序构建的排序树（先按 a 排，a 相同按 b 排，b 相同按 c 排）。

**终极避坑口诀**：

1. **缺了左边，绝对不行**：条件里没有最左边的引导列 `a`，无论怎么写，索引直接失效。
2. **顺序乱了，完全没关系**：只要把该带的列都带上了，无论 WHERE 后面先写谁后写谁（如 `c=2 AND a=1 AND b=3`），MySQL 的**查询优化器 (Optimizer)** 都会在底层自动重排成 `a=1 AND b=3 AND c=2`，完美走索引。

### 5. 没有索引时会发生什么？——全表扫描详解

如果你没给 `phone` 建索引，却执行 `SELECT * FROM user WHERE phone = '1111'`：

MySQL 没有 `phone` 的索引树可以查。它唯一的办法就是来到主键树的最底层叶子节点，从第一条数据开始**一行一行往后比对**，直到把千万条数据全部看完。这就是**全表扫描 (Full Table Scan)**，也是导致数据库卡死、CPU 飙升的罪魁祸首。

### 6. 索引失效的案发现场

四大高频失效场景：

1. **左模糊匹配**：`LIKE '%张'`，索引直接废掉。
2. **隐式类型转换**：varchar 字段查询时没加引号。
3. **使用了 OR** 且左右不全有索引。
4. **在索引列上做运算或套函数**：

```sql
-- 灾难写法：在 create_time 索引列上套了 YEAR() 函数
EXPLAIN SELECT * FROM user WHERE YEAR(create_time) = 2026;
-- 满分优化写法：把运算放在等号右边！
EXPLAIN SELECT * FROM user
WHERE create_time >= '2026-01-01' AND create_time < '2027-01-01';
```

**字段是字符串，使用数字查**：**会失效**。MySQL 底层会隐式调用函数把表里的字符串转成数字，索引列上加了函数，B+ 树的有序性被破坏。
**字段是整数，使用字符串查**：**不会失效**。MySQL 会把参数字符串转成数字再去查树，没有动表字段的值。

### 5. 什么是函数索引？(MySQL 8.0 新特性)

```sql
-- 创建函数索引（注意双括号）
CREATE INDEX idx_year_create_time ON user ((YEAR(create_time)));
-- 此时再执行上面的灾难写法，就能完美命中该函数索引！
```

### 6. SQL 场景题：一个表有 a, b, c 等多个字段，三条查询 SQL 条件分别为 a、b、c 的组合。如果是 AND 关系，索引如何设计？如果是 OR 关系呢？

**AND 关系（联合索引）**：

```sql
-- 查询条件
WHERE a = ? AND b = ? AND c = ?
WHERE a = ? AND b = ?
WHERE a = ?
```

设计方案：建一个**联合索引** `idx(a, b, c)` 即可覆盖以上全部查询。核心原则是**最左前缀法则**——联合索引 `(a, b, c)` 可以被 `a`、`(a, b)`、`(a, b, c)` 三种组合命中，但 `b`、`(b, c)` 无法命中。

**字段顺序怎么排？** 区分度高的字段放左边（让 B+ 树尽早缩小范围）；如果区分度差不多，把**等值查询**的字段放在**范围查询**字段的左边（因为范围查询之后的字段无法使用索引）。

**OR 关系（各自建单独索引）**：

```sql
-- 查询条件
WHERE a = ? OR b = ? OR c = ?
```

此时联合索引 `(a, b, c)` **完全无效**（OR 条件下最左前缀法则不适用）。必须给 a、b、c **各自建单独的索引**：

```sql
CREATE INDEX idx_a ON table(a);
CREATE INDEX idx_b ON table(b);
CREATE INDEX idx_c ON table(c);
```

MySQL 5.0+ 的 **Index Merge 优化**会自动将多个单列索引的结果做并集（UNION），但性能通常不如 AND + 联合索引好。如果可能，最好将 OR 改写为 `UNION`：

```sql
SELECT * FROM table WHERE a = ?
UNION
SELECT * FROM table WHERE b = ?
UNION
SELECT * FROM table WHERE c = ?;
```

### 7. 事务隔离级别与锁机制

四大隔离级别：

1. **读未提交（Read Uncommitted）**：能读到别人没提交的数据（脏读）。
2. **读已提交（RC）**：Oracle 默认。每次查询生成新视图。
3. **可重复读（RR）**：**MySQL 默认**。事务开启时拍快照，整个事务期间数据一致。底层基于 MVCC 实现。
4. **串行化（Serializable）**：全部加锁，性能极差。

InnoDB 的行级锁：记录锁 (Record Lock) 精准锁住某一行、间隙锁 (Gap Lock) 防止幻读、临键锁 (Next-Key Lock) = 记录锁 + 间隙锁。

### 7. 什么时候要开启事务？

- **多个操作必须同时成功或失败**（如转账）
- **数据一致性要求高**（如秒杀下单：扣库存 + 创建订单 + 扣余额）
- **保证隔离性**（查询后更新，防止并发问题）

不需要事务的场景：单条查询、单条插入、日志记录（允许失败）。

### 8. 开启事务忘了提交有什么问题？

1. 长时间占用数据库连接 → 连接池耗尽
2. 锁等待超时（默认 50 秒后报 `Lock wait timeout exceeded`）
3. 产生脏读、幻读
4. Undo 日志膨胀占用磁盘空间
5. 死锁风险增加

### 9. MySQL 锁的使用场景

- **悲观锁 `FOR UPDATE`**：写多场景，强一致性（秒杀扣库存、转账）
- **共享锁 `LOCK IN SHARE MODE`**：读取后要确保数据不被修改（订单结算时防商品价格变动）
- **乐观锁（版本号）**：读多写少，避免加锁（用户信息修改）
- **分布式锁（Redis/Redisson）**：分布式系统，防止重复操作

### 10. SQL 中的左右连接有什么区别？

- **LEFT JOIN（左连接）**：以**左表为主**，返回左表所有行。右表没有匹配的，填 NULL。
- **RIGHT JOIN（右连接）**：以**右表为主**，返回右表所有行。左表没有匹配的，填 NULL。
- **INNER JOIN（内连接）**：只返回两表都匹配的行。

```sql
-- 查询所有用户及其订单（即使用户没下过单也要显示）
SELECT u.name, o.order_no
FROM user u
LEFT JOIN `order` o ON u.id = o.user_id;
-- 用户"张三"没有订单 → 结果中张三的 order_no 为 NULL
```

### 11. 慢查询排查与优化"三板斧"

**🔍 第一斧：案发报警** —— 通过配置 MySQL 的**慢查询日志（Slow Query Log）**，设定合理阈值，精准抓出耗时过长的 SQL。

**🔎 第二斧：现场取证 EXPLAIN 分析** —— 重点排查 `type` 字段是否为 `ALL`（全表扫描）、`key` 字段是否为 `NULL`（索引未命中）、`Extra` 字段是否出现 `Using filesort`（耗时排序）或 `Using index`（索引覆盖）。

**🔧 第三斧：对症下药** —— 排查索引失效；避免 `SELECT *` 导致频繁回表，改用覆盖索引。

### 11. 深度分页优化实战

```sql
-- 灾难写法：MySQL 会查出 1000010 条记录，然后扔掉前一百万条
SELECT * FROM orders ORDER BY id LIMIT 1000000, 10;

-- 优化后（子查询延迟关联）
SELECT o.* FROM orders o
INNER JOIN (
    SELECT id FROM orders ORDER BY id LIMIT 1000000, 10
) AS temp ON o.id = temp.id;
```

### 12. 海量数据架构：分库分表

当单表数据量突破 1000 万 ~ 2000 万，增删改查也会变慢。

- **垂直切分**：把"用户表"劈开，基础信息和扩展信息分离。
- **水平切分**：哈希取模算法（数据均匀；缺点是扩容需要迁移）、范围切分算法（扩容简单；缺点是存在热点问题）。

### 13. 开启事务忘了提交有什么问题？

这不是一个理论问题，**实际开发中真的会遇到**。比如手动管理事务忘了 commit，或者 `@Transactional` 方法内调了一个超长的外部 API，事务一直挂着：

```java
// 灾难场景：事务内调外部 API
@Transactional
public void processOrder(Long orderId) {
    orderMapper.updateStatus(orderId, "PROCESSING"); // 加了行锁
    
    // 调用第三方物流 API，超时 30 秒...
    logisticsService.callExternalAPI(orderId); // 这 30 秒内行锁一直不放！
    
    orderMapper.updateStatus(orderId, "SHIPPED");
}
// 别的线程查同一行 → Lock wait timeout exceeded
```

**5 大问题**：
1. **长时间占用连接** → 连接池耗尽（默认 10 个连接，占满了新请求全部超时）
2. **锁等待超时** → 其他事务操作同一行数据被阻塞 50 秒后报错 `Lock wait timeout exceeded`
3. **Undo 日志膨胀** → 事务不提交 Undo 日志无法清理，磁盘越积越大
4. **MVCC 快照老化** → 其他事务的一致性读要回溯很久远的版本
5. **死锁风险增加** → 事务持有锁的时间越长，和其他事务形成循环等待的概率越大

**正确做法**：缩小事务范围，把耗时的 IO 操作（调外部 API、发邮件）**移到事务外面**。

### 14. 乐观锁版本号冲突怎么处理？

**版本号冲突 ≠ 失败**，而是一种正常的并发信号。关键是怎么处理：

```java
// 方案1：自动重试（最常用）
public void updateWithRetry(User user, int maxRetry) {
    for (int i = 0; i < maxRetry; i++) {
        // 每次重试前重新查最新数据和版本号
        User latest = userMapper.selectById(user.getId());
        latest.setAge(user.getAge());
        
        int rows = userMapper.updateWithVersion(latest);
        // UPDATE user SET age=#{age}, version=version+1 WHERE id=#{id} AND version=#{version}
        
        if (rows > 0) return; // 成功
        // rows == 0 说明版本号不匹配（别人先改了），继续重试
    }
    throw new RuntimeException("更新失败，请刷新后重试");
}

// 方案2：提示用户（适合编辑页面场景）
// 前端显示"数据已被他人修改，请刷新页面后重试"

// 方案3：降级为悲观锁（冲突特别激烈时）
// 先乐观尝试 3 次，都失败了就改用 SELECT ... FOR UPDATE
```

### 15. 讲一下数据库的 undo log、redo log、binlog

- **undo log (回滚日志)**：记录修改前的数据状态。核心作用是事务回滚和实现 MVCC。
- **redo log (重做日志)**：InnoDB 特有的物理日志。核心作用是 **Crash-safe 掉电恢复**。
- **binlog (归档日志)**：Server 层逻辑日志。核心作用是主从复制和数据备份恢复。
- **执行顺序**：**先写 undo log** → 内存中修改数据页产生 redo log → 提交时采用**两阶段提交**：先写 redo log (prepare) → 写 binlog → 提交 redo log (commit)。

---

## 七、 Redis 与分布式中间件

### 1. 访问 Redis 为什么快？

1. **完全基于内存**：数据读写在纳秒级别。
2. **高效的底层数据结构**：全局 Hash 表 O(1)、SDS 动态字符串、跳表 SkipList。
3. **单线程模型**：核心业务由单线程串行处理，避免了多线程抢锁和 CPU 上下文切换的开销。
4. **I/O 多路复用**：底层使用 epoll/kqueue，一个单线程就能并发监听成千上万个 Socket 连接。

### 2. Redis 有哪些数据类型？Zset 的底层数据结构是什么？

- 基本类型：String、List、Hash、Set、Zset (Sorted Set)。高级类型：Bitmap、HyperLogLog、GEO。
- **Zset 底层结构**：数据量小时用**压缩列表 (ziplist / listpack)**；数据量大时用**字典 (Dict) + 跳表 (SkipList)**。字典用于 O(1) 查元素的 score，跳表用于 O(log N) 的范围和排序。
- **跳表原理**：在链表之上增加多层"索引指针"。每插入一个节点，通过抛硬币算法决定层数。查找时从最高层开始跳跃，效率逼近红黑树且实现简单得多。

**为什么 Redis 选择跳表而不是红黑树？** 范围查询更快（顺序遍历 vs 中序遍历）、实现更简单（200行 vs 1000+行）、并发更友好（局部修改 vs 全局旋转）。

### 3. 为什么用 Hash 存对象？

String 存 JSON 的问题：修改单个字段需要读取整个 JSON → 反序列化 → 修改 → 序列化 → 写回。

Hash 存对象的优势：字段级操作（`HSET user:1001 age 26`）、小对象时使用 ziplist 编码节省空间、语义清晰。

### 4. Redis 存储时内存不够了怎么办？

会触发 `maxmemory-policy` 内存淘汰策略。最推荐的通用配置是 **allkeys-lru**（对全部键，淘汰最近最少使用的）。

### 5. 缓存穿透、缓存击穿、缓存雪崩

**1. 缓存穿透 —— "无中生有的隐身黑客"**

恶意请求大量查询缓存和数据库中都不存在的数据。解法：缓存空对象 + 布隆过滤器。

**2. 缓存击穿 —— "顶流明星的突发暴击"**

某一个极度热点的 Key 突然过期，十万级并发请求同时冲进 MySQL。解法：互斥锁（分布式锁 + Double-Check） + 逻辑过期。

```java
public String getProductInfo(String productId) {
    String cacheKey = "product:" + productId;
    String lockKey = "lock:product:" + productId;
    String data = redis.get(cacheKey);
    if (data != null) return data;

    RLock lock = redissonClient.getLock(lockKey);
    try {
        if (lock.tryLock(3, TimeUnit.SECONDS)) {
            // 双重检查 Double-Check
            data = redis.get(cacheKey);
            if (data != null) return data;
            data = mysql.query("SELECT * FROM product WHERE id = " + productId);
            redis.setEx(cacheKey, data, 1, TimeUnit.HOURS);
            return data;
        } else {
            Thread.sleep(50);
            return getProductInfo(productId);
        }
    } finally {
        if (lock.isHeldByCurrentThread()) lock.unlock();
    }
}
```

**3. 缓存雪崩 —— "保安大队的集体罢工"**

大量 Key 在同一时刻集体过期。解法：随机过期时间（防扎堆） + 高可用架构（哨兵/集群）。

### 6. 布隆过滤器 vs Redis Bitmap

- **布隆过滤器**：多对多模糊映射，能容忍极小误判率。适合 UUID、长字符串、URL。
- **Redis Bitmap**：一对一精准绑定，100% 精确。适合连续自增 ID（签到打卡、DAU 统计）。

### 7. 怎么使用 Redis 实现分布式锁？Redisson 的原理

原生实现：`SET resource_name my_random_value NX PX 30000`。

**避坑**：原生锁的严重缺陷是如果业务执行超时，锁到期自动释放，第二个线程会闯入。

**Redisson 原理**：

- **加锁机制与可重入**：底层使用 Hash 结构。Key 为锁名，Hash 内部 field 为"线程唯一ID"，value 记录重入次数。通过执行 **Lua 脚本**保证原子性。
- **看门狗 (Watchdog)**：拿到锁后，后台开启定时任务，默认每隔 10 秒重置存活时间为 30 秒，直到你主动 `unlock()` 关掉看门狗。

### 8. 为什么 Redis 可以实现分布式锁？它本身不也是一个程序吗，是否会存在并发问题？

**核心答案**：Redis 的命令执行是**单线程串行**的，所有客户端发来的命令最终都排成一条队列被**逐个执行**，天然保证了每条命令的原子性。所以 `SET key value NX`（不存在才设置）在 Redis 内部不会出现两个线程同时执行的情况——要么你抢到了，要么我抢到了。

**但 Redis 单线程 ≠ 分布式锁不需要考虑并发安全**：

Redis 单线程保证的是**Redis 服务端内部命令执行的原子性**。但从 Java 应用端来看，"判断锁是否存在 + 加锁"如果分成两条命令，中间可能有其他客户端插队。所以必须用 `SET NX PX`（一条命令完成判断+加锁+设超时）或 **Lua 脚本**把多步操作打包成原子操作提交给 Redis。

**补充：Redis 是单线程还是多线程？** Redis 6.0 之后引入了**多线程 I/O**，但这只用于网络数据的读写和协议解析。真正的**命令执行仍然是单线程串行的**，所以原子性保证不变。

### 9. 什么是 session 共享问题？为什么 Redis 能解决？

**问题**：传统 Session 存储在 Web 服务器内存中。集群部署时，用户第一次请求到 A 服务器创建 Session，第二次请求被负载均衡到 B 服务器，B 服务器找不到这个 Session，用户被迫重新登录。

**Redis 解决原因**：Redis 是一个**独立于 Web 服务器的集中式存储**。所有服务器共用同一个 Redis 存储 Session（或 Token），无论请求被分配到哪台服务器，都能从 Redis 中找到用户的会话信息。**本质是用集中式存储替代了单机存储。**

### 10. 分布式锁的 5 大核心要素

实现一个健壮的分布式锁，必须满足以下条件：

1. **互斥性**：使用 `SET key value NX`（Not eXists）保证只有一个线程能抢到锁。
2. **防死锁**：必须给锁设置**过期时间**（`PX 30000`），防止持有锁的节点宕机导致死锁。且 `SETNX` 和设置过期时间必须是**一条原子命令**。
3. **防误删**：锁的 Value 必须设为当前线程的**唯一标识（UUID+ThreadId）**，释放锁前判断是否为自己的锁。
4. **原子释放**：判断和删除锁的动作必须使用 **Lua 脚本**执行，保证原子性。
5. **自动续期**：使用 **Redisson 的 Watch Dog 机制**，后台定时为未执行完的业务自动续期。

### 11. Redis Stream 怎么实现削峰缓冲的？

**问题**：秒杀场景下，瞬时流量可能有十万级请求同时涌入，如果直接打到数据库，数据库必崩。

**Redis Stream 削峰原理**：把请求先快速"接住"，放进消息队列排队，后台消费者按照自己的处理能力慢慢消费。

1. **生产者（前端请求）**：秒杀校验通过后，使用 Lua 脚本原子性地扣减 Redis 中的库存，并通过 `XADD` 将订单信息投递到 Redis Stream。
2. **消费者（后台线程）**：使用 `XREADGROUP` 以消费者组方式阻塞读取 Stream 中的消息，逐条落库（创建订单、扣减数据库库存等）。
3. **ACK 确认**：消费成功后调用 `XACK` 确认。如果消费者崩溃，未 ACK 的消息留在 pending list 中，重启后可从 pending list 恢复重试。

### 12. Redis 的持久化（RDB、AOF、混合持久化）

1. **RDB (快照)**：`bgsave` fork 出子进程生成紧凑的 .rdb 二进制文件。文件小重启快，但两次快照间隙断电数据全丢。
2. **AOF (追加日志)**：每个写命令记录到日志尾部。配置 `everysec` 最多丢 1 秒数据，但日志越积越大需要重写。
3. **混合持久化 (Redis 4.0+)**：AOF 重写时先写 RDB 格式快照，后续增量以 AOF 追加。兼具两者的极致优点。

### 9. Redis 集群方案

- **主从复制 + 哨兵 (Sentinel)**：高可用，读写分离。但只有一个 Master，写能力有限。
- **Redis Cluster**：16384 个哈希槽分配给多个 Master。数据分片支持海量数据，多 Master 写能力强。

### 10. 消息的可达性、可靠性、有序性、幂等性

| 特性 | 定义 | 实现方案 | 关键技术 |
|------|------|----------|----------|
| 可达性 | 消息不丢失 | ACK + pending list | Redis Stream |
| 可靠性 | 业务一致性 | 本地消息表 + 定时重试 | 事务 |
| 有序性 | 按顺序消费 | 单线程/分区/版本号 | 线程池 |
| 幂等性 | 重复消费幂等 | 唯一索引/Redis去重/版本号 | 数据库/Redis |

### 15. Hash 适合范围查询吗？List 呢？

**Hash 不适合范围查询**。Hash 内部是哈希表或压缩列表，**没有排序能力**，无法按 field 值做范围查询。

```redis
HSET user:1001 name "张三"
HSET user:1001 age 25
# 你没法执行"查所有 age > 20 的 field"这种操作
# 范围查询应该用 Zset：ZRANGEBYSCORE scores 80 100
```

**List 部分适合**。List 支持按下标范围获取（`LRANGE key 0 9` 取前 10 个元素），但这是**位置下标**范围，不是**值的大小**范围。如果需要按 score/值做范围查询，还是得用 **Zset**。

### 16. 有了 MySQL（Buffer Pool 也在内存中），Redis 还有必要存在吗？

**有必要，三个核心原因**：

```
1. Buffer Pool 有限且容易被冷数据挤走：
   MySQL Buffer Pool 默认 128MB，远不能把所有数据放内存。
   一个冷查询就可能把热数据从 Buffer Pool 挤走，
   导致后续热数据也要读磁盘 → 性能抖动。
   Redis 是专门的缓存层，可以精确控制缓存哪些热数据。

2. MySQL 每次查询有大量中间开销：
   即使数据在 Buffer Pool 里，也要经过：
   TCP 连接管理 → SQL 解析 → 查询优化器 → 权限校验 → 执行器
   Redis 命令极其轻量，直接内存键值查找，省掉了这一串中间环节。

3. 并发能力差距巨大：
   MySQL 连接数有限（默认 151），每个连接一个线程。
   Redis 单线程 + epoll 轻松处理 10万+ QPS。
   高并发下 Redis 挡在前面，保护 MySQL 不被打爆。
```

### 17. 缓存穿透、击穿、雪崩，哪个最好解决？哪个最不好解决？

- **最好解决的：缓存穿透**。方案成熟且简单——缓存空对象（几行代码）或布隆过滤器（一个数据结构）就能有效拦截。防御手段明确。
- **最不好解决的：缓存雪崩**。触发条件多样（大量 Key 同时过期、Redis 宕机），影响面最广（不是一个 Key 的问题，是整体缓存层失效）。需要从多个层面综合防御：随机过期时间 + Redis 高可用集群 + 多级缓存 + 限流降级熔断，没有银弹。
- **缓存击穿**介于两者之间：针对热点 Key 用互斥锁或逻辑过期即可。

---

## 八、 网络协议

### 1. 登录的时候 HTTP 请求是怎么发送的？

1. **应用层**：浏览器将账号密码封装进 HTTP 报文的 Body 中（POST 请求）。
2. **解析 IP**：查询 DNS 将域名解析为服务器 IP。
3. **传输层 TCP 三次握手**：
   - **第一次 (C→S)**：客户端发出 SYN=1, seq=x。
   - **第二次 (S→C)**：服务端回发 SYN=1, ACK=1, ack=x+1, seq=y。
   - **第三次 (C→S)**：客户端回发 ACK=1, ack=y+1, seq=x+1。连接建立。
4. **网络层与链路层**：IP 协议加上源/目标 IP；MAC 层通过 ARP 找到下一跳 MAC 地址。
5. 服务端 Tomcat 接收解析后交给 Spring MVC 校验登录。

### 2. HTTPS 加密具体是怎么做的？

HTTPS = HTTP + SSL/TLS。采用**非对称加密 + 对称加密 + CA 数字证书**的混合模式：

1. **ClientHello**：客户端发送支持的加密套件列表和随机数 A。
2. **ServerHello**：服务端确定加密套件，回传随机数 B 和 **CA 证书**（内含服务器公钥）。
3. **证书校验**：客户端用内置的根证书校验证书合法性。
4. **Pre-Master**：客户端生成第三个随机数，用服务器公钥**非对称加密**后发给服务端。
5. **生成会话密钥**：双方用三个随机数拼成**对称加密密钥（Master Secret）**。
6. **加密通信**：此后所有数据使用此密钥进行对称加密传输。

**非对称加密**仅在 TLS 握手期间使用（安全传递 Pre-master）；**对称加密**在整个业务会话期间使用（快速加解密数据）。

### 3. TCP 可靠性六大保证机制

1. **序号与确认应答 (SEQ & ACK)**：每个数据包标记序号，接收端回复 ACK。
2. **超时重传**：超时未收到 ACK 就重传数据。
3. **流量控制（滑动窗口）**：接收端告诉发送端接收窗口大小，控制发送速度。
4. **拥塞控制**：慢启动 → 拥塞避免 → 快速重传 → 快速恢复。
5. **校验和 (Checksum)**：验证数据完整性。
6. **顺序保证**：按 SEQ 重排序乱序数据包。

### 4. TCP 断开连接为什么是四次挥手而不是三次？

核心原因：**TCP 是全双工通信，需要双方都关闭**。

第 1、2 次挥手关闭 Client → Server 通道；第 3、4 次挥手关闭 Server → Client 通道。Server 收到 Client 的 FIN 后可能还有数据要发，所以 ACK 和 FIN 不能合并。

### 5. TIME_WAIT 为什么要设置 2MSL？

1. **确保最后的 ACK 能到达 Server**：1 个 MSL 等 ACK 到达 Server + 1 个 MSL 等 Server 的重传 FIN 返回。
2. **防止旧连接的数据包干扰新连接**：2MSL 后旧数据包一定过期了。

### 6. TIME_WAIT 过多怎么解决？

- 调整内核参数：`tcp_tw_reuse = 1`，增大端口范围。
- **HTTP 长连接**（根治）：`Connection: keep-alive`，一个 TCP 连接发送多个 HTTP 请求。
- 连接池复用。
- 让服务端主动关闭（转移 TIME_WAIT 到服务端）。

### 7. HTTP 幂等性

| 方法 | 幂等性 | 原因 |
|------|--------|------|
| GET | 幂等 | 只读，不修改数据 |
| POST | 非幂等 | 每次创建新资源 |
| PUT | 幂等 | 更新为相同值，结果相同 |
| DELETE | 幂等 | 删除后再删，最终状态相同 |

### 8. 下单接口的幂等性如何实现？

推荐组合：**Token 机制（主） + 唯一索引（兜底） + 前端防抖（优化）**。

Token 机制：用户进入下单页获取 token（存入 Redis） → 提交订单时带上 token → 后端用 Lua 脚本原子性检查并删除 token → 重复提交时 token 已不存在，直接拒绝。

### 9. TCP 粘包是什么？怎么解决？

**本质**：TCP 是字节流协议，没有消息边界。应用层发送的多个数据包，在 TCP 看来都是一条连续的字节流。

```
发送端：
send("Hello")  →  5 字节
send("World")  →  5 字节

接收端可能收到：
情况1：HelloWorld     ← 粘包（两个包粘在一起）
情况2：Hel | loWorld  ← 拆包（一个包被拆开了）
```

**为什么会粘包？** TCP 在底层有 Nagle 算法（小包合并发送优化）和接收端缓冲区，多个小消息可能被合并成一个大块交给应用层。

**3 种解决方案**：

```java
// 方案1：固定长度（每个消息固定100字节，不足补0）
// 简单但浪费空间

// 方案2：分隔符（如用 \n 作为消息边界）
send("Hello\n")
send("World\n")
// 接收端按 \n 切割

// 方案3：消息头 + 长度字段（最通用，工业级方案）
// 协议格式：[4字节长度][消息体]
// 发送时：先写4字节表示后面消息体有多长
// 接收时：先读4字节得到长度N，再读N字节得到完整消息
```

**Netty 内置解决方案**：

```java
// 固定长度解码器
pipeline.addLast(new FixedLengthFrameDecoder(100));

// 分隔符解码器
pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()));

// 长度字段解码器（最常用）
pipeline.addLast(new LengthFieldBasedFrameDecoder(
    1024,  // 最大帧长度
    0,     // 长度字段偏移量
    4,     // 长度字段占4字节
    0,     // 长度调整值
    4      // 跳过的初始字节数（跳过长度字段本身）
));
```

### 10. HTTP 常用状态码与响应头字段

**状态码速记**（不用死背，理解分类规律就行）：

```
2xx 成功：服务器成功处理了请求
  200 OK              — 正常返回
  201 Created          — POST 创建资源成功
  204 No Content       — 成功但没有响应体（常见于 DELETE）

3xx 重定向：客户端需要进一步操作
  301 Moved Permanently — 永久重定向（搜索引擎会更新 URL）
  302 Found            — 临时重定向（下次还来旧地址）
  304 Not Modified     — 资源没变，用浏览器缓存（配合 ETag 使用）

4xx 客户端错误：请求本身有问题
  400 Bad Request      — 参数格式错误
  401 Unauthorized     — 未登录（需要认证）
  403 Forbidden        — 已登录但没权限
  404 Not Found        — 资源不存在
  405 Method Not Allowed — GET/POST 用错了

5xx 服务器错误：服务器处理出了问题
  500 Internal Server Error — 代码抛异常了
  502 Bad Gateway      — Nginx 转发请求，但后端 Tomcat 挂了或连不上
  503 Service Unavailable — 服务器过载或维护中
```

**常用响应头**：

| 字段 | 作用 | 例子 |
|------|------|------|
| Content-Type | 告诉客户端数据格式 | `application/json; charset=UTF-8` |
| Content-Length | 响应体字节数 | `1234` |
| Set-Cookie | 服务端给客户端种 Cookie | `sessionId=abc123; HttpOnly` |
| Cache-Control | 浏览器缓存策略 | `max-age=3600`（缓存 1 小时） |
| ETag | 资源版本标识 | `"686897696a7c876b7e"` |
| Access-Control-Allow-Origin | CORS 跨域 | `*` 或 `https://example.com` |
| Location | 重定向目标 URL | 配合 301/302 使用 |

### 11. CAP 理论

CAP 是分布式系统的三个特性，**只能同时满足两个**：

- **C (Consistency)**：一致性，所有节点同一时间看到相同数据。
- **A (Availability)**：可用性，每个请求都能得到响应。
- **P (Partition tolerance)**：分区容错，网络分区时系统仍能工作。

**为什么不能三个都要？** 网络分区（P）是不可避免的现实。一旦发生分区，你只能在 C 和 A 之间选：要么拒绝请求保证一致性（CP），要么返回旧数据保证可用性（AP）。

```
常见选型：
CP 系统：ZooKeeper、Redis Cluster（宁可不可用，也不返回错误数据）
AP 系统：Eureka、Cassandra（宁可返回旧数据，也不拒绝请求）
CA 系统：单机 MySQL（没有分区问题，但也不是分布式了）

实际项目中：
- 缓存系统通常选 AP（Redis 缓存可以短暂不一致，但不能不可用）
- 金融转账通常选 CP（宁可慢，也不能出错）
```

---

## 九、 操作系统

### 1. 操作系统的调度算法有了解吗？

常见的进程/线程调度算法：

1. **先来先服务 (FCFS)**：按请求到达的先后顺序执行。简单但可能导致短任务等待太久（"护航效应"）。
2. **短作业优先 (SJF)**：优先执行预计运行时间最短的进程。平均等待时间最优，但可能饿死长任务。
3. **时间片轮转 (Round Robin)**：每个进程分配一个固定时间片（如 10ms），用完就切换到下一个。**现代 OS 最常用**，兼顾公平性和响应速度。
4. **优先级调度**：按进程优先级高低调度。可能导致低优先级进程饿死，通常结合"优先级老化"（随等待时间提升优先级）解决。
5. **多级反馈队列**：综合了以上优点。设置多个优先级队列，新进程进最高优先级队列用短时间片，用完降到下一级用更长时间片。**Linux CFS 调度器的核心思想**。

### 2. 进程间通信方式

管道(Pipe)、命名管道(FIFO)、消息队列、共享内存（最快）、信号量（PV操作）、信号(Signal)、Socket（跨主机）。

### 2. 线程继承进程哪些资源？

**线程共享**：代码段、数据段、堆内存、全局变量、文件描述符、信号处理器。
**线程独享**：线程ID、栈、寄存器、程序计数器、errno变量。

### 3. 线程比进程轻量，为什么？

1. **创建/销毁快**：线程只分配栈空间，快 100-1000 倍。
2. **切换快**：不需要切换页表和刷新 TLB，快 10-100 倍。
3. **通信快**：直接访问共享内存，无需系统调用和数据拷贝。
4. **内存占用小**：100 个进程 5GB vs 100 个线程 850MB。

### 4. 系统调用是什么？

用户程序访问内核资源的接口。过程：用户程序调用 `read()` → 触发软中断 → CPU 切换到内核态 → 保存用户态上下文 → 查系统调用表 → 执行 `sys_read()` → 恢复用户态上下文 → 切换回用户态。

### 5. 用户态切换到内核态还有哪些方式？

1. **系统调用**（用户程序主动，如 read、write）
2. **异常**（CPU 检测到错误，如除零、缺页）
3. **中断**（外部硬件，如时钟、键盘、网卡）

---

## 十、 算法与场景题

### 1. 手撕：快速排序、冒泡排序

（手撕经典排序算法）

### 2. 手撕 Hot100：最长回文子串

使用**中心扩展算法**，时间复杂度 O(n²)，空间复杂度 O(1)。

```java
public String longestPalindrome(String s) {
    if (s == null || s.length() < 1) return "";
    int start = 0, end = 0;
    for (int i = 0; i < s.length(); i++) {
        int len1 = expandAroundCenter(s, i, i);     // 奇数中心
        int len2 = expandAroundCenter(s, i, i + 1); // 偶数中心
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
    return right - left - 1;
}
```

### 3. N 个线程交替打印 1~m

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
                            condition.await(); // 避坑：必须用 while 防止虚假唤醒
                        }
                        if (count > m) {
                            condition.signalAll(); // 避坑：必须唤醒其他在 await 的线程
                            break;
                        }
                        System.out.println(Thread.currentThread().getName() + " : " + count++);
                        turn = (turn + 1) % n;
                        condition.signalAll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock(); // 避坑：锁释放必须在 finally 中
                    }
                }
            }, "线程" + (i + 1)).start();
        }
    }
    public static void main(String[] args) { print(3, 10); }
}
```

### 4. 判断树是否是完全二叉树

思路：BFS 层序遍历，遇到第一个 null 后，后面不能再有非 null 节点。时间 O(n)，空间 O(n)。

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
            if (hasNull) return false;
            queue.offer(node.left);
            queue.offer(node.right);
        }
    }
    return true;
}
```

### 5. TopK 问题

**方案 1：小顶堆**（适合海量数据，时间 O(n log k)，空间 O(k)）

```java
public int findKthLargest(int[] nums, int k) {
    PriorityQueue<Integer> heap = new PriorityQueue<>();
    for (int num : nums) {
        heap.offer(num);
        if (heap.size() > k) heap.poll();
    }
    return heap.peek();
}
```

### 6. 有序数组转平衡二叉搜索树

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
```

### 7. Git 冲突解决标准流程

1. **沟通与抉择**：与同事沟通，手动删掉 `<<<<<<< HEAD` 等冲突标记，保留正确的代码。
2. **标记已解决**：执行 `git add <文件名>`。
3. **提交合并**：执行 `git commit`。

**加分项**：实际企业开发中，通常直接使用 IDEA 自带的 Resolve Conflicts 图形化工具，通过三方对比视图一键精准合并。

### 8. 场景题：打开页面慢怎么排查？

从浏览器开发者工具 Network 面板分析：前端资源加载慢（CDN、压缩、懒加载）、DNS 解析慢、后端接口慢（慢查询日志 + EXPLAIN）。

### 9. 回溯算法框架（LeetCode 高频模板）

回溯的本质不是"直接算出答案"，而是**遍历一棵选择树**，把每条合法路径都走到底。

**核心骨架**（所有回溯题都是这个结构的变体）：

```java
void backtrack(当前状态, 选择列表, 路径path, 结果集res) {
    if (满足收集条件) {
        res.add(new ArrayList<>(path)); // 存快照，不是存引用！
        return;
    }
    
    for (选择 : 当前层的候选列表) {
        // 做选择
        path.add(选择);
        // 递归到下一层
        backtrack(下一层状态, ...);
        // 撤销选择（回到父节点，试别的分支）
        path.remove(path.size() - 1);
    }
}
```

**两种常见回溯树**：

```
类型1：二叉决策树（当前元素选/不选）
适合：subsets（子集）

类型2：for 枚举树（当前层谁来做下一个元素）
适合：permutations（排列）、combinationSum（组合求和）

区分口诀：
"当前这个元素要不要" → 二叉树
"这一层谁来做下一个元素" → for 枚举
```

**同层去重**（combinationSum2、subsetsWithDup 的核心）：

```java
// 必须先排序！让重复值相邻
Arrays.sort(nums);

for (int i = start; i < nums.length; i++) {
    // 同层后续重复值跳过
    if (i > start && nums[i] == nums[i - 1]) continue;
    
    path.add(nums[i]);
    backtrack(i + 1, ...); // 每个位置只能用一次 → i+1
    path.remove(path.size() - 1);
}
// 注意：i > start（不是 i > 0）才是"本层后续候选"
// i == start 是本层第一个候选，必须允许
```

### 10. 网格 DFS 框架（岛屿类题目通用模板）

岛屿题的本质是**在网格中找连通块**。

**通用 DFS 模板**：

```java
void dfs(char[][] grid, int i, int j) {
    // 1. 越界检查
    if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length) return;
    // 2. 不是目标格子就停
    if (grid[i][j] != '1') return;
    // 3. 标记已访问（防止重复访问）
    grid[i][j] = '0';
    // 4. 向四个方向扩散
    dfs(grid, i - 1, j); // 上
    dfs(grid, i + 1, j); // 下
    dfs(grid, i, j - 1); // 左
    dfs(grid, i, j + 1); // 右
}
```

**做题时先问自己 4 个问题**：

| 问题 | numIslands | solve (被围绕的区域) | maxAreaOfIsland |
|------|-----------|---------------------|----------------|
| 目标格子是谁？ | `'1'` | `'O'` | `1` |
| 从哪里启动 DFS？ | 全图扫描遇 `'1'` | **只从边界**上的 `'O'` | 全图扫描遇 `1` |
| 标记方式？ | `'1'→'0'`（淹掉） | `'O'→'P'`（保护） | `1→0` |
| 结果？ | 启动几次 = 几个岛 | 剩余 `O→X`，`P→O` | 取最大面积 |

---

## 十一、 项目深度挖掘

### 请介绍一个你实习/项目中参与的项目，重点说明你负责的非简单 CRUD 模块或技术挑战。

**答题框架**：使用 STAR 法则，重点突出"为什么要这么做"和"踩了什么坑"。

**示例思路**（以苍穹外卖项目为例）：

> 背景（S）：外卖系统在秒杀场景下面临高并发挑战，单纯的 CRUD 会导致超卖和数据库被打爆。
>
> 任务（T）：我负责设计秒杀下单模块，要求在 1000+ 并发下保证库存不超卖、系统不宕机。
>
> 行动（A）：
> 1. **分布式锁防超卖**：从最初的 `synchronized` → `SET NX` → 发现锁过期问题 → 引入 Redisson 看门狗自动续期。
> 2. **异步解耦提吞吐**：把校验库存和创建订单拆开。校验通过后丢入 Redis Stream 消息队列，后台消费者异步落库。
> 3. **Lua 脚本保原子性**：把"判库存 + 扣库存 + 发消息"打包成 Lua 脚本提交给 Redis，一次性执行。
>
> 结果（R）：TPS 从 120 提升到 2500，响应时间从 890ms 降到 15ms，0 超卖。

### STAR 法则

- **S**ituation（项目背景与难点挑战）
- **T**ask（我接到的具体任务）
- **A**ction（**重中之重**：运用什么技术手段，踩了什么坑，怎么解决的）
- **R**esult（量化指标：TPS 提升多少，响应延迟降低多少）

### 并发量上来了怎么调整？

单体 Tomcat 瓶颈 → 水平扩容 Nginx 负载均衡 → 引入 Redis 缓存挡掉 90% 读请求 → 引入 MQ 异步削峰填谷排队写 → 单表过千万分库分表 + ES 异构查询。

### 延迟双删流程

```java
public void updateUser(User user) {
    redisTemplate.delete("user:" + user.getId());  // 1. 删除缓存
    userMapper.update(user);                          // 2. 更新数据库
    new Thread(() -> {
        Thread.sleep(500);
        redisTemplate.delete("user:" + user.getId());  // 3. 延迟后再次删除
    }).start();
}
```

### WebSocket 在哪些地方用到？

WebSocket 是一种**全双工通信协议**，建立连接后服务端可以**主动推送**消息给客户端，不需要客户端轮询。

```java
// 传统 HTTP 轮询：客户端每隔 1 秒问一次"有新消息吗？"
// → 99% 的请求是无意义的空查询，浪费带宽和服务器资源

// WebSocket：建连后保持长连接，服务端有数据直接推
@ServerEndpoint("/ws/chat/{userId}")
public class ChatEndpoint {
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        // 用户上线，保存 session
        onlineSessions.put(userId, session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 收到客户端消息，转发给目标用户
        ChatMessage msg = JSON.parseObject(message, ChatMessage.class);
        Session target = onlineSessions.get(msg.getToUserId());
        if (target != null) {
            target.getBasicRemote().sendText(message); // 服务端主动推送
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        onlineSessions.remove(userId); // 用户下线
    }
}
```

**适用场景**：实时聊天、在线状态、新订单通知、系统公告推送、股票实时行情。

### DFA 敏感词过滤

DFA（确定有限状态自动机）用于高效匹配敏感词。核心是构建一棵多叉树（类似 Trie 树），匹配时间复杂度 O(n)（n 是文本长度），和敏感词数量无关。

```java
// 构建 DFA 词典树
public class DFAFilter {
    private Map<Character, Object> root = new HashMap<>();

    // 添加敏感词
    public void addWord(String word) {
        Map<Character, Object> node = root;
        for (char c : word.toCharArray()) {
            node = (Map<Character, Object>) node.computeIfAbsent(c, k -> new HashMap<>());
        }
        node.put('\0', true); // 标记词尾
    }

    // 检测文本中是否包含敏感词
    public boolean contains(String text) {
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> node = root;
            int j = i;
            while (j < text.length() && node.containsKey(text.charAt(j))) {
                node = (Map<Character, Object>) node.get(text.charAt(j));
                if (node.containsKey('\0')) return true; // 命中
                j++;
            }
        }
        return false;
    }
}
// 为什么不用暴力匹配？暴力匹配 O(n*m*k)，DFA 只要 O(n)
```

### 零拷贝（Zero-Copy）

**传统 IO 发送文件需要 4 次数据拷贝**：

```
硬盘 → 内核缓冲区（DMA拷贝）
内核缓冲区 → 用户缓冲区（CPU拷贝）  ← 浪费
用户缓冲区 → Socket缓冲区（CPU拷贝） ← 浪费
Socket缓冲区 → 网卡（DMA拷贝）
```

**零拷贝只要 2 次**（跳过用户态，数据不经过应用程序）：

```java
// Java NIO 的 transferTo → 底层调用 sendfile 系统调用
FileChannel fileChannel = new FileInputStream(file).getChannel();
fileChannel.transferTo(0, fileChannel.size(), socketChannel);
// 数据直接从内核缓冲区 → 网卡，不经过用户态

// Kafka、Nginx、RocketMQ 底层都用了零拷贝
```

**Netty 的零拷贝是应用层优化**（不是 OS 层面的零拷贝）：

```java
// CompositeByteBuf：逻辑组合多个 ByteBuf，不做内存拷贝
CompositeByteBuf composite = Unpooled.compositeBuffer();
composite.addComponents(true, buf1, buf2); // 不拷贝，只是逻辑合并

// Slice：共享底层数组，不拷贝
ByteBuf slice = buf.slice(0, 10); // 共享 buf 的前 10 字节
```

---

## 💡 小白友好版底层脉络梳理（融会贯通）

### 一、 内存与并发线（JVM + 并发编程）

我们写的 Java 代码跑在 JVM 里。JVM 的"堆"是共享的大仓库，存放我们 new 出来的各种对象。垃圾回收（GC）就是雇了几个环卫工人（CMS、G1）去仓库收垃圾。

多线程就像你请了多个打工人去仓库搬货。他们抢着修改同一个订单状态时，就会打架（并发安全问题）。怎么防打架？

1. **发锁**：比如 synchronized 或 ReentrantLock。没拿到锁的就去 AQS 里坐板凳。
2. **不发锁（无锁并发）**：比如 CAS，改之前先对比旧值和实际值，一样才改。

### 二、 Web 交互线（网络 + Spring + Tomcat）

用户点一下登录，浏览器先通过 HTTP/HTTPS 找服务器，中间做 TCP 三次握手和 TLS 密码核对。

敲开门后，Tomcat 把请求交给 Spring 框架。Spring 就像一个超级工厂（IoC 容器），通过反射和缓存自动帮你组装好所有 Bean，遇到循环依赖还能用三级缓存解决。

### 三、 存储与性能线（MySQL + Redis）

数据落到 MySQL 里，数据太多查起来像海底捞针，于是有了 **B+ 树索引**。

但是硬盘再快并发高了也扛不住，于是在前面挡一层 **Redis**。Redis 数据全在内存里，单线程 + 多路复用模式一秒钟能应付上万次查询。需要分布式锁时就用 Redisson 的看门狗 + Lua 脚本。

---

> **文档版本**：v3.0（合并版）
> **更新时间**：2026年4月
> **内容来源**：Java后端高频面试题汇总 + 作业帮暑期实习面经全集
