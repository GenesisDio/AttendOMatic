package model;

import com.avaje.ebean.Model;
import com.google.api.client.util.Key;

import javax.persistence.*;
import java.sql.Time;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "courses")
@UniqueConstraint(columnNames = {"reference_id"})
public class Course extends Model {
    
    public static Model.Finder<Long, Course> find = new Model.Finder<Long, Course>(Course.class);
    
    @Key
    @Id
    public Long id;
    
    public String name;
    public String section;
    
    public String referenceId;
    public String nextKeycode;
    
    public String currentKeycode;
    public Instant keycodeOpenTime;
    public Instant keycodeExpireTime;
    
    @ManyToOne
    public Teacher teacher;
    public String sheetUrl;
    
    public String getSheetId() {
        try {
            String ret = sheetUrl.substring(sheetUrl.indexOf("/d/")+3);
            return ret.substring(0, ret.indexOf('/'));
        } catch (Exception ignored) {
            return null;
        }
    }
    
    public void openAttendance() {
        if (nextKeycode == null)
            throw new IllegalStateException("A keycode has not been set!");
        
        currentKeycode = nextKeycode;
        keycodeOpenTime = Instant.now();
        keycodeExpireTime = Instant.now().plusSeconds(1800);
    }
    
    public boolean canEnroll() {
        Instant now = Instant.now();
        return now.isAfter(keycodeOpenTime) && now.isBefore(keycodeExpireTime);
    }
    
    public boolean isDirty() {
        return currentKeycode == null || Instant.now().isAfter(keycodeExpireTime) && currentKeycode.equals(nextKeycode);
    }
}
