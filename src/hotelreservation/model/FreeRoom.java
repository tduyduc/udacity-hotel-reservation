package hotelreservation.model;

public class FreeRoom extends Room implements IRoom {
  public FreeRoom(String roomNumber, RoomType roomType) throws IllegalArgumentException {
    super(roomNumber, 0, roomType);
  }
}
