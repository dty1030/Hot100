这份总结将我们刚才讨论的“银行比喻”与“底层源码”结合，梳理成了一份标准的面试复习文档。你可以直接复制到你的笔记软件中。

---

# 🧵 Java 线程池 (ThreadPoolExecutor) 深度总结

## 1. 为什么要使用线程池？
* **降低资源消耗**：通过池化技术重复利用已创建的线程，减少线程创建和销毁的性能开销。
* **提高响应速度**：任务到达时，无需等待线程创建即可立即执行。
* **提高线程的可管理性**：线程是稀缺资源，使用线程池可以进行统一分配、调优和监控。

---

## 2. 线程池 7 大核心参数


| 参数名 | 类型 | 银行比喻 | 作用描述 |
| :--- | :--- | :--- | :--- |
| **`corePoolSize`** | `int` | **正式工数量** | 核心线程数。即使没有任务，也不会被销毁的常驻线程。 |
| **`maximumPoolSize`** | `int` | **总柜台数** | 最大线程数。核心线程+临时工的总上限。 |
| **`keepAliveTime`** | `long` | **解雇倒计时** | 非核心线程闲置多久后会被销毁。 |
| **`unit`** | `TimeUnit` | **时间单位** | `keepAliveTime` 的单位（秒、毫秒等）。 |
| **`workQueue`** | `BlockingQueue` | **等候区椅子** | 存放待执行任务的阻塞队列。 |
| **`threadFactory`** | `ThreadFactory` | **人力资源 (HR)** | 负责创建新线程，通常用于给线程起个好记的名字。 |
| **`handler`** | `RejectedHandler` | **大堂保安** | 任务爆满且无法处理时的**拒绝策略**。 |

---

## 3. 线程池任务执行流程 (核心逻辑)


当一个新任务通过 `execute()` 方法提交时：
1.  **判断核心线程**：如果正在运行的线程数 < `corePoolSize`，则创建一个**新线程**执行任务。
2.  **判断阻塞队列**：如果核心线程已满，任务被尝试放入 `workQueue` **排队**。
3.  **判断最大线程**：如果队列也满了，判断当前线程数 < `maximumPoolSize`。如果成立，创建一个**非核心线程**（临时工）执行任务。
4.  **执行拒绝策略**：如果达到最大线程数且队列仍满，则由 `handler` 按照策略处理任务。

> **记忆口诀**：先看正式工，再看等候椅，椅满招临时，全满找保安。

---

## 4. 四种拒绝策略 (RejectedExecutionHandler)
当系统达到饱和状态时，可选的四种处理方案：

* **`AbortPolicy` (默认)**：直接抛出 `RejectedExecutionException` 异常，阻止系统正常运行（**打巴掌**）。
* **`CallerRunsPolicy`**：将任务退回给调用者（提交任务的线程）去执行。这样能保证任务不丢，同时降低新任务提交速度（**让引荐人自己干**）。
* **`DiscardPolicy`**：直接丢弃任务，不予处理也不报错（**假装没看见**）。
* **`DiscardOldestPolicy`**：丢弃队列中最老的一个任务（排在最前面的），尝试再次提交当前任务（**踢走排队最久的**）。

---

## 5. 常见的阻塞队列 (workQueue)
* **`ArrayBlockingQueue`**：基于数组的有界队列，按 FIFO（先进先出）排序。
* **`LinkedBlockingQueue`**：基于链表的有界队列（默认容量为 $2^{31}-1$），通常用于固定大小线程池。
* **`SynchronousQueue`**：不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作。

---

## 6. 代码示例
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5,                             // corePoolSize
    10,                            // maximumPoolSize
    60L,                           // keepAliveTime
    TimeUnit.SECONDS,              // unit
    new LinkedBlockingQueue<>(100), // workQueue
    Executors.defaultThreadFactory(), 
    new ThreadPoolExecutor.CallerRunsPolicy() // handler
);
```

---

这份总结能帮你应付 90% 的面试初筛。准备好之后，我们可以继续探讨 **“如何设置合理的线程池大小（CPU 密集型 vs IO 密集型）”**，或者开启 **“MySQL B+ 树”** 的详细教学？