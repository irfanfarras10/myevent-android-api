package id.myevent.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/** User DAO. */
@Entity
@Table(name = "event_organizer")
@Data
public class UserDao {
  @Id
  @JsonIgnore
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(unique = true, name = "username", nullable = false)
  private String username;

  @Column(unique = true, name = "email", nullable = false)
  private String email;

  @JsonIgnore
  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "organizer_name", nullable = false)
  private String organizerName;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;
}
