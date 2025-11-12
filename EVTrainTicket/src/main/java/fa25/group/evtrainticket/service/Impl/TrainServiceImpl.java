package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Train;
import fa25.group.evtrainticket.repository.ScheduleRepository;
import fa25.group.evtrainticket.repository.TrainRepository;
import fa25.group.evtrainticket.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public Train createTrain(Train train) {
        if(trainRepository.existsByTrainNumber(train.getTrainNumber())){
            throw new IllegalArgumentException("Tàu: "+ train.getTrainName() + " đã tồn tại");
        }

        List<Schedule> trainSchedules = new ArrayList<>();
        if(!"BẢO TRÌ".equalsIgnoreCase(train.getStatus()) && train.getSchedules() != null && !train.getSchedules().isEmpty()) {
            checkSchedules(train.getSchedules(), null);
            for (Schedule s : train.getSchedules()) {
                Schedule dbSchedule = scheduleRepository.findById(s.getScheduleID())
                        .orElseThrow(() -> new IllegalArgumentException("Lịch trình ID " + s.getScheduleID() + " không tồn tại"));
                dbSchedule.setTrain(train); // gán ngược lại quan hệ
                trainSchedules.add(dbSchedule);
            }
        }
        train.setSchedules(trainSchedules);
        return trainRepository.save(train);
    }

    @Override
    public Train getTrainById(Integer id) {
        return trainRepository.findById(id).orElseThrow(() -> new RuntimeException("Tàu với ID: " + id + " không tồn tại"));
    }

    @Override
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @Override
    public Train updateTrain(Integer id, Train trainDetails) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new RuntimeException("Train not found"));

        if (trainDetails.getSchedules() != null && !trainDetails.getSchedules().isEmpty()) {
            List<Schedule> validSchedules = trainDetails.getSchedules().stream()
                    .filter(s -> s.getScheduleID() != null)
                    .collect(Collectors.toList());
            checkSchedules(validSchedules, id);
        }

        // Ngắt quan hệ các schedule cũ
        if (train.getSchedules() != null && !train.getSchedules().isEmpty()) {
            for (Schedule oldSchedule : train.getSchedules()) {
                oldSchedule.setTrain(null);
            }
            train.getSchedules().clear();
        }

        // Update thông tin
        train.setTrainNumber(trainDetails.getTrainNumber());
        train.setTrainName(trainDetails.getTrainName());
        train.setCapacity(trainDetails.getCapacity());
        train.setStatus(trainDetails.getStatus());
        train.setNotes(trainDetails.getNotes());

        // Gán schedule mới
        if (!"BẢO TRÌ".equalsIgnoreCase(train.getStatus()) && trainDetails.getSchedules() != null && !trainDetails.getSchedules().isEmpty()) {
            List<Schedule> newSchedules = trainDetails.getSchedules().stream()
                    .filter(s -> s.getScheduleID() != null)
                    .map(s -> scheduleRepository.findById(s.getScheduleID())
                            .orElseThrow(() -> new IllegalArgumentException("Lịch trình ID " + s.getScheduleID() + " không tồn tại")))
                    .toList();

            // Set quan hệ 2 chiều
            for (Schedule schedule : newSchedules) {
                schedule.setTrain(train);
                train.getSchedules().add(schedule);
            }
        }

        return trainRepository.save(train);
    }

    @Override
    public void deleteTrain(Integer id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found"));
        train.getSchedules().forEach(schedule -> schedule.setTrain(null));
        trainRepository.delete(train);
    }

    @Override
    public Train findByTrainName(String trainName) {
        return trainRepository.findByTrainName(trainName);
    }

    /**
     * Validate danh sách lịch trình
     * - Ga đến của lịch trình trước = Ga khởi hành của lịch trình sau
     * - Thời gian khởi hành của lịch trình sau > Thời gian đến của lịch trình trước
     * - Lịch trình không được sử dụng bởi tàu khác
     */

    @Override
    public void checkSchedules(List<Schedule> schedules, Integer trainId) {
        if (schedules.size() > 3){
            throw new IllegalArgumentException("Một tàu chỉ được có tối đa 3 lịch trình");
        }

        List<Schedule> sortedSchedules = new ArrayList<>(schedules);

        for(int i = 0; i < sortedSchedules.size(); i++) {
            Schedule currentSchedule = sortedSchedules.get(i);

            // Kiểm tra lịch trình có tồn tại không
            Schedule dbSchedule = scheduleRepository.findById(currentSchedule.getScheduleID())
                    .orElseThrow(() -> new IllegalArgumentException("Lịch trình ID " + currentSchedule.getScheduleID() + " không tồn tại"));

            // Kiểm tra lịch trình đã được sử dụng bởi tàu khác chưa
            List<Train> trainsUsingSchedule = trainRepository.findBySchedules_ScheduleID(currentSchedule.getScheduleID());
            for(Train train : trainsUsingSchedule) {
                if(!train.getTrainID().equals(trainId)) {
                    throw new IllegalArgumentException(
                            "Lịch trình '" + dbSchedule.getDepartureStation().getName() + " → " +
                                    dbSchedule.getArrivalStation().getName() + "' đã được sử dụng bởi tàu " + train.getTrainName()
                    );
                }
            }

            // Validate với lịch trình tiếp theo
            if(i < sortedSchedules.size() - 1) {
                Schedule nextSchedule = sortedSchedules.get(i + 1);
                Schedule dbNextSchedule = scheduleRepository.findById(nextSchedule.getScheduleID())
                        .orElseThrow(() -> new IllegalArgumentException("Lịch trình ID " + nextSchedule.getScheduleID() + " không tồn tại"));

                // Kiểm tra ga đến của lịch trình hiện tại = ga khởi hành của lịch trình tiếp theo
                if(!dbSchedule.getArrivalStation().getStationID().equals(dbNextSchedule.getDepartureStation().getStationID())) {
                    throw new IllegalArgumentException(
                            "Ga đến của lịch trình " + (i+1) + " (" + dbSchedule.getArrivalStation().getName() +
                                    ") phải trùng với ga khởi hành của lịch trình " + (i+2) + " (" +
                                    dbNextSchedule.getDepartureStation().getName() + ")"
                    );
                }

                // Kiểm tra thời gian: giờ khởi hành lịch trình sau > giờ đến lịch trình trước
                LocalDateTime currentArrival = dbSchedule.getArrivalTime();
                LocalDateTime nextDeparture = dbNextSchedule.getDepartureTime();

                if(!nextDeparture.isAfter(currentArrival)) {
                    throw new IllegalArgumentException(
                            "Thời gian khởi hành của lịch trình " + (i+2) + " (" +
                                    nextDeparture.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                                    ") phải sau thời gian đến của lịch trình " + (i+1) + " (" +
                                    currentArrival.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ")"
                    );
                }
            }
        }
    }

}
