package it.unitn.disi.diversicon.cli.test;

import static it.unitn.disi.diversicon.internal.Internals.checkNotEmpty;
import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.commands.HelpCommand;
import it.unitn.disi.diversicon.cli.commands.LogCommand;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliIoException;
import it.unitn.disi.diversicon.internal.Internals;

/**
 * @since 0.1.0
 *
 */
public class CliTester {

    private static final Logger LOG = LoggerFactory.getLogger(CliTester.class);
                    
    /**
     * @since 0.1.0
     */
    public static TestEnv createTestEnv() {

        try {
            Path testHome = Internals.createTempDir("divercli-test-home");

            TestEnv ret = new TestEnv(testHome,
                    Files.createDirectories(Paths.get(testHome.toString(), "divercli")),
                    Files.createDirectories(Paths.get(testHome.toString(), ".config", "divercli"))
                    );

            System.setProperty(DiverCli.SYSTEM_PROPERTY_CONF_DIR,
                    ret.getTestConfDir()
                       .toString());

            // so it always work even in stupid Eclipse, see see
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=388683
            System.setProperty(DiverCli.SYSTEM_PROPERTY_TESTING, "true");

            // filter ini to have temp working dir...
            Internals.copyDirFromResource(DiverCli.class, "it/unitn/disi/diversicon/cli/conf-template",
                    ret.getTestConfDir()
                       .toFile());
            Path iniPath = Paths.get(ret.getTestConfDir()
                                        .toString(),
                    DiverCli.DIVERCLI_INI);
            byte[] encoded = Files.readAllBytes(iniPath);
            String filteredIni = new String(encoded, StandardCharsets.UTF_8)
                                                                            .replace(
                                                                                    DiverCli.DEFAULT_H2_FILE_DB_PATH,
                                                                                    ret.getTestWorkingDir()
                                                                                       .toString() + "/"
                                                                                            + DiverCli.DEFAULT_H2_FILE_DB_PATH);

            Files.write(iniPath, filteredIni.getBytes());
            return ret;
        } catch (IOException e) {
            throw new DiverCliIoException("Something went wrong!", e);
        }
    }
    
    /**
     * @since 0.1.0
     */
    public static void resetTestEnv() {        
        System.setProperty(DiverCli.SYSTEM_PROPERTY_CONF_DIR, "");
    }
         
  
}
