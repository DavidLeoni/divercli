package it.unitn.disi.diversicon.cli.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.commands.InitCommand;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliIoException;
import it.unitn.disi.diversicon.data.DivWn31;
import it.unitn.disi.diversicon.internal.Internals;

/**
 * @since 0.1.0
 *
 */
public class CliTester {

    private static final Logger LOG = LoggerFactory.getLogger(CliTester.class);

    public static final String WORKING = "working";
    
    /**
     * @since 0.1.0
     */
    public static TestEnv createTestEnv() {

        try {
            Path testHome = Internals.createTempDir("divercli-test-home");

            TestEnv ret = new TestEnv(testHome,
                    Files.createDirectories(Paths.get(testHome.toString(), WORKING)),
                    Paths.get(testHome.toString(), ".config", "divercli"));


            System.setProperty(DiverCli.SYSTEM_PROPERTY_USER_HOME,
                    ret.getTestHome()
                       .toString());
            
            System.setProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR,
                    ret.getTestWorkingDir()
                       .toString());
            
            
            
            // so it always work even in stupid Eclipse, see see
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=388683
            System.setProperty(DiverCli.SYSTEM_PROPERTY_TESTING, "true");

            return ret;
        } catch (IOException e) {
            throw new DiverCliIoException("Something went wrong!", e);
        }
    }

    /**
     * @since 0.1.0
     */
    public static void resetTestEnv() {
        System.setProperty(DiverCli.SYSTEM_PROPERTY_USER_HOME, "");
        System.setProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR, "");
        System.setProperty(DiverCli.SYSTEM_PROPERTY_TESTING, "");
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
    
}
