package id.myevent.repository;

import id.myevent.model.dao.EventCategoryDao;
import org.springframework.data.repository.CrudRepository;
/** Event Category Repository. */

public interface EventCategoryRepository extends CrudRepository<EventCategoryDao, Long> {}
