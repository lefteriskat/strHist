package gr.demokritos.iit.irss.semagrow.rdf;


import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.base.range.PrefixRange;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by angel on 7/15/14.
 */
public class RDFLiteralRange implements RangeLength<Literal>, Rangeable<RDFLiteralRange> {

    static final Logger logger = LoggerFactory.getLogger(RDFLiteralRange.class);

    private Map<URI,RangeLength<?>> ranges = new HashMap<URI, RangeLength<?>>();
   // private URI valueType;
   // private RangeLength<?> range;
    private boolean infinite = false;

    public RDFLiteralRange(Map<URI,RangeLength<?>> ranges) {
        this.ranges = ranges;
    }


    public RDFLiteralRange() {
        infinite = true;
    }

    public RDFLiteralRange(URI valueType, RangeLength<?> range) {

        this.ranges.put(valueType, range);
		//this.valueType = valueType;
		//this.range = range;
	}


    public RDFLiteralRange(int low, int high)
    {
        this(XMLSchema.INT, new IntervalRange(low, high));
    }

    public RDFLiteralRange(long low, long high) {
        this(XMLSchema.INT, new IntervalRange((int) low, (int) high));
    }

    public RDFLiteralRange(String range) {

        // TODO: maybe change it to take the list as a parameter
        ArrayList<String> stringList = new ArrayList<String>();
        stringList.add(range);
        this.ranges.put(XMLSchema.STRING,  new PrefixRange(stringList));
        //this.valueType = XMLSchema.STRING;
        //this.range = new PrefixRange(stringList);
    }

    public RDFLiteralRange(Date begin, Date end) {

        this(XMLSchema.DATETIME, new CalendarRange(begin, end));
    }

    //Copy constructor
    public RDFLiteralRange(RDFLiteralRange range) {

        this.ranges = new HashMap<URI, RangeLength<?>>(range.getRanges());
    }

    public void addSubRange(URI type, RangeLength<?> subRange) {
        ranges.put(type, subRange);
    }

    public boolean isUnit() {

        if (infinite) return false;

        boolean res = true;

        if (ranges.size() == 1) {

            for (URI type : ranges.keySet()) {

                res = res && ranges.get(type).isUnit();
            }
        }

        return res;
    }

    public boolean isEmpty() {
        boolean nonempty = !ranges.isEmpty();
        for (RangeLength<?> r : ranges.values())
            nonempty = !r.isEmpty();

        return !nonempty;
    }

    public String toString() {

        //uriprefixes for PrefixRange
        //uris for ExplicitSetRange
        //intervalRange for IntervalRange
        //calendarRange for CalendarRange
        String res = "";
    	if (infinite)
    		return "Infinite";
    	else {

            for (URI type : ranges.keySet()) {

                res = ranges.get(type).toString() + " " + res;
            }
        }
    		return res;
    }

	public RDFLiteralRange intersection(RDFLiteralRange literalRange) {

        if (infinite) return literalRange;

        if (literalRange.isInfinite())
            return this;

        RDFLiteralRange res = null;

        if (literalRange.ranges.size() != 1) {

            if (literalRange.ranges.size() == 0)
                return new RDFLiteralRange(Collections.<URI, RangeLength<?>>emptyMap());

            logger.debug("Argument should be a range of single type");
            return null;
        }

        URI literalValueType = ValueFactoryImpl.getInstance().createURI("http://uri");
        RangeLength<?> literalrange = null;

        // Only 1 loop!
        for (Map.Entry<URI, RangeLength<?>> entry : literalRange.ranges.entrySet()) {

            literalValueType = entry.getKey();
            literalrange = entry.getValue();
        }

        for (Map.Entry<URI, RangeLength<?>> entry : ranges.entrySet()) {

            RangeLength<?> range = entry.getValue();
            if (entry.getKey().equals(literalValueType)) {

                if (literalValueType.equals(XMLSchema.INTEGER) || literalValueType.equals(XMLSchema.INT)) {
                    res = new RDFLiteralRange(literalValueType,
                            ((IntervalRange) range).intersection(
                                    (IntervalRange) literalrange));
                } else if (literalValueType.equals(XMLSchema.LONG)) {

                    res = new RDFLiteralRange(literalValueType,
                            ((IntervalRange) range).intersection(
                                    (IntervalRange) literalrange));
                } else if (literalValueType.equals(XMLSchema.STRING)) {

                    res = new RDFLiteralRange(literalValueType,
                            ((PrefixRange) range).intersection(
                                    (PrefixRange) literalrange));
                } else if (literalValueType.equals(XMLSchema.DATETIME)) {

                    res = new RDFLiteralRange(literalValueType,
                            ((CalendarRange) range).intersection(
                                    (CalendarRange) literalrange));
                }
                break;
            }
        }


        if (res == null)
            return res;

        return res;
    }


    //Tested (only interval range)
	public RDFLiteralRange minus(RDFLiteralRange literalRange) {


        if (infinite)
            return literalRange;

        RDFLiteralRange res = null;

        if (literalRange.ranges.size() != 1
                || ranges.size() != 1) {
            logger.debug("Argument should be a " +
                    "range of single type");
            return res;
        }

        URI literalValueType = ValueFactoryImpl.getInstance().createURI("http://uri");
        RangeLength<?> literalrange = null;

        // Only 1 loop!
        for (Map.Entry<URI, RangeLength<?>> entry : literalRange.ranges.entrySet()) {

            literalValueType = entry.getKey();
            literalrange = entry.getValue();
        }

        for (Map.Entry<URI, RangeLength<?>> entry : ranges.entrySet()) {

            RangeLength<?> range = entry.getValue();
            if (entry.getKey().equals(literalValueType)) {

                if (literalValueType.equals(XMLSchema.INTEGER) || literalValueType.equals(XMLSchema.INT)) {
                    res = new RDFLiteralRange(literalValueType,
                            ((IntervalRange) range).minus(
                                    (IntervalRange) literalrange));
                } else if (literalValueType.equals(XMLSchema.LONG)) {

                    res = new RDFLiteralRange(literalValueType,
                            ((IntervalRange) range).minus(
                                    (IntervalRange) literalrange));
                } else if (literalValueType.equals(XMLSchema.STRING)) {

                    res = new RDFLiteralRange(literalValueType,
                            ((PrefixRange) range).minus(
                                    (PrefixRange) literalrange));
                } else if (literalValueType.equals(XMLSchema.DATETIME)) {

                    res = new RDFLiteralRange(literalValueType,
                            ((CalendarRange) range).minus(
                                    (CalendarRange) literalrange));
                }
                break;
            }
        }


        if (res == null)
            return res;

        return res;
	}



	public boolean includes(Literal literal) {

        if (isInfinite())
            return true;

        for (Map.Entry<URI, RangeLength<?>> entry : ranges.entrySet()) {

            URI valueType = entry.getKey();
            RangeLength<?> range = entry.getValue();


            if (literal.getDatatype().equals(valueType)) {
                if (valueType.equals(XMLSchema.INTEGER) || valueType.equals(XMLSchema.INT)) {
                    return ((IntervalRange) range).includes(literal.intValue());

                } else if (valueType.equals(XMLSchema.LONG)) {
                    return ((IntervalRange) range).includes(literal.intValue());

                } else if (valueType.equals(XMLSchema.STRING)) {
                    return ((PrefixRange) range).includes(literal.stringValue());

                } else if (valueType.equals(XMLSchema.DATETIME)) {
                    return ((CalendarRange) range).
                            includes(literal.calendarValue().
                                    toGregorianCalendar().getTime());
                }
            }
        }
    		
    	return false;
    }

    @SuppressWarnings("unchecked")
	public boolean contains(RDFLiteralRange literalRange) {

        if (isInfinite())
            return true;

        if (literalRange.isInfinite())
            return false;

        if (literalRange.ranges.size() != 1) {
            if (this.getRanges().size() == 0 && literalRange.ranges.size() == 0)
                return true;

            logger.debug("Argument should be a range of single type");
            return false;
        }

        URI literalValueType = ValueFactoryImpl.getInstance().createURI("http://uri");
        RangeLength<?> literalrange = null;

        // Only 1 loop!
        for (Map.Entry<URI, RangeLength<?>> entry : literalRange.ranges.entrySet()) {

            literalValueType = entry.getKey();
            literalrange = entry.getValue();
        }

        for (Map.Entry<URI, RangeLength<?>> entry : ranges.entrySet()) {

            RangeLength<?> range = entry.getValue();
            if (entry.getKey().equals(literalValueType)) {

                if (literalValueType.equals(XMLSchema.INTEGER) || literalValueType.equals(XMLSchema.INT)) {
                    return  ((IntervalRange) range).contains(
                            (IntervalRange) literalrange);
                } else if (literalValueType.equals(XMLSchema.LONG)) {

                    return  ((IntervalRange) range).contains(
                            (IntervalRange) literalrange);
                } else if (literalValueType.equals(XMLSchema.STRING)) {

                    return ((PrefixRange) range).contains(
                            (PrefixRange) literalrange);
                } else if (literalValueType.equals(XMLSchema.DATETIME)) {

                    return ((CalendarRange) range).contains(
                            (CalendarRange) literalrange);
                }
            }
        }

        return false;
    }



    public boolean intersects(Range<?> r) {
        if (r instanceof RDFLiteralRange) {
            return intersects((RDFLiteralRange)r);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
	public boolean intersects(RDFLiteralRange literalRange) {

        if (infinite) return true;

        if (literalRange.ranges.size() != 1) {

            // FIXME: Is this conceptually right?
            if (this.getRanges().size() == 0 && literalRange.ranges.size() == 0)
                return true;

            logger.debug("Argument should be a range of single type");

            return false;
        }

        URI literalValueType = ValueFactoryImpl.getInstance().createURI("http://uri");
        RangeLength<?> literalrange = null;

        // Only 1 loop!
        for (Map.Entry<URI, RangeLength<?>> entry : literalRange.ranges.entrySet()) {

            literalValueType = entry.getKey();
            literalrange = entry.getValue();
        }

        for (Map.Entry<URI, RangeLength<?>> entry : ranges.entrySet()) {

            RangeLength<?> range = entry.getValue();
            if (entry.getKey().equals(literalValueType)) {

                if (literalValueType.equals(XMLSchema.INTEGER) || literalValueType.equals(XMLSchema.INT)) {
                    return  ((IntervalRange) range).intersects(
                            (IntervalRange) literalrange);
                } else if (literalValueType.equals(XMLSchema.LONG)) {

                    return  ((IntervalRange) range).intersects(
                            (IntervalRange) literalrange);
                } else if (literalValueType.equals(XMLSchema.STRING)) {

                    return ((PrefixRange) range).intersects(
                            (PrefixRange) literalrange);
                } else if (literalValueType.equals(XMLSchema.DATETIME)) {

                    return ((CalendarRange) range).intersects(
                            (CalendarRange) literalrange);
                }
            }
        }

        return false;
    }

    public RDFLiteralRange tightRange(RDFLiteralRange literalRange) {

        if (isInfinite() || literalRange.isInfinite())
            return new RDFLiteralRange();



        if (literalRange.ranges.size() != 1) {

            if (literalRange.ranges.size() == 0)
                return new RDFLiteralRange(this.getRanges());

            logger.debug("Argument should be a range of single type");
            return null;
        }

        URI literalValueType = ValueFactoryImpl.getInstance().createURI("http://uri");
        RangeLength<?> literalrange = null;



        // Only 1 loop!
        for (Map.Entry<URI, RangeLength<?>> entry : literalRange.ranges.entrySet()) {

            literalValueType = entry.getKey();
            literalrange = entry.getValue();
        }

        //add another range (only to be used for root bucket)
        if (!ranges.containsKey(literalValueType)) {

            RDFLiteralRange res = new RDFLiteralRange(this);
            res.addSubRange(literalValueType, literalrange);
            return res;
        }

        for (Map.Entry<URI, RangeLength<?>> entry : ranges.entrySet()) {

            RangeLength<?> range = entry.getValue();
            if (entry.getKey().equals(literalValueType)) {

                if (literalValueType.equals(XMLSchema.INTEGER) || literalValueType.equals(XMLSchema.INT)) {

                    IntervalRange ires = ((IntervalRange) range).tightRange(
                            (IntervalRange) literalrange);
                    RDFLiteralRange res = new RDFLiteralRange(this);
                    res.setValue(literalValueType, ires);
                    return res;
                } else if (literalValueType.equals(XMLSchema.LONG)) {

                    IntervalRange ires = ((IntervalRange) range).tightRange(
                            (IntervalRange) literalrange);
                    RDFLiteralRange res = new RDFLiteralRange(this);
                    res.setValue(literalValueType, ires);
                    return res;
                } else if (literalValueType.equals(XMLSchema.STRING)) {

                    PrefixRange ires = ((PrefixRange) range).tightRange(
                            (PrefixRange) literalrange);
                    RDFLiteralRange res = new RDFLiteralRange(this);
                    res.setValue(literalValueType, ires);
                    return res;
                } else if (literalValueType.equals(XMLSchema.DATETIME)) {

                    CalendarRange ires = ((CalendarRange) range).tightRange(
                            (CalendarRange) literalrange);
                    RDFLiteralRange res = new RDFLiteralRange(this);
                    res.setValue(literalValueType, ires);
                    return res;
                }
            }
        }

        /*
        if (valueType.equals(literalRange.getValueType()))
        {

            if (valueType.equals(XMLSchema.INTEGER)) {

                IntervalRange res = ((IntervalRange<Integer>) range).tightRange(
                        (IntervalRange<Integer>) literalRange.getRange());
                return new RDFLiteralRange(XMLSchema.INTEGER, res);

            } else if (valueType.equals(XMLSchema.LONG)) {

                IntervalRange res = ((IntervalRange<Integer>) range).tightRange(
                        (IntervalRange<Integer>) literalRange.getRange());
                return new RDFLiteralRange(XMLSchema.INTEGER, res);
            } else if (valueType.equals(XMLSchema.STRING)) {

                PrefixRange res = ((PrefixRange) range).tightRange(
                        (PrefixRange) literalRange.getRange());
                return new RDFLiteralRange(XMLSchema.STRING, res);
            } else if (valueType.equals(XMLSchema.DATETIME)) {
                CalendarRange res = ((CalendarRange) range).tightRange(
                        (CalendarRange) literalRange.getRange());
                return new RDFLiteralRange(XMLSchema.DATETIME, res);
            }
        }
        */

        return null;
    }


    /**
     * expands an RDFLiteralRange containing ONLY
     * one subrange, so that is includes {v}
     * @param l
     */

    public void expand(Literal l) {

        URI type = l.getDatatype();

        if (ranges.containsKey(type)) {
            if (type.equals(XMLSchema.INTEGER) || type.equals(XMLSchema.INT)) {
                Range<Integer> r =  (Range<Integer>) ranges.get(l.getDatatype());
                r.expand(l.intValue());
            } else if (type.equals(XMLSchema.DATETIME)) {
                Range<Date> r =  (Range<Date>) ranges.get(l.getDatatype());
                r.expand(l.calendarValue().toGregorianCalendar().getTime());
            } else {
                throw new NotImplementedException();
            }
        } else {
            // FIXME: add a new range for that type.
            if (type.equals(XMLSchema.INTEGER) || type.equals(XMLSchema.INT)) {
                IntervalRange r = new IntervalRange(l.intValue(), l.intValue());
                ranges.put(type, r);
            } else if (type.equals(XMLSchema.DATETIME)) {
                CalendarRange r = new CalendarRange(l.calendarValue().toGregorianCalendar().getTime(),
                                                    l.calendarValue().toGregorianCalendar().getTime());
                ranges.put(type, r);
            } else {
                throw new NotImplementedException();
            }
        }

    }

    public Map<URI, RangeLength<?>> getRanges() {

        return ranges;
    }

    public void setValue(URI type, RangeLength<?> rangeN) {

        ranges.put(type, rangeN);
    }

    public long getLength() {

        if (infinite) return Integer.MAX_VALUE;

        if (ranges.size() != 1) {
            logger.debug("This method cannot be called" +
                    "for RDFLiteralRange ranges with " +
                    "more than one subrange.");
            return 0;
        }

        for (Map.Entry<URI, RangeLength<?>> entry : ranges.entrySet()) {

            return (entry.getValue()).getLength();
        }

        return 0;
    }

    public boolean isInfinite() {

        return infinite;
    }

    public void setRanges(Map<URI, RangeLength<?>> ranges) {
        this.ranges = ranges;
    }

    public boolean hasSameType(RDFLiteralRange literalRange) {

        if (this.isInfinite() && literalRange.isInfinite())
            return true;

        if (literalRange.ranges.size() != 1 || ranges.size() != 1) {
            logger.debug("This method cannot be called" +
                    "for RDFLiteralRange ranges with " +
                    "more than one subrange " +
                    "and argument should be a " +
                    "range of single type as well.");


            return false;
        }

        for (Map.Entry<URI, RangeLength<?>> entry : ranges.entrySet()) {

            for (Map.Entry<URI, RangeLength<?>> otherEntry : literalRange.ranges.entrySet()) {

                return entry.getKey().equals(otherEntry.getKey());
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof RDFLiteralRange) {
            RDFLiteralRange rdfLiteralRange = (RDFLiteralRange)obj;
            return this.infinite == rdfLiteralRange.infinite &&
                   this.ranges.equals(rdfLiteralRange.getRanges());
        }

        return false;
    }

    public static void main(String args[] ) {
        
        RDFLiteralRange rootRange = new RDFLiteralRange(0,10);
        System.out.println("Before: " + rootRange);
        RDFLiteralRange queryRange = new RDFLiteralRange("http://a");
        System.out.println("Query: " + queryRange);
        RDFLiteralRange rootRangeN = rootRange.tightRange(queryRange);
        System.out.println("After: " + rootRangeN);
        RDFLiteralRange query2Range = new RDFLiteralRange(14,17);
        RDFLiteralRange rootRangeN2 = rootRangeN.tightRange(query2Range);
        System.out.println("Query2: " + query2Range);
        System.out.println("After: " + rootRangeN2);

    }
}
