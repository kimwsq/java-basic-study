package sample;

/*
	•	문자열 "dog#cat#bird"가 있다.
	•	이 문자열을 #로 분리한 뒤,
	•	다시 | 기호로 합쳐서 출력한다.
 */
public class Test2Example {
    public static void main(String[] args) {
        String input = "dog#cat#brid";
        String[] inputArr = input.split("#");

        String output = String.join("|", inputArr);
        System.out.println(output);
    }
}
