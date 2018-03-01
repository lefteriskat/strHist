package gr.demokritos.iit.irss.semagrow.rdf.io.sevod;

import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.RDFURIRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFValueRange;
import gr.demokritos.iit.irss.semagrow.rdf.io.json.JSONSerializer;
import gr.demokritos.iit.irss.semagrow.rdf.io.vocab.SEVOD;
import gr.demokritos.iit.irss.semagrow.rdf.io.vocab.VOID;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by nickozoulis on 8/10/2014.
 */
public class VoIDeserializer {

    private Model model;
    private STHolesHistogram<RDFRectangle> histogram;

    public VoIDeserializer(String path) {
        model = readModelFromFile(path);

        // Root node is set by Eleon as (svd:datasetTop, void:subset, rootResource).
        Resource root = model.filter(SEVOD.ROOT, VOID.SUBSET, null).objectResource();

        histogram = new STHolesHistogram<RDFRectangle>();
        histogram.setRoot(initBucket(root, null));
    }


    public STHolesHistogram<RDFRectangle> getHistogram() {
        return this.histogram;
    }


    private STHolesBucket<RDFRectangle> initBucket(Resource res, STHolesBucket<RDFRectangle> parent) {
        System.out.println(res);
        STHolesBucket<RDFRectangle> bucket = new STHolesBucket<RDFRectangle>();
        bucket.setParent(parent);

        // Filling bucket fields.
        bucket.setStatistics(getBucketStats(res));
        bucket.setBox(getRectangle(res));

        for (Value v : getNodeChildren(res))
            bucket.getChildren().add(initBucket((Resource)v, bucket));

        return bucket;
    }


    private RDFRectangle getRectangle(Resource res) {
        return new RDFRectangle(
                getURIRange(res, VOID.URIREGEXPATTERN),
                getExplicitSetRange(res, VOID.PROPERTY),
                getRDFValueRange(res));
    }


    private RDFURIRange getURIRange(Resource res, URI predicate) {

        ArrayList<String> arrList = new ArrayList<String>();

        Set<Value> set = model.filter(res, predicate, null).objects();

        for (Value v : set)
            arrList.add(v.stringValue());

        return new RDFURIRange(arrList);
    }

    private PrefixRange getPrefixRange(Resource res, URI predicate) {

        ArrayList<String> arrList = new ArrayList<String>();

        Set<Value> set = model.filter(res, predicate, null).objects();

        for (Value v : set)
            arrList.add(v.stringValue());

        return new PrefixRange(arrList);
    }

    private ExplicitSetRange<URI> getExplicitSetRange(Resource res, URI predicate) {

        Set<URI> hashSet = new HashSet<URI>();

        Set<Value> set = model.filter(res, predicate, null).objects();

        for (Value v : set) {
            if (v instanceof URI)
                hashSet.add((URI)v);
        }

        return new ExplicitSetRange<URI>(hashSet);
    }

    private RDFValueRange getRDFValueRange(Resource res) {
        //RDFURIRange r = get
        RDFLiteralRange lr = getRDFLiteralRange(res);
        RDFURIRange u = getRDFURIRange(res);
        return new RDFValueRange(u, lr);
    }

    private RDFLiteralRange getRDFLiteralRange(Resource res) {
        Map <URI, RangeLength<?>> objectRanges = new HashMap<URI, RangeLength<?>>();

        IntervalRange integerInterval = getIntInterval(res);
        if (integerInterval != null)
            objectRanges.put(XMLSchema.INT, integerInterval);

        CalendarRange calendarRange = getCalendarRange(res);
        if (calendarRange != null)
            objectRanges.put(XMLSchema.DATETIME, calendarRange);

        PrefixRange prefixRange = getPrefixRange(res, SEVOD.STRINGOBJECTREGEXPATTERN);

        if (!prefixRange.isEmpty())
            objectRanges.put(XMLSchema.STRING, prefixRange);

        return new RDFLiteralRange(objectRanges);
    }

    private RDFURIRange getRDFURIRange(Resource res) {
        RDFURIRange u = new RDFURIRange();
        Model m = model.filter(res, SEVOD.OBJECTREGEXPATTERN, null);
        List<String> prefixList = new LinkedList<String>();
        for (Statement s : m) {
            String prefix = s.getObject().stringValue();
            prefixList.add(prefix);
        }
        if (!prefixList.isEmpty())
            u = new RDFURIRange(prefixList);

        return u;
    }

    private CalendarRange getCalendarRange(Resource res) {

        Model m = model.filter(res, SEVOD.DATEINTERVAL, null);
        if (!m.isEmpty()) {

            SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+hh:mm");
            Resource dateRange = m.objectResource();
            Date from = null, to = null;
            try {
                from = parserSDF.parse(model.filter(dateRange, SEVOD.FROM, null).objectString());
                to = parserSDF.parse(model.filter(dateRange, SEVOD.TO, null).objectString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return new CalendarRange(from, to);
        } else
            return null;
    }


    private IntervalRange getIntInterval(Resource res) {

        Model m = model.filter(res, SEVOD.INTINTERVAL, null);
        if (!m.isEmpty()) {

            Resource intRange = m.objectResource();

            int from = Integer.parseInt(model.filter(intRange, SEVOD.FROM, null).objectString());
            int to = Integer.parseInt(model.filter(intRange, SEVOD.TO, null).objectString());

            return new IntervalRange(from, to);
        } else
            return null;
    }


    private Stat getBucketStats(Resource res) {

        Long frequency = Long.parseLong(model.filter(res, VOID.TRIPLES, null).objectString());

        List<Long> distinctCount = new ArrayList<Long>();
        List<Long> minCount = new ArrayList<Long>();
        List<Long> maxCount = new ArrayList<Long>();
        
        distinctCount.add(Long.parseLong(model.filter(res, VOID.DISTINCTSUBJECTS, null).objectString()));
        distinctCount.add(Long.parseLong(model.filter(res, VOID.PROPERTIES, null).objectString()));
        distinctCount.add(Long.parseLong(model.filter(res, VOID.DISTINCTOBJECTS, null).objectString()));
        minCount.add(Long.parseLong(model.filter(res, VOID.MINSUBJECTS, null).objectString()));
        minCount.add(Long.parseLong(model.filter(res, VOID.MINPROPERTIES, null).objectString()));
        minCount.add(Long.parseLong(model.filter(res, VOID.MINOBJECTS, null).objectString()));
        maxCount.add(Long.parseLong(model.filter(res, VOID.MAXSUBJECTS, null).objectString()));
        maxCount.add(Long.parseLong(model.filter(res, VOID.MAXPROPERTIES, null).objectString()));
        maxCount.add(Long.parseLong(model.filter(res, VOID.MAXOBJECTS, null).objectString()));

        return new Stat(frequency, distinctCount,minCount,maxCount);
    }


    private Set<Value> getNodeChildren(Resource res) {
        return model.filter(res, VOID.SUBSET, null).objects();
    }


    private Model readModelFromFile(String path) {

        InputStream inputStream = null;
        try {
            inputStream  = new FileInputStream(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RDFFormat format = Rio.getParserFormatForFileName(path);

        Model model = null;
        try {
            model =  Rio.parse(inputStream, "", format);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RDFParseException e) {
            e.printStackTrace();
        }

        return model;
    }

/*
    public static void main(String[] args) {
        STHolesHistogram<RDFRectangle> histogram =
                new VoIDeserializer("/home/nickozoulis/git/sthist/rdf/src/main/resources/histVoID.ttl").
                        getHistogram();

        new JSONSerializer(histogram, "/home/nickozoulis/git/sthist/rdf/src/main/resources/histJSON.txt");
    }
    */

}
