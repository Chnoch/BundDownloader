/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ch.chnoch.appengine.bunddownloader;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;


/**
 * Servlet to check that the current user is authorized and to serve the start
 * page for DrEdit.
 * 
 * @author vicfryzel@google.com (Vic Fryzel)
 * @author nivco@google.com (Nicolas Garnier)
 */
@SuppressWarnings("serial")
public class StartPageServlet extends BundDownloaderServlet {
	/**
	 * Ensure that the user is authorized, and setup the required values for
	 * index.jsp.
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		// Making sure the code gets processed
		try {
			String id = getClientId(req, resp);
			req.setAttribute("client_id", new Gson().toJson(id));
		} catch (RuntimeException e) {
			return;
		}
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity subscriber = new Entity("Subscriber");
		subscriber.setProperty("userId", getClientId(req, resp));
		subscriber.setProperty("date", new Date());
		datastore.put(subscriber);
		
		resp.sendRedirect("/success.jsp");
	}
}