package id.myevent.repository;

import id.myevent.model.dao.EventStatusDao;
import org.springframework.data.repository.CrudRepository;

/** Event Status Repository. */
public interface EventStatusRepository extends CrudRepository<EventStatusDao, Long> {
}
