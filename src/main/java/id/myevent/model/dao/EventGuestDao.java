package id.myevent.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

/** Event Guest Dao
 *
 */

@Entity
@Table(name="event_guest")
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
