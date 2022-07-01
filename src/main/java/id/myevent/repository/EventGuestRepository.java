package id.myevent.repository;

import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.EventGuestDao;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/** Event Guest Repository. */
public interface EventGuestRepository extends CrudRepository<EventGuestDao, Long> {

  @Query("SELECT eventGuest FROM EventGuestDao eventGuest where eventGuest.event.id= :eventId")
  public List<EventGuestDao> findByEvent(@Param("eventId") Long eventId);
}
