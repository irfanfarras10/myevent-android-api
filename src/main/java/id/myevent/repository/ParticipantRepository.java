package id.myevent.repository;

import id.myevent.model.dao.ParticipantDao;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository extends CrudRepository<ParticipantDao, Long> {

  @Query("SELECT participant FROM ParticipantDao participant where participant.event.id= :eventId")
  public List<ParticipantDao> findByEvent(@Param("eventId") Long eventId);

}