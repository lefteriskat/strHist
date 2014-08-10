package gr.demokritos.iit.irss.semagrow;

import gr.demokritos.iit.irss.semagrow.rdf.parsing.HistogramIO;
import gr.demokritos.iit.irss.semagrow.rdf.qfr.RDFQueryRecord;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by Nick on 10-Aug-14.
 */
public class TestHistogram {

    private static String trainingPool = "src\\main\\resources\\training_pool\\";
    private static String evaluationPool = "src\\main\\resources\\evaluation_pool\\";
    private static String outputPath = "src\\main\\resources\\histograms\\training_pool\\";

    private static Logger logger = LoggerFactory.getLogger(TestHistogram.class);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        HistogramIO histIO;
        CustomCollection<RDFQueryRecord> collection = new CustomCollection<RDFQueryRecord>(trainingPool);
        Iterator<RDFQueryRecord> iter = collection.iterator();

        STHolesHistogram h = new STHolesHistogram();

        while (iter.hasNext()) {
            RDFQueryRecord rdfRq = iter.next();

            h.refine(rdfRq);
            System.out.println(rdfRq.getQuery());
            System.out.println("Actual Cardinality: " + rdfRq.getQueryResult().getBindingSets().size());
            iter.remove();

            // Write histogram to file.
            histIO = new HistogramIO(outputPath + getSubjectLastSplit(rdfRq), h);
            histIO.write();
        }

        long end = System.currentTimeMillis();
        System.out.println("Total Time: " + (double) (end - start) / 1000 + " sec.");
    }


    private static String getSubjectLastSplit(RDFQueryRecord rdfQr) {

        String prefix = rdfQr.getLogQuery().getQueryStatements().get(0).getValue();
        String[] splits = prefix.split("/");

        return splits[splits.length - 1];
    }
}
