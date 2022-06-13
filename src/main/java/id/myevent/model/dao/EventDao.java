package id.myevent.model.dao;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;

/** Event DAO. */
@Entity
@Table(name = "event")
@Data
public class EventDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  
  @Column(name = "name", unique = true)
  private String name;
  
  @Column(name = "description", nullable = false)
  private String description;
  
  @Column(name = "datetime_event_start", nullable = false)
  private int dateTimeEventStart;
  
  @Column(name = "datetime_event_end", nullable = false)
  private int dateTimeEventEnd;
  
  @Column(name = "location", nullable = false)
  private String location;
  
  @Column(name = "banner_photo", nullable = false)
  private String bannerPhoto;
  
  @Column(name = "datetime_registration_start", nullable = false)
  private String dateTimeRegistrationStart;
  
  @Column(name = "datetime_registration_end", nullable = false)
  private String dateTimeRegistrationEnd;
  
  @ManyToOne
  @JoinColumn(name = "status_id", nullable = false)
  private EventStatusDao status;
  
  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private CategoryDao category;
  
  @ManyToOne
  @JoinColumn(name = "payment_category_id", nullable = false)
  private PaymentCategoryDao paymentCategory;
  
  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  private Set<ContactPersonDao> contactPersons;
  
  @ManyToOne
  @JoinColumn(name = "event_organizer_id", nullable = false)
  private UserDao eventOrganizer;
}
