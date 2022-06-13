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

/** Category DAO. */
@Entity
@Table(name = "category")
@Data
public class CategoryDao {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  
  @Column(unique = true, name = "name", nullable = false)
  private String name;
  
  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
  private Set<EventDao> events;
}
