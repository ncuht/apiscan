# apiscan

## 项目背景

程序员最讨厌的两件事：一是别人的代码没有文档，二是给自己的代码写文档。

~~有买卖就有伤害~~有需求就有市场，比如swagger就可以自动生成文档，但是swagger是侵入式的，需要程序员手动编写代码，且与业务代码混在一起。

本项目是一个maven插件，只要在`pom.xml`中集成本插件，就可以自动生成markdown格式的API文档。

与此功能类似的有一个[apiggs](https://github.com/apigcc/apigcc-maven-plugin)
，apiggs通过解析源文件生成接口文档，所以可以读取注释，但最新一次更新也已经是2019年了，对Spring的支持也不完善。

本项目通过ClassLoader获取类信息，所以获取不了字段注释，但是对Spring的支持比apiggs更完善。

## 功能说明

本项目运行时的类信息，生成Spring项目的Http接口文档的Maven插件。

注：下文中**插件项目**指本仓库代码，**业务项目**指使用本插件的项目。


## 快速使用

### 1. 编译本插件项目的代码，并安装到本地Maven仓库

```shell
mvn clean install
```

### 2. 在业务项目的`pom.xml`引入插件

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
            <!--configuration的配置可选。不写configuration配置的情况下，默认值：output=API，debug=false-->
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

### 3. 编译业务项目即可生成API文档

```shell
mvn clean compile
```

## 调试本插件项目代码

1. 在业务项目进行调试编译

```shell
mvnDebug clean compile
```

执行命令后，进入debug模式，等待调试程序运行，debug默认端口是8000。

2. 在插件项目中运行`Remote JVM Debug`

点击IDEA右上角`Run`按钮左边的`Run / Debug Configurations`，在下拉列表选择`Edit Configurations...`，点击弹出窗口左上角的`+`按钮，选择`Remote JVM 
Debug`，在`Configuration`的`Port`填入第一步显示的debug端口`8000`，最后运行该运行配置即可进入插件Debug模式。


## 欢迎加入

本项目在github与gitcode都有代码仓，欢迎加入，一起完善插件功能。

github: [https://github.com/ncuht/apiscan](https://github.com/ncuht/apiscan)

gitcode: [https://gitcode.com/java_t_t/apiscan/tree/master](https://gitcode.com/java_t_t/apiscan/tree/master)


## 联系我

由于不常登陆github和gitcode，若响应不及时，请直接发送邮件到我邮箱 [hetao_ncu@foxmail.com](mailto:hetao_ncu@foxmail.com)