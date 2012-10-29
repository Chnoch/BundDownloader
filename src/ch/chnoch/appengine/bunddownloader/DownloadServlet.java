package ch.chnoch.appengine.bunddownloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.model.File;

@SuppressWarnings("serial")
public class DownloadServlet extends BundDownloaderServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		Date date = new Date();
		try {
			 URL url1 = new
			 URL("http://epaper.derbund.ch/getAll.asp?d=03102012");
			// URL url1 = new URL("http://www.ca7.uscourts.gov/rules/type.pdf");
			// URL url1 = new
			// URL("http://www.imcg.net/media/newsletter/nl1201.pdf");
			// URL url1 = new
			// URL("http://www.marburger-weltladen.de/media/weltsicht/weltsicht_2011-2.pdf");
//			URL url1 = new URL(
//					"http://sdapillars.org/media/download_gallery/No.8.pdf");
			URLConnection urlConnection = url1.openConnection();
			urlConnection.setConnectTimeout(0);
			urlConnection.setReadTimeout(0);
			InputStream is = urlConnection.getInputStream();
			// String enc = urlConnection.getContentEncoding();
			// InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			// BufferedReader br = new BufferedReader(isr);

			ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
			byte[] ba1 = new byte[1024];
			int baLength;

			while ((baLength = is.read(ba1)) != -1) {
				tmpOut.write(ba1, 0, baLength);
			}

			Drive service = getDriveService(req, resp);

			// Insert a file
			File file = new File();
			file.setTitle("Der Bund Heute.pdf");
			file.setDescription("Der Bund");
			file.setFileExtension("pdf");
			file.setMimeType("application/pdf");

			AbstractInputStreamContent content = new ByteArrayContent(
					"application/pdf", tmpOut.toByteArray());
			Insert insert = service.files().insert(file, content);
			insert.getMediaHttpUploader().setChunkSize(1024 * 1024);
			insert.execute();
			resp.getOutputStream()
					.print("Successfully created a file <br> <a href=\"start\">Start</a>");

			is.close();
			tmpOut.close();
		} catch (Exception exc) {
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			Date current = new Date();
			System.out.println(dateFormat.format(date));
			System.out.println(dateFormat.format(current));
			exc.printStackTrace();
		}
	}

//	private void uploadWithHTTP(ByteArrayOutputStream tmpOut) throws ClientProtocolException, IOException {
//		File file = new File();
//		file.setTitle("Der Bund Heute.pdf");
//		file.setDescription("Der Bund");
//		file.setMimeType("application/pdf");
//
//		AbstractInputStreamContent content = new ByteArrayContent(
//				"application/pdf", tmpOut.toByteArray());
//		
//		String url = "https://www.googleapis.com/upload/drive/v2/files?uploadType=media";
//		HttpParams params = new BasicHttpParams();
//		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
//		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//		params.setParameter("content-type", "application/pdf");
//		params.setParameter("body", file);
//		HttpClient httpclient = new DefaultHttpClient(params);
//		HttpPost httppost = new HttpPost(url);
//		httppost.setParams(params);
//		HttpResponse response = httpclient.execute(httppost);
//	}

	private Drive getDriveService(HttpServletRequest req,
			HttpServletResponse resp) {
		Credential credentials = getCredential(req, resp);

		return new Drive.Builder(TRANSPORT, JSON_FACTORY, credentials).build();
	}

}
