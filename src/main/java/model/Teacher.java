package model;

import com.avaje.ebean.Model;
import com.google.api.client.util.Key;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by merrillm on 11/22/16.
 */
@Entity
@Table(name = "teachers")
public class Teacher extends Model {
    
    @Key
    @Id
    public Long id;
    
    public String firstName;
    public String lastName;
    
    public String username;
    
    public List<Course> courses() {
        return new Model.Finder<Long, Course>(Course.class)
                .where().eq("teacher_id", id).findList();
    }
    
}
