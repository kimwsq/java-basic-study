package proxy;

import java.io.*;
import java.net.*;

/*
외부 프로그램 만들기
# 첫 번째 터미널
java ExternalServer 8001

# 두 번째 터미널
java ExternalServer 8002
 */


public class ExternalServer {
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8001;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("외부 서버 실행 중: " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: text/plain\r\n");
            out.write("\r\n");
            out.write("응답 from external server at port " + port);
            out.flush();
            socket.close();
        }
    }
}