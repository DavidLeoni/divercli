package eu.kidf.diversicon.cli;

import static eu.kidf.diversicon.core.internal.Internals.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.ImportJob;
import eu.kidf.diversicon.core.LogMessage;

/**
 * Custom logback appender to insert import log into the database.
 * 
 * @since 0.1.0
 *
 */
public class ImportAppender extends AppenderBase<ILoggingEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger(ImportAppender.class);

    private Diversicon diversicon;
    
    /**
     * @since 0.1.0
     */
    public ImportAppender (Diversicon diversicon) {
        checkNotNull(diversicon);
        
        this.diversicon = diversicon;
        start();
    }

 
    /**
     * @since 0.1.0
     */
    public void append(ILoggingEvent event) {
        ImportJob importJob = diversicon.getDbInfo().getCurrentImportJob();
        
        org.slf4j.event.Level slf4jLevel = null;
        try {
            slf4jLevel = org.slf4j.event.Level.valueOf(event.getLevel().toString());
        } catch (Exception ex){
            LOG.error("Couldn't convert from logback to slf4j format level, skipping append log into db!", ex);
        }
            
        if (slf4jLevel != null && event.getLevel().isGreaterOrEqual(Level.WARN)){
            importJob.addLogMessage(new LogMessage(importJob, slf4jLevel, event.getFormattedMessage()));
        }            
                 
    }
  }