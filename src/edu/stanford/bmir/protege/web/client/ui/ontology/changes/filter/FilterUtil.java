/**
 * 
 */
package edu.stanford.bmir.protege.web.client.ui.ontology.changes.filter;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.DatePicker;
import com.gwtext.client.widgets.PagingToolbar;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.event.DatePickerListenerAdapter;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;
import com.gwtext.client.widgets.grid.GridPanel;

/**
 * @author z.khan
 * 
 */
public class FilterUtil {

    private ComboBox filterTypeComboBox;

    private ToolbarButton cancelButton;
    private ToolbarButton applyFilterButton;
    private FlexTable filterInputFlex;
    private TextBox filterInputTextBox = new TextBox();
    protected final TextBox toDateText = new TextBox();
    protected final TextBox fromDateText = new TextBox();
    protected DatePicker fromDatePicker = new DatePicker();
    protected DatePicker toDatePicker = new DatePicker();
    protected PopupPanel fromDatePopupPanel;
    protected PopupPanel toDatePopupPanel;
    protected Store store;
    protected GridPanel changesGrid;

    /**
     * Adds filter widget to top toolbar
     * 
     * @param proxy
     * @param store2
     */
    public void addFilterWidgetToToolbar(Toolbar toolbar, Store store, GridPanel changesGrid) {
        this.store = store;
        this.changesGrid = changesGrid;

        filterTypeComboBox = new ComboBox();
        cancelButton = createCancelButton();
        applyFilterButton = createApplyFilterButton();

        Object[][] filterTypeStates = new Object[][] {
                new Object[] { ChangesFilterData.filterTypeList[0], ChangesFilterData.filterTypeList[0] },
                new Object[] { ChangesFilterData.filterTypeList[1], ChangesFilterData.filterTypeList[1] },
                new Object[] { ChangesFilterData.filterTypeList[2], ChangesFilterData.filterTypeList[2] }, };

        final Store filterTypeStore = new SimpleStore(new String[] { "abbr", "filterType" }, filterTypeStates);

        filterTypeComboBox.setStore(filterTypeStore);
        filterTypeComboBox.setDisplayField("filterType");
        filterTypeComboBox.setValue("By author");

        filterInputFlex = new FlexTable();
        toolbar.addText("Filter ");
        toolbar.addElement(filterTypeComboBox.getElement());

        toolbar.addElement(filterInputFlex.getElement());
        toolbar.addButton(cancelButton);

        toolbar.addSpacer();
        toolbar.addButton(applyFilterButton);

        addFilterTypeChangeListener();
        updateFilterInputPanel();
    }

    /**
     * Adds listener to filter type listbox and updates the filter input panel.
     */
    private void addFilterTypeChangeListener() {

        filterTypeComboBox.addListener(new ComboBoxListenerAdapter() {

            @Override
            public void onSelect(ComboBox comboBox, Record record, int index) {
                updateFilterInputPanel();
            }

        });
    }

    /**
     * Updated the filter input panel depending on the fiter type selected
     */
    private void updateFilterInputPanel() {

        String filterType = filterTypeComboBox.getValue();
        ChangesFilterData.filterType = filterType;
        if (filterType.equals(ChangesFilterData.filterTypeList[0]) || filterType.equals(ChangesFilterData.filterTypeList[2])) { // By author or by description 

            filterInputFlex.clear();
            filterInputFlex.getFlexCellFormatter().setWidth(0, 0, "10px");
            filterInputFlex.setWidget(0, 1, filterInputTextBox);

        } else if (filterType.equals(ChangesFilterData.filterTypeList[1])) { // By date
            filterInputFlex.clear();

            final ToolbarButton selectFromDateButton = new ToolbarButton();
            selectFromDateButton.setIcon(GWT.getHostPageBaseURL() + "images/portlet/calendar.jpg");
            selectFromDateButton.addListener(new ButtonListenerAdapter() {

                @Override
                public void onClick(Button button, EventObject e) {

                    fromDatePopupPanel = new PopupPanel(true);

                    fromDatePopupPanel.add(fromDatePicker);
                    fromDatePopupPanel.setPopupPosition(selectFromDateButton.getAbsoluteLeft(), selectFromDateButton
                            .getAbsoluteTop()
                            + selectFromDateButton.getOffsetHeight());
                    fromDatePopupPanel.show();
                }

            });

            addFromDateListener(fromDatePicker, fromDateText);

            final ToolbarButton selectToDateButton = new ToolbarButton();
            selectToDateButton.setIcon(GWT.getHostPageBaseURL() + "images/portlet/calendar.jpg");
            selectToDateButton.addListener(new ButtonListenerAdapter() {

                @Override
                public void onClick(Button button, EventObject e) {
                    toDatePopupPanel = new PopupPanel(true);
                    toDatePopupPanel.add(toDatePicker);
                    toDatePopupPanel.setPopupPosition(selectToDateButton.getAbsoluteLeft(), selectToDateButton.getAbsoluteTop()
                            + selectToDateButton.getOffsetHeight());
                    toDatePopupPanel.show();
                }

            });

            addToDateListener(toDatePicker, toDateText);
            filterInputFlex.getFlexCellFormatter().setWidth(0, 0, "10px");
            filterInputFlex.setWidget(0, 1, new Label(" From "));
            filterInputFlex.setWidget(0, 2, fromDateText);
            filterInputFlex.setWidget(0, 3, selectFromDateButton);
            filterInputFlex.setWidget(0, 4, new Label(" To "));
            filterInputFlex.setWidget(0, 5, toDateText);
            filterInputFlex.setWidget(0, 6, selectToDateButton);
        }
    }

    /**
     * @param dateTextBox
     * @param datePopupPanel
     */
    private void addFromDateListener(DatePicker datePicker, final TextBox dateTextBox) {
        datePicker.addListener(new DatePickerListenerAdapter() {

            @Override
            public void onSelect(com.gwtext.client.widgets.DatePicker dataPicker, Date date) {
                ChangesFilterData.fromDate = date;
                String dateString = DateTimeFormat.getFormat("d MMMM yyyy").format(date);
                dateTextBox.setText(dateString);
                fromDatePopupPanel.hide();
            }
        });
    }

    /**
     * @param dateTextBox
     * @param datePopupPanel
     */
    private void addToDateListener(DatePicker datePicker, final TextBox dateTextBox) {
        datePicker.addListener(new DatePickerListenerAdapter() {

            @Override
            public void onSelect(com.gwtext.client.widgets.DatePicker dataPicker, Date date) {
                ChangesFilterData.toDate = date;
                String dateString = DateTimeFormat.getFormat("d MMMM yyyy").format(date);
                dateTextBox.setText(dateString);
                toDatePopupPanel.hide();
            }
        });
    }

    protected Component createSearchField() {
        final TextField filterInputTextBox = new TextField("", "filter text");
        filterInputTextBox.setAutoWidth(true);
        filterInputTextBox.setEmptyText("flter text");
        return filterInputTextBox;
    }

    protected ToolbarButton createCancelButton() {
        cancelButton = new ToolbarButton("Cancel");
        cancelButton.setCls("toolbar-button");
        cancelButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                fromDateText.setText("");
                toDateText.setText("");
                filterInputTextBox.setText("");
            }
        });
        return cancelButton;
    }

    protected ToolbarButton createApplyFilterButton() {
        applyFilterButton = new ToolbarButton("Go");
        applyFilterButton.setCls("toolbar-button");
        applyFilterButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                updateFilterData();
                store.removeAll();
                PagingToolbar pToolbar = (PagingToolbar) changesGrid.getBottomToolbar();
                store.load(0, pToolbar.getPageSize());
                changesGrid.getView().refresh();
            }

        });
        return applyFilterButton;
    }

    private void updateFilterData() {
        String filterType = filterTypeComboBox.getValue();
        ChangesFilterData.filterInputText = filterType;

        if (filterType.equals(ChangesFilterData.filterTypeList[0]) || filterType.equals(ChangesFilterData.filterTypeList[2])) { // By author or by description
            ChangesFilterData.filterInputText = filterInputTextBox.getText();
        }
        // when filter type is 'by date' then to and from dates are already updated in the respt. date picker listener.
        ChangesFilterData.isFilterDataCurrent = true;
    }

}
