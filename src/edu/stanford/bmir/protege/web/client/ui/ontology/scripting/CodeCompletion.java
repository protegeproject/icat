package edu.stanford.bmir.protege.web.client.ui.ontology.scripting;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class CodeCompletion {
	
	private PopupPanel popupPanel; 
	
	private CellListResource resource = GWT.create(CellListResource.class);
	
	private MenuCell contextMenuItem = new MenuCell();
	private CellList<String> contextMenuItems = new CellList<String>(contextMenuItem, resource);
	
	private SingleSelectionModel<String> contextMenuSelectionModel = new SingleSelectionModel<String>();
	
	private SelectionChangeEvent.Handler selectionChangedHandler;
	
	private String searchStr;
	
	private Callback<String> callbackOnSelection;
	
	
	public CodeCompletion(Callback<String> callback) {
		this.callbackOnSelection = callback;
		
		VerticalPanel vp = new VerticalPanel();
		vp.add(contextMenuItems);

		popupPanel = new PopupPanel(true);
		popupPanel.add(vp);
		
		popupPanel.setAnimationEnabled(false);
		popupPanel.setGlassEnabled(false);
		popupPanel.hide();
		popupPanel.setStyleName("contextMenu");
		
		contextMenuItems.setSelectionModel(contextMenuSelectionModel);
		contextMenuSelectionModel.addSelectionChangeHandler(createSelectionChangeHandler());
	}
	
	private SelectionChangeEvent.Handler createSelectionChangeHandler() {
		selectionChangedHandler = new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				String selection = contextMenuSelectionModel.getSelectedObject();
				if (searchStr != null && searchStr.length() > 0) {
					selection = selection.replaceAll(searchStr, "");
				}
				popupPanel.hide();
				callbackOnSelection.onDone(selection);
			}
		};
		return selectionChangedHandler;
	}
	
	public void displayCodeCompletionResults(List<String> results, String searchStr, int x, int y) {
		if (isVisible()) {
			popupPanel.hide();
		}
		
		this.searchStr = searchStr; 
		
		contextMenuItems.setRowCount(results.size(), true);
		contextMenuItems.setRowData(0, results);

		if (results.size() == 0) {
			return;
		}
		
		popupPanel.setPopupPositionAndShow(new PositionCallback() {
			
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
				popupPanel.getElement().getStyle().setProperty("overflow", "auto");
				popupPanel.setPopupPosition(x, y);
				
			}
		});
	}
	
	
	public boolean isVisible() {
		return popupPanel.isVisible();
	}
	
	public void hide() {
		popupPanel.hide();
	}
	
	
	/***** classes ******/
	
	class MenuCell extends AbstractCell<String> {

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
			if (searchStr != null && searchStr.length() > 0) {
				value = value.replaceAll(searchStr, "<b>" + searchStr + "</b>");
			}
			SafeHtml html = SafeHtmlUtils.fromSafeConstant("<div class='cell'> " + value + "</div>");
			sb.append(html);

		}
	}

	public interface CellListResource extends CellList.Resources {
		@Source({ "myCellList.css" })
		CellList.Style cellListStyle();
	}
	
}
