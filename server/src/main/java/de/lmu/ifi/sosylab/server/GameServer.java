package de.lmu.ifi.sosylab.server;

import java.io.IOException;

/**
 * The main class of the game server. It starts the application to let clients connect themselves to
 * this server, and deals with the distribution of incoming actions accordingly.
 */
public class GameServer {
  /** Launch the game server. */
  public static void main(String[] args) throws IOException {
    final ServerNetworkConnection connection = new ServerNetworkConnection();
    connection.start();

    Runtime.getRuntime().addShutdownHook(new Thread(connection::stop));
  }
}
