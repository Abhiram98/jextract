package br.ufmg.dcc.labsoft.jextract.ui;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileExporter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;


public abstract class AbstractProjectMenuAction<T> extends ObjectMenuAction<T> {

	public AbstractProjectMenuAction(Class<T> objectType) {
		super(objectType);
	}

	@Override
	public void handleAction(IAction action, List<T> projects) throws Exception {
		String actionId = action.getId();
		final IProject project = convertoToIProject(projects.get(0));
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.findEmr")) {
			findEmr(project);
		}
	}

	protected abstract IProject convertoToIProject(T object);

	private void findEmr(final IProject project) throws Exception {
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			final Settings settings = dialog.getSettings();
			AbstractJob job = new AbstractJob("Generating Recommendations") {
				@Override
				protected void doWorkIteration(int i, IProgressMonitor monitor) throws Exception {

					JsonParser parser = new JsonParser();
					String projectName = "CoreNLP";
					String subproject = "";
//					String projectName = "intellij-community";
//					String subproject = "platform";
					String jsonFileName =
							String.format("/Users/abhiram/Documents/JetGPT/extended_corpus/%s-data.json", projectName);
					String outDir = "/Users/abhiram/Documents/JetGPT/extended_corpus/JExtractOut/";
					JsonArray data = (JsonArray) parser.parse(new FileReader(jsonFileName));
//					String projectPath = project.getRawLocation().toString();
					String projectPath = String.format("/Users/abhiram/Documents/JetGPT/extended_corpus/projects/%s",projectName);
					String basePath = "/Users/abhiram/Documents/JetGPT/extended_corpus/projects";
					Integer count = 0;
					Integer limit = 2000;


					JsonArray completed = (JsonArray) parser.parse(
							new FileReader(
								String.format("/Users/abhiram/Documents/JetGPT/extended_corpus/%s-completed.json",
										projectName)));
					List<Integer> completedList = new ArrayList<Integer>();
					for (Object c : completed) {
						completedList.add(Integer.parseInt(c.toString()));
					}

					for (Object obj : data) {
						System.out.println(count+" Complteted.");
//						if (completedList.contains(count)) {
//							System.out.println(count+ " done");
//							count +=1;
//							continue;
//						}
						if (count > limit){
							break;
						}
						JsonObject ref = (JsonObject) obj;
						String pname = ref.get("projectName").toString().replaceAll("\"", "");
						String sha = ref.get("sha").toString().replaceAll("\"", "");
						String fileName = ref.get("filename").toString().replaceAll("\"", "");
						String functionName = ref.get("functionName").toString().replaceAll("\"", "");
						System.out.println(pname);
						System.out.println(sha);
						System.out.println(fileName);
						System.out.println(functionName);

						if (subproject.equals("") || fileName.startsWith(subproject+"/")){
							System.out.println("Found subproject");
						}
						else{
							count+=1;
							continue;
						}

//						project.close(null);
						// Change git to Sha
						Runtime rt = Runtime.getRuntime();
						String[] gitChange = {"git", "-C", projectPath, "checkout", "-f", sha};
						rt.exec(gitChange).waitFor();

						// Refresh project.
//						BufferedReader stdInput = new BufferedReader(new
//								InputStreamReader(rt.exec("pwd").getInputStream()));
//						System.out.println("Here is the standard output of the command:\n");
//						String s = null;
//						while ((s = stdInput.readLine()) != null) {
//							System.out.println(s);
//						}

						String p2;
						if (subproject.equals("")){
							p2="";
						}else{
							p2=subproject+"/";
						}

						String [] cpProject = {"cp",
								String.format("%s/.%sproject", basePath, projectName),
								String.format("%s/%s.project", projectPath, p2)};
						File f = new File(String.format("%s/%s.project", projectPath, p2));
						if(!f.exists()) {
							// do something
							rt.exec(cpProject).waitFor();
						}
						String [] cpClasspath = {"cp", String.format("%s/.%sclasspath", basePath, projectName),
								String.format("%s/%s.classpath", projectPath, p2)};
						File f2 = new File(String.format("%s/%s.classpath", projectPath, p2));
						if(!f2.exists()) {
							// do something
							rt.exec(cpClasspath).waitFor();
						}
//						project.open(null);
						String outFile = outDir+projectName+"-"+count.toString();

						try {
//							project.refreshLocal(IResource.DEPTH_INFINITE, null);
//							project.refreshLocal(1, null);
							String splitSrc = fileName.split("/src")[0];
							String srcFolder = String.format("%s/src", splitSrc);
							project.refreshLocal(IResource.DEPTH_INFINITE, null);
						}catch (Exception e){
							System.out.println("Couldn't refresh project");
							PrintWriter writer = new PrintWriter(outFile, "UTF-8");
							writer.println("Failed to refresh project.");
							writer.close();
							continue;
						}

						String resultsTxt = "";

						// TODO: Run Jextract
//						project.getFile("").getClass().getMethods();
//						JavaCore.create(project).findModule().fi;

						final List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
						final EmrGenerator generator = new EmrGenerator(recomendations, settings);
						if (subproject.equals("")) {
							generator.setOnlyThisFile(fileName);
						}
						else {
							String[] splitPaths = fileName.split(subproject + "/");
							generator.setOnlyThisFile(splitPaths[1]);
							generator.setOnlyThisMethodStr(functionName);
						}
						try {
//							Method[] classMethods = project.getFile(fileName).getClass().getMethods();
//							for(Method m: classMethods){
//								if (m.getName().equals(functionName)){
//									System.out.println("Found function!");
//									generator.generateRecomendations(m);
//								}
//							}

							generator.generateRecomendations(project);
							if (recomendations.size() >0) {
								new EmrFileExporter(recomendations, outFile).export();
							}
							else{
								PrintWriter writer = new PrintWriter(outFile, "UTF-8");
								writer.println("foundFile="+generator.foundFile);
								writer.println("foundMethod="+generator.foundMethod);
								writer.println("noSource="+generator.noSource);
								writer.close();
							}
						} catch (Exception e){
							System.out.println("EMR failed.");
							File file = new File(outFile);
							PrintStream ps = new PrintStream(file);
							e.printStackTrace(ps);
							ps.close();
						}


						count += 1;



					}


//					final List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
//					final EmrGenerator generator = new EmrGenerator(recomendations, settings);
//					generator.generateRecomendations(project);
//
//					Display.getDefault().asyncExec(new Runnable() {
//						@Override
//                        public void run() {
//							try {
//								showResultView(recomendations, project, settings);
//							} catch (Exception e) {
//								throw new RuntimeException(e);
//							}
//						}
//					});
				}
			};
			job.schedule();
		}
	}

}
