package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.ImportJob;
import it.unitn.disi.diversicon.LogMessage;

/**
 * Custom logback appender to insert import log into the database.
 * 
 * @since 0.1.0
 *
 */
public class ImportAppender extends AppenderBase<ILoggingEvent> {
    
    private static final Logger LOG = LoggerFactory.getLogger(ImportAppender.class);

    private Diversicon diversicon;
    
    public ImportAppender (Diversicon diversicon) {
        checkNotNull(diversicon);
        
        this.diversicon = diversicon;
    }

    @Override
    public void start() {

      super.start();
    }

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