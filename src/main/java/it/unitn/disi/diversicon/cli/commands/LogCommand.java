package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.cli.DiverCli;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Show imports done so far.")
public class LogCommand implements DiverCliCommand {

    /**
     * @since 0.1.0
     */
    public static final String CMD = "log";

    private static final Logger LOG = LoggerFactory.getLogger(LogCommand.class);

    private DiverCli diverCli;

    /**
     * @since 0.1.0
     */
    public LogCommand(DiverCli diverCli) {
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

    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void run() {

        diverCli.connect();
        LOG.info("\n");
        LOG.info(diverCli.getDiversicon()
                         .formatDbStatus(true));
        LOG.info("\n");
        LOG.info(diverCli.getDiversicon()
                         .formatImportJobs(false));
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
