package id.myevent.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;

/**
 * Ticket Participant Dao.
 */
@Entity
@Table(name = "ticket_participant")
@Data
public class TicketParticipantDao {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @JsonIgnore
  @ManyToOne()
  @JoinColumn(name = "ticket_id", referencedColumnName = "id", nullable = false)
  private TicketDao ticket;

  @JsonIgnore
  @ManyToOne()
  @JoinColumn(name = "participant_id", referencedColumnName = "id", nullable = false)
  private ParticipantDao participant;

  @JsonIgnore
  @ManyToOne()
  @JoinColumn(name = "event_payment_id", referencedColumnName = "id")
  private EventPaymentDao eventPayment;

  @Column(name = "event_date")
  private Long event_date;

  @Column(name = "purchase_date")
  private Long purchase_date;

  @Column(name = "status")
  private String status;

  @JsonIgnore
  @Column(name = "payment_photo_proof")
  private byte[] paymentPhotoProof;

  @JsonIgnore
  @Column(name = "payment_proof_name")
  private String paymentPhotoName;

  @JsonIgnore
  @Column(name = "payment_proof_type")
  private String paymentPhotoType;
}
