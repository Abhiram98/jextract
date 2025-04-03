JExtract
========

JExtract is a Extract Method Refactoring recommendation tool

# Building the plugin
Follow the first step [here](https://medium.com/@ravi_theja/creating-your-first-eclipse-plugin-9b1b5ba33b58) 
to setup your eclipse environment.

# Running Custom Evaluation
1. Set the following environment variable 
   1. `PROJECT_NAME`
   2. `PROJECT_PATH`
   3. `DATA_FILE` - file containing the list of refactorings in the oracle. Use the same schema as [this](example-data.json) file.
   4. `OUT_DIRECTORY` - results from running jextract go here.
2. Launch Jextract 
3. Right click on project:

   `Jextract -> Find Extract Method Opportuniies -> <Choose Settings> -> OK`
4. Results of running JExtract will be saved in this format:
   `OUT_DIRECTORY/<PROJECT-NAME>-<COUNT>.txt`
