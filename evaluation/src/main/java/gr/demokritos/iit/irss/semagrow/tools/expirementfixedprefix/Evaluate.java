package gr.demokritos.iit.irss.semagrow.tools.expirementfixedprefix;

import eu.semagrow.core.impl.plan.PlanVisitorBase;
import eu.semagrow.core.plan.Plan;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFCircleSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.RDFSTHolesHistogram;
import gr.demokritos.iit.irss.semagrow.tools.AnalysisMetrics;
import gr.demokritos.iit.irss.semagrow.tools.QueryEvaluatorStructure;
import gr.demokritos.iit.irss.semagrow.tools.Utils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickozoulis on 20/11/2014.
 */
public class Evaluate {

    static final Logger logger = LoggerFactory.getLogger(Evaluate.class);
    static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
    private static String PREFIXES = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
    private static Hashtable<String, Long> hashTable;
    private AnalysisMetrics metrics = new AnalysisMetrics();

    // Setup Parameters
    private static final String DISTINCTPath = "/var/tmp/distinct/";
    private static String inputPath, outputPath,dbpediaVersion;
    private static Integer numOfQueries;


    public static void main(String[] args) throws IOException, RepositoryException {
        OptionParser parser = new OptionParser("i:o:v:n:");
        OptionSet options = parser.parse(args);

        if (options.hasArgument("i") && options.hasArgument("o") 
         && options.hasArgument("v") && options.hasArgument("n")) {
            inputPath = options.valueOf("i").toString();
            outputPath = options.valueOf("o").toString();
            dbpediaVersion = options.valueOf("v").toString();
            numOfQueries = Integer.parseInt(options.valueOf("n").toString());
            //executeExperiment();
            Evaluate ev = new Evaluate();
            ev.executeQuery();
        } else {
            logger.error("Invalid arguments");
            System.exit(1);
        }
    }


    private static void executeExperiment ()throws IOException, RepositoryException {
        // evaluate(Utils.getRepository(inputPath));
    }

    private void executeQuery() throws IOException {


        Path path = Paths.get(outputPath, "results_.csv");
        BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
        bw.write("Prefix, Actual, Est, AbsErr%\n\n");

        RDFSTHolesHistogram histogram = loadHistogram(1);
       
        // Evaluate a point query on histogram and triple store.
        evaluateWithSampleTestQueries1(histogram, bw, 0.01);

        bw.flush();
        bw.close();

    }

    private void evaluate(Repository repo) throws IOException, RepositoryException {
        // Load Evaluations
        logger.info("Loading point query evaluations: ");
        loadPointQueryEvaluations();

        logger.info("Starting evaluation: ");
        RepositoryConnection conn;

        try {
            conn = repo.getConnection();

            Path path = Paths.get(outputPath, "results_.csv");
            BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
            bw.write("Year, Prefix, Act, Est, AbsErr%\n\n");

            RDFSTHolesHistogram histogram = loadHistogram(1);
            //RDFCircleSTHolesHistogram histogram = loadCircleHistogram(1);

            // Evaluate a point query on histogram and triple store.
            evaluateWithSampleTestQueries1(histogram, bw, 0.01);

            bw.flush();
            bw.close();
            conn.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        repo.shutDown();
    }

    private static RDFSTHolesHistogram loadHistogram(int iteration) {
        RDFSTHolesHistogram histogram;

        if (iteration == 0)
            histogram = Utils.loadPreviousHistogram(outputPath);
        else
            histogram = Utils.loadCurrentHistogram(outputPath);

        return histogram;
    }

//    private static RDFCircleSTHolesHistogram loadCircleHistogram(int iteration) {
//        RDFCircleSTHolesHistogram histogram;
//
//        if (iteration == 0)
//            histogram = Utils.loadPreviousCircleHistogram(outputPath);
//        else
//            histogram = Utils.loadCurrentCircleHistogram(outputPath);
//
//        return histogram;
//    }

    private static void evaluateWithSampleTestQueries(RepositoryConnection conn,
                                                      RDFSTHolesHistogram histogram,
                                                      BufferedWriter bw,
                                                      double percentage) {

        logger.info("Executing test queries of year: ");

        String testQuery;
        Set samplingRows = Utils.getSamplingRows(DISTINCTPath + "subjects_.txt", percentage);
        Iterator iter = samplingRows.iterator();

        while (iter.hasNext()) {
            try {
                Integer i = (Integer) iter.next();
                String subject = Utils.loadDistinctSubject(i, DISTINCTPath);

                testQuery = PREFIXES + " select * where {<%s> dc:subject ?o}";
                testQuery = String.format(testQuery, subject);

                evaluateTestQuery(conn, histogram, testQuery, bw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            bw.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void evaluateWithSampleTestQueries1(STHistogram histogram,
                                                BufferedWriter bw,
                                                double percentage) {

        logger.info("Executing test queries: ");

        String testQuery;
        QueryEvaluatorStructure actualEval=null, histEval=null;

        List<String> categories = Utils.loadRandomCategories("/var/tmp/log.txt",numOfQueries);
       
        ActualQueryExecutor actual = new ActualQueryExecutor("histVOID.ttl");
        actual.startConnection();

        ActualQueryExecutor hist = new ActualQueryExecutor("histVOID.ttl");
        hist.startConnection();
        String category;

        for(int i=0;i<numOfQueries;i++){
        	category = categories.get(i);
            testQuery = PREFIXES + "SELECT * FROM <http://dbpedia" +dbpediaVersion+ ".org> WHERE {?s skos:subject "+category+".}";
           // System.out.println("Run normally.... ");
            try {
				actualEval = actual.runSemagrowTest(testQuery, metrics);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            //System.out.println("Results = " + actualEval.getResultCount() + "\n -------------------------------------------------- \n");

            //System.out.println("Run hist evaluation.... ");
            try {
				histEval = hist.runSemagrowTest(testQuery, metrics);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            //System.out.println("\n -------------------------------------------------- \n");

            evaluateTestQuery1((RDFSTHolesHistogram)histogram, testQuery, actualEval.getResultCount(), bw);
            //evaluateTestCircleQuery(histogram, testQuery, actualEval.getResultCount(), bw);

            metrics.setActual_execution_time(actualEval.getTime());
            metrics.setEstimate_execution_time(histEval.getTime());
            metrics.setActual_results(actualEval.getResultCount());

            evaluateQuery(actualEval, histEval);

            System.out.println(metrics.toString());


            try {
                bw.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            metrics.initialize();

        }
        actual.closeConnection();
        try {
			actual.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        hist.closeConnection();
        try {
			hist.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }

    private void evaluateQuery(QueryEvaluatorStructure actualEval, QueryEvaluatorStructure histEval) {


        System.out.println("ACTUAL: \n" + actualEval.getPlan().toString());
        System.out.println("HIST: \n"+histEval.getPlan().toString());
        if (actualEval.getPlan().equals(histEval.getPlan())) {
            //System.out.println("Equal plans ");
            metrics.setEqual_plan(true);

        } else {
            //System.out.println("Not Equal plans");
            metrics.setEqual_plan(false);
        }

        actualEval.getPlan().visit(new PlanVisitorBase<RuntimeException>() {

            @Override
            public void meet(Plan plan) throws RuntimeException {

                metrics.setActual_cardinality(plan.getProperties().getCardinality());
                metrics.setActual_cpu_cost(plan.getProperties().getCost().getOverallCost());

                plan.visitChildren(new PlanVisitorBase<RuntimeException>() {
                    @Override
                    public void meet(Plan plan) throws RuntimeException {
                        //System.out.println("child plan with cost " + plan.getProperties().getCost());
                        plan.setParentNode(plan);
                    }

                });
            }

        });

        histEval.getPlan().visit(new PlanVisitorBase<RuntimeException>() {

            @Override
            public void meet(Plan plan) throws RuntimeException {

                metrics.setEstimate_cardinality(plan.getProperties().getCardinality());
                metrics.setEstimate_cpu_cost(plan.getProperties().getCost().getOverallCost());

                plan.visitChildren(new PlanVisitorBase<RuntimeException>() {
                    @Override
                    public void meet(Plan plan) throws RuntimeException {
                        //System.out.println("child plan with cost " + plan.getProperties().getCost());
                        plan.setParentNode(plan);
                    }

                });
            }

        });
    }

    private void evaluateTestQuery1(RDFSTHolesHistogram histogram,
                                           String testQuery, long actualResults, BufferedWriter bw) {
        String prefix = getPrefix(testQuery);

//        long actual = hashTable.get(prefix);
        long estimate = Utils.evaluateOnHistogram1(histogram, testQuery);
        long error;

        metrics.setEstimate_results(estimate);


        //System.out.println("Run histogram.... Results = " + estimate);

        if (actualResults == 0 && estimate == 0)
            error = 0;
        else
            error = (Math.abs(actualResults - estimate) * 100) / (Math.max(actualResults, estimate));

        metrics.setError(error);
        try {
            bw.write(prefix + ", " + actualResults + ", " + estimate + ", " + error + "%");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("\n ************************************** \n");


    }

    /*private void evaluateTestCircleQuery(RDFCircleSTHolesHistogram histogram,
                                    String testQuery, long actualResults, BufferedWriter bw) {
        String prefix = getPrefix(testQuery);

//        long actual = hashTable.get(prefix);
        long estimate = Utils.evaluateOnCircleHistogram(histogram, testQuery);

        long error;

        metrics.setEstimate_results(estimate);


        //System.out.println("Run histogram.... Results = " + estimate);

        if (actualResults == 0 && estimate == 0)
            error = 0;
        else
            error = (Math.abs(actualResults - estimate) * 100) / (Math.max(actualResults, estimate));

        metrics.setError(error);
        try {
            bw.write(prefix + ", " + actualResults + ", " + estimate + ", " + error + "%");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("\n ************************************** \n");


    }*/

    private static void evaluateTestQuery(RepositoryConnection conn, RDFSTHolesHistogram histogram,
                                          String testQuery, BufferedWriter bw) {
        String prefix = getPrefix(testQuery);

        long actual = hashTable.get(prefix);
        long estimate = Utils.evaluateOnHistogram(histogram, testQuery);
        long error;

        if (actual == 0 && estimate == 0)
            error = 0;
        else
            error = (Math.abs(actual - estimate) * 100) / (Math.max(actual, estimate));

        try {
            bw.write(prefix + ", " + actual + ", " + estimate + ", " + error + "%");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPrefix(String testQuery) {
        String pattern = "<(.*?)>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(testQuery);

        while (m.find()) {
            if (m.group(0).contains("http://dbpedia.org/resource/Category:")) {
                String[] splits = m.group(1).split(":");
                return splits[splits.length - 1];
            }
        }

        return "";
    }

    private static Hashtable loadPointQueryEvaluations() {
        hashTable = new Hashtable<>();

        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISTINCTPath + "evals_" + 2014 + ".csv"));

            while ((line = br.readLine()) != null) {
                String[] splits = line.split(",");

                try {
                    if (splits.length == 2)
                        hashTable.put(splits[0].trim(), Long.parseLong(splits[1].trim()));
                } catch (NumberFormatException e) {
                }
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hashTable;
    }



}
