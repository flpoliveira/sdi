
//Example 25

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.*;

public class MultiThreadChatClient implements Runnable {

    // The client socket
    private static Socket clientSocket = null;
    // The output stream
    private static PrintStream os = null;
    // The input stream
    private static DataInputStream is = null;

    private static BufferedReader inputLine = null;
    private static boolean closed = false;

    // MULTICAST
    private static MulticastSocket socket = null;
    private static DatagramPacket inPacket = null;
    private static byte[] inBuf = new byte[256];

    public static void main(String[] args) {

        // The default port.
        int portNumber = 6464;
        // The default host.
        String host = "10.20.221.230";

        if (args.length < 2) {
            System.out.println("Usage: java MultiThreadChatClient <host> <portNumber>\n" + "Now using host=" + host
                    + ", portNumber=" + portNumber);
        } else {
            host = args[0];
            portNumber = Integer.valueOf(args[1]).intValue();
        }

        /*
         * Open a socket on a given host and port. Open input and output streams.
         */
        try {
            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + host);
        }

        /*
         * If everything has been initialized then we want to write some data to the
         * socket we have opened a connection to on the port portNumber.
         */

        if (clientSocket != null && os != null && is != null) {
            try {

                /* Create a thread to read from the server. */
                // new Thread(new MultiThreadChatClient()).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //código para executar em paralelo
                        MulticastSocket socket = null;
                        DatagramPacket inPacket = null;
                        byte[] inBuf = new byte[256];
                        try {
                            //Prepare to join multicast group
                            socket = new MulticastSocket(1997);
                            //   InetAddress address = InetAddress.getByName("224.0.0.1");      
                            InetAddress address = InetAddress.getByName("224.0.0.10");

                            socket.joinGroup(address);
                        
                            while (true) {
                                inPacket = new DatagramPacket(inBuf, inBuf.length);
                                socket.receive(inPacket);
                                String msg = new String(inBuf, 0, inPacket.getLength());
                                System.out.println("From " + inPacket.getAddress() + " Msg : " + msg);
                            }
                        } catch (IOException ioe) {
                                System.out.println(ioe);
                        }
                    }
                }).start();

                while (!closed) {
                    // MAP: Efetua a leitura do teclado das novas mensagens do
                    // cliente e envia para o servidor via socket;
                    //System.out.println("Aqui");
                    String Name = inputLine.readLine().trim();

                    //System.out.println("Aqui2");
                    os.println("username: " + Name);
                    // MULTICAST RECEIVER
                    try {
                        // Prepare to join multicast group
                        socket = new MulticastSocket(1997);
                        // InetAddress address = InetAddress.getByName("224.0.0.1");
                        InetAddress address = InetAddress.getByName("224.0.0.10");
                        socket.joinGroup(address);


                        while (true) {
                            // ESCRITA
                            System.out.println("Digite");
                            String texto = inputLine.readLine();
                            // texto += '\n';
                            os.println(texto);
                        }
                    } catch (IOException ioe) {
                        System.out.println(ioe);
                    }
                }
                /*
                 * Close the output stream, close the input stream, close the socket.
                 */
                os.close();
                is.close();
                clientSocket.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }

    /*
     * Create a thread to read from the server. (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        /*
         * Keep on reading from the socket till we receive "Bye" from the server. Once
         * we received that then we want to break.
         */

        try {
            while (true) {
                inPacket = new DatagramPacket(inBuf, inBuf.length);
                socket.receive(inPacket);
                String msg = new String(inBuf, 0, inPacket.getLength());
                System.out.println("From " + inPacket.getAddress() + " Msg : " + msg);
            }
        } catch (IOException ioe) {
                System.out.println(ioe);
        }

    }
}
