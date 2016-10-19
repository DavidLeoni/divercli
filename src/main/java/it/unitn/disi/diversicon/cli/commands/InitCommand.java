package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotBlank;
import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliException;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliIoException;
import it.unitn.disi.diversicon.data.DivWn31;
import it.unitn.disi.diversicon.internal.Internals;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Sets up current folder with a new database project.")
public class InitCommand implements DiverCliCommand { 
    
    /**
     * @since 0.1.0
     */
    public static final String CMD = "init";
    
    private static final Logger LOG = LoggerFactory.getLogger(InitCommand.class);

    private DiverCli cli;
        
    @Parameter(names = { "--sql" },  description = "Initalize the project with a h2 database from a sql dump, "
            + "which can be compressed and expressed as a URL" ) // todo put smartphones url
    String restoreSqlPath;

    @Parameter(names = { "--db" },  description = "Initialize the project from a .h2.db dump, "
            + "which can be compressed and expressed as a URL. For Wordnet 3.1 packaged dump, you can use "
    + DivWn31.H2DB_URI)
    String restoreH2DbPath;       
    
    private File prjFolder;
    private File iniFile;
    private String dbName;
    

    public InitCommand(DiverCli diverCli){
        checkNotNull(diverCli);
        this.cli = diverCli;
    }
        
    
    /**
     * {@inheritDoc}
     * @since 0.1.0
     */
    @Override
    public void configure(){        
        
        prjFolder = cli.getProjectDir();
        iniFile = new File(prjFolder, DiverCli.INI_FILENAME);
        dbName = new File(prjFolder.getAbsolutePath()).getName();
        checkNotBlank("Invalid db name!", dbName);
        
        if (prjFolder.exists()){
            if (prjFolder.isDirectory()){
                if (new File(prjFolder, DiverCli.INI_FILENAME).exists()){
                    throw new DiverCliException("Target directory "+ prjFolder.getAbsolutePath()
                    + " already contains a " + DiverCli.INI_FILENAME + " file!");
                }
                
                File targetDb = new File(prjFolder , dbName + ".h2.db");
                if (targetDb.exists()){
                    throw new DiverCliException("Target database already exists: " + targetDb.getAbsolutePath() );
                }
            } else{
                throw new DiverCliException("Target project path is not a directory: " + prjFolder.getAbsolutePath());
            }                            
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
        
        String targetDbPath = prjFolder.getAbsolutePath() + "/" + dbName;
        
        DBConfig dbCfg = Diversicons.h2MakeDefaultFileDbConfig(
                targetDbPath, false);                         
                
        cli.setDbConfig(dbCfg);
                
        if (!Internals.isBlank(restoreH2DbPath)){            
            Diversicons.restoreH2Db(restoreH2DbPath, targetDbPath);    
        } else if (!Internals.isBlank(restoreSqlPath)){        
            Diversicons.h2RestoreSql(restoreSqlPath, dbCfg);    
        }  else {
            Diversicons.dropCreateTables(dbCfg);
        }                                                                               
               
        
        Internals.copyDirFromResource(DiverCli.class,
                DiverCli.TEMPLATES_DIR + Diversicons.getDatabaseId(dbCfg),
                prjFolder);

        try {

            Wini ini = new Wini(iniFile);

            ini.put(DiverCli.DATABASE_SECTION_INI, "jdbc_driver_class", dbCfg.getJdbc_driver_class());
            ini.put(DiverCli.DATABASE_SECTION_INI, "db_vendor", dbCfg.getDb_vendor());
            ini.put(DiverCli.DATABASE_SECTION_INI, "jdbc_url", dbCfg.getJdbc_url());
            ini.put(DiverCli.DATABASE_SECTION_INI, "user", dbCfg.getUser());
            ini.put(DiverCli.DATABASE_SECTION_INI, "password", dbCfg.getPassword());
            ini.store();
        } catch (IOException ex) {
            throw new DiverCliIoException(
                    "Error while saving INI file to " + iniFile.getAbsolutePath(),
                    ex);
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
