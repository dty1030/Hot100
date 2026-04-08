# 📋 面经实录合集（interviewclip）

> 三场真实面试的题目整理，按面试场次排列，附简洁答案。
> 详细版本请参考 Interview.md。

---

## 面经一（技术一面 · 偏 Redis + MySQL + 算法）

### 1. Java 有哪几种基础数据类型？其中哪些有对应的包装类？

Java 有 8 种基础数据类型：byte(1)、short(2)、int(4)、long(8)、float(4)、double(8)、char(2)、boolean。

所有基础类型都有对应的包装类（Integer、Double、Character、Boolean 等），用于支持面向对象特性和泛型。

### 2. JVM 的内存模型？

JVM 运行时数据区分为两大类：

**线程私有**：程序计数器（记录当前字节码行号，唯一不会 OOM 的区域）、虚拟机栈（方法调用的栈帧，存局部变量表、操作数栈）、本地方法栈（为 Native 方法服务）。

**线程共享**：Java 堆（存放对象实例和数组，GC 主战场，按代划分为年轻代和老年代）、方法区/元空间（存放类信息、常量、静态变量。Java 8 后实现为位于直接内存的 Metaspace，不再受堆大小限制）。

### 3. Integer a = 1 存放在哪部分？Integer a = 1 和 Integer a = 100 有什么区别吗？

局部变量引用 `a` 存放在**虚拟机栈**中，Integer 对象实例存放在**堆内存**中。

**核心区别**：Java 在 Integer 类加载时预先缓存了 [-128, 127] 之间的对象。`Integer a = 1` 触发自动装箱调用 `Integer.valueOf(1)`，直接从 IntegerCache 缓存池返回。而 `Integer a = 200` 超出缓存范围，每次都会 `new Integer(200)` 在堆中创建新对象。

**避坑**：两个缓存范围内的 Integer 用 `==` 比较是 true（同一对象），超出范围的必须用 `equals()`。

```java
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)]; // 命中缓存
    return new Integer(i); // 超范围，new 新对象
}
```

### 4. 讲一下有哪些垃圾回收算法及其原理。

1. **标记-清除 (Mark-Sweep)**：先用 GC Roots 追踪标记存活对象，然后直接清除未标记的垃圾。**缺点**：产生大量内存碎片。
2. **复制算法 (Copying)**：内存一分为二，把存活对象紧凑地复制到另一半，原区域全部清空。**优点**：无碎片。**缺点**：空间利用率 50%。JVM 新生代的 Eden + 两个 Survivor (8:1:1) 就是优化的复制算法。
3. **标记-整理 (Mark-Compact)**：标记存活对象后向一端移动，清理边界以外内存。适合存活率高的老年代。

**JVM 真实做法（分代收集）**：新生代用复制算法，老年代用标记-清除或标记-整理。

### 5. 有哪些垃圾回收器？

- **CMS (Concurrent Mark Sweep)**：老年代收集器，以最短停顿时间为目标，基于标记-清除。并发阶段不阻塞用户线程。
- **G1 (Garbage-First)**：现代主流收集器（JDK 9 默认）。将堆划分为多个 Region，优先回收垃圾最多的 Region。可设置停顿时间目标（如 200ms）。
- **ZGC / Shenandoah**：极低延迟收集器（停顿 < 10ms），支持 TB 级堆内存。

### 7. 访问 Redis 为什么快？

1. **完全基于内存**：省去磁盘寻址耗时，数据读写在纳秒级。
2. **高效的底层数据结构**：全局 Hash 表 O(1)、SDS 动态字符串、跳表 SkipList。
3. **单线程模型**：核心命令执行由单线程串行处理，避免了多线程抢锁和 CPU 上下文切换的开销。
4. **I/O 多路复用**：底层使用 OS 的 epoll/kqueue，一个单线程就能并发监听成千上万个 Socket 连接，谁有数据就处理谁，网络吞吐量极高。

**I/O 多路复用原理**：传统模型中，一个线程只能盯着一个连接等数据（阻塞 I/O）。多路复用让一个线程同时监听多个文件描述符（Socket），内核负责通知"哪些连接有数据了"，线程只处理就绪的连接。epoll 是 Linux 上最高效的实现，事件驱动，不需要遍历所有连接，时间复杂度 O(1)。

### 8. Redis 有哪些数据类型？Zset 的底层数据结构？跳表的原理？

**基本类型**：String、List、Hash、Set、Zset (Sorted Set)。**高级类型**：Bitmap、HyperLogLog、GEO。

**Zset 底层**：数据量小时用压缩列表 (ziplist/listpack)；数据量大时用**字典 (Dict) + 跳表 (SkipList)**。字典用于 O(1) 查元素的 score，跳表用于 O(log N) 的范围查询和排序。

**跳表原理**：普通链表查找需 O(N)。跳表在链表之上增加多层"索引指针"：

```
Level 3:  10 ----------------------→ 70
Level 2:  10 -------→ 40 -------→ 70
Level 1:  10 → 20 → 30 → 40 → 50 → 60 → 70 → 80
```

每插入一个节点，通过随机算法（约 25% 概率）决定是否升一层索引。查找时从最高层开始向右跳跃，大了就降一层继续找，空间换时间，效率逼近红黑树且实现简单得多。

### 9. Redis 存储时内存不够了怎么办？

会触发 `maxmemory-policy` 内存淘汰策略。主流有：

1. **noeviction**（默认）：直接返回错误，不淘汰。
2. **allkeys-lru**：对全部键，淘汰最近最少使用的（**最推荐的通用配置**）。
3. **volatile-lru**：只在设了过期时间的键中淘汰最近最少使用的。
4. **allkeys-lfu / volatile-lfu**：Redis 4.0 引入，按使用**频率**（次数最少）淘汰。
5. 还有随机淘汰（random）和只淘汰马上过期的（ttl）策略。

### 10. 怎么使用 Redis 实现分布式锁？讲一下 Redisson 实现了哪些功能和原理。

原生实现：`SET resource_name my_random_value NX PX 30000`（原子性完成"不存在则设置 + 加超时"）。

**Redisson 功能与原理**：

- **可重入锁**：底层使用 Hash 结构。Key 为锁名，field 为"线程唯一ID"，value 记录重入次数。加锁/解锁通过 **Lua 脚本**保证原子性。
- **看门狗 (Watchdog)**：解决锁过期问题。拿到锁后后台开启定时任务，默认每 10 秒重置存活时间为 30 秒，直到主动 `unlock()` 关掉看门狗。彻底解决业务没跑完锁提前释放的灾难。

### 11. 讲一下 Redis 的持久化（AOF、RDB、合体）

1. **RDB (快照)**：`bgsave` fork 子进程生成紧凑的 .rdb 二进制文件。文件小重启快，但两次快照间隙断电数据全丢。
2. **AOF (追加日志)**：每个写命令记录到日志尾部。配置 `everysec` 最多丢 1 秒数据，但日志越积越大需重写（`bgrewriteaof`）。
3. **混合持久化 (Redis 4.0+)**：AOF 重写时先写 RDB 格式快照在文件开头，后续增量命令以 AOF 追加到尾部。重启时先飞速加载 RDB，再重放一小段 AOF，兼具两者的极致优点。

### 12. 数据库索引的数据结构？对比 B 树和 B+ 树。

MySQL InnoDB 引擎默认使用 **B+ 树**。

- **B 树**：所有节点都同时存储键值和数据记录。
- **B+ 树对比优势**：
  1. **更少的磁盘 IO**：非叶子节点**只存索引不存数据**，一个磁盘页能塞下更多索引指针，树高更低（3-4 层可存千万数据）。
  2. **范围查询极快**：所有数据都在叶子节点，叶子之间用**双向链表**相连，`WHERE id > 10` 只需找到起点后顺链表扫描即可。

### 13. 索引失效的场景？字段是字符串用数字查会失效吗？字段是整数用字符串查呢？

**常见失效场景**：联合索引违背最左前缀、`LIKE '%xxx'` 左模糊、在索引列上做函数/运算、使用 OR 时两边有非索引字段、隐式类型转换。

- **字段是字符串，使用数字查**：**会失效**。`varchar_col = 123`，MySQL 会隐式调用 `CAST(varchar_col AS SIGNED)` 把表里每行字符串转数字，相当于在索引列上加了函数，B+ 树有序性被破坏。
- **字段是整数，使用字符串查**：**不会失效**。`int_col = '123'`，MySQL 把参数 `'123'` 转成数字 123 去查树，没有动表字段的值，索引正常生效。

### 14. 数据库调优，索引优化。

1. **SQL 分析**：开启慢查询日志，用 `EXPLAIN` 分析执行计划。重点看 type（保证至少 range/ref，绝不能 ALL），Extra 是否 Using filesort。
2. **覆盖索引**：避免 `SELECT *`，把查询列放入联合索引，直接在二级索引上拿到数据，**避免回表**。
3. **索引下推 (ICP)**：MySQL 5.6+ 特性，在引擎层直接过滤非最左的索引条件，减少回表次数。
4. **架构优化**：读写分离、Redis 缓存前置、大表分库分表。

### 15. 讲一下 undo log、redo log、binlog。创建一条记录先写 undo log 还是 redo log？

- **undo log (回滚日志)**：记录修改前的数据状态。核心作用是事务回滚和实现 MVCC。
- **redo log (重做日志)**：InnoDB 特有的物理日志。核心作用是 **Crash-safe 掉电恢复**。
- **binlog (归档日志)**：Server 层逻辑日志。核心作用是主从复制和数据备份恢复。

**执行顺序**：**先写 undo log**（用于构建历史版本供事务回滚和隔离）→ 内存中修改数据页产生 redo log → 提交时采用**两阶段提交 (2PC)**：先写 redo log (prepare) → 写 binlog → 提交 redo log (commit)，保证 binlog 和 redo log 状态一致。

### 16. 手撕 Hot100：最长回文子串

使用**中心扩展算法**，时间 O(n²)，空间 O(1)：

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
```

---

## 面经二（技术二面 · 偏项目 + Spring + 网络 + 并发）

### 1. 印象最深的项目（围绕项目展开拷打）

使用 **STAR 法则**：**S**ituation（项目背景与挑战）→ **T**ask（我的具体任务）→ **A**ction（**重中之重**：用了什么技术、踩了什么坑、怎么解决）→ **R**esult（量化指标：TPS 提升多少、响应降低多少）。

**并发量上来了怎么调整的答题思路**：单体 Tomcat 瓶颈 → 水平扩容 Nginx 负载均衡 → Redis 缓存挡 90% 读请求 → MQ 异步削峰排队写 → 单表过千万分库分表 + ES 异构查询。

### 2. 说说 Java 里常用的框架。

- 控制层核心：Spring、Spring Boot、Spring MVC。
- 持久层框架：MyBatis、MyBatis-Plus、Spring Data JPA。
- 微服务架构：Spring Cloud (Alibaba 体系：Nacos、Sentinel、Seata)、Dubbo。
- 中间件客户端：Redisson (Redis)、RabbitMQ/Kafka 客户端。

### 3. Spring Boot 自动装配怎么实现的？

核心在 `@SpringBootApplication` → `@EnableAutoConfiguration` → `AutoConfigurationImportSelector`。

它会用 SpringFactoriesLoader 扫描类路径下所有依赖 jar 包中的 `META-INF/spring.factories` 文件，提取所有全限定类名，然后结合 `@ConditionalOnClass`、`@ConditionalOnMissingBean` 等条件注解过滤，只有满足条件的配置类才会被注册到 IoC 容器中。

### 4. Spring Boot 启动的时候会加载什么？

1. 初始化 SpringApplication，推断应用类型（如 Web 应用）。
2. 加载系统环境变量、命令行参数和 application.yml/properties，封装成 Environment。
3. 创建 ApplicationContext 容器。
4. 执行 `refreshContext`（扫描 @Component 并实例化 Bean）。
5. 启动内嵌 Web 服务器（如 Tomcat）。
6. 触发 CommandLineRunner / ApplicationRunner 回调。

### 5. Bean 的生命周期。

1. **实例化**：调用构造方法创建空壳对象。
2. **属性赋值**：处理 @Autowired 等依赖注入。
3. **初始化**：Aware 接口回调 → BeanPostProcessor 前置处理 → @PostConstruct / InitializingBean → BeanPostProcessor 后置处理（**AOP 代理对象在这里生成**）。
4. **销毁**：容器关闭时 @PreDestroy / DisposableBean.destroy。

### 6. Bean 容器是怎么管理的？底层做了什么？

底层由 `DefaultListableBeanFactory` 管理。Spring 启动时先解析注解生成 **BeanDefinition**（类的蓝图：全路径、是否单例、依赖关系），存入 `beanDefinitionMap`（ConcurrentHashMap）。然后遍历这个 Map，利用**反射**调用构造器创建实例，单例 Bean 缓存入单例池 `singletonObjects` 中。

### 7. IoC 介绍一下，怎么实现的？

IoC（控制反转）：把创建对象的控制权从代码内部转移给 Spring 容器。底层依靠**解析注解蓝图 + Java 反射 + 工厂模式 + ConcurrentHashMap 缓存**实现对象的动态创建与统一存放。

### 8. DI 呢？和 IoC 有没有关系，怎么实现的？

DI（依赖注入）是 IoC 思想的**具体实现手段**。IoC 是目标，DI 是手段。容器在创建对象时，发现它依赖另一个对象，会自动从缓存里找到并通过**反射调用 setter 方法或带参构造函数**注入进去。

### 9. 循环依赖怎么解决的？

针对**单例 Setter/属性注入**，Spring 采用**三级缓存**机制：

1. A 实例化后（空壳），先将 ObjectFactory 放入**三级缓存** `singletonFactories`（提前暴露早期引用）。
2. A 注入属性发现需要 B，去实例化 B。
3. B 注入属性发现需要 A，从三级缓存拿到 A 的 ObjectFactory 获得 A 的早期引用（如有 AOP 则提前生成代理），移入**二级缓存** `earlySingletonObjects`。
4. B 完成初始化放入一级缓存。A 拿到 B 也完成初始化放入一级缓存。

**避坑**：构造器注入的循环依赖 Spring 无法解决。

### 10. 登录的时候 HTTP 请求是怎么发送的？

1. **应用层**：浏览器将账号密码封装进 HTTP POST 报文 Body。
2. **DNS 解析**：域名 → IP 地址。
3. **传输层 TCP 三次握手**：SYN → SYN+ACK → ACK，建立可靠连接。
4. **网络层 + 链路层**：IP 协议加上源/目标 IP；MAC 层通过 ARP 找下一跳 MAC 地址。
5. 服务端 Tomcat 接收后交给 Spring MVC 校验登录。

### 11. HTTPS 加密具体怎么做的？

HTTPS = HTTP + SSL/TLS，采用**非对称加密 + 对称加密 + CA 数字证书**的混合模式：

1. **ClientHello**：客户端发送支持的加密套件列表和随机数 A。
2. **ServerHello**：服务端确定加密套件，回传随机数 B 和 **CA 证书**（含服务器公钥）。
3. **证书校验**：客户端用内置的根证书校验证书合法性。
4. **Pre-Master**：客户端生成第三个随机数，用服务器公钥**非对称加密**后发给服务端。
5. **生成会话密钥**：双方用三个随机数拼成**对称加密密钥（Master Secret）**。
6. **加密通信**：此后所有数据使用此密钥**对称加密**传输。

### 12. 对称加密和非对称加密在 HTTPS 中分别在哪里用到的？

- **非对称加密（慢，安全）**：仅在 TLS 握手期间使用，用于**安全传递 Pre-master**。黑客没有服务器私钥无法解密。
- **对称加密（快）**：在握手成功后的整个业务会话期间使用。所有 HTTP 数据用协商出的**会话密钥**加解密。

### 13. CA 了解吗？

CA (Certificate Authority) 即数字证书认证机构。服务端向 CA 申请证书，CA 用自己的私钥对服务器信息摘要进行加密生成数字签名，附在证书上。浏览器出厂时内置各大顶级 CA 的公钥，用来验证证书真伪。

### 14. 中间人攻击怎么防范？

HTTPS 依靠 CA 证书机制防范。黑客拦截请求后发给客户端一个伪造的公钥证书，但由于黑客没有真正的 CA 私钥，无法生成让 CA 公钥能解密校验通过的合法签名。浏览器校验失败会抛出大红屏警告 `NET::ERR_CERT_AUTHORITY_INVALID`，中断连接。

### 15. 高并发状态下，锁怎么加？

1. **无锁化**：LongAdder、AtomicInteger (CAS+volatile)。
2. **分段锁**：JDK 8 ConcurrentHashMap 锁桶首节点。
3. **读写分离锁**：ReentrantReadWriteLock，读读不互斥。
4. **缩短锁内代码**：只锁关键代码，不锁耗时 IO。

### 16. Java 都有哪些锁？synchronized 怎么实现的？能用在哪？

锁分类：乐观/悲观锁、公平/非公平锁、可重入锁、自旋锁、独占/共享锁。

`synchronized` 属于悲观锁、非公平锁、可重入锁。可以修饰**代码块、实例方法、静态方法**。

**底层原理**：编译后插入 `monitorenter` / `monitorexit` 指令。对象头 Mark Word 指向 ObjectMonitor。JDK 1.6 引入锁升级：无锁 → 偏向锁 → 轻量级锁（CAS 自旋） → 重量级锁（OS 互斥量）。

### 17. Java 里的事务怎么用的？如果事务没有成功，会抛出什么异常？

通常使用 `@Transactional` 声明式事务。底层基于 AOP，方法前 `setAutoCommit(false)` 开启事务，正常完成 commit()，抛异常则 rollback()。

**避坑**：默认**只对 RuntimeException 和 Error 回滚**。如果抛出的是受检异常（如 IOException），事务会**静默提交**！必须写明 `@Transactional(rollbackFor = Exception.class)` 才万无一失。

事务失败时具体抛出的异常取决于业务代码。如果是 Spring 事务框架本身的问题（如传播行为冲突），会抛出 `TransactionException` 及其子类（如 `UnexpectedRollbackException`）。

---

## 面经三（技术三面 · 偏 Java 基础 + 并发 + 集合底层）

### 1. 面向对象的三大特性是什么？分别介绍一下。

1. **封装**：将属性隐藏在内部，通过 getter/setter 方法控制访问。
2. **继承**：子类继承父类的特征和行为，提高代码复用。
3. **多态**：同一个行为具有多个不同表现形式。父类引用指向子类对象，运行时根据实际类型执行不同方法。

### 2. 继承和实现接口的两个最大区别是什么？有什么共同点？

- **区别**：1. 类只支持**单继承**，但可以**多实现**接口。2. 继承是 "is-a" 强耦合关系，接口是 "can-do" 能力契约。
- **共同点**：都能作为多态的基础；都不能被直接实例化。

### 3. 你知道 default 方法吗？

Java 8 引入 `default` 关键字，允许在接口中编写带默认实现的方法。主要目的是**向后兼容**——给接口新增方法时，旧的实现类不需要全部修改，可以直接继承默认实现。

### 4. 多态的底层实现原理是什么？虚拟机是怎么运行多态这个过程的？

底层核心是**虚方法表 (vtable)**。类被 JVM 加载时，会在方法区为每个类生成一张虚方法表，记录各方法的实际内存地址。子类重写了父类方法，则指针指向子类的方法地址。运行时遇到 `invokevirtual` 字节码指令，JVM 提取对象的实际类型，查虚方法表执行对应代码。

### 5. 动态分派是什么？

动态分派是在**运行期**根据对象的实际类型来确定方法执行版本的分配过程。Java 的方法重写（Override）就是靠动态分派实现的，这也是多态的具体体现。与之对应的是静态分派（编译期确定），如方法重载（Overload）。

### 6. 介绍一下 static 和 final 关键字。

- **static**：全局共享。修饰变量（方法区）、方法（无需实例调用）、代码块（类加载执行一次）。静态方法内不能用 `this`。
- **final**：不可改变。修饰类（不能继承，如 String）、方法（不能被重写）、变量（常量，只能赋值一次）。

### 7. 什么是泛型？什么是泛型擦除？

泛型是参数化类型机制，编译时进行类型检查。**泛型擦除**：编译期间所有泛型信息被编译器抹除，替换为原始类型（通常是 Object）。

### 8. 泛型擦除之后，虚拟机是怎么运行的？

JVM 运行时不知道泛型的存在，处理的都是 Object。编译器在擦除后会自动插入 `checkcast` 指令（强制类型转换），并生成**桥接方法 (Bridge Method)** 解决泛型擦除导致的子类重写多态失效问题。

### 9. 介绍一下 Java 的异常体系。

顶层是 `java.lang.Throwable`，分两派：

1. **Error**：JVM 内部严重错误（OutOfMemoryError、StackOverflowError），应用程序不应捕获。
2. **Exception**：程序可处理。分为**受检异常**（IOException，编译期强制 try-catch）和**非受检异常**（RuntimeException，如 NullPointerException，编译期不检查）。

### 10. 你知道的 Error 有哪些类型？在运行时会不会出现 Error？

常见的有 `OutOfMemoryError`（堆内存耗尽）、`StackOverflowError`（递归太深栈满）、MetaspaceOOM。在运行时**会**出现，出现后导致所在线程崩溃，可能导致整个 JVM 进程退出。

### 11. 你熟悉的数据结构有哪些？

数组（连续内存查询快）、单/双向链表（插入删除快）、栈（LIFO）、队列（FIFO）、哈希表（Hash 散列，O(1)）、树（二叉搜索树、红黑树，O(log N)）。

### 12. CopyOnWriteArrayList 的底层实现原理是什么？底层一般用的什么锁？

原理是**写时复制**，底层使用 **ReentrantLock** 保证并发写安全。

内部数组被 `volatile` 修饰保证可见性。读操作完全无锁；写操作先加锁，拷贝出新数组，在新数组上修改，替换引用后释放锁。**只适合读多写少**场景，频繁写会高频数组拷贝导致 GC 压力。

### 13. ReentrantLock 的原理是什么？核心机制是什么？

ReentrantLock 是基于 API 的独占、可重入悲观锁。核心机制完全依赖 **AQS (AbstractQueuedSynchronizer)**。支持公平锁（排队）和非公平锁（先 CAS 抢一把，默认策略，性能更高）。

### 14. 什么是 AQS？AQS 的核心方法有哪些？

AQS 是 Java 并发包的基石。维护一个 `volatile int state`（0 空闲，1 占用）和一个基于**双向链表的 FIFO 等待队列**。

**核心方法**：

- `tryAcquire()`：尝试抢锁，底层使用 `compareAndSetState` (CAS) 将 state 从 0 改为 1。成功则设锁持有者为自己。
- `acquireQueued()`：抢不到锁，包装成 Node 加入队列尾部，调用 `LockSupport.park()` 挂起。
- `tryRelease()`：释放锁，state 减 1，归零则唤醒队列头节点的下一个节点。

```java
private volatile int state;

protected final boolean compareAndSetState(int expect, int update) {
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```

### 15. 什么是多线程？多线程有几种实现方式？

多线程是操作系统调度的最小单元。实现方式 4 种：继承 Thread、实现 Runnable、实现 Callable + FutureTask、使用 ExecutorService 线程池（**实战唯一推荐**）。

### 16. 线程有几种状态？运行态分几种状态？

`Thread.State` 定义 6 种：NEW、RUNNABLE、BLOCKED（等锁）、WAITING（无限期等待）、TIMED_WAITING（限期等待）、TERMINATED。

在操作系统层面，RUNNABLE 涵盖了**就绪态 (Ready)**（在就绪队列等 CPU 时间片）和**运行态 (Running)**（正在 CPU 上执行代码）。

### 17. 你知道 wait 和 notify 吗？请介绍一下。

Object 的 native 方法，用于线程间等待/通知。调用 `wait()` 时必须已持有该对象的 Monitor 锁。调用后线程**释放锁**进入 WaitSet 挂起。另一线程获取同一把锁后调用 `notify()` / `notifyAll()`，被挂起的线程才会重新竞争锁。

### 18. 哪个状态是不释放锁的？

`Thread.sleep()` 进入 TIMED_WAITING 状态时**不会释放锁**。此外线程尝试获取 synchronized 锁失败进入 BLOCKED 时，原先持有的其他锁也不会释放（死锁温床）。

### 19. 线程池有几种类型？

JDK Executors 工厂预设类型（**阿里规约禁止直接使用**）：

1. **FixedThreadPool**（固定线程数，无界队列：容易 OOM）。
2. **CachedThreadPool**（核心线程 0，最大线程无穷大：可能创建海量线程压垮 CPU）。
3. **SingleThreadExecutor**（单线程，无界队列：容易 OOM）。
4. **ScheduledThreadPool**（定时/周期任务）。

**正确做法**：通过 `ThreadPoolExecutor` 构造函数手动定义参数。

### 20. 线程池的核心参数有哪些？

7 个核心参数：

1. **corePoolSize**：核心线程数（常驻工人）。
2. **maximumPoolSize**：最大线程数（忙不过来时最多几个工人）。
3. **keepAliveTime**：临时工空闲多久被辞退。
4. **unit**：存活时间单位。
5. **workQueue**：任务阻塞队列。
6. **threadFactory**：线程工厂（给线程起有意义的名字）。
7. **handler**：拒绝策略。

### 21. 设置线程池大小需要注意什么条件？

- **CPU 密集型**：N(cpu) + 1。线程过多导致频繁上下文切换反而变慢。
- **I/O 密集型**：N(cpu) × 2，或更精确的 N(cpu) × (1 + WaitTime / ComputeTime)。

### 22. 线程池的淘汰（拒绝）策略是什么？

1. **AbortPolicy**（默认）：直接抛 RejectedExecutionException。
2. **CallerRunsPolicy**：由提交任务的线程亲自执行，起流量控制作用。
3. **DiscardPolicy**：静默丢弃最新任务。
4. **DiscardOldestPolicy**：丢弃队列最老任务，重新尝试提交。

---

## 面经四（补充高频题 · 偏 Java 基础 + 缓存策略 + JVM）

### 1. 登录和校验流程

**基于 Session 的传统方案**：

1. 用户提交账号密码 → 服务端校验通过 → 服务端创建 Session 对象（存用户信息），将 SessionID 通过 `Set-Cookie` 返回给浏览器。
2. 后续每次请求，浏览器自动携带 Cookie（含 SessionID） → 服务端根据 SessionID 查到 Session 对象 → 确认用户身份。
3. **问题**：Session 存在服务端内存，集群部署时存在 **Session 共享问题**（A 服务器创建的 Session，B 服务器找不到）。

**基于 Token/JWT 的现代方案**（项目中常用）：

1. 用户提交账号密码 → 服务端校验通过 → 服务端签发一个 **JWT（JSON Web Token）**，其中包含用户 ID 等信息，用密钥签名后返回给前端。
2. 前端存储 Token（LocalStorage），后续每次请求在 Header 中携带 `Authorization: Bearer <token>`。
3. 服务端通过**拦截器 (Interceptor)** 校验 Token 签名的合法性和是否过期 → 解析出用户信息放入 ThreadLocal → 放行请求。
4. **优势**：Token 本身携带信息，服务端无状态，天然支持集群部署，不需要 Session 共享。

### 2. 延迟双删和主动更新策略

**主动更新策略**（Cache Aside Pattern，最常用）：

- **读操作**：先查 Redis → 命中则直接返回；未命中则查 MySQL → 写入 Redis 并设置过期时间。
- **写操作**：**先更新数据库，再删除缓存**（不是更新缓存，因为更新缓存可能导致并发下的脏数据）。

**延迟双删**：为了解决"先更新 DB 再删缓存"在极端并发下仍可能出现短暂不一致的问题：

```
1. 删除缓存
2. 更新数据库
3. 延迟 500ms 后再次删除缓存（兜底，清除在步骤 1~2 期间被其他线程写入的旧缓存）
```

**为什么不用"先删缓存再更新 DB"？** 因为在高并发下，线程 A 删完缓存还没来得及更新 DB，线程 B 读缓存 miss 后查到旧数据写入缓存，导致长期不一致。

### 3. 缓存雪崩

**案发现场**：大量 Key 在同一时刻集体过期，或 Redis 服务器直接宕机。海量正常请求全部砸向 MySQL。

**解法**：

- **随机过期时间（防扎堆）**：统一过期时间基础上加 1~5 分钟随机值。
- **高可用架构（防宕机）**：搭建 Redis 哨兵模式 (Sentinel) 或集群模式 (Cluster)。
- **多级缓存**：本地缓存（Caffeine）+ Redis 分布式缓存。
- **限流降级**：Sentinel 或 Hystrix 对下游进行熔断保护。

### 4. 乐观锁和悲观锁。乐观锁版本号冲突怎么处理？

**悲观锁**：认为冲突多，先加锁再操作。实现：`synchronized`、`ReentrantLock`、MySQL `FOR UPDATE`。

**乐观锁**：认为冲突少，不加锁，更新时才检查。实现：版本号机制、CAS。

```sql
-- 乐观锁版本号实现
UPDATE product
SET stock = stock - 1, version = version + 1
WHERE id = #{id} AND version = #{version};
-- 返回 rows = 0 说明版本号不匹配（别人先改了）
```

**版本号冲突的处理方式**：

1. **重试**：捕获到更新失败（rows=0），重新查询最新数据和版本号，再次尝试更新（通常设置最大重试次数，如 3 次）。
2. **提示用户**：前端提示"数据已被修改，请刷新页面后重试"（适用于用户编辑场景）。
3. **转悲观锁**：先乐观尝试，多次失败后降级为悲观锁 `FOR UPDATE`。

### 5. 接口和抽象类的区别

| 对比项 | 接口 (Interface) | 抽象类 (Abstract Class) |
|--------|-----------------|----------------------|
| 多继承 | 可以实现**多个**接口 | 只能继承**一个**抽象类 |
| 方法实现 | Java 8 前只有抽象方法；Java 8 后可以有 `default` 和 `static` 方法 | 可以有抽象方法和普通方法 |
| 成员变量 | 只能有 `public static final` 常量 | 可以有各种类型的成员变量 |
| 构造方法 | 没有构造方法 | 有构造方法（供子类调用） |
| 设计语义 | "**能做什么**"（can-do），定义能力契约 | "**是什么**"（is-a），抽取共性模板 |

**选型口诀**：如果多个不相关的类需要同一种能力 → 用接口。如果多个相关的类有公共代码需要复用 → 用抽象类。

### 6. public、private、protected 的区别

| 修饰符 | 同一个类 | 同一个包 | 子类 | 不同包 |
|--------|---------|---------|------|--------|
| public | ✓ | ✓ | ✓ | ✓ |
| protected | ✓ | ✓ | ✓ | ✗ |
| (default/包访问) | ✓ | ✓ | ✗ | ✗ |
| private | ✓ | ✗ | ✗ | ✗ |

**记忆口诀**：public 全开放，private 全封闭，protected 比 default 多了一个"子类可见"。

### 7. 什么是方法重载（Overload）？和重写（Override）的区别？

**重载 (Overload)**：同一个类中，方法名相同，但**参数列表不同**（参数个数、类型或顺序不同）。返回值类型可以不同，但不能仅靠返回值区分重载。编译期确定调用哪个版本（**静态分派**）。

```java
public int add(int a, int b) { return a + b; }
public double add(double a, double b) { return a + b; }  // 重载
public int add(int a, int b, int c) { return a + b + c; } // 重载
```

**重写 (Override)**：子类重新定义父类的同名同参数方法。运行期根据对象实际类型确定调用版本（**动态分派**，多态的基础）。

### 8. ArrayList 实现循环遍历有几种方式？

```java
List<String> list = Arrays.asList("A", "B", "C");

// 1. 普通 for 循环（按下标访问，ArrayList 友好）
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}

// 2. 增强 for-each（底层是 Iterator）
for (String s : list) {
    System.out.println(s);
}

// 3. Iterator 迭代器（支持安全删除）
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    System.out.println(it.next());
}

// 4. Lambda + forEach（Java 8+）
list.forEach(s -> System.out.println(s));

// 5. Stream 流（Java 8+）
list.stream().forEach(System.out::println);

// 6. ListIterator（支持双向遍历和修改）
ListIterator<String> lit = list.listIterator();
while (lit.hasNext()) {
    System.out.println(lit.next());
}
```

**避坑**：在遍历中删除元素，只能用方式 3 的 `it.remove()`，使用方式 2 的 for-each 直接 `list.remove()` 会抛 `ConcurrentModificationException`。

### 9. JVM 双亲委派模型

**类加载器层级**（从上到下）：

1. **启动类加载器 (Bootstrap ClassLoader)**：加载 `jre/lib` 下的核心类（如 `java.lang.String`），C++ 实现。
2. **扩展类加载器 (Extension ClassLoader)**：加载 `jre/lib/ext` 下的扩展类。
3. **应用类加载器 (Application ClassLoader)**：加载我们自己写的类（classpath 下的类）。
4. **自定义类加载器**：用户自己实现，如热部署、加密 class 等场景。

**双亲委派机制**：当一个类加载器收到加载请求时，**先委托给父类加载器去加载**，父类加载器也继续向上委托。只有当父类加载器加载不了（在自己的搜索范围内找不到该类），子类加载器才自己动手加载。

**为什么要这么做？** 保证 Java 核心类的安全性和唯一性。例如你自己写了一个 `java.lang.String` 类，双亲委派机制保证最终加载的一定是 JDK 自带的 String，而不是你的"冒牌货"，防止核心 API 被篡改。

```
              Bootstrap ClassLoader
                      ↑ 委派
              Extension ClassLoader
                      ↑ 委派
              Application ClassLoader
                      ↑ 委派
              自定义 ClassLoader
```

### 10. ThreadLocal 的作用

ThreadLocal 为每个线程提供一个**独立的变量副本**，实现线程隔离，避免了多线程共享变量的线程安全问题。

**底层原理**：每个 Thread 对象内部有一个 `ThreadLocalMap`（类似 HashMap）。调用 `threadLocal.set(value)` 时，以这个 ThreadLocal 实例作为 Key，value 作为 Value，存入当前线程自己的 ThreadLocalMap 中。不同线程各存各的，互不干扰。

**典型使用场景**：

1. **存储当前登录用户信息**：拦截器校验完 Token 后，把用户信息 `set` 到 ThreadLocal 中。Controller/Service 层任何地方都可以通过 `get()` 直接拿到当前用户，不用层层传参。
2. **数据库连接管理**：Spring 事务管理中，通过 ThreadLocal 保证同一个线程内的多个 DAO 操作使用同一个 Connection。
3. **日期格式化**：`SimpleDateFormat` 线程不安全，可以用 ThreadLocal 给每个线程一个独立实例。

```java
// 实战：存储当前登录用户
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user) { tl.set(user); }
    public static UserDTO getUser() { return tl.get(); }
    public static void removeUser() { tl.remove(); } // 避坑：必须 remove 防止内存泄漏！
}
```

**避坑（内存泄漏）**：ThreadLocal 的 Key 是弱引用，GC 后 Key 变成 null，但 Value 是强引用不会被回收。如果线程池中线程长期存活，这些"Key 为 null 的 Value"会一直占用内存。所以**必须在请求结束时调用 `remove()`**（通常在拦截器的 `afterCompletion` 中执行）。

---

## 面经五（技术面 · 偏 Redis 深度 + MySQL + 项目）

### 1. Redis 持久化策略

1. **RDB (快照)**：定时把内存数据 dump 到 .rdb 文件。文件小、恢复快，但两次快照间隙断电会丢数据。
2. **AOF (追加日志)**：每个写命令追加到日志文件。配置 `everysec` 最多丢 1 秒数据。
3. **混合持久化 (Redis 4.0+)**：AOF 重写时先写 RDB 格式 + 后续增量 AOF。

### 2. 哪种策略会丢失消息？

- **RDB 会丢**：两次快照之间（比如 5 分钟间隔）如果断电，这期间的数据全丢。
- **AOF everysec 也可能丢**：最多丢最近 1 秒的数据（刷盘前断电）。
- **AOF always 几乎不丢**：每条写命令都立即刷盘，但性能最差。

### 3. 哪种策略数据恢复更快？

**RDB 恢复更快**。RDB 是紧凑的二进制文件，加载时直接映射到内存。而 AOF 恢复需要**逐条重放**日志中的写命令，文件越大恢复越慢。

### 4. AOF 和 RDB 结合（混合持久化）会丢失消息吗？

仍然**可能丢极少量数据**（最多 1 秒左右），因为增量部分使用的还是 AOF everysec 策略。但比单独用 RDB 安全得多，比单独用 AOF 恢复快得多。这是 Redis 4.0+ 的**推荐方案**。

### 5. 介绍一下 Redis 中的 String、Hash、List 的应用场景。

| 类型 | 底层结构 | 典型应用场景 |
|------|----------|------------|
| **String** | SDS 动态字符串 | 缓存 JSON 对象、计数器（`INCR`）、分布式锁（`SET NX`）、验证码、限流（滑动窗口计数） |
| **Hash** | ziplist / hashtable | 存储对象（`HSET user:1001 name "张三"`），支持字段级读写（只改年龄不用读取整个 JSON） |
| **List** | ziplist / quicklist | 消息队列（LPUSH + BRPOP）、最新列表（如最新评论、最新文章）、任务队列 |

### 6. Hash 适合范围查询吗？

**不适合**。Hash 内部是哈希表（大数据量时）或压缩列表（小数据量时），**没有排序能力**。无法按 field 值做范围查询。如果需要按某个属性的范围查询，应该用 **Zset（Sorted Set）**，它的跳表天然有序，`ZRANGEBYSCORE` 可以高效地做范围查询。

### 7. List 适合范围查询吗？

**部分适合**。List 支持按下标范围获取（`LRANGE key 0 9` 取前 10 个元素），但这是按**位置下标**范围取，不是按**值**的大小范围。如果需要按 score/值做范围查询，还是得用 **Zset**。

### 8. MySQL 索引了解吗？

InnoDB 默认使用 **B+ 树**索引。非叶子节点只存索引不存数据，叶子节点通过双向链表相连，范围查询极快。分为聚簇索引（主键索引，叶子存完整行数据）和非聚簇索引（二级索引，叶子存主键 ID，查完整数据需要回表）。

### 9. 根据某个字段查询表，但查询时间长，可能原因是什么？

1. **没有建索引**：该字段上没有索引，导致全表扫描（`type = ALL`）。
2. **索引失效**：虽然有索引但没走上——在索引列上使用了函数、发生了隐式类型转换、左模糊匹配 `LIKE '%xxx'`、违背最左前缀法则等。
3. **回表次数过多**：`SELECT *` 导致二级索引查完后还要频繁回到聚簇索引取数据。应改用覆盖索引。
4. **数据量过大**：单表数据量超千万，即使走了索引也可能慢。考虑分库分表。
5. **锁等待**：其他事务对该行/表加了排他锁，当前查询在排队等锁。
6. **深度分页**：`LIMIT 1000000, 10` 需要先扫过 100 万行再丢掉，可用子查询延迟关联优化。

### 10. 缓存穿透、缓存击穿、缓存雪崩，你认为哪个最好解决？哪个最不好解决？

**最好解决的：缓存穿透**。因为方案成熟且简单——缓存空对象（几行代码）或布隆过滤器（引入一个数据结构）就能有效拦截。且穿透通常是恶意攻击或参数异常，防御手段明确。

**最不好解决的：缓存雪崩**。因为它的触发条件多样（大量 Key 同时过期、Redis 宕机），影响面最广（不是一个 Key 的问题，是整体缓存层失效）。需要从多个层面综合防御：随机过期时间 + Redis 高可用集群 + 多级缓存 + 限流降级熔断，没有银弹，且运维复杂度高。

缓存击穿介于两者之间：针对热点 Key 用互斥锁或逻辑过期即可，方案确定但要考虑锁粒度和性能。

### 11. 有了 MySQL，Redis 是来存储什么的？Redis 中的数据 MySQL 还要有吗？

Redis 主要存储**热点数据的副本**（缓存）和**临时性数据**。

- **缓存热点数据**：商品详情、用户信息等高频读取的数据。MySQL 中**必须有**原始数据，Redis 只是一份加速读取的副本。Redis 挂了或数据过期后，会从 MySQL 重新加载。
- **纯 Redis 数据（MySQL 可以没有）**：Session/Token、验证码、限流计数器、分布式锁、排行榜等临时性或实时性数据，这些数据生命周期短或不需要持久化到数据库。

**核心关系**：Redis 是缓存层（快但容量小），MySQL 是持久层（慢但可靠）。数据以 MySQL 为准，Redis 为加速手段。

### 12. Redis 为什么快？

1. **基于内存**：省去磁盘 IO。
2. **高效数据结构**：全局 Hash 表 O(1)、跳表、SDS。
3. **单线程模型**：无锁竞争、无上下文切换开销。
4. **I/O 多路复用（epoll）**：单线程并发处理上万连接。

### 13. MySQL 的数据也在内存中（Buffer Pool），那 Redis 还有必要存在吗？

**有必要，原因有三**：

1. **MySQL Buffer Pool 有限且易被淘汰**：Buffer Pool 默认 128MB（可调大），但远不能把所有数据缓存住。冷数据查询会触发磁盘 IO 把热数据从 Buffer Pool 挤走，导致后续热数据查询也要读磁盘。Redis 是专门的缓存层，可以精确控制缓存哪些热数据。
2. **MySQL 每次查询有协议解析、SQL 解析、优化器、执行器等开销**：即使数据在 Buffer Pool 里，一次查询也要经过 TCP 连接管理、SQL 解析、查询优化、权限校验等一系列步骤。Redis 的命令极其轻量，直接内存键值查找，少了很多中间环节。
3. **并发能力差距巨大**：MySQL 的连接数有限（默认 151），且每个连接都有线程开销。Redis 单线程 + epoll 可以轻松处理 10 万+ QPS，MySQL 即使数据在内存中也很难达到这个吞吐量。在高并发场景下，Redis 挡在前面能**保护 MySQL 不被打爆**。

---

> **文档版本**：v2.0
> **内容来源**：五场真实面试题目整理
> **详细解析**：请参考 Interview.md 完整版
