package tn.esprit.spring.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tn.esprit.spring.entities.Mission;

@Repository
public interface MissionRepository extends CrudRepository<Mission, Integer> {

	List<Mission> findAllByNameLikeAndDepartementNameOrderByName(String name, String departement, PageRequest of);

}
