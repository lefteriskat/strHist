package gr.demokritos.iit.irss.semagrow.qfr;

import eu.semagrow.core.impl.util.FilterCollector;
import eu.semagrow.querylog.api.QueryLogRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.qfr.QueryResult;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.Stat;
import gr.demokritos.iit.irss.semagrow.base.range.CalendarRange;
import gr.demokritos.iit.irss.semagrow.base.range.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import gr.demokritos.iit.irss.semagrow.file.ResultMaterializationManager;
import gr.demokritos.iit.irss.semagrow.rdf.*;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.Iteration;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.algebra.helpers.VarNameCollector;
import org.openrdf.query.impl.EmptyBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by katerina on 23/11/2015.
 */
public class QueryRecordCAdapter implements QueryRecord<RDFCircle, Stat> {
    static final Logger logger = LoggerFactory.getLogger(QueryRecordCAdapter.class);

    private QueryLogRecord queryLogRecord;

    private RDFCircle circle;

    private StatementPattern pattern;

    private Collection<ValueExpr> filters;

    private BindingSet bindings = EmptyBindingSet.getInstance();

    private ResultMaterializationManager fileManager;

    public QueryRecordCAdapter(QueryLogRecord queryLogRecord, ResultMaterializationManager fileManager)
            throws IllegalArgumentException {
        this.queryLogRecord = queryLogRecord;
        this.fileManager = fileManager;

        //String qStr = queryLogRecord.getQuery();
        TupleExpr expr = queryLogRecord.getExpr();

        Collection<StatementPattern> patterns = StatementPatternCollector.process(expr);
        filters = FilterCollector.process(expr);

        if (patterns.size() != 1)
            throw new IllegalArgumentException("Only single-pattern queries are supported");

        pattern = patterns.iterator().next();
        bindings = queryLogRecord.getBindings();
        //if (queryLogRecord.get)

    }

    @Override
    public String getQuery() {

        URI resultFile = queryLogRecord.getResults();
        if (resultFile != null)
            return resultFile.stringValue() + " " + getRectangle().toString();
        else
            return "<nofile>" + " " + getRectangle().toString();
    }

    @Override
    public RDFCircle getRectangle() {

        if (circle == null)
            circle = computePatternRectangle(pattern, filters);

        return circle;
    }


    private List<Var> getDimensions() {
        List<Var> list = new ArrayList<Var>();
        list.add(pattern.getSubjectVar());
        list.add(pattern.getPredicateVar());
        list.add(pattern.getObjectVar());
        return list;
    }

    private Value getValue(Var v) {
        if (v.hasValue())
            return v.getValue();
        else
            return bindings.getValue(v.getName());
    }

    private RDFCircle computePatternRectangle(StatementPattern pattern, Collection<ValueExpr> filters) {

        Value sVal = getValue(pattern.getSubjectVar());
        Value pVal = getValue(pattern.getPredicateVar());
        Value oVal = getValue(pattern.getObjectVar());



        RDFStrRange sRange = new RDFStrRange();
        ExplicitSetRange<URI> pRange = new ExplicitSetRange<URI>();
        RDFValueRange oRange = new RDFValueRange();

        if (sVal != null) {
            sRange = new RDFStrRange(sVal.stringValue());

        }

        if (pVal != null) {
            ArrayList<URI> singletonPredicate = new ArrayList<URI>();
            if (pVal instanceof URI) {
                singletonPredicate.add((URI)pVal);
                pRange = new ExplicitSetRange<URI>(singletonPredicate);
            }
        }

        if (oVal != null) {
            if (oVal instanceof Literal) {
                Literal oLit = (Literal)oVal;
                RDFLiteralRange lRange = new RDFLiteralRange(oLit.getDatatype(), computeObjectRange(oLit));
                oRange = new RDFValueRange(lRange);
            } else if (oVal instanceof URI) {
                URI oURI = (URI)oVal;
                RDFURIRange uriRange = new RDFURIRange(Collections.singletonList(oURI.stringValue()));
                oRange = new RDFValueRange(uriRange);
            }
        } else  {
            // oVal is a variable but can have filters
            oRange = computeObjectRange(pattern.getObjectVar(), filters);
        }

        return new RDFCircle(sRange, pRange, oRange);
    }

    private RDFCircle computeRectangle(Value s, Value p, Value o) {

        RDFStrRange sRange = new RDFStrRange();
        ExplicitSetRange<URI> pRange = new ExplicitSetRange<URI>();
        RDFValueRange oRange = new RDFValueRange();

        if (s != null) {
            sRange = new RDFStrRange(s.stringValue());
        }

        if (p != null) {
            ArrayList<URI> predicates = new ArrayList<URI>();
            predicates.add((URI)p);
            pRange = new ExplicitSetRange<URI>(predicates);
        }

        if (o != null) {
            if (o instanceof Literal) {
                Literal l = (Literal)o;
                RDFLiteralRange lRange = new RDFLiteralRange(l.getDatatype(), computeObjectRange(l));
                oRange.setLiteralRange(lRange);
            } else if (o instanceof URI) {
                URI u = (URI)o;
                RDFURIRange uriRange = new RDFURIRange(Collections.singletonList(u.stringValue()));
                oRange.setUriRange(uriRange);
            }
        }

        return new RDFCircle(sRange, pRange, oRange);
    }

    private RangeLength<?> computeObjectRange(Literal lit) {
        if (lit.getDatatype().equals(XMLSchema.INT) || lit.getDatatype().equals(XMLSchema.INTEGER)) {
            int val = lit.intValue();
            return new IntervalRange(val,val);
        } else if (lit.getDatatype().equals(XMLSchema.DATETIME) || lit.getDatatype().equals(XMLSchema.DATE)) {
            Date val = lit.calendarValue().toGregorianCalendar().getTime();
            return new CalendarRange(val,val);
        } else {
            //return new PrefixRange(lit.stringValue());
            //FIXME
            return null;
        }
    }

    private RDFValueRange computeObjectRange(Var var, Collection<ValueExpr> filters) {
        //FIXME
        Collection<ValueExpr> relevantFilters = findRelevantFilters(var, filters);

        Map<URI, Literal> lows = new HashMap<URI,Literal>();
        Map<URI, Literal> highs = new HashMap<URI,Literal>();

        for (ValueExpr e : relevantFilters) {
            if (e instanceof Compare) {
                Compare c = (Compare)e;
                ValueExpr high = null, low = null;
                if (c.getOperator().equals(Compare.CompareOp.LE))
                {
                    if (c.getLeftArg().equals(var))
                        high = c.getRightArg();
                    else if (c.getRightArg().equals(var))
                        low = c.getLeftArg();
                } else if (c.getOperator().equals(Compare.CompareOp.GE)) {
                    if (c.getRightArg().equals(var))
                        high = c.getLeftArg();
                    else if (c.getLeftArg().equals(var))
                        low = c.getRightArg();
                }

                if (low != null && low instanceof Var) {
                    Var l = (Var)low;
                    Value v = getValue(l);
                    if (v != null && v instanceof Literal) {
                        Literal lowLit = (Literal)v;
                        if (lowLit.getDatatype() != null) {
                            lows.put(lowLit.getDatatype(), lowLit);
                        }
                    }
                }

                if (high != null && high instanceof Var) {
                    Var l = (Var)high;
                    Value v = getValue(l);
                    if (v != null && v instanceof Literal) {
                        Literal highLit = (Literal)v;
                        if (highLit.getDatatype() != null) {
                            highs.put(highLit.getDatatype(), highLit);
                        }
                    }
                }
            }
        }

        Set<URI> types = lows.keySet();
        types.retainAll(highs.keySet());
        //FIXME: we should take the union of types (not the intersection)
        //TODO: find the ultimate MIN and MAX of each type.

        Map<URI, RangeLength<?>> ranges = new HashMap<URI, RangeLength<?>>();
        for (URI t : types) {
            RangeLength<?> r = null;

            if (t.equals(XMLSchema.INT))
                r = new IntervalRange(lows.get(t).intValue(), highs.get(t).intValue());
            else if (t.equals(XMLSchema.DATETIME))
                r = new CalendarRange(lows.get(t).calendarValue().toGregorianCalendar().getTime(),
                        highs.get(t).calendarValue().toGregorianCalendar().getTime());

            if (r != null)
                ranges.put(t, r);
        }

        if (ranges.isEmpty())
            return new RDFValueRange();
        else
            return new RDFValueRange(new RDFLiteralRange(ranges));
    }

    private Collection<ValueExpr> findRelevantFilters(Var var, Collection<ValueExpr> filters) {

        Collection<ValueExpr> relevant = new LinkedList<ValueExpr>();

        for (ValueExpr e : filters) {
            Set<String> varnames = VarNameCollector.process(e);
            if (varnames.contains(var.getName()))
                relevant.add(e);
        }

        return relevant;
    }

    @Override
    public QueryResult<RDFCircle, Stat> getResultSet() {
        return new QueryResultImpl();
    }

    private class QueryResultImpl implements QueryResult<RDFCircle, Stat> {

        private Stat emptyStat = new Stat();

        @Override
        public Stat getCardinality(RDFCircle rect) {

            RDFCircle queryRect = getRectangle();

            if (!queryRect.intersects(rect))
                return emptyStat;

            CloseableIteration<BindingSet,QueryEvaluationException> iter = getResult();

            iter = filter(rect, iter);

            try {
                Stat stat =  getStat(iter);
                iter.close();
                return stat;
            } catch (QueryEvaluationException e) { }

            return emptyStat;
        }

        private CloseableIteration<BindingSet, QueryEvaluationException>
        filter(RDFCircle circle, CloseableIteration<BindingSet, QueryEvaluationException> iter)
        {
            Value sVal = getValue(pattern.getSubjectVar());
            Value pVal = getValue(pattern.getPredicateVar());
            Value oVal = getValue(pattern.getObjectVar());

            if (sVal == null)
                iter = new URIRangeFilterIteration(pattern.getSubjectVar().getName(), circle.getSubjectRange(), iter);

            if (pVal == null)
                iter = new URIRangeFilterIteration(pattern.getPredicateVar().getName(), circle.getPredicateRange(), iter);

            if (oVal == null)
                iter = new ValueRangeFilterIteration(pattern.getObjectVar().getName(), circle.getObjectRange(), iter);

            return iter;
        }

        private CloseableIteration<BindingSet, QueryEvaluationException>
        getResult()
        {
            if (queryLogRecord.getCardinality() == 0)
                return new EmptyIteration<BindingSet, QueryEvaluationException>();
            try {
                return fileManager.getResult(queryLogRecord.getResults());
            } catch (QueryEvaluationException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Stat getStat(Iteration<BindingSet, QueryEvaluationException> iter)
                throws QueryEvaluationException
        {
        	Stat stat = new Stat();
            long count = 0;
            Map<String, Set<Value>> distinctValues = new HashMap<String,Set<Value>>();
            Map<String, Map<Value,Long>> maxAndMinValues = new HashMap<String,Map<Value,Long>>();
            while (iter.hasNext()) {
                count++;
                BindingSet b = iter.next();

                for (String bindingName : b.getBindingNames()) {
                	
                	Value currValue = b.getValue(bindingName);
                    Set<Value> d;
                    Map<Value,Long> v;
                    if (!distinctValues.containsKey(bindingName)){
                        d = new HashSet<Value>();
                        v = new HashMap<Value,Long>();
                    } else {
                        d  = distinctValues.get(bindingName);
                        v  = maxAndMinValues.get(bindingName);
                    }
                    
                    //Count how many times every distinct value appears
                    if(v.containsKey(currValue)) {
                    	v.put(currValue, v.get(currValue)+1);
                    }else {
                    	v.put(currValue,(long) 1);
                    }
                    d.add(b.getValue(bindingName));
                    distinctValues.put(bindingName, d);
                    maxAndMinValues.put(bindingName, v);
                }
            }
            
            stat.setFrequency(count);
            
//            Stream<Map.Entry<Value,Long>> sorted =
//            		values.entrySet().stream()
//            	       .sorted(Map.Entry.comparingByValue());
            Map<String,Long> maxCardinality = new HashMap<String,Long>();
            Map<String,Long> minCardinality = new HashMap<String,Long>();
            
            for(String bindingName : maxAndMinValues.keySet()) {
            	Map<Value,Long> distinctCardinalityMap = maxAndMinValues.get(bindingName);
            	boolean first = true;
            	Long current;
            	for(Value distinct : distinctCardinalityMap.keySet()) {
            		current = distinctCardinalityMap.get(distinct);
            		if(first) {
            			maxCardinality.put(bindingName, current );
            			minCardinality.put(bindingName, current );
            			first = false;
            			continue;
            		}
            		
            		if(current > maxCardinality.get(bindingName)) {
            			maxCardinality.put(bindingName,current);
            		}else if(current < minCardinality.get(bindingName)) {
            			minCardinality.put(bindingName,current);
            		}
            	}
            }

            ArrayList<Long> distinctCounts = new ArrayList<Long>();
            ArrayList<Long> minCounts = new ArrayList<Long>();
            ArrayList<Long> maxCounts = new ArrayList<Long>();

            List<Var> dimensions = getDimensions();

            for (Var dim : dimensions) {

                Value v = getValue(dim);

                if (v != null) {
                    if (count == 0) {
                        distinctCounts.add((long) 0);
                        minCounts.add((long) 0);
                        maxCounts.add((long) 0);
                    }
                    else {
                        distinctCounts.add((long) 1);
                        minCounts.add((long) 1);
                        maxCounts.add((long) 1);
                    }
                                    } else {
                    Set<Value> values = distinctValues.get(dim.getName());
                    Long max = maxCardinality.get(dim.getName());
                    Long min = minCardinality.get(dim.getName());
                    if (values == null)
                        distinctCounts.add((long) 0);
                    else
                        distinctCounts.add((long) values.size());
                    minCounts.add((min!=null)?min:(long) 1);
                    maxCounts.add((max!=null)?max:(long) 1);
                }
            }

            stat.setDistinctCount(distinctCounts);
            stat.setMinCount(minCounts);
            stat.setMaxCount(maxCounts);
            return stat;
        }

        @Override
        public List<RDFCircle> getRectangles(RDFCircle rect) {
            return getRectangles();
        }

        public List<RDFCircle> getRectangles() {

            Map<Value, RDFCircle> rectangles = new HashMap<Value, RDFCircle>();

            CloseableIteration<BindingSet, QueryEvaluationException> iter = getResult();

            try {

                while (iter.hasNext()) {
                    BindingSet b = iter.next();
                    RDFCircle rect;

                    Value sVal = getValue(pattern.getSubjectVar());
                    Value pVal = getValue(pattern.getPredicateVar());
                    Value oVal = getValue(pattern.getObjectVar());

                    sVal = (sVal == null) ? b.getValue(pattern.getSubjectVar().getName()) : sVal;
                    pVal = (pVal == null) ? b.getValue(pattern.getPredicateVar().getName()) : pVal;
                    oVal = (oVal == null) ? b.getValue(pattern.getObjectVar().getName()) : oVal;


                    if (rectangles.containsKey(pVal)) {
                        rect = rectangles.get(pVal);
                        rect.getSubjectRange().expand((URI)sVal);
                        //rect.getPredicateRange().expand(pVal.stringValue());
                        rect.getObjectRange().expand(oVal);
                    } else {
                        rect = computeRectangle(sVal, pVal, oVal);
                        rectangles.put(pVal, rect);
                    }
                }

            } catch(QueryEvaluationException e) {
                return Collections.<RDFCircle>emptyList();
            }

            return new LinkedList<RDFCircle>(rectangles.values());
        }

    }

}
