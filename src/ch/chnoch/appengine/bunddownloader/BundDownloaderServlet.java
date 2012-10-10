package ch.chnoch.appengine.bunddownloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import ch.chnoch.appengine.bunddownloader.CredentialMediator.InvalidClientSecretsException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

@SuppressWarnings("serial")
public class BundDownloaderServlet extends HttpServlet {

	private BlobstoreService blobstoreService = BlobstoreServiceFactory
			.getBlobstoreService();

	private static String CLIENT_ID = "306760025553-k0cehr6gijkq0livhic9k1s3i8fg6snl.apps.googleusercontent.com";
	private static String CLIENT_SECRET = "tEMT75KSl8avcG2BADkJSLWW";

	private static String REDIRECT_URI = "http://localhost/oauth2callback";

	 public void doGet(HttpServletRequest req, HttpServletResponse resp)
	 throws IOException, ServletException {
//	 UserService userService = UserServiceFactory.getUserService();
//	 User user = userService.getCurrentUser();
	 InputStream is = null;
	 try {
	 // URL url1 = new
	 // URL("http://epaper.derbund.ch/getAll.asp?d=03102012");
	 //
	 // byte[] ba1 = new byte[1024];
	 // int baLength;
	 // ArrayList<Byte> list = new ArrayList<Byte>();
	 //
	 // URLConnection urlConnection = url1.openConnection();
	 // urlConnection.setConnectTimeout(20000);
	 // long size = urlConnection.getContentLengthLong();
	 // System.out.println("Size of stream: " + size);
	 // urlConnection.
	 // is = url1.openStream();
	 // while ((baLength = is.read(ba1)) != -1) {
	 // for (Byte b : ba1) {
	 // list.add(b);
	 // }
	 // }
	
	 HttpTransport httpTransport = new NetHttpTransport();
	 JsonFactory jsonFactory = new JacksonFactory();
	
//	 GoogleAuthorizationCodeFlow flow = new
//	 GoogleAuthorizationCodeFlow.Builder(
//	 httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET,
//	 Arrays.asList(DriveScopes.DRIVE)).setAccessType("online")
//	 .setApprovalPrompt("auto").build();
	
	 GoogleAuthorizationCodeRequestUrl urlBuilder = new
	 GoogleAuthorizationCodeRequestUrl(
	 CLIENT_ID, REDIRECT_URI, Arrays.asList(DriveScopes.DRIVE))
	 .setAccessType("offline").setApprovalPrompt("force");
	 urlBuilder.set("user_id", "chnoch@gmail.com");
	 String url = urlBuilder.build();
	
	 // String url = flow.newAuthorizationUrl()
	 // .setRedirectUri(REDIRECT_URI).build();
	 System.out.println("URL: " + url);
//	 resp.sendRedirect(url);
	 // System.out
	 //
//	 .println("Please open the following URL in your browser then type the authorization code:");
	 // System.out.println("  " + url);
	 // BufferedReader br = new BufferedReader(new InputStreamReader(
	 // System.in));
	 // String code = br.readLine();
	 //
	 // GoogleTokenResponse response = flow.newTokenRequest(code)
	 // .setRedirectUri(REDIRECT_URI).execute();
	 // GoogleCredential credential = new GoogleCredential()
	 // .setFromTokenResponse(response);
	 //
	 // // Create a new authorized API client
	 // Drive service = new Drive.Builder(httpTransport, jsonFactory,
	 // credential).build();
	 //
	 // // Insert a file
	 // File body = new File();
	 // body.setTitle("My document");
	 // body.setDescription("A test document");
	 // body.setMimeType("text/plain");
	 //
	 // java.io.File fileContent = new java.io.File("document.txt");
	 // FileContent mediaContent = new FileContent("text/plain",
	 // fileContent);
	 //
	 // File file = service.files().insert(body, mediaContent).execute();
	 //
	 } catch (Exception e) {
	 e.printStackTrace();
	 } finally {
	
	 // is.close();
	 }
	 //
	 // if (user != null) {
	 // resp.setContentType("text/plain");
	 // resp.getWriter().println("Hello, " + user.getNickname());
	 // } else {
	 // resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
	 // }
	 }

	protected static final HttpTransport TRANSPORT = new NetHttpTransport();
	protected static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * Default MIME type of files created or handled by DrEdit.
	 * 
	 * This is also set in the Google APIs Console under the Drive SDK tab.
	 */
	public static final String DEFAULT_MIMETYPE = "text/plain";

	/**
	 * MIME type to use when sending responses back to DrEdit JavaScript client.
	 */
	public static final String JSON_MIMETYPE = "application/json";

	/**
	 * Path component under war/ to locate client_secrets.json file.
	 */
	public static final String CLIENT_SECRETS_FILE_PATH = "/WEB-INF/client_secrets.json";

	/**
	 * Scopes for which to request access from the user.
	 */
	public static final List<String> SCOPES = Arrays.asList(
			// Required to access and manipulate files.
			"https://www.googleapis.com/auth/drive"
			// Required to identify the user in our data store.
//			"https://www.googleapis.com/auth/userinfo.email",
//			"https://www.googleapis.com/auth/userinfo.profile"
			);

	protected void sendError(HttpServletResponse resp, int code, String message) {
		try {
			resp.sendError(code, message);
		} catch (IOException e) {
			throw new RuntimeException(message);
		}
	}

	protected InputStream getClientSecretsStream() {
		return getServletContext()
				.getResourceAsStream(CLIENT_SECRETS_FILE_PATH);
	}

	protected CredentialMediator getCredentialMediator(HttpServletRequest req,
			HttpServletResponse resp) {
		// Authorize or fetch credentials. Required here to ensure this happens
		// on first page load. Then, credentials will be stored in the user's
		// session.
		CredentialMediator mediator;
		try {
			mediator = new CredentialMediator(req, getClientSecretsStream(),
					SCOPES);
			mediator.getActiveCredential();
			return mediator;
		} catch (CredentialMediator.NoRefreshTokenException e) {
			try {
				System.out.println("Before redirect" + e.getAuthorizationUrl());
				resp.sendRedirect(e.getAuthorizationUrl());
			} catch (IOException ioe) {
				throw new RuntimeException(
						"Failed to redirect user for authorization");
			}
			throw new RuntimeException(
					"No refresh token found. Re-authorizing.");
		} catch (InvalidClientSecretsException e) {
			String message = String.format(
					"This application is not properly configured: %s",
					e.getMessage());
			sendError(resp, 500, message);
			throw new RuntimeException(message);
		} catch (IOException e) {
			String message = String.format(
					"An error happened while reading credentials: %s",
					e.getMessage());
			sendError(resp, 500, message);
			throw new RuntimeException(message);
		}
	}

	protected Credential getCredential(HttpServletRequest req,
			HttpServletResponse resp) {
		try {
			CredentialMediator mediator = getCredentialMediator(req, resp);
			return mediator.getActiveCredential();
		} catch (CredentialMediator.NoRefreshTokenException e) {
			try {
				resp.sendRedirect(e.getAuthorizationUrl());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				throw new RuntimeException(
						"Failed to redirect for authorization.");
			}
		} catch (IOException e) {
			String message = String.format(
					"An error happened while reading credentials: %s",
					e.getMessage());
			sendError(resp, 500, message);
			throw new RuntimeException(message);
		}
		return null;
	}

	protected String getClientId(HttpServletRequest req,
			HttpServletResponse resp) {
		return getCredentialMediator(req, resp).getClientSecrets().getWeb()
				.getClientId();
	}

	protected void deleteCredential(HttpServletRequest req,
			HttpServletResponse resp) {
		CredentialMediator mediator = getCredentialMediator(req, resp);
		try {
			mediator.deleteActiveCredential();
		} catch (IOException e) {
			String message = String.format(
					"An error happened while reading credentials: %s",
					e.getMessage());
			sendError(resp, 500, message);
			throw new RuntimeException(message);
		}
	}
}
