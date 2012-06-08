package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

/**
 * This contains addition field "Result" used to notify the output when 
 * registering new user.
 * 
 * @author z.khan
 */
public class UserRegData extends UserData implements Serializable {
    private String result;

    public UserRegData() {
    }

    public UserRegData(String result) {
        this.result = result;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result
     *            the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

}
