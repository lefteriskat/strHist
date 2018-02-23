package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import gr.demokritos.iit.irss.semagrow.sesame.QueryLogInterceptor;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import info.aduna.iteration.CloseableIteration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.*;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static String inputPath;
    private static int numOfQueries;
    // Sparql query to be evaluated
    private static String query = PREFIXES + "SELECT *  WHERE {?s skos:subject ?category. FILTER regex(str(?category), \"^%s\")}";

    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("i:b:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("i") && options.hasArgument("b")) {
            inputPath = options.valueOf("i").toString();
            numOfQueries = Integer.parseInt(options.valueOf("b").toString());

            executeExperiment();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }

    private static void executeExperiment() throws IOException, RepositoryException {
        executors = Executors.newCachedThreadPool();

        interceptor = new QueryLogInterceptor(Utils.getHandler(), Utils.getMateralizationManager(executors));

        try {
            ((QueryLogWriter) handler).startQueryLog();
        } catch (QueryLogException e) {
            e.printStackTrace();
        }
        
        queryStore(Utils.getRepository(inputPath));
        
        try {
            ((QueryLogWriter) handler).endQueryLog();
        } catch (QueryLogException e) {
            e.printStackTrace();
        }

        executors.shutdown();
    }

    private static void queryStore(Repository repo) throws IOException, RepositoryException {
        List<String> subjects = loadRandomCategories("/var/tmp/log.txt",numOfQueries);

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
                ParsedTupleQuery psq = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, q, "http://example.org/");
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

   /**
     * Loads random distinct repo categories, from which the training workload will be created.
     * @return
     */
    private static List<String> loadRandomCategories(String path,Integer queryLogSize) {
        ArrayList<String> list = new ArrayList<String>();
        Random rand = new Random(); 
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = "";
            Integer i=0,j;
            while ((line = br.readLine()) != null) {
                if(i < queryLogSize) {
                    list.add(line.trim());
                }
                else {
                    j=rand.nextInt(i+1); //rand(0,i)
                    
                    if(j< queryLogSize) {
                        list.set(j,line.trim());
                    }
                }
                
                i++;
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

}

