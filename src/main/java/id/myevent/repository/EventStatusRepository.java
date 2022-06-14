package id.myevent.repository;

import id.myevent.model.dao.EventStatusDao;
import org.springframework.data.repository.CrudRepository;

public interface EventStatusRepository extends CrudRepository<EventStatusDao, Long> {
}
