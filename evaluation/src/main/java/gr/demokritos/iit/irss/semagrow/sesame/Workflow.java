package gr.demokritos.iit.irss.semagrow.sesame;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by angel on 10/11/14.
 */
public class Workflow {

//    private static String q = "prefix dc: <http://purl.org/dc/elements/1.1/> " +
//            "prefix semagrow: <http://www.semagrow.eu/rdf/> " +
//            "select * { " +
//            "?pub dc:title ?title ." +
//            "?pub semagrow:year \"%s\" ." +
//            "?pub semagrow:origin \"%s\" . }";
//    // Use it like this : String.format(q, "2012", "US");
//
//    private static int startDate, endDate;
//    private static String tripleStorePath = "/mnt/ocfs2/IDF_data/journals/exp_triples/histogram_data/data_";
//    public static String logOutputPath;

    static String q = "select * {<http://agris.fao.org/aos/records/XF7590017> ?p ?o}";
    static final String tripleStorePath = "/home/nickozoulis/data_";

    /**
     * s = Starting date, e = Ending Date, l = LogOutput path
     * @param args
     * @throws RepositoryException
     * @throws IOException
     */
    static public void main(String[] args) throws RepositoryException, IOException, NumberFormatException {
        OptionParser parser = new OptionParser("s:e:l:");
        OptionSet options = parser.parse(args);

//        if (options.hasArgument("s") && options.hasArgument("e") && options.hasArgument("l")) {
//            startDate = Integer.parseInt(options.valueOf("s").toString());
//            endDate = Integer.parseInt(options.valueOf("e").toString());
//            if (startDate > endDate) System.exit(1);
//            logOutputPath = options.valueOf("l").toString();
//        }
//        else System.exit(1);

        runExperiment();
    }

    private static void runExperiment() throws RepositoryException, IOException {
        for (int i=1975; i<=1975; i++) {
            // -- Query Evaluation
            try {
                RepositoryConnection conn = getFedRepository(getRepository(i)).getConnection();
                
                TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, q);

                TupleQueryResult res = query.evaluate();

                while (res.hasNext())
                    System.out.println(res.next());


            } catch (MalformedQueryException mqe) {mqe.printStackTrace();
            } catch (QueryEvaluationException e) {e.printStackTrace();}

            // -- Histogram Training

//            // The evaluation of the query will write logs (query feedback).
//            List<RDFQueryRecord> listQueryRecords = new LogParser(logOutputPath).parse();
//
//            STHolesHistogram<RDFRectangle> histogram = new STHolesHistogram<RDFRectangle>();
//            histogram.refine(listQueryRecords);

//            Maybe write histogram to file in void or json format

            // -- Histogram Testing

//            Compare with actual cardinalities using ActualCardinalityEstimator


        }
    }

    private static Repository getFedRepository(Repository actual) throws RepositoryException {
        Sail sail = new TestSail(actual);
        Repository repo = new SailRepository(sail);
        repo.initialize();
        return repo;
    }

    private static Repository getRepository(int year) throws RepositoryException, IOException {
        Properties properties = new Properties();

        File journal = new File(tripleStorePath + year + ".jnl");

        properties.setProperty(
                BigdataSail.Options.FILE,
                journal.getAbsolutePath()
        );

        // Instantiate a sail and a Sesame repository
        BigdataSail sail = new BigdataSail(properties);
        Repository repo = new BigdataSailRepository(sail);
        repo.initialize();

        return repo;
    }

}
