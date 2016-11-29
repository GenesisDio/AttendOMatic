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

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class AttendAPI {
    
    private AttendRequestValidator validator = AttendRequestValidator.getInstance();
    private AttendanceLogger attendanceLogger = AttendanceLogger.getInstance();
    
    
    public AttendAPI() {
    }
    
    public void deploy() {
        
        String port = "4567";
        
        Map<String, String> env = System.getenv();
        if (env.containsKey("PORT"))
            port = env.get("PORT");
        
//        staticFileLocation("/public");
        Spark.staticFileLocation("/public");
        Spark.staticFiles.expireTime(1);
        Spark.port(Integer.parseInt(port));
        
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
            try {
                JsonNode json = InterpretBody.jsonAsJson(req);
                Long courseId = json.get("id").asLong();
                String key = json.get("keycode").asText().toLowerCase();
        
                Course course = new Model.Finder<Long, Course>(Course.class)
                        .byId(courseId);
        
                if (course == null) {
                    res.status(400);
                    return "Bad Request: Missing course :(";
                }
        
                course.nextKeycode = key;
                course.update();
        
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.status(400);
                return "Your fault. Look what you've done.";
            }
        });
        
        before("/api/course/open", loggedInFilter);
        post("/api/course/open", (req,res) -> {
            try {
                JsonNode json = InterpretBody.jsonAsJson(req);
                Long courseId = json.get("id").asLong();
                String key = json.get("keycode").asText().toLowerCase();
    
                Course course = new Model.Finder<Long, Course>(Course.class)
                        .byId(courseId);
    
                if (course == null) {
                    res.status(400);
                    return "Bad Request: Missing course :(";
                }
    
                course.nextKeycode = key;
                course.openAttendance();
                course.update();
    
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                res.status(400);
                return "Your fault. Look what you've done.";
            }
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
            
            Map<String, Course> courseMap = new HashMap<String, Course>();
            for (Course course : teacher.courses())
                courseMap.put(course.id +"", course);
            
        	String jsonString = mapper.writeValueAsString(courseMap);
        	return jsonString;
        });
    
        before("/api/course", loggedInFilter);
        put("/api/course", (req, res) -> {
            try {
                Course course = InterpretBody.jsonAsClass(req, Course.class);
                course.teacher = currentTeacher(req);
                course.update();
                System.out.println(course.name);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(400);
            }
            return "";
        });
        post("/api/course", (req, res) -> {
            Course course = InterpretBody.jsonAsClass(req, Course.class);
            course.teacher = currentTeacher(req);
            course.save();
            
            return "";
        });
    
        before("/api/manual", loggedInFilter);
        post("/api/manual", (req, res) -> {
            JsonNode form = InterpretBody.jsonAsJson(req);
            return attendanceLogger.log(form.get("studentid").asText(), Course.find.byId(form.get("courseid").asLong()), form.get("date").asText().substring(0,10)).id;
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
