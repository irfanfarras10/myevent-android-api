package id.myevent.model.dao;

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
@Table(name = "contact_person")
@Data
public class ContactPersonDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  
  @Column(name = "name", nullable = false)
  private String name;
  
  @ManyToOne
  @JoinColumn(name = "social_media_id", nullable = false)
  private SocialMediaDao socialMedia;
  
  @ManyToOne
  @JoinColumn(name = "event_id", nullable = false)
  private EventDao event;
}
