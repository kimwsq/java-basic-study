package file;

import java.io.File;

public class ListFilesExample {
    public static void main(String[] args) {
        File folder = new File("C:/example");  // 네가 탐색할 폴더 경로
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    System.out.println(file.getName());
                }
            }
        }
    }
}