package gr.demokritos.iit.irss.semagrow.rdf.io.vocab;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Created by angel on 10/11/14.
 */
public class VOID {

    static ValueFactory vf = ValueFactoryImpl.getInstance();

    public static final String NAMESPACE = "http://rdfs.org/ns/void#";
    public static final String PREFIX = "void";
    public static final URI DATASET = vf.createURI(VOID.NAMESPACE, "Dataset");
    public static final URI SUBSET = vf.createURI(VOID.NAMESPACE, "subset");
    public static final URI TRIPLES = vf.createURI(VOID.NAMESPACE, "triples");
    public static final URI DISTINCTOBJECTS = vf.createURI(VOID.NAMESPACE, "distinctObjects");
    public static final URI DISTINCTSUBJECTS = vf.createURI(VOID.NAMESPACE, "distinctSubjects");
    public static final URI PROPERTIES = vf.createURI(VOID.NAMESPACE, "properties");
    public static final URI PROPERTY = vf.createURI(VOID.NAMESPACE, "property");
    public static final URI URIREGEXPATTERN = vf.createURI(VOID.NAMESPACE, "uriRegexPattern");
    public static final URI RADIUS = vf.createURI(VOID.NAMESPACE, "radius");
    public static final URI MAXOBJECTS = vf.createURI(VOID.NAMESPACE, "maxObjects");
    public static final URI MAXSUBJECTS = vf.createURI(VOID.NAMESPACE, "maxSubjects");
    public static final URI MAXPROPERTIES = vf.createURI(VOID.NAMESPACE, "maxProperties");
    public static final URI MINOBJECTS = vf.createURI(VOID.NAMESPACE, "minObjects");
    public static final URI MINSUBJECTS = vf.createURI(VOID.NAMESPACE, "minSubjects");
    public static final URI MINPROPERTIES = vf.createURI(VOID.NAMESPACE, "minProperties");
}
