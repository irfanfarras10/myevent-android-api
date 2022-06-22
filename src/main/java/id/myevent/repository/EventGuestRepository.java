package id.myevent.repository;

import id.myevent.model.dao.EventGuestDao;
import org.springframework.data.repository.CrudRepository;

/** Event Guest Repository. */
public interface EventGuestRepository extends CrudRepository<EventGuestDao, Long> {
}
