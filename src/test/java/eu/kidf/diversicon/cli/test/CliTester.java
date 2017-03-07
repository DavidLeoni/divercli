package eu.kidf.diversicon.cli.test;

import java.nio.file.Path;
import java.nio.file.Paths;

import eu.kidf.diversicon.cli.DiverCli;
import eu.kidf.diversicon.cli.commands.InitCommand;
import eu.kidf.diversicon.data.DivWn31;

/**
 * @since 0.1.0
 *
 */
public class CliTester {       

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
