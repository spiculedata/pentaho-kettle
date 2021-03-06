/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2016-2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.rap.rwt.client.service.StartupParameters;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.ui.core.PropsUI;

public class WebSpoonEntryPoint extends AbstractEntryPoint {

  @Override
  protected void createContents( Composite parent ) {
    /*
     *  Create a KettleHome for the current user.
     *  kettle.properties is automatically created for this user, but not used.
     *  Currently, only .spoonrc is aware of multiple users.
     */
    KettleClientEnvironment.createKettleUserHome();
    /*
     *  The following lines were migrated from Spoon.main
     *  because they are session specific.
     */
    PropsUI.init( parent.getDisplay(), Props.TYPE_PROPERTIES_SPOON );

    // Options
    StartupParameters serviceParams = RWT.getClient().getService( StartupParameters.class );
    List<String> args = new ArrayList<String>();
    String[] options = { "rep", "user", "pass", "trans", "job", "dir", "file" };
    for ( String option : options ) {
      if ( serviceParams.getParameter( option ) != null ) {
        args.add( "-" + option + "=" + serviceParams.getParameter( option ) );
      }
    }

    // Execute Spoon.createContents
    CommandLineOption[] commandLineArgs = Spoon.getCommandLineArgs( args );
    Spoon.getInstance().setCommandLineArgs( commandLineArgs );
    Spoon.getInstance().setShell( parent.getShell() );
    Spoon.getInstance().createContents( parent );
    Spoon.getInstance().setArguments( args.toArray( new String[ args.size() ] ) );
    try {
      ExtensionPointHandler.callExtensionPoint( Spoon.getInstance().getLog(), KettleExtensionPoint.SpoonStart.id, commandLineArgs );
    } catch ( Throwable e ) {
      LogChannel.GENERAL.logError( "Error calling extension points", e );
    }

    // For VFS browser, set the user data directory. This will be overwritten by the last open file if exists.
    Spoon.getInstance().setLastFileOpened( Const.getKettleUserDataDirectory() );

    // Load last used files
    Spoon.getInstance().loadLastUsedFiles();

    /*
     *  The following lines are webSpoon additional functions
     */
    if ( Spoon.getInstance().getProperties().showExitWarning() ) {
      ExitConfirmation serviceConfirm = RWT.getClient().getService( ExitConfirmation.class );
      serviceConfirm.setMessage( "Do you really wanna leave this site?" );
    }
  }

}
