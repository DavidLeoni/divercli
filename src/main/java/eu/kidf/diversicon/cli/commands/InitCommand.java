package eu.kidf.diversicon.cli.commands;

import static eu.kidf.diversicon.core.internal.Internals.checkNotBlank;
import static eu.kidf.diversicon.core.internal.Internals.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import eu.kidf.diversicon.cli.DiverCli;
import eu.kidf.diversicon.cli.exceptions.DiverCliException;
import eu.kidf.diversicon.cli.exceptions.DiverCliIoException;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.Diversicons;
import eu.kidf.diversicon.core.internal.Internals;
import eu.kidf.diversicon.data.DivWn31;

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
    private File prjIniFile;
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
        prjIniFile = new File(prjFolder, DiverCli.INI_FILENAME);
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
     * @since 0.1.0
     */
    // taken from http://mailman.qos.ch/pipermail/logback-user/2008-November/000751.html
    private List<ch.qos.logback.classic.Logger> findLoggers(String prefix) {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      List<ch.qos.logback.classic.Logger> ret = new ArrayList<>();
      for (ch.qos.logback.classic.Logger log : lc.getLoggerList()) {
        if(log.getLevel() != null || hasAppenders(log)) {
          //if (log.getName().startsWith(prefix)){
            
              ret.add(log);
          //}
        }
      }
      return ret;
    }

    /**
     * @since 0.1.0
     */
    private boolean hasAppenders(ch.qos.logback.classic.Logger logger) {
      Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
      return it.hasNext();
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
            Diversicons.h2RestoreSql(restoreSqlPath, cli.divConfig());    
        }  else {
            // Output change needed because drop create is importing 
            // div-upper, see https://github.com/diversicon-kb/diversicon-core/issues/33
 
            
            PrintStream savedOut = System.out;           

            LOG.info("Recreating tables in database  " + dbCfg.getJdbc_url() + " ...");

                       
            LoggerContext lc =
                     ((ch.qos.logback.classic.Logger)LOG).getLoggerContext();
                                     List<String> strList = new ArrayList<String>();
                                     Iterator<ch.qos.logback.classic.Logger> it =
                     lc.getLoggerList().iterator();
                                     while(it.hasNext()) {
                                       Logger log = it.next();
                                       strList.add(log.getName());
                                     }
            

            List<ch.qos.logback.classic.Logger> loggers = findLoggers(Diversicons.class.getPackage().getName());
            List<Level> savedLevels = new ArrayList<>();
            
            try {
                for (ch.qos.logback.classic.Logger log : loggers){
                    savedLevels.add(log.getLevel());
                    Level lev = log.getLevel();
                    
                    if (lev == null || lev.isGreaterOrEqual(Level.ERROR)){                                   
                        log.setLevel(Level.OFF);  // i.e. because of bastard Hibernate ...
                    } else {
                        log.setLevel(Level.ERROR); // because warnings will be issued for missing db namespaces
                    }
                        
                }
            
                
                // because of UBY printlns, just setting log level isn enough :-/                
                System.setOut(new PrintStream(
                        new OutputStream() { @Override public void write(int b) { }}
                        ));
                Diversicons.dropCreateTables(dbCfg);            
            } finally {
                System.setOut(savedOut);
                int i = 0;
                for (ch.qos.logback.classic.Logger log : loggers){
                    if (i >=  savedLevels.size()){
                        System.err.println("ERROR WHILE RESTORING LOGGERS !!");
                        break;
                    }
                    log.setLevel(savedLevels.get(i)); // because warnings will be issued for missing db namespaces
                    i++;
                } 

                               
            }
        }                                                                               
               
        
        Internals.copyDirFromResource(DiverCli.class,
                DiverCli.TEMPLATES_DIR + Diversicons.getDatabaseId(dbCfg),
                prjFolder);

        try {

            Wini prjIni = new Wini(prjIniFile);

            prjIni.put(DiverCli.DATABASE_SECTION_INI, "jdbc_driver_class", dbCfg.getJdbc_driver_class());
            prjIni.put(DiverCli.DATABASE_SECTION_INI, "db_vendor", dbCfg.getDb_vendor());
            prjIni.put(DiverCli.DATABASE_SECTION_INI, "jdbc_url", dbCfg.getJdbc_url());
            prjIni.put(DiverCli.DATABASE_SECTION_INI, "user", dbCfg.getUser());
            prjIni.put(DiverCli.DATABASE_SECTION_INI, "password", dbCfg.getPassword());
            prjIni.store();
        } catch (IOException ex) {
            throw new DiverCliIoException(
                    "Error while saving INI file to " + prjIniFile.getAbsolutePath(),
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
