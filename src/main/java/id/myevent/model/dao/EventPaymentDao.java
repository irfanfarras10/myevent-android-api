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
 * Event Payment Dao.
 */
@Entity
@Table(name = "event_payment")
@Data
public class EventPaymentDao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "type")
  private String type;

  @Column(name = "information")
  private String information;

  @JsonIgnore
  @ManyToOne()
  @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
  private EventDao event;

}
