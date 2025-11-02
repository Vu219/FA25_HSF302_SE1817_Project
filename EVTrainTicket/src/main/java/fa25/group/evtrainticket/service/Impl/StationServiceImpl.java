package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.entity.Station;
import fa25.group.evtrainticket.repository.StationRepository;
import fa25.group.evtrainticket.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {
    private final StationRepository stationRepository;

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public Station getStationsByID(Integer stationID) {
        return stationRepository.findById(stationID).orElseThrow(()-> new IllegalArgumentException("Không tìm thấy ga với mã: " + stationID));
    }

    @Override
    public Station saveStation(Station station) {
        if(stationRepository.existsByCodeAndName(station.getCode(), station.getName())){
            throw new IllegalArgumentException("Ga tàu " + station.getName() + " đã tồn tại");
        }
        return stationRepository.save(station);
    }

    @Override
    public Station updateStation(Integer stationID, Station newStationData) {
        Station stationToUpdate = getStationsByID(stationID);
        stationToUpdate.setName(newStationData.getName());
        stationToUpdate.setCode(newStationData.getCode());
        stationToUpdate.setAddress(newStationData.getAddress());
        stationToUpdate.setCity(newStationData.getCity());
        stationToUpdate.setProvince(newStationData.getProvince());
        stationToUpdate.setStatus(newStationData.getStatus());
        return stationRepository.save(stationToUpdate);
    }

    @Override
    public void deleteStation(Integer stationID) {
        Station stationToDelete = getStationsByID(stationID);
        stationRepository.delete(stationToDelete);
    }
}
