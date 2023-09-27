package hotelreservation.model;

import java.util.Objects;

public class Room implements IRoom {
  private final String roomNumber;
  private final double price;
  private final RoomType roomType;

  public Room(String roomNumber, double price, RoomType roomType) throws IllegalArgumentException {
    if (!Double.isFinite(price) || price < 0) {
      throw new IllegalArgumentException("Price must be a non-negative decimal number!");
    }
    this.price = price;

    this.roomNumber = Objects.requireNonNull(roomNumber);
    this.roomType = Objects.requireNonNull(roomType);
  }

  @Override
  public String getRoomNumber() {
    return this.roomNumber;
  }

  @Override
  public double getRoomPrice() {
    return this.price;
  }

  @Override
  public RoomType getRoomType() {
    return this.roomType;
  }

  @Override
  public boolean isFree() {
    return 0. == this.price;
  }

  @Override
  public String toString() {
    return (
      "Room " +
        this.roomNumber +
        " - " +
        (RoomType.DOUBLE == this.roomType ? "Double" : "Single") +
        " - " +
        (this.isFree() ? "FREE" : ("$" + this.price))
    );
  }

  @Override
  public boolean equals(Object obj) {
    return (
      this == obj || (
        obj instanceof Room && this.roomNumber.equals(((Room) obj).roomNumber)
      )
    );
  }

  @Override
  public int hashCode() {
    return this.roomNumber.hashCode();
  }
}
