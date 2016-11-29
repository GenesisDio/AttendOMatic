package model;

import com.avaje.ebean.Model;
import com.google.api.client.util.Key;

import javax.persistence.*;
import java.util.List;

/**
 * Created by merrillm on 11/22/16.
 */
@Entity
@Table(name = "teachers")
public class Teacher extends Model {
    
    public static Model.Finder<Long, Teacher> find = new Model.Finder<Long, Teacher>(Teacher.class);
    
    @Key
    @Id
    public Long id;
    
    public String name;
    public String email;
    
    @Column(columnDefinition = "TEXT")
    public String idToken;
    
    public List<Course> courses() {
        return (List<Course>) Course.find.where().eq("teacher_id", id).findList();
    }
    
}
