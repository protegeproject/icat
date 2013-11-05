package edu.stanford.bmir.protege.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.AdminService;
import edu.stanford.bmir.protege.web.client.rpc.data.LoginChallengeData;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.client.ui.login.constants.AuthenticationConstants;
import edu.stanford.bmir.protege.web.client.ui.openid.constants.OpenIdConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protegex.owl.jena.export.JenaExportPlugin;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.storage.OWLKnowledgeBaseFactory;

/**
 * Administrative services for user management
 *
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class AdminServiceImpl extends RemoteServiceServlet implements AdminService {

    private static final long serialVersionUID = 7616699639338297327L;


    public UserData getCurrentUserInSession() {
        HttpServletRequest request = getThreadLocalRequest();
        final HttpSession session = request.getSession();
        final UserData userData = (UserData) session.getAttribute(AuthenticationConstants.USERDATA_OBJECT);
        return userData;
    }


    public void logout() {
        HttpServletRequest request = getThreadLocalRequest();
        final HttpSession session = request.getSession();
        UserData userData = (UserData) session.getAttribute(AuthenticationConstants.USERDATA_OBJECT);
        String userName = userData == null ? null : userData.getName();
        Log.getLogger().info("User " + userName + " logged out on " + new Date());
        session.setAttribute(AuthenticationConstants.USERDATA_OBJECT, null);
    }

    public void changePassword(String userName, String password) {
        Protege3ProjectManager.getProjectManager().getMetaProjectManager().changePassword(userName, password);
    }

    public String getUserEmail(String userName) {
        return Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUserEmail(userName);
    }

    public void setUserEmail(String userName, String email) {
        Protege3ProjectManager.getProjectManager().getMetaProjectManager().setUserEmail(userName, email);
    }

    public ArrayList<ProjectData> getProjects(String user) {
        return Protege3ProjectManager.getProjectManager().getMetaProjectManager().getProjectsData(user);
    }

    public List<Collection<String>> getAllowedOperations(String project, String user) {
        Collection<Operation> allDefinedOps = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getDefinedOperations();
        Collection<Operation> allowedOps = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getAllowedOperations(project, user);
        List<Collection<String>> returnList = new ArrayList<Collection<String>>();
        returnList.add(getOperationsAsString(allowedOps));
        returnList.add(getOperationsAsString(allDefinedOps));
        return returnList;
    }


    public Collection<String> getAllowedServerOperations(String userName) {
        Collection<Operation> ops = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getAllowedServerOperations(userName);
        return getOperationsAsString(ops);
    }

    public Collection<String> getOperationsAsString(Collection<Operation> ops) {
        Collection<String> opsAsString = new ArrayList<String>();
        for (Operation op : ops) {
            opsAsString.add(op.getName());
        }
        return opsAsString;
    }


    public void refreshMetaproject() {
        Protege3ProjectManager.getProjectManager().getMetaProjectManager().reloadMetaProject();
    }

    public void sendPasswordReminder(String userName) {
        String email = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUserEmail(userName);
        if (email == null) {
            throw new IllegalArgumentException("User " + userName + " does not have an email configured.");
        }
        changePassword(userName, EmailConstants.RESET_PASSWORD);
        EmailUtil.sendEmail(email, EmailConstants.FORGOT_PASSWORD_SUBJECT, EmailConstants.FORGOT_PASSWORD_EMAIL_BODY,
                ApplicationProperties.getEmailAccount());
    }

    public LoginChallengeData getUserSaltAndChallenge(String userNameOrEmail) {
        User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUser(userNameOrEmail);
        if (user == null) {
            return null;
        }
        String userSalt = user.getSalt();
        if (userSalt == null) {
            return null;
        }
        String encodedChallenge = generateSalt();
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        session.setAttribute(AuthenticationConstants.LOGIN_CHALLENGE, encodedChallenge);
        return new LoginChallengeData(userSalt, encodedChallenge);
    }

    public boolean allowsCreateUsers() {
        return Protege3ProjectManager.getProjectManager().getMetaProjectManager().allowsCreateUser();
    }

    public UserData authenticateToLogin(String userNameOrEmail, String response) {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        String challenge = (String) session.getAttribute(AuthenticationConstants.LOGIN_CHALLENGE);
        session.setAttribute(AuthenticationConstants.LOGIN_CHALLENGE, null);

        //Will check both user name and email
        User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUser(userNameOrEmail);
        if (user == null) { //user not in metaproject
            return null;
        }

        UserData userData = null;

        AuthenticationUtil authenticatinUtil = new AuthenticationUtil();
        boolean isverified = authenticatinUtil.verifyChallengedHash(user.getDigestedPassword(), response, challenge);
        if (isverified) {
            userData = AuthenticationUtil.createUserData(user.getName());
            session.setAttribute(AuthenticationConstants.USERDATA_OBJECT, userData);
            Log.getLogger().info("User " + user.getName() + " logged in on " + new Date());
        }

        return userData;
    }

    private static String encodeBytes(byte[] bytes) {
        int stringLength = 2 * bytes.length;
        BigInteger bi = new BigInteger(1, bytes);
        String encoded = bi.toString(16);
        while (encoded.length() < stringLength) {
            encoded = "0" + encoded;
        }
        return encoded;
    }

    private String generateSalt() {
        byte[] salt = new byte[8];
        Random random = new Random();
        random.nextBytes(salt);
        String encodedSalt = encodeBytes(salt);
        return encodedSalt;
    }

    public String checkUserLoggedInMethod() {
        String loginMethod = null;
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession sess = request.getSession();
        loginMethod = (String) sess.getAttribute(AuthenticationConstants.LOGIN_METHOD);
        return loginMethod;
    }

    public void clearPreviousLoginAuthenticationData() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        session.setAttribute(AuthenticationConstants.USERDATA_OBJECT, null);
        session.setAttribute(AuthenticationConstants.LOGIN_METHOD, null);
        session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_ID, null);
        session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_PROVIDER, null);
        session.setAttribute(OpenIdConstants.HTTPSESSION_OPENID_URL, null);
    }

    public boolean changePasswordEncrypted(String userName, String encryptedPassword, String salt) {
        User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUser(userName);
        if (user == null) {
            return false;
        }
        user.setDigestedPassword(encryptedPassword, salt);
        return true;
    }

    public String getNewSalt() {
        Random random = new Random();
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        String newSalt = encodeBytes(salt);
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        session.setAttribute(AuthenticationConstants.NEW_SALT, newSalt);
        return newSalt;
    }

    //used only for https
    public UserData registerUser(String userName, String password) {
        return Protege3ProjectManager.getProjectManager().getMetaProjectManager().registerUser(userName, password);
    }

    public UserData registerUserViaEncrption(String name, String hashedPassword, String emailId) {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        String salt = (String) session.getAttribute(AuthenticationConstants.NEW_SALT);
        String emptyPassword = "";

        UserData userData = Protege3ProjectManager.getProjectManager().getMetaProjectManager() .registerUser(name, emptyPassword);

        if (userData == null) {
            return null;
        }

        User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUser(name);
        user.setDigestedPassword(hashedPassword, salt);
        user.setEmail(emailId);

        return userData;
    }

    public String download(String projectName) {
        Project project = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);

        if (project == null) {
            return null;
        }

        //TODO: support for downloading frames ontologies will be added later, if necessary
        if (project.getKnowledgeBase().getKnowledgeBaseFactory() instanceof OWLKnowledgeBaseFactory == false) {
            return null;
        }

        String fileName = getExportedFileName(projectName);
        String exportDirectory = ApplicationProperties.getDownloadServerPath();
        final String exportFilePath = exportDirectory + fileName;
        String downloadedFile = exportFilePath + ".zip";

        Log.getLogger().info("Started download of " + projectName + " on " + new Date() + " by user " + KBUtil.getUserInSession(getThreadLocalRequest()));
        long t0 = System.currentTimeMillis();

        try {
            JenaExportPlugin exportPlugin = new JenaExportPlugin();
            Collection errors = new ArrayList();
            exportPlugin.exportWithNativeWriter((OWLModel)project.getKnowledgeBase(), URIUtilities.createURI(exportFilePath), errors);
            if (errors.size() > 0) {
                Log.getLogger().warning("There were errors or warnings at exporting " + projectName + " to OWL.");
            }
            createZipArchive(exportFilePath, downloadedFile);
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Download of " + projectName + " failed.", e);
            throw new RuntimeException("Download of " + projectName + " failed.");
        }

        Log.getLogger().info("Download of " + projectName +" took " + (System.currentTimeMillis() - t0)/1000 + " seconds.");
        return getSimpleFileName(downloadedFile);
    }

    private String getExportedFileName(String projectName) {
        StringBuffer fileName = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HHmmss");
        final String formattedDate = sdf.format(new Date());

        fileName.append(projectName);
        fileName.append("_");
        fileName.append(formattedDate);
        fileName.append(".owl");

        return fileName.toString();
    }

    private void createZipArchive(String source, String target) {
        byte[] buf = new byte[1024];

        File targetFile = new File(target);

        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFile));
            FileInputStream in = new FileInputStream(source);
            out.putNextEntry(new ZipEntry(getSimpleFileName(source)));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
            out.close();
        } catch (IOException e) {
            Log.getLogger().log(Level.WARNING, "Could not create zip file: " + target, e);
            throw new RuntimeException("Could not create zip file: " + target, e);
        }
    }


    private String getSimpleFileName(String filePath) {
        return new File(filePath).getName();
    }
}
