# Maven in Action 项目

这是一个学习Maven的示例项目，包含《Maven实战》书中各章节的代码示例。

> 原来的代码这里有， <https://github.com/liqiangit/maven-in-action>
> 我使用AI 工具将其从 Eclispe 迁移到 VSCode，并将代码升级到java 21 并添加了必要的配置和说明。

## 项目结构

```text
maven-in-action/
├── ch-3/          # 第3章：Maven入门
├── ch-5/          # 第5章：坐标和依赖
├── ch-7/          # 第7章：生命周期和插件
├── ch-8/          # 第8章：聚合与继承
├── ch-10/         # 第10章：使用Maven进行测试
├── ch-12/         # 第12章：使用Maven构建Web应用
├── ch-17/         # 第17章：编写Maven插件
├── ch-18/         # 第18章：Archetype
├── clean-build-all.sh    # 清理和构建所有项目的脚本
├── test_environment.sh   # 测试环境的脚本
└── README.md             # 项目说明文档
```

## VSCode 开发 Java 设置指南

### 1. 安装必要的扩展

在VSCode中安装以下扩展来支持Java开发：

- **Extension Pack for Java** - 包含Java开发所需的核心扩展
- **Maven for Java** - Maven项目支持
- **Spring Boot Extension Pack** - 如果开发Spring Boot应用
- **Lombok Annotations Support for VS Code** - Lombok注解支持

### 2. 配置Java开发环境

#### 2.1 安装JDK

确保系统已安装JDK 8或更高版本。推荐使用：

- OpenJDK 11 或 17
- Oracle JDK

验证安装：

```bash
java -version
javac -version
```

#### 2.2 配置VSCode Java设置

在VSCode设置中配置以下选项：

```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-11",
      "path": "/path/to/jdk-11",
      "default": true
    }
  ],
  "java.compile.nullAnalysis.mode": "automatic",
  "java.debug.settings.onBuildFailureProceed": true,
  "maven.executable.path": "mvn",
  "java.settings.url": "https://raw.githubusercontent.com/redhat-developer/vscode-java/master/settings/java.settings.json"
}
```

### 3. Maven项目配置

#### 3.1 项目导入

- 打开项目根目录
- VSCode会自动检测Maven项目并提示导入
- 或者使用命令面板：`Ctrl+Shift+P` -> `Java: Import Java Projects`

#### 3.2 Maven设置

在项目根目录创建 `.vscode/settings.json`：

```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "maven.terminal.useJavaHome": true,
  "maven.terminal.customEnv": [
    {
      "environmentVariable": "JAVA_HOME",
      "value": "/path/to/your/jdk"
    }
  ],
  "java.compile.nullAnalysis.mode": "automatic"
}
```

### 4. 常用开发功能

#### 4.1 代码导航

- **Ctrl+Click** - 跳转到定义
- **F12** - 转到定义
- **Ctrl+Shift+O** - 转到符号
- **Ctrl+T** - 查看所有符号

#### 4.2 代码补全和重构

- 自动导入
- 代码补全
- 重命名重构
- 提取方法/变量

#### 4.3 调试配置

在 `.vscode/launch.json` 中配置调试：

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Current File",
      "request": "launch",
      "mainClass": "${file}"
    },
    {
      "type": "java",
      "name": "Debug Maven Test",
      "request": "launch",
      "mainClass": "org.junit.platform.console.ConsoleLauncher",
      "args": [
        "--class-path",
        "${workspaceFolder}/target/test-classes:${workspaceFolder}/target/classes",
        "--scan-class-path"
      ]
    }
  ]
}
```

### 5. 常用Maven命令

在VSCode中可以使用内置的Maven视图或终端执行命令：

```bash
# 清理项目
mvn clean

# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package

# 安装到本地仓库
mvn install

# 跳过测试
mvn -DskipTests package

# 查看依赖树
mvn dependency:tree
```

### 6. 问题排查

#### 6.1 常见问题

1. **项目无法识别为Java项目**
   - 确保安装了Java扩展包
   - 重新加载窗口：`Ctrl+Shift+P` -> `Developer: Reload Window`

2. **Maven命令找不到**
   - 确保Maven已安装并配置在PATH中
   - 在VSCode设置中指定Maven路径

3. **依赖解析失败**
   - 检查网络连接
   - 清理本地仓库：`mvn dependency:purge-local-repository`

#### 6.2 调试技巧

- 使用VSCode内置终端执行Maven命令
- 查看Problems面板获取编译错误
- 使用Java Language Server日志进行问题诊断

### 7. 推荐的工作流程

1. 打开项目根目录
2. 等待VSCode自动导入Maven项目
3. 使用Maven视图管理依赖和生命周期
4. 使用内置终端执行Maven命令
5. 利用调试功能测试代码
6. 使用Git集成进行版本控制

## 项目使用说明

### 构建所有项目

```bash
./clean-build-all.sh
```

### 测试环境

```bash
./test_environment.sh
```

### 各章节说明

- **ch-3**: Maven基础项目结构
- **ch-5**: 依赖管理示例
- **ch-7**: 插件和生命周期
- **ch-8**: 多模块项目
- **ch-10**: 测试配置
- **ch-12**: Web应用构建
- **ch-17**: 自定义插件开发
- **ch-18**: Archetype使用

## 系统要求

- Java 8+
- Maven 3.6+
- VSCode 1.60+

## 许可证

本项目用于学习目的，遵循相关书籍的版权规定。
