package tn.esprit.spring.services;



import java.util.List;
import java.util.Optional;

import tn.esprit.spring.entities.Mission;

public interface IMissionService {
    Mission add(Mission mission) throws Exception;
    Mission update(Mission mission) throws Exception;
    void delete(int missionId);
    Optional<Mission> getById(int missionId);
    List<Mission> getPaginated(final int page, final int size, final String name, final String departement);
    Iterable<Mission> getAll();
}
