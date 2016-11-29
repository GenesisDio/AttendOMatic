import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.Course;
import model.Receipt;
import model.Teacher;
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
        
        post("/api/teacher/idtok", (req, res) -> {
            try {
                System.out.println(req.body());
                JsonNode json = InterpretBody.jsonAsJson(req);
                String idToken = json.get("id_token").textValue();
                String email = json.get("email").textValue();
                String name = json.get("name").textValue();
                
                Teacher teacher = (Teacher) Teacher.find.where().eq("email", email).findUnique();
    
                if (teacher == null) {
                    teacher = new Teacher();
                    teacher.idToken = idToken;
                    teacher.email = email;
                    teacher.name = name;
                    teacher.save();
                } else {
                    teacher.name = name;
                    teacher.email = email;
                    teacher.idToken = idToken;
                    teacher.save();
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(400);
                return "";
            }
            return "";
        });
        
        get("/api/courses", (req, res) -> {
        	
        	Teacher teacher = (Teacher) Teacher.find.where().eq("email", "mattmerr47@gmail.com").findUnique();
        	ObjectMapper mapper = new ObjectMapper();
        	String jsonString = mapper.writeValueAsString(teacher.courses());
        	return jsonString;
        });
        
        post("/api/course", (req, res) -> {
            Course course = InterpretBody.jsonAsClass(req, Course.class);
            course.save();
            
            return "";
        });
    }
    
    public void stop() {
        spark.Spark.stop();
    }
}
