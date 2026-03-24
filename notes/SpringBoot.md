
---

# 🍃 Spring Boot & 循环依赖核心考点深度总结

## 1. Spring Boot 自动装配（开箱即用）原理
* **核心注解**：`@SpringBootApplication`，其内部真正起作用的是 **`@EnableAutoConfiguration`**。
* **扫描与加载（找图纸）**：项目启动时，底层会通过 `SpringFactoriesLoader` 去扫描所有引入的 Jar 包，找到 `META-INF/spring.factories`（或 Spring Boot 2.7+ 之后的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`）文件。这个文件里写满了各大框架提前写好的自动配置类（AutoConfiguration）。
* **智能按需装配（条件装配）**：Spring 不会把这些类全加载，而是利用 **`@Conditional` 系列注解** 进行条件判断：
    * `@ConditionalOnClass`：判断当前 classpath 下有没有这个类（比如有没有引入 Redis 的依赖包）。
    * `@ConditionalOnMissingBean`：判断用户自己有没有配过这个 Bean（优先用用户自定义的）。
    * 条件满足后，才会自动把对应的 Bean 注册到 IOC 容器中，从而实现免 XML 配置的“开箱即用”。

## 2. Spring 循环依赖与三级缓存机制
* **什么是循环依赖**：Bean A 的属性需要注入 Bean B，同时 Bean B 的属性又需要注入 Bean A。
* **解决前提**：Spring 只能解决**单例作用域（Singleton）**下的** Setter / 字段注入（@Autowired）**循环依赖，无法解决构造器注入的循环依赖。



* **三级缓存的具体分工**：
    1.  **一级缓存 (`singletonObjects`)**：成品库。存放已经完全实例化、属性赋值完成的最终完整 Bean。
    2.  **二级缓存 (`earlySingletonObjects`)**：半成品库。存放刚刚实例化完成，但**还没有进行属性注入**的 Bean 的引用（提前暴露出来应急用的）。
    3.  **三级缓存 (`singletonFactories`)**：工厂库。存放一个对象工厂（ObjectFactory），用于生成半成品 Bean 的引用，或者在必要时**提前生成 AOP 代理对象**。
* **核心解决流程**：
  A 开始实例化 -> 将暴露 A 的工厂存入三级缓存 -> A 发现需要注入 B，去创建 B -> B 实例化 -> B 发现需要 A，就**去三级缓存中调用工厂方法拿到了 A 的半成品引用** -> B 顺利完成初始化，放入一级缓存 -> A 拿到了完整的 B，A 也顺利完成初始化，放入一级缓存。死锁解开！
* **必考点：为什么一定要三级缓存？只用两级不行吗？**
  如果项目中没有任何 AOP（切面代理），两级缓存完全够用。但三级缓存的本质是为了**处理 AOP**。通过放入三级缓存的 ObjectFactory，Spring 可以保证：如果 A 被循环依赖了，且 A 需要被切面代理，那么注入给 B 的 A 的引用，**一定是通过工厂提前生成的代理对象**，而不是原始对象。

---

