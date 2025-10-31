package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Station;

import java.util.List;

public interface StationService {
    List<Station> getAllStations();
    Station getStationById(Integer id);
    Station saveStation(Station station);
    void deleteStation(Integer id);
}