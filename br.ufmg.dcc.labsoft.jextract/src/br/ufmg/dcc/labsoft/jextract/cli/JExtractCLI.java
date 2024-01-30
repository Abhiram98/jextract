package br.ufmg.dcc.labsoft.jextract.cli;

import java.io.File;
import java.lang.Runnable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command
public class JExtractCLI implements Runnable{
    @Option(names = "--projectDirectory")
    String projectDirectory = null;
    @Option(names = "--methodName")
    String methodName = null;
    @Option(names = "--minMethodStatements")
    Integer minMethodStatements = 3;
    @Option(names = "--minExtractedStatements")
    Integer minExtractedStatements = 3;
    @Option(names = "--maxRecoPerMethod")
    Integer maxRecoPerMethod = 3;
    @Option(names = "--maxFragments")
    Integer maxFragments = 1;
    @Option(names = "--reorderPenalty")
    Integer reorderPenalty = 1;
    @Option(names = "--minScore")
    Double minScore = 0.0;
    @Option(names = "--resultsDirectory")
    String resultsDirectory = "out";


    private IProject openProject() throws CoreException{

        String[] subPaths = projectDirectory.split("/");
        String projectName = subPaths[subPaths.length-1];
        System.out.println("ProjectName: "+ projectName);
        IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] allProjects = workspace.getProjects();
        IProgressMonitor monitor = new NullProgressMonitor();

        IProject project =
                workspace.getProject(projectName);
        if (project.exists()){
//            project.close(monitor);
//            project.open(monitor);
//            project.delete(false ,monitor);
            System.out.println(project.isOpen());
            if(!project.isOpen()) {
                project.open(monitor);
            }
            return project;
//            IJavaProject javaProject = JavaCore.create(project);
//            System.out.println("ClassPATH:::");
//            for (IClasspathEntry cp : javaProject.getRawClasspath()){
//                System.out.println(cp.getPath());
//            }
//            return javaProject.getProject();
        }
        else {
            System.out.println("Importing new project!");
            IProjectDescription description =
                    ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
            description.setLocation(new Path(projectDirectory));
            project.create(description, monitor);
            project.open(monitor);
            System.out.println(project.getDescription());
        }
        IJavaProject javaProject = JavaCore.create(project);
//        javaProject.open(null);
        System.out.println("ClassPATH:::");
        for (IClasspathEntry cp : javaProject.getRawClasspath()){
            System.out.println(cp.getPath());
        }
        System.out.println("end of classpath.");

//        IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
//            public String queryOverwrite(String file) { return ALL; }
//        };
//
//        String baseDir = projectDirectory; // location of files to import
//        ImportOperation importOperation = new ImportOperation(project.getFullPath(),
//                new File(baseDir), FileSystemStructureProvider.INSTANCE, overwriteQuery);
//        importOperation.setCreateContainerStructure(false);
//        try {
//            importOperation.run(new NullProgressMonitor());
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return project;

    }

    @Override
    public void run(){
        System.out.println("Running!");
        System.out.println(projectDirectory);
        System.out.println(methodName);
        if (projectDirectory==null){
            System.out.println("No project chosen.");
            return;
        }
        // TODO: Open project in eclipse.
        IProject project = null;
        try{
            project = openProject();
        } catch (CoreException e){
            System.out.println("failed to open project: " + e);
            e.printStackTrace(System.out);
            return;
        }
//        project.getResourceAttributes().setArchive();
        // TODO: Find method in project.
//        IPath path;
//        project.getFile("something", path).getClass().getDeclaredMethods();

        // TODO: Generate Settings Obj.
        IJavaProject javaProject = JavaCore.create(project);
        try {
            javaProject.open(null);
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }
        Settings settings = new Settings();
        settings.setMinMethodSize(minMethodStatements);
        settings.setMinScore(minScore);
        settings.setMaxPerMethod(maxRecoPerMethod);
        settings.setPenalty(reorderPenalty);
        settings.setMinExtractedSize(minExtractedStatements);
        settings.setMaxFragments(maxFragments);
//        settings.javaProject = javaProject;


        // TODO: Call JExtract.
        System.out.println("ProjectName:"+project.getName());
        List<ExtractMethodRecomendation> recommendations = new ArrayList<ExtractMethodRecomendation>();
        EmrGenerator analyser = new EmrGenerator(recommendations, settings);
        try{
            analyser.generateRecomendations(project);
        } catch (Exception e){
            System.out.println("Failed to generate recommendations: "+ e);
            e.printStackTrace(System.out);
        }
        System.out.println(recommendations.size()+ " recommendations generated.");
        System.out.println(recommendations);

    }
}
