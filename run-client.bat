@echo off
chcp 65001 >nul
echo === 启动大富翁客户端 ===
java -cp "out;lib/java-websocket-1.5.7.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-nop-2.0.9.jar" Main
pause
