import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Carl on 2/18/2016.
 */
public class Client {

    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;

    public static void main(String[] args) {

        String serverAddress = args[0];
        int portNum = Integer.parseInt(args[1]);

        try {
            new Client(serverAddress, portNum);
        } catch(IOException e) {
            System.out.println("Error: Could not connect to server.");
        }
    }

    public Client(String serverAddress, int portNum) throws IOException {

        // establish connection to server
        Socket socket = new Socket(serverAddress, portNum);

        // assign input and output for client
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        scanner = new Scanner(System.in);

        // first get the initial server messages
        for(int j = 0; j < 2; j++) {
            String message = input.readLine();
            System.out.println(message);
        }

        // then continuously send input from user and display output from server
        while(true) {

            // send input to server
            String userInput = scanner.nextLine();
            output.println(userInput);

            // display server messages
            String outputMessage = input.readLine();
            System.out.println(outputMessage);

            // catch termination error code
            if(outputMessage.equals("-5")) {
                break;
            }
        }

    }

}
