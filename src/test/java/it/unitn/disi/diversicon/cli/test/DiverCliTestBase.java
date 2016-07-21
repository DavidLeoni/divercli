package it.unitn.disi.diversicon.cli.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.internal.Internals;


/**
 * Base to inherit from for DiverCli tests
 * @since 0.1.0
 */
public class DiverCliTestBase {
    
    protected Path testHome;
    protected Path testWorkingDir;
    protected Path testConfDir;

    @Before
    public void beforeMethod() throws IOException {

        testHome = Files.createTempDirectory("divercli-test-home");
        testConfDir = Files.createDirectories(Paths.get(testHome.toString(), ".config", "divercli"));
        testWorkingDir = Files.createTempDirectory("divercli-test-working-dir");

        System.setProperty(DiverCli.SYSTEM_CONF_DIR,
                testConfDir.toString());

        // filter ini to have temp working dir...
        Internals.copyDirFromResource(DiverCli.class, "it/unitn/disi/diversicon/cli/conf-template", testConfDir.toFile());
        Path iniPath = Paths.get(testConfDir.toString(), DiverCli.DIVERCLI_INI);
        byte[] encoded = Files.readAllBytes(iniPath);
        String filteredIni = new String(encoded, StandardCharsets.UTF_8)
                                                                        .replace(
                                                                                DiverCli.DEFAULT_H2_FILE_DB_PATH.replace(
                                                                                        ".h2.db", ""),
                                                                                testWorkingDir.toString() + "/"
                                                                                        + DiverCli.DEFAULT_H2_FILE_DB_PATH);
        // LOG.debug("\nFiltered ini = \n" + filteredIni);
        Files.write(iniPath, filteredIni.getBytes());

        FileUtils.deleteDirectory(new File("db/"));
    }

    @After
    public void afterMethod() {
        testHome = null;
        testConfDir = null;
        System.setProperty(DiverCli.SYSTEM_CONF_DIR, "");
    }
}
