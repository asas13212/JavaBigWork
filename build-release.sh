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
echo -e "[1/5] JDK: $($JAVA --version 2>&1 | head -1)"

# ---- 2. 清理并编译 ----
echo -e "[2/5] 编译项目..."
rm -rf "$OUT_DIR" "$BUILD_DIR" "$RELEASE_DIR"
mkdir -p "$OUT_DIR" "$BUILD_DIR" "$RELEASE_DIR"

# 复制资源文件（图片等）
cp -r src/img "$OUT_DIR/" 2>/dev/null || true

# 编译所有 Java 文件
$JAVAC -encoding UTF-8 -d "$OUT_DIR" -sourcepath src $(find src -name "*.java")

echo -e "      编译完成"

# ---- 3. 打包 JAR ----
echo -e "[3/5] 打包 JAR..."

# 创建 MANIFEST
echo "Main-Class: $MAIN_CLASS" > "$BUILD_DIR/MANIFEST.MF"
echo "" >> "$BUILD_DIR/MANIFEST.MF"

# 打包
cd "$OUT_DIR"
"$JAR" cfm "../../$BUILD_DIR/$APP_NAME.jar" "../../$BUILD_DIR/MANIFEST.MF" .
cd - > /dev/null

echo -e "      JAR 已生成: $BUILD_DIR/$APP_NAME.jar"

# ---- 4. 生成 EXE 安装包 ----
echo -e "[4/5] 生成 EXE 安装包（含 JRE）..."
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

# ---- 5. 尝试生成 MSI 安装包（需要 WiX） ----
echo -e "[5/5] 尝试生成 MSI 安装包..."
if command -v candle &> /dev/null; then
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
    echo -e "      ${YELLOW}未检测到 WiX Toolset，跳过 MSI 生成${NC}"
    echo -e "      ${YELLOW}如需 MSI，请安装: https://wixtoolset.org/${NC}"
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
