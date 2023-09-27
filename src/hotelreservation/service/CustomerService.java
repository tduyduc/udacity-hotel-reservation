package hotelreservation.service;

import hotelreservation.exception.CustomerAlreadyExistsException;
import hotelreservation.model.Customer;

import java.util.*;

public final class CustomerService {
  private static final CustomerService instance = new CustomerService();
  private CustomerService() {}

  private final Set<Customer> customers = new HashSet<>();

  public static CustomerService getInstance() {
    return CustomerService.instance;
  }

  public void addCustomer(String firstName, String lastName, String email)
    throws IllegalArgumentException, CustomerAlreadyExistsException {
    if (
      !this.customers.add(
        new Customer(firstName, lastName, email)
      )
    ) {
      throw new CustomerAlreadyExistsException();
    }
  }

  public Customer getCustomer(String customerEmail) {
    Objects.requireNonNull(customerEmail);
    return this.customers
      .stream()
      .filter(customer -> customer.getEmail().equals(customerEmail))
      .findFirst()
      .orElse(null);
  }

  public Collection<Customer> getAllCustomers() {
    return Collections.unmodifiableCollection(this.customers);
  }
}
