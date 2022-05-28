package id.myevent.repository;

import id.myevent.model.DAO.UserDAO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserDAO, Long> {
    UserDAO findByUsername(String username);
}
