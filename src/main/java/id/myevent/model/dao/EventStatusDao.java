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
import lombok.EqualsAndHashCode;

/** Event Status DAO. */
@Entity
@Table(name = "event_status")
@EqualsAndHashCode(exclude = "events")
public class EventStatusDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  
  @Column(name = "name", nullable = false)
  private String name;
  
  @OneToMany(mappedBy = "eventStatus", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<EventDao> events;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<EventDao> getEvents() {
    return events;
  }

  public void setEvents(Set<EventDao> events) {
    this.events = events;
  }
}
