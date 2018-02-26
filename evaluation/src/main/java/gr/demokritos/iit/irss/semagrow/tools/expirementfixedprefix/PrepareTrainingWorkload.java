package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import gr.demokritos.iit.irss.semagrow.sesame.QueryLogInterceptor;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import info.aduna.iteration.CloseableIteration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;


/**
 * Created by nickozoulis on 10/11/2014.
 */
public class PrepareTrainingWorkload {

    static final Logger logger = LoggerFactory.getLogger(PrepareTrainingWorkload.class);
    private static URI endpoint = ValueFactoryImpl.getInstance().createURI("http://dbpedia.org");
    private static String PREFIXES = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
                                      "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
                                      "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
                                      "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
    private static QueryLogInterceptor interceptor;
    private static ExecutorService executors;

    // Setup Parameters
    private static String dbpediaVersion;
    private static int numOfQueries,logNum;
    // Sparql query to be evaluated
    private static String query;

    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("v:b:l:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("v") && options.hasArgument("b") && options.hasArgument("l")) {
            dbpediaVersion = options.valueOf("v").toString();
            numOfQueries = Integer.parseInt(options.valueOf("b").toString());
            logNum = Integer.parseInt(options.valueOf("l").toString());
            query = PREFIXES + "SELECT * FROM <http://dbpedia" +dbpediaVersion+ ".org> WHERE {?s skos:subject ?category. FILTER regex(str(?s), \"^%s\")}";
            
            executeExperiment();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void executeExperiment() throws IOException, RepositoryException {
        executors = Executors.newCachedThreadPool();

        QueryLogHandler handler = Utils.getHandler(numOfQueries,dbpediaVersion,logNum);
        interceptor = new QueryLogInterceptor(handler, Utils.getMateralizationManager(executors));


        try {
            ((QueryLogWriter) handler).startQueryLog();
        } catch (QueryLogException e) {
            e.printStackTrace();
        }
        
        queryStore(Utils.getRepository(dbpediaVersion));
        
        try {
            ((QueryLogWriter) handler).endQueryLog();
        } catch (QueryLogException e) {
            e.printStackTrace();
        }

        executors.shutdown();
    }

    private static void queryStore(Repository repo) throws IOException, RepositoryException {
        List<String> subjects = Utils.loadRandomCategories("/var/tmp/log2.txt",numOfQueries);

        logger.info("Starting querying triple store: ");
        RepositoryConnection conn;

        int trimPos = 2;
        String trimmedSubject;

        for (int j=0; j<subjects.size(); j++) {
            logger.info("Query No: " + j);
            try {
                conn = repo.getConnection();

                // This line controls the rate of how fast the querying prefixes should expand.
                // For example if batch is 100 queries, then with {j mod 25} we would end up with 4 different
                // prefix depths.
                if (j % 25 == 0) trimPos++;

                trimmedSubject = Utils.trimSubject(subjects.get(j), trimPos);
                String q = String.format(query, trimmedSubject);
                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);
                logger.info("Query: " + q);

                // Get TupleExpr
                ParsedTupleQuery psq = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, q, "http://dbpedia.org/");
                TupleExpr tupleExpr = psq.getTupleExpr();

                // Intercepts the query results.
                CloseableIteration<BindingSet, QueryEvaluationException> result =
                        interceptor.afterExecution(endpoint, tupleExpr, tupleQuery.getBindings(), tupleQuery.evaluate());
                Utils.consumeIteration(result);

                conn.close();
            } catch (MalformedQueryException | RepositoryException | QueryEvaluationException mqe) {
                mqe.printStackTrace();
            }
        }

        repo.shutDown();
    }

   

}

