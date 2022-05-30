package id.myevent.model.dao;

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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(unique = true, name = "username")
  private String username;

  @Column(unique = true, name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "organizer_name")
  private String organizerName;

  @Column(name = "phone_number")
  private String phoneNumber;
}
