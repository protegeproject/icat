package edu.stanford.bmir.protege.web.server;

public class EmailConstants {

    public static final String FORGOT_PASSWORD_SUBJECT = "Password reset for " + ApplicationProperties.getApplicationName();

    public static final String RESET_PASSWORD = "Wel2010come";
    public static final String FORGOT_PASSWORD_EMAIL_BODY =
        "Your password has been reset to: " + RESET_PASSWORD +
           "\n\nPlease change your password from the Options menu the next time you log into " +
              ApplicationProperties.getApplicationName()+ "." +
               "\n\n The " + ApplicationProperties.getApplicationName() + " administrator" ;

}
