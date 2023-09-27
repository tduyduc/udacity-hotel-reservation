package hotelreservation.api;

import hotelreservation.exception.RoomAlreadyExistsException;
import hotelreservation.model.Customer;
import hotelreservation.model.IRoom;
import hotelreservation.service.CustomerService;
import hotelreservation.service.ReservationService;

import java.util.Collection;
import java.util.List;

public class AdminResource {
  private static final AdminResource instance = new AdminResource();
  private final CustomerService customerService = CustomerService.getInstance();
  private final ReservationService reservationService = ReservationService.getInstance();

  private AdminResource() {}

  public static AdminResource getInstance() {
    return AdminResource.instance;
  }

  public Customer getCustomer(String email) {
    return this.customerService.getCustomer(email);
  }

  public void addRooms(List<IRoom> rooms) {
    for (final IRoom room : rooms) {
      try {
        this.reservationService.addRoom(room);
      } catch (RoomAlreadyExistsException raee) {
        System.out.println("Room " + room.getRoomNumber() + " already exists. Skipping.");
        continue;
      }
      System.out.println(room.toString() + " has been added!");
    }
  }

  public Collection<IRoom> getAllRooms() {
    return this.reservationService.getAllRooms();
  }

  public Collection<Customer> getAllCustomers() {
    return this.customerService.getAllCustomers();
  }

  public void displayAllReservations() {
    this.reservationService.printAllReservations();
  }
}
