package br.ufmg.dcc.labsoft.jextract.cli;

import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import br.ufmg.dcc.labsoft.jextract.cli.JExtractCLI;
import picocli.CommandLine;

public class JextractMain implements IApplication{

    @Override
    public Object start(final IApplicationContext context) throws Exception {
        System.out.println("Hello RCP World!");
        final Map<?,?> args = context.getArguments();
        final String[] appArgs = (String[]) args.get("application.args");
        for (final String arg : appArgs) {
            System.out.println(arg);
        }

        new CommandLine(new JExtractCLI()).execute(appArgs);

        return IApplication.EXIT_OK;
    }

    @Override
    public void stop() {

    }
}
