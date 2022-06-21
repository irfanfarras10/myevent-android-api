package id.myevent.repository;

import id.myevent.model.dao.TicketDao;
import org.springframework.data.repository.CrudRepository;

/** Ticket Repository. */
public interface TicketRepository extends CrudRepository<TicketDao, Long> {
}
