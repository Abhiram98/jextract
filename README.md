JExtract
========

JExtract is a Extract Method Refactoring recommendation tool

# CLI

Java version: java 11
```
java -XstartOnFirstThread \
-Dorg.eclipse.swt.graphics.Resource.reportNonDisposed=true \
--add-modules=ALL-SYSTEM -Dfile.encoding=UTF-8 \
-classpath /Applications/Eclipse.app/Contents/Eclipse/plugins/org.eclipse.equinox.launcher_1.6.400.v20210924-0641.jar org.eclipse.equinox.launcher.Main \
-application br.ufmg.dcc.labsoft.jextract.cli \
-data /Users/abhiram/eclipse-workspace/../runtime-br.ufmg.dcc.labsoft.jextract.cli \
-configuration file:/Users/abhiram/eclipse-workspace/.metadata/.plugins/org.eclipse.pde.core/br.ufmg.dcc.labsoft.jextract.cli/ \
-dev file:/Users/abhiram/eclipse-workspace/.metadata/.plugins/org.eclipse.pde.core/br.ufmg.dcc.labsoft.jextract.cli/dev.properties \
-os macosx -ws cocoa -arch aarch64 -nl en_US -consoleLog
```
