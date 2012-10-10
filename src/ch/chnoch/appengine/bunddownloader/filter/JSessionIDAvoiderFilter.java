package ch.chnoch.appengine.bunddownloader.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class JSessionIDAvoiderFilter implements Filter {

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		boolean allowFilterChain = redirectToAvoidJsessionId(
				(HttpServletRequest) req, (HttpServletResponse) res);

		// I'm doing this because if I execute the request completely, it will
		// perform a pretty heavy lookup operation. No need to do it twice.
		if (allowFilterChain)
			chain.doFilter(req, res);
	}

	public static boolean redirectToAvoidJsessionId(HttpServletRequest req,
			HttpServletResponse res) {
		HttpSession s = req.getSession();
//		if (s.isNew()) {

			// i'm not quite sure whether this is safe. A user without cookies
			// should never be allowed on this application in the first place,
			// but a endless loop at this place could lead to a 'pants down' of
			// several important applications
			if (!(req.isRequestedSessionIdFromCookie() && req
					.isRequestedSessionIdFromURL())) {
				// yeah we have request parameters actually on that request.
				String qs = req.getQueryString();

				String requestURI = req.getRequestURI();
				requestURI = requestURI.split(";")[0];
				try {
					res.sendRedirect(requestURI + "?" + qs);
					return false;
				} catch (IOException e) {
					// logger.error("Error sending redirect. " +
					// e.getMessage());
				}
			}
//		}
		return true;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		int i= 2;
	}
}