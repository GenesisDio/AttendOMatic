import spark.Spark;

import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

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
                res.status(BAD_REQUEST_400);
                return "";
            }
            
            try {
                attendanceLogger.log(req);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        });
    }
    
    public void stop() {
        spark.Spark.stop();
    }
}
