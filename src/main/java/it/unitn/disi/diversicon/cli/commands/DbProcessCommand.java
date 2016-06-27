package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.cli.DiverCli;

/** 
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Processes the db graph to speed up "
        + " operations requiring the transitive closure.")
public class DbProcessCommand implements DiverCliCommand {

    
    /**
     * @since 0.1
     */
    public static final String CMD = "db process";

    DiverCli diverCli;
    
    public DbProcessCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    @Override
    public void configure(){
        // empty, for now diversicon will do the checks
    }
    
    @Override
    public void run() {
        diverCli.connect();
        diverCli.getDiversicon().processGraph();
    }

    @Override
    public String getName() {
        return CMD;
    }


}




