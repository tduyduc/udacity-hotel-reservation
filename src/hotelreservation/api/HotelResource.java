package hotelreservation.api;

import hotelreservation.exception.CustomerAlreadyExistsException;
import hotelreservation.exception.CustomerNotFoundException;
import hotelreservation.exception.RoomAlreadyReservedException;
import hotelreservation.model.Customer;
import hotelreservation.model.IRoom;
import hotelreservation.model.Reservation;
import hotelreservation.service.CustomerService;
import hotelreservation.service.ReservationService;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

public class HotelResource {
  private static final HotelResource instance = new HotelResource();
  private final CustomerService customerService = CustomerService.getInstance();
  private final ReservationService reservationService = ReservationService.getInstance();

  private HotelResource() {}

  public static HotelResource getInstance() {
    return HotelResource.instance;
  }

  public Customer getCustomer(String email) {
    return this.customerService.getCustomer(email);
  }

  public void createACustomer(String email, String firstName, String lastName) throws CustomerAlreadyExistsException {
    this.customerService.addCustomer(firstName, lastName, email);
  }

  public IRoom getRoom(String roomNumber) {
    return this.reservationService.getARoom(roomNumber);
  }

  public Reservation bookARoom(String customerEmail, IRoom room, Date checkInDate, Date checkOutDate)
    throws CustomerNotFoundException, RoomAlreadyReservedException {
    final Customer customer = this.getCustomerOrThrow(customerEmail);
    return this.reservationService.reserveARoom(customer, room, checkInDate, checkOutDate);
  }

  public Collection<Reservation> getCustomerReservations(String customerEmail)
    throws CustomerNotFoundException {
    return this.reservationService.getCustomerReservations(
      this.getCustomerOrThrow(customerEmail)
    );
  }

  public Collection<IRoom> findRooms(Date checkIn, Date checkOut) {
    return this.reservationService.findRooms(checkIn, checkOut);
  }

  private Customer getCustomerOrThrow(String customerEmail) throws CustomerNotFoundException {
    return Optional.ofNullable(this.getCustomer(customerEmail)).orElseThrow(CustomerNotFoundException::new);
  }
}
