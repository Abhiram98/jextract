package br.ufmg.dcc.labsoft.jextract.cli;

import java.lang.Runnable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command
public class JExtractCLI implements Runnable{
    @Option(names = "--projectDirectory", interactive = true)
    String projectDirectory = null;

    @Option(names = "--methodName", interactive = true)
    String methodName = null;

    @Override
    public void run() {
        System.out.println("Running!");
        System.out.println(projectDirectory);
        System.out.println(methodName);
    }
}
