/**
 * 
 */
package edu.stanford.bmir.protege.web.client.ui.ontology.changes.filter;

import java.util.Date;

/**
 * Contains data for portlet Changes filter
 * 
 * @author z.khan
 * 
 */
public class ChangesFilterData {

    public static String[] filterTypeList = new String[] { "By author", "By date", "By description" };

    public static String filterType;

    public static String filterInputText;

    public static Date toDate;

    public static Date fromDate;

    /**
     * true if the apply filter button is currently selected by user, set to
     * false when the data is displayed
     */
    public static boolean isFilterDataCurrent;

}
