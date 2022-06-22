package id.myevent.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

/**
 * Event Guest Dao.
 */

@Entity
@Table(name = "event_guest")
@Data
public class EventGuestDao {

  @Id
  @JsonIgnore
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "email")
  private String email;

  @Column(name = "already_shared")
  private boolean alreadyShared;

  @JsonIgnore
  @ManyToOne()
  @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
  private EventDao event;
}
