package eu.kidf.diversicon.cli.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import eu.kidf.diversicon.cli.DiverCli;
import eu.kidf.diversicon.cli.commands.InitCommand;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.data.DivWn31;

/**
 * @since 0.1.0
 */
public class DiverCliIT extends DiverCliTestBase {  

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
