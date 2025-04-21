package http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/*
	•	8080 포트에서 클라이언트 요청을 기다리고
	•	요청이 오면 “Hello, Client!“라고 답을 해준다.
 */
public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("서버가 8080대기중");

        while(true) {
            Socket socket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line = in.readLine();
            System.out.println(line);

            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: text/html\r\n");
            out.write("\r\n");
            out.write("Hello Client");
            out.flush();

            in.close();
            out.close();
            socket.close();
        }
    }
}
