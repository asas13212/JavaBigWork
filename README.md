# 大富翁 · Monopoly
Java 大作业，基于 Swing 的大富翁桌游，支持双人对战和人机对战。

---

## 一、普通用户：下载安装包（推荐✨，无需安装 Java）

前往 [Releases](https://github.com/asas13212/Monopoly/releases) 页面，下载 `大富翁Monopoly.exe` 安装包：

1. 双击运行安装程序
2. 按提示完成安装
3. 桌面快捷方式 / 开始菜单直接启动游戏

> 安装包已内置 Java 运行环境，**无需单独安装 JDK**。

---

## 二、开发者：源码运行

### 环境要求
- JDK 25（[Eclipse Temurin 推荐](https://adoptium.net/)）
- 无需额外依赖（纯 Swing）

### 方式 A：IDE 一键启动（最简单）
1. 用 IDEA 或 Eclipse 打开项目文件夹
2. 找到 `src/Main.java`
3. 右键 → Run
4. 游戏窗口自动弹出

### 方式 B：命令行
```bash
# 编译
javac -encoding UTF-8 -d out -sourcepath src src/Main.java

# 运行
java -cp out Main
```

---

## 三、自行打包发布

```bash
bash build-release.sh
```

生成的安装包在 `release/` 目录下。打包需要 JDK 25（含 jpackage），MSI 格式额外需要 [WiX Toolset](https://wixtoolset.org/)。