package de.lmu.ifi.sosylab.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The network layer of the chat server. Takes care of processing both the connection requests and
 * message handling.
 */
public class ServerNetworkConnection {

  private static final int PORT = 8080;

  private final ExecutorService executorService;

  private final ServerSocket socket;

  private final List<GameRoom> gameRooms;


  private final Runnable connectionAcceptor = new Runnable() {
    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          Socket clientSocket = socket.accept();
          UserMessageHandler handler =
              new UserMessageHandler(ServerNetworkConnection.this, clientSocket);
          System.out.println("server runs");
          executorService.execute(handler);
          System.out.println("execute runs");
        }
      } catch (IOException e) {
        // Thrown when the socket gets interrupted
      }
    }
  };



  /**
   * Set up a new {@link ServerNetworkConnection} that handles all the incoming client connection
   * requests and enables them to exchange messages with each other.
   *
   * @throws IOException thrown when the socket is unable to be created at the given port
   */
  public ServerNetworkConnection() throws IOException {
    executorService = Executors.newCachedThreadPool();
    socket = new ServerSocket(PORT);
    gameRooms = Collections.synchronizedList(new ArrayList<>());

  }

  /**
   * Check if a chosen room name is still available.
   *
   * @param roomName The room name to be looked up
   * @return <code>true</code> if no other room has taken this name, <code>false</code> otherwise.
   */
  public boolean isRoomNameAvailable(String roomName) {
    synchronized (gameRooms) {
      return !gameRooms.stream().anyMatch(e -> e.getRoomName().equals(roomName));
    }
  }

  public void addNewRoom(GameRoom gameRoom) {
    synchronized (gameRooms) {
      gameRooms.add(gameRoom);
    }
  }

  public void removeGameRoom(GameRoom gameRoom) {
    synchronized (gameRooms) {
      gameRooms.remove(gameRoom);
    }
  }


  public List<GameRoom> getGameRooms() {
    return List.copyOf(gameRooms);
  }

  /**
   * Start the network-connection, so that clients can establish a connection to this server.
   */
  public void start() {
    executorService.execute(connectionAcceptor);
  }

  /**
   * Stop the network-connection.
   */
  public void stop() {
    executorService.shutdownNow();
    try {
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}