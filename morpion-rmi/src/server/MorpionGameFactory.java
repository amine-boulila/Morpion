package server;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import shared.MorpionCallback;

public class MorpionGameFactory {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> playerRoomMap = new ConcurrentHashMap<>();
    private static final long ROOM_TIMEOUT = 30 * 60 * 1000;
    private static final long GAMEOVER_CLEANUP_GRACE_PERIOD = 30 * 1000;
    private static final int MAX_ROOMS = 2;

    public MorpionGameFactory() {
        startCleanupTask();
    }

    private void startCleanupTask() {
        new Thread(this::cleanupRooms).start();
    }

    private void cleanupRooms() {
        while (true) {
            try {
                Thread.sleep(30000);
                long currentTime = System.currentTimeMillis();
                
                rooms.entrySet().removeIf(entry -> {
                    GameRoom room = entry.getValue();
                    try {
                        boolean isOver = room.isGameOver();
                        boolean inactive = currentTime - room.getLastActivity() > ROOM_TIMEOUT;
                        boolean gracePeriodOver = isOver && 
                            (currentTime - room.getLastActivity() > GAMEOVER_CLEANUP_GRACE_PERIOD);
                            
                        if (inactive || gracePeriodOver) {
                            Arrays.asList(room.getPlayerX(), room.getPlayerO())
                                  .forEach(player -> playerRoomMap.remove(player));
                            return true;
                        }
                    } catch (Exception e) {
                        return true;
                    }
                    return false;
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Core room management methods
    public synchronized String createRoom() throws RemoteException {
        if (rooms.size() >= MAX_ROOMS) {
            throw new RemoteException("Server is at maximum room capacity");
        }
        String roomId = "room-" + UUID.randomUUID().toString().substring(0, 8);
        rooms.put(roomId, new GameRoom());
        return roomId;
    }

    public boolean joinRoom(String roomId, String playerName, MorpionCallback callback) throws RemoteException {
        GameRoom room = rooms.get(roomId);
        if (room == null) return false;
        
        String result = room.registerPlayer(playerName, callback);
        if (result.startsWith("SUCCESS")) {
            playerRoomMap.put(playerName, roomId);
            return true;
        }
        return false;
    }

    public GameRoom getPlayerRoom(String playerName) throws RemoteException {
        String roomId = playerRoomMap.get(playerName);
        if (roomId == null) throw new RemoteException("Player not in any game room");
        GameRoom room = rooms.get(roomId);
        if (room == null) throw new RemoteException("Game room not found");
        return room;
    }

    public List<String> getAvailableRooms() {
        return rooms.entrySet().stream()
            .filter(entry -> {
                try {
                    return !entry.getValue().isGameReady();
                } catch (RemoteException e) {
                    return false;
                }
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public void removePlayer(String playerName) {
        playerRoomMap.remove(playerName);
    }

    public boolean isPlayerConnected(String playerName) {
        return playerRoomMap.containsKey(playerName);
    }
}