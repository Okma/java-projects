import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Carl on 2/18/2016.
 */
public class Server {

    /** Singleton instance of the server */
    public static Server Singleton = new Server();

    /** data members */
    private ServerSocket serverSocket;
    private ArrayList<Calculator> threads = new ArrayList<Calculator>();
    private boolean bShutdownRequested = false;

    /** static shutdown method */
    public static void ShutdownServer() {

        /** Signal that shutdown has been requested */
        Singleton.bShutdownRequested = true;

        /** Terminate all running threads */
        for(Calculator thread : Singleton.threads) {
            try {
                thread.socket.close();
            } catch(IOException io) {
                System.out.println("Error: Socket IO exception while attempt to interrupt thread at shutdown.");
            }
            thread.interrupt();
        }

        /** Close the server socket */
        try {
            Singleton.serverSocket.close();
        } catch(IOException io) {
            System.out.println("Error: Socket IO exception while attempt to close socket at shutdown.");
        }
    }

    public static void main(String[] args) throws Exception {
        // Initialize ServerSocket with port of args[0].
        String portString = args[0];
        System.out.printf("Server is now running on port %s.\n", portString);
        Integer portNum = Integer.parseInt(portString);

        Singleton.serverSocket = new ServerSocket(portNum);

        while(!Singleton.bShutdownRequested) {
            try {
                Calculator calc = new Calculator(Singleton.serverSocket.accept());
                calc.start();
                Singleton.threads.add(calc);
            } catch (SocketException e) {
                System.out.println("Server is shutting down. Goodbye!");
            }
        }

    }

    /** Create a private thread class to fire when a socket connection is received. */
    private static class Calculator extends Thread {

        // reference to connecting client socket
        private Socket socket;

        public Calculator(Socket socket) {
            this.socket = socket;
        }

        /** Overriding Thread run() */
        public void run() {
            boolean bShutdown = false;
            try {
                /** Create reader and writer for input and output, respectively. */
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                /** Output and instructions */
                output.println("Client connected.");
                output.println("Please enter your desired operation followed by 2-4 inputs, each separated by a space.");

                /** Until bad input, continuously read input from client connection. */
                while(true) {
                    String clientInput = input.readLine(); // read the line from reader

                    // create string of the inputs
                    String inputs = clientInput.substring(clientInput.indexOf(' ') + 1);

                    // split inputs separated by space
                    String[] inputArr = inputs.split(" ");

/*                    System.out.println(clientInput);
                    System.out.println(inputs);
                    for(String s : inputArr) {
                        System.out.println(s);
                    }*/

                    // error code -2: too few inputs
                    if(inputArr.length <  2) {

                        // 'bye' command
                        if(clientInput.equals("bye")) {
                            output.println("-5");
                            throw new InterruptedException("Client requests quit.");
                        }
                        // 'terminate' command
                        else if(clientInput.equals("terminate")) {
                            output.println("-5");
                            bShutdown = true;
                            throw new InterruptedException("Client requests server shutdown.");
                        }
                        else {
                            output.println("-2");
                            continue;
                        }
                    }
                    // error code -3: too many inputs
                    else if(inputArr.length > 4) {
                        output.println("-3");
                        continue;
                    }

                    // error code-4: check for bad input -- decimals, negatives, and fractions
                    if(inputs.contains("-") || inputs.contains(".") || inputs.contains("/")) {
                        output.println("-4");
                        continue;
                    }

                    // start attempting to parse all the input strings
                    ArrayList<Integer> numbers = new ArrayList<Integer>();
                    for(String s : inputArr) {
                        try {
                            numbers.add(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            // int not parable, throw bad input error code (-4)
                            numbers.clear();
                            output.println("-4");
                            break;
                        }
                    }

                    // catch failed number parse
                    if(numbers.size() == 0) {
                        continue;
                    }

                    float outputResult = numbers.get(0);
                    // check and execute operator
                    switch(clientInput.substring(0, clientInput.indexOf(' '))) {
                        case "add":
                            for (int i = 1; i < numbers.size(); i++) {
                                outputResult += numbers.get(i);
                            }
                            output.println(outputResult);
                            break;
                        case "subtract":
                            for (int i = 1; i < numbers.size(); i++) {
                                outputResult -= numbers.get(i);
                            }
                            output.println(outputResult);
                            break;
                        case "multiply":
                            for (int i = 1; i < numbers.size(); i++) {
                                outputResult *= numbers.get(i);
                            }
                            output.println(outputResult);
                            break;
                        case "divide":
                            for (int i = 1; i < numbers.size(); i++) {
                                outputResult /= numbers.get(i);
                            }
                            output.println(outputResult);
                            break;
                        default:
                            // invalid operation
                            output.println("-1");
                            break;
                    }
                }

            } catch(IOException e) {
                System.out.println("Failed to create socket/buffers from client socket.");
            } catch(InterruptedException ie) {

                System.out.println("Client disconnecting.");

                // handle server shutdown, if requested
                if(bShutdown) {
                    Server.ShutdownServer();
                }

            } finally {
                try {
                    socket.close();
                } catch (IOException io) {
                    System.out.println("Socket can't close or is invalid.");
                } finally {
                    this.interrupt();
                }
            }
        }
    }

}
