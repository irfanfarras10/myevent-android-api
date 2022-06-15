package id.myevent.repository;

import id.myevent.model.dao.EventPaymentCategoryDao;
import org.springframework.data.repository.CrudRepository;

/** Event Category Repository. */
public interface EventPaymentCategoryRepository
    extends CrudRepository<EventPaymentCategoryDao, Long> {}
