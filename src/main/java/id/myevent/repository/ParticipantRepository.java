package id.myevent.repository;

import id.myevent.model.dao.ParticipantDao;
import org.springframework.data.repository.CrudRepository;

public interface ParticipantRepository extends CrudRepository<ParticipantDao, Long> {
}