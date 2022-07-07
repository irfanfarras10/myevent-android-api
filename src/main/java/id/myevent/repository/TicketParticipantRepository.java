package id.myevent.repository;

import id.myevent.model.dao.EventDao;
import id.myevent.model.dao.TicketParticipantDao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Ticket Participant Repository.
 */
public interface TicketParticipantRepository extends CrudRepository<TicketParticipantDao, Long> {

  @Query("SELECT tp.purchase_date, COUNT(tp.id) FROM TicketParticipantDao tp JOIN tp.participant p "
      + "ON tp.participant.id = p.id WHERE p.event.id = :eventId group by tp.purchase_date")
  List<TicketParticipantDao> purchaseByDate(Long eventId);

  @Query("SELECT tp FROM TicketParticipantDao tp WHERE tp.participant.id = :participantId")
  Optional<TicketParticipantDao> findByParticipantId(Long participantId);

  @Query("SELECT tp FROM TicketParticipantDao tp where tp.paymentPhotoName = :imageName")
  Optional<TicketParticipantDao> findByImageName(@Param("imageName") String imageName);

}
