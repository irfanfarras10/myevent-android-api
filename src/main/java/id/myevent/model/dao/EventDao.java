package id.myevent.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
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
  
  @Column(name = "date_event_start", nullable = false)
  private long dateEventStart;
  
  @Column(name = "date_event_end", nullable = false)
  private long dateEventEnd;

  @Column(name = "time_event_start", nullable = false)
  private long timeEventStart;

  @Column(name = "time_event_end", nullable = false)
  private long timeEventEnd;
  
  @Column(name = "venue")
  private String venue;

  @JsonIgnore
  @Column(name = "banner_photo", unique = false, nullable = false)
  private byte[] bannerPhoto;

  @JsonIgnore
  @Column(name = "banner_photo_name", nullable = false)
  private String bannerPhotoName;

  @JsonIgnore
  @Column(name = "banner_photo_type", nullable = false)
  private String bannerPhotoType;
  
  @Column(name = "datetime_registration_start")
  private long dateTimeRegistrationStart;
  
  @Column(name = "datetime_registration_end")
  private long dateTimeRegistrationEnd;
  
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
  private List<TicketDao> eventTicket;

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  private List<EventContactPersonDao> eventContactPersons;

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  private List<EventGuestDao> eventGuest;

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
  private List<EventPaymentDao> eventPayment;
  
  @ManyToOne()
  @JoinColumn(name = "event_organizer_id", referencedColumnName = "id", nullable = false)
  private EventOrganizerDao eventOrganizer;
}
