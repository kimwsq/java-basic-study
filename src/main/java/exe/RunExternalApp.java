package exe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunExternalApp {
    public static void main(String[] args) {
        // 실행할 파일 경로 (절대경로 or 상대경로 가능)
        String exePath = "C:\\path\\to\\yourApp.exe";

        // 전달할 인자들
        String arg1 = "hello";
        String arg2 = "world";

        // ProcessBuilder에 실행파일과 인자들을 순서대로 추가
        ProcessBuilder processBuilder = new ProcessBuilder(exePath, arg1, arg2);

        try {
            // 프로세스 시작
            Process process = processBuilder.start();

            BufferedReader stdOutReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            // 에러 출력 스트림
            BufferedReader stdErrReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream())
            );

            String line;

            System.out.println("=== 표준 출력 ===");
            while ((line = stdOutReader.readLine()) != null) {
                System.out.println(line);
            }

            System.out.println("\n=== 에러 출력 ===");
            while ((line = stdErrReader.readLine()) != null) {
                System.err.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\n프로세스 종료 코드: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
