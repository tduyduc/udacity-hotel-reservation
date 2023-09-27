package hotelreservation.menu;

import hotelreservation.api.HotelResource;
import hotelreservation.exception.CustomerAlreadyExistsException;
import hotelreservation.exception.CustomerNotFoundException;
import hotelreservation.exception.RoomAlreadyReservedException;
import hotelreservation.model.Customer;
import hotelreservation.model.IRoom;
import hotelreservation.model.Reservation;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MainMenu {
  private static final SimpleDateFormat ISO_8601_SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static final int MENU_CHOICE_FIND_RESERVE_ROOM = 1;
  private static final int MENU_CHOICE_VIEW_MY_RESERVATIONS = 2;
  private static final int MENU_CHOICE_CREATE_ACCOUNT = 3;
  private static final int MENU_CHOICE_TO_ADMIN_MENU = 4;
  private static final int MENU_CHOICE_EXIT = 5;

  static final String ESCAPE_WORD = "quit"; // Visible within package
  public static final int NEXT_DAYS_FOR_ROOM_RECOMMENDATIONS = 7;

  private final HotelResource hotelResource = HotelResource.getInstance();

  private final Scanner scanner;

  public MainMenu(Scanner scanner) {
    this.scanner = Objects.requireNonNull(scanner);
  }

  public void displayMenu() {
    while (true) {
      System.out.println("Please select an option:");
      System.out.println("1. Find and reserve a room");
      System.out.println("2. View my reservations");
      System.out.println("3. Create an account");
      System.out.println("4. Admin");
      System.out.println("5. Exit");

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
        this.warnClosedScanner();
        this.exit();
        return;
      }

      switch (choice) {
        case MainMenu.MENU_CHOICE_FIND_RESERVE_ROOM -> {
          this.findReserveRoom();
          this.notifyBackToMainMenu();
        }

        case MainMenu.MENU_CHOICE_VIEW_MY_RESERVATIONS -> {
          this.viewCustomerReservations();
          this.notifyBackToMainMenu();
        }

        case MainMenu.MENU_CHOICE_CREATE_ACCOUNT -> {
          this.createAccount();
          this.notifyBackToMainMenu();
        }

        case MainMenu.MENU_CHOICE_TO_ADMIN_MENU -> {
          this.toAdminMenu();
          this.notifyBackToMainMenu();
        }

        case MainMenu.MENU_CHOICE_EXIT -> {
          this.exit();
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

  private void exit() {
    System.out.println("Goodbye!");
  }

  private void notifyBackToMainMenu() {
    System.out.println("Taking you back to main menu...\n");
  }

  private void toAdminMenu() {
    new AdminMenu(this.scanner).displayMenu();
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

  /**
   * @return Scanned line of string, or null the scanner was closed.
   */
  private String getNextLine() {
    try {
      return this.scanner.nextLine();
    } catch (NoSuchElementException | IllegalStateException exception) {
      this.warnClosedScanner();
      return null;
    }
  }

  /**
   * @return A Date object if date input was correct, or null if "quit" was inputted or the scanner was closed.
   */
  private Date getDateFromScanner() {
    while (true) {
      final String scannedDate = this.getNextLineOrQuit();
      if (null == scannedDate) {
        return null;
      }

      final Date parsedDate = MainMenu.ISO_8601_SIMPLE_DATE_FORMAT.parse(
        scannedDate, new ParsePosition(0)
      );
      if (null == parsedDate) {
        System.out.println(
          "Invalid date format. Please enter a valid date as YYYY-MM-DD or \"" +
            MainMenu.ESCAPE_WORD +
            "\" to quit."
        );
        continue;
      }
      return parsedDate;
    }
  }

  private void findReserveRoom() {
    System.out.println("Enter your email address, or \"" +
      MainMenu.ESCAPE_WORD +
      "\" to quit."
    );
    final String email = this.getNextLineOrQuit();
    if (null == email) {
      return;
    }

    final Customer customer = this.hotelResource.getCustomer(email);
    if (null == customer) {
      System.out.println(
        "Sorry, your email address is not found in the system. " +
          "Please create an account from the main menu first, and then try reserving a room again."
      );
      return;
    }

    while (true) {
      System.out.println("Enter check-in date (YYYY-MM-DD):");
      Date checkInDate;
      while (true) {
        checkInDate = this.getDateFromScanner();
        if (null == checkInDate) {
          return;
        }
        if (checkInDate.before(new Date())) {
          System.out.println("Check-in date must be same or after today! Please try again.");
          continue;
        }
        break;
      }

      System.out.println("Enter check-out date (YYYY-MM-DD):");
      Date checkOutDate;
      while (true) {
        checkOutDate = this.getDateFromScanner();
        if (null == checkOutDate) {
          return;
        }
        if (checkInDate.after(checkOutDate)) {
          System.out.println("Check-out date must be same or after check-in date! Please try again.");
          continue;
        }
        break;
      }

      Collection<IRoom> foundRooms = this.hotelResource.findRooms(checkInDate, checkOutDate);
      if (foundRooms.isEmpty()) {
        System.out.println("There are currently no rooms available for your date range. :(");

        final Date newCheckInDate = this.addDaysForRecommendations(checkInDate);
        final Date newCheckOutDate = this.addDaysForRecommendations(checkOutDate);

        foundRooms = this.hotelResource.findRooms(newCheckInDate, newCheckOutDate);
        if (foundRooms.isEmpty()) {
          System.out.println("Please try another date range instead.");
          continue; // outer loop
        }

        System.out.println(
          "Here are some available rooms for you if you choose to reserve from " +
            MainMenu.ISO_8601_SIMPLE_DATE_FORMAT.format(newCheckInDate) +
            " to " +
            MainMenu.ISO_8601_SIMPLE_DATE_FORMAT.format(newCheckOutDate) +
            ":"
        );
        checkInDate = newCheckInDate;
        checkOutDate = newCheckOutDate;
      } else {
        System.out.println(
          "Here are some available rooms for your reservation from " +
            MainMenu.ISO_8601_SIMPLE_DATE_FORMAT.format(checkInDate) +
            " to " +
            MainMenu.ISO_8601_SIMPLE_DATE_FORMAT.format(checkOutDate) +
            ":"
        );
      }

      foundRooms.forEach(System.out::println);
      System.out.println();
      System.out.println("Please enter the room number you want to reserve or \"" +
        MainMenu.ESCAPE_WORD +
        "\" to quit."
      );

      while (true) {
        final String roomNumber = this.getNextLineOrQuit();
        if (null == roomNumber) {
          return;
        }

        final Optional<IRoom> selectedRoom = foundRooms
          .stream()
          .filter(room -> room.getRoomNumber().equals(roomNumber))
          .findFirst();

        if (selectedRoom.isEmpty()) {
          System.out.println("Your input doesn't match any rooms. Please try again, or enter \"" +
            MainMenu.ESCAPE_WORD +
            "\" to quit."
          );
          continue; // inner loop
        }

        try {
          this.hotelResource.bookARoom(customer.getEmail(), selectedRoom.get(), checkInDate, checkOutDate);
        } catch (RoomAlreadyReservedException rare) {
          System.out.println("Sorry, room " + roomNumber + " has already been reserved. Please choose another room.");
          continue;
        } catch (CustomerNotFoundException cnfe) {
          System.out.println(
            "Sorry, your email address is not found in the system. Perhaps it has been deleted. " +
              "Please create an account from the main menu first, and then try reserving a room again."
          );
          return;
        }

        System.out.println("Room " + roomNumber + " has been reserved successfully. Thank you!");
        return;
      }
    }
  }

  private Date addDaysForRecommendations(Date date) {
    return Date.from(
      Objects.requireNonNull(date).toInstant().plus(MainMenu.NEXT_DAYS_FOR_ROOM_RECOMMENDATIONS, ChronoUnit.DAYS)
    );
  }

  private void viewCustomerReservations() {
    System.out.println(
      "Enter the email address you've registered with our system (or enter \"" +
        MainMenu.ESCAPE_WORD +
        "\" to quit):"
    );
    final String email = this.getNextLineOrQuit();
    if (null == email) {
      return;
    }

    final Collection<Reservation> reservations;
    try {
      reservations = this.hotelResource.getCustomerReservations(email);
    } catch (CustomerNotFoundException cnfe) {
      System.out.println("You haven't registered this email address. Please create an account.");
      return;
    }

    if (reservations.isEmpty()) {
      System.out.println("You haven't booked any rooms.");
      return;
    }

    System.out.println("Here are the rooms you've booked:");
    reservations.forEach(System.out::println);
  }

  private void createAccount() {
    System.out.println("Enter first name:");
    final String firstName;
    while (true) {
      final String scannedName = this.getNextLine();
      if (null == scannedName) {
        return;
      }
      if (scannedName.isEmpty()) {
        System.out.println("First name is required.");
        continue;
      }
      firstName = scannedName;
      break;
    }

    System.out.println("Enter last name:");
    final String lastName;
    while (true) {
      final String scannedName = this.getNextLine();
      if (null == scannedName) {
        return;
      }
      if (scannedName.isEmpty()) {
        System.out.println("Last name is required.");
        continue;
      }
      lastName = scannedName;
      break;
    }

    System.out.println("Enter email address (or enter \"" + MainMenu.ESCAPE_WORD + "\" to quit):");
    while (true) {
      final String email = this.getNextLineOrQuit();
      if (null == email) {
        return;
      }

      try {
        this.hotelResource.createACustomer(email, firstName, lastName);
      } catch (IllegalArgumentException iae) {
        System.out.println("The email address you've entered is not in a correct format. Please try again.");
        continue;
      } catch (CustomerAlreadyExistsException caee) {
        System.out.println(
          "Another customer with this email address already exists! Please use another email address."
        );
        continue;
      }

      System.out.println("Your registration has completed successfully. Thank you for registering!");
      break;
    }
  }
}
