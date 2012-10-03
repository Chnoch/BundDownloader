package ch.chnoch.appengine.bunddownloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class BundDownloaderServlet extends HttpServlet {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	
    private static String CLIENT_ID = "306760025553-kc34abpk7mupa9c86oifglvesvseqbli.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "3XVEAoBWN2r_m5cbAwH-M1Md";

    private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		InputStream is = null;
		try {
//            URL url1 = new URL("http://epaper.derbund.ch/getAll.asp?d=20092012");
//
//            byte[] ba1 = new byte[1024];
//            int baLength;
//            ArrayList<Byte> list = new ArrayList<Byte>();
//            is = url1.openStream();
//            while ((baLength = is.read(ba1)) != -1) {
//            	for (Byte b : ba1) {
//            		list.add(b);
//            	}
//            }
            
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();
           
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
                .setAccessType("online")
                .setApprovalPrompt("auto").build();
            
            String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
            System.out.println("Please open the following URL in your browser then type the authorization code:");
            System.out.println("  " + url);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String code = br.readLine();
            
            GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
            GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);
            
            //Create a new authorized API client
            Drive service = new Drive.Builder(httpTransport, jsonFactory, credential).build();

            //Insert a file  
            File body = new File();
            body.setTitle("My document");
            body.setDescription("A test document");
            body.setMimeType("text/plain");
            
            java.io.File fileContent = new java.io.File("document.txt");
            FileContent mediaContent = new FileContent("text/plain", fileContent);

            File file = service.files().insert(body, mediaContent).execute();
            

        } catch (MalformedURLException e) {
            // ...
        } catch (IOException e) {
            // ...
        } finally {
        	
        	is.close();
        }

		if (user != null) {
			resp.setContentType("text/plain");
			resp.getWriter().println("Hello, " + user.getNickname());
		} else {
			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
		}
	}
}
