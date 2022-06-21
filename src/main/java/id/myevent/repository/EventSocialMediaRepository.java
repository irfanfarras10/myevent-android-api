package id.myevent.repository;

import id.myevent.model.dao.EventSocialMediaDao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** Event Social Media Repository. */
@Repository
public interface EventSocialMediaRepository extends CrudRepository<EventSocialMediaDao, Long> {
}
