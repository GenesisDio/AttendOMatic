import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.parser.JSONParser;
import model.Course;
import spark.Request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by merrillm on 11/20/16.
 */
public class InterpretBody {
    
    private static final ObjectMapper mapper;
    
    static {
        mapper = new ObjectMapper();
    }
    
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
    
    public static JsonNode jsonAsJson(Request request) throws IOException {
        return mapper.readTree(request.body());
    }
    
    public static <T> T jsonAsClass(Request request, Class<T> tClass) throws  IOException {
        return mapper.readerFor(tClass).readValue(request.body());
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
