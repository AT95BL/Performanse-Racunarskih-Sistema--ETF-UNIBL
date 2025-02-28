import java.util.Random;
import java.util.Scanner;

public class NumberPi {

    public static void main(String[] args) {

        Random rand = new Random();
        double x, y;
        int brInside = 0, brOutside = 0;

        System.out.println("Prava vrijednost broja PI: " + Math.PI + "\n");
        System.out.println("1 - Generisanje slučajnih tačaka za procjenu π");
        System.out.println("2 - Postavljanje preciznosti za procjenu π");

        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
        double pi = 0.0;

        if (n == 1) {

            System.out.println("Unesite broj generisanja");
            int m = scan.nextInt();

            if (m <= 0) {
                System.out.println("Broj generisanja mora biti pozitivan broj.");
                return; // Prekini program ako je broj generisanja neispravan
            }
            for (int i = 0; i < m; i++) {
                x = rand.nextDouble();
                y = rand.nextDouble();
                if (Math.pow(x, 2) + Math.pow(y, 2) < Math.pow(1, 2))
                    brInside++;
                brOutside++;
            }
            pi = 4 * ((double) brInside / (double) brOutside);
            System.out.println(pi);

        } else if (n == 2) {
            System.out.println("Unesite broj preciznosti m");
            int m = scan.nextInt();
            if (m <= 0) {
                System.out.println("Broj preciznosti mora biti pozitivan broj.");
                return; // Prekini program ako je broj preciznosti neispravan
            }
            do {
                x = rand.nextDouble();
                y = rand.nextDouble();
                if (Math.pow(x, 2) + Math.pow(y, 2) < Math.pow(1, 2))
                    brInside++;
                brOutside++;
                if (brOutside != 0) {
                    pi = 4 * ((double) brInside / (double) brOutside);
                }
            } while ((Math.abs(Math.PI - pi)) > 1 / Math.pow(10, m + 1));
            System.out.println(pi);
        }
        scan.close();
    }
}
