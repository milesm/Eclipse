package com.mobilesorcery.sdk.builder.android.ui.properties;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.mobilesorcery.sdk.builder.android.AndroidPackager;
import com.mobilesorcery.sdk.builder.android.PropertyInitializer;
import com.mobilesorcery.sdk.core.PropertyUtil;
import com.mobilesorcery.sdk.ui.DefaultMessageProvider;
import com.mobilesorcery.sdk.ui.MoSyncPropertyPage;
import com.mobilesorcery.sdk.ui.UpdateListener;

public class AndroidPropertyPage extends MoSyncPropertyPage {

    private Text packageText;
    private Text versionNumberText;

    protected Control createContents(Composite parent) {
        Composite main = new Composite(parent, SWT.NONE);
        main.setLayout(new GridLayout(2, false));
        
        Label packageLabel = new Label(main, SWT.NONE);
        packageLabel.setText("Android Package name");
        packageText = new Text(main, SWT.SINGLE | SWT.BORDER);
        packageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label versionNumberLabel = new Label(main, SWT.NONE);
        versionNumberLabel.setText("Android Version code");
        
        versionNumberText = new Text(main, SWT.SINGLE | SWT.BORDER);
        versionNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        UpdateListener listener = new UpdateListener(this);
        versionNumberText.addListener(SWT.Modify, listener);
        packageText.addListener(SWT.Modify, listener);
        
        initUI();
        return main;
    }
    
    private void initUI() {
        setText(packageText, getProject().getProperty(PropertyInitializer.ANDROID_PACKAGE_NAME));
        setText(versionNumberText, Integer.toString(PropertyUtil.getInteger(getProject(), PropertyInitializer.ANDROID_VERSION_CODE)));
    }
    
    public boolean performOk() {
        getProject().setProperty(PropertyInitializer.ANDROID_PACKAGE_NAME, packageText.getText());
        PropertyUtil.setInteger(getProject(), PropertyInitializer.ANDROID_VERSION_CODE, Integer.parseInt(versionNumberText.getText()));
        return true;
    }

    protected void validate() {
        IMessageProvider message = DefaultMessageProvider.EMPTY;
        boolean failedVersionCode = true;
        try {
            int value = Integer.parseInt(versionNumberText.getText());
            failedVersionCode = value < 1;
        } catch (Exception e) {
            // Fall thru.
        }
        
        if (failedVersionCode) {
            message = new DefaultMessageProvider("Version number must be integer > 0", IMessageProvider.ERROR);
        }
        
        if (DefaultMessageProvider.isEmpty(message)) {
            message = AndroidPackager.validatePackageName(packageText.getText());
        }
        
        setMessage(message);
    }
}
