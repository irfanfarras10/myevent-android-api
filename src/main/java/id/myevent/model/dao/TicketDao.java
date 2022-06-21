package id.myevent.model.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** Event DAO. */
@Entity
@Table(name = "ticket")
@Data
public class TicketDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "price", nullable = false)
  private long price;

  @Column(name = "quota_per_day")
  private long quotaPerDay;

  @Column(name = "quota_total", nullable = false)
  private long quotaTotal;

  @JsonIgnore
  @ManyToOne()
  @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
  private EventDao event;
}
