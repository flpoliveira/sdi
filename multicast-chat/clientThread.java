
/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. When a client leaves the chat room this thread informs also
 * all the clients about that and terminates.
 */
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.*;

class clientThread extends Thread {

  private static DataInputStream is = null;
  private static PrintStream os = null;
  private static Socket clientSocket = null;
  // private static final clientThread[] threads;
  private static int maxClientsCount = 10;

  private static ServerSocket serverSocket = null;
  // The client socket.
  // private static Socket clientSocket = null;

  // This chat server can accept up to maxClientsCount clients' connections.
  // private static final int maxClientsCount = 10;
  private static clientThread[] threads = new clientThread[maxClientsCount];

  // MULTICAST
  private static DatagramSocket socket = null;
  private static DatagramPacket outPacket = null;
  private static byte[] outBuf;
  final int PORT = 1997;
  // private DatagramSocket socket;
  private static MulticastSocket mcsocket = null;

  public clientThread(Socket clientSocket, clientThread[] threads) throws SocketException {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
    socket = new DatagramSocket();
  }

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 6464;
    if (args.length < 1) {
      System.out.println("Usage: java MultiThreadChatServer <portNumber>\n" + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        // System.out.println("maxClient = " + maxClientsCount);
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads)).start();
            break;
          }
        }
        if (i == maxClientsCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;
    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      os.println("Enter your name.");
      String name = is.readLine().trim();

      os.println("Hello " + name + " to our chat room.\nTo leave enter /quit in a new line");
      sendPacket("*** A new user " + name + " entered the chat room !!! ***");
      while (true) {
        try {
          String line = is.readLine();
  
          if (line.startsWith("/quit")) {
            break;
          }
          sendPacket(line);
          long counter = 0;
          String msg;
          while (true) {
            msg = is.readLine();
            // outBuf = msg.getBytes();
            InetAddress address = InetAddress.getByName("224.0.0.10");
            // outPacket = new DatagramPacket(outBuf, outBuf.length, address, 1997);
            // socket.send(outPacket);
            sendPacket(msg);
            System.out.println("Server sends : " + msg);
            try {
              Thread.sleep(500);
            } catch (InterruptedException ie) {
            }
          }
        } catch (IOException ioe) {
          System.out.println(ioe);
        }
      }

      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          threads[i].os.println("*** The user " + name + " is leaving the chat room !!! ***");
        }
      }
      os.println("*** Bye " + name + " ***");

      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] == this) {
          threads[i] = null;
        }
      }

      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }

  private void sendPacket(String msg) throws IOException {
    byte[] buffer = msg.getBytes();
    socket.send(new DatagramPacket(buffer, buffer.length, InetAddress.getByName("224.0.0.10"), 1997));
  }

  private void sendPacket64(String name, String msg) throws IOException {
		int cabeca = (name + ": ").getBytes().length;
		while (msg.getBytes().length + cabeca > 64000) {
			sendPacket64(name, msg.substring(0, 64001 - cabeca));
			msg = msg.substring(64001 - cabeca, msg.length());
		}
		sendPacket(name + ": " + msg);
	}
}
