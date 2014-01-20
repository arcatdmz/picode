
REM Setup class path
REM set jdi=.\processing\lib\com.ibm.icu_4.4.2.v20110823.jar;.\processing\lib\jdi.jar;.\processing\lib\jdimodel.jar;.\processing\lib\org.eclipse.osgi_3.8.1.v20120830-144521.jar
set jdi=.\processing\lib\tools.jar
set pde=.\lib\core\library\core.jar;%jdi%;.\processing\lib\ant.jar;.\processing\lib\ant-launcher.jar;.\processing\lib\antlr.jar;.\processing\lib\apple.jar;.\processing\lib\jna.jar;.\processing\lib\org-netbeans-swing-outline.jar;.\processing\lib\ecj.jar
set phybots=.\lib\phybots\library\phybots-core-1.0.2.jar;.\lib\phybots\library\connector-1.0.6.jar;.\lib\phybots\library\capture-1.0.3.jar;.\lib\phybots\library\bluecove-2.1.1-SNAPSHOT.jar;.\lib\phybots\library\fantom.jar
set kinect=.\lib\kinect\library\kinect.jar;.\lib\kinect\library\libthrift-0.9.0.jar;.\lib\kinect\library\slf4j-api-1.5.8.jar;.\lib\kinect\library\slf4j-simple-1.5.8.jar
set picode=.\picode.jar;%pde%;%phybots%;%kinect%

set jre7x86="C:\Program Files (x86)\Java\jre7\bin\java.exe"
set jre6x86="C:\Program Files (x86)\Java\jre6\bin\java.exe"

REM Look for JRE 7
if exist %jre7x86% (
  set java=%jre7x86%

REM Look for JRE 6
) else if exist %jre6x86% (
  set java=%jre6x86%

REM Look for default java command
) else if "%JAVA_HOME%" == "" (
  set java=java.exe

REM Look for default java path
) else (
  set java="%JAVA_HOME%\bin\java.exe"
)

%java% -Djava.library.path=.\lib\phybots\library\ -classpath "%picode%" com.phybots.picode.PicodeMain%*
