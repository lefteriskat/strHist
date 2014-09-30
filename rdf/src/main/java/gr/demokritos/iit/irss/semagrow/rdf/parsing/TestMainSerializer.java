package gr.demokritos.iit.irss.semagrow.rdf.parsing;

import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;

/**
 * Created by nickozoulis on 30/9/2014.
 */
public class TestMainSerializer {

    public static void main(String[] args) {

        // Read histogram from file.
        STHolesBucket<RDFRectangle> rootBucket = HistogramIO.readJSON("/home/nickozoulis/git/sthist/rdf/src/main/resources/histogram.json.txt");
        System.out.println(rootBucket);

        STHolesHistogram<RDFRectangle> histogram = new STHolesHistogram();
        histogram.setRoot(rootBucket);

        // Serialize histogram to VOID.
        Serializer serializer = new Serializer("application/x-turtle", "/home/nickozoulis/git/sthist/rdf/src/main/resources/");
        serializer.serialize(histogram);

    }
}
