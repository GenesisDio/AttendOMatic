package model;

import com.avaje.ebean.Model;
import com.google.api.client.util.Key;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "receipts")
public class Receipt extends Model {
    
    public static Model.Finder find = new Model.Finder<Long, Receipt>(Receipt.class);
    
    public Receipt(String studentId){
        this.studentId = studentId;
    }
    
    @Key
    @Id
    public int id;
    public String studentId;
    public Date timeSubmitted = new Date();
}
