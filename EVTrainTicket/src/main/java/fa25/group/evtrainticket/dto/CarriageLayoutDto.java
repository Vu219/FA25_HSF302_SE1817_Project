package fa25.group.evtrainticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarriageLayoutDto {
    private Integer carriageId;
    private String carriageNumber;
    private String carriageTypeName;
    private Integer totalSeats;
    private List<seatDto> seats; // (Danh sách phẳng, được SeatService sử dụng)

    // --- PHẦN BỊ THIẾU ---
    // Đây là Map mà Controller sẽ TẠO RA và HTML sẽ SỬ DỤNG
    private Map<Integer, List<seatDto>> seatsByRow;
    private Integer maxColumns;
    // --- KẾT THÚC PHẦN BỊ THIẾU ---

    // Constructor for backward compatibility with existing service
    public CarriageLayoutDto(Integer carriageId, String carriageNumber, String carriageTypeName, Integer totalSeats, List<seatDto> seats) {
        this.carriageId = carriageId;
        this.carriageNumber = carriageNumber;
        this.carriageTypeName = carriageTypeName;
        this.totalSeats = totalSeats;
        this.seats = seats;
        // this.seatsByRow sẽ được set trong Controller
    }


    public Map<Integer, List<seatDto>> getSeatsByRow() {
        return this.seatsByRow;
    }


    public void setSeatsByRow(Map<Integer, List<seatDto>> seatsByRow) {
        this.seatsByRow = seatsByRow;
    }
}