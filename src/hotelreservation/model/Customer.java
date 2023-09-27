package hotelreservation.model;

import java.util.Objects;
import java.util.regex.Pattern;

public class Customer {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

  private final String firstName;
  private final String lastName;
  private final String email;

  public Customer(String firstName, String lastName, String email) throws IllegalArgumentException {
    if (Objects.requireNonNull(firstName).isEmpty() || Objects.requireNonNull(lastName).isEmpty()) {
      throw new IllegalArgumentException("Customer name and email must not be empty!");
    }

    if (!Customer.EMAIL_PATTERN.matcher(email).matches()) {
      throw new IllegalArgumentException("Customer email is in incorrect format!");
    }

    this.firstName = firstName;
    this.lastName = lastName;
    this.email = Objects.requireNonNull(email);
  }

  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public String getEmail() {
    return this.email;
  }

  @Override
  public String toString() {
    return this.firstName + " " + this.lastName + " <" + this.email + ">";
  }

  @Override
  public boolean equals(Object obj) {
    return (
      this == obj || (
        obj instanceof Customer && this.email.equals(((Customer) obj).email)
      )
    );
  }

  @Override
  public int hashCode() {
    return this.email.hashCode();
  }
}
