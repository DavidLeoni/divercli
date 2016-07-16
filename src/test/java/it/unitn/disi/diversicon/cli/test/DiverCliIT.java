package it.unitn.disi.diversicon.cli.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.commands.DbRestoreCommand;
import it.unitn.disi.diversicon.data.wn30.DivWn30;

/**
 * @since 0.1.0
 */
public class DiverCliIT extends DiverCliTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(DiverCliIT.class);


    /**
     * @since 0.1.0
     */
    @Test
    public void testRestoreDbFromWordnetDb() throws IOException {

        Path dir = Files.createTempDirectory("divercli-test");
        String target = dir.toString() + "/test";

        DiverCli cli = DiverCli.of(DbRestoreCommand.CMD,
                "--db", DivWn30.WORDNET_DIV_H2_DB_RESOURCE_URI,
                "--target", target,
                "--set-default");

        cli.run();

        File outf = new File(target + ".h2.db");

        assertTrue(outf.exists());
        assertTrue(outf.length() > 0);

        Diversicon div = Diversicon.connectToDb(cli.getDbConfig());
        div.getSession()
           .close();
    }


}
