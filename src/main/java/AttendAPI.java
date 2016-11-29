import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.plus.samples.verifytoken.Checker;
import model.Course;
import model.Receipt;
import model.Teacher;
import spark.Filter;
import spark.Request;
import spark.Spark;

import java.util.Map;

import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static spark.Spark.*;

public class AttendAPI {
    
    private AttendRequestValidator validator = AttendRequestValidator.getInstance();
    private AttendanceLogger attendanceLogger = AttendanceLogger.getInstance();
    
    
    public AttendAPI() {
    }
    
    public void deploy() {
//        staticFileLocation("/public");
        Spark.staticFileLocation("/public");
        Spark.staticFiles.expireTime(1);
        
        Filter loggedInFilter = (req, res) -> {
            if (req.session().attribute("email") == null || currentTeacher(req) == null)
                halt(403, "You are not logged in!");
        };
        
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
        
        before("/api/course/keycode", loggedInFilter);
        post("/api/course/keycode", (req,res) -> {
            Map<String, String> form = InterpretBody.asForm(req.body());
            String courseId = form.get("id");
            String key = form.get("keycode");
            
            Course course = new Model.Finder<Long, Course>(Course.class)
                    .byId(Long.valueOf(courseId));
            
            if (course == null) {
                res.status(400);
                return "Bad Request: Missing course :(";
            }
            
            course.nextKeycode = key;
            
            if (course.canEnroll())
                course.currentKeycode = key;
            
            course.update();
            
            return "";
        });
        
        before("/api/course/open", loggedInFilter);
        post("/api/course/open", (req,res) -> {
            Map<String, String> form = InterpretBody.asForm(req.body());
            String courseId = form.get("id");
            String key = form.get("keycode");
        
            Course course = new Model.Finder<Long, Course>(Course.class)
                    .byId(Long.valueOf(courseId));
        
            if (course == null) {
                res.status(400);
                return "Bad Request: Missing course :(";
            }
            
            course.nextKeycode = key;
            course.openAttendance();
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
                String idToken = req.body();
                Checker checker = new Checker(new String[]{GoogleLoginAPI.CLIENT_ID}, GoogleLoginAPI.CLIENT_ID);
                GoogleIdToken.Payload payload = checker.check(idToken);
                
                if (payload == null || payload.getEmail() == null) {
                    res.status(403);
                    return "Bad Login";
                }
                
                req.session().attribute("email", payload.getEmail());
                
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                
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
    
        before("/api/courses", loggedInFilter);
        get("/api/courses", (req, res) -> {
        	Teacher teacher = (Teacher) Teacher.find.where().eq("email", req.session().attribute("email")).findUnique();
        	ObjectMapper mapper = new ObjectMapper();
        	String jsonString = mapper.writeValueAsString(teacher.courses());
        	return jsonString;
        });
    
        before("/api/course", loggedInFilter);
        post("/api/course", (req, res) -> {
            Course course = InterpretBody.jsonAsClass(req, Course.class);
            course.teacher = currentTeacher(req);
            course.save();
            
            return "";
        });
    
        before("/api/manual", loggedInFilter);
        post("/api/manual", (req, res) -> {
            // Validate request comes from a teacher
            
            
            return "";
        });
    
        before("/api/curlogin", loggedInFilter);
        get("/api/curlogin", (req, res) -> {
            String email = req.session().attribute("email");
            return new ObjectMapper().writeValueAsString(Teacher.find.where().eq("email", email).findUnique());
        });
    }
    
    public Teacher currentTeacher(Request req) {
        return (Teacher)Teacher.find.where().eq("email", req.session().attribute("email")).findUnique();
    }
    
    public void stop() {
        spark.Spark.stop();
    }
}
