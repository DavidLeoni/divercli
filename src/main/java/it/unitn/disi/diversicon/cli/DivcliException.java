package it.unitn.disi.diversicon.cli;


/**
 * A generic runtime exception. 
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class DivcliException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    protected DivcliException(){
        super();
    }
    
    public DivcliException(Throwable tr) {
        super(tr);
    }

    public DivcliException(String msg, Throwable tr) {
        super(msg, tr);
    }

    public DivcliException(String msg) {
        super(msg);
    }
}