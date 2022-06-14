package id.myevent.repository;

import org.springframework.data.repository.CrudRepository;

import id.myevent.model.dao.EventCategoryDao;

public interface EventCategoryRepository extends CrudRepository<EventCategoryDao, Long>{
  
}

