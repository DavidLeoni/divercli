package it.unitn.disi.diversicon.cli.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.ini4j.Wini;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.commands.InitCommand;
import it.unitn.disi.diversicon.cli.commands.LogCommand;
import it.unitn.disi.diversicon.data.DivWn31;
import it.unitn.disi.diversicon.internal.Internals;

/**
 * @since 0.1.0
 */
public class DiverCliIT extends DiverCliTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(DiverCliIT.class);

    /**
     * @since 0.1.0
     */
    @Test
    public void testInitDbFromWordnetDb() throws IOException {                      
                
        DiverCli cli = DiverCli.of(
                InitCommand.CMD,                
                "--db", DivWn31.H2DB_URI);

        cli.run();

        File outf = new File(testEnv.getTestWorkingDir() + "/" + CliTester.WORKING + ".h2.db");

        assertTrue(outf.exists());
        assertTrue(outf.length() > 0);

        File outIni = new File(testEnv.getTestWorkingDir() + "/" + DiverCli.DIVERCLI_INI);

        assertTrue(outIni.exists());
        assertTrue(outIni.length() > 0);
        
        
        Diversicon div = Diversicon.connectToDb(cli.getDbConfig());
        div.getSession()
           .close();
    }
    
   


}
