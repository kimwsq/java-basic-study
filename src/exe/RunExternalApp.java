package exe;

import java.io.IOException;

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

            // 프로세스가 끝날 때까지 기다림 (옵션)
            int exitCode = process.waitFor();
            System.out.println("실행 완료. 종료 코드: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
