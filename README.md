JExtract
========

JExtract is a Extract Method Refactoring recommendation tool

# Building the plugin
Follow the first two steps [here](https://medium.com/@ravi_theja/creating-your-first-eclipse-plugin-9b1b5ba33b58) 
to setup your eclipse environment.

# Running Custom Evaluation
1. Set the following environment variable 
   1. `PROJECT_NAME`
   2. `PROJECT_PATH`
   3. `DATA_FILE` - file containing the list of refactorings in the oracle. Use the same schema as [this](example-data.json) file.
   4. `OUT_DIRECTORY` - results from running jextract go here.
2. Launch Jextract 
3. Right click on project:

   `Jextract -> Find Extract Method Opportuniies ->`
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
