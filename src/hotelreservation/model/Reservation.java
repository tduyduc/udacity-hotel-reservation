package hotelreservation.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Reservation {
  private static final SimpleDateFormat ISO_8601_SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private final Customer customer;
  private final IRoom room;
  private final Date checkInDate;
  private final Date checkOutDate;

  public Reservation(Customer customer, IRoom room, Date checkInDate, Date checkOutDate)
    throws IllegalArgumentException {
    this.customer = Objects.requireNonNull(customer);
    this.room = Objects.requireNonNull(room);

    if (Objects.requireNonNull(checkInDate).after(Objects.requireNonNull(checkOutDate))) {
      throw new IllegalArgumentException("Check-in date must be same or before check-out date!");
    }
    this.checkInDate = checkInDate;
    this.checkOutDate = checkOutDate;
  }

  public Customer getCustomer() {
    return this.customer;
  }

  public IRoom getRoom() {
    return this.room;
  }

  public Date getCheckInDate() {
    return this.checkInDate;
  }

  public Date getCheckOutDate() {
    return this.checkOutDate;
  }

  @Override
  public String toString() {
    return (
      "Reservation: " +
        this.customer +
        " - " +
        this.room +
        " [" +
        Reservation.ISO_8601_SIMPLE_DATE_FORMAT.format(this.checkInDate) +
        " to " +
        Reservation.ISO_8601_SIMPLE_DATE_FORMAT.format(this.checkOutDate) +
        "]"
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (null == obj || this.getClass() != obj.getClass()) return false;

    final Reservation that = (Reservation) obj;
    return (
      this.customer.equals(that.customer) &&
        this.room.equals(that.room) &&
        this.checkInDate.equals(that.checkInDate) &&
        this.checkOutDate.equals(that.checkOutDate)
    );
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash *= 13;
    hash += this.customer.hashCode();
    hash *= 13;
    hash += this.room.hashCode();
    hash *= 13;
    hash += this.checkInDate.hashCode();
    hash *= 13;
    hash += this.checkOutDate.hashCode();
    return hash;
  }
}
