# apiscan

## 说明

本项目是用于生成Spring项目的Http接口文档的Maven插件。

## 快速使用

### 1. 编译并安装到本地Maven仓库

```shell
mvn clean install
```

### 2. 在项目的`pom.xml`引入插件

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.apiscan</groupId>
            <artifactId>apiscan-spring-maven-plugin</artifactId>
            <version>0.0.1</version>
            <executions>
                <execution>
                    <goals>
                        <goal>scan</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <!--文档输出目录（相对于pom.xml所在的目录）-->
                <output>API文档</output>
                <!--是否打开debug模式-->
                <debug>true</debug>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 3. 编译项目即可生成API文档

```shell
mvn clean install
```