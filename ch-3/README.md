# ch3项目依赖及其JAR包详细介绍

## 项目概述

ch3项目是一个简单的Maven Hello World示例项目，展示了Maven的基本使用方式。

## 项目基本信息

- **GroupId**: com.juvenxu.mvnbook
- **ArtifactId**: hello-world
- **Version**: 1.0-SNAPSHOT
- **Java版本**: 21

## 依赖项详细介绍

### 1. 测试依赖 (Test Scope)

#### 1.1 JUnit Jupiter API (版本: 5.10.1)

- **作用**: 现代Java单元测试框架的API组件
- **功能**: 提供测试注解和断言API
- **传递依赖**:
  - `opentest4j:1.3.0`: 提供测试异常和断言失败的标准API
  - `junit-platform-commons:1.10.1`: JUnit平台通用组件
  - `apiguardian-api:1.1.2`: API稳定性注解

## 构建插件

### 1. Maven Compiler Plugin (版本: 3.11.0)

- **作用**: 编译Java源代码
- **配置**: 设置Java 21作为编译目标

### 2. Maven Shade Plugin (版本: 3.5.1)

- **作用**: 创建可执行的***uber JAR包***
- **功能**: 将所有依赖打包到一个JAR文件中
- **配置**: 设置主类为 `com.juvenxu.mvnbook.helloworld.HelloWorld`

## 生成的JAR包

### 1. hello-world-1.0-SNAPSHOT.jar

- **类型**: 可执行的uber JAR
- **大小**: 包含所有依赖的完整包
- **主类**: com.juvenxu.mvnbook.helloworld.HelloWorld
- **内容**: 项目类文件 + Maven元数据

### 2. original-hello-world-1.0-SNAPSHOT.jar

- **类型**: 原始JAR包（不含依赖）
- **作用**: 由maven-shade-plugin生成的原始包备份

## 项目结构说明

### 源代码

- `HelloWorld.java`: 主类，包含sayHello方法和main方法
- `HelloWorldTest.java`: 单元测试类，使用JUnit 5进行测试

### 构建结果

项目成功构建并生成了可执行的JAR包，可以通过以下命令运行：

```bash
java -jar ch-3/hello-world/target/hello-world-1.0-SNAPSHOT.jar
```

## 依赖优化说明

### 优化后的依赖

pom.xml包含实际使用的依赖：

- `junit-jupiter-api`: 提供测试注解和断言API

### 优化效果

- **依赖分析**: `No dependency problems found`
- **构建成功**: 所有测试通过
- **依赖精简**: 只包含必要的依赖

## 依赖总结

该项目主要依赖现代测试框架，体现了Maven项目的基本结构：

- 使用JUnit 5 API进行单元测试
- 使用Maven Shade Plugin创建可执行包
- 所有测试依赖都设置为test scope，不会包含在最终的可执行JAR中
- 经过优化，只包含实际使用的依赖，避免了依赖冗余

---

## 如何确定依赖版本

### 依赖版本确定因素

依赖版本的选择通常基于以下几个因素：

#### 1. Java版本兼容性

- **Java 21** 是项目的目标版本
- 选择的依赖需要支持 Java 21
- 现代依赖库通常向后兼容，但需要检查文档

#### 2. 依赖库的兼容性矩阵

- **JUnit 5.10.1**: 支持 Java 8+，与 Java 21 完全兼容
- 这些版本都是当前稳定且与Java 21兼容的版本

#### 3. 项目需求

- 测试框架：选择现代、活跃维护的版本
- 构建工具：选择与Maven版本兼容的插件版本

#### 4. 版本选择策略

- **最新稳定版**: 通常选择最新的稳定版本
- **长期支持版**: 对于生产环境，可能选择LTS版本
- **兼容性**: 确保所有依赖版本相互兼容

### 版本检查方法

#### 1. 查看官方文档

```bash
# 查看Maven中央仓库中的最新版本
# 访问：https://mvnrepository.com/
```

#### 2. 使用Maven命令

```bash
# 查看可用版本
mvn versions:display-dependency-updates

# 检查插件更新
mvn versions:display-plugin-updates
```

#### 3. 检查兼容性

- 查看依赖库的官方文档
- 检查版本间的兼容性矩阵
- 测试不同版本的组合

### 具体检查兼容性矩阵的方法

#### 1. 官方文档检查

- **JUnit 5**: 访问 [JUnit 5官方文档](https://junit.org/junit5/docs/current/user-guide/)
- **Maven插件**: 查看 [Apache Maven插件文档](https://maven.apache.org/plugins/)

#### 2. 使用Maven中央仓库

```bash
# 访问Maven中央仓库网站查看依赖信息
# https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter
```

#### 3. 实际测试验证

```bash
# 使用不同版本进行测试
mvn clean test

# 如果测试失败，检查版本兼容性
# 查看错误日志，确定是否是版本冲突
```

#### 4. 依赖冲突检测

```bash
# 检查依赖冲突
mvn dependency:tree -Dverbose

# 查看依赖分析
mvn dependency:analyze
```

#### 5. 兼容性矩阵示例

**JUnit 5 兼容性矩阵**:

- JUnit 5.10.1 需要 Java 8+
- 与 Mockito 4.x+ 兼容

#### 6. 常见兼容性问题

- **Java版本不匹配**: 依赖需要支持项目使用的Java版本
- **传递依赖冲突**: 不同依赖引入相同库的不同版本
- **API变更**: 主要版本升级可能包含破坏性变更

### 本项目版本选择依据

| 依赖                  | 版本   | 选择原因                  |
| --------------------- | ------ | ------------------------- |
| JUnit Jupiter API     | 5.10.1 | 最新稳定版，支持Java 21   |
| Maven Compiler Plugin | 3.11.0 | 支持Java 21编译           |
| Maven Shade Plugin    | 3.5.1  | 稳定版本，功能完整        |

## 运行项目

```bash
# 构建项目
cd ch-3/hello-world
mvn clean package

# 运行项目
java -jar target/hello-world-1.0-SNAPSHOT.jar
```

输出结果应为：`Hello Maven`
