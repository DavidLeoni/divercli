package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.Diversicons;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Creates a file database")
class CreateDbCommand {

    private DiverCli diverCli;
        
    @Parameter(names = { "--restore",
    "-r" },  description = "Restores an h2 database from a sql dump. Dump can be expressed as a URL." 
    + " and can be in a compressed format."
    + " DB configuration MUST point to a non-existing database, otherwise"
    + " behaviour is unspecified. For Wordnet 3.0 packaged dump, you can use "
    + Diversicons.WORDNET_DB_RESOURCE_URI)
    String restoreSqlDumpPath = "";
    
    @Parameter(names = "--dbPath", description = "The path to the database to create. For H2, don't include .h2.db .")
    String dbPath;
    
    @Parameter(names = "--default", description = "Sets user configuration to use this database as default for successive operations.")
    boolean makeDefault = false;

    CreateDbCommand(DiverCli diverCli){
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }
    
    void configure(){
    }
    
    void run(){
        Diversicons.restoreH2Dump(restoreSqlDumpPath, diverCli.dbConfig);
    }
}
