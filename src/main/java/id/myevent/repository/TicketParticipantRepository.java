package id.myevent.repository;

import id.myevent.model.dao.TicketParticipantDao;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TicketParticipantRepository extends CrudRepository<TicketParticipantDao, Long> {

  @Query("SELECT tp.purchase_date, COUNT(tp.id) FROM TicketParticipantDao tp JOIN tp.participant p "
      + "ON tp.participant.id = p.id WHERE p.event.id = :eventId group by tp.purchase_date")
  List<TicketParticipantDao> purchaseByDate(Long eventId);

}
