package thread;

import java.io.*;
import java.net.*;
import java.util.Random;
/*
 지연시간 랜덤 테스트서버
	1.	외부 서버 실행 (9001)
 java SimulatedExternalServer 9001
	2.	각각의 프록시 서버 실행 (8081, 8082)
	3.	브라우저 또는 클라이언트에서 / 요청 여러 번 반복 전송

 */
public class SimulatedExternalServer {
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9001;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("외부 서버 포트 " + port + " 실행 중...");

        Random random = new Random();

        while (true) {
            Socket socket = serverSocket.accept();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            int delay = 500 + random.nextInt(1000); // 0.5~1.5초
            try { Thread.sleep(delay); } catch (InterruptedException ignored) {}

            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: text/plain\r\n\r\n");
            out.write("응답 from port " + port + " (지연: " + delay + "ms)");
            out.flush();
            socket.close();
        }
    }
}