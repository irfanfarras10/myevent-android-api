package id.myevent.repository;

import id.myevent.model.dao.TicketParticipantDao;
import org.springframework.data.repository.CrudRepository;

public interface TicketParticipantRepository extends CrudRepository<TicketParticipantDao, Long> {
}
