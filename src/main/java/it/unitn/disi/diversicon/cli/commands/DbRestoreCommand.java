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
import it.unitn.disi.diversicon.cli.exceptions.DiverCliException;
import it.unitn.disi.diversicon.data.DivWn31;
import it.unitn.disi.diversicon.internal.Internals;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Restores a file database")
public class DbRestoreCommand implements DiverCliCommand { 
    
    /**
     * @since 0.1.0
     */
    public static final String CMD = "db-restore";
    
    private static final Logger LOG = LoggerFactory.getLogger(DbRestoreCommand.class);

    private DiverCli diverCli;
        
    @Parameter(names = { "--sql" },  description = "Restores an h2 database from a sql dump. Dump can be expressed as a URL." 
    + " and can be in a compressed format."
    + " DB configuration MUST point to a non-existing database, otherwise"
    + " behaviour is unspecified. For Wordnet 3.0 packaged dump, you can use "
    + DivWn31.SQL_URI)
    String restoreSqlPath;

    @Parameter(names = { "--db" },  description = "Restores an h2 database from a .h2.db dump. Dump can be expressed as a URL." 
    + " and can be in a compressed format."
    + " DB configuration MUST point to a non-existing database, otherwise"
    + " behaviour is unspecified. For Wordnet 3.0 packaged dump, you can use "
    + DivWn31.H2DB_URI)
    String restoreH2DbPath;
    
    
    @Parameter(names = {"--target", "-t"}, 
            description = "The path to the database to create. For H2, don't include .h2.db .",
            required = true)
    String targetDbPath;
    
    @Parameter(names = {"--set-default", "-d"}, description = "Sets user configuration to use this database as default for successive operations.")
    boolean makeDefault = false;
    
    @Parameter(names = {"--create-conf", "-f"}, description = "Creates in the target database directory another directory named DBNAME.conf with the configuration needed to access the db")
    boolean createConf = false;

    public DbRestoreCommand(DiverCli diverCli){
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }
        
    
    /**
     * {@inheritDoc}
     * @since 0.1.0
     */
    @Override
    public void configure(){
        
        if (Internals.isBlank(restoreSqlPath) && Internals.isBlank(restoreH2DbPath)){
            throw new ParameterException("Need either --sql  or --db  argument!");
        }
        
        if (!Internals.isBlank(restoreSqlPath)
                && !Internals.isBlank(restoreH2DbPath)){
            throw new ParameterException("Tried to restore two files to same db! Files were:\n " + restoreH2DbPath + "\n   and\n     "
                    + restoreSqlPath);
        }     
                
    }
    
    /**
     * {@inheritDoc}
     * @since 0.1.0
     */
    @Override
    public void run(){
        
        DBConfig dbCfg = Diversicons.makeDefaultH2FileDbConfig(targetDbPath, false);
        
        if (!Internals.isBlank(restoreH2DbPath)){            
            Diversicons.restoreH2Db(restoreH2DbPath, targetDbPath);    
        } else if (!Internals.isBlank(restoreSqlPath)){        
            Diversicons.restoreH2Sql(restoreSqlPath, dbCfg);    
        } 
        
        if (createConf){        
            File targetDb = new File(targetDbPath);
                        
            File parentFolder = targetDb.getParentFile();
            if (!parentFolder.isDirectory()){
                throw new DiverCliException("Expected a directory where to place the configuration, found instead: " + parentFolder.getAbsolutePath());
            }
            File configFolder = new File(parentFolder, targetDb.getName() + "-conf");
            DiverCli.saveConfig(dbCfg, configFolder);
            LOG.info("Created configuration folder " + configFolder.getAbsolutePath());
        }
        
        if (makeDefault){
            diverCli.setDbConfig(dbCfg);
            diverCli.saveConfig();
            LOG.info("Set default configuration to track " + new File(targetDbPath).getAbsolutePath() + " database");
        }
        
        
        
    }
    
    /**
     * {@inheritDoc}
     * @since 0.1.0
     */
    @Override
    public String getName() {
        return CMD;
    }
}
