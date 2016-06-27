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
@Parameters(separators = "=", commandDescription = "Creates a file database")
public class DbCreateCommand implements DiverCliCommand { 
    
    /**
     * @since 0.1
     */
    public static final String CMD = "db-create";
    
    private static final Logger LOG = LoggerFactory.getLogger(DbCreateCommand.class);

    private DiverCli diverCli;
        
    @Parameter(names = { "--sql" },  description = "Restores an h2 database from a sql or a .h2.db dump. Dump can be expressed as a URL." 
    + " and can be in a compressed format."
    + " DB configuration MUST point to a non-existing database, otherwise"
    + " behaviour is unspecified. For Wordnet 3.0 packaged dump, you can use "
    + DivWn30.WORDNET_DIV_SQL_RESOURCE_URI)
    String restoreSqlPath = "";

    @Parameter(names = { "--db" },  description = "Restores an h2 database from a .h2.db dump. Dump can be expressed as a URL." 
    + " and can be in a compressed format."
    + " DB configuration MUST point to a non-existing database, otherwise"
    + " behaviour is unspecified. For Wordnet 3.0 packaged dump, you can use "
    + DivWn30.WORDNET_DIV_H2_DB_RESOURCE_URI)
    String restoreH2DbPath = "";
    
    
    @Parameter(names = {"--target", "-t"}, 
            description = "The path to the database to create. For H2, don't include .h2.db .",
            required = true)
    String targetDbPath = "";
    
    @Parameter(names = {"--set-default", "-d"}, description = "Sets user configuration to use this database as default for successive operations.")
    boolean makeDefault = false;

    public DbCreateCommand(DiverCli diverCli){
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }
    
    @Override
    public void configure(){
        if (!restoreH2DbPath.trim().isEmpty()
                && !restoreSqlPath.trim().isEmpty()){
            throw new ParameterException("Tried to restore two files to same db! Files were:\n " + restoreH2DbPath + "\n   and\n     "
                    + restoreSqlPath);
        }     
                
    }
    
    @Override
    public void run(){
        
        DBConfig dbCfg = Diversicons.makeDefaultH2FileDbConfig(targetDbPath);
        
        if (!restoreH2DbPath.trim().isEmpty()){            
            Diversicons.restoreH2Db(restoreH2DbPath, targetDbPath);    
        }
                       
        if (!restoreSqlPath.trim().isEmpty()){        
            Diversicons.restoreH2Sql(restoreSqlPath, dbCfg);    
        }
        
        if (makeDefault){
            diverCli.setDbConfig(dbCfg);
            diverCli.saveConfig();
            LOG.info("Set default configuration to track " + new File(targetDbPath).getAbsolutePath() + " database");
        }
        
    }
    
    @Override
    public String getName() {
        return CMD;
    }
}
