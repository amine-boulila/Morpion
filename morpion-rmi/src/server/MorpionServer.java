package server;

import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import shared.*;

public class MorpionServer implements MorpionInterface {
    static {
        // Set the security policy file
        System.setProperty("java.security.policy", "server.policy");

        // Enable the security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }

    private final MorpionGameFactory roomFactory = new MorpionGameFactory();

    public MorpionServer() {
        initializeSharedFiles();
    }

    @Override
    public List<String> listAvailableRooms() throws RemoteException {
        return roomFactory.getAvailableRooms();
    }

    @Override
    public synchronized String createNewRoom() throws RemoteException {
        return roomFactory.createRoom();
    }

    @Override
    public boolean joinRoom(String roomId, String playerName, MorpionCallback callback) throws RemoteException {
        return roomFactory.joinRoom(roomId, playerName, callback);
    }

    // Player-specific operations
    @Override
    public String makeMove(int row, int col, String playerName) throws RemoteException {
        return roomFactory.getPlayerRoom(playerName).makeMove(row, col, playerName);
    }

    @Override
    public boolean isPlayerTurn(String playerName) throws RemoteException {
        return roomFactory.getPlayerRoom(playerName).isPlayerTurn(playerName);
    }

    @Override
    public void resetGame(String playerName) throws RemoteException {
        GameRoom room = roomFactory.getPlayerRoom(playerName);
        room.resetGame();
        room.getPlayerXCallback().updateBoard(room.getBoardState());
        room.getPlayerOCallback().updateBoard(room.getBoardState());
    }

    // Interface compliance methods
    @Override
    public String getCurrentBoard() throws RemoteException {
        throw new UnsupportedOperationException("Player context required");
    }

    @Override
    public boolean isGameOver() throws RemoteException {
        throw new UnsupportedOperationException("Player context required");
    }

    @Override
    public String getWinner() throws RemoteException {
        throw new UnsupportedOperationException("Player context required");
    }

    @Override
    public boolean isGameReady() throws RemoteException {
        throw new UnsupportedOperationException("Player context required");
    }

    @Override
    public void resetGame() throws RemoteException {
        throw new UnsupportedOperationException("Player context required");
    }

    // Other implemented methods
    @Override
    public void disconnectPlayer(String playerName) throws RemoteException {
        try {
            GameRoom room = roomFactory.getPlayerRoom(playerName);
            room.disconnectPlayer(playerName);
        } finally {
            roomFactory.removePlayer(playerName);
        }
    }

    @Override
    public String getPlayerSymbol(String playerName) throws RemoteException {
        return roomFactory.getPlayerRoom(playerName).getPlayerSymbol(playerName);
    }

    @Override
    public Map<String, Integer> getPlayerStats(String playerName) throws RemoteException {
        return roomFactory.getPlayerRoom(playerName).getPlayerStats(playerName);
    }

    @Override
    public List<String> getMatchHistory(String playerName) throws RemoteException {
        return roomFactory.getPlayerRoom(playerName).getMatchHistory(playerName);
    }

    @Override
    public String getOpponentName(String playerName) throws RemoteException {
        return roomFactory.getPlayerRoom(playerName).getOpponentName(playerName);
    }

    @Override
    public boolean isPlayerConnected(String playerName) throws RemoteException {
        return roomFactory.isPlayerConnected(playerName);
    }

    // Maintain original registration method
    @Override
    public String registerPlayer(String playerName, MorpionCallback callback) throws RemoteException {
        throw new UnsupportedOperationException("Use createNewRoom/joinRoom for registration");
    }

    private void initializeSharedFiles() {
    String[] filesToLoad = {"MorpionCallback.class", "MorpionInterface.class"};
    for (String file : filesToLoad) {
        loadSharedFile(file);
    }
}

private void loadSharedFile(String fileName) {
    try {
        String url = "http://localhost/shared/";
        URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL(url)});
        
        String className = "shared." + fileName.replace(".class", "");
        Class<?> loadedClass = classLoader.loadClass(className);
        
        System.out.println("Successfully loaded class from Apache: " + loadedClass.getName());
    } catch (Exception e) {
        System.err.println("Failed to load class from Apache: " + fileName);
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        try {
            MorpionServer server = new MorpionServer();
            MorpionInterface stub = (MorpionInterface) UnicastRemoteObject.exportObject(server, 0);
            LocateRegistry.createRegistry(1099).rebind("MorpionGame", stub);
            System.out.println("Server operational on RMI registry");
        } catch (Exception e) {
            System.err.println("Server startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}