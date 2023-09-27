package hotelreservation.menu;

import hotelreservation.api.AdminResource;
import hotelreservation.api.HotelResource;
import hotelreservation.exception.CustomerAlreadyExistsException;
import hotelreservation.exception.CustomerNotFoundException;
import hotelreservation.exception.RoomAlreadyReservedException;
import hotelreservation.model.Customer;
import hotelreservation.model.IRoom;
import hotelreservation.model.Room;
import hotelreservation.model.RoomType;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class AdminMenu {
  private static final int MENU_CHOICE_SEE_ALL_CUSTOMERS = 1;
  private static final int MENU_CHOICE_SEE_ALL_ROOMS = 2;
  private static final int MENU_CHOICE_SEE_ALL_RESERVATIONS = 3;
  private static final int MENU_CHOICE_ADD_ROOM = 4;
  private static final int MENU_CHOICE_POPULATE_TEST_DATA = 5;
  private static final int MENU_CHOICE_TO_MAIN_MENU = 6;

  private static final Pattern ROOM_NUMBER_PATTERN = Pattern.compile("^[0-9]{3}$");

  private final AdminResource adminResource = AdminResource.getInstance();
  private final HotelResource hotelResource = HotelResource.getInstance();

  private final Scanner scanner;

  public AdminMenu(Scanner scanner) {
    this.scanner = Objects.requireNonNull(scanner);
  }

  public void displayMenu() {
    while (true) {
      System.out.println("Please select an option:");
      System.out.println("1. See all customers");
      System.out.println("2. See all rooms");
      System.out.println("3. See all reservations");
      System.out.println("4. Add a room");
      System.out.println("5. Populate test data");
      System.out.println("6. Back to main menu");

      final int choice;
      try {
        choice = this.scanner.nextInt();
        // Ignore everything until next line!
        this.scanner.nextLine();
      } catch (InputMismatchException ime) {
        this.warnInvalidInput();
        this.scanner.nextLine();
        continue;
      } catch (NoSuchElementException | IllegalStateException exception) {
        if (exception instanceof IllegalStateException) {
          this.warnClosedScanner();
        }
        return;
      }

      switch (choice) {
        case AdminMenu.MENU_CHOICE_SEE_ALL_CUSTOMERS -> {
          this.viewAllCustomers();
          this.notifyBackToAdminMenu();
        }

        case AdminMenu.MENU_CHOICE_SEE_ALL_ROOMS -> {
          this.viewAllRooms();
          this.notifyBackToAdminMenu();
        }

        case AdminMenu.MENU_CHOICE_SEE_ALL_RESERVATIONS -> {
          this.viewAllReservations();
          this.notifyBackToAdminMenu();
        }

        case AdminMenu.MENU_CHOICE_ADD_ROOM -> {
          this.addRoom();
          this.notifyBackToAdminMenu();
        }

        case AdminMenu.MENU_CHOICE_POPULATE_TEST_DATA -> {
          this.populateTestData();
          this.notifyBackToAdminMenu();
        }

        case AdminMenu.MENU_CHOICE_TO_MAIN_MENU -> {
          return;
        }

        default -> this.warnInvalidInput();
      }
    }
  }

  private void warnInvalidInput() {
    System.out.println("You haven't selected a valid numeric option. Please try again!\n");
  }

  private void warnClosedScanner() {
    System.out.println("Input has already been closed...");
  }

  private void notifyBackToAdminMenu() {
    System.out.println("Taking you back to admin menu...\n");
  }

  private <T> void viewData(
    Supplier<Collection<T>> dataSupplier,
    String emptyCollectionMessage,
    String nonEmptyCollectionMessage
  ) {
    final Collection<T> collection = dataSupplier.get();
    if (collection.isEmpty()) {
      System.out.println(emptyCollectionMessage);
      System.out.println();
      return;
    }

    System.out.println(nonEmptyCollectionMessage);
    collection.forEach(System.out::println);
    System.out.println();
  }

  private void viewAllCustomers() {
    this.viewData(
      this.adminResource::getAllCustomers,
      "There are currently no registered customers!",
      "Here are the customers registered in this system:"
    );
  }

  private void viewAllRooms() {
    this.viewData(
      this.adminResource::getAllRooms,
      "There are currently no rooms!",
      "Here are the rooms added in this system:"
    );
  }

  private void viewAllReservations() {
    this.adminResource.displayAllReservations();
  }

  /**
   * @return Scanned line of string, or null if "quit" was inputted or the scanner was closed.
   */
  private String getNextLineOrQuit() {
    final String scannedLine;
    try {
      scannedLine = this.scanner.nextLine();
    } catch (NoSuchElementException | IllegalStateException exception) {
      this.warnClosedScanner();
      return null;
    }

    if (MainMenu.ESCAPE_WORD.equals(scannedLine)) {
      return null;
    }
    return scannedLine;
  }

  private void addRoom() {
    while (true) {
      System.out.println(
        "At any time during the data entry process, you can enter \"" +
          MainMenu.ESCAPE_WORD +
          "\" to quit."
      );

      System.out.println("Enter room number (must be 3 digits):");
      final String roomNumber;
      while (true) {
        final String scannedRoomNumber = this.getNextLineOrQuit();
        if (null == scannedRoomNumber) {
          return;
        }
        if (!AdminMenu.ROOM_NUMBER_PATTERN.matcher(scannedRoomNumber).matches()) {
          System.out.println("The room number must consists of exactly 3 digits. Please try again.");
          continue;
        }

        roomNumber = scannedRoomNumber;
        break;
      }

      System.out.println("Enter price (non-negative decimal, 0 for free room):");
      final double price;
      while (true) {
        final String scannedPrice = this.getNextLineOrQuit();
        if (null == scannedPrice) {
          return;
        }

        final double parsedPrice;
        try {
          parsedPrice = Double.parseDouble(scannedPrice);
        } catch (NumberFormatException nfe) {
          System.out.println("Your input didn't seem to be a decimal number. Please try again.");
          continue;
        }

        price = parsedPrice;
        break;
      }

      System.out.println("Enter room type (single or double):");
      final RoomType roomType;
      while (true) {
        final String scannedRoomType = this.getNextLineOrQuit();
        if (null == scannedRoomType) {
          return;
        }
        if (RoomType.SINGLE.toString().equalsIgnoreCase(scannedRoomType)) {
          roomType = RoomType.SINGLE;
          break;
        }
        if (RoomType.DOUBLE.toString().equalsIgnoreCase(scannedRoomType)) {
          roomType = RoomType.DOUBLE;
          break;
        }

        System.out.println("Your input wasn't recognized. Please try again.");
      }

      final IRoom room;
      try {
        room = new Room(roomNumber, price, roomType);
      } catch (IllegalArgumentException iae) {
        System.out.println("Your inputted price doesn't seem to be non-negative. Please try again.");
        continue;
      }

      this.adminResource.addRooms(List.of(room));
      break;
    }
  }

  private void populateTestData() {
    final Random random = new Random();

    // Thanks to Mockaroo for these mock data!
    final String mockCustomersCsv = """
first_name,last_name,email
Amberly,Atmore,aatmore0@gizmodo.com
Willa,Kyllford,wkyllford1@merriam-webster.com
Zechariah,O'Lunney,zolunney2@unc.edu
Pace,Buttwell,pbuttwell3@tiny.cc
Loralee,Inett,linett4@yolasite.com
Emiline,Boteman,eboteman0@netvibes.com
Aristotle,Helder,ahelder1@businessinsider.com
Lyle,Worge,lworge2@histats.com
Costanza,Cunniam,ccunniam3@pcworld.com
Ardene,Loft,aloft4@desdev.cn
Joann,Strotone,jstrotone5@wp.com
Gabbie,Dannell,gdannell6@wix.com
Vyky,Reye,vreye7@tinyurl.com
Stacee,Mutter,smutter8@auda.org.au
Karil,Rumgay,krumgay9@blog.com""";

    final Customer[] customers = Arrays
      .<String>stream(mockCustomersCsv.split("\n"))
      .skip(1) // Skip header line
      .<Customer>map(
        line -> {
          final String[] splitLine = line.split(",");
          return new Customer(splitLine[0], splitLine[1], splitLine[2]);
        }
      )
      .toArray(Customer[]::new);
    for (final Customer customer : customers) {
      try {
        this.hotelResource.createACustomer(customer.getEmail(), customer.getFirstName(), customer.getLastName());
      } catch (CustomerAlreadyExistsException caee) {
        System.out.println("Customer " + customer + " already exists. Skipping.");
        continue;
      }
      System.out.println(customer.toString() + " has been added!");
    }

    final int FLOORS = 5;
    final int ROOMS_PER_FLOOR = 12;
    IRoom[] rooms = IntStream.rangeClosed(1, FLOORS).boxed()
      .<IRoom>flatMap(
        floor -> IntStream.rangeClosed(1, ROOMS_PER_FLOOR).boxed()
          .map(
            roomIndex -> new Room(
              String.format("%d%02d", floor, roomIndex), // e.g. 101, 102
              random.nextBoolean() ? 0 : ((double) Math.round(random.nextDouble(0, 25600))) / 100,
              random.nextBoolean() ? RoomType.SINGLE : RoomType.DOUBLE
            )
          )
      )
      .toArray(IRoom[]::new);
    this.adminResource.addRooms(Arrays.stream(rooms).toList());

    final Supplier<Customer> randomCustomerSupplier = () -> customers[random.nextInt(customers.length)];
    final Supplier<IRoom> randomRoomSupplier = () -> rooms[random.nextInt(rooms.length)];
    final Supplier<Date> randomDateSupplier = () -> Date.from(
      new Date().toInstant().plus(random.nextInt(-20, 20), ChronoUnit.DAYS)
    );

    // Map.Entry is an emulation of a pair!
    // Key: check-in date; Value: check-out date
    final Supplier<Map.Entry<Date, Date>> randomDateRangeSupplier = () -> {
      final Date date1 = randomDateSupplier.get();
      final Date date2 = randomDateSupplier.get();
      return date1.before(date2) ? Map.entry(date1, date2) : Map.entry(date2, date1);
    };

    final byte RESERVATIONS_TO_GENERATE = 32;
    for (byte reservation = 0; reservation < RESERVATIONS_TO_GENERATE; ) {
      final Map.Entry<Date, Date> dateRange = randomDateRangeSupplier.get();
      try {
        System.out.println(
          this.hotelResource.bookARoom(
            randomCustomerSupplier.get().getEmail(),
            randomRoomSupplier.get(),
            dateRange.getKey(),
            dateRange.getValue()
          ).toString() + " has been booked!"
        );
      } catch (RoomAlreadyReservedException | CustomerNotFoundException rare) {
        continue; // Silently skip!
      }
      reservation += 1;
    }

    System.out.println("Test data have been populated successfully!");
  }
}
