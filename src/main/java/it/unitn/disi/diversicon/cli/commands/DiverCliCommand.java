package it.unitn.disi.diversicon.cli.commands;

public interface DiverCliCommand {
    
    void configure();
    void run();
    String getName();    
}
