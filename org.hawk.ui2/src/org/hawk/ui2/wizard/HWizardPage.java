package org.hawk.ui2.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.hawk.ui2.util.HManager;

/**
 * 
 */

public class HWizardPage extends WizardPage {
	private Text folderText;

	private Text indexerNameText;

	private Table pluginTable;

	/**
	 * Constructor for .
	 * 
	 * @param pageName
	 */
	public HWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("New Hawk Instance");
		setDescription("This wizard creates a new Hawk Instance to index models repositories.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		

		Label label = new Label(container, SWT.NULL);
		label.setText("&Indexer Name:");
		
		indexerNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		indexerNameText.setLayoutData(gd);
		
		ModifyListener ml =new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (e.getSource()==indexerNameText){
					String[] nm= folderText.getText().split(File.separator);
					if (nm[nm.length-1]!=getIndexerName())
						folderText.setText(basePath+File.separator+getIndexerName());
				}
				dialogChanged();
			}
		};

		indexerNameText.addModifyListener(ml);
		
		label = new Label(container, SWT.NULL);
		label.setText("");
		
		label = new Label(container, SWT.NULL);
		label.setText("&Local storage folder:");

		folderText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		folderText.setLayoutData(gd);
		folderText.setEditable(false);
		folderText.addModifyListener(ml);

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);

		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING );
		label.setLayoutData(gd);
		label.setText("&Hawk plugins:");
		
		  pluginTable = new Table(container, SWT.BORDER | SWT.V_SCROLL
	            | SWT.H_SCROLL);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		pluginTable.setLayoutData(gd);
	    pluginTable.setHeaderVisible(false);
	     
	     TableColumn column = new TableColumn(pluginTable, SWT.NULL);
	     column.setText("plugin");
	       
	     for(String plugin:this.getHawkPlugins()){
	    	 TableItem item = new TableItem(pluginTable, SWT.NULL); 
	    	 item.setText(plugin); 
	    	 item.setText(0, plugin);
	    	 //item.setChecked(true);
	     }
	     
	     pluginTable.getColumn(0).pack();
	     
	     pluginTable.addListener(SWT.Selection, new Listener() {
			@Override
	         public void handleEvent(Event event) {
	           if (event.detail == SWT.CHECK) {
					dialogChanged();
	           }
	         }
	       });
	     
			label = new Label(container, SWT.NULL);
			label.setText("");

			label = new Label(container, SWT.NULL);
			label.setText("");
			
			Button startButton = new Button (container, SWT.CHECK);
			startButton.setText ("Start with Workspace");
			startButton.setVisible(false);
			startButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dialogChanged();
				}
			});
		     
			label = new Label(container, SWT.NULL);
			label.setText("");

			label = new Label(container, SWT.NULL);
			label.setText("");
			
			Button deleteButton = new Button (container, SWT.CHECK);
			deleteButton.setText ("Delete existing indexes");
			deleteButton.setVisible(false);
			deleteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					dialogChanged();
				}
			});
	     

		initialize();
		dialogChanged();
		setControl(container);
	}
	
	private List<String> getHawkPlugins(){
		List<String> all =new ArrayList<String>();
		all.addAll(HManager.getUpdaterTypes());
		all.addAll(HManager.getIndexTypes());
		all.addAll(HManager.getMetaModelTypes());
		all.addAll(HManager.getModelTypes());
		all.addAll(HManager.getVCSTypes());
		return all;
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {

		//set the default indexer name "MyHawk"
		indexerNameText.setText("myhawk");
		//set the default indexer location
		//folderText.setText(basePath+File.separator+this.getIndexerName());
	}
	
	private String basePath=ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toString();
	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		DirectoryDialog dd = new DirectoryDialog(getShell(), SWT.OPEN);
		
		dd.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toString());
		dd.setMessage("Select a folder where the index files will be stored");
		dd.setText("Select a directory");
		String result = dd.open();

		if (result!= null){
			basePath=result;
			folderText.setText(basePath+File.separator+this.getIndexerName());
		}

	}

	/**
	 * Ensures that both text fields are set.
	 */
	private static final Pattern PATTERN = Pattern.compile("[^a-z0-9_]");
	
	private void dialogChanged() {

		String indexerName = getIndexerName();		
		
		if (indexerName.length() == 0 || getContainerName().length()==0) {
			updateStatus("Indexer name/folder must be specified");
			return;
		}
		//valid chars in indexername
	    Matcher m = PATTERN.matcher(indexerName);
		if (m.find()) {
			updateStatus("File name must be lowercase with only numbers and underscores allowed.");
			return;
		}		
		if (getContainerName().length() == 0) {
			updateStatus("Index storage folder must be specified");
			return;
		}		
		//must not already exist
		File f = new File(getContainerName());
		if (f.exists() && f.isDirectory() && f.listFiles().length>0){
			updateStatus("Index storage folder must be empty");
			return;
		}
		//must be writable
		if (!f.getParentFile().exists() && !f.getParentFile().canWrite()){
			updateStatus("Index storage folder must be writeable");
			return;
		}
		
		//check plugins form a valid hawk?
				


		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return folderText.getText();
	}

	public String getIndexerName() {
		return indexerNameText.getText();
	}
	
	public List<String> getPlugins(){
		List<String> selected = new ArrayList<String>();
		
		for (TableItem t: pluginTable.getItems()){
			if (t.getChecked())
				selected.add(t.getText());
		}
		
		return selected;
	}
}