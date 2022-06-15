package id.myevent.repository;

import id.myevent.model.dao.EventVenueCategoryDao;
import org.springframework.data.repository.CrudRepository;

/** Event Venue Category Repository. */
public interface EventVenueCategoryRepository extends CrudRepository<EventVenueCategoryDao, Long> {}
