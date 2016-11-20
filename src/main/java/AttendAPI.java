import model.Receipt;
import spark.Spark;

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
                res.redirect("/#/failure");
                return "Bad Request! :(";
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
    }
    
    public void stop() {
        spark.Spark.stop();
    }
}
