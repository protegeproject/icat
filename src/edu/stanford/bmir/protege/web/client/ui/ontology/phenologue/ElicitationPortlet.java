package edu.stanford.bmir.protege.web.client.ui.ontology.phenologue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBox;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.layout.HorizontalLayout;
import com.gwtext.client.widgets.layout.VerticalLayout;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.SWRLParaphrasingServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.SelectionEvent;
import edu.stanford.bmir.protege.web.client.util.StringTokenizer;

/**
 * @author Saeed
 * 
 */
public class ElicitationPortlet extends AbstractEntityPortlet {

	public static String txt;
	public static String sig;
	public static String[] SWRLClassNames;
	public static String[] SWRLIndvNames;
	public static String[] SWRLDataValNames;
	public static String[] SWRLBuiltinNames;
	public int counter;
	public boolean ifPart;
	public String addResult;
	Panel mainPanel;

	public HashMap<Integer, ArrayList<String>> bodyMap;
	public HashMap<Integer, ArrayList<String>> headMap;

	public ElicitationPortlet(Project project) {
		super(project);
		// TODO Auto-generated constructor stub
	}

	public ElicitationPortlet(Project project, String txt) {
		super(project);
		this.txt = txt;
	}

	@Override
	public void intialize() {

		mainPanel = new Panel();
		mainPanel.setBorder(false);
		mainPanel.setLayout(new VerticalLayout());
		bodyMap = new HashMap<Integer, ArrayList<String>>();
		headMap = new HashMap<Integer, ArrayList<String>>();
		String[] lines;
		counter = 0;
		ifPart = true;
		final String projectName = this.project.getProjectName();

		if (txt != null) {
			lines = txt.split("#");
			for (int i = 0; i < lines.length; i++) {
				GridLine myGridLine = new GridLine(lines[i]);
				mainPanel.add(myGridLine);
			}
		}

		Toolbar toolbar = new Toolbar();

		ToolbarButton button = new ToolbarButton("Preview",
				new ButtonListenerAdapter() {
					public void onClick(Button button, EventObject e) {
						SWRLWindow window = new SWRLWindow(makeSWRL(bodyMap,
								headMap));
						window.setAnimateTarget(button.getId());
						window.show();
					}
				});

		final TextField name = new TextField("Name: ", "name", 100);
		name.setEmptyText("Rule's name");
		name.autoSize();

		ToolbarButton addRule = new ToolbarButton("Add",
				new ButtonListenerAdapter() {
					public void onClick(Button button, EventObject e) {

						String rule = makeSWRL(bodyMap, headMap);
						String rname = name.getValueAsString();
						rule = rule.replaceAll("<br>", " ");
						System.out.println("Name CLIENT = " + rname);
						System.out.println("Rule CLIENT = " + rule);
						SWRLParaphrasingServiceManager.getInstance()
								.addSWRLRule(projectName, rule, rname, sig,
										new AddSWRLRule());

						// notifySelectionListeners(new SelectionEvent(
						// ElicitationPortlet.this));

					}
				});

		toolbar.addFill();
		toolbar.addButton(button);
		toolbar.addButton(addRule);
		toolbar.addElement(name.getElement());
		mainPanel.setBottomToolbar(toolbar);

		mainPanel.setAutoHeight(true);
		mainPanel.setAutoWidth(true);
		mainPanel.setAutoScroll(true);
		this.add(mainPanel);

	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

//	@Override
//	public Collection<EntityData> getSelection() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public class GridLine extends Panel {
		public String txt;

		public GridLine(String txt) {
			this.txt = txt;

			this.setLayout(new HorizontalLayout(0));
			this.setHeight("20px");

			if (txt != null && !txt.equals("")) {
				while (txt.charAt(0) == '@') {
					TextBox tb = new TextBox();
					tb.setWidth("150px");
					tb.setReadOnly(true);
					this.add(tb);
					txt = txt.substring(1);
				}
				generate(txt);
			}
		}

		private void generate(String txt) {
			String[] words;
			String term;
			txt = txt.trim();
			txt = txt.replaceAll("\"\'", "\'<");
			txt = txt.replaceAll("\'\"", ">\'");

			if (txt == null || txt.equals("")) {
				System.out.println("Term is empty.");

			} else if (txt.startsWith("AND")) {
				System.out.println("STARTS WITH AND");
				TextBox tb = new TextBox();
				tb.setWidth("150px");
				tb.setText("AND");
				tb.setReadOnly(true);
				this.add(tb);
				txt = txt.substring(4);
				generate(txt);

			} else if (txt.contains("WHERE")) {
				String[] Wtemp = txt.split("WHERE");

				for (int i = 0; i < Wtemp.length - 1; i++) {
					generate(Wtemp[i]);

					TextBox tb = new TextBox();
					tb.setWidth("150px");
					tb.setText("WHERE");
					tb.setReadOnly(true);
					this.add(tb);
				}
				generate(Wtemp[Wtemp.length - 1]);

			} else if (txt.startsWith("THEN")) {
				System.out.println("THEN PART");
				words = mySplit(txt);
				TextBox tb = new TextBox();
				tb.setWidth("150px");
				tb.setText("THEN");
				tb.setReadOnly(true);
				this.add(tb);
				ifPart = false;

			} else if (txt.startsWith("FOR EACH")) {
				System.out.println("CREATE OWLTHING");
				words = mySplit(txt);

				TextBox tb0 = new TextBox();
				tb0.setWidth("150px");
				tb0.setText("FOR EACH");
				tb0.setReadOnly(true);
				this.add(tb0);

				final IndexedTextBox tb1 = new IndexedTextBox(counter, 1, false);
				tb1.setWidth("150px");
				tb1.setText(words[2]);
				this.add(tb1);
				tb1.addKeyPressHandler(new KeyPressHandler() {
					public void onKeyPress(KeyPressEvent event) {
						if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
							ArrayList tmp = headMap.get(tb1.index);
							tmp.set(tb1.offset, tb1.getValue());
							System.out.println(tb1.getValue());
						}
					}

				});

				TextBox tb2 = new TextBox();
				tb2.setWidth("150px");
				tb2.setText("THERE IS " + words[5]);
				tb2.setReadOnly(true);
				this.add(tb2);

				final IndexedTextBox tb3 = new IndexedTextBox(counter, 2, false);
				tb3.setWidth("150px");
				tb3.setText(words[6]);
				this.add(tb3);
				tb3.addKeyPressHandler(new KeyPressHandler() {
					public void onKeyPress(KeyPressEvent event) {
						if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
							ArrayList tmp = headMap.get(tb3.index);
							tmp.set(tb3.offset, tb3.getValue());
							System.out.println(tb3.getValue());
						}
					}

				});

				TextBox tb4 = new TextBox();
				tb4.setWidth("150px");
				tb4.setText("SUCH THAT");
				tb4.setReadOnly(true);
				this.add(tb4);

				ArrayList<String> args = new ArrayList<String>();
				args.add("swrlx:createOWLThing");
				args.add(words[6]);
				args.add(words[2]);
				bodyMap.put(counter, args);

				counter++;

			} else if (txt.contains("!1!")) {
				System.out.println("CLASS!!");
				txt = txt.replaceAll("!1!", "");
				words = mySplit(txt);
				if (words.length != 4) {
					System.out.println("Error in CLASS syntax.");
				} else {
					final IndexedTextBox tb0 = new IndexedTextBox(counter, 1,
							ifPart);
					tb0.setWidth("150px");
					tb0.setText(words[0]);
					this.add(tb0);

					tb0.addKeyPressHandler(new KeyPressHandler() {
						public void onKeyPress(KeyPressEvent event) {
							if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
								ArrayList tmp;
								if (tb0.ifp)
									tmp = bodyMap.get(tb0.index);
								else
									tmp = headMap.get(tb0.index);
								tmp.set(tb0.offset, tb0.getValue());
								System.out.println(tb0.getValue());
							}
						}

					});

					TextBox tb1 = new TextBox();
					tb1.setWidth("150px");
					tb1.setText(words[1] + " " + words[2]);
					tb1.setReadOnly(true);
					this.add(tb1);

					term = words[3];
					final ClassCombo myClassCombo = new ClassCombo(term,
							counter, ifPart);
					this.add(myClassCombo);

					myClassCombo.addListener(new ComboBoxListenerAdapter() {
						public void onChange(Field field, Object newVal,
								Object oldVal) {
							ArrayList tmp;
							if (myClassCombo.ifp)
								tmp = bodyMap.get(myClassCombo.index);
							else
								tmp = headMap.get(myClassCombo.index);
							tmp.set(0, newVal);
							System.out.println("ComboBox::onChange(" + oldVal
									+ "-->" + newVal + ")");
						}
					});

					ArrayList<String> args = new ArrayList<String>();
					args.add(term);
					args.add(words[0]);

					if (ifPart)
						bodyMap.put(counter, args);
					else
						headMap.put(counter, args);

					counter++;

				}

			} else if (txt.contains("!2!")) {
				System.out.println("OBJECT PROPERTY!!");
				txt = txt.replaceAll("!2!", "");
				words = mySplit(txt);
				if (words.length != 3) {
					System.out.println("Error in Object Property syntax."
							+ words.length);
				} else {
					final IndexedTextBox tb0 = new IndexedTextBox(counter, 1,
							ifPart);
					tb0.setWidth("150px");
					tb0.setText(words[0]);
					this.add(tb0);

					tb0.addKeyPressHandler(new KeyPressHandler() {
						public void onKeyPress(KeyPressEvent event) {
							if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
								ArrayList tmp;
								if (tb0.ifp)
									tmp = bodyMap.get(tb0.index);
								else
									tmp = headMap.get(tb0.index);
								tmp.set(tb0.offset, tb0.getValue());
								System.out.println(tb0.getValue());
							}
						}

					});

					term = words[1];
					final IndvCombo myIndvCombo = new IndvCombo(term, counter,
							ifPart);
					this.add(myIndvCombo);
					myIndvCombo.addListener(new ComboBoxListenerAdapter() {
						public void onChange(Field field, Object newVal,
								Object oldVal) {
							ArrayList tmp;
							if (myIndvCombo.ifp)
								tmp = bodyMap.get(myIndvCombo.index);
							else
								tmp = headMap.get(myIndvCombo.index);
							tmp.set(0, newVal);
							System.out.println("ComboBox::onChange(" + oldVal
									+ "-->" + newVal + ")");
						}
					});

					final IndexedTextBox tb1 = new IndexedTextBox(counter, 2,
							ifPart);
					tb1.setWidth("150px");
					tb1.setText(words[2]);
					this.add(tb1);
					tb1.addKeyPressHandler(new KeyPressHandler() {
						public void onKeyPress(KeyPressEvent event) {
							if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
								ArrayList tmp;
								if (tb1.ifp)
									tmp = bodyMap.get(tb1.index);
								else
									tmp = headMap.get(tb1.index);
								tmp.set(tb1.offset, tb1.getValue());
								System.out.println(tb1.getValue());
							}
						}

					});

					ArrayList<String> args = new ArrayList<String>();
					args.add(term);
					args.add(words[0]);
					args.add(words[2]);

					if (ifPart)
						bodyMap.put(counter, args);
					else
						headMap.put(counter, args);

					counter++;

				}

			} else if (txt.contains("!4!")) {
				System.out.println("DATA VALUE PROPERTY!!");
				txt = txt.replaceAll("!4!", "");
				words = mySplit(txt);
				if (words.length != 6) {
					System.out.println("Error in Data Value Property syntax."
							+ words.length);
				} else {
					final IndexedTextBox tb0 = new IndexedTextBox(counter, 1,
							ifPart);
					tb0.setWidth("150px");
					tb0.setText(words[0]);
					this.add(tb0);
					tb0.addKeyPressHandler(new KeyPressHandler() {
						public void onKeyPress(KeyPressEvent event) {
							if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
								ArrayList tmp;
								if (tb0.ifp)
									tmp = bodyMap.get(tb0.index);
								else
									tmp = headMap.get(tb0.index);
								tmp.set(tb0.offset, tb0.getValue());
								System.out.println(tb0.getValue());
							}
						}

					});

					TextBox tb1 = new TextBox();
					tb1.setWidth("150px");
					tb1.setText(words[1] + " " + words[2]);
					tb1.setReadOnly(true);
					this.add(tb1);

					final IndexedTextBox tb2 = new IndexedTextBox(counter, 2,
							ifPart);
					tb2.setWidth("150px");
					tb2.setText(words[3]);
					this.add(tb2);
					tb2.addKeyPressHandler(new KeyPressHandler() {
						public void onKeyPress(KeyPressEvent event) {
							if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
								ArrayList tmp;
								if (tb2.ifp)
									tmp = bodyMap.get(tb2.index);
								else
									tmp = headMap.get(tb2.index);
								tmp.set(tb2.offset, tb2.getValue());
								System.out.println(tb2.getValue());
							}
						}

					});

					TextBox tb3 = new TextBox();
					tb3.setWidth("150px");
					tb3.setText(words[4]);
					tb3.setReadOnly(true);
					this.add(tb3);

					term = words[5];
					final DataValCombo myDataValComboo = new DataValCombo(term,
							counter, ifPart);
					this.add(myDataValComboo);
					myDataValComboo.addListener(new ComboBoxListenerAdapter() {
						public void onChange(Field field, Object newVal,
								Object oldVal) {
							ArrayList tmp;
							if (myDataValComboo.ifp)
								tmp = bodyMap.get(myDataValComboo.index);
							else
								tmp = headMap.get(myDataValComboo.index);
							tmp.set(0, newVal);
							System.out.println("ComboBox::onChange(" + oldVal
									+ "-->" + newVal + ")");
						}
					});

					ArrayList<String> args = new ArrayList<String>();
					args.add(term);
					args.add(words[0]);
					args.add(words[3]);

					if (ifPart)
						bodyMap.put(counter, args);
					else
						headMap.put(counter, args);

					counter++;

				}

			} else if (txt.contains("!5!")) {
				System.out.println("BUILTIN PROPERTY!!");
				txt = txt.replaceAll("!5!", "");
				words = mySplit(txt);

				final IndexedTextBox tb0 = new IndexedTextBox(counter, 1,
						ifPart);
				tb0.setWidth("150px");
				tb0.setText(words[0]);
				this.add(tb0);

				tb0.addKeyPressHandler(new KeyPressHandler() {
					public void onKeyPress(KeyPressEvent event) {
						if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
							ArrayList tmp;
							if (tb0.ifp)
								tmp = bodyMap.get(tb0.index);
							else
								tmp = headMap.get(tb0.index);
							tmp.set(tb0.offset, tb0.getValue());
							System.out.println(tb0.getValue());
						}
					}

				});

				term = words[1];
				final BuiltinCombo myBuiltinCombo = new BuiltinCombo(term,
						counter, ifPart);
				this.add(myBuiltinCombo);
				myBuiltinCombo.addListener(new ComboBoxListenerAdapter() {
					public void onChange(Field field, Object newVal,
							Object oldVal) {
						ArrayList tmp;
						if (myBuiltinCombo.ifp)
							tmp = bodyMap.get(myBuiltinCombo.index);
						else
							tmp = headMap.get(myBuiltinCombo.index);
						tmp.set(0, newVal);
						System.out.println("ComboBox::onChange(" + oldVal
								+ "-->" + newVal + ")");
					}
				});

				for (int i = 2; i < words.length; i++) {
					final IndexedTextBox tb1 = new IndexedTextBox(counter, i,
							ifPart);
					tb1.setWidth("150px");
					tb1.setText(words[i]);
					this.add(tb1);
					tb1.addKeyPressHandler(new KeyPressHandler() {
						public void onKeyPress(KeyPressEvent event) {
							if (event.getCharCode() == KeyboardListener.KEY_ENTER) {
								ArrayList tmp;
								if (tb1.ifp)
									tmp = bodyMap.get(tb1.index);
								else
									tmp = headMap.get(tb1.index);
								tmp.set(tb1.offset, tb1.getValue());
								System.out.println(tb1.getValue());
							}
						}

					});
				}

				ArrayList<String> args = new ArrayList<String>();
				args.add(term);
				args.add(words[0]);
				for (int i = 2; i < words.length; i++)
					args.add(words[i]);

				if (ifPart)
					bodyMap.put(counter, args);
				else
					headMap.put(counter, args);

				counter++;
			} else {

				words = mySplit(txt);
				for (int j = 0; j < words.length; j++) {
					term = words[j];
					if (term.equals("IF")) {
						TextBox tb = new TextBox();
						tb.setWidth("150px");
						tb.setText(term);
						tb.setReadOnly(true);
						this.add(tb);

					} else {
						TextBox tb = new TextBox();
						tb.setWidth("150px");
						// tb.setHeight("50px");
						tb.setText(term);
						this.add(tb);
					}
				}
			}
		}

	}

	private String makeSWRL(HashMap body, HashMap head) {
		String swrl = " ";

		Iterator bit = body.keySet().iterator();
		while (bit.hasNext()) {
			int index = (Integer) bit.next();
			ArrayList<String> ar = (ArrayList<String>) body.get(index);
			swrl += (String) ar.get(0) + "(";

			for (int i = 1; i < ar.size(); i++) {
				String arg = (String) ar.get(i);
				//System.out.println("AAA " + arg);
				if (arg.charAt(0) == '"') { // ////////
					arg = arg.replaceAll("\"", "");
				}

//				if (arg.contains("<") || arg.contains(">")) {
//					System.out.println("saalaaaaaaaaaaaaaaaaaaam");
//					arg = arg.replaceAll("\'<", "<");
//					arg = arg.replaceAll(">\'", ">");
//				}
				arg = arg.replaceAll("\'<", "<");
				arg = arg.replaceAll(">\'", ">");
				arg = arg.replaceAll("'", "\"");
				arg = arg.replaceAll("<", "\'");
				arg = arg.replaceAll(">", "\'");
				swrl += arg;

				if (i != ar.size() - 1) {
					swrl += ",";
				}
			}
			swrl += ") <br> ^ ";
		}
		if (swrl.charAt(swrl.length() - 2) == '^')
			swrl = swrl.substring(0, swrl.length() - 3);
		swrl += " -> ";

		Iterator hit = head.keySet().iterator();
		while (hit.hasNext()) {
			int index = (Integer) hit.next();
			ArrayList<String> ar = (ArrayList<String>) head.get(index);
			swrl += ar.get(0) + "(";

			for (int i = 1; i < ar.size(); i++) {
				String arg = (String) ar.get(i);
				if (arg.charAt(0) == '"') {
					arg = arg.replaceAll("\"", "");
				}
				arg = arg.replaceAll("\'<", "<");
				arg = arg.replaceAll(">\'", ">");
				arg = arg.replaceAll("'", "\"");
				arg = arg.replaceAll("<", "\'");
				arg = arg.replaceAll(">", "\'");
				swrl += arg;

				if (i != ar.size() - 1) {
					swrl += ",";
				}
			}
			swrl += ") <br> ^ ";
		}

		if (swrl.charAt(swrl.length() - 2) == '^')
			swrl = swrl.substring(0, swrl.length() - 3);

		// System.out.println("SWRL RULE = " + swrl);
		return swrl;
	}

	public class IndexedTextBox extends TextBox {
		int index, offset;
		boolean ifp;

		public IndexedTextBox(int index, int offset, boolean ifp) {
			super();
			this.index = index;
			this.offset = offset;
			this.ifp = ifp;
		}
	}

	public class ClassCombo extends ComboBox {
		String term;
		int index;
		boolean ifp;

		public ClassCombo(String term, int index, boolean ifp) {
			this.term = term;
			this.index = index;
			this.ifp = ifp;
			int clength = ElicitationPortlet.this.SWRLClassNames.length;
			String[][] states = new String[clength][];
			for (int i = 0; i < clength; i++) {
				states[i] = new String[1];
				states[i][0] = ElicitationPortlet.this.SWRLClassNames[i];
			}

			// Store store = new Store(reader);
			final Store store = new SimpleStore(new String[] { "names" },
					states);
			store.load();

			this.setMinChars(1);
			this.setFieldLabel("State");
			this.setStore(store);
			this.setDisplayField("names");
			this.setMode(ComboBox.LOCAL);
			this.setEmptyText("Enter Class");
			this.setLoadingText("Searching...");
			this.setTypeAhead(true);
			this.setSelectOnFocus(true);
			this.setWidth(150);
			// this.setHeight("50px");
			this.setHideTrigger(true); // do not show drop fown icon
			this.setValue(term);

		}

	}

	public class IndvCombo extends ComboBox {
		String term;
		int index;
		boolean ifp;

		public IndvCombo(String term, int index, boolean ifp) {
			this.term = term;
			this.index = index;
			this.ifp = ifp;
			int clength = ElicitationPortlet.this.SWRLIndvNames.length;
			String[][] states = new String[clength][];
			for (int i = 0; i < clength; i++) {
				states[i] = new String[1];
				states[i][0] = ElicitationPortlet.this.SWRLIndvNames[i];
			}

			// Store store = new Store(reader);
			final Store store = new SimpleStore(new String[] { "names" },
					states);
			store.load();

			this.setMinChars(1);
			this.setFieldLabel("State");
			this.setStore(store);
			this.setDisplayField("names");
			this.setMode(ComboBox.LOCAL);
			this.setEmptyText("Enter Object Property");
			this.setLoadingText("Searching...");
			this.setTypeAhead(true);
			this.setSelectOnFocus(true);
			this.setWidth(150);
			// this.setHeight("50px");
			this.setHideTrigger(true); // do not show drop fown icon
			this.setValue(term);

		}

	}

	public class DataValCombo extends ComboBox {
		String term;
		int index;
		boolean ifp;

		public DataValCombo(String term, int index, boolean ifp) {
			this.term = term;
			this.index = index;
			this.ifp = ifp;
			int clength = ElicitationPortlet.this.SWRLDataValNames.length;
			String[][] states = new String[clength][];
			for (int i = 0; i < clength; i++) {
				states[i] = new String[1];
				states[i][0] = ElicitationPortlet.this.SWRLDataValNames[i];
			}

			// Store store = new Store(reader);
			final Store store = new SimpleStore(new String[] { "names" },
					states);
			store.load();

			this.setMinChars(1);
			this.setFieldLabel("State");
			this.setStore(store);
			this.setDisplayField("names");
			this.setMode(ComboBox.LOCAL);
			this.setEmptyText("Enter Datavalue Property");
			this.setLoadingText("Searching...");
			this.setTypeAhead(true);
			this.setSelectOnFocus(true);
			this.setWidth(150);
			// this.setHeight("50px");
			this.setHideTrigger(true); // do not show drop fown icon
			System.out.println("DATA VALUE COMB " + term);
			this.setValue(term);

		}

	}

	public class BuiltinCombo extends ComboBox {
		String term;
		int index;
		boolean ifp;

		public BuiltinCombo(String term, int index, boolean ifp) {
			this.term = term;
			this.index = index;
			this.ifp = ifp;
			int clength = ElicitationPortlet.this.SWRLBuiltinNames.length;
			String[][] states = new String[clength][];
			for (int i = 0; i < clength; i++) {
				states[i] = new String[1];
				states[i][0] = ElicitationPortlet.this.SWRLBuiltinNames[i];
			}

			// Store store = new Store(reader);
			final Store store = new SimpleStore(new String[] { "names" },
					states);
			store.load();

			this.setMinChars(1);
			this.setFieldLabel("State");
			this.setStore(store);
			this.setDisplayField("names");
			this.setMode(ComboBox.LOCAL);
			this.setEmptyText("Enter Built-in Property");
			this.setLoadingText("Searching...");
			this.setTypeAhead(true);
			this.setSelectOnFocus(true);
			this.setWidth(150);
			// this.setHeight("50px");
			this.setHideTrigger(true); // do not show drop fown icon
			this.setValue(term);

		}

	}

	public class SWRLWindow extends Window {
		public SWRLWindow(String text) {
			Panel windowPanel = new Panel();
			windowPanel.setHtml(text);
			windowPanel.setShadow(true);

			this.setTitle("Window Panel");
			this.setIconCls("paste-icon");
			this.setMaximizable(true);
			this.setResizable(true);
			this.setLayout(new FitLayout());
			this.setAutoWidth(true);
			// this.setWidth(400);
			this.setModal(false);
			this.setAutoHeight(true);

			this.add(windowPanel);
		}
	}

	private class AddSWRLRule extends AbstractAsyncHandler {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting SWRL Names from server", caught);
		}

		@Override
		public void handleSuccess(Object result) {
			// TODO Auto-generated method stub
			String Stringresult = (String) result;

			ElicitationPortlet.this.addResult = Stringresult;

			System.out.println("Result CLIENT = "
					+ ElicitationPortlet.this.addResult);
			SWRLWindow window = new SWRLWindow(
					ElicitationPortlet.this.addResult);

			notifySelectionListeners(new SelectionEvent(ElicitationPortlet.this));

			window.show();

		}
	}

	private String[] mySplit(String text) {
		ArrayList<String> list = new ArrayList<String>();
		// System.out.println(text);
		boolean mys = false;
		String tmp;
		String qo = "";

		StringTokenizer st = new StringTokenizer(text, " \'", true);
		while (st.hasMoreTokens()) {
			tmp = st.nextToken();
			if (tmp.equals("\'") && !mys) {
				mys = true;
				qo = "\'";
				continue;
			} else if (!tmp.equals("\'") && mys) {
				qo += tmp;
				continue;
			} else if (tmp.equals("\'") && mys) {
				qo += "\'";
				mys = false;
				list.add(qo);
				continue;
			} else if (!tmp.equals(" ") && !mys) {
				list.add(tmp);
			}
		}

		String a[] = new String[list.size()];
		for (int i = 0; i < a.length; i++)
			a[i] = list.get(i);

		// for (int i = 0; i < a.length; i++)
		// System.out.println(a[i]);

		return a;

	}

	@Override
	public Collection<EntityData> getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

}
