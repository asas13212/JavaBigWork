#!/bin/bash
# 大富翁联机服务端编译打包脚本
# 用法: bash build-server.sh

set -e

echo "=== 编译服务端 ==="

# 清理旧输出
rm -rf out/server
mkdir -p out/server

# 编译所有 server 文件（含共享依赖）
javac -encoding UTF-8 \
  -cp "lib/java-websocket-1.5.7.jar;lib/slf4j-api-2.0.9.jar" \
  -d out/server \
  src/shared/MessageType.java \
  src/shared/Message.java \
  src/server/util/MessageCodec.java \
  src/server/engine/GameState.java \
  src/server/engine/GameEngine.java \
  src/server/engine/EngineEvent.java \
  src/server/engine/MonopolyEngine.java \
  src/server/session/PlayerSession.java \
  src/server/session/SessionManager.java \
  src/server/room/Room.java \
  src/server/room/RoomManager.java \
  src/server/room/MonopolyRoom.java \
  src/server/transport/WebSocketEndpoint.java \
  src/server/MonopolyServer.java

echo "编译成功！"

# 打包（将依赖 jar 解压合并）
mkdir -p out/server/jar
cp lib/java-websocket-1.5.7.jar lib/slf4j-api-2.0.9.jar lib/slf4j-nop-2.0.9.jar out/server/jar/
cd out/server/jar
jar xf java-websocket-1.5.7.jar
jar xf slf4j-api-2.0.9.jar
jar xf slf4j-nop-2.0.9.jar
rm -f java-websocket-1.5.7.jar slf4j-api-2.0.9.jar slf4j-nop-2.0.9.jar META-INF/MANIFEST.MF
cd ..

# 创建 MANIFEST
echo "Main-Class: server.MonopolyServer" > MANIFEST.MF

# 打包 (当前在 out/server/)
jar cfm monopoly-server.jar MANIFEST.MF -C jar/ . -C . server shared

# 清理临时文件
rm -rf jar MANIFEST.MF

cd ../..

echo ""
echo "=== 打包完成: out/server/monopoly-server.jar ==="
echo ""
echo "部署命令:"
echo "  scp out/server/monopoly-server.jar root@<阿里云IP>:/opt/monopoly-server/"
echo "  ssh root@<阿里云IP> 'java -jar /opt/monopoly-server/monopoly-server.jar &'"
echo ""
echo "本地测试:"
echo "  java -jar out/server/monopoly-server.jar"
