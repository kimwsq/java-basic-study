package test;

import java.io.File;

/*
	현재 프로젝트 src/ 폴더에 있는 모든 .java 파일 이름을 출력한다.
	src/test/Test3Example.java
    src/test/Test1Example.java
    src/test/Test2Example.java
    src/file/ListFilesExample.java
    src/io/WriteFileExample.java
    src/io/ReadFileExample.java
    src/string/JoinStringExample.java
    src/string/SplitStringExample.java
 */
//public class Test3Example {
//    public static void main(String[] args) {
//        File folder = new File("src");  // src 폴더 기준
//
//        listJavaFiles(folder);
//    }
//
//    private static void listJavaFiles(File folder) {
//        File[] listOfFiles = folder.listFiles();
//
//        if (listOfFiles != null) {
//            for (File file : listOfFiles) {
//                if (file.isDirectory()) {
//                    // 📁 하위 폴더가 있으면 재귀적으로 호출
//                    listJavaFiles(file);
//                } else if (file.isFile() && file.getName().endsWith(".java")) {
//                    // 📄 .java 파일이면 이름 출력
//                    System.out.println(file.getPath());
//                }
//            }
//        }
//    }
//}

/*
	현재 프로젝트 src/ 폴더에 있는 모든 .java 파일 이름을 출력한다.
	ListFilesExample
	ReadFileExample....
 */
public class Test3Example {
    public static void main(String[] args) {
        File folder = new File("src");  // src 폴더 기준

        listJavaFiles(folder);
    }

    private static void listJavaFiles(File folder) {
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    // 📁 하위 폴더가 있으면 재귀적으로 호출
                    listJavaFiles(file);
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    // 📄 .java 파일이면 파일 이름 출력 (확장자 제거)
                    String fileName = file.getName();
                    String withoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                    System.out.println(withoutExtension);
                }
            }
        }
    }
}

