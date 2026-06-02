#!/bin/bash
# ============================================================
# Monopoly 发布打包脚本（含联机功能 + 图片资源修复）
# 使用 jpackage 将游戏打包成原生 Windows 免安装版
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
APP_VERSION="1.1"
MAIN_CLASS="Main"
OUT_DIR="out/release"
BUILD_DIR="build"
RELEASE_DIR="release"
LIB_DIR="lib"

# ---- 颜色 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Monopoly 发布打包工具 v${APP_VERSION}${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# ---- 1. 检查 JDK ----
if [ ! -f "$JAVA" ]; then
    echo -e "${RED}错误: 找不到 JDK 25，请修改 JDK_HOME 变量${NC}"
    exit 1
fi
echo -e "[1/6] JDK: $("$JAVA" --version 2>&1 | head -1)"

# ---- 2. 清理并编译 ----
echo -e "[2/6] 编译项目..."

rm -rf "$OUT_DIR" "$BUILD_DIR" "$RELEASE_DIR"
mkdir -p "$OUT_DIR" "$BUILD_DIR" "$RELEASE_DIR"

# 解压依赖 jar 到输出目录（联机功能需要 WebSocket）
mkdir -p "$OUT_DIR/tmp"
for jar in "$LIB_DIR"/*.jar; do
    if [ -f "$jar" ]; then
        echo "      合并依赖: $(basename $jar)"
        (cd "$OUT_DIR/tmp" && "$JAR" xf "$(pwd)/$jar" 2>/dev/null)
    fi
done
# 清理依赖 jar 中的 META-INF 清单（避免覆盖主清单）
rm -rf "$OUT_DIR/tmp/META-INF"

# 编译所有 Java 文件（含 server 包，方便别人本地起服务端）
"$JAVAC" -encoding UTF-8 \
    -cp "$LIB_DIR/*" \
    -d "$OUT_DIR" \
    -sourcepath src \
    $(find src -name "*.java")

echo -e "      编译完成"

# ---- 3. 复制资源 + 合并依赖到输出目录 ----
echo -e "[3/6] 复制资源..."

# 复制图片（到 src/img 位置，保持和开发时一致的路径）
mkdir -p "$OUT_DIR/src"
cp -r src/img "$OUT_DIR/src/" 2>/dev/null || true

# 合并依赖 class 文件（从 tmp 移到主输出目录）
if [ -d "$OUT_DIR/tmp" ]; then
    cp -r "$OUT_DIR/tmp"/* "$OUT_DIR/" 2>/dev/null || true
    rm -rf "$OUT_DIR/tmp"
fi

echo -e "      资源 + 依赖已合并"

# ---- 4. 打包 JAR ----
echo -e "[4/6] 打包 JAR..."

# 创建 MANIFEST
echo "Main-Class: $MAIN_CLASS" > "$BUILD_DIR/MANIFEST.MF"
echo "Class-Path: ." >> "$BUILD_DIR/MANIFEST.MF"
echo "" >> "$BUILD_DIR/MANIFEST.MF"

"$JAR" cfm "$BUILD_DIR/$APP_NAME.jar" "$BUILD_DIR/MANIFEST.MF" -C "$OUT_DIR" .

echo -e "      JAR 已生成: $BUILD_DIR/$APP_NAME.jar ($(du -h $BUILD_DIR/$APP_NAME.jar | cut -f1))"

# ---- 5. 生成免安装绿色版 ----
echo -e "[5/6] 生成免安装绿色版..."

"$JPACKAGE" \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --input "$BUILD_DIR" \
  --main-jar "$APP_NAME.jar" \
  --main-class "$MAIN_CLASS" \
  --type app-image \
  --dest "$RELEASE_DIR"

# ★ 关键修复：把 src/ 复制到 release 根目录，确保图片路径 src/img/... 可访问 ★
cp -r src "$RELEASE_DIR/$APP_NAME/"

# 创建 logs 目录
mkdir -p "$RELEASE_DIR/$APP_NAME/logs"

echo -e "      绿色版已生成: $RELEASE_DIR/$APP_NAME/"
echo -e "      图片资源已复制: $RELEASE_DIR/$APP_NAME/src/img/"

# ---- 6. 尝试生成 EXE/MSI（需要 WiX） ----
echo -e "[6/6] 安装包..."
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
else
    echo -e "      ${YELLOW}未检测到 WiX Toolset，跳过 EXE/MSI${NC}"
    echo -e "      ${YELLOW}绿色版可直接使用: $RELEASE_DIR/$APP_NAME/${NC}"
fi

# ---- 完成 ----
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  打包完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "运行方式: 双击 ${YELLOW}$RELEASE_DIR/$APP_NAME/$APP_NAME.exe${NC}"
echo ""
echo -e "目录结构:"
ls -lh "$RELEASE_DIR/$APP_NAME/" | head -8
echo "  src/img/  ← 图片资源"
echo ""
echo -e "用户无需装 Java，双击 exe 即可运行！"
