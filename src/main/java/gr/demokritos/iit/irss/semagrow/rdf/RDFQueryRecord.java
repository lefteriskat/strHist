package gr.demokritos.iit.irss.semagrow.rdf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import gr.demokritos.iit.irss.semagrow.api.ExplicitSetRange;
import gr.demokritos.iit.irss.semagrow.api.PrefixRange;
import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.QueryResult;
import gr.demokritos.iit.irss.semagrow.parsing.Binding;
import gr.demokritos.iit.irss.semagrow.parsing.LogQuery;
import gr.demokritos.iit.irss.semagrow.parsing.Utilities;
import gr.demokritos.iit.irss.semagrow.rdf.RDFRectangle;

public class RDFQueryRecord implements QueryRecord<RDFRectangle> {

	// Contains the Query's signature elements.
	private LogQuery logQuery;
	// Contains the Query's result's bindings sets.
	private RDFQueryResult queryResults;


	public RDFQueryRecord(LogQuery logQuery) {
		this.logQuery = logQuery;
		setQueryResult(new RDFQueryResult(logQuery.getQueryStatements()));
	}


	/**
	 * Returns a Rectangle over the Query.
	 */
	public RDFRectangle getRectangle() {

		PrefixRange subjectRange = getSubjectRange(logQuery.getQueryStatements().get(0));

		ExplicitSetRange<String> predicateRange = getPredicateRange(logQuery.getQueryStatements().get(1));

		RDFLiteralRange objectRange = getObjectRange(logQuery.getQueryStatements().get(2));
				
		return new RDFRectangle(subjectRange, predicateRange, objectRange);
	}// getRectangle


	/**
	 * TODO: Unchecked
	 */
	private RDFLiteralRange getObjectRange(Binding binding) {
		RDFLiteralRange objectRange = null;
		
		// Check what's inside the object's value.
		if (binding.getValue().equals("")) {// If object is a variable
			objectRange = new RDFLiteralRange();
			System.err.println("Empty Object");
			//TODO: Xtypaei nullpointerexception giati ston keno constructor tou RDFLiteralRange
			// den dinetai timi sti metavliti range i ipoia xrisimopoieitai stin toString
		} else if (binding.getValue().contains("^^")) {// URI
			String value = Utilities.getValueFromURI(binding.getValue());
			String type = Utilities.getTypeFromURI(binding.getValue());
			
			if (type.equals("int")) {
				objectRange = new RDFLiteralRange(Integer.parseInt(value), Integer.parseInt(value));
			} else if (type.equals("long")) {
				objectRange = new RDFLiteralRange(Long.parseLong(value), Long.parseLong(value));
			} else if (type.equals("dateTime")) {
				
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Date date = null;
				
				try {date = format.parse(value);}
				catch (ParseException e) {e.printStackTrace();}
				
				if (date != null)
					objectRange = new RDFLiteralRange(date, date);
				else 
					System.err.println("Date Format Error.");
			}
			
		} else {// Plain Literal or URL
			objectRange = new RDFLiteralRange(binding.getValue());
		}	
		
		return objectRange;
	}// getObjectRange
	
	
	public static void main(String[] args) {
		String v = "\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"^^<http://www.w3.org/2001/XMLSchema#dateTime>";
		System.out.println(v);
//		System.out.println(getTypeFromURI(v));
//		System.out.println(getValueFromURI(v));
		
	}


	


	private ExplicitSetRange<String> getPredicateRange(Binding binding) {
		HashSet<String> set = new HashSet<String>();

		// Check if binding is a const variable or not.
		if (binding.getValue().equals("")) {// Is variable
			// Call the empty constructor, it handles this case.
			return new ExplicitSetRange();
		} else {// Is const
				// Add the whole value as predicate
				// TODO: Think if this is really ok.
			set.add(binding.getValue());
		}

		return new ExplicitSetRange<String>(set);
	}// getPredicateRange


	private PrefixRange getSubjectRange(Binding binding) {
		ArrayList<String> prefix = new ArrayList<String>();

		// Check if binding is a const variable or not.
		if (binding.getValue().equals("")) {// Is variable
			// Call the empty constructor, it handles this case.
			return new PrefixRange();
		} else {// Is const
				// Add the whole value as prefix
				// TODO: Think if this is really ok.
			prefix.add(binding.getValue());
		}

		return new PrefixRange(prefix);
	}// getSubjectRange


	/**
	 * Returns a Rectangle over the Query's Results.
	 * TODO: Not implemented yet.
	 */
	public QueryResult<RDFRectangle> getResultSet() {
		return null;
	}


	/**
	 * Returns a String representation of the Query.
	 */
	public String getQuery() {
		return logQuery.getQuery();
	}// getQuery


	@Override
	public boolean equals(Object obj) {

		if (obj instanceof RDFQueryRecord) {

			RDFQueryRecord qr = (RDFQueryRecord) obj;

			if (qr.getLogQuery().equals(this.getLogQuery()))
				return true;
		}

		return false;
	}// equals


	/*
	 * Getters & Setters.
	 */

	public LogQuery getLogQuery() {
		return logQuery;
	}


	public void setLogQuery(LogQuery logQuery) {
		this.logQuery = logQuery;
	}


	public RDFQueryResult getQueryResult() {
		return queryResults;
	}


	public void setQueryResult(RDFQueryResult queryResult) {
		this.queryResults = queryResult;
	}

}
