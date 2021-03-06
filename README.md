# Web-based Spoon (aka webSpoon)

webSpoon is a web-based graphical designer for Pentaho Data Integration with the same look & feel as Spoon.
Kettle transformation/job files can be designed and executed in your favorite web browser.
This is one of the community activities and not supported by Pentaho.

## Use cases

- Data security
- Remote use
- Ease of management
- Cloud

Please see [public talks](https://www.slideshare.net/HiromuHota/presentations) for more details.

# How to use

Please refer to the [wiki](https://github.com/HiromuHota/pentaho-kettle/wiki) and [issues](https://github.com/HiromuHota/pentaho-kettle/issues).

# How to deploy

There are two ways: with Docker and without Docker.
Docker is recommended because it is simple hence error-free.
In either way, webSpoon will be deployed at `http://address:8080/spoon/spoon`.

## With Docker (recommended)

```
$ docker run -e JAVA_OPTS="-Xms1024m -Xmx2048m" -d -p 8080:8080 hiromuhota/webspoon:latest-full
```

## Without Docker

Please refer to the [wiki](https://github.com/HiromuHota/pentaho-kettle/wiki/System-Requirements) for system requirements.

1. Unzip `pdi-ce-8.0.0.0-28.zip`, then copy `system` and `plugins` folders to `$CATALINA_HOME`.
2. Run [install.sh](https://raw.githubusercontent.com/HiromuHota/webspoon-docker/master/install.sh) at `$CATALINA_HOME`.
3. (Re)start the Tomcat.

The actual commands look like below:

```
$ export CATALINA_HOME=/home/vagrant/apache-tomcat-8.5.23
$ cd ~/
$ unzip ~/Downloads/pdi-ce-8.0.0.0-28.zip
$ cd $CATALINA_HOME
$ cp -r ~/data-integration/system ./
$ cp -r ~/data-integration/plugins ./
$ wget https://raw.githubusercontent.com/HiromuHota/webspoon-docker/master/install.sh
$ chmod +x install.sh
$ export version=0.8.0.13
$ export dist=8.0.0.0-28
$ ./install.sh
$ ./bin/startup.sh
```

# How to config (optional)

## User authentication

Edit `WEB-INF/web.xml` to uncomment/enable user authentication.

```
  <!-- Uncomment the followings to enable login page for webSpoon
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
      /WEB-INF/spring/*.xml
    </param-value>
  </context-param>

  <filter>
    <filter-name>springSecurityFilterChain</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>springSecurityFilterChain</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>

  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>
  -->
```

Edit `WEB-INF/spring/security.xml` to manage users.
The following example shows how to assign <i>user</i> with password of <i>password</i> to <i>USER</i> role.

```
<b:beans>
  <user-service>
    <user name="user" password="password" authorities="ROLE_USER" />
  </user-service>
</b:beans>
```

It would be possible to use LDAP as an authentication provider.
See [here](http://docs.spring.io/spring-security/site/docs/4.1.x/reference/html/ns-config.html) for more details.
webSpoon uses the same framework for user authentication: Spring Security, as Pentaho User Console.
Thus, it would also be possible to use Microsoft Active Directory as described in Pentaho's official documentation for [User Security](https://help.pentaho.com/Documentation/7.0/0P0/Setting_Up_User_Security).

## Third-party plugins and JDBC drivers

Place third-party plugins in `$CATALINA_HOME/plugins` and JDBC drivers in `$CATALINA_HOME/lib` as below:

```
$CATALINA_HOME
├── system
├── plugins
│   ├── YourPlugin
│   │   └── YourPlugin.jar
│   ├── ...
├── lib
│   ├── YourJDBC.jar
│   ├── ...
├── webapps
│   ├── spoon.war
│   ├── ...
```

# How to develop

Spoon relies on SWT for UI widgets, which is great for being OS agnostic, but it only runs as a desktop app.
RAP/RWT provides web UIs with SWT API, so replacing SWT with RAP/RWT allows Spoon to run as a web app with a little code change.
Having said that, some APIs are not implemented; hence, a little more code change is required than it sounds.

## Coding philosophy

1. Minimize the difference from the original Spoon.
2. Decide RWT or webSpoon to be modified so that the change can be minimized.

These are the major changes so far:

- Add org.pentaho.di.ui.spoon.WebSpoon, which configures web app.
- Modify ui/ivy.xml in order to add RWT-related dependencies and remove SWT.
- Many comment-outs/deletions to avoid compile errors due to RWT/SWT difference.
- Make singleton objects (e.g., `PropsUI`, `GUIResource`) session-unique (see [here](http://www.eclipse.org/rap/developers-guide/devguide.php?topic=singletons.html) for the details).

## Branches and Versioning

I started this project in the webspoon branch, branched off from the branch 6.1 of between 6.1.0.5-R and 6.1.0.6-R.
Soon I realized that I should have branched off from one of released versions.
So I decided to make two branches: webspoon-6.1 and webspoon-7.0, each of which was rebased onto 6.1.0.1-R and 7.0.0.0-R, respectively.
I made the branch webspoon-6.1 as the default one for this git repository as the branch webspoon-7.0 currently cannot use the marketplace plugin.

webSpoon uses 4 digits versioning with the following rules:

- The 1st digit is always 0 (never be released as a separate software).
- The 2nd and 3rd digits represent the base Kettle version, e.g., 6.1, 7.0.
- The last digit represents the patch version.

As a result, the next (pre-)release version will be 0.6.1.4, meaning it is based on the Kettle version 6.1 with the 4th patch.
There could be a version of 0.7.0.4, which is based on the Kettle version 7.0 with (basically) the same patch.

## Build and locally publish dependent libraries

Please build and locally-publish the following dependent libraries.

- pentaho-xul-swt
- org.eclipse.rap.rwt
- org.eclipse.rap.jface
- org.eclipse.rap.fileupload
- org.eclipse.rap.filedialog
- org.eclipse.rap.rwt.testfixture

### pentaho-commons-xul

```
$ git clone -b webspoon-8.0 https://github.com/HiromuHota/pentaho-commons-xul.git
$ cd pentaho-commons-xul/pentaho-xul-swt
$ ant clean-all resolve publish-local
```

### rap

```
$ git clone -b webspoon-3.1-maintenance https://github.com/HiromuHota/rap.git
$ cd rap
$ mvn clean install -N
$ mvn clean install -pl bundles/org.eclipse.rap.rwt -am
$ mvn clean install -pl bundles/org.eclipse.rap.jface -am
$ mvn clean install -pl bundles/org.eclipse.rap.fileupload -am
$ mvn clean install -pl bundles/org.eclipse.rap.filedialog -am
$ mvn clean install -pl tests/org.eclipse.rap.rwt.testfixture -am
```

## Build in the command line

**Make sure patched dependent libraries have been published locally, and no cached jars for RAP (if there is any update).**

Compile `kettle-core`, `kettle-engine` and `kettle-ui-swt`; and install the packages into the local repository:

```bash
$ git clone -b webspoon-8.0 https://github.com/HiromuHota/pentaho-kettle.git
$ cd pentaho-kettle
$ mvn clean install -pl core,engine,ui
```

Change directory and build a war file (`spoon.war`):

```bash
$ mvn clean package -pl assemblies/pdi-ce
```

## UI testing using Selenium (outdated)

Currently, only Google Chrome browser has been tested for when running UI test cases.
The tests run in headless mode unless a parameter `-Dheadless.unittest=false` is passed.
To run tests in headless mode, the version of Chrome should be higher than 59.

The default url is `http://localhost:8080/spoon`.
Pass a parameter like below if webSpoon is deployed to a different url.

The following command runs all the unit test cases including UI in non-headless mode.

```
$ ant test -Dtest.baseurl=http://localhost:8080/spoon/spoon -Dheadless.unittest=false
```

# Notices

- Pentaho is a registered trademark of Pentaho, Inc.
- Oracle and Java are registered trademarks of Oracle and/or its affiliates.
- Ubuntu is a registered trademark of Canonical Ltd.
- Mac and OS X are trademarks of Apple Inc., registered in the U.S. and other countries.
- Windows and Active Directory are registered trademark of Microsoft Corporation in the U.S. and other countries.
- Apache Karaf is a trademark of The Apache Software Foundation.
- Google Chrome browser is a trademark of Google Inc.
- Other company and product names mentioned in this document may be the trademarks of their respective owners.
