package id.myevent.repository;

import id.myevent.model.dao.EventDao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * User Repository.
 */
@Repository
public interface EventRepository extends CrudRepository<EventDao, Long> {

  @Query("SELECT event FROM EventDao event where event.eventStatus.id = :statusId and event.eventOrganizer.id = :organizerId order by event.id asc")
  List<EventDao> findByStatus(@Param("statusId") Long statusId,
                              @Param("organizerId") Long organizerId);

  @Query("SELECT event FROM EventDao event where event.bannerPhotoName = :imageName")
  Optional<EventDao> findByImageName(@Param("imageName") String imageName);

  @Query("SELECT event FROM EventDao event WHERE event.name LIKE %:name%")
  List<EventDao> findByName(@Param("name") String name);

  @Query("SELECT event FROM EventDao event where event.eventOrganizer.id = :organizerId order by event.id asc")
  List<EventDao> findAllByOrderByIdAsc(@Param("organizerId") Long organizerId);
}
