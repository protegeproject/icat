/**
 *
 */
package edu.stanford.bmir.protege.web.server.openid;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegResponse;

import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.bmir.protege.web.client.ui.openid.constants.OpenIdConstants;
import edu.stanford.smi.protege.util.Log;

/**
 * @author z.Khan
 *
 */
public class OpenIdAuthenticationResponseServlet extends HttpServlet {

    private static final long serialVersionUID = 8089333325493540320L;

    @Override
    public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException,
            IOException {
        try {
            HttpSession httpSession = httpRequest.getSession(false);
            ConsumerManager manager = (ConsumerManager) httpSession.getAttribute("manager");

            ParameterList openidResp = new ParameterList(httpRequest.getParameterMap());

            String openIdIdentity = httpRequest.getParameter("openid.identity");
            DiscoveryInformation discovered = (DiscoveryInformation) httpSession.getAttribute("discovered");

            StringBuffer receivingURL = httpRequest.getRequestURL();
            String queryString = httpRequest.getQueryString();
            if (queryString != null && queryString.length() > 0) {
                receivingURL.append("?").append(httpRequest.getQueryString());
            }
            // verify the response
            VerificationResult verification = null;

            verification = manager.verify(receivingURL.toString(), openidResp, discovered);
            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

                HttpSession session = httpRequest.getSession(true);
                session.setAttribute("openid_identifier", authSuccess.getIdentity());
                String emailId = null;
                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
                    emailId = (String) fetchResp.getAttributeValues("email").get(0);

                    session.setAttribute("emailFromFetch", emailId);
                }

                if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                    SRegResponse sregResp = (SRegResponse) authSuccess.getExtension(SRegMessage.OPENID_NS_SREG);
                    emailId = sregResp.getAttributeValue("email");
                }

                if (emailId != null) {
                    httpSession.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_ID, emailId);
                } else {
                    httpSession.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_ID, openIdIdentity);
                }

                httpSession.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_URL, verified.getIdentifier());

                Log.getLogger().info("OpenId login for: " + emailId);

            } else {
                httpSession.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_ID, null);
                httpSession.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_PROVIDER, null);
                httpSession.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_URL, null);
            }
            httpSession.setAttribute(AuthenticationConstants.LOGIN_METHOD,  AuthenticationConstants.LOGIN_METHOD_OPEN_ID_ACCOUNT);

            httpResponse.setContentType("text/html");
            PrintWriter out = httpResponse.getWriter();
            out.println("<html>");
            out.println("<body onLoad='window.close();'>");
            out.println("</html>");
            out.close();

        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error in the OpenId authentication servlet: " + e.getMessage(), e);
        }
    }
}
