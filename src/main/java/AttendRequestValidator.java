import spark.Request;

public class AttendRequestValidator {
    
    private static final AttendRequestValidator instance;
    
    static {
        instance = new AttendRequestValidator();
    }
    
    
    public static AttendRequestValidator getInstance() {
        return instance;
    }
    
    
    public boolean isValid(Request req) {
        // TODO: Add functionality here -merrillm
        return req.ip().startsWith("130.86");
    }
}
