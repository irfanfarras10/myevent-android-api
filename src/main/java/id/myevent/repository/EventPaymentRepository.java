package id.myevent.repository;

import id.myevent.model.dao.EventPaymentDao;
import org.springframework.data.repository.CrudRepository;

/**
 * Event Payment Repository.
 */
public interface EventPaymentRepository extends CrudRepository<EventPaymentDao, Long> {
}
