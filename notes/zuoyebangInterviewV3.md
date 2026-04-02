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

---

## 二、 Java 集合框架

### 1. 你熟悉的数据结构有哪些？

数组（连续内存查询快）、单/双向链表（插入删除快）、栈（LIFO 先进后出）、队列（FIFO 先进先出）、哈希表（基于 Hash 散列，查询 O(1)）、树（如二叉搜索树、红黑树，查询 O(log N)）。

### 2. HashSet 是怎么保证元素不重复的？

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

### 5. HashMap 和 ConcurrentHashMap 实现区别？

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

### 8. 高并发状态下，通常应该怎么加锁？

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

### 2. 静态代码块 static { ... } 深度解析

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

假设 A 依赖 B，B 依赖 A：

1. A 实例化后（刚 new 出来，没赋值），先将一个能获取 A 的匿名工厂函数（ObjectFactory）放入**第三级缓存** `singletonFactories` 中。
2. A 开始属性注入，发现需要 B，于是去实例化 B。
3. B 实例化后注入属性发现需要 A，B 从三级缓存拿到了 A 的 ObjectFactory，调用 `getObject()` 获得 A 的早期引用，并把 A 移入**第二级缓存** `earlySingletonObjects`。
4. B 成功注入 A，B 完成初始化放入一级缓存（单例池）。
5. A 拿到 B 进行注入，最终 A 也完成初始化放入一级缓存。

**避坑**：构造器注入产生的循环依赖 Spring 无法解决。

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

### 9. MyBatis 核心安全：#{} 与 ${} 的区别

- `#{}` **(预编译占位符)**：底层使用 JDBC 的 `PreparedStatement`。先预编译 SQL 语句，再将参数作为纯文本安全注入。**有效防止 SQL 注入攻击**。
- `${}` **(字符串拼接)**：将参数原封不动地直接拼接到 SQL 语句中。**极易引发 SQL 注入漏洞**。仅在动态传入表名或动态指定 ORDER BY 排序字段等无法使用预编译的场景下使用。

### 10. Spring Boot 实战规范：RESTful 接口

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

### 4. 索引失效的案发现场

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

### 6. 事务隔离级别与锁机制

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

### 10. 慢查询排查与优化"三板斧"

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

### 13. 讲一下数据库的 undo log、redo log、binlog

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

### 8. Redis 的持久化（RDB、AOF、混合持久化）

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

---

## 九、 操作系统

### 1. 进程间通信方式

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

---

## 十一、 项目深度挖掘

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