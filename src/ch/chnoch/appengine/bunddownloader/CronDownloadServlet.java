package ch.chnoch.appengine.bunddownloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpMethod;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.model.File;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public class CronDownloadServlet extends BundDownloaderServlet {

	private static final String DOWNLOAD_URL = "http://epaper.derbund.ch/getAll.asp?d=";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
			SimpleDateFormat niceFormatter = new SimpleDateFormat("dd.MM.yyyy");
			String date = formatter.format(new Date());
			URL url1 = new URL(DOWNLOAD_URL + date);
			System.out.println("Initiaing a cron download to stored userId's on date: " + date);
			URLConnection urlConnection = url1.openConnection();
			urlConnection.setConnectTimeout(0);
			urlConnection.setReadTimeout(0);
			InputStream is = urlConnection.getInputStream();
			
			
			URLFetchService s = URLFetchServiceFactory.getURLFetchService();
			
			s.fetch(new HTTPRequest(url1, HttpMethod.GET, com.google.appengine.api.urlfetch.FetchOptions.Builder.allowTruncate())).getContent();

			ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
			byte[] ba1 = new byte[1024];
			int baLength;

			while ((baLength = is.read(ba1)) != -1) {
				tmpOut.write(ba1, 0, baLength);
			}
			
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			
			Query q = new Query("Subscribers");
			List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
			
			// Create a file
			File file = new File();
			file.setTitle("Der Bund " + niceFormatter.format(new Date()) + ".pdf");
			file.setDescription("Der Bund");
			file.setFileExtension("pdf");
			file.setMimeType("application/pdf");
			
			AbstractInputStreamContent content = new ByteArrayContent(
					"application/pdf", tmpOut.toByteArray());
			
			
			for (Entity e : results) {
				String userId = e.getProperty("userId").toString();
//				Log.info("Pushing to account with userId: " + userId);
				pushFileToAccount(userId, file, content);
//				Log.info("Successfull push");
			}

			is.close();
			tmpOut.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	private void pushFileToAccount(String userId, File file, AbstractInputStreamContent content) throws Exception {
		Drive service = getDriveService(userId);
		Insert insert = service.files().insert(file, content);
		insert.getMediaHttpUploader().setChunkSize(1024 * 1024);
		insert.execute();
		
	}
	
	private Drive getDriveService(String userId) {
		Credential credentials = getCredentialFromUserID(userId);
		return new Drive.Builder(TRANSPORT, JSON_FACTORY, credentials).build();
	}

}
