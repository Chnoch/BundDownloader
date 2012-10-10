package ch.chnoch.appengine.bunddownloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class OAuth2CallbackServlet extends HttpServlet {

	private GoogleClientSecrets secrets;

	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private static String CLIENT_ID = "306760025553-k0cehr6gijkq0livhic9k1s3i8fg6snl.apps.googleusercontent.com";
	private static String CLIENT_SECRET = "tEMT75KSl8avcG2BADkJSLWW";

	private static String REDIRECT_URI = "http://localhost/oauth2callback";
	/**
	 * HttpTransport to use for external requests.
	 */
	private static final HttpTransport TRANSPORT = new NetHttpTransport();

	HttpTransport httpTransport = new NetHttpTransport();
	JsonFactory jsonFactory = new JacksonFactory();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
    	System.out.println("Callback");
    	String authorizationCode = (String)req.getParameter("code");
    	System.out.println("Auth Code: " + authorizationCode);
    	try {
    	      GoogleTokenResponse response =
    	          new GoogleAuthorizationCodeTokenRequest(
    	              TRANSPORT,
    	              JSON_FACTORY,
    	              CLIENT_ID,
    	              CLIENT_SECRET,
    	              authorizationCode,
    	              REDIRECT_URI).execute();
    	     Credential credential = buildEmptyCredential().setFromTokenResponse(response);
    	     
    	     Drive service = new Drive.Builder(httpTransport, jsonFactory,
 					credential).build();

 			// Insert a file
 			File body = new File();
 			body.setTitle("My document.pdf");
 			body.setDescription("A test document");
 			body.setMimeType("application/pdf");

// 			File fileContent = new File();
// 			FileContent mediaContent = new FileContent("text/plain",
// 					ByteArrayContent.fromString(body.getMimeType(), contentString));
 			File file = service.files().insert(body, ByteArrayContent.fromString(body.getMimeType(), "This is a Test String")).execute();
    	} catch (Exception exc) {
    		exc.printStackTrace();
    	}
        
    }

	private Credential buildEmptyCredential() {
		return new GoogleCredential.Builder().setClientSecrets(CLIENT_ID, CLIENT_SECRET)
				.setTransport(TRANSPORT).setJsonFactory(JSON_FACTORY).build();
	}

}
