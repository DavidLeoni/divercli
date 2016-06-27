package it.unitn.disi.diversicon.cli.exceptions;

public class DiverCliIllegalStateException extends DiverCliException {
   
    private static final long serialVersionUID = 1L;

    public DiverCliIllegalStateException() {
        super();
    }

    public DiverCliIllegalStateException(String msg, Throwable tr) {
        super(msg, tr);
    }

    public DiverCliIllegalStateException(String msg) {
        super(msg);
    }

    public DiverCliIllegalStateException(Throwable tr) {
        super(tr);
    }

    

}
