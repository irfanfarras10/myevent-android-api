package id.myevent.repository;

import id.myevent.model.dao.EventOrganizerDao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** User Repository. */
@Repository
public interface EventOrganizerRepository extends CrudRepository<EventOrganizerDao, Long> {
  EventOrganizerDao findByUsername(String username);
}
