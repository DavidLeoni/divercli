package it.unitn.disi.diversicon.cli.test;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Custom logback appender for documentation purposes
 * 
 * @since 0.1.0
 *
 */
public class DocAppender extends AppenderBase<ILoggingEvent> {
       
    private List<ILoggingEvent> events;
    
    /**
     * @since 0.1.0
     */
    public DocAppender () {
        events = new ArrayList<>();
        start();
        
    }

 
    /**
     * @since 0.1.0
     */
    public void append(ILoggingEvent event) {   
        events.add(event);                
    }
    
    /**
     * @since 0.1.0
     */
    public String asString(){
        StringBuilder sb = new StringBuilder();
        for (ILoggingEvent event : events){
            sb.append(event.getFormattedMessage() + "\n");
        }
        return sb.toString();
    }
  }