package hotelreservation.service;

import hotelreservation.exception.RoomAlreadyExistsException;
import hotelreservation.exception.RoomAlreadyReservedException;
import hotelreservation.model.Customer;
import hotelreservation.model.IRoom;
import hotelreservation.model.Reservation;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class ReservationService {
  private static final ReservationService instance = new ReservationService();
  private ReservationService() {}

  private final List<Reservation> reservations = new LinkedList<>();
  private final Set<IRoom> rooms = new HashSet<>();

  public static ReservationService getInstance() {
    return ReservationService.instance;
  }

  public void addRoom(IRoom room) throws RoomAlreadyExistsException {
    if (!this.rooms.add(Objects.requireNonNull(room))) {
      throw new RoomAlreadyExistsException();
    }
  }

  public IRoom getARoom(String roomId) {
    Objects.requireNonNull(roomId);
    return this.rooms
      .stream()
      .filter(room -> room.getRoomNumber().equals(roomId))
      .findFirst()
      .orElse(null);
  }

  public Reservation reserveARoom(Customer customer, IRoom room, Date checkInDate, Date checkOutDate)
    throws RoomAlreadyReservedException {
    final BiFunction<Date, Date, Boolean> isOverlappingDate = this.getOverlappingDatePredicate(
      Objects.requireNonNull(checkInDate),
      Objects.requireNonNull(checkOutDate)
    );

    final Predicate<Reservation> isReserved = reservation ->
      reservation.getRoom().equals(room) &&
        isOverlappingDate.apply(
          reservation.getCheckInDate(), reservation.getCheckOutDate()
        );

    if (this.reservations.stream().anyMatch(isReserved)) {
      throw new RoomAlreadyReservedException();
    }

    final Reservation reservation = new Reservation(customer, room, checkInDate, checkOutDate);
    this.reservations.add(reservation);
    return reservation;
  }

  public Collection<IRoom> findRooms(Date checkInDate, Date checkOutDate) {
    final BiFunction<Date, Date, Boolean> isOverlappingDate = this.getOverlappingDatePredicate(
      Objects.requireNonNull(checkInDate),
      Objects.requireNonNull(checkOutDate)
    );

    return this.rooms.stream()
      .filter(
        room -> this.reservations
          .stream()
          .filter(reservation -> reservation.getRoom().equals(room))
          .noneMatch(
            reservation ->
              isOverlappingDate.apply(reservation.getCheckInDate(), reservation.getCheckOutDate())
          )
      )
      .toList();
  }

  public Collection<IRoom> getAllRooms() {
    return Collections.unmodifiableCollection(this.rooms);
  }

  public Collection<Reservation> getCustomerReservations(Customer customer) {
    Objects.requireNonNull(customer);
    return this.reservations.stream().filter(
      reservation -> reservation.getCustomer().equals(customer)
    ).toList();
  }

  public void printAllReservations() {
    if (this.reservations.isEmpty()) {
      System.out.println("There are currently no reservations.\n");
      return;
    }

    System.out.println("Current reservations:");
    this.reservations.forEach(System.out::println);
    System.out.println();
  }

  private BiFunction<Date, Date, Boolean> getOverlappingDatePredicate(Date startDate, Date endDate) {
    Objects.requireNonNull(startDate);
    Objects.requireNonNull(endDate);
    return (Date lowerBound, Date upperBound) -> {
      Objects.requireNonNull(lowerBound);
      Objects.requireNonNull(upperBound);
      return (
        (startDate.compareTo(lowerBound) >= 0 && startDate.compareTo(upperBound) <= 0) ||
          (endDate.compareTo(lowerBound) >= 0 && endDate.compareTo(upperBound) <= 0) ||
          (lowerBound.compareTo(startDate) >= 0 && lowerBound.compareTo(endDate) <= 0) ||
          (upperBound.compareTo(startDate) >= 0 && upperBound.compareTo(endDate) <= 0)
      );
    };
  }
}
