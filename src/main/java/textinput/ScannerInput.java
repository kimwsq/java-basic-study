package textinput;

import java.io.File;
import java.util.Scanner;

public class ScannerInput {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);  // 입력 스트림 준비

        while (true) {
            System.out.print("이름을 입력하세요 (exit 입력 시 종료): ");
            String name = scanner.nextLine();

            if (name.equalsIgnoreCase("exit")) {
                System.out.println("입력을 종료합니다.");
                break;
            }

            System.out.print("나이를 입력하세요: ");
            String ageInput = scanner.nextLine();

            int age;
            try {
                age = Integer.parseInt(ageInput);  // 숫자 검증
            } catch (NumberFormatException e) {
                System.out.println("⚠️ 나이는 숫자로 입력해주세요!");
                continue;
            }

            System.out.println("👤 입력된 정보 → 이름: " + name + ", 나이: " + age);
            System.out.println("------");
        }

        scanner.close();  // 자원 정리
    }

    public static void readFile(String[] args) throws Exception {
        File file = new File("input.txt");
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            System.out.println("읽은 줄: " + line);
        }
    }

}