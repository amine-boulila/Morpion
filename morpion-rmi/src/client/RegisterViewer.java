package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegisterViewer {
    public static void main(String[] args) {
        try {
            // Connect to the RMI registry
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // List all objects in the registry
            System.out.println("Objects in the RMI registry:");
            String[] boundNames = registry.list();
            for (String name : boundNames) {
                System.out.println("- " + name);
            }
        } catch (Exception e) {
            System.err.println("Error listing objects in registry: " + e.getMessage());
            e.printStackTrace();
        }
    }
}