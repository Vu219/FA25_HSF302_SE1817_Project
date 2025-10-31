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
    public Station getStationById(Integer id) {
        return stationRepository.findById(id).orElse(null);
    }

    @Override
    public Station saveStation(Station station) {
        return stationRepository.save(station);
    }

    @Override
    public void deleteStation(Integer id) {
        stationRepository.deleteById(id);
    }
}