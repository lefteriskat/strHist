package gr.demokritos.iit.irss.semagrow.base.range;

import gr.demokritos.iit.irss.semagrow.api.range.Range;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.api.range.Rangeable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a set of T explicitly by defining each member of the set. Created by
 * angel on 7/12/14.
 */
public class ExplicitSetRange<T>
        implements RangeLength<T>, Rangeable<ExplicitSetRange<T>>
{

    private Set<T> items;
    private boolean infinite = false;

	public ExplicitSetRange(Collection<T> items) {
		this.items = new HashSet<T>(items);
	}


    // Construct an infinite explicitSet range
	public ExplicitSetRange() {
        this.items = new HashSet<T>();
		infinite = true;
	}


    //Tested
    public boolean includes(T value) {

        if (infinite)
            return true;

        return items.contains(value);
    }

    //Tested
	public boolean contains(ExplicitSetRange<T> range) {

        // for estimation
        if (isInfinite())
            return true;

        if (range.isInfinite())
            return false;
        else
            return items.containsAll(range.items);
	}

    //Tested
    public boolean intersects(ExplicitSetRange<T> range) {

        if (infinite) return true;

        ExplicitSetRange<T> esr = new ExplicitSetRange<T>(range.items);

        esr.items.retainAll(this.items);

        return esr.getLength() != 0;

    }


    //Tested
    public ExplicitSetRange<T> tightRange(ExplicitSetRange<T> tExplicitSetRange) {

        if (infinite) return new ExplicitSetRange<T>();

        Set<T> itemsN = new HashSet<T>(items);
        itemsN.addAll(tExplicitSetRange.items);

        return new ExplicitSetRange<T>(itemsN);
    }


    public void expand(T v) { items.add(v); }

    //Tested
    public ExplicitSetRange<T> intersection(ExplicitSetRange<T> range) {

        if (infinite) return range;

        ExplicitSetRange<T> esr = new ExplicitSetRange<T>(range.items);

        esr.items.retainAll(this.items);

        return esr;
	}

    
    //Tested
    public ExplicitSetRange<T> minus(ExplicitSetRange<T> tExplicitSetRange) {

        //todo: handle infinite
        Set<T> set = new HashSet<T>(this.items);
        set.removeAll(tExplicitSetRange.items);
        ExplicitSetRange<T> r = new ExplicitSetRange<T>(set);
        return r;
    }


    //Tested
    public String toString() {
        String res = "";

        if (items.size() > 1)
            res += "{";

        boolean comma = false;

        for (T p : items) {

            if (comma)
                res +=",";

            res += p;

            comma = true;
        }

        if (items.size() > 1)
            res += "}";

        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof ExplicitSetRange) {
            ExplicitSetRange<T> setRange = (ExplicitSetRange<T>)obj;

            return (this.getItems().equals(setRange.getItems())) ? true : false;
        }

        return false;
    }

    public boolean isUnit() {

        if (infinite) return false;

        return getLength() == 1;
    }

    public boolean isInfinite() {

        return infinite;
    }

    public boolean isEmpty() { return !isInfinite() && items.isEmpty(); }

    public long getLength() {

        if (infinite) return Integer.MAX_VALUE;
        return items.size();
    }


	public static void main(String[] args) {

		HashSet<String> s1 = new HashSet<String>();
		s1.add("a");
		s1.add("b");
		s1.add("c");
		ExplicitSetRange<String> esr1 = new ExplicitSetRange<String>(s1);
		HashSet<String> s2 = new HashSet<String>();
		s2.add("a");
		s2.add("b");
		s2.add("d");
		ExplicitSetRange<String> esr2 = new ExplicitSetRange<String>(s2);
        HashSet<String> s3 = new HashSet<String>();
        s3.add("a");
        s3.add("b");
        ExplicitSetRange<String> esr3 = new ExplicitSetRange<String>(s3);
        HashSet<String> s4 = new HashSet<String>();
        s4.add("c");
        s4.add("d");
        ExplicitSetRange<String> esr4 = new ExplicitSetRange<String>(s4);

        //Test includes
        String v1 = "a";
        System.out.println(esr1 + " includes " + v1 + " : " +
                esr1.includes(v1));
        System.out.println(esr1 + " contains " + esr3 + " : " +
                esr1.contains(esr3));

        //Test intersection
        if (esr1.intersects(esr2)) {
            System.out.println(esr1 + " intersection with " + esr2 +
                    " = " + esr1.intersection(esr2));
        }
        if (esr3.intersects(esr4)) {
            System.out.println("Intersects test failed");
        }
        //Test tight range
        System.out.println("Tight range of " + esr1 + " and " + esr2 +
                " = " + esr1.tightRange(esr2));
        //Test minus
        System.out.println(esr1 + " minus " + esr2 + " = " +
                esr1.minus(esr2));
        System.out.println(esr1 + " minus " + esr3 + " = " +
                esr1.minus(esr3));
        System.out.println(esr3 + " minus " + esr1 + " = " +
                esr3.minus(esr1));


	}

    public Set<T> getItems() {
        return items;
    }



    public boolean intersects(Range<?> r) {
        if (r instanceof ExplicitSetRange) {
            return intersects((ExplicitSetRange<T>)r);
        }
        return false;
    }
}
