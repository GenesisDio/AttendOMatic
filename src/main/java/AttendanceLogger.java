import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import spark.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by merrillm on 10/2/16.
 */
public class AttendanceLogger {
    
    private static final AttendanceLogger instance;
    
    private static final String ENV_SHEET_ID = "SHEET_ID";
    
    /** Application name. */
    private static final String APPLICATION_NAME =
            "Google Sheets API Java Quickstart";
    
    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials//sheets.googleapis.com-java-quickstart.json");
    
    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    
    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;
    
    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart.json
     */
    private static final List<String> SCOPES =
            Arrays.asList( SheetsScopes.SPREADSHEETS );
    
    static {
        instance = new AttendanceLogger();
    
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
    
    public static AttendanceLogger getInstance() {
        return instance;
    }
    
    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        // Place client_secret.json file location here
        InputStream in = new FileInputStream("." + File.separator + "client.json");
        // SheetsQuickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }
    
    /***
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    public void log(Request request) throws IOException {
        // Build a new authorized API client service.
        Sheets service = getSheetsService();
        
        // Prints the names and majors of students in a sample spreadsheet:
        // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
        String spreadsheetId = System.getenv(ENV_SHEET_ID);
        
        int updateRow = rowFor(request.body().split("=")[1],
                spreadsheetId,
                service.spreadsheets());
        System.out.println(updateRow);
        
        if (updateRow < 0) {
            throw new IllegalArgumentException("Unknown StudentID:" + request.body());
        }
        
        // Create requests object
        List<com.google.api.services.sheets.v4.model.Request> requests = new ArrayList<>();
        
        // Create values object
        List<CellData> values = new ArrayList<>();
        
        // Add string 6/21/2016 value
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(("9/12/2016"))));
        
        // Prepare request with proper row and column and its value
        requests.add(new com.google.api.services.sheets.v4.model.Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(0)
                                .setRowIndex(0)     // set the row to row 0
                                .setColumnIndex(6)) // set the new column 6 to value 9/12/2016 at row 0
                        .setRows(Arrays.asList(
                                new RowData().setValues(values)))
                        .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));
        
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest)
                .execute();
        
        List<CellData> valuesNew = new ArrayList<>();
        // Add string 6/21/2016 value
        valuesNew.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(("Y"))));
        
        // Prepare request with proper row and column and its value
        requests.add(new com.google.api.services.sheets.v4.model.Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(0)
                                .setRowIndex(updateRow)     // set the row to row 1
                                .setColumnIndex(6)) // set the new column 6 to value "Y" at row 1
                        .setRows(Arrays.asList(
                                new RowData().setValues(valuesNew)))
                        .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));
        BatchUpdateSpreadsheetRequest batchUpdateRequestNew = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequestNew)
                .execute();
        
    }
    
    public int rowFor(String studentId, String spreadsheetId, Sheets.Spreadsheets spreadsheets) throws IOException {
        String range = "D:D";
        ValueRange response = spreadsheets.values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.size() == 0) {
            return -1;
        } else {
            for (int i = 0; i < values.size(); i++) {
                System.out.printf("%s\n", values.get(i).get(0));
                if (values.get(i).get(0).equals(studentId))
                    return i;
            }
        }
        return -1;
    }
}

