import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import spark.Response;
import spark.Request;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Created by merrillm on 11/28/16.
 */
public class GoogleLoginAPI {
    
    private static final String CLIENT_ID = "1075583454957-6t6vqmk3aa4ofbnvk1mnfgf1t8j2gqp3";
    
    public static void route(){
        
    }
    
//    public static String loginToken(Request req, Response res) {
//        JsonFactory jsonFactory = new JsonFactory();
//        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
//                .setAudience(Arrays.asList(CLIENT_ID))
//                // If you retrieved the token on Android using the Play Services 8.3 API or newer, set
//                // the issuer to "https://accounts.google.com". Otherwise, set the issuer to
//                // "accounts.google.com". If you need to verify tokens from multiple sources, build
//                // a GoogleIdTokenVerifier for each issuer and try them both.
//                .setIssuer("https://accounts.google.com")
//                .build();
//
//// (Receive idTokenString by HTTPS POST)
//
//        GoogleIdToken idToken = null;
//        try {
//            idToken = verifier.verify(req.body());
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (idToken != null) {
//            GoogleIdToken.Payload payload = idToken.getPayload();
//
//            // Print user identifier
//            String userId = payload.getSubject();
//            System.out.println("User ID: " + userId);
//
//            // Get profile information from payload
//            String email = payload.getEmail();
//            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
//            String name = (String) payload.get("name");
//            String pictureUrl = (String) payload.get("picture");
//            String locale = (String) payload.get("locale");
//            String familyName = (String) payload.get("family_name");
//            String givenName = (String) payload.get("given_name");
//
//            // Use or store profile information
//            // ...
//
//        } else {
//            System.out.println("Invalid ID token.");
//        }
//    }
}
