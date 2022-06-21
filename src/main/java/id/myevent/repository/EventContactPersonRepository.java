package id.myevent.repository;

import id.myevent.model.dao.EventContactPersonDao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** Event Contact Person Repository. */
@Repository
public interface EventContactPersonRepository extends CrudRepository<EventContactPersonDao, Long> {
}
