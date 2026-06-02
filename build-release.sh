#!/bin/bash
# ============================================================
# Monopoly 发布打包脚本（含联机功能 + 图片资源修复）
# 使用 jpackage 将游戏打包成原生 Windows 免安装版
# 用户无需安装 Java 即可运行
# ============================================================

set -e

# ---- 根目录（必须在项目根目录运行） ----
ROOT="$(pwd)"

# ---- 配置 ----
# 优先使用 PATH 上的 javac（已验证可用），JPACKAGE 需要完整路径
JAVA="java"
JAVAC="javac"
JAR="jar"
JPACKAGE="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot/bin/jpackage"

APP_NAME="大富翁Monopoly"
APP_VERSION="1.1"
MAIN_CLASS="Main"
OUT_DIR="$ROOT/out/release"
BUILD_DIR="$ROOT/build"
RELEASE_DIR="$ROOT/release"
LIB_DIR="$ROOT/lib"
SRC_DIR="$ROOT/src"

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
echo -e "[1/6] JDK: $("$JAVA" --version 2>&1 | head -1)"

if [ ! -f "$JPACKAGE" ]; then
    echo -e "${RED}错误: 找不到 jpackage${NC}"
    echo -e "${RED}路径: $JPACKAGE${NC}"
    echo -e "${RED}请修改 JPACKAGE 变量指向 JDK 25 的 bin/jpackage.exe${NC}"
    exit 1
fi

# ---- 2. 清理 ----
echo -e "[2/6] 清理旧文件..."
rm -rf "$OUT_DIR" "$BUILD_DIR" "$RELEASE_DIR"
mkdir -p "$OUT_DIR" "$BUILD_DIR" "$RELEASE_DIR"

# ---- 3. 合并依赖 jar ----
echo -e "[3/6] 合并依赖 JAR..."
mkdir -p "$OUT_DIR/tmp"
for jar in "$LIB_DIR"/*.jar; do
    if [ -f "$jar" ]; then
        echo "      → $(basename "$jar")"
        (cd "$OUT_DIR/tmp" && "$JAR" xf "$jar")
    fi
done
rm -rf "$OUT_DIR/tmp/META-INF"

# ---- 4. 编译 ----
echo -e "[4/6] 编译项目..."

"$JAVAC" -encoding UTF-8 \
    -cp "lib/java-websocket-1.5.7.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-nop-2.0.9.jar" \
    -d "$OUT_DIR" \
    -sourcepath "$SRC_DIR" \
    $(find "$SRC_DIR" -name "*.java")

echo -e "      编译完成"

# ---- 5. 复制资源 + 合并 ----
echo -e "[5/6] 打包 JAR..."

# 复制图片资源（保持 src/img/ 路径结构）
mkdir -p "$OUT_DIR/src"
cp -r "$SRC_DIR/img" "$OUT_DIR/src/"

# 合并依赖 class
if [ -d "$OUT_DIR/tmp" ]; then
    cp -r "$OUT_DIR/tmp"/* "$OUT_DIR/" 2>/dev/null || true
    rm -rf "$OUT_DIR/tmp"
fi

# 创建 MANIFEST
echo "Main-Class: $MAIN_CLASS" > "$BUILD_DIR/MANIFEST.MF"
echo "" >> "$BUILD_DIR/MANIFEST.MF"

# 打包 JAR
"$JAR" cfm "$BUILD_DIR/$APP_NAME.jar" "$BUILD_DIR/MANIFEST.MF" -C "$OUT_DIR" .

JAR_SIZE=$(du -h "$BUILD_DIR/$APP_NAME.jar" | cut -f1)
echo -e "      JAR: $BUILD_DIR/$APP_NAME.jar ($JAR_SIZE)"

# ---- 6. jpackage 生成绿色版 ----
echo -e "[6/6] 生成绿色版..."

"$JPACKAGE" \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --input "$BUILD_DIR" \
  --main-jar "$APP_NAME.jar" \
  --main-class "$MAIN_CLASS" \
  --type app-image \
  --dest "$RELEASE_DIR"

# ★ 关键：复制 src/img 到 release，否则 exe 找不到图片 ★
cp -r "$SRC_DIR" "$RELEASE_DIR/$APP_NAME/"
mkdir -p "$RELEASE_DIR/$APP_NAME/logs"

echo -e "      绿色版: $RELEASE_DIR/$APP_NAME/"

# ---- 完成 ----
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  打包完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "运行: 双击 ${YELLOW}release\\$APP_NAME\\$APP_NAME.exe${NC}"
echo ""
echo -e "目录内容:"
ls -lh "$RELEASE_DIR/$APP_NAME/" 2>/dev/null | head -8
echo "  src/img/  ← 图片资源"
echo ""
echo -e "✅ 用户无需装 Java，双击即运行！"
