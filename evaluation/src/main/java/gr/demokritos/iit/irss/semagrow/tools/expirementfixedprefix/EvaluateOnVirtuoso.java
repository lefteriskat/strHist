package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.semagrow.querylog.api.QueryLogException;
import eu.semagrow.querylog.api.QueryLogHandler;
import eu.semagrow.querylog.api.QueryLogWriter;
import gr.demokritos.iit.irss.semagrow.base.Estimation;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.sesame.QueryLogInterceptor;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import info.aduna.iteration.CloseableIteration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class EvaluateOnVirtuoso {
	static final Logger logger = LoggerFactory.getLogger(EvaluateOnVirtuoso.class);
    private static URI endpoint = ValueFactoryImpl.getInstance().createURI("http://dbpedia.org");
    private static String PREFIXES = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
                                      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
                                      "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
                                      "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
    private static QueryLogInterceptor interceptor;
    private static ExecutorService executors;

    // Setup Parameters
    private static String dbpediaVersion;
    private static int numOfQueries;
    // Sparql query to be evaluated
    private static String query;

    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("v:n:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("v") && options.hasArgument("n")) {
            dbpediaVersion = options.valueOf("v").toString();
            numOfQueries = Integer.parseInt(options.valueOf("n").toString());

            query = PREFIXES + "SELECT ?category FROM <http://dbpedia" +dbpediaVersion+ ".org> WHERE {<%s> skos:subject ?category .}";
            
            executeExperiment();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void executeExperiment() throws IOException, RepositoryException {
        executors = Executors.newCachedThreadPool();

        
        RDFSTHolesHistogram histogram = Utils.loadCurrentHistogram("/var/tmp/strHist/");
        

        evaluate(Utils.getRepository(dbpediaVersion),histogram);
        
       

        executors.shutdown();
    }

    private static void evaluate(Repository repo,RDFSTHolesHistogram histogram) throws IOException, RepositoryException {
        List<String> subjects = Utils.loadRandomCategories("/var/tmp/log2.txt",numOfQueries);
        Long actual;
        Estimation estimation;
        logger.info("Starting evaluating triple store: ");
        RepositoryConnection conn;

        FileWriter fileWriter = new FileWriter("/var/tmp/strHist/results"+dbpediaVersion+".txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("Actual\tCurrent\tK1\tK2\tK3\tK4\tK5");
        for (int j=0; j<subjects.size(); j++) {
            logger.info("Query No: " + j);
            try {
                conn = repo.getConnection();

          
                String q = String.format(query, subjects.get(j));

                estimation = Utils.newEvaluateOnHistogram(histogram, q);
                actual = Utils.evaluateOnTripleStore(conn, q);
                
                printWriter.println(actual +"\t"+estimation.toString());
                
                conn.close();
            } catch (RepositoryException mqe) {
                mqe.printStackTrace();
            }
        }
        printWriter.close();
        repo.shutDown();
    }

}
