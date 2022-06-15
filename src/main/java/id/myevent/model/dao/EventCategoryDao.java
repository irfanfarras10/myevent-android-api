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

/** Category DAO. */
@Entity
@Table(name = "event_category")
@Data
public class EventCategoryDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  
  @Column(unique = true, name = "name", nullable = false)
  private String name;
  
  @OneToMany(mappedBy = "eventCategory", cascade = CascadeType.ALL)
  private Set<EventDao> events;
}
