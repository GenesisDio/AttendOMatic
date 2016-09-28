import static spark.Spark.get;

public class AttendAPI {
    
    public static void main(String[] args) {
        get("/", (req,res) -> "hello world");
    }
}
