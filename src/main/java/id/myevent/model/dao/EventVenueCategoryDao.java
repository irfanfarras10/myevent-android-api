package id.myevent.model.dao;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;

/** Event Venue Category DAO. */
@Entity
@Table(name = "event_venue_category")
@Data
public class EventVenueCategoryDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name", nullable = false)
  private String name;

  @OneToMany(mappedBy = "eventVenueCategory", cascade = CascadeType.ALL)
  private Set<EventDao> events;
}
