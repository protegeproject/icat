package edu.stanford.bmir.protege.web.client.model;

public class PermissionConstants {

    public static final String READ = "Read";
    public static final String WRITE = "Write";

    public static final String CREATE_CLS = "CreateClass";
    public static final String MOVE_CLS = "MoveClass";
    public static final String RETIRE_CLS = "RetireClass";
    public static final String DELETE_CLS = "DeleteClass";
    public static final String EDIT_CLASS_RESTRICTIONS = "EditClassRestrictions";
    public static final String COMMENT = "Comment";

    public static final String CREATE_USERS = "CreateUsers";
    
    //Must match the edu.stanford.bmir.protege.web.server.AbstractMetaProjectManager.SIGN_IS_AS_OP
    public static final String SIGN_IN_AS = "SignInAs";
    
    //Name must match: edu.stanford.smi.protege.server.metaproject.OPERATION_ADMINISTER_SERVER
    public static final String ADMINISTER_SERVER = "AdministerServer";
    
    //Name must match: edu.stanford.bmir.protege.web.server.scripting.ScriptingServiceImpl.RUN_PYTHON_SCRIPT
    public static final String RUN_PYTHON_SCRIPT = "RunPythonScript";

}
