package ai.promethean;
import ai.promethean.DataModel.*;
public class Main {

    public static void main(String[] args) {
	// write your code here
        System.out.println("Hello World");
        NumericalProperty np= new NumericalProperty("Wheel Speed", 100.0);
        System.out.println(np.toString());
    }
}
