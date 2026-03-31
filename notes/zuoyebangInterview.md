好的！我帮你整理成专项突破文档：

```markdown
# 作业帮暑期实习后端开发面经汇总

> **面试时间：** 2025年3月25日  
> **岗位：** Java后端开发暑期实习  
> **难度：** ⭐⭐⭐⭐ (高频考点密集，深度较深)

---

## 📋 目录

1. [Java基础与集合](#java基础与集合)
2. [JVM与GC](#jvm与gc)
3. [MySQL数据库](#mysql数据库)
4. [Redis缓存](#redis缓存)
5. [网络协议](#网络协议)
6. [操作系统](#操作系统)
7. [算法与手撕代码](#算法与手撕代码)
8. [项目深挖](#项目深挖)
9. [答题策略](#答题策略)

---

## Java基础与集合

### Q1：HashMap和ConcurrentHashMap实现区别？

**考点：** 并发安全、底层数据结构

**答题要点：**

| 对比项 | HashMap | ConcurrentHashMap |
|--------|---------|-------------------|
| **线程安全** | ❌ 不安全 | ✅ 安全 |
| **底层结构** | 数组+链表+红黑树 | 数组+链表+红黑树 |
| **锁机制** | 无 | JDK7: 分段锁（Segment）<br>JDK8: CAS + synchronized |
| **性能** | 高（单线程） | 中（多线程场景更优） |
| **null值** | key和value都可以 | 都不可以 |

**JDK8 ConcurrentHashMap核心改进：**
```java
// 1. 取消Segment，锁粒度降低到数组的每个桶
// 2. put时：CAS + synchronized锁头节点
synchronized (f) {  // f是数组某个位置的头节点
    // 插入逻辑
}

// 3. get时：无锁（volatile保证可见性）
```

---

### Q2：ConcurrentHashMap中分段锁有什么优势（为什么要用它）？

**考点：** 并发优化、锁粒度

**答案：**

**JDK7分段锁（Segment）：**
```
整个Map = 16个Segment
每个Segment = 一个小HashMap + 一把锁

好处：
1. 16个线程可以同时操作不同的Segment，并发度提升16倍
2. 相比整个Map一把大锁，锁粒度更细
```

**JDK8进一步优化：**
```
取消Segment → 直接锁数组的每个桶（Node）
并发度 = 数组长度（理论上可以达到数组大小）
```

**对比：**
```
Hashtable：整个表一把锁 → 并发度=1
JDK7 ConcurrentHashMap：分段锁 → 并发度=16
JDK8 ConcurrentHashMap：桶级锁 → 并发度=数组长度
```

---

### Q3：CAP、写时复制（Copy-On-Write）？

**考点：** 并发容器、CAP理论

**写时复制（CopyOnWriteArrayList）：**
```java
// 核心思想：写时复制，读不加锁
public boolean add(E e) {
    final ReentrantLock lock = this.lock;
    lock.lock();  // 写操作加锁
    try {
        Object[] elements = getArray();
        int len = elements.length;
        Object[] newElements = Arrays.copyOf(elements, len + 1);  // 复制
        newElements[len] = e;
        setArray(newElements);  // 替换
        return true;
    } finally {
        lock.unlock();
    }
}

// 读操作不加锁
public E get(int index) {
    return get(getArray(), index);  // 直接读
}
```

**适用场景：**
- ✅ 读多写少（如系统配置、黑名单）
- ❌ 写多场景（每次写都复制数组，浪费内存）

**CAP理论：**
- **C**onsistency（一致性）
- **A**vailability（可用性）
- **P**artition tolerance（分区容错性）

分布式系统只能满足其中两个。

---

## JVM与GC

### Q1：Java中的GC介绍一下

**考点：** 垃圾回收机制、GC算法

**答题框架：**

#### 1. 判断垃圾（哪些对象要回收）

| 方法 | 原理 | 缺点 |
|------|------|------|
| **引用计数** | 对象被引用+1，引用失效-1 | 循环引用问题 |
| **可达性分析** | 从GC Roots出发，不可达的就是垃圾 | Java采用 |

**GC Roots包括：**
- 虚拟机栈中引用的对象
- 方法区中静态属性引用的对象
- 方法区中常量引用的对象
- 本地方法栈中引用的对象

#### 2. 垃圾回收算法

| 算法 | 原理 | 适用场景 |
|------|------|----------|
| **标记-清除** | 标记垃圾→清除 | 老年代 |
| **标记-整理** | 标记垃圾→整理内存 | 老年代 |
| **复制算法** | 复制存活对象到另一块 | 新生代（存活少） |
| **分代收集** | 新生代用复制，老年代用标记整理 | 主流 |

#### 3. 常见垃圾收集器

**新生代：**
- Serial（单线程）
- ParNew（多线程）
- Parallel Scavenge（吞吐量优先）

**老年代：**
- Serial Old
- Parallel Old
- CMS（低延迟）

**全堆：**
- **G1**（JDK9默认，分区收集）
- **ZGC**（超低延迟，<1ms）

**回答模板：**
```
Java的GC主要分为三个步骤：

1. 判断垃圾：通过可达性分析，从GC Roots出发标记可达对象
2. 回收垃圾：采用分代收集策略
   - 新生代用复制算法（Eden + 2个Survivor）
   - 老年代用标记-整理算法
3. 垃圾收集器：
   - 项目中使用G1收集器，它能预测停顿时间
   - 适合大堆内存场景，停顿可控
```

---

## MySQL数据库

### Q1：数据表找哪些字段做索引？

**考点：** 索引设计原则

**答题要点：**

**适合建索引的字段：**
1. ✅ **WHERE子句中的字段**（查询条件）
2. ✅ **JOIN ON的关联字段**
3. ✅ **ORDER BY / GROUP BY的字段**
4. ✅ **区分度高的字段**（如手机号、邮箱）
5. ✅ **经常作为查询返回的字段**（覆盖索引）

**不适合建索引的字段：**
1. ❌ 区分度低（如性别：只有男/女）
2. ❌ 频繁更新的字段（维护成本高）
3. ❌ 数据量很小的表（<1000行）
4. ❌ text、blob等大字段

**示例：**
```sql
-- 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY,           -- 主键索引
    phone VARCHAR(20) UNIQUE,        -- 唯一索引（登录用）
    email VARCHAR(50),
    age INT,
    status TINYINT,
    create_time DATETIME,
    
    INDEX idx_phone (phone),         -- 登录查询
    INDEX idx_status_time (status, create_time)  -- 联合索引（后台查询）
);
```

---

### Q2：普通索引和主键索引有什么区别？

**考点：** 聚簇索引与二级索引

**核心区别：**

| 对比项 | 主键索引（聚簇索引） | 普通索引（二级索引） |
|--------|---------------------|---------------------|
| **叶子节点** | 存储整行数据 | 存储索引列值 + 主键ID |
| **数量** | 一张表只能有一个 | 可以有多个 |
| **查询效率** | 高（一次查询） | 可能需要回表 |
| **存储** | 数据按主键顺序存储 | 单独的索引树 |

**图解：**
```
主键索引树（id）：
    10
   /  \
  5    15
叶子节点：[id=5, name=张三, age=25, ...]  ← 完整数据

普通索引树（name）：
    李四
   /   \
  张三  王五
叶子节点：[name=张三, id=5]  ← 只有name和id
          ↓
      需要回表（拿着id=5去主键索引树查完整数据）
```

**什么是回表？**
```sql
SELECT * FROM user WHERE name = '张三';

步骤：
1. 查name索引树 → 找到 id=5
2. 拿着id=5去主键索引树 → 查到完整数据
   ↑ 这一步叫"回表"
```

**如何避免回表？** → 覆盖索引
```sql
-- 只查id和name，不需要回表
SELECT id, name FROM user WHERE name = '张三';
```

---

### Q3：WHERE后的字段是索引但是还是很慢？

**考点：** 索引失效场景

**常见原因：**

#### 1. 索引失效

```sql
-- ❌ 在索引列上使用函数
SELECT * FROM user WHERE YEAR(create_time) = 2024;

-- ✅ 改写
SELECT * FROM user WHERE create_time >= '2024-01-01' AND create_time < '2025-01-01';

-- ❌ 隐式类型转换
SELECT * FROM user WHERE phone = 13800138000;  -- phone是varchar

-- ✅ 改写
SELECT * FROM user WHERE phone = '13800138000';

-- ❌ 前缀模糊查询
SELECT * FROM user WHERE name LIKE '%张%';

-- ✅ 改写（如果可以）
SELECT * FROM user WHERE name LIKE '张%';
```

#### 2. 联合索引最左前缀失效

```sql
-- 索引：idx(a, b, c)

-- ✅ 走索引
WHERE a = 1 AND b = 2 AND c = 3
WHERE a = 1 AND b = 2
WHERE a = 1

-- ❌ 不走索引
WHERE b = 2 AND c = 3  -- 缺少a
WHERE c = 3            -- 缺少a和b
```

#### 3. 数据量问题

```sql
-- 表有1000万行，索引区分度低
SELECT * FROM user WHERE status = 1;  -- status只有0和1
-- MySQL优化器可能认为全表扫描更快
```

#### 4. 回表成本太高

```sql
-- 即使走索引，但返回数据太多，回表次数多
SELECT * FROM user WHERE age > 18;  -- 90%的数据都满足
-- MySQL可能选择全表扫描
```

**排查方法：**
```sql
EXPLAIN SELECT * FROM user WHERE name = '张三';

-- 重点看：
-- type: ALL（全表扫描） / index（索引扫描） / ref（索引查找）
-- key: 实际使用的索引
-- rows: 扫描行数
-- Extra: Using filesort（需要排序） / Using temporary（使用临时表）
```

---

### Q4：什么时候要开启事务？

**考点：** 事务使用场景

**答案：**

**必须开启事务的场景：**

1. **多个数据库操作必须同时成功或失败**
```java
// 转账：扣A的钱 + 加B的钱
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    // 扣钱
    accountMapper.deduct(fromId, amount);
    // 加钱
    accountMapper.add(toId, amount);
    // 任何一步失败，都要回滚
}
```

2. **秒杀、下单等业务**
```java
@Transactional
public void createOrder(Long userId, Long productId) {
    // 1. 扣库存
    productMapper.deductStock(productId);
    // 2. 创建订单
    orderMapper.insert(order);
    // 3. 扣减用户余额
    userMapper.deductBalance(userId, order.getAmount());
}
```

3. **数据一致性要求高的场景**
```java
// 修改用户信息 + 记录操作日志
@Transactional
public void updateUser(User user) {
    userMapper.update(user);
    logMapper.insert(log);  // 必须同时成功
}
```

**不需要事务的场景：**
- 单条查询
- 单条插入/更新（且不影响其他数据）
- 只读操作

---

### Q5：开启事务忘了提交有什么问题？

**考点：** 事务管理、死锁

**问题：**

1. **长事务占用连接**
```java
@Transactional
public void longTask() {
    // 执行10分钟...
    // 这10分钟内，数据库连接一直被占用
    // 并发高时，连接池耗尽
}
```

2. **锁等待超时**
```sql
-- 线程A
BEGIN;
UPDATE user SET age = 20 WHERE id = 1;  -- 加锁
-- 忘记提交，一直持有锁

-- 线程B
UPDATE user SET age = 30 WHERE id = 1;  -- 等待锁
-- 超时报错：Lock wait timeout exceeded
```

3. **脏读/幻读**
```sql
-- 事务A
BEGIN;
SELECT * FROM user WHERE id = 1;  -- age=20
-- 不提交，一直开着

-- 事务B
UPDATE user SET age = 30 WHERE id = 1;
COMMIT;

-- 事务A再查
SELECT * FROM user WHERE id = 1;  -- 还是20（可重复读）
-- 但实际数据库已经是30了
```

4. **Undo日志膨胀**
```
事务不提交 → Undo日志一直保留 → 占用大量磁盘空间
```

**解决方案：**
```java
// 1. 使用@Transactional，Spring自动管理
@Transactional
public void method() {
    // ...
}  // 方法结束自动提交/回滚

// 2. 手动管理
try {
    connection.setAutoCommit(false);
    // 业务逻辑
    connection.commit();
} catch (Exception e) {
    connection.rollback();
} finally {
    connection.close();  // 一定要关闭
}

// 3. 设置超时
@Transactional(timeout = 30)  // 30秒超时
```

---

### Q6：有没有利用MySQL实现一些锁？在什么场景下会使用？

**考点：** MySQL锁机制、实战应用

**答案：**

#### 场景1：悲观锁（FOR UPDATE）

```java
// 秒杀扣库存
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
}
```

```sql
-- Mapper
SELECT * FROM product WHERE id = #{id} FOR UPDATE;
```

**特点：**
- 查询时加**排他锁**
- 其他事务必须等待
- 适合**写多**场景

---

#### 场景2：乐观锁（版本号）

```java
// 修改用户信息
public boolean updateUser(User user) {
    // 带版本号更新
    int rows = userMapper.updateWithVersion(user);
    
    if (rows == 0) {
        // 版本号不匹配，更新失败
        return false;
    }
    return true;
}
```

```sql
-- Mapper
UPDATE user 
SET name = #{name}, 
    age = #{age}, 
    version = version + 1
WHERE id = #{id} 
  AND version = #{version};  -- 版本号校验
```

**特点：**
- 不加锁，用版本号判断
- 适合**读多写少**场景

---

### Q7：乐观锁和悲观锁有没有了解？什么时候用？

**考点：** 锁的选择

| 对比项 | 乐观锁 | 悲观锁 |
|--------|--------|--------|
| **思想** | 认为不会冲突，不加锁 | 认为会冲突，先加锁 |
| **实现** | 版本号、CAS | FOR UPDATE、synchronized |
| **性能** | 高（无锁） | 低（等待锁） |
| **适用场景** | 读多写少 | 写多读少 |
| **失败处理** | 重试 | 等待 |

**使用场景：**

```
乐观锁：
✅ 用户信息修改（读多写少）
✅ 商品详情更新
✅ 文章点赞数

悲观锁：
✅ 秒杀扣库存（写多）
✅ 抢票
✅ 转账
```

---

## Redis缓存

### Q1：Redis用得多吗？搭建过Redis集群吗？

**考点：** Redis实战经验

**答题模板：**

```
在我的项目中Redis主要用于：

1. 缓存热点数据
   - 商品详情、用户信息
   - 减少数据库压力

2. 分布式锁
   - 使用Redisson实现秒杀一人一单

3. 消息队列
   - Redis Stream实现异步下单

集群方面：
- 本地开发：单机Redis
- 生产环境：Redis Sentinel（哨兵模式）
  - 1主2从，自动故障转移
  - 保证高可用
```

---

### Q2：Redis中用得最多的数据结构是什么？为什么要用Hash，有什么特殊考虑吗？

**考点：** Redis数据结构选择

**常用数据结构：**

| 数据结构 | 使用场景 | 项目中的应用 |
|---------|---------|-------------|
| **String** | 简单KV、计数器 | 库存数量、验证码 |
| **Hash** | 对象存储 | 用户信息、商品详情 |
| **List** | 消息队列、排行榜 | 最新评论 |
| **Set** | 去重、集合运算 | 用户标签、一人一单判断 |
| **ZSet** | 排行榜 | 商品销量排行 |

**为什么用Hash存对象？**

```redis
# ❌ 方式1：String存整个对象（JSON）
SET user:1001 '{"name":"张三","age":25,"city":"北京"}'

问题：
1. 修改age需要整个序列化/反序列化
2. 占用空间大

# ✅ 方式2：Hash存对象（字段分开）
HSET user:1001 name "张三"
HSET user:1001 age 25
HSET user:1001 city "北京"

优势：
1. 只修改单个字段：HSET user:1001 age 26
2. 节省空间（ziplist编码）
3. 语义清晰
```

---

### Q3：保证消息的可达性、可靠性、有序性、幂等性指的是什么？具体怎么实现的？

**考点：** 消息队列核心特性

#### 1. 可达性（消息不丢失）

```
生产者 → Redis Stream → 消费者

保证：
1. 生产者：XADD成功才返回（Redis持久化）
2. 消费者：ACK机制（XACK）
3. pending list：未ACK的消息可恢复
```

#### 2. 可靠性（数据一致性）

```java
// 1. 事务保证
@Transactional
public void handleOrder(Order order) {
    // 扣库存 + 创建订单 + 扣余额
    // 要么全成功，要么全失败
}

// 2. 幂等性保证（见下）
```

#### 3. 有序性

```
问题：多线程消费，顺序乱了

解决：
1. 单线程消费（简单但性能低）
2. 按userId分区（相同用户的消息发到同一个消费者）
3. 版本号/时间戳（业务层排序）
```

#### 4. 幂等性

```java
// 方式1：唯一索引
ALTER TABLE `order` ADD UNIQUE INDEX uk_user_product(user_id, product_id);

// 方式2：Redis去重
String key = "order:created:" + userId + ":" + productId;
Boolean success = redisTemplate.opsForValue()
    .setIfAbsent(key, "1", 1, TimeUnit.DAYS);

if (!success) {
    return; // 已处理过
}

// 方式3：订单号幂等
// 前端生成唯一订单号，后端检查是否已存在
```

---

### Q4：为什么会出现有序性的问题？（多线程）

**考点：** 并发导致的乱序

**问题场景：**

```
用户A发送消息：
消息1：你好     → 线程1处理
消息2：在吗     → 线程2处理
消息3：有空吗   → 线程3处理

因为线程2先处理完，导致：
消息2 → 消息1 → 消息3（顺序乱了）
```

**解决方案：**

```java
// 方案1：单线程消费（最简单）
ExecutorService executor = Executors.newSingleThreadExecutor();

// 方案2：按userId分区
int partition = userId.hashCode() % threadCount;
executors[partition].submit(task);  // 同一用户的消息发到同一线程

// 方案3：业务层排序
// 给每条消息加序号，消费时按序号排序后处理
```

---

### Q5：对Redis中的有序集合ZSet了解吗？ZSet底层是用什么实现的？

**考点：** ZSet数据结构

**使用场景：**
```redis
# 排行榜
ZADD sales:rank 100 "商品A"   # 销量100
ZADD sales:rank 200 "商品B"   # 销量200
ZADD sales:rank 150 "商品C"   # 销量150

# 查询Top3
ZREVRANGE sales:rank 0 2 WITHSCORES
# 结果：商品B(200), 商品C(150), 商品A(100)
```

**底层实现：**

```
ZSet = 跳表(skiplist) + 字典(dict)

1. 跳表：
   - 按score排序
   - 支持范围查询 O(logN)
   - 类似多层链表

2. 字典：
   - member → score 映射
   - 支持快速查找 O(1)
```

**为什么不用红黑树？**

| 对比项 | 跳表 | 红黑树 |
|--------|------|--------|
| 范围查询 | 快（顺序遍历） | 慢（需要中序遍历） |
| 实现复杂度 | 简单 | 复杂 |
| 插入/删除 | O(logN) | O(logN) |

---

### Q6：Redis中的持久化机制是怎样的？

**考点：** RDB与AOF

| 机制 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| **RDB** | 定时快照 | 性能高、恢复快 | 可能丢失最后一次快照后的数据 |
| **AOF** | 记录每个写命令 | 数据更完整 | 文件大、恢复慢 |
| **混合持久化** | RDB + AOF增量 | 兼顾性能和安全 | Redis 4.0+ |

**RDB配置：**
```redis
# redis.conf
save 900 1      # 900秒内至少1个key变化
save 300 10     # 300秒内至少10个key变化
save 60 10000   # 60秒内至少10000个key变化
```

**AOF配置：**
```redis
# 同步策略
appendfsync always     # 每次写都同步（最安全，最慢）
appendfsync everysec   # 每秒同步（推荐）
appendfsync no         # 让操作系统决定（最快，可能丢数据）
```

---

## 网络协议

### Q1：我们关注HTTP返回哪些内容？HTTP还有哪些字段？

**考点：** HTTP协议理解

**HTTP响应结构：**

```http
HTTP/1.1 200 OK                          ← 状态行
Content-Type: application/json           ← 响应头
Content-Length: 123
Set-Cookie: sessionId=abc123
Cache-Control: max-age=3600
                                         ← 空行
{"code": 200, "data": {...}}             ← 响应体
```

**常用响应头：**

| 字段 | 作用 | 示例 |
|------|------|------|
| **Content-Type** | 内容类型 | application/json |
| **Content-Length** | 内容长度 | 123 |
| **Set-Cookie** | 设置Cookie | sessionId=abc |
| **Cache-Control** | 缓存控制 | max-age=3600 |
| **Location** | 重定向地址 | /login |
| **Access-Control-Allow-Origin** | 跨域 | * |

**常用状态码：**

```
2xx 成功：
200 OK
201 Created
204 No Content

3xx 重定向：
301 Moved Permanently
302 Found
304 Not Modified

4xx 客户端错误：
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found

5xx 服务器错误：
500 Internal Server Error
502 Bad Gateway
503 Service Unavailable
```

---

### Q2：请求方法GET为什么说是幂等的？

**考点：** HTTP幂等性

**幂等性定义：**
```
多次执行同一操作，结果相同

幂等：GET、PUT、DELETE
非幂等：POST
```

**示例：**

```http
# GET（幂等）
GET /user/1001    # 查询用户
# 调用1次或100次，结果都是返回用户信息，不会改变数据

# POST（非幂等）
POST /order       # 创建订单
# 每次调用都会创建新订单，调用10次就创建10个订单

# PUT（幂等）
PUT /user/1001 {"age": 25}  # 更新用户
# 调用多次，最终结果都是age=25

# DELETE（幂等）
DELETE /user/1001
# 第1次删除成功，后续调用返回404，但结果都是"用户不存在"
```

**为什么GET是幂等的？**
```
设计原则：GET只用于查询，不修改数据
实际开发：严禁在GET请求中修改数据
```

---

### Q3：下单接口的幂等性应该如何实现呢？前端传过来的信息如何保证是同一个订单呢？

**考点：** 接口幂等性设计

**方案对比：**

| 方案 | 实现 | 优点 | 缺点 |
|------|------|------|------|
| **唯一订单号** | 前端生成UUID | 简单 | 依赖前端 |
| **Token机制** | 后端生成，前端提交 | 安全 | 复杂 |
| **数据库唯一索引** | 唯一约束 | 可靠 | 数据库压力 |

**推荐方案：Token机制**

```java
// 1. 用户进入下单页，获取token
@GetMapping("/order/token")
public Result getOrderToken() {
    String token = UUID.randomUUID().toString();
    // 存入Redis，5分钟过期
    redisTemplate.opsForValue().set("order:token:" + token, "1", 5, TimeUnit.MINUTES);
    return Result.ok(token);
}

// 2. 提交订单时，带上token
@PostMapping("/order")
public Result createOrder(@RequestBody OrderDTO dto, @RequestHeader("order-token") String token) {
    // 检查并删除token（原子操作）
    String script = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    Long result = redisTemplate.execute(
        new DefaultRedisScript<>(script, Long.class),
        Collections.singletonList("order:token:" + token),
        "1"
    );
    
    if (result == 0) {
        return Result.fail("请勿重复提交");
    }
    
    // 创建订单
    orderService.create(dto);
    return Result.ok();
}
```

**流程图：**
```
用户进入下单页
    ↓
GET /order/token → 获取token（存Redis）
    ↓
用户填写信息
    ↓
POST /order + token
    ↓
后端检查token → 存在？删除：创建订单 / 不存在：拒绝
```

---

### Q4：HTTP的TCP的可靠性从哪些方面来保证呢？

**考点：** TCP可靠性机制

**TCP六大机制：**

#### 1. 序号与确认应答

```
发送端：发送数据包，标记序号
接收端：收到后发送ACK确认
发送端：收到ACK才认为发送成功
```

#### 2. 超时重传

```
发送数据 → 等待ACK
超时未收到 → 重传
```

#### 3. 流量控制（滑动窗口）

```
接收端告诉发送端：我还能接收多少数据
发送端根据接收窗口大小发送数据
```

#### 4. 拥塞控制

```
慢启动 → 拥塞避免 → 快速重传 → 快速恢复
根据网络状况动态调整发送速率
```

#### 5. 校验和

```
发送端：计算数据校验和
接收端：验证校验和，错误则丢弃
```

#### 6. 顺序保证

```
接收端按序号重新排序数据包
保证数据按发送顺序到达应用层
```

---

### Q5：TCP断开连接的时候为什么不是三次？

**考点：** 四次挥手原理

**为什么是四次挥手？**

```
Client                      Server
  |                            |
  |-------- FIN(我没数据了) ---->|  第1次
  |                            |
  |<------- ACK(知道了) --------|  第2次
  |                            |
  |                            |  Server继续发送数据...
  |                            |
  |<------- FIN(我也没数据了) --|  第3次
  |                            |
  |-------- ACK(知道了) -------->|  第4次
```

**为什么不能合并成三次？**

```
原因：服务端可能还有数据要发送

第2次（ACK）：我知道你要关了，但我还有数据要发
第3次（FIN）：我的数据也发完了，可以关了

如果没有数据了，ACK和FIN可以合并（实际就变成三次）
但为了通用性，设计成四次
```

**对比三次握手：**

```
握手：可以在第3次带数据，所以是三次
挥手：不能保证立刻关闭，所以是四次
```

---

### Q6：TIME_WAIT为什么要设置2MSL？

**考点：** TIME_WAIT状态

**MSL：**
```
MSL = Maximum Segment Lifetime（最大报文生存时间）
通常 = 30秒 或 2分钟
2MSL = 60秒 或 4分钟
```

**为什么要等2MSL？**

#### 原因1：确保最后的ACK到达

```
Client                      Server
  |                            |
  |<------- FIN ----------------|
  |                            |
  |-------- ACK ---------------->|
  |                            |  如果这个ACK丢了？
  |        TIME_WAIT(2MSL)     |
  |                            |  Server会重传FIN
  |<------- FIN（重传）---------|
  |                            |  Client还在TIME_WAIT，能收到
  |-------- ACK ---------------->|
```

**2MSL = 1个MSL（FIN到达）+ 1个MSL（ACK返回）**

#### 原因2：避免旧连接数据干扰新连接

```
旧连接：Client:8080 → Server:80
新连接：Client:8080 → Server:80（端口复用）

如果立刻关闭，旧连接的延迟数据包可能被新连接收到
等待2MSL，确保旧数据包都过期了
```

---

### Q7：TIME_WAIT过多？

**考点：** 高并发场景问题

**问题：**
```bash
netstat -an | grep TIME_WAIT | wc -l
# 结果：10000+（太多了！）

问题：
1. 占用端口（客户端端口有限：1024-65535）
2. 占用内存
3. 无法建立新连接
```

**解决方案：**

#### 1. 减少TIME_WAIT数量

```bash
# /etc/sysctl.conf

# 1. 开启TIME_WAIT快速回收
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_tw_recycle = 1  # 不推荐，可能导致问题

# 2. 减少TIME_WAIT时间
net.ipv4.tcp_fin_timeout = 30  # 默认60秒

# 3. 增加可用端口范围
net.ipv4.ip_local_port_range = 10000 65000

# 生效
sysctl -p
```

#### 2. 应用层优化

```java
// 1. 使用HTTP长连接（Keep-Alive）
// 减少连接建立和关闭次数

// 2. 连接池复用
HikariConfig config = new HikariConfig();
config.setMaximumPoolSize(20);  // 复用连接

// 3. 让服务端主动关闭
// TIME_WAIT在主动关闭方，让客户端被动关闭
```

---

### Q8：TCP粘包指的是什么？怎么解决？

**考点：** TCP粘包拆包

**什么是粘包？**

```
发送端：
send("Hello")
send("World")

接收端可能收到：
情况1：HelloWorld      ← 粘包（两个包粘在一起）
情况2：Hel  loWorld    ← 拆包（一个包被拆开）
情况3：Hello Wo  rld   ← 粘包+拆包
```

**为什么会粘包？**
```
TCP是字节流协议，没有消息边界
应用层发送的多个数据包，在TCP看来都是字节流
```

**解决方案：**

#### 1. 固定长度

```java
// 每个消息固定100字节
byte[] msg = new byte[100];
// 不足100字节，补0
```

#### 2. 分隔符

```java
// 用换行符分隔
send("Hello\n")
send("World\n")

// 接收端按\n切割
```

#### 3. 消息头+长度

```java
// 自定义协议
[长度(4字节)][消息体]

// 发送
int length = data.length;
byte[] header = intToBytes(length);  // 4字节长度
send(header + data);

// 接收
byte[] header = read(4);  // 先读4字节
int length = bytesToInt(header);
byte[] body = read(length);  // 再读指定长度
```

**Netty解决方案：**

```java
// 固定长度
pipeline.addLast(new FixedLengthFrameDecoder(100));

// 分隔符
pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()));

// 长度字段
pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
```

---

## 操作系统

### Q1：进程之间的通信方式？

**考点：** IPC（Inter-Process Communication）

| 方式 | 原理 | 优点 | 缺点 | 使用场景 |
|------|------|------|------|----------|
| **管道(Pipe)** | 半双工，父子进程 | 简单 | 只能父子进程 | Shell命令 |
| **命名管道(FIFO)** | 有名字的管道 | 任意进程 | 半双工 | 进程间通信 |
| **消息队列** | 消息链表 | 异步 | 有大小限制 | 异步通信 |
| **共享内存** | 直接访问同一块内存 | 最快 | 需要同步 | 大数据传输 |
| **信号量** | PV操作 | 同步 | 只能传递信号 | 进程同步 |
| **信号(Signal)** | 异步通知 | 简单 | 信息少 | kill命令 |
| **Socket** | 网络通信 | 跨主机 | 复杂 | 网络应用 |

**示例：**

```bash
# 管道
ls | grep ".java"

# 消息队列
ipcs -q  # 查看消息队列

# 共享内存
ipcs -m  # 查看共享内存

# 信号
kill -9 1234  # 发送SIGKILL信号
```

---

### Q2：Socket会有哪些过程？服务端IO多路复用？

**考点：** Socket编程、IO模型

**Socket通信过程：**

```
服务端                          客户端
  |                              |
socket()  创建套接字            socket()
  |                              |
bind()    绑定IP和端口           |
  |                              |
listen()  监听                   |
  |                              |
accept()  阻塞等待        →    connect()  连接
  |                              |
read()    读取数据        ←    write()    发送数据
  |                              |
write()   发送数据        →    read()     读取数据
  |                              |
close()   关闭连接               close()
```

**IO多路复用：**

```java
// select/poll/epoll 监听多个socket

// 传统：一个线程处理一个连接
while (true) {
    Socket socket = serverSocket.accept();  // 阻塞
    new Thread(() -> handle(socket)).start();
}

// IO多路复用：一个线程处理多个连接
Selector selector = Selector.open();
serverSocket.register(selector, SelectionKey.OP_ACCEPT);

while (true) {
    selector.select();  // 等待事件
    Set<SelectionKey> keys = selector.selectedKeys();
    for (SelectionKey key : keys) {
        if (key.isAcceptable()) {
            // 新连接
        } else if (key.isReadable()) {
            // 可读
        }
    }
}
```

**select/poll/epoll区别：**

| 特性 | select | poll | epoll |
|------|--------|------|-------|
| **最大连接数** | 1024（FD_SETSIZE） | 无限制 | 无限制 |
| **性能** | O(n) | O(n) | O(1) |
| **触发方式** | 水平触发 | 水平触发 | 边缘触发/水平触发 |

---

### Q3：怎么向进程发送信号？

**考点：** 信号机制

**常用方法：**

```bash
# 1. kill命令
kill -9 1234        # 强制杀死进程
kill -15 1234       # 优雅停止（SIGTERM）
kill -STOP 1234     # 暂停进程
kill -CONT 1234     # 继续进程

# 2. killall（按名字）
killall java

# 3. pkill（模式匹配）
pkill -f "tomcat"

# 4. 快捷键
Ctrl+C  # 发送SIGINT（中断）
Ctrl+Z  # 发送SIGTSTP（暂停）
```

**代码发送信号：**

```java
// Java
Runtime.getRuntime().exec("kill -15 " + pid);

// C
#include <signal.h>
kill(pid, SIGTERM);
```

**常用信号：**

| 信号 | 值 | 含义 | 能否捕获 |
|------|-----|------|---------|
| SIGINT | 2 | 中断（Ctrl+C） | 可以 |
| SIGKILL | 9 | 强制杀死 | 不可以 |
| SIGTERM | 15 | 终止（默认） | 可以 |
| SIGSTOP | 19 | 暂停 | 不可以 |
| SIGCONT | 18 | 继续 | 可以 |

---

### Q4：线程继承进程哪些资源？哪些资源是进程独享的？

**考点：** 进程与线程区别

**线程共享进程的资源：**

```
✅ 代码段（.text）
✅ 数据段（.data、.bss）
✅ 堆内存（heap）
✅ 文件描述符
✅ 信号处理器
✅ 当前工作目录
✅ 用户ID、组ID
```

**线程独享的资源：**

```
❌ 线程ID
❌ 栈（stack）
❌ 寄存器
❌ 程序计数器（PC）
❌ 信号屏蔽字
❌ errno变量
```

**图解：**

```
进程
├── 代码段 ────────────┐
├── 数据段 ────────────┤
├── 堆（共享）─────────┤  所有线程共享
├── 文件描述符 ────────┤
│                      │
├── 线程1              │
│   ├── 栈1 ───────────┤  线程1独享
│   ├── 寄存器1        │
│   └── PC1            │
│                      │
├── 线程2              │
│   ├── 栈2 ───────────┤  线程2独享
│   ├── 寄存器2        │
│   └── PC2            │
```

---

### Q5：线程比进程轻量，为什么轻量？

**考点：** 线程优势

**轻量体现：**

#### 1. 创建/销毁快

```
进程：
- 复制整个进程地址空间
- 分配独立的页表
- 复制文件描述符
- 时间：毫秒级

线程：
- 只分配栈空间
- 共享进程资源
- 时间：微秒级
```

#### 2. 切换快

```
进程切换：
1. 保存当前进程状态
2. 切换页表（刷新TLB）
3. 加载新进程状态
开销：大（需要切换地址空间）

线程切换：
1. 保存当前线程状态（寄存器、PC）
2. 加载新线程状态
3. 不需要切换页表
开销：小（在同一地址空间内）
```

#### 3. 通信快

```
进程：
- 需要IPC（管道、消息队列、共享内存）
- 需要系统调用
- 慢

线程：
- 直接访问共享内存
- 无需系统调用
- 快
```

#### 4. 内存占用小

```
进程：
- 独立地址空间（MB级别）

线程：
- 只有栈（KB级别）
- Linux默认栈大小：8MB
```

---

### Q6：系统调用是什么意思？系统调用的过程？

**考点：** 用户态与内核态

**什么是系统调用？**

```
用户程序想要访问硬件、文件等资源
必须通过操作系统提供的接口（系统调用）
从用户态切换到内核态执行
```

**系统调用的过程：**

```
1. 用户程序调用系统调用（如read()）
   ↓
2. 触发软中断（int 0x80 或 syscall指令）
   ↓
3. CPU切换到内核态
   ↓
4. 保存用户态上下文（寄存器、PC等）
   ↓
5. 根据系统调用号，跳转到对应的内核函数
   ↓
6. 执行内核函数（访问硬件、文件等）
   ↓
7. 返回结果到用户空间
   ↓
8. 恢复用户态上下文
   ↓
9. CPU切换回用户态
   ↓
10. 用户程序继续执行
```

**示例：**

```c
// 用户程序
int fd = open("/tmp/test.txt", O_RDONLY);  // 系统调用

// 内核处理
sys_open() {
    // 1. 权限检查
    // 2. 分配文件描述符
    // 3. 打开文件
    // 4. 返回fd
}
```

---

### Q7：用户态切换到内核态除了系统调用还有哪些方式？

**考点：** 用户态/内核态切换

**三种方式：**

#### 1. 系统调用（主动）

```c
read(fd, buf, size);  // 用户主动调用
```

#### 2. 异常（被动）

```
除零错误、缺页中断、非法指令等
CPU自动切换到内核态处理
```

#### 3. 中断（外部）

```
硬件中断：
- 时钟中断（定时器）
- IO中断（键盘、网卡）
- 硬盘中断

软件中断：
- int指令
```

**对比：**

| 方式 | 触发者 | 示例 |
|------|--------|------|
| 系统调用 | 用户程序主动 | read、write |
| 异常 | CPU检测到错误 | 除零、缺页 |
| 中断 | 外部硬件 | 时钟、IO |

---

### Q8：中断是由什么引起的？

**考点：** 中断机制

**中断分类：**

#### 1. 硬件中断（外中断）

```
时钟中断：
- 定时器到期
- 触发进程调度

IO中断：
- 键盘输入
- 鼠标点击
- 网卡收到数据
- 硬盘读写完成

电源中断：
- 电源异常
```

#### 2. 软件中断（内中断）

```
异常：
- 除零错误
- 非法指令
- 缺页中断
- 访问越界

系统调用：
- int 0x80
- syscall指令
```

**中断处理流程：**

```
1. 硬件检测到中断信号
   ↓
2. 保存当前程序状态
   ↓
3. 根据中断向量表，跳转到中断处理程序
   ↓
4. 执行中断处理
   ↓
5. 恢复之前的程序状态
   ↓
6. 继续执行被中断的程序
```

---

## 算法与手撕代码

### Q1：判断树是否是完全二叉树

**考点：** 树的遍历、BFS

**思路：**

```
完全二叉树：
1. 除了最后一层，其他层都是满的
2. 最后一层从左到右连续

检查方法：
- 层序遍历（BFS）
- 遇到第一个null后，后面不能再有非null节点
```

**代码：**

```java
public boolean isCompleteTree(TreeNode root) {
    if (root == null) return true;
    
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    
    boolean hasNull = false;  // 是否遇到过null
    
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        
        if (node == null) {
            hasNull = true;  // 遇到null了
        } else {
            if (hasNull) {
                // 之前遇到过null，现在又有非null节点
                return false;  // 不是完全二叉树
            }
            queue.offer(node.left);
            queue.offer(node.right);
        }
    }
    
    return true;
}
```

**测试用例：**

```
      1
     / \
    2   3
   / \
  4   5

结果：true（完全二叉树）

      1
     / \
    2   3
   /     \
  4       5

结果：false（第3层不连续）
```

---

### Q2：TopK问题

**考点：** 堆、快速选择

**方法1：堆（适合海量数据）**

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

**方法2：快速选择（快速排序思想）**

```java
public int findKthLargest(int[] nums, int k) {
    return quickSelect(nums, 0, nums.length - 1, k);
}

private int quickSelect(int[] nums, int left, int right, int k) {
    if (left == right) return nums[left];
    
    // 随机选择pivot
    int pivotIndex = left + new Random().nextInt(right - left + 1);
    int pivot = nums[pivotIndex];
    
    // 分区
    swap(nums, pivotIndex, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
        if (nums[i] > pivot) {  // 大的放左边
            swap(nums, i, storeIndex);
            storeIndex++;
        }
    }
    swap(nums, storeIndex, right);
    
    // 判断pivot位置
    if (storeIndex == k - 1) {
        return nums[storeIndex];
    } else if (storeIndex < k - 1) {
        return quickSelect(nums, storeIndex + 1, right, k);
    } else {
        return quickSelect(nums, left, storeIndex - 1, k);
    }
}

// 时间复杂度：O(n) 平均，O(n²) 最坏
// 空间复杂度：O(1)
```

---

### Q3：有序数组转平衡二叉搜索树

**考点：** 递归、BST

**思路：**

```
有序数组：[-10, -3, 0, 5, 9]

选择中间元素作为根节点：0
左半部分构建左子树：[-10, -3]
右半部分构建右子树：[5, 9]

递归构建
```

**代码：**

```java
public TreeNode sortedArrayToBST(int[] nums) {
    if (nums == null || nums.length == 0) return null;
    return buildTree(nums, 0, nums.length - 1);
}

private TreeNode buildTree(int[] nums, int left, int right) {
    if (left > right) return null;
    
    // 选择中间元素作为根节点
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

---

## 项目深挖

### Q1：你这个系统为什么对消息的可达性要求这么高？

**答题思路：**

```
我的项目是一个即时通讯系统，主要用于：

1. 业务场景：
   - 客服聊天：用户咨询必须及时收到回复
   - 订单通知：支付成功、发货等重要通知
   - 系统消息：账户安全提醒

2. 为什么要求高可达性：
   - 客服响应时间直接影响用户体验
   - 订单通知丢失会导致用户投诉
   - 系统消息关乎账户安全

3. 实现方式：
   - Redis Stream持久化消息
   - ACK机制确认消费
   - pending list崩溃恢复
```

---

### Q2：WebSocket在哪些地方用到？

**答题模板：**

```
项目中使用WebSocket实现：

1. 实时聊天
   - 用户发消息 → WebSocket推送给对方
   - 避免轮询，实时性更好

2. 在线状态
   - 用户上线 → WebSocket连接建立
   - 用户离线 → WebSocket连接断开

3. 消息推送
   - 新订单通知
   - 系统公告

为什么用WebSocket而不是HTTP轮询？
- 减少服务器压力（不用频繁请求）
- 实时性更好（服务端主动推送）
- 节省带宽（不用每次都发HTTP头）
```

---

### Q3：简单介绍一下主动更新，延迟双删流程（出现了什么问题用这个）？

**考点：** 缓存一致性

**延迟双删：**

```java
// 问题：先删缓存，再更新数据库，可能导致脏数据

public void updateUser(User user) {
    // 1. 删除缓存
    redisTemplate.delete("user:" + user.getId());
    
    // 2. 更新数据库
    userMapper.update(user);
    
    // 3. 延迟后再次删除缓存（延迟双删）
    new Thread(() -> {
        try {
            Thread.sleep(500);  // 延迟500ms
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
T4: 线程B把旧数据写回缓存
    ↑ 缓存是旧数据，数据库是新数据，不一致！

解决：
T1: 线程A删除缓存
T2: 线程B查询，缓存miss，查数据库（旧数据）
T3: 线程A更新数据库（新数据）
T4: 线程B把旧数据写回缓存
T5: 线程A延迟后再次删除缓存 ✅
    ↑ 缓存被删除，下次查询会拿到新数据
```

---

### Q4：布隆过滤器原理？

**考点：** 缓存穿透解决方案

**原理：**

```
1. 初始化：
   - 一个大的bit数组（全是0）
   - k个哈希函数

2. 添加元素：
   - 对元素进行k次哈希
   - 把对应位置的bit设为1

3. 查询元素：
   - 对元素进行k次哈希
   - 检查对应位置的bit
   - 全是1 → 可能存在
   - 有0 → 一定不存在
```

**图解：**

```
bit数组：[0,0,0,0,0,0,0,0,0,0]

添加"hello"：
hash1("hello") = 2 → bit[2] = 1
hash2("hello") = 5 → bit[5] = 1
hash3("hello") = 8 → bit[8] = 1

结果：[0,0,1,0,0,1,0,0,1,0]

查询"hello"：
bit[2] = 1, bit[5] = 1, bit[8] = 1 → 可能存在

查询"world"：
hash1("world") = 2 → bit[2] = 1 ✓
hash2("world") = 4 → bit[4] = 0 ✗ → 一定不存在
```

**特点：**
- ✅ 节省空间（只用bit）
- ✅ 查询速度快（O(k)）
- ❌ 有误判率（可能存在实际不存在）
- ❌ 不能删除（删除会影响其他元素）

**应用场景：**

```java
// 防止缓存穿透
if (!bloomFilter.mightContain(userId)) {
    return null;  // 一定不存在，直接返回
}

// 可能存在，查Redis
String user = redisTemplate.get("user:" + userId);
if (user == null) {
    // 查数据库
}
```

---

### Q5：DFA和NFA的区别？为什么要用DFA而不是树或者是字符串匹配？

**考点：** 敏感词过滤算法

**DFA（确定有限状态自动机）：**

```
敏感词：["SB", "傻逼", "垃圾"]

构建DFA：
     root
    /  |  \
   S   傻  垃
   |   |   |
   B   逼  圾
  (end)(end)(end)

匹配"你是SB"：
你 → root（继续）
是 → root（继续）
S → 跳到S节点
B → 跳到B节点 → (end) → 命中！
```

**为什么用DFA？**

| 方案 | 时间复杂度 | 缺点 |
|------|-----------|------|
| **暴力匹配** | O(n * m * k) | 太慢（n=文本长度，m=敏感词数，k=敏感词长度） |
| **KMP** | O(n * m) | 需要对每个敏感词单独匹配 |
| **Trie树** | O(n * k) | 可以，但DFA更快 |
| **DFA** | O(n) | 最快 |

**代码示例：**

```java
public class DFA {
    private Map<Character, Object> root = new HashMap<>();
    
    // 构建DFA
    public void addWord(String word) {
        Map<Character, Object> node = root;
        for (char c : word.toCharArray()) {
            node = (Map<Character, Object>) node.computeIfAbsent(c, k -> new HashMap<>());
        }
        node.put('end', true);  // 标记结束
    }
    
    // 检查文本
    public boolean contains(String text) {
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> node = root;
            int j = i;
            while (j < text.length() && node.containsKey(text.charAt(j))) {
                node = (Map<Character, Object>) node.get(text.charAt(j));
                if (node.containsKey("end")) {
                    return true;  // 命中敏感词
                }
                j++;
            }
        }
        return false;
    }
}
```

---

### Q6：能简单介绍一下零拷贝吗？Netty中的byteBuf零拷贝和操作系统的零拷贝有什么区别？

**考点：** 零拷贝优化

#### 操作系统零拷贝

**传统IO（4次拷贝）：**

```
1. 硬盘 → 内核缓冲区（DMA拷贝）
2. 内核缓冲区 → 用户缓冲区（CPU拷贝）
3. 用户缓冲区 → Socket缓冲区（CPU拷贝）
4. Socket缓冲区 → 网卡（DMA拷贝）
```

**零拷贝（2次拷贝）：**

```
1. 硬盘 → 内核缓冲区（DMA拷贝）
2. 内核缓冲区 → 网卡（DMA拷贝）

没有用户态参与！
```

**实现方式：**

```java
// sendfile
FileChannel fileChannel = new FileInputStream(file).getChannel();
fileChannel.transferTo(0, fileChannel.size(), socketChannel);

// mmap
MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
```

---

#### Netty零拷贝

**不是操作系统层面的零拷贝，而是应用层优化：**

```java
// 1. CompositeByteBuf（组合多个ByteBuf，不需要内存拷贝）
CompositeByteBuf composite = Unpooled.compositeBuffer();
composite.addComponents(buf1, buf2);  // 逻辑组合，不拷贝

// 2. Slice（切片，共享底层数组）
ByteBuf slice = buf.slice(0, 10);  // 共享buf的前10字节

// 3. Wrap（包装数组）
byte[] array = new byte[10];
ByteBuf buf = Unpooled.wrappedBuffer(array);  // 不拷贝，直接包装
```

**对比：**

| 零拷贝 | 层级 | 优化点 |
|--------|------|--------|
| **操作系统** | 内核态 | 减少CPU拷贝次数 |
| **Netty** | 应用层 | 减少ByteBuf拷贝次数 |

---

## 答题策略

### 1. 自我介绍模板

```
面试官您好，我叫XXX，目前是XXX大学XXX专业大三学生。

我主要的技术栈是Java后端开发，熟悉：
- Spring Boot、MyBatis-Plus等框架
- MySQL数据库和Redis缓存
- 多线程和JVM调优

项目方面，我做过一个【项目名称】，主要负责：
- 核心功能：XXX（用技术亮点）
- 性能优化：XXX（用数据说话）
- 解决难点：XXX（体现思考）

我对后端开发很感兴趣，希望能加入作业帮，学习更多实战经验。
```

---

### 2. 项目介绍STAR法则

**S**ituation（背景）
**T**ask（任务）
**A**ction（行动）
**R**esult（结果）

```
S: 项目是一个外卖系统，面临秒杀场景的高并发问题
T: 需要保证在1000+并发下，库存不超卖且系统稳定
A: 我采用了Redisson分布式锁 + Lua脚本 + Redis Stream异步下单
R: 最终TPS从120提升到2500，响应时间从890ms降到15ms
```

---

### 3. 不会的问题怎么答

```
❌ 不好的回答：
"这个我不会"
"我没学过"

✅ 好的回答：
"这个知识点我了解得不够深入，但我知道XXX（说相关的）"
"我目前掌握的是XXX，您提到的这个我会回去学习"
"我的理解是XXX，不知道对不对？"
```

---

### 4. 常见套路问题

#### "你有什么想问我的吗？"

```
✅ 推荐：
1. 如果我有幸加入，未来主要负责哪方面的工作？
2. 团队目前在技术上有什么挑战？
3. 公司对实习生有什么期望和要求？

❌ 避免：
1. 薪资待遇（HR会谈）
2. 加班情况（敏感）
3. 没有问题（显得不用心）
```

---

## 🎯 复习重点

**高频考点（必须掌握）：**

1. ⭐⭐⭐ **HashMap/ConcurrentHashMap底层**
2. ⭐⭐⭐ **MySQL索引（B+树、回表、失效场景）**
3. ⭐⭐⭐ **Redis数据结构、持久化、缓存问题**
4. ⭐⭐⭐ **TCP三次握手/四次挥手、TIME_WAIT**
5. ⭐⭐⭐ **进程/线程区别、通信方式**
6. ⭐⭐ **JVM GC**
7. ⭐⭐ **系统调用、中断**
8. ⭐⭐ **算法题（TopK、BST、完全二叉树）**

**项目准备：**
- 画出系统架构图
- 准备2-3个技术难点
- 用数据说话（性能提升XX%）
- STAR法则讲故事

---

## 📚 推荐复习资料

```
1. 《Java并发编程的艺术》- ConcurrentHashMap
2. 《高性能MySQL》- 索引优化
3. 《Redis设计与实现》- 数据结构
4. 《深入理解计算机系统》- 系统调用
5. LeetCode Hot 100 - 算法题
```

---

祝你面试顺利！💪 有问题随时问我！
```

---

这份面经整理得非常全面，涵盖了作业帮后端面试的所有核心考点。建议你：

1. **先过一遍所有问题**，标记出不会的
2. **重点攻克高频考点**（HashMap、MySQL、Redis、TCP）
3. **准备项目介绍**，用STAR法则
4. **刷算法题**，至少TopK、树相关要熟练
5. **模拟面试**，找同学互相练习
