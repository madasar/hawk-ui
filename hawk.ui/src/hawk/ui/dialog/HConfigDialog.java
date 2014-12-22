package hawk.ui.dialog;

import hawk.ui.util.HManager;
import hawk.ui.util.HModel;


import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.googlecode.hawk.core.IVcsManager;


public class HConfigDialog extends Dialog {


	private HModel index;
	private List mmList;

	public HConfigDialog(Shell parentShell, HModel in) {
		super(parentShell);
	    setShellStyle(getShellStyle() & ~SWT.CLOSE);

		index=in;
	}

	@Override
	   protected void createButtonsForButtonBar(Composite parent) {
	    super.createButtonsForButtonBar(parent);

	    Button cancel = getButton(IDialogConstants.OK_ID);
	    cancel.setText("Done");
	    setButtonLayoutData(cancel);
	 }
	
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) 
	{
	    if (id == IDialogConstants.CANCEL_ID) return null;
	    return super.createButton(parent, id, label, defaultButton);
	}
	
	protected Control createDialogArea(Composite parent) {
				
		TabFolder tabFolder = new TabFolder(parent, SWT.BORDER);
		
	    TabItem metamodelTab = new TabItem(tabFolder, SWT.NULL);
	    metamodelTab.setText("Metamodels" );
	    metamodelTab.setControl(mmTab(tabFolder));
	    
	    TabItem vcsTab = new TabItem(tabFolder, SWT.NULL);
	    vcsTab.setText("Indexed Locations");
	    vcsTab.setControl(vcsTab(tabFolder));
	    
		
		tabFolder.pack();
		return tabFolder;
	}


	private Composite mmTab(TabFolder parent){
		
		final Composite composite = new Composite(parent, SWT.BORDER);
   		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;     
		composite.setLayout(gridLayout);

		mmList = new List (composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
	    GridData gridDataQ = new GridData();
	    gridDataQ.grabExcessHorizontalSpace = true;
	    gridDataQ.horizontalAlignment = GridData.FILL_BOTH;
	    gridDataQ.heightHint = 300;
	    gridDataQ.widthHint = 600;
	    gridDataQ.horizontalSpan = 2;
	    
	    mmList.setLayoutData(gridDataQ);
		
		updateMMList();
		
		Button remove = new Button(composite, SWT.PUSH);
		remove.setText("Remove");
		remove.setEnabled(false);
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//remove action
			}
		});
		
		Button browse = new Button(composite, SWT.PUSH);
		browse.setText("Add...");
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				mmBrowse();
			}
		});
		

		return composite;
	}

	
	private void mmBrowse() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		
		fd.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toString());
		//TODO: allow selection of only parse-able/known metamodels-file-types
		fd.setFilterExtensions(new String [] {"*.ecore"});
		fd.setText("Select a metamodel");
		String result = fd.open();

		if (result!= null){
			File metaModel = new File (result);
			if (metaModel.exists() && metaModel.canRead() && metaModel.isFile()){
				index.registerMeta(metaModel);
				updateMMList();
			}
		}

	}

	private void updateMMList() {
		mmList.removeAll();
		for (String mm: index.getRegisteredMetamodels()){
			mmList.add(mm);
		}
	}
	
	private void updateLocList() {
		locList.removeAll();
		for (String loc: index.getLocations()){
			locList.add(loc);
		}
	}	
	

	private Combo combo;
	
	private Composite vcsTab(TabFolder parent){
		final Composite composite = new Composite(parent, SWT.BORDER);
   		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;     
		composite.setLayout(gridLayout);

		locList = new List (composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
	    GridData gridDataQ = new GridData();
	    gridDataQ.grabExcessHorizontalSpace = true;
	    gridDataQ.horizontalAlignment = GridData.FILL_BOTH;
	    gridDataQ.heightHint = 300;
	    gridDataQ.widthHint = 600;
	    gridDataQ.horizontalSpan = 5;
	    locList.setLayoutData(gridDataQ);
	    updateLocList();
	    
		
		//combo (VCS types)
		combo = new Combo (composite, SWT.READ_ONLY);
		//ask HModel for a list of supported VCS types
		combo.setItems (index.getVCSTypeNames().toArray(new String[0]));    
		GridData gridDataC = new GridData();
	    gridDataC.grabExcessHorizontalSpace = false;
	    gridDataC.widthHint=200;
	    gridDataC.minimumWidth=200;
	    gridDataC.horizontalAlignment = GridData.FILL_BOTH;
	    combo.setLayoutData(gridDataC);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				location.setText("");
				setAuthBrowseButton();
			}
		});
		
		location = new Text(composite,  SWT.BORDER);	    
		GridData gridDataR = new GridData();
		gridDataR.widthHint=250;
	    gridDataR.grabExcessHorizontalSpace = true;
	    gridDataR.horizontalAlignment = GridData.FILL_BOTH;
	    location.setLayoutData(gridDataR);
	    location.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAuthBrowseButton();
				setAddButton();
			}
		});
	    
	    authBrowse = new Button(composite, SWT.PUSH);
		GridData gridDataA = new GridData();
		gridDataA.widthHint=105;
	    authBrowse.setLayoutData(gridDataA);
	    
	    setAuthBrowseButton();
	    //just until the first event
		authBrowse.setEnabled(false);
		
		add = new Button(composite, SWT.PUSH);
		add.setText("Add");
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(validateSVN()){
					index.addSVN(location.getText(), user, pass);
				}
				if(validateFolder()){
					index.addLocal(location.getText());
				}
				location.setText("");
				//combo.setItems (index.getVCSTypeNames().toArray(new String[0]));    		
				setAuthBrowseButton();
				setAddButton();
				updateLocList();
			}
		});
		
		add.setEnabled(false);

		return composite;
	}
	private Button authBrowse = null;
	
	private void setAuthBrowseButton(){
		if (authBrowse!=null){
			if (authRequired()&&!authBrowse.getText().equals("Authenticate...")){
				authBrowse.removeListener(SWT.Selection, browseFolder);
				authBrowse.addListener(SWT.Selection, authenticate);
				authBrowse.setText("Authenticate...");
			}
			if (!authRequired()&&!authBrowse.getText().equals("Browse...")){
				authBrowse.removeListener(SWT.Selection, authenticate);
				authBrowse.addListener(SWT.Selection, browseFolder);
				authBrowse.setText("Browse...");
			}
			if(authRequired()&&!validSVNLoc())
				authBrowse.setEnabled(false);
			else
				authBrowse.setEnabled(true);
		}
	}
	
	private Listener browseFolder = new Listener() {
		public void handleEvent(Event e) {
			DirectoryDialog dd = new DirectoryDialog(getShell(), SWT.OPEN);
			
			dd.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toString());
			dd.setMessage("Select a folder to add to the indexer");
			dd.setText("Select a directory");
			String result = dd.open();

			if (result!= null){
				location.setText(result);
			}	
			setAddButton();
		}
	};
	
	private boolean userPassOK=false;
	private String user="";
	private String pass="";
	
	private Listener authenticate = new Listener() {
		public void handleEvent(Event e) {
			
			UsernamePasswordDialog upd= new UsernamePasswordDialog(getShell());
			userPassOK=(upd.open()==Window.OK);
			if (userPassOK){
				user=upd.getPassword();
				pass=upd.getUsername();
			}
			setAddButton();
		}
	};
	
	private boolean validateSVN(){
		if (authRequired()&&userPassOK&&!user.equals("")&&!pass.equals("")){
			//could do an SVN connect test
			return validSVNLoc();
		}
		return false;
	}

	private boolean validSVNLoc() {
		String loc=location.getText().toLowerCase();
		if(loc.startsWith("http://")||loc.startsWith("https://")||loc.startsWith("svn://"))
			return true;
		else
			return false;
	}
	
	private boolean validateFolder(){
		File dir=new File(location.getText());
		if (!authRequired()&&dir.exists()&&dir.isDirectory()&&dir.canRead())
			return true;
		return false;
	}
	
	private void setAddButton(){
		if (validateSVN())
			add.setEnabled(true);
		else
		if (validateFolder())
			add.setEnabled(true);
		else
			add.setEnabled(false);
	}
	
	private Text location;
	private Button add;
	private List locList;
	
	private void showMessage(String message) {
		MessageDialog.openInformation(
			getShell(),
			"Hawk",
			message);
	}
	
	private boolean authRequired(){
		if (combo.getSelectionIndex()<0)
			return false;
		String selection = combo.getItem(combo.getSelectionIndex());
		return selection.toLowerCase().contains("svn") || selection.toLowerCase().contains("git");
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure Indexer: "+index.getName());
	}
	
	
	public class UsernamePasswordDialog extends Dialog {
		  private static final int RESET_ID = IDialogConstants.NO_TO_ALL_ID + 1;

		  private Text usernameField;
		  private Text passwordField;

		  private String password;
		  private String user;

		  public UsernamePasswordDialog(Shell parentShell) {
		    super(parentShell);
		  }

		  protected Control createDialogArea(Composite parent) {
		    Composite comp = (Composite) super.createDialogArea(parent);

		    GridLayout layout = (GridLayout) comp.getLayout();
		    layout.numColumns = 2;

		    Label usernameLabel = new Label(comp, SWT.RIGHT);
		    usernameLabel.setText("Username: ");

		    usernameField = new Text(comp, SWT.SINGLE);
		    GridData data = new GridData(GridData.FILL_HORIZONTAL);
		    usernameField.setLayoutData(data);

		    Label passwordLabel = new Label(comp, SWT.RIGHT);
		    passwordLabel.setText("Password: ");

		    passwordField = new Text(comp, SWT.SINGLE | SWT.PASSWORD);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    passwordField.setLayoutData(data);

		    return comp;
		  }

		  protected void createButtonsForButtonBar(Composite parent) {
		    super.createButtonsForButtonBar(parent);
		    createButton(parent, RESET_ID, "Reset All", false);
		  }

		  protected void buttonPressed(int buttonId) {
		    if (buttonId == RESET_ID) {
		      usernameField.setText("");
		      passwordField.setText("");
		    } else {
		      super.buttonPressed(buttonId);
		    }
		  }
		  
		  public String getUsername(){
			  return this.user;
		  }		  
		  public String getPassword(){
			  return this.password;
		  }
		  
		  protected void okPressed() {
			    // Copy data from SWT widgets into fields on button press.
			    // Reading data from the widgets later will cause an SWT
			    // widget diposed exception.
			    user = usernameField.getText();
			    password = passwordField.getText();
			    super.okPressed();
		  }	
		}
}
