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

@Entity
@Table(name = "participant")
@Data
public class ParticipantDao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "email")
  private String email;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "status")
  private String status;

  @JsonIgnore
  @ManyToOne()
  @JoinColumn(name = "event_id", referencedColumnName = "id", nullable = false)
  private EventDao event;

  @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
  private List<TicketParticipantDao> ticketParticipants;

}
