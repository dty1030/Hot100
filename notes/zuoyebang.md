# 作业帮暑期实习后端开发面经全集

> **更新时间：** 2026年4月  
> **岗位：** Java后端开发暑期实习  
> **难度评级：** ⭐⭐⭐⭐⭐ (高频考点密集，深度较深)  
> **面试轮次：** 一面技术面

---

## 📋 目录

1. [Java基础与集合框架](#java基础与集合框架)
2. [JVM与垃圾回收](#jvm与垃圾回收)
3. [MySQL数据库](#mysql数据库)
4. [Redis缓存与消息队列](#redis缓存与消息队列)
5. [网络协议](#网络协议)
6. [操作系统](#操作系统)
7. [算法与数据结构](#算法与数据结构)
8. [项目深度挖掘](#项目深度挖掘)
9. [面试策略与技巧](#面试策略与技巧)

---

## Java基础与集合框架

### Q1：HashMap和ConcurrentHashMap实现区别？

**考点：** 并发安全、线程安全的Map实现

**核心对比：**

| 对比维度 | HashMap | ConcurrentHashMap |
|---------|---------|-------------------|
| **线程安全** | ❌ 不安全 | ✅ 线程安全 |
| **底层结构** | 数组 + 链表 + 红黑树 | 数组 + 链表 + 红黑树 |
| **JDK7锁机制** | 无锁 | 分段锁（Segment，16个） |
| **JDK8锁机制** | 无锁 | CAS + synchronized（锁桶） |
| **并发度** | 1（单线程） | JDK7: 16 / JDK8: 数组长度 |
| **null支持** | key和value都可为null | 都不允许null |
| **扩容** | 单线程扩容 | 多线程协助扩容 |
| **性能** | 单线程最优 | 高并发场景优于Hashtable |

**JDK8 ConcurrentHashMap核心改进：**

```java
// 1. 取消Segment分段锁，降低锁粒度
// 之前：16个Segment，每个Segment一把锁
// 现在：直接锁数组的每个桶（Node）

// 2. put操作：CAS + synchronized
final V putVal(K key, V value, boolean onlyIfAbsent) {
    int hash = spread(key.hashCode());
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f;
        int n, i, fh;
        
        // 情况1：桶为空，CAS插入
        if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null)))
                break;  // CAS成功，无需加锁
        }
        // 情况2：桶不为空，synchronized锁头节点
        else {
            synchronized (f) {  // 只锁这一个桶
                // 链表插入或树节点插入
            }
        }
    }
}

// 3. get操作：完全无锁
// volatile保证可见性
transient volatile Node<K,V>[] table;

public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    // 直接读取，不加锁
    return (e = tabAt(tab, (n - 1) & h)) != null ? e.val : null;
}
```

**为什么取消Segment？**

```
JDK7 分段锁：
- 并发度固定为16（Segment数量）
- 即使有100个线程，最多也只能16个同时操作
- 锁粒度还是太粗

JDK8 桶级锁：
- 并发度 = 数组长度（可以很大）
- 100个线程可以同时操作100个不同的桶
- 锁粒度更细，并发性能更好
```

---

### Q2：ConcurrentHashMap中分段锁有什么优势（为什么要用它）？

**考点：** 并发控制、锁优化

**分段锁设计思想（JDK7）：**

```
传统方案（Hashtable）：
整个Map一把大锁
   ↓
100个线程只有1个能操作
   ↓
并发度 = 1


分段锁方案（JDK7 ConcurrentHashMap）：
将Map分成16个Segment
每个Segment一把锁
   ↓
100个线程，最多16个同时操作不同Segment
   ↓
并发度 = 16（提升16倍）
```

**图解分段锁：**

```
ConcurrentHashMap
├── Segment[0] 🔒 → [桶1, 桶2, 桶3, ...]
├── Segment[1] 🔒 → [桶1, 桶2, 桶3, ...]
├── Segment[2] 🔒 → [桶1, 桶2, 桶3, ...]
└── ...
└── Segment[15] 🔒 → [桶1, 桶2, 桶3, ...]

线程A操作Segment[0]  ✓ 可以同时
线程B操作Segment[5]  ✓ 可以同时
线程C操作Segment[0]  ✗ 需要等待线程A
```

**性能对比：**

| 方案 | 锁粒度 | 并发度 | 适用场景 |
|------|--------|--------|----------|
| **Hashtable** | 整个表 | 1 | 低并发 |
| **JDK7 ConcurrentHashMap** | Segment级 | 16 | 中等并发 |
| **JDK8 ConcurrentHashMap** | 桶级 | 数组长度 | 高并发 |

**JDK8进一步优化：**

```
取消Segment → 直接锁桶（Node）
   ↓
并发度 = 数组长度（默认16，可扩容到很大）
   ↓
+ CAS无锁操作（桶为空时）
   ↓
性能进一步提升
```

**代码示例：**

```java
// JDK8实现
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// 10个线程同时put，key的hash值分散在不同桶
// 理想情况下，10个线程可以完全并发执行
ExecutorService executor = Executors.newFixedThreadPool(10);
for (int i = 0; i < 10; i++) {
    final int id = i;
    executor.submit(() -> {
        map.put("key" + id, id);  // 锁不同的桶，互不影响
    });
}
```

---

### Q3：CAP、Copy-On-Write（写时复制）？

**考点：** 并发容器、分布式理论

#### Copy-On-Write（写时复制）

**核心思想：** 读写分离，写时复制

```java
public class CopyOnWriteArrayList<E> {
    private volatile transient Object[] array;
    
    // 读操作：不加锁，直接读
    public E get(int index) {
        return get(getArray(), index);  // 无锁读
    }
    
    // 写操作：加锁，复制数组
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();  // 写操作加锁
        try {
            Object[] elements = getArray();
            int len = elements.length;
            
            // 复制原数组，长度+1
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;  // 添加新元素
            
            setArray(newElements);  // 替换原数组
            return true;
        } finally {
            lock.unlock();
        }
    }
}
```

**工作流程：**

```
初始状态：array = [A, B, C]
         ↓
线程1读取：直接读array，无需加锁  ← 快
线程2读取：直接读array，无需加锁  ← 快
         ↓
线程3写入D：
  1. 加锁
  2. 复制 [A, B, C] → [A, B, C, D]
  3. array指向新数组
  4. 解锁
         ↓
线程1、2继续读旧数组 [A, B, C]  ← 不受影响
新来的读操作读新数组 [A, B, C, D]
```

**优缺点：**

| 优点 | 缺点 |
|------|------|
| ✅ 读操作完全无锁，性能极高 | ❌ 写操作需要复制数组，耗时耗内存 |
| ✅ 读写分离，读不阻塞 | ❌ 不适合写多的场景 |
| ✅ 线程安全 | ❌ 数据可能不是最新的（弱一致性） |

**适用场景：**

```java
// ✅ 适合：读多写少
// 1. 系统配置信息
CopyOnWriteArrayList<Config> configs = new CopyOnWriteArrayList<>();
// 配置很少改变，但频繁读取

// 2. 黑名单、白名单
CopyOnWriteArraySet<String> blacklist = new CopyOnWriteArraySet<>();
// 黑名单更新不频繁，但查询频繁

// 3. 监听器列表
CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
// 监听器注册后很少变化

// ❌ 不适合：写多的场景
// 每次写都要复制整个数组，性能差
```

---

#### CAP理论

**定义：** 分布式系统的三个特性，只能同时满足两个

| 特性 | 全称 | 含义 |
|------|------|------|
| **C** | Consistency | 一致性：所有节点同一时间看到相同数据 |
| **A** | Availability | 可用性：每个请求都能得到响应（成功或失败） |
| **P** | Partition tolerance | 分区容错性：系统在网络分区时仍能工作 |

**为什么只能选两个？**

```
场景：分布式数据库，两个节点
节点A ←—— 网络分区 ——→ 节点B

用户写入A：age = 30
但网络分区，无法同步到B

此时用户查询B：
- 选择C（一致性）：拒绝查询，等待同步 → 失去A（可用性）
- 选择A（可用性）：返回旧数据age=20 → 失去C（一致性）

P（分区容错）是必须的，因为网络随时可能分区
所以实际是在C和A之间权衡
```

**常见选择：**

| 系统类型 | 选择 | 示例 |
|---------|------|------|
| **CP** | 一致性 + 分区容错 | ZooKeeper、HBase、Redis Cluster |
| **AP** | 可用性 + 分区容错 | Cassandra、DynamoDB、Eureka |
| **CA** | 一致性 + 可用性 | 单机数据库（MySQL、PostgreSQL） |

**项目中的应用：**

```
我的项目选择AP（可用性优先）：
- 使用Redis缓存，即使数据库挂了，缓存仍可用
- 采用最终一致性：缓存可能短暂不一致，但最终会同步
- 牺牲强一致性，换取高可用
```

---

## JVM与垃圾回收

### Q1：Java中的GC介绍一下

**考点：** 垃圾回收机制、GC算法、垃圾收集器

**答题框架（4个层次）：**

#### 1️⃣ 如何判断对象是垃圾？

| 算法 | 原理 | 优点 | 缺点 | Java采用 |
|------|------|------|------|----------|
| **引用计数** | 对象被引用+1，失效-1 | 简单、高效 | 无法解决循环引用 | ❌ |
| **可达性分析** | 从GC Roots出发，不可达即垃圾 | 解决循环引用 | 需要STW | ✅ |

**GC Roots包括：**

```
1. 虚拟机栈中引用的对象（局部变量）
2. 方法区中静态属性引用的对象
3. 方法区中常量引用的对象
4. 本地方法栈中引用的对象
5. 被synchronized持有的对象
```

**可达性分析示例：**

```
GC Roots → objA → objB
              ↓
            objC → objD（循环引用，但从GC Roots可达）

objE → objF（相互引用，但从GC Roots不可达）
  ↑      ↓
  └──────┘

结果：
- objA、B、C、D：可达，不是垃圾
- objE、F：不可达，是垃圾（即使循环引用）
```

---

#### 2️⃣ 垃圾回收算法

| 算法 | 原理 | 优点 | 缺点 | 适用区域 |
|------|------|------|------|----------|
| **标记-清除** | 标记垃圾→清除 | 简单 | 产生碎片 | 老年代 |
| **标记-整理** | 标记垃圾→移动存活对象 | 无碎片 | 慢（需移动） | 老年代 |
| **复制算法** | 复制存活对象到另一块区域 | 快、无碎片 | 浪费空间 | 新生代 |
| **分代收集** | 新生代用复制，老年代用标记整理 | 综合最优 | 复杂 | 主流 |

**图解垃圾回收算法：**

```
标记-清除：
[A][B][C][D][E] → 标记B、D是垃圾 → [A][ ][C][ ][E]
问题：产生内存碎片

标记-整理：
[A][B][C][D][E] → 标记B、D → [A][C][E][ ][ ]
优点：内存连续，无碎片

复制算法：
区域1: [A][B][C][D][E]  →  区域2: [A][C][E]
      (B、D是垃圾)             (只复制存活对象)
优点：快速，无碎片
缺点：浪费50%空间
```

**分代收集（重点）：**

```
堆内存划分：
新生代（Young Generation）
├── Eden区（8/10）
└── Survivor区（2/10）
    ├── From（1/10）
    └── To（1/10）

老年代（Old Generation）


工作流程：
1. 新对象在Eden区创建
2. Eden满了 → Minor GC
   - 存活对象复制到Survivor
   - Eden清空
3. 对象在Survivor区来回复制（From ↔ To）
4. 存活超过15次 → 晋升到老年代
5. 老年代满了 → Full GC
```

---

#### 3️⃣ 垃圾收集器

**新生代收集器：**

| 收集器 | 特点 | 适用 |
|--------|------|------|
| **Serial** | 单线程，STW | 客户端模式 |
| **ParNew** | Serial多线程版 | 配合CMS |
| **Parallel Scavenge** | 多线程，吞吐量优先 | 后台任务 |

**老年代收集器：**

| 收集器 | 特点 | 适用 |
|--------|------|------|
| **Serial Old** | 单线程，标记-整理 | 客户端模式 |
| **Parallel Old** | Parallel Scavenge老年代版 | 吞吐量优先 |
| **CMS** | 并发标记清除，低延迟 | 互联网应用 |

**全堆收集器：**

| 收集器 | JDK版本 | 特点 | STW时间 |
|--------|---------|------|---------|
| **G1** | JDK 9默认 | 分区收集，可预测停顿 | 10-200ms |
| **ZGC** | JDK 11+ | 超低延迟 | <1ms |
| **Shenandoah** | JDK 12+ | 低延迟 | <10ms |

**G1收集器（重点）：**

```
特点：
1. 将堆分成多个Region（默认2048个）
2. 每个Region可以是Eden、Survivor、Old
3. 优先回收垃圾最多的Region（Garbage First）
4. 可设置停顿时间目标（如：-XX:MaxGCPauseMillis=200）

适用场景：
- 堆内存较大（>4GB）
- 需要可预测的GC停顿时间
- 互联网应用、微服务
```

---

#### 4️⃣ GC调优

**常用JVM参数：**

```bash
# 堆内存设置
-Xms4g          # 初始堆大小
-Xmx4g          # 最大堆大小（建议与Xms相同，避免动态扩容）

# 新生代设置
-Xmn2g          # 新生代大小
-XX:SurvivorRatio=8  # Eden:Survivor = 8:2

# 垃圾收集器
-XX:+UseG1GC    # 使用G1收集器
-XX:MaxGCPauseMillis=200  # 最大停顿时间200ms

# GC日志
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xloggc:/var/log/gc.log
```

**GC调优步骤：**

```
1. 监控GC情况
   - jstat -gc <pid> 1000  # 每秒输出GC统计
   - 查看GC日志

2. 分析问题
   - Full GC频繁？ → 老年代不足
   - Minor GC频繁？ → 新生代太小
   - GC时间长？ → 堆太大或算法不合适

3. 调整参数
   - 增加堆内存
   - 调整新生代/老年代比例
   - 更换GC收集器

4. 验证效果
   - 压测
   - 监控线上表现
```

---

**完整回答模板：**

```
Java的GC主要包括四个方面：

1. 判断垃圾：
   通过可达性分析，从GC Roots出发，标记可达对象

2. 回收策略：
   采用分代收集
   - 新生代：复制算法，快速回收
   - 老年代：标记-整理，避免碎片

3. 垃圾收集器：
   我们项目使用G1收集器
   - 可预测停顿时间
   - 适合大堆内存
   - 停顿时间稳定在100ms左右

4. 调优经验：
   - 设置-Xms和-Xmx相同，避免动态扩容
   - 根据GC日志调整新生代大小
   - 监控Full GC频率，避免频繁Full GC
```

---

## MySQL数据库

### Q1：数据表找哪些字段做索引？

**考点：** 索引设计原则、查询优化

**索引设计5大原则：**

#### 1️⃣ WHERE子句中的字段

```sql
-- ✅ 适合建索引
SELECT * FROM user WHERE phone = '13800138000';
-- phone经常出现在WHERE中，建索引

-- ❌ 不需要索引
SELECT * FROM user;
-- 没有WHERE条件，索引无用
```

#### 2️⃣ JOIN的关联字段

```sql
-- ✅ 两个表的关联字段都要建索引
SELECT u.*, o.* 
FROM user u
JOIN `order` o ON u.id = o.user_id;

-- user表：id（主键自带索引）
-- order表：user_id需要建索引
CREATE INDEX idx_user_id ON `order`(user_id);
```

#### 3️⃣ ORDER BY / GROUP BY的字段

```sql
-- ✅ 经常排序的字段建索引
SELECT * FROM user ORDER BY create_time DESC;
CREATE INDEX idx_create_time ON user(create_time);

-- ✅ 分组字段建索引
SELECT status, COUNT(*) FROM user GROUP BY status;
CREATE INDEX idx_status ON user(status);
```

#### 4️⃣ 区分度高的字段

```sql
-- ✅ 区分度高（推荐）
phone（手机号：10^11种）
email（邮箱：无限种）
id_card（身份证：10^18种）

-- ❌ 区分度低（不推荐）
gender（性别：只有2种）
status（状态：3-5种）
```

**计算区分度：**

```sql
-- 区分度 = 不同值数量 / 总行数
SELECT COUNT(DISTINCT phone) / COUNT(*) FROM user;
-- 结果接近1，区分度高，适合建索引

SELECT COUNT(DISTINCT gender) / COUNT(*) FROM user;
-- 结果接近0，区分度低，不适合建索引
```

#### 5️⃣ 覆盖索引（避免回表）

```sql
-- 查询：只需要id、name、age
SELECT id, name, age FROM user WHERE name = '张三';

-- 建联合索引：(name, age)
CREATE INDEX idx_name_age ON user(name, age);

-- 好处：
-- 1. name在索引中，可以快速定位
-- 2. age也在索引中，不需要回表
-- 3. id是主键，索引中自动包含
-- → 完全不需要回表，性能最优
```

---

**不适合建索引的情况：**

| 场景 | 原因 | 示例 |
|------|------|------|
| **数据量小** | 全表扫描更快 | 表只有100行 |
| **区分度低** | 索引效果差 | 性别字段 |
| **频繁更新** | 维护成本高 | 计数器字段 |
| **大字段** | 索引占用空间大 | TEXT、BLOB |
| **很少查询** | 浪费资源 | 备注字段 |

---

**实战示例：**

```sql
-- 用户表设计
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- 主键索引（自动）
    phone VARCHAR(20) NOT NULL,               -- 登录字段
    email VARCHAR(50),
    name VARCHAR(50),
    age INT,
    gender TINYINT,                           -- 0男1女
    status TINYINT,                           -- 0正常1禁用
    create_time DATETIME,
    update_time DATETIME,
    
    UNIQUE KEY uk_phone (phone),              -- 唯一索引（登录）
    KEY idx_name_age (name, age),             -- 联合索引（覆盖查询）
    KEY idx_status_time (status, create_time) -- 联合索引（管理后台查询）
) ENGINE=InnoDB;

-- 索引说明：
-- 1. uk_phone：登录时查询
-- 2. idx_name_age：覆盖查询 SELECT id, name, age WHERE name = ?
-- 3. idx_status_time：后台查询 WHERE status = ? ORDER BY create_time
-- 4. gender不建索引：区分度太低
```

---

### Q2：普通索引和主键索引有什么区别？

**考点：** 聚簇索引、二级索引、回表

**核心区别：**

| 对比项 | 主键索引（聚簇索引） | 普通索引（二级索引） |
|--------|---------------------|---------------------|
| **英文名** | Clustered Index | Secondary Index |
| **叶子节点** | 存储完整行数据 | 存储索引列值 + 主键值 |
| **数量限制** | 一张表只有一个 | 可以有多个 |
| **查询效率** | 一次查询即可 | 可能需要回表 |
| **数据存储** | 数据按主键顺序物理存储 | 单独的B+树 |
| **是否必须** | InnoDB必须有 | 可选 |

---

**图解索引结构：**

```
表数据：
id  name   age  city
1   张三   25   北京
2   李四   30   上海
5   王五   28   深圳

主键索引（id）：
       2
      / \
     1   5
叶子节点：
[id=1, name=张三, age=25, city=北京]  ← 完整数据
[id=2, name=李四, age=30, city=上海]
[id=5, name=王五, age=28, city=深圳]


普通索引（name）：
     李四
    /   \
  张三   王五
叶子节点：
[name=张三, id=1]  ← 只有索引列和主键
[name=李四, id=2]
[name=王五, id=5]
```

---

**什么是回表？**

```sql
-- 查询：
SELECT * FROM user WHERE name = '张三';

执行过程：
1. 在name索引树上找到 name='张三' → 得到 id=1
2. 拿着 id=1 去主键索引树 → 查询完整数据
   ↑ 这一步就叫"回表"（回到主键索引表查数据）

-- 回表的代价：
- 需要两次B+树查询
- IO次数增加
- 性能下降
```

**如何避免回表？** → **覆盖索引**

```sql
-- 方式1：只查询索引包含的列
SELECT id, name FROM user WHERE name = '张三';
-- name索引叶子节点有：name和id
-- 不需要回表！

-- 方式2：建联合索引
CREATE INDEX idx_name_age ON user(name, age);

SELECT id, name, age FROM user WHERE name = '张三';
-- 索引包含：name、age、id（主键自动包含）
-- 完全覆盖，不需要回表！
```

---

**为什么InnoDB必须有主键索引？**

```
InnoDB是索引组织表：
- 数据必须按某个索引组织
- 如果没有指定主键，InnoDB会：
  1. 找第一个非NULL的唯一索引作为主键
  2. 如果没有，自动创建一个隐藏的6字节rowid作为主键

所以，最好显式指定主键
```

---

**MyISAM的索引结构（对比）：**

```
MyISAM：
- 主键索引和普通索引都是非聚簇的
- 叶子节点存储：索引值 + 数据行地址
- 数据和索引分开存储

InnoDB：
- 主键索引是聚簇的（数据和索引在一起）
- 普通索引是非聚簇的（存储主键值）
```

---

### Q3：WHERE后的字段是索引但是还是很慢？

**考点：** 索引失效、查询优化

**索引失效的7大场景：**

#### 1️⃣ 索引列上使用函数

```sql
-- ❌ 索引失效
SELECT * FROM user WHERE YEAR(create_time) = 2024;
-- 原因：对索引列使用了YEAR()函数，无法使用索引

-- ✅ 改写
SELECT * FROM user 
WHERE create_time >= '2024-01-01' 
  AND create_time < '2025-01-01';
```

#### 2️⃣ 隐式类型转换

```sql
-- 表结构：phone VARCHAR(20)

-- ❌ 索引失效
SELECT * FROM user WHERE phone = 13800138000;
-- 原因：phone是字符串，传入数字，MySQL会转换：
-- WHERE CAST(phone AS signed int) = 13800138000
-- 对索引列使用了函数，失效

-- ✅ 正确写法
SELECT * FROM user WHERE phone = '13800138000';
```

#### 3️⃣ 前缀模糊查询

```sql
-- ❌ 索引失效
SELECT * FROM user WHERE name LIKE '%张%';
SELECT * FROM user WHERE name LIKE '%三';

-- ✅ 可以使用索引
SELECT * FROM user WHERE name LIKE '张%';

-- 原因：
-- B+树是有序的：张三、张四、张五...
-- '张%' 可以定位起始位置
-- '%张%' 无法定位，只能全表扫描
```

#### 4️⃣ 联合索引不满足最左前缀

```sql
-- 索引：idx(a, b, c)

-- ✅ 使用索引
WHERE a = 1                    -- 使用idx(a)
WHERE a = 1 AND b = 2          -- 使用idx(a, b)
WHERE a = 1 AND b = 2 AND c = 3 -- 使用idx(a, b, c)
WHERE a = 1 AND c = 3          -- 使用idx(a)，c无法用

-- ❌ 不使用索引
WHERE b = 2                    -- 缺少a
WHERE c = 3                    -- 缺少a、b
WHERE b = 2 AND c = 3          -- 缺少a

-- 记忆：必须从最左边开始，且不能跳过
```

#### 5️⃣ OR条件中有非索引列

```sql
-- 索引：idx_name

-- ❌ 索引失效
SELECT * FROM user WHERE name = '张三' OR age = 25;
-- 原因：age没有索引，MySQL认为全表扫描更快

-- ✅ 改写方案1：给age也建索引
CREATE INDEX idx_age ON user(age);

-- ✅ 改写方案2：改用UNION
SELECT * FROM user WHERE name = '张三'
UNION
SELECT * FROM user WHERE age = 25;
```

#### 6️⃣ 不等于（!=、<>）

```sql
-- ❌ 可能索引失效
SELECT * FROM user WHERE status != 1;

-- 原因：
-- 如果status大部分都是1，那么 !=1 会返回大部分数据
-- MySQL优化器认为全表扫描更快

-- ✅ 改写
SELECT * FROM user WHERE status IN (0, 2, 3);
```

#### 7️⃣ IS NULL / IS NOT NULL

```sql
-- ❌ 可能索引失效
SELECT * FROM user WHERE phone IS NULL;

-- MySQL 5.7+：可以使用索引
-- 早期版本：不能使用索引

-- 建议：
-- 1. 字段设计时避免NULL
-- 2. 使用默认值代替NULL
```

---

**其他导致慢的原因：**

#### 回表次数太多

```sql
-- 即使走了索引，但返回大量数据，回表次数多
SELECT * FROM user WHERE age > 18;
-- 如果90%的用户都>18，走索引反而慢
-- MySQL优化器可能选择全表扫描

-- 解决：使用覆盖索引
SELECT id, name, age FROM user WHERE age > 18;
```

#### 索引区分度低

```sql
-- 索引：idx_status
SELECT * FROM user WHERE status = 1;

-- 如果status只有0和1，区分度很低
-- 即使走索引，效果也不好
```

---

**排查方法：**

```sql
-- 使用EXPLAIN分析
EXPLAIN SELECT * FROM user WHERE name = '张三';

-- 重点关注：
type:    -- 查询类型
  - ALL：全表扫描（最差）
  - index：索引扫描
  - range：范围扫描
  - ref：索引查找
  - const：主键/唯一索引（最优）

key:     -- 实际使用的索引
  - NULL：没使用索引
  - idx_name：使用了idx_name索引

rows:    -- 扫描行数（越少越好）

Extra:   -- 额外信息
  - Using filesort：需要排序（慢）
  - Using temporary：使用临时表（慢）
  - Using index：覆盖索引（快）
  - Using where：使用WHERE过滤
```

---

### Q4：什么时候要开启事务？

**考点：** 事务使用场景、ACID特性

**必须开启事务的3大场景：**

#### 1️⃣ 多个操作必须同时成功或失败

```java
// 场景：转账
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    // 步骤1：扣A的钱
    accountMapper.deduct(fromId, amount);
    
    // 步骤2：加B的钱
    accountMapper.add(toId, amount);
    
    // 任何一步失败，都要回滚
    // 否则：扣了A的钱，但B没收到 → 钱丢了
}
```

#### 2️⃣ 数据一致性要求高

```java
// 场景：秒杀下单
@Transactional
public void seckill(Long userId, Long productId) {
    // 步骤1：扣库存
    int rows = productMapper.deductStock(productId);
    if (rows == 0) {
        throw new RuntimeException("库存不足");
    }
    
    // 步骤2：创建订单
    Order order = new Order(userId, productId);
    orderMapper.insert(order);
    
    // 步骤3：扣减用户余额
    userMapper.deductBalance(userId, order.getAmount());
    
    // 三步必须同时成功，否则回滚
}
```

#### 3️⃣ 保证隔离性

```java
// 场景：查询后更新（防止并发问题）
@Transactional
public void updateStock(Long productId) {
    // 步骤1：查询库存
    Product product = productMapper.selectById(productId);
    
    // 步骤2：判断库存
    if (product.getStock() > 0) {
        // 步骤3：扣库存
        product.setStock(product.getStock() - 1);
        productMapper.updateById(product);
    }
    
    // 事务保证：查询到更新期间，其他事务无法修改
}
```

---

**不需要事务的场景：**

```java
// ✅ 单条查询（只读操作）
public User getUserById(Long id) {
    return userMapper.selectById(id);
}

// ✅ 单条插入（不影响其他数据）
public void saveUser(User user) {
    userMapper.insert(user);
}

// ✅ 日志记录（允许失败）
public void log(String message) {
    logMapper.insert(new Log(message));
    // 即使日志失败，也不影响业务
}
```

---

**Spring事务管理：**

```java
// 声明式事务（推荐）
@Transactional
public void method() {
    // 业务逻辑
}  // 自动提交或回滚

// 编程式事务（灵活控制）
@Autowired
private TransactionTemplate transactionTemplate;

public void method() {
    transactionTemplate.execute(status -> {
        try {
            // 业务逻辑
            return result;
        } catch (Exception e) {
            status.setRollbackOnly();  // 手动回滚
            throw e;
        }
    });
}

// 事务传播行为
@Transactional(propagation = Propagation.REQUIRED)  // 默认
@Transactional(propagation = Propagation.REQUIRES_NEW)  // 新建事务
@Transactional(propagation = Propagation.NOT_SUPPORTED)  // 不使用事务
```

---

### Q5：开启事务忘了提交有什么问题？

**考点：** 长事务危害、事务管理

**5大问题：**

#### 1️⃣ 长时间占用数据库连接

```java
// 错误示例：
@Transactional
public void longTask() {
    // 查询数据
    List<User> users = userMapper.selectAll();
    
    // 复杂业务逻辑（耗时10分钟）
    for (User user : users) {
        // 调用外部API
        externalService.process(user);
        // 复杂计算
        calculate(user);
    }
    
    // 更新数据
    userMapper.updateBatch(users);
}  // 事务持续10分钟！

问题：
- 数据库连接被占用10分钟
- 高并发下，连接池很快耗尽
- 其他请求无法获取连接，超时报错
```

**解决方案：**

```java
// ✅ 缩小事务范围
public void longTask() {
    // 查询（无需事务）
    List<User> users = userMapper.selectAll();
    
    // 业务逻辑（无需事务）
    for (User user : users) {
        externalService.process(user);
        calculate(user);
    }
    
    // 只在更新时开启事务
    updateInTransaction(users);
}

@Transactional
private void updateInTransaction(List<User> users) {
    userMapper.updateBatch(users);
}  // 事务只持续几毫秒
```

---

#### 2️⃣ 锁等待超时

```sql
-- 线程A
BEGIN;
UPDATE user SET age = 20 WHERE id = 1;  -- 对id=1加锁
-- 忘记COMMIT，一直持有锁

-- 线程B（10秒后）
UPDATE user SET age = 30 WHERE id = 1;  -- 等待锁
-- 50秒后超时：
-- ERROR 1205: Lock wait timeout exceeded
```

**配置锁等待超时时间：**

```sql
-- 查看当前超时时间
SHOW VARIABLES LIKE 'innodb_lock_wait_timeout';
-- 默认：50秒

-- 修改（不推荐，应该优化代码）
SET innodb_lock_wait_timeout = 10;
```

---

#### 3️⃣ 产生脏读、幻读

```sql
-- 隔离级别：READ COMMITTED

-- 事务A
BEGIN;
SELECT age FROM user WHERE id = 1;  -- age=20
-- 不提交，一直开着

-- 事务B
UPDATE user SET age = 30 WHERE id = 1;
COMMIT;

-- 事务A再查
SELECT age FROM user WHERE id = 1;  -- age=30（脏读）
-- 问题：同一事务内，两次查询结果不同
```

---

#### 4️⃣ Undo日志膨胀

```
事务不提交 → Undo日志一直保留 → 占用大量磁盘空间

Undo日志作用：
1. 回滚时恢复数据
2. 实现MVCC（多版本并发控制）

长事务 → Undo日志无法清理 → 磁盘空间耗尽
```

**监控Undo日志：**

```sql
-- 查看Undo日志大小
SHOW ENGINE INNODB STATUS\G

-- 找到长事务
SELECT * FROM information_schema.innodb_trx
WHERE trx_started < DATE_SUB(NOW(), INTERVAL 60 SECOND);
```

---

#### 5️⃣ 死锁风险增加

```sql
-- 事务A
BEGIN;
UPDATE user SET age = 20 WHERE id = 1;  -- 锁住id=1
-- 等待...
UPDATE user SET age = 20 WHERE id = 2;  -- 想锁id=2

-- 事务B
BEGIN;
UPDATE user SET age = 30 WHERE id = 2;  -- 锁住id=2
-- 等待...
UPDATE user SET age = 30 WHERE id = 1;  -- 想锁id=1

-- 结果：死锁（A等B释放id=2，B等A释放id=1）
-- MySQL检测到死锁，回滚其中一个事务
```

---

**如何避免忘记提交？**

```java
// 1. 使用@Transactional（Spring自动管理）
@Transactional
public void method() {
    // ...
}  // 正常结束自动提交，异常自动回滚

// 2. try-finally手动管理
Connection conn = dataSource.getConnection();
try {
    conn.setAutoCommit(false);
    // 业务逻辑
    conn.commit();
} catch (Exception e) {
    conn.rollback();
    throw e;
} finally {
    conn.close();  // 一定要关闭
}

// 3. 设置事务超时
@Transactional(timeout = 30)  // 30秒超时自动回滚
public void method() {
    // ...
}
```

---

### Q6：有没有利用MySQL实现一些锁？在什么场景下会使用？

**考点：** MySQL锁机制、悲观锁/乐观锁

**MySQL锁的分类：**

```
按粒度分：
- 表锁：锁整张表
- 行锁：锁某几行（InnoDB）
- 间隙锁：锁索引之间的间隙

按类型分：
- 共享锁（S锁）：读锁，多个事务可同时持有
- 排他锁（X锁）：写锁，只有一个事务能持有
```

---

#### 场景1：悲观锁（FOR UPDATE）

**适用：写多场景，防止并发修改**

```java
// 秒杀扣库存
@Transactional
public void seckill(Long productId) {
    // 查询时加排他锁
    Product product = productMapper.selectByIdForUpdate(productId);
    
    if (product.getStock() <= 0) {
        throw new RuntimeException("库存不足");
    }
    
    // 扣库存
    product.setStock(product.getStock() - 1);
    productMapper.updateById(product);
    
    // 创建订单
    // ...
}
```

```sql
-- Mapper.xml
SELECT * FROM product WHERE id = #{id} FOR UPDATE;

-- FOR UPDATE 的效果：
-- 1. 对这一行加排他锁
-- 2. 其他事务的 SELECT ... FOR UPDATE 会等待
-- 3. 其他事务的 UPDATE 也会等待
-- 4. 事务提交后，锁自动释放
```

**FOR UPDATE的锁范围：**

```sql
-- 主键查询：行锁
SELECT * FROM product WHERE id = 1 FOR UPDATE;
-- 只锁id=1这一行

-- 唯一索引：行锁
SELECT * FROM product WHERE code = 'P001' FOR UPDATE;
-- 只锁code='P001'这一行

-- 非唯一索引：可能锁多行 + 间隙锁
SELECT * FROM product WHERE category_id = 10 FOR UPDATE;
-- 锁所有category_id=10的行，以及间隙

-- 无索引：表锁（慎用！）
SELECT * FROM product WHERE name = '商品A' FOR UPDATE;
-- 如果name没有索引，锁整张表
```

---

#### 场景2：共享锁（LOCK IN SHARE MODE）

**适用：读取后要确保数据不被修改**

```java
// 订单结算（防止商品价格变动）
@Transactional
public void checkout(Long orderId) {
    // 查询订单
    Order order = orderMapper.selectById(orderId);
    
    // 查询商品价格（加共享锁）
    Product product = productMapper.selectByIdLockInShareMode(order.getProductId());
    
    // 计算总价
    BigDecimal total = product.getPrice().multiply(new BigDecimal(order.getQuantity()));
    
    // 更新订单金额
    order.setTotalAmount(total);
    orderMapper.updateById(order);
}
```

```sql
SELECT * FROM product WHERE id = #{id} LOCK IN SHARE MODE;

-- LOCK IN SHARE MODE 的效果：
-- 1. 加共享锁（S锁）
-- 2. 允许其他事务读取（也加S锁）
-- 3. 阻止其他事务修改（加X锁会等待）
-- 4. 确保读取到的数据在事务内不会被改
```

---

#### 场景3：乐观锁（版本号）

**适用：读多写少，避免加锁**

```java
// 用户信息修改
public boolean updateUser(User user) {
    // 带版本号更新
    int rows = userMapper.updateWithVersion(user);
    
    if (rows == 0) {
        // 版本号不匹配，说明被其他线程修改过
        throw new RuntimeException("数据已被修改，请重新获取");
    }
    return true;
}
```

```sql
-- 方式1：版本号
UPDATE user 
SET name = #{name}, 
    age = #{age}, 
    version = version + 1    -- 版本号+1
WHERE id = #{id} 
  AND version = #{version};  -- 版本号校验

-- 方式2：时间戳
UPDATE user 
SET name = #{name}, 
    age = #{age}, 
    update_time = NOW()
WHERE id = #{id} 
  AND update_time = #{oldUpdateTime};
```

**工作流程：**

```
线程A：
1. 查询：id=1, version=10
2. 修改
3. 更新：WHERE id=1 AND version=10 → 成功，version=11

线程B：
1. 查询：id=1, version=10
2. 修改
3. 更新：WHERE id=1 AND version=10 → 失败（版本号已经是11）
4. 提示用户：数据已被修改，请刷新
```

---

#### 场景4：分布式锁（Redis实现）

**适用：分布式系统，防止重复操作**

```java
// Redisson分布式锁
@Autowired
private RedissonClient redissonClient;

public void seckill(Long userId, Long productId) {
    // 一人一单锁
    String lockKey = "lock:seckill:" + userId + ":" + productId;
    RLock lock = redissonClient.getLock(lockKey);
    
    try {
        // 尝试获取锁，等待10秒，持有30秒
        boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
        
        if (!isLocked) {
            throw new RuntimeException("请勿重复下单");
        }
        
        // 业务逻辑
        createOrder(userId, productId);
        
    } catch (InterruptedException e) {
        throw new RuntimeException("获取锁失败");
    } finally {
        lock.unlock();  // 释放锁
    }
}
```

---

**锁的对比：**

| 锁类型 | 实现 | 适用场景 | 优点 | 缺点 |
|--------|------|----------|------|------|
| **FOR UPDATE** | MySQL排他锁 | 写多，强一致性 | 强一致 | 性能低 |
| **LOCK IN SHARE MODE** | MySQL共享锁 | 读后不能改 | 允许并发读 | 写阻塞 |
| **乐观锁** | 版本号 | 读多写少 | 无锁，性能高 | 可能失败 |
| **分布式锁** | Redis/ZK | 分布式系统 | 跨服务器 | 复杂 |

---

### Q7：乐观锁和悲观锁有没有了解？什么时候用？

**考点：** 并发控制策略

**核心区别：**

| 对比项 | 乐观锁 | 悲观锁 |
|--------|--------|--------|
| **哲学思想** | 认为冲突少，不加锁 | 认为冲突多，先加锁 |
| **实现方式** | 版本号、CAS | FOR UPDATE、synchronized |
| **加锁时机** | 更新时检查 | 读取时就加锁 |
| **性能** | 高（无锁） | 低（等待锁） |
| **失败处理** | 重试或提示用户 | 等待锁释放 |
| **适用场景** | 读多写少 | 写多读少 |

---

#### 乐观锁详解

**实现方式1：版本号**

```java
// 实体类
@Data
public class User {
    private Long id;
    private String name;
    private Integer age;
    private Integer version;  // 版本号
}

// 更新方法
public boolean updateUser(User user) {
    // SQL：
    // UPDATE user 
    // SET name = ?, age = ?, version = version + 1
    // WHERE id = ? AND version = ?
    
    int rows = userMapper.updateWithVersion(user);
    
    if (rows == 0) {
        // 版本号不匹配，更新失败
        // 选项1：重试
        // 选项2：提示用户刷新页面
        return false;
    }
    return true;
}
```

**实现方式2：CAS（Compare And Swap）**

```java
// Java并发包中的原子类
AtomicInteger stock = new AtomicInteger(100);

// CAS扣库存
public boolean deductStock() {
    while (true) {
        int current = stock.get();  // 读取当前值
        
        if (current <= 0) {
            return false;  // 库存不足
        }
        
        int next = current - 1;
        
        // CAS：如果当前值还是current，就更新为next
        if (stock.compareAndSet(current, next)) {
            return true;  // 成功
        }
        // CAS失败，说明被其他线程修改了，继续循环重试
    }
}
```

---

#### 悲观锁详解

**实现方式1：数据库锁（FOR UPDATE）**

```java
@Transactional
public void seckill(Long productId) {
    // 查询时加锁
    Product product = productMapper.selectByIdForUpdate(productId);
    
    if (product.getStock() <= 0) {
        throw new RuntimeException("库存不足");
    }
    
    // 扣库存
    product.setStock(product.getStock() - 1);
    productMapper.updateById(product);
}  // 事务结束，锁自动释放
```

**实现方式2：Java锁（synchronized）**

```java
private final Object lock = new Object();

public void deductStock() {
    synchronized (lock) {  // 加锁
        // 查询库存
        int stock = getStock();
        
        // 扣库存
        if (stock > 0) {
            setStock(stock - 1);
        }
    }  // 释放锁
}
```

---

#### 使用场景对比

**乐观锁适用场景：**

```java
// 1. 用户信息修改（读多写少）
public void updateUserInfo(User user) {
    // 用户修改个人信息，冲突概率低
    // 使用乐观锁，性能更好
}

// 2. 商品详情更新（后台操作）
public void updateProduct(Product product) {
    // 商品信息更新不频繁
    // 使用乐观锁
}

// 3. 文章点赞（允许短暂不一致）
public void like(Long articleId) {
    // 点赞数允许短暂不准确
    // 使用乐观锁 + 最终一致性
}
```

**悲观锁适用场景：**

```java
// 1. 秒杀扣库存（写多，强一致性）
@Transactional
public void seckill(Long productId) {
    // 高并发扣库存，冲突概率高
    // 使用悲观锁，避免超卖
    Product product = productMapper.selectByIdForUpdate(productId);
    // ...
}

// 2. 转账（强一致性）
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    // 转账必须保证数据准确
    // 使用悲观锁
    Account from = accountMapper.selectByIdForUpdate(fromId);
    Account to = accountMapper.selectByIdForUpdate(toId);
    // ...
}

// 3. 抢票（高并发）
@Transactional
public void bookTicket(Long ticketId) {
    // 抢票冲突概率极高
    // 使用悲观锁
    Ticket ticket = ticketMapper.selectByIdForUpdate(ticketId);
    // ...
}
```

---

**决策树：**

```
问自己：
1. 读多写少？
   ├─ 是 → 乐观锁
   └─ 否 → 继续

2. 写操作冲突概率高？
   ├─ 是 → 悲观锁
   └─ 否 → 乐观锁

3. 对数据一致性要求极高？
   ├─ 是 → 悲观锁
   └─ 否 → 乐观锁

4. 性能要求极高？
   ├─ 是 → 乐观锁
   └─ 否 → 悲观锁
```

---

**混合使用：**

```java
// 先乐观，失败后悲观
public void updateStock(Long productId) {
    // 尝试乐观锁
    boolean success = tryOptimisticUpdate(productId);
    
    if (!success) {
        // 乐观锁失败，改用悲观锁
        pessimisticUpdate(productId);
    }
}

@Transactional
public void pessimisticUpdate(Long productId) {
    Product product = productMapper.selectByIdForUpdate(productId);
    // ...
}
```

---

## Redis缓存与消息队列

### Q1：Redis用得多吗？搭建过Redis集群吗？

**考点：** Redis实战经验、集群架构

**项目中Redis的使用：**

#### 1️⃣ 缓存热点数据

```java
// 商品详情缓存
public Product getProduct(Long id) {
    String key = "product:" + id;
    
    // 1. 先查Redis
    String json = redisTemplate.opsForValue().get(key);
    if (json != null) {
        return JSON.parseObject(json, Product.class);
    }
    
    // 2. Redis没有，查数据库
    Product product = productMapper.selectById(id);
    
    // 3. 写入Redis，过期时间30分钟
    redisTemplate.opsForValue().set(key, JSON.toJSONString(product), 30, TimeUnit.MINUTES);
    
    return product;
}
```

#### 2️⃣ 分布式锁

```java
// 使用Redisson实现秒杀一人一单
@Autowired
private RedissonClient redissonClient;

public void seckill(Long userId, Long productId) {
    String lockKey = "lock:seckill:" + userId;
    RLock lock = redissonClient.getLock(lockKey);
    
    try {
        boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
        if (!isLocked) {
            throw new RuntimeException("请勿重复下单");
        }
        
        // 业务逻辑
        createOrder(userId, productId);
        
    } finally {
        lock.unlock();
    }
}
```

#### 3️⃣ 消息队列

```java
// Redis Stream实现异步下单
public void sendOrderMessage(Order order) {
    StreamRecords records = StreamRecords
        .newRecord()
        .in("stream:order")
        .ofObject(order);
    
    redisTemplate.opsForStream().add(records);
}
```

---

**Redis集群搭建：**

#### 方案1：主从复制 + 哨兵（Sentinel）

**架构：**

```
Master（主节点）
    ↓ 复制
Slave1    Slave2（从节点）

Sentinel1  Sentinel2  Sentinel3（哨兵，监控主从）

工作流程：
1. Master宕机
2. Sentinel检测到
3. 选举一个Slave提升为Master
4. 其他Slave指向新Master
5. 故障转移完成
```

**配置：**

```bash
# redis.conf（Master）
port 6379
daemonize yes

# redis.conf（Slave）
port 6380
replicaof 127.0.0.1 6379  # 指向Master

# sentinel.conf
port 26379
sentinel monitor mymaster 127.0.0.1 6379 2  # 2个Sentinel同意就故障转移
sentinel down-after-milliseconds mymaster 5000  # 5秒未响应判定下线
sentinel failover-timeout mymaster 10000
```

**优点：**
- ✅ 高可用：Master挂了自动切换
- ✅ 读写分离：Master写，Slave读

**缺点：**
- ❌ 只有一个Master，写能力有限
- ❌ 数据量大了，单机内存不够

---

#### 方案2：Redis Cluster（集群）

**架构：**

```
16384个哈希槽，分配给多个Master

Master1（槽0-5460）
    ↓
Slave1

Master2（槽5461-10922）
    ↓
Slave2

Master3（槽10923-16383）
    ↓
Slave3

数据分片：
key → hash(key) % 16384 → 找到对应的Master
```

**配置：**

```bash
# redis.conf
port 7000
cluster-enabled yes  # 开启集群
cluster-config-file nodes-7000.conf
cluster-node-timeout 5000

# 启动6个节点（3主3从）
redis-server redis-7000.conf
redis-server redis-7001.conf
redis-server redis-7002.conf
redis-server redis-7003.conf
redis-server redis-7004.conf
redis-server redis-7005.conf

# 创建集群
redis-cli --cluster create \
  127.0.0.1:7000 \
  127.0.0.1:7001 \
  127.0.0.1:7002 \
  127.0.0.1:7003 \
  127.0.0.1:7004 \
  127.0.0.1:7005 \
  --cluster-replicas 1  # 每个Master 1个Slave
```

**优点：**
- ✅ 数据分片，支持海量数据
- ✅ 多个Master，写能力强
- ✅ 高可用：Master挂了，Slave自动提升

**缺点：**
- ❌ 不支持多key操作（key可能在不同节点）
- ❌ 客户端需要支持集群协议

---

**项目中的选择：**

```
开发环境：
- 单机Redis（简单够用）

生产环境：
- 小项目：主从 + 哨兵（3个节点）
- 大项目：Redis Cluster（6个节点起）
```

---

### Q2：Redis中用得最多的数据结构是什么？为什么要用Hash，有什么特殊考虑吗？

**考点：** Redis数据结构选择

**5大数据结构使用对比：**

| 数据结构 | 使用场景 | 项目中的应用 | 时间复杂度 |
|---------|---------|-------------|-----------|
| **String** | 简单KV、计数器、缓存JSON | 验证码、库存数量 | O(1) |
| **Hash** | 对象存储、字段级操作 | 用户信息、商品详情 | O(1) |
| **List** | 消息队列、最新列表 | 最新评论、任务队列 | O(1) |
| **Set** | 去重、集合运算 | 标签、一人一单判断 | O(1) |
| **ZSet** | 排行榜、带权重 | 商品销量排行 | O(logN) |

---

#### 为什么用Hash存对象？

**方案对比：**

```redis
# ❌ 方案1：String存整个对象（JSON）
SET user:1001 '{"name":"张三","age":25,"city":"北京","phone":"13800138000"}'

问题：
1. 修改单个字段（如age）需要：
   - 读取整个JSON
   - 反序列化
   - 修改age
   - 序列化
   - 写回Redis
   → 性能差

2. 占用空间大：
   - JSON格式本身有额外字符（"{", ":", ","）
   - 字段名重复存储


# ✅ 方案2：Hash存对象（字段分开）
HSET user:1001 name "张三"
HSET user:1001 age 25
HSET user:1001 city "北京"
HSET user:1001 phone "13800138000"

# 或者批量设置
HMSET user:1001 name "张三" age 25 city "北京" phone "13800138000"

优势：
1. 字段级操作：
   HSET user:1001 age 26  # 只改age，无需读取其他字段
   HINCRBY user:1001 age 1  # 原子自增

2. 节省空间：
   - 使用ziplist编码（field和value连续存储）
   - 比JSON省空间

3. 语义清晰：
   HGET user:1001 name  # 直接获取name字段
```

---

**Hash的底层编码优化：**

```redis
# Redis会根据数据量自动选择编码

# 小对象（默认512个字段以内）：ziplist（压缩列表）
配置：
hash-max-ziplist-entries 512  # 字段数量阈值
hash-max-ziplist-value 64     # 单个value大小阈值

ziplist特点：
- 内存连续
- 节省空间（比hashtable省30%-50%）
- 查询O(n)，但n很小时影响不大


# 大对象：hashtable
- 内存不连续
- 占用空间大
- 查询O(1)
```

---

**实战示例：**

```java
// 用户信息缓存（使用Hash）
public void cacheUser(User user) {
    String key = "user:" + user.getId();
    
    Map<String, String> map = new HashMap<>();
    map.put("name", user.getName());
    map.put("age", String.valueOf(user.getAge()));
    map.put("city", user.getCity());
    map.put("phone", user.getPhone());
    
    redisTemplate.opsForHash().putAll(key, map);
    
    // 设置过期时间
    redisTemplate.expire(key, 30, TimeUnit.MINUTES);
}

// 获取用户信息
public User getUser(Long id) {
    String key = "user:" + id;
    
    Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
    
    if (map.isEmpty()) {
        return null;
    }
    
    User user = new User();
    user.setId(id);
    user.setName((String) map.get("name"));
    user.setAge(Integer.parseInt((String) map.get("age")));
    user.setCity((String) map.get("city"));
    user.setPhone((String) map.get("phone"));
    
    return user;
}

// 只修改年龄
public void updateAge(Long id, Integer age) {
    String key = "user:" + id;
    redisTemplate.opsForHash().put(key, "age", String.valueOf(age));
}
```

---

**什么时候不用Hash？**

```redis
# 1. 对象很大（字段超过512个）
# → 用多个Hash分片

# 2. 需要整体操作（如序列化传输）
# → 用String存JSON

# 3. 字段很少（1-2个）
# → 用String，避免Hash的开销

# 4. 需要设置单个字段的过期时间
# → Hash不支持字段级过期，用String
```

---

### Q3：保证消息的可达性、可靠性、有序性、幂等性指的是什么？具体怎么实现的？

**考点：** 消息队列核心特性

**四大特性详解：**

---

#### 1️⃣ 可达性（消息不丢失）

**定义：** 消息一定能从生产者到达消费者

**实现方案：**

```
生产者 → Redis Stream → 消费者

保证链路：
1. 生产者 → Redis：
   - XADD成功才返回
   - Redis持久化（AOF）
   
2. Redis → 消费者：
   - ACK机制（XACK）
   - pending list（未ACK的消息）
   
3. 消费者崩溃：
   - 重启后读取pending list
   - 重新处理未ACK的消息
```

**代码实现：**

```java
// 生产者
public void sendMessage(Order order) {
    // 发送到Stream
    StreamRecords records = StreamRecords
        .newRecord()
        .in("stream:order")
        .ofObject(order);
    
    RecordId id = redisTemplate.opsForStream().add(records);
    log.info("消息已发送，ID: {}", id);
}

// 消费者
@Bean
public void consumeMessage() {
    String streamKey = "stream:order";
    String group = "order-group";
    String consumer = "consumer-1";
    
    // 1. 先读pending list（崩溃恢复）
    List<MapRecord> pending = redisTemplate.opsForStream()
        .pending(streamKey, group, Range.unbounded(), 10);
    
    for (MapRecord record : pending) {
        // 处理消息
        handleOrder(record);
        // ACK确认
        redisTemplate.opsForStream().acknowledge(streamKey, group, record.getId());
    }
    
    // 2. 再读新消息
    List<MapRecord> records = redisTemplate.opsForStream()
        .read(Consumer.from(group, consumer),
              StreamReadOptions.empty().count(10).block(Duration.ofSeconds(5)),
              StreamOffset.create(streamKey, ReadOffset.lastConsumed()));
    
    for (MapRecord record : records) {
        handleOrder(record);
        redisTemplate.opsForStream().acknowledge(streamKey, group, record.getId());
    }
}
```

---

#### 2️⃣ 可靠性（数据一致性）

**定义：** 业务操作和消息发送要么都成功，要么都失败

**实现方案：**

```java
// 本地事务 + 消息发送
@Transactional
public void createOrder(Order order) {
    // 1. 扣库存
    productMapper.deductStock(order.getProductId());
    
    // 2. 创建订单
    orderMapper.insert(order);
    
    // 3. 扣余额
    userMapper.deductBalance(order.getUserId(), order.getAmount());
    
    // 4. 发送消息（在事务内）
    sendOrderMessage(order);
    
    // 事务提交 → 消息和数据库操作同时生效
    // 事务回滚 → 消息也不发送
}
```

**问题：消息发送失败怎么办？**

```java
// 可靠性增强：发送到本地消息表
@Transactional
public void createOrder(Order order) {
    // 1. 业务操作
    productMapper.deductStock(order.getProductId());
    orderMapper.insert(order);
    
    // 2. 插入本地消息表（与业务在同一事务）
    LocalMessage msg = new LocalMessage(order);
    msg.setStatus(MessageStatus.PENDING);  // 待发送
    messageMapper.insert(msg);
    
    // 事务提交
}

// 定时任务：扫描本地消息表，重试发送
@Scheduled(fixedRate = 1000)
public void retrySendMessage() {
    List<LocalMessage> pending = messageMapper.selectPending();
    
    for (LocalMessage msg : pending) {
        try {
            // 发送到Redis Stream
            sendToRedis(msg.getContent());
            
            // 更新状态为已发送
            msg.setStatus(MessageStatus.SENT);
            messageMapper.updateById(msg);
            
        } catch (Exception e) {
            // 发送失败，下次重试
            log.error("消息发送失败", e);
        }
    }
}
```

---

#### 3️⃣ 有序性

**定义：** 消息按发送顺序被消费

**问题场景：**

```
用户A发送消息：
消息1：你好     → 线程1处理
消息2：在吗     → 线程2处理
消息3：有空吗   → 线程3处理

因为线程2先处理完，导致：
接收顺序：消息2 → 消息1 → 消息3（乱序）
```

**解决方案：**

```java
// 方案1：单线程消费（最简单，但性能差）
@Bean
public void singleThreadConsumer() {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    
    executor.submit(() -> {
        while (true) {
            List<MapRecord> records = readMessages();
            for (MapRecord record : records) {
                handleMessage(record);  // 顺序处理
            }
        }
    });
}

// 方案2：按userId分区（相同用户的消息发到同一线程）
@Bean
public void partitionConsumer() {
    int threadCount = 10;
    ExecutorService[] executors = new ExecutorService[threadCount];
    
    for (int i = 0; i < threadCount; i++) {
        executors[i] = Executors.newSingleThreadExecutor();
    }
    
    while (true) {
        List<MapRecord> records = readMessages();
        
        for (MapRecord record : records) {
            Long userId = (Long) record.getValue().get("userId");
            
            // 根据userId分区
            int partition = (int) (userId % threadCount);
            
            // 发送到对应线程
            executors[partition].submit(() -> handleMessage(record));
        }
    }
}

// 方案3：版本号 + 业务层排序
public void handleMessage(Message msg) {
    // 消息带序号
    Long seq = msg.getSeq();
    
    // 存入缓存（按序号排序）
    cacheMessages(msg);
    
    // 按序号顺序处理
    processInOrder();
}
```

---

#### 4️⃣ 幂等性

**定义：** 同一消息重复消费，结果相同

**问题场景：**

```
消费者处理完消息，准备ACK时崩溃
→ 重启后，消息还在pending list
→ 再次消费，导致重复处理（如：重复扣库存）
```

**解决方案：**

```java
// 方案1：唯一索引（数据库层面保证）
ALTER TABLE `order` ADD UNIQUE INDEX uk_order_no(order_no);

// 插入时，重复订单号会报错，捕获异常即可
try {
    orderMapper.insert(order);
} catch (DuplicateKeyException e) {
    // 订单已存在，忽略
    return;
}

// 方案2：Redis去重
public void handleMessage(Order order) {
    String key = "order:processed:" + order.getOrderNo();
    
    // setIfAbsent：key不存在才设置
    Boolean success = redisTemplate.opsForValue()
        .setIfAbsent(key, "1", 1, TimeUnit.DAYS);
    
    if (Boolean.FALSE.equals(success)) {
        // 消息已处理过，直接返回
        return;
    }
    
    // 处理业务
    processOrder(order);
}

// 方案3：版本号控制
UPDATE `order` 
SET status = 'PAID', version = version + 1
WHERE order_no = #{orderNo} 
  AND status = 'UNPAID'  -- 只有未支付才能改为已支付
  AND version = #{version};

// 返回rows=0，说明已经处理过
```

---

**完整示例（四大特性结合）：**

```java
// Lua脚本：原子性 + 可达性 + 幂等性
String luaScript = 
    "local stock = redis.call('get', KEYS[1]) " +
    "if tonumber(stock) <= 0 then " +
    "    return 0 " +
    "end " +
    "local processed = redis.call('get', KEYS[2]) " +  // 幂等性检查
    "if processed then " +
    "    return -1 " +
    "end " +
    "redis.call('decr', KEYS[1]) " +  // 扣库存
    "redis.call('setex', KEYS[2], 86400, '1') " +  // 标记已处理
    "redis.call('xadd', KEYS[3], '*', 'orderId', ARGV[1]) " +  // 发送消息
    "return 1";

// 执行
Long result = redisTemplate.execute(
    new DefaultRedisScript<>(luaScript, Long.class),
    Arrays.asList("stock:1001", "order:processed:123", "stream:order"),
    "123"
);

// 结果：
// 1：成功
// 0：库存不足
// -1：重复下单（幂等性）
```

---

**总结表格：**

| 特性 | 定义 | 实现方案 | 关键技术 |
|------|------|----------|----------|
| **可达性** | 消息不丢失 | ACK + pending list | Redis Stream |
| **可靠性** | 业务一致性 | 本地消息表 + 定时重试 | 事务 |
| **有序性** | 按顺序消费 | 单线程/分区/版本号 | 线程池 |
| **幂等性** | 重复消费幂等 | 唯一索引/Redis去重/版本号 | 数据库/Redis |

---

### Q4：为什么会出现有序性的问题？（多线程）

**考点：** 并发消费导致的乱序

**根本原因：多线程并发消费，处理速度不一致**

---

**问题场景1：聊天消息乱序**

```
用户A发送3条消息：
T1: 发送"你好"     → 消息1
T2: 发送"在吗"     → 消息2
T3: 发送"有空吗"   → 消息3

消费端：3个线程并发处理
线程1处理消息1：处理时间100ms
线程2处理消息2：处理时间10ms   ← 最快
线程3处理消息3：处理时间50ms

结果：
T1: 线程2处理完消息2 → "在吗"
T2: 线程3处理完消息3 → "有空吗"
T3: 线程1处理完消息1 → "你好"

接收方看到的顺序：在吗 → 有空吗 → 你好（乱了）
```

---

**问题场景2：订单状态更新乱序**

```
订单状态变化：
T1: 创建订单     → 消息1（status: CREATED）
T2: 支付成功     → 消息2（status: PAID）
T3: 发货         → 消息3（status: SHIPPED）

多线程消费：
线程2处理消息2（支付）→ 50ms完成
线程3处理消息3（发货）→ 30ms完成  ← 最快
线程1处理消息1（创建）→ 100ms完成

结果：
数据库状态：SHIPPED → PAID → CREATED
最终状态：CREATED（错误！应该是SHIPPED）
```

---

**为什么单线程不会乱序？**

```
单线程：
消息1 → 处理完 → ACK
  ↓
消息2 → 处理完 → ACK
  ↓
消息3 → 处理完 → ACK

严格按顺序，一个接一个
```

**为什么多线程会乱序？**

```
多线程：
消息1 → 线程1 → 处理中...
消息2 → 线程2 → 处理完 ✓
消息3 → 线程3 → 处理完 ✓
         ↑
      线程1还没完成，但线程2、3已经完成了
```

---

**解决方案详解：**

#### 方案1：单线程消费（牺牲性能，保证顺序）

```java
@Bean
public void orderedConsumer() {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    
    executor.submit(() -> {
        while (true) {
            // 读取消息
            List<MapRecord> records = readMessages();
            
            // 顺序处理
            for (MapRecord record : records) {
                handleMessage(record);
                ackMessage(record);
            }
        }
    });
}

优点：绝对有序
缺点：性能低（TPS受限于单线程处理速度）
```

---

#### 方案2：按业务Key分区（兼顾性能和顺序）

```java
@Bean
public void partitionedConsumer() {
    int partitions = 10;  // 10个分区
    ExecutorService[] executors = new ExecutorService[partitions];
    
    // 每个分区一个单线程池
    for (int i = 0; i < partitions; i++) {
        executors[i] = Executors.newSingleThreadExecutor();
    }
    
    while (true) {
        List<MapRecord> records = readMessages();
        
        for (MapRecord record : records) {
            // 提取业务Key（如：userId、orderId）
            String userId = (String) record.getValue().get("userId");
            
            // 计算分区
            int partition = Math.abs(userId.hashCode() % partitions);
            
            // 提交到对应分区
            executors[partition].submit(() -> {
                handleMessage(record);
                ackMessage(record);
            });
        }
    }
}

特点：
- 同一userId的消息，一定在同一线程处理 → 有序
- 不同userId的消息，可以并发处理 → 性能好
```

**图解分区：**

```
消息流：
用户A的消息：msg1(A) → msg2(A) → msg3(A)
用户B的消息：msg1(B) → msg2(B) → msg3(B)

分区：
partition = hash(userId) % 10

用户A → hash → partition 3
  msg1(A) →┐
  msg2(A) →├→ 线程3（顺序处理）
  msg3(A) →┘

用户B → hash → partition 7
  msg1(B) →┐
  msg2(B) →├→ 线程7（顺序处理）
  msg3(B) →┘

用户A和用户B的消息可以并发处理 ✓
但同一用户的消息是有序的 ✓
```

---

#### 方案3：版本号 + 队列重排序

```java
// 消息带序号
public class Message {
    private String userId;
    private Long seq;  // 序号
    private String content;
}

// 消费端：先缓存，再排序处理
Map<String, PriorityQueue<Message>> buffers = new ConcurrentHashMap<>();

public void handleMessage(Message msg) {
    String userId = msg.getUserId();
    
    // 获取用户的消息队列（按seq排序）
    PriorityQueue<Message> queue = buffers.computeIfAbsent(
        userId,
        k -> new PriorityQueue<>(Comparator.comparing(Message::getSeq))
    );
    
    // 加入队列
    synchronized (queue) {
        queue.offer(msg);
        
        // 处理连续的消息
        Long expectedSeq = getExpectedSeq(userId);
        while (!queue.isEmpty() && queue.peek().getSeq().equals(expectedSeq)) {
            Message next = queue.poll();
            processMessage(next);  // 按序处理
            expectedSeq++;
        }
        
        updateExpectedSeq(userId, expectedSeq);
    }
}
```

---

**实战选择：**

| 场景 | 推荐方案 | 理由 |
|------|---------|------|
| **聊天消息** | 按userId分区 | 不同用户并发，同一用户有序 |
| **订单状态** | 按orderId分区 | 不同订单并发，同一订单有序 |
| **支付回调** | 版本号 + 队列 | 支付网关可能乱序回调 |
| **日志采集** | 单线程或不保证 | 日志对顺序要求不高 |

---

### Q5：对Redis中的有序集合ZSet了解吗？ZSet底层是用什么实现的？

**考点：** ZSet数据结构、跳表原理

---

#### ZSet使用场景

```redis
# 场景1：商品销量排行榜
ZADD sales:rank 100 "商品A"   # 销量100
ZADD sales:rank 200 "商品B"   # 销量200
ZADD sales:rank 150 "商品C"   # 销量150

# 查询Top3
ZREVRANGE sales:rank 0 2 WITHSCORES
# 结果：
# 1) "商品B"  200
# 2) "商品C"  150
# 3) "商品A"  100

# 场景2：学生成绩排名
ZADD scores 85 "张三"
ZADD scores 92 "李四"
ZADD scores 78 "王五"

# 查询90分以上
ZRANGEBYSCORE scores 90 100
# 结果："李四"

# 场景3：延迟队列（score=时间戳）
ZADD delay:queue 1648800000 "task1"  # 10分钟后执行
ZADD delay:queue 1648800600 "task2"  # 20分钟后执行

# 取出到期任务
ZRANGEBYSCORE delay:queue 0 当前时间戳
```

---

#### ZSet底层实现

**ZSet = 跳表(skiplist) + 字典(dict)**

```
为什么需要两个数据结构？

1. 跳表：
   - 按score排序
   - 支持范围查询（ZRANGE、ZRANGEBYSCORE）
   - 时间复杂度：O(logN)

2. 字典：
   - member → score 映射
   - 支持快速查找member的score（ZSCORE）
   - 时间复杂度：O(1)
```

---

#### 跳表（Skiplist）原理

**跳表 = 多层链表**

```
原始链表（查找50需要遍历）：
10 → 20 → 30 → 40 → 50 → 60 → 70 → 80
查找50：需要5步

跳表（多层索引加速）：
Level 3:  10 ----------------------→ 70
Level 2:  10 -------→ 40 -------→ 70
Level 1:  10 → 20 → 30 → 40 → 50 → 60 → 70 → 80
查找50：
  1. Level 3: 10 → 70（过大，下降）
  2. Level 2: 10 → 40 → 70（过大，下降）
  3. Level 1: 40 → 50（找到）
  只需3步！
```

**跳表vs红黑树：**

| 对比项 | 跳表 | 红黑树 |
|--------|------|--------|
| **查找** | O(logN) | O(logN) |
| **插入** | O(logN) | O(logN) |
| **范围查询** | ✅ 快（顺序遍历） | ❌ 慢（需中序遍历） |
| **实现复杂度** | ✅ 简单 | ❌ 复杂（旋转、变色） |
| **空间复杂度** | O(N)（多层索引） | O(N) |

**为什么Redis选择跳表而不是红黑树？**

```
1. 范围查询更快：
   跳表：找到起点后，直接顺序遍历
   红黑树：需要中序遍历（递归或栈）

2. 实现更简单：
   跳表：插入/删除只需修改指针
   红黑树：需要复杂的旋转和变色操作

3. 代码可读性：
   跳表：200行代码
   红黑树：1000+行代码

4. 并发友好：
   跳表：每个节点独立，局部修改
   红黑树：旋转涉及多个节点，全局操作
```

---

#### ZSet的编码

**Redis会根据数据量选择编码：**

```redis
# 小对象：ziplist（压缩列表）
配置：
zset-max-ziplist-entries 128  # 元素数量阈值
zset-max-ziplist-value 64     # 单个value大小阈值

ZADD myzset 1 "a"
ZADD myzset 2 "b"
# 此时用ziplist编码（省内存）

# 大对象：skiplist + dict
ZADD myzset ... （超过128个元素）
# 自动转换为skiplist编码
```

---

#### 实战代码

```java
// 商品销量排行榜
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// 增加销量
public void incrSales(Long productId, Integer count) {
    String key = "sales:rank";
    redisTemplate.opsForZSet().incrementScore(key, productId, count);
}

// 获取Top10
public List<Product> getTop10() {
    String key = "sales:rank";
    
    // 倒序查询（ZREVRANGE）
    Set<ZSetOperations.TypedTuple<Object>> result = 
        redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 9);
    
    List<Product> products = new ArrayList<>();
    for (ZSetOperations.TypedTuple<Object> tuple : result) {
        Long productId = (Long) tuple.getValue();
        Double sales = tuple.getScore();
        
        Product product = productMapper.selectById(productId);
        product.setSales(sales.intValue());
        products.add(product);
    }
    
    return products;
}

// 获取某商品排名
public Long getRank(Long productId) {
    String key = "sales:rank";
    
    // 倒序排名（销量高的排前面）
    Long rank = redisTemplate.opsForZSet().reverseRank(key, productId);
    
    return rank == null ? -1 : rank + 1;  // 排名从1开始
}
```

---

### Q6：Redis中的持久化机制是怎样的？

**考点：** RDB、AOF、混合持久化

**持久化对比：**

| 机制 | 全称 | 原理 | 优点 | 缺点 |
|------|------|------|------|------|
| **RDB** | Redis DataBase | 定时快照 | 性能高、恢复快 | 可能丢失数据 |
| **AOF** | Append Only File | 记录每个写命令 | 数据完整性好 | 文件大、恢复慢 |
| **混合** | RDB + AOF | RDB快照 + AOF增量 | 兼顾性能和安全 | Redis 4.0+ |

---

#### RDB（快照）

**工作原理：**

```
定时将内存数据dump到磁盘

触发时机：
1. 手动触发：SAVE、BGSAVE命令
2. 自动触发：配置规则（如：5分钟内1000次写入）
3. 关闭触发：SHUTDOWN命令
```

**配置：**

```redis
# redis.conf

# 快照规则（满足任一条件就触发）
save 900 1       # 900秒内至少1个key变化
save 300 10      # 300秒内至少10个key变化
save 60 10000    # 60秒内至少10000个key变化

# 快照文件
dbfilename dump.rdb
dir /var/lib/redis/

# 压缩（节省空间，但消耗CPU）
rdbcompression yes

# 校验（防止文件损坏）
rdbchecksum yes
```

**SAVE vs BGSAVE：**

```redis
# SAVE：阻塞
SAVE
# 主进程执行快照，期间无法处理请求
# 生产环境禁用！

# BGSAVE：非阻塞（推荐）
BGSAVE
# fork子进程执行快照，主进程继续服务
# 利用COW(Copy-On-Write)机制
```

**RDB优缺点：**

```
优点：
✅ 性能高：子进程处理，不影响主进程
✅ 恢复快：直接加载二进制文件
✅ 文件小：压缩存储

缺点：
❌ 数据丢失：上次快照到崩溃之间的数据会丢失
   例如：每5分钟快照一次，崩溃最多丢5分钟数据
❌ fork耗时：数据量大时，fork子进程可能卡顿
```

---

#### AOF（追加日志）

**工作原理：**

```
记录每个写命令到日志文件
恢复时：重新执行日志中的命令

写入流程：
1. 客户端发送写命令（SET key value）
2. Redis执行命令
3. 写入AOF缓冲区
4. 根据策略同步到磁盘
```

**配置：**

```redis
# 开启AOF
appendonly yes

# AOF文件
appendfilename "appendonly.aof"

# 同步策略（重要！）
appendfsync always     # 每次写都同步（最安全，最慢）
appendfsync everysec   # 每秒同步（推荐，最多丢1秒数据）
appendfsync no         # 让操作系统决定（最快，可能丢数据）
```

**同步策略对比：**

| 策略 | 同步频率 | 性能 | 安全性 | 适用场景 |
|------|---------|------|--------|----------|
| **always** | 每次写入 | 慢 | 最高（几乎不丢数据） | 金融交易 |
| **everysec** | 每秒一次 | 中 | 高（最多丢1秒） | 推荐，通用场景 |
| **no** | OS决定 | 快 | 低（可能丢数据） | 不重要的缓存 |

**AOF重写（压缩）：**

```redis
# 问题：AOF文件越来越大

# 例如：
SET key 1
SET key 2
SET key 3
# 最终key=3，但AOF记录了3条命令

# AOF重写：只保留最终状态
SET key 3  # 只需这一条

# 自动重写配置
auto-aof-rewrite-percentage 100  # 文件大小增长100%触发
auto-aof-rewrite-min-size 64mb   # 文件至少64MB才重写

# 手动重写
BGREWRITEAOF
```

**AOF优缺点：**

```
优点：
✅ 数据完整性好：everysec最多丢1秒
✅ 文件可读：文本格式，易于修复
✅ 故障恢复：可以手动修改AOF文件

缺点：
❌ 文件大：记录每条命令
❌ 恢复慢：需要重新执行所有命令
❌ 性能稍差：写入操作要同步到磁盘
```

---

#### 混合持久化（Redis 4.0+）

**原理：** RDB快照 + AOF增量 = 最优方案

```
开启混合持久化：
aof-use-rdb-preamble yes

工作流程：
1. AOF重写时，先写入RDB格式的快照
2. 然后追加增量的AOF命令

文件结构：
[RDB快照部分][AOF增量部分]

恢复流程：
1. 先加载RDB部分（快速恢复大部分数据）
2. 再执行AOF部分（恢复最新数据）
```

**优势：**

```
✅ 恢复快：RDB部分快速加载
✅ 数据完整：AOF增量保证不丢数据
✅ 文件小：RDB压缩 + AOF只保留增量
```

---

#### 持久化选择

| 场景 | 推荐方案 | 配置 |
|------|---------|------|
| **缓存场景** | 不持久化或RDB | appendonly no |
| **通用场景** | 混合持久化 | aof-use-rdb-preamble yes<br>appendfsync everysec |
| **金融场景** | AOF always | appendfsync always |

---

**实战配置：**

```redis
# redis.conf（推荐配置）

# RDB
save 900 1
save 300 10
save 60 10000
dbfilename dump.rdb

# AOF
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec  # 每秒同步

# 混合持久化
aof-use-rdb-preamble yes

# 重写
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

# 目录
dir /var/lib/redis/
```

---

## 网络协议

### Q1：我们关注HTTP返回哪些内容?HTTP还有哪些字段?

**考点：** HTTP协议理解

**HTTP响应完整结构：**

```http
HTTP/1.1 200 OK                                    ← 状态行
Content-Type: application/json; charset=UTF-8      ← 响应头开始
Content-Length: 1234
Server: nginx/1.18.0
Date: Wed, 01 Apr 2026 10:00:00 GMT
Cache-Control: max-age=3600
Set-Cookie: sessionId=abc123; HttpOnly; Secure
Access-Control-Allow-Origin: *
Connection: keep-alive
ETag: "686897696a7c876b7e"
Last-Modified: Wed, 01 Apr 2026 09:00:00 GMT
                                                   ← 空行
{"code": 200, "message": "success", "data": {...}} ← 响应体
```

---

**常用响应头详解：**

| 响应头 | 作用 | 示例 | 说明 |
|--------|------|------|------|
| **Content-Type** | 内容类型 | application/json | 告诉客户端数据格式 |
| **Content-Length** | 内容长度 | 1234 | 响应体字节数 |
| **Server** | 服务器信息 | nginx/1.18.0 | 服务器软件 |
| **Date** | 响应时间 | Wed, 01 Apr 2026 10:00:00 GMT | 服务器时间 |
| **Cache-Control** | 缓存策略 | max-age=3600 | 缓存3600秒 |
| **Set-Cookie** | 设置Cookie | sessionId=abc123 | 客户端存储 |
| **Access-Control-Allow-Origin** | 跨域 | * | 允许所有域名 |
| **Connection** | 连接管理 | keep-alive | 保持连接 |
| **ETag** | 资源标识 | "686897696a7c876b7e" | 资源版本号 |
| **Last-Modified** | 最后修改时间 | Wed, 01 Apr 2026 09:00:00 GMT | 用于缓存 |

---

**Content-Type常用值：**

```http
# JSON（最常用）
Content-Type: application/json

# 表单提交
Content-Type: application/x-www-form-urlencoded

# 文件上传
Content-Type: multipart/form-data

# HTML
Content-Type: text/html; charset=UTF-8

# 纯文本
Content-Type: text/plain

# XML
Content-Type: application/xml

# 图片
Content-Type: image/jpeg
Content-Type: image/png

# 二进制流
Content-Type: application/octet-stream
```

---

**Cache-Control详解：**

```http
# 不缓存
Cache-Control: no-cache, no-store, must-revalidate

# 缓存1小时
Cache-Control: max-age=3600

# 公共缓存（CDN可缓存）
Cache-Control: public, max-age=86400

# 私有缓存（只能浏览器缓存）
Cache-Control: private, max-age=3600

# 必须重新验证
Cache-Control: no-cache, must-revalidate
```

---

**HTTP状态码（必须掌握）：**

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| **200 OK** | 成功 | 正常返回数据 |
| **201 Created** | 已创建 | POST创建资源成功 |
| **204 No Content** | 无内容 | DELETE成功但无返回 |
| **301 Moved Permanently** | 永久重定向 | 网站迁移 |
| **302 Found** | 临时重定向 | 临时跳转 |
| **304 Not Modified** | 未修改 | 资源未变化，用缓存 |
| **400 Bad Request** | 请求错误 | 参数格式错误 |
| **401 Unauthorized** | 未认证 | 需要登录 |
| **403 Forbidden** | 禁止访问 | 权限不足 |
| **404 Not Found** | 未找到 | 资源不存在 |
| **405 Method Not Allowed** | 方法不允许 | GET/POST用错 |
| **500 Internal Server Error** | 服务器错误 | 代码异常 |
| **502 Bad Gateway** | 网关错误 | Nginx无法连接后端 |
| **503 Service Unavailable** | 服务不可用 | 服务器过载 |

---

**请求头（对应的）：**

```http
GET /api/user/1001 HTTP/1.1
Host: api.example.com
User-Agent: Mozilla/5.0 ...
Accept: application/json                    # 接受JSON
Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
Accept-Encoding: gzip, deflate, br
Connection: keep-alive
Cookie: sessionId=abc123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Referer: https://www.example.com/page
Content-Type: application/json              # POST/PUT时
Content-Length: 123
```

---

### Q2：请求方法GET为什么说是幂等的？

**考点：** HTTP幂等性、RESTful设计

**幂等性定义：**

```
多次执行同一操作，结果相同

数学定义：
f(f(x)) = f(x)

HTTP定义：
发送N次相同请求，服务器状态与发送1次相同
```

---

**HTTP方法幂等性对比：**

| 方法 | 幂等性 | 原因 | 示例 |
|------|--------|------|------|
| **GET** | ✅ 幂等 | 只读，不修改数据 | GET /user/1001 |
| **POST** | ❌ 非幂等 | 每次创建新资源 | POST /order（创建订单） |
| **PUT** | ✅ 幂等 | 更新为相同值 | PUT /user/1001 {age:25} |
| **DELETE** | ✅ 幂等 | 删除后再删，结果相同 | DELETE /user/1001 |
| **PATCH** | ❌ 可能非幂等 | 部分更新，看实现 | PATCH /user/1001 {age+1} |
| **HEAD** | ✅ 幂等 | 类似GET，只返回头 | HEAD /user/1001 |
| **OPTIONS** | ✅ 幂等 | 查询支持的方法 | OPTIONS /api/user |

---

**详细分析：**

#### GET（幂等）

```http
# 第1次请求
GET /user/1001
# 返回：{"id":1001,"name":"张三","age":25}

# 第2次请求
GET /user/1001
# 返回：{"id":1001,"name":"张三","age":25}（相同）

# 第N次请求
GET /user/1001
# 返回：相同

结论：GET只读取数据，不修改服务器状态，幂等
```

#### POST（非幂等）

```http
# 第1次请求
POST /order
{"product_id":1001,"quantity":1}
# 创建订单：order_id=1

# 第2次请求（相同参数）
POST /order
{"product_id":1001,"quantity":1}
# 创建订单：order_id=2（新订单！）

# 第N次请求
POST /order
# 每次创建新订单，order_id不同

结论：POST创建资源，每次调用都创建新的，非幂等
```

#### PUT（幂等）

```http
# 第1次请求
PUT /user/1001
{"age":25}
# 更新：age=25

# 第2次请求
PUT /user/1001
{"age":25}
# 更新：age=25（还是25，没变化）

# 第N次请求
PUT /user/1001
{"age":25}
# 结果：age始终是25

结论：PUT是全量更新，多次更新为相同值，结果相同，幂等
```

#### DELETE（幂等）

```http
# 第1次请求
DELETE /user/1001
# 删除成功，返回200

# 第2次请求
DELETE /user/1001
# 已删除，返回404（资源不存在）

# 第N次请求
DELETE /user/1001
# 返回404

结论：第1次删除成功，后续都是"资源不存在"，最终状态相同，幂等
```

---

**为什么设计成这样？**

```
1. GET幂等 → 可以放心重试、刷新、缓存
   浏览器刷新页面不会有副作用

2. POST非幂等 → 需要防重复提交
   表单提交后刷新会提示"重新提交表单"

3. PUT幂等 → 更新操作可以重试
   网络不稳定时，客户端可以重发

4. DELETE幂等 → 删除操作可以重试
   即使重复删除也不会有问题
```

---

**实战中的幂等性设计：**

```java
// ❌ POST非幂等，容易重复提交
@PostMapping("/order")
public Result createOrder(@RequestBody OrderDTO dto) {
    Order order = new Order();
    // ...
    orderMapper.insert(order);  // 每次都创建新订单
    return Result.ok(order.getId());
}

// ✅ POST + Token机制实现幂等
@PostMapping("/order")
public Result createOrder(@RequestBody OrderDTO dto, 
                          @RequestHeader("order-token") String token) {
    // 检查token（见之前的幂等性实现）
    if (!checkAndDeleteToken(token)) {
        return Result.fail("请勿重复提交");
    }
    
    // 创建订单
    Order order = new Order();
    orderMapper.insert(order);
    return Result.ok(order.getId());
}

// ✅ PUT天然幂等
@PutMapping("/user/{id}")
public Result updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
    User user = new User();
    user.setId(id);
    user.setAge(dto.getAge());
    userMapper.updateById(user);  // 多次更新，结果相同
    return Result.ok();
}
```

---

### Q3：下单接口的幂等性应该如何实现？前端传过来的信息如何保证是同一个订单？

**考点：** 接口幂等性设计

**问题场景：**

```
用户点击"提交订单"按钮
→ 网络卡顿，用户连点3次
→ 后端收到3个相同的请求
→ 创建了3个订单
→ 用户投诉：我只买了1件，为什么扣了3次钱？
```

---

**5种幂等性实现方案：**

#### 方案1：前端防抖（治标不治本）

```javascript
// 前端按钮防抖
let submitting = false;

function submitOrder() {
    if (submitting) {
        alert("正在提交，请勿重复点击");
        return;
    }
    
    submitting = true;
    
    axios.post('/api/order', orderData)
        .then(res => {
            alert("下单成功");
        })
        .finally(() => {
            submitting = false;  // 恢复按钮
        });
}

问题：
- 用户可以禁用JS
- 网络异常时，用户刷新页面会重新提交
- 恶意用户可以直接调用API
```

---

#### 方案2：唯一订单号（前端生成）

```java
// 前端生成唯一订单号
const orderNo = generateUUID();  // "550e8400-e29b-41d4-a716-446655440000"

axios.post('/api/order', {
    orderNo: orderNo,
    productId: 1001,
    quantity: 1
});

// 后端：唯一索引保证幂等
@PostMapping("/order")
public Result createOrder(@RequestBody OrderDTO dto) {
    Order order = new Order();
    order.setOrderNo(dto.getOrderNo());  // 前端传来的orderNo
    // ...
    
    try {
        orderMapper.insert(order);  // 唯一索引：uk_order_no
        return Result.ok(order.getId());
    } catch (DuplicateKeyException e) {
        // 订单号重复，说明已经提交过
        return Result.fail("订单已存在");
    }
}

// 数据库
ALTER TABLE `order` ADD UNIQUE INDEX uk_order_no(order_no);

优点：
✅ 简单易实现
✅ 依赖数据库保证幂等

缺点：
❌ 依赖前端（不安全）
❌ orderNo可能被篡改
```

---

#### 方案3：Token机制（推荐）

```java
// 步骤1：用户进入下单页，获取token
@GetMapping("/order/token")
public Result getOrderToken() {
    // 生成唯一token
    String token = UUID.randomUUID().toString();
    
    // 存入Redis，5分钟过期
    redisTemplate.opsForValue().set("order:token:" + token, "1", 5, TimeUnit.MINUTES);
    
    return Result.ok(token);
}

// 步骤2：提交订单时，带上token
@PostMapping("/order")
public Result createOrder(@RequestBody OrderDTO dto, 
                          @RequestHeader("order-token") String token) {
    // Lua脚本：原子性检查并删除token
    String luaScript = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    Long result = redisTemplate.execute(
        new DefaultRedisScript<>(luaScript, Long.class),
        Collections.singletonList("order:token:" + token),
        "1"
    );
    
    if (result == 0) {
        // token不存在或已使用
        return Result.fail("请勿重复提交");
    }
    
    // 创建订单
    Order order = new Order();
    // ...
    orderMapper.insert(order);
    
    return Result.ok(order.getId());
}

优点：
✅ 后端生成token，安全
✅ 一次性token，天然防重
✅ 性能好（Redis）

缺点：
❌ 需要两次请求（获取token + 提交）
```

**流程图：**

```
用户进入下单页
    ↓
GET /order/token → 返回token=abc123（Redis存储）
    ↓
用户填写信息
    ↓
POST /order + token=abc123
    ↓
后端检查token：
  - 存在 → 删除token → 创建订单 → 返回成功
  - 不存在 → 返回"请勿重复提交"
    ↓
用户再次提交（token已删除）
    ↓
返回"请勿重复提交"
```

---

#### 方案4：数据库唯一索引（业务字段）

```java
// 一人一单场景
ALTER TABLE `order` ADD UNIQUE INDEX uk_user_product(user_id, product_id);

@PostMapping("/seckill")
public Result seckill(@RequestBody SeckillDTO dto) {
    Order order = new Order();
    order.setUserId(dto.getUserId());
    order.setProductId(dto.getProductId());
    // ...
    
    try {
        orderMapper.insert(order);
        return Result.ok();
    } catch (DuplicateKeyException e) {
        return Result.fail("您已购买过该商品");
    }
}

优点：
✅ 数据库保证，绝对可靠
✅ 代码简单

缺点：
❌ 每次都要访问数据库
❌ 并发高时，大量异常
```

---

#### 方案5：Redis分布式锁

```java
@PostMapping("/seckill")
public Result seckill(@RequestBody SeckillDTO dto) {
    String lockKey = "lock:seckill:" + dto.getUserId() + ":" + dto.getProductId();
    RLock lock = redissonClient.getLock(lockKey);
    
    try {
        // 尝试获取锁
        boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
        
        if (!isLocked) {
            return Result.fail("请勿重复下单");
        }
        
        // 检查是否已下单
        Order exists = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, dto.getUserId())
                .eq(Order::getProductId, dto.getProductId())
        );
        
        if (exists != null) {
            return Result.fail("您已购买过该商品");
        }
        
        // 创建订单
        Order order = new Order();
        // ...
        orderMapper.insert(order);
        
        return Result.ok();
        
    } finally {
        lock.unlock();
    }
}

优点：
✅ 分布式环境可用
✅ 防止并发重复下单

缺点：
❌ 依赖Redis
❌ 代码复杂
```

---

**方案对比：**

| 方案 | 性能 | 安全性 | 复杂度 | 适用场景 |
|------|------|--------|--------|----------|
| **前端防抖** | 高 | 低 | 低 | 辅助手段 |
| **前端orderNo** | 高 | 低 | 低 | 信任前端 |
| **Token机制** | 高 | 高 | 中 | 通用场景 |
| **唯一索引** | 中 | 高 | 低 | 简单场景 |
| **分布式锁** | 低 | 高 | 高 | 秒杀场景 |

**推荐组合：**

```
Token机制（主） + 唯一索引（兜底） + 前端防抖（优化）
```

---

### Q4：HTTP的TCP的可靠性从哪些方面来保证呢？

**考点：** TCP可靠性机制

**TCP六大可靠性保证机制：**

---

#### 1️⃣ 序号与确认应答（SEQ & ACK）

**原理：**

```
发送端：
- 给每个数据包标记序号（SEQ）
- 等待接收端确认（ACK）

接收端：
- 收到数据包后，发送ACK确认
- ACK = SEQ + 数据长度
```

**示例：**

```
发送端                                接收端
  |                                      |
  |---- SEQ=100, LEN=50, DATA[100-149] --|→
  |                                      | 收到100-149
  |←--- ACK=150 ------------------------|
  |                                      |
  |---- SEQ=150, LEN=50, DATA[150-199] --|→
  |                                      | 收到150-199
  |←--- ACK=200 ------------------------|
  |                                      |

说明：
- SEQ=100：从第100字节开始
- LEN=50：这个包有50字节
- ACK=150：确认收到了100-149，期待下一个是150
```

---

#### 2️⃣ 超时重传

**原理：**

```
发送数据后，启动定时器
超时未收到ACK → 重传数据
```

**示例：**

```
发送端                                接收端
  |                                      |
  |---- SEQ=100, DATA ----------------->|→ 数据包丢失
  |                                      |
  | [等待ACK，超时3秒]                   |
  |                                      |
  |---- SEQ=100, DATA（重传）---------->|
  |                                      | 收到
  |←--- ACK=150 ------------------------|
```

**超时时间（RTO）计算：**

```
RTO = RTT + 4 * RTT偏差

RTT（Round-Trip Time）：往返时间
- 测量：发送数据到收到ACK的时间
- 动态调整：网络快时RTO短，网络慢时RTO长
```

---

#### 3️⃣ 流量控制（滑动窗口）

**问题：**

```
发送端发送速度太快
→ 接收端来不及处理
→ 数据丢失
```

**解决：滑动窗口（Sliding Window）**

```
接收端告诉发送端：我的接收窗口大小是N字节
发送端：一次最多发送N字节

接收端                                发送端
  |                                      |
  |←--- ACK=100, WIN=1000 --------------|  接收窗口1000字节
  |                                      |
  |                                      | 可以发送1000字节
  |                                      |
  |---- 发送500字节 ------------------→|
  |←--- ACK=600, WIN=500 --------------|  接收窗口还剩500
  |                                      |
  |                                      | 只能再发500字节
```

**窗口滑动：**

```
发送窗口：[已发送已确认][已发送未确认][可发送][不可发送]
           ←----已处理----→←--窗口大小--→

收到ACK后，窗口向右滑动：
[已发送已确认][可发送][不可发送]
        ←--窗口大小--→
```

---

#### 4️⃣ 拥塞控制

**问题：**

```
网络拥堵
→ 发送太多数据
→ 路由器丢包
→ 更拥堵
```

**解决：拥塞控制（Congestion Control）**

**四个算法：**

```
1. 慢启动（Slow Start）
   - 初始窗口很小（1个MSS）
   - 每收到1个ACK，窗口×2
   - 指数增长：1 → 2 → 4 → 8 → 16 ...
   - 达到阈值（ssthresh）后，进入拥塞避免

2. 拥塞避免（Congestion Avoidance）
   - 窗口线性增长
   - 每个RTT，窗口+1
   - 慢慢试探网络容量

3. 快速重传（Fast Retransmit）
   - 收到3个重复ACK
   - 立刻重传（不等超时）
   - 进入快速恢复

4. 快速恢复（Fast Recovery）
   - 窗口减半
   - 进入拥塞避免
```

**图解：**

```
窗口大小
    ↑
    |           拥塞避免（线性增长）
    |          ／
    |         ／ 
 16 |--------╱----------X（丢包）
    |       ╱           |
  8 |      ╱            |  快速恢复
    |     ╱             ↓ （窗口减半）
  4 |    ╱          ----╱----
    |   ╱          ╱
  2 |  ╱          ╱
    | ╱慢启动    ╱
  1 |╱（指数）  ╱拥塞避免
    +------------------→ 时间
```

---

#### 5️⃣ 校验和（Checksum）

**原理：**

```
发送端：计算数据的校验和
接收端：重新计算，对比校验和
  - 一致：数据完整
  - 不一致：数据损坏，丢弃
```

**示例：**

```
数据：[0x12, 0x34, 0x56, 0x78]
校验和：(0x12 + 0x34 + 0x56 + 0x78) % 256 = 0x14

TCP头：
| ... | Checksum: 0x14 | ... | DATA |

接收端：
重新计算：(0x12 + 0x34 + 0x56 + 0x78) % 256 = 0x14
对比：0x14 == 0x14 ✓ 数据正确
```

---

#### 6️⃣ 顺序保证

**问题：**

```
发送：包1 → 包2 → 包3
接收：包2 → 包1 → 包3（乱序）
```

**解决：序号重排**

```
接收端：
1. 收到包2（SEQ=200），缓存
2. 收到包1（SEQ=100），缓存
3. 收到包3（SEQ=300），缓存
4. 按SEQ排序：包1 → 包2 → 包3
5. 交给应用层（顺序正确）
```

---

**完整流程示例：**

```
客户端                                    服务器
  |                                          |
  |-- SYN, SEQ=100 ------------------------→| 1. 建立连接
  |←- SYN-ACK, SEQ=200, ACK=101 -----------| 
  |-- ACK, SEQ=101, ACK=201 ---------------→|
  |                                          |
  |-- SEQ=101, LEN=100, DATA -------------->| 2. 发送数据
  |                                          |  校验和✓
  |←- ACK=201, WIN=1000 --------------------| 3. 确认
  |                                          |
  |-- SEQ=201, LEN=50, DATA -------------→X | 4. 数据包丢失
  |                                          |
  | [超时3秒]                                |
  |                                          |
  |-- SEQ=201, LEN=50, DATA（重传）------->| 5. 重传
  |                                          |  校验和✓
  |←- ACK=251, WIN=950 ---------------------| 6. 确认
  |                                          |
```

---

**总结表格：**

| 机制 | 解决问题 | 实现方式 |
|------|---------|----------|
| **序号与ACK** | 数据丢失 | 每个包标记SEQ，接收端回复ACK |
| **超时重传** | ACK丢失 | 超时未收到ACK，重传数据 |
| **流量控制** | 接收端过载 | 滑动窗口，接收端控制发送速度 |
| **拥塞控制** | 网络拥堵 | 慢启动、拥塞避免、快速重传/恢复 |
| **校验和** | 数据损坏 | 计算校验和，验证数据完整性 |
| **顺序保证** | 数据乱序 | 按SEQ重排序 |

---

### Q5：TCP断开连接的时候为什么不是三次？

**考点：** 四次挥手原理

**四次挥手完整流程：**

```
Client                                    Server
  |                                          |
  |-- FIN, SEQ=100 -------------------------→| 第1次挥手
  |   （Client：我没数据要发了）              | Client进入FIN_WAIT_1
  |                                          |
  |←- ACK, SEQ=200, ACK=101 -----------------| 第2次挥手
  |   （Server：知道了，我收到你的FIN）      | Server进入CLOSE_WAIT
  |                                          | Client进入FIN_WAIT_2
  |                                          |
  |                                          | Server继续发送数据...
  |←- DATA, SEQ=200 -------------------------| （可能还有数据要发）
  |-- ACK, SEQ=101, ACK=250 ----------------→|
  |                                          |
  |←- FIN, SEQ=250, ACK=101 -----------------| 第3次挥手
  |   （Server：我也没数据要发了）            | Server进入LAST_ACK
  |                                          | Client进入TIME_WAIT
  |                                          |
  |-- ACK, SEQ=101, ACK=251 ----------------→| 第4次挥手
  |   （Client：知道了）                      | Server进入CLOSED
  |                                          |
  | [TIME_WAIT等待2MSL]                      |
  |                                          |
  ↓ 进入CLOSED                               |
```

---

**为什么是四次而不是三次？**

#### 核心原因：全双工通信，需要双方都关闭

```
TCP是全双工的：
- Client → Server：一条通道
- Server → Client：另一条通道

关闭连接：
- 第1、2次挥手：关闭Client → Server通道
- 第3、4次挥手：关闭Server → Client通道
```

#### 为什么第2次和第3次不能合并？

```
第2次挥手（ACK）：
  Server说："我知道你要关闭了"

第3次挥手（FIN）：
  Server说："我也要关闭了"

中间可能有延迟：
  Server收到Client的FIN后
  → 可能还有数据要发送
  → 发送完数据
  → 再发送自己的FIN
```

**示例：**

```
场景：HTTP请求/响应

Client                                    Server
  |                                          |
  |-- GET /api/data HTTP/1.1 --------------→| 请求
  |                                          |
  |-- FIN（我的请求发完了）-----------------→| 第1次挥手
  |                                          |
  |←- ACK（知道了，但我还没响应完）----------| 第2次挥手
  |                                          |
  |←- HTTP/1.1 200 OK -----------------------| Server继续发送响应
  |←- Content-Length: 10000 ------------------|
  |←- DATA... --------------------------------|
  |←- DATA... --------------------------------| （大响应体）
  |←- DATA...（完）--------------------------| 
  |                                          |
  |←- FIN（我的响应也发完了）----------------| 第3次挥手
  |                                          |
  |-- ACK（知道了）-------------------------→| 第4次挥手
```

---

**三次挥手可能吗？**

**可以！** 如果Server没有数据要发，ACK和FIN可以合并

```
Client                                    Server
  |                                          |
  |-- FIN, SEQ=100 -------------------------→| 第1次
  |                                          |
  |←- ACK+FIN, SEQ=200, ACK=101 -------------| 第2+3次（合并）
  |                                          |
  |-- ACK, SEQ=101, ACK=201 ----------------→| 第4次
```

**但实际情况：**
- 大部分时候Server还有数据要发
- TCP协议设计时考虑通用性
- 所以标准流程是四次挥手

---

**对比三次握手（为什么握手可以合并）：**

```
三次握手：
Client                                    Server
  |-- SYN --------------------------------→| 第1次
  |←- SYN+ACK（合并）----------------------| 第2次
  |-- ACK ---------------------------------→| 第3次

为什么第2次可以合并？
- Server收到SYN后，立刻回复SYN+ACK
- 没有"Server还有数据要发"的问题
- 所以可以合并

四次挥手：
- 收到FIN后，不能立刻回复FIN
- 因为可能还有数据要发
- 所以ACK和FIN分开
```

---

**状态转换图：**

```
Client状态：
ESTABLISHED → FIN_WAIT_1 → FIN_WAIT_2 → TIME_WAIT → CLOSED
               ↑            ↑            ↑
             发FIN       收ACK       收FIN，发ACK

Server状态：
ESTABLISHED → CLOSE_WAIT → LAST_ACK → CLOSED
               ↑            ↑          ↑
            收FIN，发ACK   发FIN     收ACK
```

---

### Q6：TIME_WAIT为什么要设置2MSL？

**考点：** TIME_WAIT状态、MSL

**MSL定义：**

```
MSL = Maximum Segment Lifetime（最大报文生存时间）

定义：一个TCP报文在网络中存活的最长时间
常见值：
- Linux：30秒或60秒
- Windows：120秒

2MSL = 2 * MSL = 60秒 或 120秒 或 240秒
```

---

**TIME_WAIT状态：**

```
四次挥手最后：

Client                                    Server
  |                                          |
  |←- FIN, SEQ=250 --------------------------| Server发送FIN
  |                                          |
  |-- ACK, SEQ=101, ACK=251 ----------------→| Client发送ACK
  |                                          |
  | [进入TIME_WAIT状态]                      | Server进入CLOSED
  |                                          |
  | [等待2MSL时间]                           |
  |                                          |
  ↓ 进入CLOSED                               |

问题：为什么Client要等待2MSL？
```

---

**原因1：确保最后的ACK能到达Server**

```
场景：最后的ACK丢失

Client                                    Server
  |                                          |
  |←- FIN, SEQ=250 --------------------------| Server发送FIN
  |                                          | 启动定时器
  |                                          |
  |-- ACK, SEQ=101, ACK=251 -------------→X | ACK丢失
  |                                          |
  | [进入TIME_WAIT]                          | [等待ACK，超时]
  |                                          |
  |←- FIN, SEQ=250（重传）-------------------| Server重传FIN
  |                                          |
  |-- ACK, SEQ=101, ACK=251 ----------------→| Client重发ACK
  |                                          | Server收到，进入CLOSED
  | [继续等待2MSL]                           |

时间计算：
- Client发送ACK → Server收到：1个MSL
- Server重传FIN → Client收到：1个MSL
- 总共：2MSL
```

**图解：**

```
时间轴：
         Client发ACK            Server重传FIN       Client再发ACK
            ↓                      ↓                    ↓
  |---------|---------|---------|---------|---------|
  0         MSL       2MSL
  
  |←--------ACK到达------→|←----FIN返回---→|
  |←---------2MSL---------→|

如果等待时间 < 2MSL：
- Server的重传FIN可能到达不了
- Server会一直等待，无法正常关闭
```

---

**原因2：防止旧连接的数据包干扰新连接**

```
场景：快速重用端口

旧连接：
Client:8080 → Server:80
关闭，但有延迟的数据包还在网络中

新连接（立刻复用端口）：
Client:8080 → Server:80

问题：
旧连接的延迟数据包 → 被新连接收到 → 数据错乱
```

**TIME_WAIT解决：**

```
Client                                    Server
  |                                          |
旧连接关闭                                   |
  | [TIME_WAIT等待2MSL]                      |
  |                                          |
  | 旧数据包在网络中（最多存活1个MSL）        |
  | ↓                                        |
  | [2MSL后，旧数据包一定过期了]             |
  |                                          |
新连接建立                                   |
  |-- SYN --------------------------------→| 此时旧数据包已经消失
  |←- SYN-ACK ------------------------------| 新连接数据不会混淆
  |-- ACK ---------------------------------→|
```

**图解：**

```
旧连接：
Client发送最后数据包（t=0）
         ↓
    [数据包存活MSL]
         ↓
    数据包过期（t=MSL）

TIME_WAIT等待2MSL：
  |-- MSL --|-- MSL --|
  |  数据包  |  缓冲   |
  | 可能存活 |  期间   |
  
  0        MSL       2MSL
  
t=2MSL时，旧数据包一定已经过期
```

---

**实际影响：**

```bash
# 查看TIME_WAIT连接
netstat -an | grep TIME_WAIT | wc -l
# 结果：10000+（很多！）

问题：
1. 占用端口（客户端端口有限：1024-65535）
2. 占用内存（每个连接约4KB）
3. 无法建立新连接（端口耗尽）
```

---

**为什么是2MSL而不是1MSL或3MSL？**

```
1MSL：不够
- 只能保证ACK到达
- 无法等待Server的重传FIN

3MSL：太长
- 2MSL已经足够
- 3MSL浪费时间和资源

2MSL：刚刚好
- 1个MSL：ACK到达Server
- 1个MSL：Server重传的FIN返回Client
```

---

**完整时序图：**

```
时间轴：
t=0   Client发送FIN
t=1   Server收到FIN，发送ACK
t=2   Client收到ACK，Server发送FIN
t=3   Client收到FIN，发送ACK，进入TIME_WAIT
t=3+MSL   最坏情况：ACK丢失，Server收不到
t=3+MSL   Server重传FIN
t=3+2MSL  Client收到重传的FIN，再发ACK
t=3+2MSL  TIME_WAIT结束，进入CLOSED

总结：
TIME_WAIT = 2MSL = 从发送最后ACK到连接真正关闭的时间
```

---

### Q7：TIME_WAIT过多？

**考点：** 高并发场景问题、系统调优

**问题现象：**

```bash
# 查看TIME_WAIT数量
netstat -an | grep TIME_WAIT | wc -l
# 结果：30000+

# 查看可用端口
cat /proc/sys/net/ipv4/ip_local_port_range
# 结果：32768 60999
# 可用端口：60999 - 32768 = 28231

问题：
- TIME_WAIT占用了所有端口
- 无法建立新连接
- 报错：Cannot assign requested address
```

---

**为什么会有大量TIME_WAIT？**

```
场景：高并发HTTP短连接

客户端                                    服务器
  |-- 建立连接 --------------------------→|
  |-- GET /api/data ----------------------→|
  |←- 200 OK, DATA ------------------------|
  |-- 主动关闭连接（发FIN）---------------→| 
  |                                          |
  | [进入TIME_WAIT，占用端口2MSL]           |
  |                                          |
  | 建立新连接（端口被占用，等待...）       |

特点：
- 短连接：请求完立刻关闭
- 客户端主动关闭 → TIME_WAIT在客户端
- 高并发：每秒1000+请求 → 每秒1000+个TIME_WAIT
- 2MSL=60秒 → 60秒内积累60000个TIME_WAIT
```

---

**解决方案：**

#### 方案1：调整内核参数（治标）

```bash
# /etc/sysctl.conf

# 1. 允许TIME_WAIT状态的socket被重新用于新的TCP连接
net.ipv4.tcp_tw_reuse = 1

# 2. 快速回收TIME_WAIT socket（慎用，可能导致问题）
net.ipv4.tcp_tw_recycle = 0  # 新内核已废弃，不推荐

# 3. 减少TIME_WAIT时间（修改FIN_TIMEOUT）
net.ipv4.tcp_fin_timeout = 30  # 默认60秒，改为30秒

# 4. 增加可用端口范围
net.ipv4.ip_local_port_range = 10000 65000  # 55000个端口

# 5. 允许更多TIME_WAIT bucket
net.ipv4.tcp_max_tw_buckets = 50000  # 默认180000

# 生效
sysctl -p
```

**参数说明：**

| 参数 | 作用 | 副作用 |
|------|------|--------|
| **tcp_tw_reuse** | 复用TIME_WAIT端口 | 需要时间戳支持 |
| **tcp_tw_recycle** | 快速回收TIME_WAIT | 可能导致NAT问题，已废弃 |
| **tcp_fin_timeout** | 减少FIN等待时间 | 可能导致连接提前关闭 |
| **ip_local_port_range** | 增加可用端口 | 无 |

---

#### 方案2：HTTP长连接（治本，推荐）

```http
# HTTP 1.1默认开启长连接
Connection: keep-alive

# 一个TCP连接可以发送多个HTTP请求
客户端                                    服务器
  |-- 建立连接 --------------------------→|
  |-- GET /api/data1 ---------------------→|
  |←- 200 OK, DATA1 -----------------------|
  |-- GET /api/data2 ---------------------→| 复用连接
  |←- 200 OK, DATA2 -----------------------|
  |-- GET /api/data3 ---------------------→| 复用连接
  |←- 200 OK, DATA3 -----------------------|
  |-- 关闭连接 ---------------------------→|
  |                                          |
  | [只有1个TIME_WAIT]                      |

效果：
- 短连接：1000请求 = 1000个TIME_WAIT
- 长连接：1000请求 = 1个TIME_WAIT（减少1000倍）
```

**配置示例：**

```nginx
# Nginx配置
http {
    keepalive_timeout 65;  # 长连接超时65秒
    keepalive_requests 100; # 每个长连接最多100个请求
}
```

```java
// HttpClient配置
HttpClient client = HttpClients.custom()
    .setConnectionManagerShared(true)
    .setMaxConnTotal(200)
    .setMaxConnPerRoute(20)
    .setKeepAliveStrategy((response, context) -> 30 * 1000)  // 30秒
    .build();
```

---

#### 方案3：连接池（应用层优化）

```java
// 数据库连接池
HikariConfig config = new HikariConfig();
config.setMaximumPoolSize(20);  // 最多20个连接
config.setMinimumIdle(5);       // 最少5个连接
config.setConnectionTimeout(30000);
config.setIdleTimeout(600000);

HikariDataSource dataSource = new HikariDataSource(config);

// 好处：
// - 连接复用，不需要频繁建立/关闭
// - 减少TIME_WAIT
```

---

#### 方案4：让服务端主动关闭（转移TIME_WAIT）

```
问题：客户端主动关闭 → TIME_WAIT在客户端 → 客户端端口耗尽

解决：让服务端主动关闭 → TIME_WAIT在服务端

服务端：
- 端口固定（如80）
- 不会耗尽端口
- TIME_WAIT影响小

实现：
- 服务端先发FIN
- 客户端被动关闭
```

**HTTP示例：**

```http
# 服务端发送Connection: close
HTTP/1.1 200 OK
Connection: close  # 告诉客户端：我要关闭了
Content-Length: 123

{...}

# 然后服务端主动关闭（发FIN）
# TIME_WAIT在服务端
```

---

**方案对比：**

| 方案 | 效果 | 成本 | 推荐 |
|------|------|------|------|
| **调整内核参数** | 缓解 | 低 | ⭐⭐ |
| **HTTP长连接** | 根治 | 低 | ⭐⭐⭐⭐⭐ |
| **连接池** | 很好 | 中 | ⭐⭐⭐⭐ |
| **服务端关闭** | 转移问题 | 低 | ⭐⭐⭐ |

**最佳实践：**

```
1. 应用层：使用HTTP长连接 + 连接池
2. 系统层：调整内核参数（tcp_tw_reuse=1，增大端口范围）
3. 监控：监控TIME_WAIT数量，及时告警
```

---

(由于内容过长，我将在下一个回复中继续完成"操作系统"和"算法与手撕代码"部分)
## 操作系统

### Q1：进程之间的通信方式？

**考点：** IPC（Inter-Process Communication）

**7种IPC方式：**

| 方式 | 原理 | 优点 | 缺点 | 使用场景 | 命令 |
|------|------|------|------|----------|------|
| **管道(Pipe)** | 半双工，父子进程 | 简单 | 只能父子进程 | Shell命令 | `ls \| grep` |
| **命名管道(FIFO)** | 有名字的管道 | 任意进程 | 半双工 | 进程间通信 | `mkfifo` |
| **消息队列** | 消息链表 | 异步 | 有大小限制 | 异步通信 | `ipcs -q` |
| **共享内存** | 直接访问同一块内存 | 最快 | 需要同步 | 大数据传输 | `ipcs -m` |
| **信号量** | PV操作 | 同步 | 只能传递信号 | 进程同步 | `ipcs -s` |
| **信号(Signal)** | 异步通知 | 简单 | 信息少 | kill命令 | `kill -9` |
| **Socket** | 网络通信 | 跨主机 | 复杂 | 网络应用 | `netstat` |

---

### Q2：Socket会有哪些过程？服务端IO多路复用？

**Socket通信完整流程：**

```
服务端                                    客户端
  |                                          |
socket()      创建套接字                 socket()
  ↓                                          ↓
bind()        绑定IP:Port                   |
  ↓                                          |
listen()      监听                           |
  ↓                                          |
accept()      阻塞等待        ←--------  connect()  连接
  ↓                                          ↓
read()        读取数据        ←--------  write()    发送
  ↓                                          ↓
write()       发送数据        -------->  read()     接收
  ↓                                          ↓
close()       关闭                       close()
```

**epoll（IO多路复用）：**

```c
int epfd = epoll_create(1024);
struct epoll_event ev, events[1024];

// 添加监听socket
ev.events = EPOLLIN;
ev.data.fd = listenfd;
epoll_ctl(epfd, EPOLL_CTL_ADD, listenfd, &ev);

while (1) {
    int nready = epoll_wait(epfd, events, 1024, -1);
    
    for (int i = 0; i < nready; i++) {
        if (events[i].data.fd == listenfd) {
            // 新连接
            int connfd = accept(listenfd, NULL, NULL);
            ev.data.fd = connfd;
            epoll_ctl(epfd, EPOLL_CTL_ADD, connfd, &ev);
        } else {
            // 可读事件
            read(events[i].data.fd, buffer, sizeof(buffer));
        }
    }
}
```

**select/poll/epoll对比：**

| 特性 | select | poll | epoll |
|------|--------|------|-------|
| **最大连接数** | 1024 | 无限制 | 无限制 |
| **性能** | O(n) | O(n) | O(1) |
| **跨平台** | ✅ | ✅ | ❌（Linux） |

---

### Q3：怎么向进程发送信号？

**3种方式：**

```bash
# 1. kill命令
kill -15 1234       # SIGTERM（优雅停止）
kill -9 1234        # SIGKILL（强制杀死）

# 2. killall（按名字）
killall java

# 3. 快捷键
Ctrl+C  # SIGINT
Ctrl+Z  # SIGTSTP
```

**常用信号：**

| 信号 | 值 | 含义 | 能否捕获 |
|------|-----|------|---------|
| SIGINT | 2 | 中断（Ctrl+C） | ✅ |
| SIGKILL | 9 | 强制杀死 | ❌ |
| SIGTERM | 15 | 终止（默认） | ✅ |

---

### Q4：线程继承进程哪些资源？哪些资源是进程独享的？

**线程共享进程的资源：**

```
✅ 代码段（.text）
✅ 数据段（.data、.bss）
✅ 堆内存（heap）
✅ 全局变量
✅ 文件描述符
✅ 信号处理器
```

**线程独享的资源：**

```
❌ 线程ID（TID）
❌ 栈（stack）
❌ 寄存器
❌ 程序计数器（PC）
❌ errno变量
```

---

### Q5：线程比进程轻量，为什么轻量？

**4个方面的"轻量"：**

#### 1. 创建/销毁快

```
进程创建：
- 复制整个地址空间
- 复制页表
- 时间：毫秒级

线程创建：
- 只分配栈空间
- 共享进程资源
- 时间：微秒级

速度：线程创建比进程快100-1000倍
```

#### 2. 切换快

```
进程切换：
- 切换页表
- 刷新TLB
- 缓存失效
- 慢

线程切换：
- 不需要切换页表
- 同一地址空间
- 快（快10-100倍）
```

#### 3. 通信快

```
进程通信：
- 需要IPC（管道、消息队列）
- 需要系统调用
- 需要拷贝数据

线程通信：
- 直接访问共享内存
- 无需系统调用
- 无需拷贝
- 快1000倍+
```

#### 4. 内存占用小

```
100个进程：100 * 50MB = 5GB
100个线程：50MB + 100 * 8MB = 850MB

节省：83%
```

---

### Q6：系统调用是什么意思？系统调用的过程？

**系统调用：** 用户程序访问内核资源的接口

**完整过程（10步）：**

```
1. 用户程序调用read()
2. 触发软中断（int 0x80）
3. CPU切换到内核态
4. 保存用户态上下文
5. 查系统调用表
6. 跳转到sys_read()
7. 执行内核函数
8. 将结果写入寄存器
9. 恢复用户态上下文
10. 切换回用户态
```

**图解：**

```
用户态                             内核态
  |                                  |
  | read(fd, buf, size);             |
  | ↓ int 0x80                       |
  +--------------------------------→ |
                                     | sys_read() {
                                     |   // 读取文件
                                     | }
  ←--------------------------------+ |
  | 继续执行                         |
```

---

### Q7：用户态切换到内核态除了系统调用还有哪些方式？

**3种方式：**

| 方式 | 触发者 | 示例 | 特点 |
|------|--------|------|------|
| **系统调用** | 用户程序主动 | read、write | 可预测 |
| **异常** | CPU检测到错误 | 除零、缺页 | 被动 |
| **中断** | 外部硬件 | 时钟、键盘 | 异步 |

---

### Q8：中断是由什么引起的？

**中断分类：**

#### 硬件中断

```
时钟中断：定时器到期，触发进程调度
IO中断：键盘、鼠标、网卡、硬盘
电源中断：电源异常
```

#### 软件中断

```
异常：除零、缺页、段错误
系统调用：int 0x80、syscall
```

---

## 算法与数据结构

### Q1：判断树是否是完全二叉树

**思路：** BFS层序遍历 + 标记null节点

**规则：** 遇到第一个null后，后面不能再有非null节点

**代码：**

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
            if (hasNull) {
                return false;  // 之前遇到null，现在又有非null
            }
            queue.offer(node.left);
            queue.offer(node.right);
        }
    }
    
    return true;
}
```

**测试：**

```
完全二叉树：
      1
     / \
    2   3
   / \  /
  4  5 6
→ true

非完全二叉树：
      1
     / \
    2   3
   /     \
  4       5
→ false
```

**时间复杂度：** O(n)  
**空间复杂度：** O(n)

---

### Q2：TopK问题

**方案1：堆（适合海量数据）**

```java
// 找第K大的元素
public int findKthLargest(int[] nums, int k) {
    // 小顶堆，保留最大的k个元素
    PriorityQueue<Integer> heap = new PriorityQueue<>();
    
    for (int num : nums) {
        heap.offer(num);
        if (heap.size() > k) {
            heap.poll();  // 移除最小的
        }
    }
    
    return heap.peek();  // 堆顶就是第k大
}

// 时间复杂度：O(n log k)
// 空间复杂度：O(k)
```

**为什么用小顶堆？**

```
找第K大 → 用小顶堆

nums = [3, 2, 1, 5, 6, 4], k = 2

过程：
[3]                  
[2, 3]               
[2, 3] → poll(1)     删除最小1
[3, 5] → poll(3)     删除最小3
[5, 6]               

堆顶：5 = 第2大
```

---

**方案2：快速选择**

```java
public int findKthLargest(int[] nums, int k) {
    return quickSelect(nums, 0, nums.length - 1, k);
}

private int quickSelect(int[] nums, int left, int right, int k) {
    if (left == right) return nums[left];
    
    int pivotIndex = left + new Random().nextInt(right - left + 1);
    int pivot = nums[pivotIndex];
    
    // 分区：大的放左边
    swap(nums, pivotIndex, right);
    int storeIndex = left;
    
    for (int i = left; i < right; i++) {
        if (nums[i] > pivot) {
            swap(nums, i, storeIndex++);
        }
    }
    swap(nums, storeIndex, right);
    
    // 判断位置
    if (storeIndex == k - 1) {
        return nums[storeIndex];
    } else if (storeIndex < k - 1) {
        return quickSelect(nums, storeIndex + 1, right, k);
    } else {
        return quickSelect(nums, left, storeIndex - 1, k);
    }
}

// 时间复杂度：O(n) 平均
// 空间复杂度：O(1)
```

---

### Q3：有序数组转平衡二叉搜索树

**思路：** 选择中间元素作为根，递归构建

**代码：**

```java
public TreeNode sortedArrayToBST(int[] nums) {
    if (nums == null || nums.length == 0) return null;
    return buildTree(nums, 0, nums.length - 1);
}

private TreeNode buildTree(int[] nums, int left, int right) {
    if (left > right) return null;
    
    // 选择中间元素
    int mid = left + (right - left) / 2;
    TreeNode root = new TreeNode(nums[mid]);
    
    // 递归构建左右子树
    root.left = buildTree(nums, left, mid - 1);
    root.right = buildTree(nums, mid + 1, right);
    
    return root;
}
```

**示例：**

```
输入：[-10, -3, 0, 5, 9]

输出：
      0
     / \
   -3   9
   /   /
 -10  5
```

**时间复杂度：** O(n)  
**空间复杂度：** O(log n)

---

## 项目深度挖掘

### Q1：你这个系统为什么对消息的可达性要求这么高？

**答题模板：**

```
我的项目是即时通讯系统，对消息可达性要求高的原因：

1. 业务场景：
   - 客服聊天：用户咨询必须收到回复
   - 订单通知：支付、发货等重要通知
   - 系统消息：账户安全提醒

2. 业务影响：
   - 消息丢失 → 用户投诉 → 差评
   - 通知丢失 → 用户不知道状态 → 客诉

3. 实现方式：
   - Redis Stream持久化
   - ACK确认机制
   - pending list崩溃恢复

4. 数据支撑：
   - 可达率：99.9%+
   - 延迟：<100ms
```

---

### Q2：简单介绍一下延迟双删流程

**问题：** 缓存一致性

**延迟双删方案：**

```java
public void updateUser(User user) {
    // 1. 删除缓存
    redisTemplate.delete("user:" + user.getId());
    
    // 2. 更新数据库
    userMapper.update(user);
    
    // 3. 延迟后再次删除
    new Thread(() -> {
        try {
            Thread.sleep(500);
            redisTemplate.delete("user:" + user.getId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }).start();
}
```

**为什么要延迟双删？**

```
时间线：
T1: 线程A删除缓存
T2: 线程B查询，缓存miss，查数据库（旧数据）
T3: 线程A更新数据库（新数据）
T4: 线程B把旧数据写回缓存 ← 不一致
T5: 线程A延迟后再次删除缓存 ✅
```

---

## 面试策略与技巧

### 1. 自我介绍模板（1-2分钟）

```
面试官您好，我叫XXX，目前是XXX大学XXX专业大三学生。

技术栈：
- 熟悉Java后端，掌握Spring Boot、MyBatis-Plus
- 熟悉MySQL索引优化和Redis缓存
- 了解多线程并发和JVM调优

项目经验：
我做过苍穹外卖项目，主要负责：
- 核心功能：秒杀下单，使用Redis Stream异步处理，TPS从120提升到2500
- 性能优化：Redisson分布式锁解决超卖，响应时间从890ms降到15ms
- 技术难点：实现消息的可达性、有序性、幂等性

我对后端开发很感兴趣，希望能加入作业帮，学习更多实战经验。
```

---

### 2. 项目介绍STAR法则

**S**ituation（背景）  
**T**ask（任务）  
**A**ction（行动）  
**R**esult（结果）

```
S：外卖系统，面临秒杀高并发问题
T：1000+并发下，保证库存不超卖且系统稳定
A：使用Redisson分布式锁、Lua脚本、Redis Stream异步下单
R：TPS从120提升到2500，响应时间从890ms降到15ms，0超卖
```

---

### 3. 不会的问题怎么答

```
❌ 不好："这个我不会"
✅ 好的："这个我了解得不够深入，但我知道它和XXX相关...
         如果有机会，我会重点学习这部分。"
```

---

### 4. 反问问题

```
✅ 推荐：
1. 如果有幸加入，主要负责哪方面工作？
2. 团队目前在技术上有什么挑战？
3. 公司对实习生有什么期望？

❌ 避免：
1. 薪资待遇？（HR会谈）
2. 加班多吗？（敏感）
3. 没有问题（不用心）
```

---

## 🎯 复习重点

**高频考点（必须掌握）：**

1. ⭐⭐⭐ HashMap/ConcurrentHashMap底层
2. ⭐⭐⭐ MySQL索引（B+树、回表、失效）
3. ⭐⭐⭐ Redis数据结构、持久化、缓存一致性
4. ⭐⭐⭐ TCP三次握手/四次挥手、TIME_WAIT
5. ⭐⭐⭐ 进程/线程、IPC、IO多路复用
6. ⭐⭐ JVM GC、垃圾回收算法
7. ⭐⭐ 系统调用、中断
8. ⭐⭐ 算法（TopK、完全二叉树）

---

## 📚 推荐资料

**书籍：**
1. 《深入理解Java虚拟机》- JVM
2. 《Java并发编程的艺术》- 并发
3. 《高性能MySQL》- 数据库
4. 《Redis设计与实现》- Redis

**刷题：**
1. LeetCode Hot 100
2. 牛客网面经

---

## 快速记忆口诀

**HashMap：**
```
数组加链表，红黑树优化
哈希冲突用拉链，链长超8就转树
```

**MySQL索引失效：**
```
函数计算列上用，类型转换隐式坑
前缀模糊%开头，OR条件有一空
最左前缀不能断，索引失效要记牢
```

**TCP可靠性：**
```
序号ACK保顺序，超时重传防丢失
流量控制滑窗口，拥塞控制慢启动
```

**四次挥手：**
```
主动关闭发FIN，被动确认回ACK
被动关闭发FIN，主动确认回ACK
TIME_WAIT等2MSL，确保ACK能到达
```

---

**祝你面试顺利！加油！** 💪💪💪

---

**文档版本：** v2.0  
**更新时间：** 2026年4月  
**作者：** Claude & Maorou  
**用途：** 作业帮暑期实习后端开发面试准备
