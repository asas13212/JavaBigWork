@echo off
chcp 65001 >nul
echo === 编译大富翁（客户端 + 服务端）===

:: 清理
if exist out rmdir /s /q out
mkdir out

:: 全量编译（使用 JDK 25）
"C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot\bin\javac" -encoding UTF-8 ^
  -cp "lib/java-websocket-1.5.7.jar;lib/slf4j-api-2.0.9.jar" ^
  -d out ^
  src/shared/MessageType.java ^
  src/shared/Message.java ^
  src/debug/Log.java ^
  src/debug/DebugTools.java ^
  src/main/ConstantNum.java ^
  src/main/GameMode.java ^
  src/main/GameController.java ^
  src/main/NetworkClient.java ^
  src/main/Player.java ^
  src/main/LocalController.java ^
  src/main/RemoteController.java ^
  src/main/OnlinePanel.java ^
  src/main/LogPanel.java ^
  src/main/BoardConfig.java ^
  src/main/DiceController.java ^
  src/main/MainMap.java ^
  src/main/GameMenu.java ^
  src/main/GameCard.java ^
  src/main/ModeSelectPanel.java ^
  src/main/LeaderAnim.java ^
  src/main/Win.java ^
  src/main/AIDecision.java ^
  src/main/UILayerPanel.java ^
  src/main/TileLayerPanel.java ^
  src/main/ActorLayerPanel.java ^
  src/architecture/Tile.java ^
  src/architecture/TileType.java ^
  src/architecture/Land.java ^
  src/architecture/HotelLand.java ^
  src/architecture/ShopLand.java ^
  src/architecture/ResidentLand.java ^
  src/architecture/GymLand.java ^
  src/architecture/ParkLand.java ^
  src/architecture/Casino.java ^
  src/architecture/Chance.java ^
  src/architecture/Event.java ^
  src/architecture/Hospital.java ^
  src/architecture/Prison.java ^
  src/architecture/Start.java ^
  src/architecture/Shop.java ^
  src/architecture/Empty.java ^
  src/props/Prop.java ^
  src/props/BaoZi.java ^
  src/props/Barrier.java ^
  src/props/Mine.java ^
  src/props/Dice.java ^
  src/props/ExamWeek.java ^
  src/props/HouseLevelUp.java ^
  src/props/IdCard.java ^
  src/props/Theft.java ^
  src/server/util/MessageCodec.java ^
  src/server/engine/GameState.java ^
  src/server/engine/GameEngine.java ^
  src/server/engine/EngineEvent.java ^
  src/server/engine/MonopolyEngine.java ^
  src/server/session/PlayerSession.java ^
  src/server/session/SessionManager.java ^
  src/server/room/Room.java ^
  src/server/room/RoomManager.java ^
  src/server/room/MonopolyRoom.java ^
  src/server/transport/WebSocketEndpoint.java ^
  src/server/MonopolyServer.java ^
  src/Main.java

if %errorlevel% neq 0 (
  echo.
  echo === 编译失败！ ===
  pause
  exit /b 1
)

echo.
echo === 编译成功！ ===
echo.
echo 启动客户端: run-client.bat
echo 启动服务端: run-server.bat
pause
