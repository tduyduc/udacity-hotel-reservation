package hotelreservation.model;

public class Tester {
  public static void main(String[] args) {
    final Customer customer = new Customer("first", "second", "j@domain.com");
    System.out.println(customer);

    try {
      final Customer invalidCustomer = new Customer("first", "second", "email");
      System.out.println(invalidCustomer);
    } catch (IllegalArgumentException iae) {
      if (!iae.getMessage().equals("Customer email is in incorrect format!")) {
        throw iae;
      }
      System.out.println("invalidCustomer has been correctly caught!");
    }
  }
}
