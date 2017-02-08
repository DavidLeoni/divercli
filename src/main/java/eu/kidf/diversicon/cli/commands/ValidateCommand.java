package eu.kidf.diversicon.cli.commands;

import static eu.kidf.diversicon.core.internal.Internals.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import eu.kidf.diversicon.cli.DiverCli;
import eu.kidf.diversicon.core.DivXmlValidator;
import eu.kidf.diversicon.core.Diversicons;
import eu.kidf.diversicon.core.ExtractedStream;
import eu.kidf.diversicon.core.XmlValidationConfig; 

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Validates the provided xml file (doesn't require a project to run)")
public class ValidateCommand implements DiverCliCommand {
          
    /**
     * @since 0.1.0
     */
    public static final String CMD = "validate-xml";
    
    private static final Logger LOG = LoggerFactory.getLogger(ValidateCommand.class);    

    @Parameter(names = {"--strict", "-s"}, description = "If enabled, occurrance of warnings will make validation fail.")
    private Boolean strict = false;

    @Parameter(names = {"--fail-fast","-a"}, description = "If enabled the validator will throw"
            + " an error as soon log-limit errors are reached (if log-limit is -1 the handler will throw on first error).")
    private Boolean failFast = false;

    
    @Parameter(names = {"--log-limit", "-l"}, description = "The amount of logs which will be outputted. If -1 all"
     + " log messages will be emitted.")
    private int logLimit = 10;
    
    @Parameter(names = {"--schema", "-c"}, description = "The filepath to the Xml Schema used to validate the document."           
         + " Will override the schema pointed to in the document.")
    private String schema;
       
    @Parameter(description = "The filepath of the xml to validate", arity = 1, required = true)
    private List<String> xmlPaths = new ArrayList<>();
    
    
    private DiverCli cli;
    
    private XmlValidationConfig xmlValidationConfig;

    /**
     * @since 0.1.0
     */
    public ValidateCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.cli = diverCli;        
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void configure() {
        
        XmlValidationConfig.Builder builder = XmlValidationConfig.builder()
                .setFailFast(failFast)
                .setLogLimit(logLimit)
                .setStrict(strict);
        // todo what about setLog ?
        
        if (schema != null){
            builder.setXsdUrl(schema);           
        }
        
        xmlValidationConfig = builder.build();
        
        
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void run() {
        ExtractedStream es = Diversicons.readData(xmlPaths.get(0));
        DivXmlValidator validator = Diversicons.validateXml(es.toTempFile(), xmlValidationConfig);
        
        LOG.info("");
        LOG.info("");
        if (validator.getErrorHandler().getWarningCount() > 0){
            LOG.info("XML is valid (but there were warnings)!");
        } else {
            LOG.info("XML is valid!");    
        }
        
        LOG.info("");
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public String getName() {
        return CMD;
    }

}
