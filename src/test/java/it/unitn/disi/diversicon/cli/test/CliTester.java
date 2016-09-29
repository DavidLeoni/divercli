package it.unitn.disi.diversicon.cli.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.commands.InitCommand;
import it.unitn.disi.diversicon.data.DivWn31;

/**
 * @since 0.1.0
 *
 */
public class CliTester {

    private static final Logger LOG = LoggerFactory.getLogger(CliTester.class);   
    
    
    /**
     * @since 0.1.0
     */
    public static void createTestEnv() {

    }


    /**
     * Makes default db project in directory {@code working/ }
     * so to have 
     * <pre>
     *    working/divercli.ini 
     *    working/working.h2.db
     *    ... 
     * </pre>
     * 
     * @since 0.1.0
     */
    public static DiverCli initEmpty() {        
        DiverCli cli = DiverCli.of(InitCommand.CMD);
        cli.run();
        return cli;
    }
    
    public static DiverCli initWn31(){
        DiverCli cli = DiverCli.of(InitCommand.CMD, "--db", DivWn31.H2DB_URI);
        cli.run();
        
        return cli;
    }

    /**
     * @since 0.1.0
     */
    public static Path getTestGlobalConfDir() {
        String prop = System.getProperty(DiverCli.SYSTEM_PROPERTY_USER_HOME);
        
        return Paths.get(prop + "/" + DiverCli.GLOBAL_CONF_DIR + DiverCli.INI_FILENAME); 
    }
    
}
