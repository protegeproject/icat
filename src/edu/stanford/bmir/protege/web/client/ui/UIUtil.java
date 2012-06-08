package edu.stanford.bmir.protege.web.client.ui;

import com.google.gwt.user.client.Timer;
import com.gwtext.client.core.ExtElement;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.MessageBoxConfig;
import com.gwtext.client.widgets.WaitConfig;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class UIUtil {

	public static void showLoadProgessBar(final String message, final String barMessage) {
        MessageBox.show(new MessageBoxConfig() {  
            {  
                setMsg(message);  
                setProgressText(barMessage);
                setDefaultTextHeight(30);
                setWidth(300);                
                setWait(true);
                setClosable(true);
                setTitle("Dialog");
                setWaitConfig(new WaitConfig() { 
                    {
                    	setInterval(200);  
                    }  
                });  
                //setAnimEl(button.getId());  
            }  
        });		
	}
	
	public static void hideLoadProgessBar() {
        MessageBox.hide();        
	}
	
	public static void mask(final ExtElement el, final String message, final boolean animate, int delayInMilliSeconds) {
		Timer timer = new Timer() {
			public void run() {			
				el.mask(message, animate);
			}
		};
		timer.schedule(delayInMilliSeconds);
	}
	
	public static void unmask(ExtElement el) {
		el.unmask();
	}
	
	public static String getDisplayText(Object object) {
		if (object instanceof EntityData) {
			String browserText = ((EntityData) object).getBrowserText();
			if (browserText == null) {
				browserText = ((EntityData) object).getName();
			}
			return browserText;
		} else {
			return object.toString();
		}
	}
	
}