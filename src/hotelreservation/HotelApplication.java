package hotelreservation;

import hotelreservation.menu.MainMenu;

import java.util.Scanner;

public class HotelApplication {
  public static void main(String[] args) {
    final Scanner scanner = new Scanner(System.in);
    new MainMenu(scanner).displayMenu();
    scanner.close();
  }
}
