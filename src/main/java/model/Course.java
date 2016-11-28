package model;

import com.avaje.ebean.Model;
import com.google.api.client.util.Key;

import javax.persistence.*;

@Entity
@Table(name = "courses")
public class Course extends Model {
    
    @Key
    @Id
    public Long id;
    
    public String name;
    public String section;
    
    public String referenceId;
    public String keycode;
    
    // public Date keycodeExpireTime;
    
    @ManyToOne
    public Teacher teacher;
    public String sheetId;
    
}
