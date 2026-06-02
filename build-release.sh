#!/bin/bash
# ============================================================
# Monopoly 发布打包脚本
# 使用 jpackage 将游戏打包成原生 Windows 安装包 (EXE/MSI)
# 用户无需安装 Java 即可运行
# ============================================================

set -e

# ---- 配置 ----
JDK_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot"
JAVA="$JDK_HOME/bin/java"
JAVAC="$JDK_HOME/bin/javac"
JAR="$JDK_HOME/bin/jar"
JPACKAGE="$JDK_HOME/bin/jpackage"

APP_NAME="大富翁Monopoly"
APP_VERSION="1.0"
MAIN_CLASS="Main"
OUT_DIR="out/production/Monopoly"
BUILD_DIR="build"
RELEASE_DIR="release"

# ---- 颜色 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Monopoly 发布打包工具${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# ---- 1. 检查 JDK ----
if [ ! -f "$JAVA" ]; then
    echo -e "${RED}错误: 找不到 JDK 25，请修改 JDK_HOME 变量${NC}"
    exit 1
fi
echo -e "[1/5] JDK: $("$JAVA" --version 2>&1 | head -1)"

# ---- 2. 清理并编译 ----
echo -e "[2/5] 编译项目..."
rm -rf "$OUT_DIR" "$BUILD_DIR" "$RELEASE_DIR"
mkdir -p "$OUT_DIR" "$BUILD_DIR" "$RELEASE_DIR"

# 复制资源文件（图片等）
cp -r src/img "$OUT_DIR/" 2>/dev/null || true

# 编译所有 Java 文件
"$JAVAC" -encoding UTF-8 -d "$OUT_DIR" -sourcepath src $(find src -name "*.java")

echo -e "      编译完成"

# ---- 3. 打包 JAR ----
echo -e "[3/5] 打包 JAR..."

# 创建 MANIFEST
echo "Main-Class: $MAIN_CLASS" > "$BUILD_DIR/MANIFEST.MF"
echo "" >> "$BUILD_DIR/MANIFEST.MF"

# 打包（用绝对路径，避免 Windows 路径问题）
"$JAR" cfm "$(pwd)/$BUILD_DIR/$APP_NAME.jar" "$(pwd)/$BUILD_DIR/MANIFEST.MF" -C "$(pwd)/$OUT_DIR" .

echo -e "      JAR 已生成: $BUILD_DIR/$APP_NAME.jar"

# ---- 4. 生成免安装绿色版（不需要 WiX） ----
echo -e "[4/5] 生成免安装绿色版..."
"$JPACKAGE" \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --input "$BUILD_DIR" \
  --main-jar "$APP_NAME.jar" \
  --main-class "$MAIN_CLASS" \
  --type app-image \
  --dest "$RELEASE_DIR"

echo -e "      绿色版已生成: $RELEASE_DIR/$APP_NAME/"

# ---- 5. 尝试生成 EXE/MSI 安装包（需要 WiX） ----
echo -e "[5/5] 尝试生成安装包..."
if command -v light &> /dev/null || command -v wix &> /dev/null; then
    "$JPACKAGE" \
      --name "$APP_NAME" \
      --app-version "$APP_VERSION" \
      --input "$BUILD_DIR" \
      --main-jar "$APP_NAME.jar" \
      --main-class "$MAIN_CLASS" \
      --type exe \
      --dest "$RELEASE_DIR" \
      --win-console
    echo -e "      EXE 安装包已生成"

    "$JPACKAGE" \
      --name "$APP_NAME" \
      --app-version "$APP_VERSION" \
      --input "$BUILD_DIR" \
      --main-jar "$APP_NAME.jar" \
      --main-class "$MAIN_CLASS" \
      --type msi \
      --dest "$RELEASE_DIR" \
      --win-dir-chooser \
      --win-menu \
      --win-shortcut
    echo -e "      MSI 安装包已生成"
else
    echo -e "      ${YELLOW}未检测到 WiX Toolset，跳过安装包生成${NC}"
    echo -e "      ${YELLOW}安装 WiX 后可生成 EXE/MSI: winget install WiXToolset.WiXToolset -e${NC}"
    echo -e "      ${YELLOW}绿色版可直接使用，位于 $RELEASE_DIR/$APP_NAME/${NC}"
fi

# ---- 完成 ----
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  打包完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "输出目录: ${YELLOW}$RELEASE_DIR/${NC}"
ls -lh "$RELEASE_DIR" 2>/dev/null
echo ""
echo -e "用户安装后即可直接运行，无需安装 Java！"
