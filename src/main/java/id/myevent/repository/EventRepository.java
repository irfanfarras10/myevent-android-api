package id.myevent.repository;

import id.myevent.model.dao.EventDao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** User Repository. */
@Repository
public interface EventRepository extends CrudRepository<EventDao, Long> {

}
