package hawk.ui.dialog;

import hawk.ui.util.HModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.googlecode.hawk.core.IModelIndexer;

public class HQueryDialog extends Dialog {


		private Text queryField;
		private Text resultField;

	private HModel index;

	public HQueryDialog(Shell parentShell, HModel in) {
		super(parentShell);
	    setShellStyle(getShellStyle() & ~SWT.CLOSE);
		index = in;
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
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label instructionsLabel = new Label(container, SWT.NONE);
		instructionsLabel.setLayoutData(new GridData(GridData.BEGINNING,
				GridData.CENTER, false, false, 2, 1));
		instructionsLabel.setText("Enter a query and click [Query] to get a result.");

		final Label qLabel = new Label(container, SWT.NONE);
		qLabel.setText("Query:");
		
		final Label dummy = new Label(container, SWT.NONE);
		dummy.setText("");

		queryField = new Text(container,  SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	    GridData gridDataQ = new GridData();
	    gridDataQ.grabExcessHorizontalSpace = true;
	    gridDataQ.horizontalAlignment = GridData.FILL_BOTH;
	    gridDataQ.minimumWidth = 600;
	    gridDataQ.minimumHeight = 300;
	    gridDataQ.heightHint = 100;
	    gridDataQ.horizontalSpan = 2;
	    
		queryField.setLayoutData(gridDataQ);

		final Label rLabel = new Label(container, SWT.NONE);
		rLabel.setText("Result:");
		
		
		final Label dummy2 = new Label(container, SWT.NONE);
		dummy2.setText("");
		

		resultField = new Text(container,  SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);	    
		GridData gridDataR = new GridData();
	    gridDataR.grabExcessHorizontalSpace = true;
	    gridDataR.horizontalAlignment = GridData.FILL_BOTH;
	    gridDataR.minimumWidth = 600;
	    gridDataR.minimumHeight = 300;
	    gridDataR.heightHint = 100;
	    gridDataR.horizontalSpan = 2;
		resultField.setLayoutData(gridDataR);
		resultField.setEditable(false);
		
		Button button = new Button(container, SWT.PUSH);
		button.setText("Query");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resultField.setText(index.query(queryField.getText()));
			}
		});
		
		queryField.setText("");
		queryField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				//if queryField contains valid query
					//resultField.setText(index.runEOL(queryField.getText()));
			}
		});

		return container;
	}



	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Query: "+index.getName());
	}




}
