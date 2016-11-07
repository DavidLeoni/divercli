package eu.kidf.diversicon.cli.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.ini4j.Wini;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.kidf.diversicon.cli.DiverCli;
import eu.kidf.diversicon.cli.commands.InitCommand;
import eu.kidf.diversicon.cli.commands.LogCommand;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.internal.Internals;
import eu.kidf.diversicon.data.DivWn31;

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

        File outf = new File(System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR), WORKING + ".h2.db");

        assertTrue(outf.exists());
        assertTrue(outf.length() > 0);

        File outIni = new File(System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR), DiverCli.INI_FILENAME);

        assertTrue(outIni.exists());
        assertTrue(outIni.length() > 0);
        
        
        Diversicon div = Diversicon.connectToDb(cli.divConfig());
        div.getSession()
           .close();
    }
    
   


}
