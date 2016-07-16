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
import it.unitn.disi.diversicon.internal.Internals;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Resets current database. If it doesn't exist it is created empty.")
public class DbResetCommand implements DiverCliCommand { 
    
    /**
     * @since 0.1.0
     */
    public static final String CMD = "db-reset";
    
    private static final Logger LOG = LoggerFactory.getLogger(DbResetCommand.class);

    private DiverCli diverCli;
        
    public DbResetCommand(DiverCli diverCli){
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }
        
    
    @Override
    public void configure(){                   
                
    }
    
    @Override
    public void run(){        
        Diversicons.dropCreateTables(diverCli.getDbConfig());        
    }
    
    @Override
    public String getName() {
        return CMD;
    }
}
