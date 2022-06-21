package id.myevent.model.dao;

import java.util.List;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  
  @Column(name = "venue")
  private String venue;

  @JsonIgnore
  @Column(name = "banner_photo", unique = false, length = 100000, nullable = false)
  private byte[] bannerPhoto;

  @JsonIgnore
  @Column(name = "banner_photo_name", nullable = false)
  private String bannerPhotoName;

  @JsonIgnore
  @Column(name = "banner_photo_type", nullable = false)
  private String bannerPhotoType;
  
  @Column(name = "datetime_registration_start")
  private int dateTimeRegistrationStart;
  
  @Column(name = "datetime_registration_end")
  private int dateTimeRegistrationEnd;
  
  @ManyToOne()
  @JoinColumn(name = "event_status_id", referencedColumnName = "id", nullable = false)
  private EventStatusDao eventStatus;
  
  @ManyToOne()
  @JoinColumn(name = "event_category_id", referencedColumnName = "id", nullable = false)
  private EventCategoryDao eventCategory;

  @ManyToOne()
  @JoinColumn(name = "event_venue_category", referencedColumnName = "id", nullable = false)
  private EventVenueCategoryDao eventVenueCategory;
  
  @ManyToOne()
  @JoinColumn(name = "event_payment_category_id", referencedColumnName = "id")
  private EventPaymentCategoryDao eventPaymentCategory;
  
  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  private List<EventContactPersonDao> eventContactPersons;
  
  @ManyToOne()
  @JoinColumn(name = "event_organizer_id", referencedColumnName = "id", nullable = false)
  private EventOrganizerDao eventOrganizer;
}
