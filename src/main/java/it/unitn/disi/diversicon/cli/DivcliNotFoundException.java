package it.unitn.disi.diversicon.cli;

/**
 * A runtime exception to raise when something is not found.
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class DivcliNotFoundException extends DivcliException {
    
    private static final long serialVersionUID = 1L;

    private DivcliNotFoundException(){
        super();
    }
    
    /**
     * Creates the NotFoundException using the provided throwable
     */
    public DivcliNotFoundException(Throwable tr) {
        super(tr);
    }

    /**
     * Creates the NotFoundException using the provided message and throwable
     */
    public DivcliNotFoundException(String msg, Throwable tr) {
        super(msg, tr);
    }

    /**
     * Creates the NotFoundException using the provided message
     */
    public DivcliNotFoundException(String msg) {
        super(msg);
    }
}