package gr.demokritos.iit.irss.semagrow.parsing;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.cedarsoftware.util.io.JsonWriter;
import gr.demokritos.iit.irss.semagrow.api.*;
import gr.demokritos.iit.irss.semagrow.rdf.RDFLiteralRange;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;
import gr.demokritos.iit.irss.semagrow.rdf.Stat;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesBucket;
import gr.demokritos.iit.irss.semagrow.stholes.STHolesHistogram;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

public class HistogramIO<R extends Rectangle<R>> {

	private String path;
	private STHolesBucket<R> rootBucket;
    private STHolesHistogram histogram;


   	public HistogramIO(String path, STHolesHistogram histogram) {
		setPath(path);
		setHistogram(histogram);
	}


	public void write() {
        writeJSOn();
    }


    public static STHolesBucket read(String path) {
        return readJSON(path);
    }


	/**
	 * Writes the histogram into a file in jSON format.
	 */
	private void writeJSOn() {
		FileWriter fw;

        try {
            fw = new FileWriter(getPath() + ".txt");

            // Write root bucket and its children via chained toJSON calls.
            fw.write(JsonWriter.formatJson(histogram.toJSON().toJSONString()));

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}


    public static STHolesBucket readJSON(String path) {
        JSONParser parser = new JSONParser();
        Object obj;
        JSONObject jsonObject;
        STHolesBucket rootBucket = null;

        try {
            obj = parser.parse(new FileReader(path));
            jsonObject = (JSONObject) obj;

            obj = jsonObject.get("bucket");
            JSONObject bucket = (JSONObject)obj;

            rootBucket = getBucket(bucket);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return rootBucket;
    }// readJSON


    private static STHolesBucket getBucket(JSONObject b) {
        STHolesBucket bucket = null;
        JSONObject jsonObject;
        RDFRectangle box;
        Stat statistics;
        Collection<STHolesBucket> children;

        // Get box
        box = getBox(b.get("box"));

        // Get statistics
        statistics = getStatistics(b.get("statistics"));

        // Instantiate root Bucket
        bucket = new STHolesBucket(box, statistics);

        // Get children buckets and set their parent
        bucket.getChildren().addAll(
                getChildren(b.get("children"), bucket));

        return bucket;
    }// getBucket


    private static Collection<STHolesBucket> getChildren(Object childrenObj, STHolesBucket parent) {
        Collection<STHolesBucket> children = new ArrayList<STHolesBucket>();
        JSONArray array = (JSONArray)childrenObj;
        JSONObject temp;
        STHolesBucket tempBucket;

        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = (JSONObject)iterator.next();

            // Get Bucket
            tempBucket = getBucket((JSONObject)temp.get("bucket"));
            // And set its parent
            tempBucket.setParent(parent);

            children.add(tempBucket);
        }

        return children;
    }// getChildren


    private static RDFRectangle getBox(Object boxObj) {
        JSONObject jsonObject;
        PrefixRange subjectRange;
        ExplicitSetRange<String> predicateRange;
        RDFLiteralRange objectRange;

        jsonObject = (JSONObject)boxObj;

        subjectRange = getSubject(jsonObject.get("subject"));
        predicateRange = getPredicate(jsonObject.get("predicate"));
        objectRange = getObject(jsonObject.get("object"));

        return new RDFRectangle(subjectRange, predicateRange, objectRange);
    }// getBox


    private static RDFLiteralRange getObject(Object objectObj) {
        Map<URI,RangeLength<?>> ranges = new HashMap<URI, RangeLength<?>>();
        RDFLiteralRange literalRange = null;
        JSONObject jsonObject = (JSONObject)objectObj, temp;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (temp.get("intervalRange") != null) {
                getObjectIntervalRange(temp.get("intervalRange"), ranges);

            } else if (temp.get("prefixRange") != null) {
                ranges.put(XMLSchema.STRING, getSubject(temp.get("prefixRange")));

            } else if (temp.get("calendarRange") != null) {
                getObjectCalendarRange(temp.get("calendarRange"), ranges);
            }

        }// while

        return new RDFLiteralRange(ranges);
    }// getObject


    private static void getObjectIntervalRange(Object obj, Map<URI,RangeLength<?>> ranges) {
        JSONObject jsonObject = (JSONObject)obj;

        long low = (Long) jsonObject.get("low");
        long high = (Long) jsonObject.get("high");

        ranges.put(XMLSchema.INTEGER, new IntervalRange((int) low, (int) high));
    }// getObjectIntervalRange


    private static void getObjectCalendarRange(Object obj, Map<URI,RangeLength<?>> ranges) {
        JSONObject jsonObject = (JSONObject)obj;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        Date dateBegin = null, dateEnd = null;

        try {
            dateBegin = format.parse((String) jsonObject.get("begin"));
            dateEnd = format.parse((String) jsonObject.get("end"));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        ranges.put(XMLSchema.INTEGER, new RDFLiteralRange(dateBegin, dateEnd));
    }// getObjectCalendarRange


    private static PrefixRange getSubject(Object subjectObj) {
        ArrayList<String> prefixList = new ArrayList<String>();
        JSONObject jsonObject = (JSONObject)subjectObj, temp = null;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (((String)temp.get("type")).equals("url"))
                prefixList.add((String)temp.get("value"));
        }

        return new PrefixRange(prefixList);
    }// getSubject


    private static ExplicitSetRange getPredicate(Object predicateObj) {
        Set<String> predicateSet = new HashSet<String>();
        JSONObject jsonObject = (JSONObject)predicateObj, temp = null;
        JSONArray array = (JSONArray)jsonObject.get("array");
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            temp = iterator.next();

            if (((String)temp.get("type")).equals("uri"))
                predicateSet.add((String)temp.get("value"));
        }

        return new ExplicitSetRange(predicateSet);
    }// getPredicate


    private static Stat getStatistics(Object statsObj) {
        Stat statistics = null;
        JSONObject jsonObject;
        Long frequency;
        List<Long> distinctCount = new ArrayList<Long>();

        jsonObject = (JSONObject)statsObj;

        frequency = (Long)jsonObject.get("triples");

        distinctCount.add((Long)jsonObject.get("distinctSubjects"));
        distinctCount.add((Long)jsonObject.get("distinctPredicates"));
        distinctCount.add((Long)jsonObject.get("distinctObjects"));

        return new Stat(frequency, distinctCount);
    }// getStatistics


	/*
	 * Getters & Setters.
	 */
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public STHolesBucket<R> getRootBucket() {
		return rootBucket;
	}
	public void setRootBucket(STHolesBucket<R> rootBucket) {
		this.rootBucket = rootBucket;
	}
    public STHolesHistogram getHistogram() {
        return histogram;
    }
    public void setHistogram(STHolesHistogram histogram) {
        this.histogram = histogram;
    }

}
