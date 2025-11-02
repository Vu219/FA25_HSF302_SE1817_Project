package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Station;

import java.util.List;

public interface StationService {
    List<Station> getAllStations();
    Station getStationsByID(Integer stationID);
    Station saveStation(Station station);
    Station updateStation(Integer stationID, Station newStationData);
    void deleteStation(Integer stationID);
}