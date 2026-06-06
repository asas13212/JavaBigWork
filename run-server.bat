@echo off
chcp 65001 >nul
echo === 启动联机服务端（端口 8080）===
"C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot\bin\java" -cp "out;lib/java-websocket-1.5.7.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-nop-2.0.9.jar" server.MonopolyServer 8080
pause
