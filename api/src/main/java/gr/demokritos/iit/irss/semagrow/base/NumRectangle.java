package gr.demokritos.iit.irss.semagrow.base;

import gr.demokritos.iit.irss.semagrow.api.RectangleWithVolume;
import gr.demokritos.iit.irss.semagrow.api.range.RangeLength;
import gr.demokritos.iit.irss.semagrow.base.range.IntervalRange;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by efi on 6/8/2014.
 */
public class NumRectangle implements RectangleWithVolume<NumRectangle>, Serializable {

    private List<IntervalRange> dims;


    public NumRectangle(List<IntervalRange> dims) {

        this.dims = new ArrayList<IntervalRange>(dims);
    }


    public int getDimensionality() {
        return dims.size();
    }


    public NumRectangle intersection(NumRectangle rec) {

        List<IntervalRange> dimsN = new ArrayList<IntervalRange>(dims.size());

        for (int i = 0; i < dims.size(); i++) {

            dimsN.add(i, dims.get(i).intersection(rec.dims.get(i)));
        }

        return new NumRectangle(dimsN);
    }


    public boolean contains(NumRectangle rec) {

        boolean res = true;

        for (int i = 0; i < dims.size(); i++) {

            res = res && dims.get(i).contains(rec.dims.get(i));
        }

        return res;
    }


    @Override
    public boolean equals(Object obj) {

        if (obj instanceof NumRectangle) {

            NumRectangle rec = (NumRectangle)obj;
            return this.contains(rec) && rec.contains(this);

        }

        return false;
    }


    //Tested
    public RangeLength<?> getRange(int i) {

        if (i >= dims.size()) {

            throw new IllegalArgumentException("Dimension " + i
                    + " is not valid");
        }

        return dims.get(i);

    }

    public void setRange(int i, IntervalRange r) {

        if (i >= dims.size()) {

            throw new IllegalArgumentException("Dimension " + i
                    + " is not valid");
        }

        dims.set(i, r);
    }


    public boolean intersects(NumRectangle rec) {

        boolean res = true;

        for (int i = 0; i < dims.size(); i++) {

            res = res && dims.get(i).intersects(rec.dims.get(i));
        }

        return res;
    }


    //Tested
    // Shrink rectangle so that it does not intersect with rec
    public void shrink(NumRectangle rec) {
    //todo
        throw new NotImplementedException();
    }

    /**
     * Shrinks rectangle along dimension {dimension}
     * by excluding {rec}
     * @param rec
     * @param dimension
     */
    public void shrink(NumRectangle rec, int dimension) {

        if (dimension >= dims.size()) {
            throw new IllegalArgumentException("Dimension " + dimension
                    + " is not valid");
        }

        IntervalRange rangeN = dims.get(dimension).minus(rec.dims.get(dimension));

        this.setRange(dimension, rangeN);
    }

    public Map.Entry<Double, Integer> getShrinkInfo(NumRectangle rec) {

        double preserved = 0;
        Integer bestDim = 0;

        List<IntervalRange> dimsN = new ArrayList<IntervalRange>(dims.size());

        for (int i = 0; i < dims.size(); i++) {

            dimsN.add(i,dims.get(i).minus(rec.dims.get(i)));
        }



        ArrayList<Double> lengths = new ArrayList<Double>();

        long originalLength;
        long newLength;
        double reduced;

        for (int i = 0; i < dims.size(); i++) {

            originalLength = dims.get(i).getLength();
            newLength = dimsN.get(i).getLength();


            //If range was [x,x]
            if (originalLength == 0) {

                if (dimsN.get(i).isUnit()) {
                    reduced = 100;
                    lengths.add(reduced);
                }
                else if (dimsN.get(i).isEmpty()) {
                    reduced = 0;
                    lengths.add(reduced);
                }
                else {
                    System.err.println("Minus operation was wrong.");
                }
            }
            else {

                reduced = ((double) newLength) / originalLength * 100;
                lengths.add(reduced);
            }

        }


        double largest = lengths.get(0);
        int j = 0; //dimension
        for (int i = 0; i < lengths.size(); i++) {

            if ( lengths.get(i) > largest ) {

                largest = lengths.get(i);
                j = i;
            }
        }

        preserved = largest;
        bestDim = j;

        AbstractMap.SimpleEntry<Double, Integer> res =
                new AbstractMap.SimpleEntry<Double, Integer>(preserved, bestDim);

        return res;
    }



    public NumRectangle computeTightBox(NumRectangle rec) {

        List<IntervalRange> dimsN = new ArrayList<IntervalRange>(dims.size());

        for (int i = 0; i < dims.size(); i++) {

            dimsN.add(i, dims.get(i).tightRange(rec.dims.get(i)));
        }

        return new NumRectangle(dimsN);
    }

    public void setDims(List<IntervalRange> dims) {

       this.dims = new ArrayList<IntervalRange>(dims);
    }

    public List<IntervalRange> getDims() {
        return this.dims;
    }

    public boolean isInfinite() {

        //todo: fix this, do we need infinite for intervalRange?
        boolean res = false;

        return res;
    }


    public boolean isMergeable(NumRectangle rec) {

        return true;
    }


    public boolean isEmpty() {
        for (IntervalRange d : dims)
            if (d.isEmpty())
                return true;

        return false;
    }

    public boolean isEnclosing(NumRectangle r) { return contains(r); }

    public long getVolume() {

        long v = (long)1;

        for (IntervalRange d : dims) {

            if (!d.isUnit())
                v *= d.getLength();
        }

        return v;
    }

    public String toString() {

        String s = "";

        for (int i = 0; i < dims.size(); i++) {
            s += "[" + dims.get(i).getLow() + "] ";
        }

        return s;

//        String res = "rectangle:\n";
//
//        for (int i = 0; i < dims.size(); i++) {
//
//            res += "\tdim " + i + ":\n" + "\t\t" + dims.get(i).toString() + "\n";
//        }
//
//        return res;
    }

}
