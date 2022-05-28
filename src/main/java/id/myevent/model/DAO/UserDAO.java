package id.myevent.model.DAO;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "event_organizer")
@Data
public class UserDAO {
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
