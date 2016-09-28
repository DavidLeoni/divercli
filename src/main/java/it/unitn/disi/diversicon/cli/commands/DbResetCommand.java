package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.cli.DiverCli;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Reset current database. If it doesn't exist it is created empty.")
public class DbResetCommand implements DiverCliCommand {

    /**
     * @since 0.1.0
     */
    public static final String CMD = "db-reset";

    private DiverCli diverCli;

    public DbResetCommand(DiverCli diverCli) {
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
        Diversicons.dropCreateTables(diverCli.dbConfig());
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
