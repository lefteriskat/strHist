package gr.demokritos.iit.irss.semagrow.sesame;

import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogException;
import gr.demokritos.iit.irss.semagrow.qfr.QueryLogHandler;
import gr.demokritos.iit.irss.semagrow.qfr.SerialQueryLogFactory;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by angel on 10/11/14.
 */
public class TestSail extends SailBase {


    private Repository actualRepo;

    private RDFSTHolesHistogram histogram;

    private ExecutorService executorService;

    private ResultMaterializationManager manager;

    private int year;

    public TestSail(Repository actual, int year) {
        this.year = year;
        actualRepo = actual;
        executorService = Executors.newFixedThreadPool(10);
    }

    public RepositoryConnection getRepositoryConnection() throws RepositoryException {
        return actualRepo.getConnection();
    }

    public RDFSTHolesHistogram getHistogram() {
        if (histogram == null)
            histogram = new RDFSTHolesHistogram();

        return histogram;
    }

    QueryLogHandler handler;

    public QueryLogHandler getQueryLogHandler() {

        RDFFormat rdfFF = RDFFormat.NTRIPLES;

        if (handler == null) {
//        RDFWriterRegistry writerRegistry = RDFWriterRegistry.getInstance();
//        RDFWriterFactory rdfWriterFactory = writerRegistry.get(rdfFF);
//        QueryLogFactory factory = new RDFQueryLogFactory(rdfWriterFactory);
            SerialQueryLogFactory factory = new SerialQueryLogFactory();
            try {
//                File qfrLog = File.createTempFile("qfr", ".log", new File("/var/tmp/"));
                File dir = new File("/var/tmp/" + year + "/");
                if (!dir.exists())
                    dir.mkdir();

                File qfrLog = new File("/var/tmp/" + year + "/" + year + "_log.ser");
                OutputStream out = new FileOutputStream(qfrLog, true);
                handler = factory.getQueryRecordLogger(out);
            } catch (IOException e) {e.printStackTrace();}
        }

        return handler;
    }


    public ResultMaterializationManager getMateralizationManager(){

        if (manager == null) {
            File baseDir = new File("/var/tmp/" + this.year + "/");
            if (!baseDir.exists())
                baseDir.mkdir();

            TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

            TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
            TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
            manager = new FileManager(baseDir, writerFactory, getExecutorService());
        }
        return manager;
    }

    @Override
    protected void shutDownInternal() throws SailException {

        executorService.shutdown();

        if (handler != null)
            try {
                handler.endQueryLog();
            } catch (QueryLogException e) {
                throw new SailException(e);
            }

    }

    @Override
    protected SailConnection getConnectionInternal() throws SailException {
        try {
            return new TestSailConnection(this);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    @Override
    public boolean isWritable() throws SailException {
        return false;
    }

    @Override
    public ValueFactory getValueFactory() { return ValueFactoryImpl.getInstance(); }

    public ExecutorService getExecutorService() { return executorService; }
}
