import model.Course;
import spark.Request;

import java.util.Map;

public class AttendRequestValidator {
    
    private static final AttendRequestValidator instance;
    
    static {
        instance = new AttendRequestValidator();
    }
    
    
    public static AttendRequestValidator getInstance() {
        return instance;
    }
    
    
    public boolean isValid(Request req) {
        try {
            Map<String, String> form = InterpretBody.asForm(req.body());
            Long courseId = Long.parseLong(form.get("courseId"));
            Course course = Course.find.byId(courseId);
            
            if (course == null || !course.canEnroll() || !course.currentKeycode.equals(form.get("keycode"))) {
                return false;
            }
            
            return req.ip().startsWith("130.86") || req.ip().equals("127.0.0.1");
        } catch (Exception e) {
            return false;
        }
    }
}
