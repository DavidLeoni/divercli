package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.data.wn30.DivWn30;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Show imports done so far.")
public class LogCommand implements  DiverCliCommand {

    /**
     * @since 0.1
     */
    public static final String CMD = "log";

    
    private static final Logger LOG = LoggerFactory.getLogger(LogCommand.class);

    private DiverCli diverCli;
        
   
    public LogCommand(DiverCli diverCli){
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }
    
    @Override
    public void configure(){       
                
    }
    
    @Override
    public void run(){
        
        diverCli.connect();
        LOG.info("\n");
        LOG.info(diverCli.getDiversicon().formatDbStatus(true));
        LOG.info("\n");
        LOG.info(diverCli.getDiversicon().formatImportJobs(false));        
    }
    
    @Override
    public String getName() {
        return CMD;
    }
}
