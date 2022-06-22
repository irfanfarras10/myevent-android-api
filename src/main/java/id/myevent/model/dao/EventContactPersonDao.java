package id.myevent.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;

/** Contact Person DAO. */
@Entity
@Table(name = "event_contact_person")
@Data
public class EventContactPersonDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  
  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "contact", nullable = false)
  private String contact;
  
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "event_social_media_id", referencedColumnName = "id", nullable = false)
  private EventSocialMediaDao eventSocialMedia;

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
  private EventDao event;
}
