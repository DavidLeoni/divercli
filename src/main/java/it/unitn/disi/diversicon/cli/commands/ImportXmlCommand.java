package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotBlank;
import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.ImportConfig;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.ImportAppender;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Imports into the db lexical resources in UBY-LMF XML format.")
public class ImportXmlCommand implements DiverCliCommand {

    /**
     * @since 0.1
     */
    public static final String CMD = "import xml";

    @Parameter(names = { "--author", "-a" }, required = true, description = "The author of the operations on the db.")
    String author = "";

    
    @Parameter(names = { "--description",
            "-d" }, required = true, description = "The description of the operation being performed on the db.")
    String description = "";

    @Parameter(names = { "--process-db", "-p" }, arity=1, description = "Processes the db graph to speed up "
            + " operations requiring the transitive closure. Defaults to true. To skip it, write  -p false")
    boolean processDb = true;    


    @Parameter( required=true, variableArity = true, description = "a space separated list of XML files in UBY-LMF format."
            + " Lexical resources must have a 'name' attribute. If there are already present resources with the"
            + " same name, content will be merged.")
    // streamlined behaviour with respect to
    // https://github.com/DavidLeoni/diversicon/issues/6
    List<String> importXmlPaths = new ArrayList();

    @Nullable
    private ImportConfig importConfig;

    private DiverCli diverCli;

    public ImportXmlCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    @Override
    public void configure() {

        checkNotBlank(author, "Tried to import files without '--author' parameter! ");

        checkNotBlank(description, "Tried to import files without '--description' parameter! ");
        importConfig = new ImportConfig();
        importConfig.setAuthor(author);
        importConfig.setDescription(description);

        importConfig.setSkipAugment(!processDb);

        for (String fileUrl : importXmlPaths) {
            importConfig.addLexicalResource(fileUrl);
        }

    }

    @Override
    public void run() {
        diverCli.connect();

        ImportAppender importAppender = new ImportAppender(diverCli.getDiversicon());

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                org.slf4j.Logger.ROOT_LOGGER_NAME);

        importAppender.setContext(logger.getLoggerContext());
        logger.addAppender(importAppender);

        diverCli.getDiversicon().importFiles(importConfig);

        logger.detachAppender(importAppender);

    }

    @Override
    public String getName() {
        return CMD;
    }

}