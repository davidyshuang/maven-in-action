#!/bin/bash

echo "=== VS Code Java 开发环境测试 ==="
echo

# 检查Java版本
echo "1. 检查Java版本:"
java -version
echo

# 检查Maven版本
echo "2. 检查Maven版本:"
mvn -version
echo

# 测试编译
echo "3. 测试项目编译:"
cd ch-3/hello-world
mvn clean compile
echo

# 测试运行
echo "4. 测试项目运行:"
mvn exec:java -Dexec.mainClass="com.juvenxu.mvnbook.helloworld.HelloWorld"
echo

echo "=== 环境测试完成 ==="
