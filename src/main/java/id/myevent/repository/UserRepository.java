package id.myevent.repository;

import id.myevent.model.dao.UserDao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** User Repository. */
@Repository
public interface UserRepository extends CrudRepository<UserDao, Long> {
  UserDao findByUsername(String username);
}
