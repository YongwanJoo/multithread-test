import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String serverHost = "172.16.226.127";
        int serverPort = 7777;

        try (Socket socket = new Socket(serverHost, serverPort)) {
            System.out.println("Connected to the server.");
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("Enter a message: ");
                String message = sc.nextLine();
                //bye로 접속 종료
                if (message.equalsIgnoreCase("bye")) {
                    output.println(message);
                    break;
                }
                output.println(message);
                String resultFromServer = input.readLine();
                System.out.println("Result from the server: " + resultFromServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
