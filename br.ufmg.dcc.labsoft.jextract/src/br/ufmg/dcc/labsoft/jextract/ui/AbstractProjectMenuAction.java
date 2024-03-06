package br.ufmg.dcc.labsoft.jextract.ui;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileExporter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

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

					analyseProjectCommits(project, settings);
				}
			};
			job.schedule();
		}
	}

	private static void analyseProjectCommits(IProject project, Settings settings) throws InterruptedException, IOException {
		JsonParser parser = new JsonParser();
		String projectName = System.getenv("PROJECT_NAME");
		String projectPath = System.getenv("PROJECT_PATH");
		String dataFile = System.getenv("DATA_FILE");
		String outDir = System.getenv("OUT_DIRECTORY");
		if (projectName==null || projectPath == null || dataFile == null|| outDir==null){
			System.out.println("One or more required environment variables not set.");
			return;
		}
        String basePath = new File(projectPath).getParent();

		copyClasspath(basePath, projectName, projectPath);
		copyDotProject(basePath, projectName, projectPath);

//		String jsonFileName =
//				String.format("%s/%s-data.json", dataDirectory, projectName);
//					String jsonFileName =
//							String.format("/Users/abhiram/Documents/JetGPT/extended_corpus/%s-data.json", projectName);
//					String outDir = "/Users/abhiram/Documents/JetGPT/extended_corpus/JExtractOut/";
//					String projectName = "intellij-community";
//					String projectPath = project.getRawLocation().toString();
//					String projectPath = String.format("/Users/abhiram/Documents/JetGPT/extended_corpus/projects/%s",projectName);
//					String basePath = "/Users/abhiram/Documents/JetGPT/extended_corpus/projects";

		JsonArray data = (JsonArray) parser.parse(new FileReader(dataFile));

		Integer count = 0;
		Integer limit = 2000; // this variable limits the number of internal Jextract calls.


		List<Integer> completedList = getCompletedRefactorings(parser, basePath, projectName);

		for (Object obj : data) {
			System.out.println(count+" Complteted.");
			if (completedList.contains(count)) {
				System.out.println(count+ " done");
				count +=1;
				continue;
			}
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

			count+=1;

//			 Change git to Sha
			Runtime rt = checkoutGitSHA(projectPath, sha);

			restoreDotProject(basePath, projectName, projectPath);
			restoreClasspath(basePath, projectName, projectPath);
			String outFile = outDir+projectName+"-"+count.toString();

			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			}catch (Exception e){
				System.out.println("Couldn't refresh project");
				PrintWriter writer = new PrintWriter(outFile, "UTF-8");
				writer.println("Failed to refresh project.");
				writer.close();
				continue;
			}


			// Run Jextract
			final List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			final EmrGenerator generator = new EmrGenerator(recomendations, settings);
			generator.setOnlyThisFile(fileName);
			generator.setOnlyThisMethodStr(functionName);

			try {

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
	}

	private static Runtime checkoutGitSHA(String projectPath, String sha) throws InterruptedException, IOException {
		Runtime rt = Runtime.getRuntime();
		String[] gitChange = {"git", "-C", projectPath, "checkout", "-f", sha};
		rt.exec(gitChange).waitFor();
		return rt;
	}

	private static List<Integer> getCompletedRefactorings(JsonParser parser, String basePath, String projectName) {
		JsonArray completed;
		try {
			 completed = (JsonArray) parser.parse(
					new FileReader(
							String.format("%s/%s-completed.json",
									basePath,
									projectName)));
		} catch (FileNotFoundException e){
			return new ArrayList<Integer>();
		}
		List<Integer> completedList = new ArrayList<Integer>();
		for (Object c : completed) {
			completedList.add(Integer.parseInt(c.toString()));
		}
		return completedList;
	}

	private static void restoreClasspath(String basePath, String projectName, String projectPath) throws InterruptedException, IOException {
		Runtime rt = Runtime.getRuntime();
		String [] cpClasspath = {"cp", String.format("%s/.%sclasspath", basePath, projectName),
				String.format("%s/.classpath", projectPath)};
		File f2 = new File(String.format("%s/.classpath", projectPath));
		if(!f2.exists()) {
			rt.exec(cpClasspath).waitFor();
		}
	}
	private static void copyClasspath(String basePath, String projectName, String projectPath) throws InterruptedException, IOException {
		Runtime rt = Runtime.getRuntime();
		String [] cpClasspath = {
				"cp",
				String.format("%s/.classpath", projectPath),
				String.format("%s/.%sclasspath", basePath, projectName)
		};
		rt.exec(cpClasspath).waitFor();
	}

	private static void restoreDotProject(String basePath, String projectName, String projectPath) throws InterruptedException, IOException {
		Runtime rt = Runtime.getRuntime();
		String [] cpProject = {"cp",
				String.format("%s/.%sproject", basePath, projectName),
				String.format("%s/.project", projectPath)};
		File f = new File(String.format("%s/.project", projectPath));
		if(!f.exists()) {
			// do something
			rt.exec(cpProject).waitFor();
		}
	}

	private static void copyDotProject(String basePath, String projectName, String projectPath) throws InterruptedException, IOException {
		Runtime rt = Runtime.getRuntime();
		String [] cpProject = {
				"cp",
				String.format("%s/.project", projectPath),
				String.format("%s/.%sproject", basePath, projectName)
		};
		rt.exec(cpProject).waitFor();
	}

}
