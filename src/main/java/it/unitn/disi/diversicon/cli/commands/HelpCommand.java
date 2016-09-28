package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.cli.DiverCli;

/**
 * Prints help about a given command
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Print help about a given command")
public class HelpCommand implements DiverCliCommand {

    private static final Logger LOG = LoggerFactory.getLogger(HelpCommand.class);

    /**
     * @since 0.1.0
     */
    public static final String CMD = "help";

    @Parameter(description = "Prints help about a given command.", arity = 1)
    private List<String> commandNameList;

    private DiverCli diverCli;

    /**
     * @since 0.1.0
     */
    public HelpCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void configure() {
        // empty, for now diversicon will do the checks
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void run() {
        LOG.info("");
        if (commandNameList == null || commandNameList.isEmpty()) {
            StringBuilder sb = new StringBuilder();            
            diverCli.getJCommander()
                    .usage(sb);
            LOG.info(sb.toString());
        } else {
            StringBuilder sb = new StringBuilder();
            String cmd = commandNameList.get(0);
            if (diverCli.getCommands()
                        .keySet()
                        .contains(cmd)) {
                diverCli.getJCommander()
                        .usage(cmd, sb);
                LOG.info(sb.toString());
            } else {
                diverCli.didYouMean(cmd);
            }

        }
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public String getName() {
        return CMD;
    }

}
