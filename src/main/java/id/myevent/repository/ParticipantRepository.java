package id.myevent.repository;

import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.ParticipantDao;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Participant Repository.
 */
public interface ParticipantRepository extends CrudRepository<ParticipantDao, Long> {

  @Query("SELECT participant FROM ParticipantDao participant where participant.event.id= :eventId")
  public List<ParticipantDao> findByEvent(@Param("eventId") Long eventId);

  @Query(
      "SELECT participant FROM ParticipantDao participant where participant.event.id= :eventId "
          + "and participant.status = 'Menunggu Konfirmasi'")
  public List<ParticipantDao> findByStatusWait(@Param("eventId") Long eventId);

  @Query("SELECT participant FROM ParticipantDao participant where participant.event.id= :eventId "
      + "and participant.status = 'Terkonfirmasi'")
  public List<ParticipantDao> findByStatusConfirmed(@Param("eventId") Long eventId);

  @Query("SELECT participant FROM ParticipantDao participant where participant.event.id= :eventId "
      + "and participant.status = 'Hadir'")
  public List<ParticipantDao> findByStatusAttend(@Param("eventId") Long eventId);

  @Query("SELECT participant FROM ParticipantDao participant WHERE participant.name LIKE %:name%")
  List<ParticipantDao> findByName(@Param("name") String name);

  @Query("SELECT participant FROM ParticipantDao participant WHERE participant.email = :email "
      + "and participant.event.id = :eventId ")
  List<ParticipantDao> findByEmail(@Param("email") String email, @Param("eventId") Long eventId);

}