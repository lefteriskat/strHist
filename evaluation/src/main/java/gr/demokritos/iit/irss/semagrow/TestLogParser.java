package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.file.FileManager;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.qfr.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by nickozoulis on 23/10/2014.
 */
public class TestLogParser {

    static final Logger logger = LoggerFactory.getLogger(TestLogParser.class);

    // Input and output file paths.
    private static String input, output;

    public static void main(String[] args) throws IOException, QueryLogException, ClassNotFoundException {

        OptionParser parser = new OptionParser("i:o:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("i") && options.hasArgument("o")) {
            input = options.valueOf("i").toString();
            output = options.valueOf("o").toString();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }

        writeRDFQueryLogSerial();

        parseRDFQueryLogSerial();
    }

    private static void writeRDFQueryLogSerial() throws IOException, QueryLogException {
        Collection<QueryLogRecord> logs = new LinkedList<QueryLogRecord>();
        QueryLogRecordCollector handler = new QueryLogRecordCollector(logs);
        // Load query logs using RDFQueryLogParser
        QueryLogParser parser = new RDFQueryLogParser(handler);

        File f = new File(input);
        logger.info("Parsing file : " + f.getName());
        parser.parseQueryLog(new FileInputStream(f));
        logger.info("Number of parsed query logs: " + logs.size());

        OutputStream out = new FileOutputStream(output);

        QueryLogHandler handlerSer = new SerialQueryLogFactory().getQueryRecordLogger(out);

        handlerSer.startQueryLog();
        for (QueryLogRecord queryRecord : logs) {
            handlerSer.handleQueryRecord(new SerialQueryLogRecord(queryRecord));
        }
        handlerSer.endQueryLog();
    }

    private static void parseRDFQueryLogSerial() throws IOException, QueryLogException {
        Collection<QueryLogRecord> logs = new LinkedList<QueryLogRecord>();

        QueryLogRecordCollector handler = new QueryLogRecordCollector(logs);
        QueryLogParser parser = new SerialQueryLogParser();
        parser.setQueryRecordHandler(handler);

        File f = new File(output);
        logger.info("Parsing file : " + f.getName());
        parser.parseQueryLog(new FileInputStream(f));
        logger.info("Number of parsed query logs: " + logs.size());

        for (QueryLogRecord queryLogRecord : logs) {
            logger.info(queryLogRecord.getQuery().toString());
            logger.info("Endpoint: " + queryLogRecord.getEndpoint());
            logger.info("Cardinality: " + queryLogRecord.getCardinality());
            logger.info("Duration: " + queryLogRecord.getDuration());
            logger.info("Binding names: " + queryLogRecord.getBindingNames().toString());
        }
    }


    private static ResultMaterializationManager getMateralizationManager(){
        File baseDir = new File("/home/nickozoulis/semagrow/");
        TupleQueryResultFormat resultFF = TupleQueryResultFormat.TSV;

        TupleQueryResultWriterRegistry registry = TupleQueryResultWriterRegistry.getInstance();
        TupleQueryResultWriterFactory writerFactory = registry.get(resultFF);
        return new FileManager(baseDir, writerFactory);
    }

}
