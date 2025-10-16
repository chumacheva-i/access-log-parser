import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Введите 1e число");
        int first_number = new Scanner(System.in).nextInt();
        //System.out.println(first_number);
        System.out.println("Введите 2e число");
        int second_number = new Scanner(System.in).nextInt();
       // System.out.println(second_number);
        int sum=first_number+second_number;
        System.out.println("Сумма = "+sum);
        int dif=first_number-second_number;
        System.out.println("Разность = "+ dif);
        int op=first_number*second_number;
        System.out.println("Произведение = " +op);

        if (second_number == 0) {
            System.out.println("Частное = Введенное вами второе число=0, делить на ноль нельзя");
        }
        else {
            double quotient = (double) first_number / second_number;
            System.out.println("Частное = " + quotient);
        }
    }
}
