package server.room;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager
{
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Random rng = new Random();

    /** Create room, return 4-digit room ID */
    public String createRoom()
    {
        String roomId;
        do {
            roomId = String.format("%04d", rng.nextInt(10000));
        } while (rooms.containsKey(roomId));

        Room room = new MonopolyRoom(roomId);
        rooms.put(roomId, room);
        return roomId;
    }

    public Room getRoom(String roomId) { return rooms.get(roomId); }
    public void removeRoom(String roomId) { rooms.remove(roomId); }
    public int getRoomCount() { return rooms.size(); }
}
