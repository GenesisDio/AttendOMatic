import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by merrillm on 11/20/16.
 */
public class InterpretBody {
    
    public static Map<String, String> asForm(String body) {
        Map<String, String> ret = new HashMap<>();
        
        String[] items = body.split("&");
        for (String item : items) {
            String[] split = item.split("=");
            
            if (split.length == 0)
                continue;
            
            ret.put(decode(split[0]), split.length>1 ? decode(split[1]) : "");
        }
        
        return ret;
    }
    
    private static String decode(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return url;
        }
    }
}
