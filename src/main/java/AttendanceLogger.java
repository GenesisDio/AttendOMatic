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
import model.Course;
import model.Receipt;
import spark.Request;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

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
    
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public Receipt log(Request request) throws IOException {
        Map<String, String> requestForm = InterpretBody.asForm(request.body());
        String studentId = requestForm.get("studentid");
        Long courseId = Long.parseLong(requestForm.get("courseId"));
        Course course = Course.find.byId(courseId);
        String date = dateFormat.format(new Date());
        return log(studentId, course, date);
    }
    public Receipt log(String studentId, Course course, String date) throws IOException {
    
        // today
        Calendar caldate = new GregorianCalendar();
        
        String[] split = date.split("-");
        
        caldate.set(Calendar.YEAR, Integer.parseInt(split[0]));
        caldate.set(Calendar.MONTH, Integer.parseInt(split[1])-1);
        caldate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(split[2]));
        
        // reset hour, minutes, seconds and millis
        caldate.set(Calendar.HOUR_OF_DAY, 0);
        caldate.set(Calendar.MINUTE, 0);
        caldate.set(Calendar.SECOND, 0);
        caldate.set(Calendar.MILLISECOND, 0);
        Instant dayStart = caldate.toInstant();
        
        // next day
        caldate.add(Calendar.DAY_OF_MONTH, 1);
        Instant dayEnd = caldate.toInstant();
        
        System.out.println(dayStart +" < " + date + " < " + dayEnd);
        
        List<Receipt> receipts = Receipt.find
                .where()
                    .eq("student_id", studentId)
                    .between("time_submitted", dayStart, dayEnd)
                .findList();
        
        if (!receipts.isEmpty())
            return receipts.get(0);
        
        System.out.println(receipts);
        
        // Build a new authorized API client service.
        Sheets service = getSheetsService();
        String spreadsheetId = course.getSheetId();
    
    
        int updateRow = rowFor(studentId,
                spreadsheetId,
                service.spreadsheets());
        
        int updateCol = colFor(date,
                spreadsheetId,
                service.spreadsheets());
        
        if (updateRow < 0) {
            throw new IllegalArgumentException("Unknown StudentID:" + studentId);
        }
        
        // Create requests object
        List<com.google.api.services.sheets.v4.model.Request> requests = new ArrayList<>();
        
        // Create values object
        List<CellData> values = new ArrayList<>();
        
        // Add string date value
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue((date))));
        
        if (updateCol < 0) {
            updateCol *= -1;
            requests.add(new com.google.api.services.sheets.v4.model.Request()
                    .setAppendDimension(new AppendDimensionRequest()
                            .setDimension("COLUMNS")
                            .setSheetId(0)
                            .setLength(1)));
        }
            
        // Prepare request with proper row and column and its value
        requests.add(new com.google.api.services.sheets.v4.model.Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(0)
                                .setRowIndex(0)     // set the row to row 0
                                .setColumnIndex(updateCol)) // set the new column 6 to value 9/12/2016 at row 0
                        .setRows(Collections.singletonList(
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
                                .setColumnIndex(updateCol)) // set the new column 6 to value "Y" at row 1
                        .setRows(Collections.singletonList(
                                new RowData().setValues(valuesNew)))
                        .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));
        BatchUpdateSpreadsheetRequest batchUpdateRequestNew = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequestNew)
                .execute();
    
        Receipt receipt = new Receipt(studentId);
        receipt.save();
        
        return receipt;
    }
    
    private static int colFor(String date, String spreadsheetId, Sheets.Spreadsheets spreadsheets) throws IOException {
        String range = "1:1";
        ValueRange response = spreadsheets.values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.size() == 0) {
            return -1;
        } else {
            int ret = values.get(0).size() - 1;
            if (ret > 0 && values.get(0).get(ret).equals(date))
                return ret;
            else
                return -(ret+1);
        }
    }
    
    private static int rowFor(String studentId, String spreadsheetId, Sheets.Spreadsheets spreadsheets) throws IOException {
        String range = "D:D";
        ValueRange response = spreadsheets.values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.size() == 0) {
            return -1;
        } else {
            for (int i = 0; i < values.size(); i++) {
                //System.out.printf("%s\n", values.get(i).get(0));
                if (values.get(i).get(0).equals(studentId))
                    return i;
            }
        }
        return -1;
    }
}

