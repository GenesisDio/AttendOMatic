import com.avaje.ebean.Model;
import model.Course;
import model.Receipt;
import spark.Spark;

import java.util.Map;

import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static spark.Spark.get;
import static spark.Spark.post;

public class AttendAPI {
    
    private AttendRequestValidator validator = AttendRequestValidator.getInstance();
    private AttendanceLogger attendanceLogger = AttendanceLogger.getInstance();
    
    
    public AttendAPI() {
    }
    
    public void deploy() {
//        staticFileLocation("/public");
        Spark.staticFileLocation("/public");
        Spark.staticFiles.expireTime(1);
        
        get("/hello", (req,res) -> "hello world");
        
        post("/api/attending", (req,res) -> {
            if (!validator.isValid(req)) {
                res.redirect("/#/failure");
                return "Bad Request! :(";
            }
            
            try {
                Receipt receipt = attendanceLogger.log(req);
                res.redirect("/#/success?confnum=" + receipt.id);
                return "Logged!";
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/#/failure");
                return "Bad Request! :(";
            }
        });
        
        post("/api/course/keycode", (req,res) -> {
            Map<String, String> form = InterpretBody.asForm(req.body());
            String courseId = form.get("course");
            String key = form.get("key");
            
            Course course = new Model.Finder<Long, Course>(Course.class)
                    .byId(Long.valueOf(courseId));
            
            if (course == null) {
                res.status(400);
                return "Bad Request: Missing course :(";
            }
            
            course.keycode = key;
            course.update();
            
            return "";
        });
        
        get("trace", (req, res)->{
            try {
                return req.ip();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ":/";
        });
    }
    
    public void stop() {
        spark.Spark.stop();
    }
}
