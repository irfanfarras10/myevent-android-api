package id.myevent.repository;

import id.myevent.model.dao.EventPaymentDao;
import id.myevent.model.dao.TicketDao;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/** Ticket Repository. */
public interface TicketRepository extends CrudRepository<TicketDao, Long> {

  @Query("SELECT ticket FROM TicketDao ticket where ticket.event.id = :eventId")
  List<TicketDao> findByEvent(@Param("eventId") Long eventId);

}
