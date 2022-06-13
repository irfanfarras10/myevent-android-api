package id.myevent.model.dao;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;

/** Event Status DAO. */
@Entity
@Table(name = "social_media")
@Data
public class SocialMediaDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  
  @Column(name = "name", nullable = false)
  private String name;
  
  @OneToMany(mappedBy = "socialMedia", cascade = CascadeType.ALL)
  private Set<ContactPersonDao> contactPersons;
}
