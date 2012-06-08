package edu.stanford.bmir.protege.web.client.ui.portlet.bioportal;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.Position;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.data.XmlReader;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.DateField;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.FieldSet;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.MultiFieldPanel;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.RowSelectionModel;
import com.gwtext.client.widgets.grid.event.RowSelectionListenerAdapter;
import com.gwtext.client.widgets.layout.AnchorLayout;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.ColumnLayoutData;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.bioportal.BioportalProposalsManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class BioPortalProposalsPortlet extends AbstractEntityPortlet {

    private GridPanel grid;
    private Store store;
    private FormPanel formPanel;

    public BioPortalProposalsPortlet(Project project) {
        super(project);
    }

    @Override
    public void initialize() {
        BioportalProposalsManager.getBioportalProposalsManager();  // init

        setLayout(new AnchorLayout());

        grid = new GridPanel();
        XmlReader reader = new XmlReader("noteBean", new RecordDef(new FieldDef[]
                                            { new StringFieldDef("id"),
                                               new StringFieldDef("ontologyId"),
                                               new StringFieldDef("type"),
                                               new StringFieldDef("author"),
                                               new StringFieldDef("created"),
                                               new StringFieldDef("subject"),
                                               new StringFieldDef("body"),
                                               new StringFieldDef("values")}));

        store = new Store(reader);

        createColumns();

      //  grid.setHeight(200);
        grid.setStore(store);

        grid.setAutoWidth(true);
        grid.stripeRows(true);
        grid.setAutoExpandColumn("subject");
        grid.setAutoScroll(true);

        formPanel = new FormPanel();
        formPanel.setFrame(true);
        formPanel.setLabelAlign(Position.LEFT);
        formPanel.setPaddings(5);
        formPanel.setLabelWidth(50);

        FieldSet fieldSet = new FieldSet();
        fieldSet.setAutoHeight(true);
        fieldSet.setBorder(false);

        TextField author = new TextField("Author", "author");
        final DateField date = new DateField("Date", "created", 100);

        MultiFieldPanel mf1 = new MultiFieldPanel();
        mf1.addToRow(author, new ColumnLayoutData(0.5));
        mf1.addToRow(date, new ColumnLayoutData(0.5));
        formPanel.add(mf1, new AnchorLayoutData("100%"));

        TextField  type = new TextField("Type", "type");
        type.setReadOnly(true);

        ComboBox statusCb = new ComboBox();
        final Store statusStore = new SimpleStore("status", new Object[]{"Under review", "New term created", "Rejected",
                "Linked to existing term"});
        statusStore.load();
        statusCb.setFieldLabel("Status");
        statusCb.setStore(statusStore);
        statusCb.setDisplayField("status");

        MultiFieldPanel mf2 = new MultiFieldPanel();
        mf2.addToRow(type, new ColumnLayoutData(0.5));
        mf2.addToRow(statusCb, new ColumnLayoutData(0.5));
        formPanel.add(mf2, new AnchorLayoutData("100%"));

        TextArea body = new TextArea("Body", "body");
        //body.setHeight(200);
        formPanel.add(body, new AnchorLayoutData("100% -53"));


        final RowSelectionModel sm = new RowSelectionModel(true);
        sm.addListener(new RowSelectionListenerAdapter() {
            @Override
            public void onRowSelect(RowSelectionModel sm, int rowIndex, Record record) {
                formPanel.getForm().loadRecord(record);
                double dateval = record.getAsDouble("created");
                date.setValue(new Date((long) dateval));
            }
        });
        grid.setSelectionModel(sm);

        add(grid, new AnchorLayoutData("100% 30%"));
        add(formPanel, new AnchorLayoutData("100% 70%"));
    }


    protected void createColumns() {
        //setup column model
       // ColumnConfig authorCol = new ColumnConfig("Author", "author");

        ColumnConfig createdCol = new ColumnConfig("Date", "created");
        createdCol.setRenderer(new Renderer() {
            public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
                    Store store) {
                String created = record.getAsString("created");
                long createdLong = Long.parseLong(created);
                Date createdDate = new Date(createdLong);
                return createdDate.toString();
            }
        });

        ColumnConfig subjectCol = new ColumnConfig("Subject", "subject");
        subjectCol.setId("subject");

        //ColumnConfig bodyCol = new ColumnConfig("Body", "body");
        //ColumnConfig valuesCol = new ColumnConfig("Values", "values");

        ColumnConfig[] columnConfigs = {subjectCol, createdCol};

        ColumnModel columnModel = new ColumnModel(columnConfigs);
        columnModel.setDefaultSortable(true);

        grid.setColumnModel(columnModel);
    }

    @Override
    public void reload() {
        store.removeAll();
        Field[] fields = formPanel.getFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setValue(null);
        }

        EntityData entity = getEntity();
        if ( entity != null) {
           setTitle("BioPortal notes and proposals for " + UIUtil.getDisplayText(getEntity()) + "  (loading...)");
           BioportalProposalsManager.getBioportalProposalsManager().getBioportalProposals
                   (getProject().getProjectName(), entity.getName(), new GetBPProposalHandler());
        }
    }

    public Collection<EntityData> getSelection() {
        // TODO Auto-generated method stub
        return null;
    }


    /*
     * Remote calls
     */

    class GetBPProposalHandler extends AbstractAsyncHandler<String> {

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at retrieving proposals for " +getEntity(), caught);
            setTitle("Could not retrieve proposals for " + UIUtil.getDisplayText(getEntity()));
        }

        @Override
        public void handleSuccess(String notesXml) {
            setTitle("BioPortal notes and proposals for " + UIUtil.getDisplayText(getEntity()));
            store.loadXmlData(notesXml, true);
        }

    }

}
