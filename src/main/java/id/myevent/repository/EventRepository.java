package id.myevent.repository;

import id.myevent.model.dao.EventDao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** User Repository. */
@Repository
public interface EventRepository extends CrudRepository<EventDao, Long> {

    @Query("SELECT event FROM EventDao event where event.eventStatus.id = :statusId")
    public List<EventDao> findByStatus(@Param("statusId") Long statusId);

    @Query("SELECT event FROM EventDao event where event.bannerPhotoName = :imageName")
    public Optional<EventDao> findByImageName(@Param("imageName") String imageName);
}
