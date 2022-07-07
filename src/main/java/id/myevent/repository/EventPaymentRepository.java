package id.myevent.repository;

import id.myevent.model.dao.EventPaymentDao;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Event Payment Repository.
 */
public interface EventPaymentRepository extends CrudRepository<EventPaymentDao, Long> {

  @Query("SELECT eventPayment FROM EventPaymentDao eventPayment where eventPayment.event.id = :eventId")
  List<EventPaymentDao> findByEvent(@Param("eventId") Long eventId);
}
