package gr.demokritos.iit.irss.semagrow.stholes;

import gr.demokritos.iit.irss.semagrow.api.QueryRecord;
import gr.demokritos.iit.irss.semagrow.api.QueryResult;
import gr.demokritos.iit.irss.semagrow.api.Rectangle;
import gr.demokritos.iit.irss.semagrow.api.STHistogram;
import gr.demokritos.iit.irss.semagrow.rdf.Stat;

import java.util.*;


/**
 * Created by angel on 7/11/14.
 */
public class STHolesHistogram<R extends Rectangle<R>> implements STHistogram<R> {


    private STHolesBucket<R> root;
    private long maxBucketsNum;
    private long bucketsNum = 0;

    public STHolesHistogram() {
        //todo: choose a constant
        maxBucketsNum = 1000;
        root = null;
        bucketsNum += bucketsNum;
    }

    public STHolesHistogram(Iterable<QueryRecord<R>> workload) {
        this();
        refine(workload);
    }

    /**
     * estimates the number of tuples
     * that match rectangle {rec}
     * @param rec rectangle
     * @return number of tuples
     */
    public long estimate(R rec) {
        if (root != null)
            return estimateAux(rec, root);
        else
            return 0;
    }

    /**
     * estimates the number of tuples contained in {rec}
     * by finding the enclosing bucket(s)
     * @param rec rectangle
     * @param b bucket
     * @return estimated number of tuples
     */
    private long estimateAux(R rec, STHolesBucket<R> b) {

        boolean isEnclosingBucket = false;
        long est = 0;

        if ((b.getBox()).contains(rec)) { //unnecessary

            isEnclosingBucket = true;

            for (STHolesBucket<R> bc : b.getChildren()) {

                if ((bc.getBox()).contains(rec)) {
                    isEnclosingBucket = false;
                    est = estimateAux(rec, bc);
                    break;
                }
            }
        }

        if (isEnclosingBucket)
            return b.getEstimate(rec);
        else
            return est;
    }

    public void refine(Iterable<? extends QueryRecord<R>> workload) {

        for (QueryRecord<R> qfr : workload)
            refine(qfr);
    }

    /**
     * refines histogram using query feedback
     * @param queryRecord query feedback
     */
    public void refine(QueryRecord<R> queryRecord) {

        // check if root is null
        if (root == null) {

            root = new STHolesBucket<R>(queryRecord.getRectangle(), null, null, null);
        } else {

            // expand root
            if (!root.getBox().contains(queryRecord.getRectangle())) {

                // expand root box so that it contains q
                R boxN = root.getBox().computeTightBox(queryRecord.getRectangle());
                //todo: is this right;
                Stat statsN = countMatchingTuples(queryRecord.getRectangle(), queryRecord);
                Collection<STHolesBucket<R>> childrenN = new ArrayList<STHolesBucket<R>>();
                STHolesBucket<R> rootN = new STHolesBucket<R>(boxN, statsN, childrenN, null);
                rootN.addChild(root);
                root = rootN;
            }
        }

        // get all c
        Iterable<STHolesBucket<R>> candidates = getCandidateBuckets(queryRecord);

        for (STHolesBucket<R> bucket : candidates) {

            STHolesBucket<R> hole = shrink(bucket, queryRecord); //calculate intersection and shrink it

            if (isInaccurateEstimation(bucket,hole))
                drillHole(bucket, hole);
        }

        // check if histogram must be compacted after refinement
        compact();
    }

    private boolean isInaccurateEstimation(STHolesBucket<R> bucket, STHolesBucket<R> hole) {

        int epsilon = 0; //todo: adjust parameter
        Stat actualStatistics = hole.getStatistics();
        Double actualDensity = actualStatistics.getDensity();

        Stat curStatistics = bucket.getStatistics();
        Double curDensity = curStatistics.getDensity();

        return (Math.abs(actualDensity - curDensity) > epsilon);
    }

    /**
     * creates a new bucket that has a rectangle that does not intersect with the children of {bucket}
     * and contains the number of tuples that matches the queryRecord
     * @param bucket parent bucket
     * @param queryRecord query feedback
     * @return shrinked bucket
     */
    private STHolesBucket<R> shrink(STHolesBucket<R> bucket, QueryRecord<R> queryRecord) {

        // Find candidate hole
        R c = bucket.getBox().intersection(queryRecord.getRectangle());

        // Shrink candidate hole in such a way that b does not intersect
        // with the rectangles of bucket.getChildren();
        List<STHolesBucket<R>> participants = new LinkedList<STHolesBucket<R>>();

        updateParticipants(participants, bucket, c);
            for (STHolesBucket<R> participant : participants) {

            c.shrink(participant.getBox());
            updateParticipants(participants, bucket, c);

            if (participants.isEmpty()) {

                break;
            }
        }


        //TODO: create a new rectangle / this is not the way to do it!
        //todo: is this still a todo?
        //R r = bucket.getBox();


        // Collect candidate hole statistics
        Stat stats= countMatchingTuples(c, queryRecord);

        // Create candidate hole bucket

        return new STHolesBucket<R>(c, stats, null, null);
    }

    /**
     * finds {bucket}'s children that partially intersect
     * with candidate hole c and stores them
     * in {participants} list
     * @param participants list of participants
     * @param bucket parent bucket
     * @param c candidate hole
     */
    private void updateParticipants(List<STHolesBucket<R>> participants,
                                    STHolesBucket<R> bucket, R c) {

        participants.clear();

        for (STHolesBucket<R> bi : bucket.getChildren()) {
            if ((c.intersects(bi.getBox())) && (!c.contains(bi.getBox()))) {

                participants.add(bi);
            }
        }
    }

    /**
     * finds the smallest box that encloses both {b1} and {b2} and
     * does not intersect partially with any other child of their parent
     * @param b1 sibling 1
     * @param b2 sibling 2
     * @return box after merge
     */
    private R getSiblingSiblingBox(STHolesBucket<R> b1, STHolesBucket<R> b2) {


        // Get parent
        STHolesBucket<R> bp = b1.getParent(); //todo: check if they are siblings
        // Find tightly enclosing box
        R c = b1.getBox().computeTightBox(b2.getBox());


        // Expand tightly enclosing box
        List<STHolesBucket<R>> participants = new LinkedList<STHolesBucket<R>>();

        updateParticipants(participants, bp, c);

        for (STHolesBucket<R> participant : participants) {

            c = c.computeTightBox(participant.getBox());

            updateParticipants(participants, bp, c);

            if (participants.isEmpty()) {

                break;
            }
        }


        return c;
    }

    /**
     * get STHolesBuckets that have nonempty intersection with a queryrecord
     * @param queryRecord query feedback
     * @return buckets that intersect with queryRecord
     */
    private Iterable<STHolesBucket<R>> getCandidateBuckets(QueryRecord<R> queryRecord) {

        R queryBox = queryRecord.getRectangle();

        Collection<STHolesBucket<R>> candidates = new LinkedList<STHolesBucket<R>>();

        // check if there are bucket with boxes that intersect with the rectangle of the query


        candidates = getCandidateBucketsAux(root, candidates, queryBox);

        return candidates;
    }

    //Tested
    private Collection<STHolesBucket<R>> getCandidateBucketsAux(
            STHolesBucket<R> b, Collection<STHolesBucket<R>> candidates,
            R queryBox)
    {

        R c = b.getBox();


        if (c.intersects(queryBox)) {

            candidates.add(b);
        }


        for (STHolesBucket<R> bc : b.getChildren())
            getCandidateBucketsAux(bc,candidates,queryBox);

        return candidates;
    }


    /**
     * Count the tuples of the query result set that match the criteria of the given bucket.
     * @param rectangle rectangle
     * @param queryRecord query feedback
     * @return statistics
     */
    private Stat countMatchingTuples(R rectangle, QueryRecord<R> queryRecord) {

        QueryResult<R> qr = queryRecord.getResultSet();

        return qr.getCardinality(rectangle);
    }

    /**
     * Create a hole (i.e. a child STHolesBucket) inside an existing bucket
     * @param parentBucket parent bucket
     * @param candidateHole candidate hole
     */
    private void drillHole(STHolesBucket<R> parentBucket, STHolesBucket<R> candidateHole)
    {

        if (parentBucket.getBox().equals(candidateHole.getBox())) {

            Stat parentStats = new Stat(candidateHole.getStatistics().getFrequency(),
                    candidateHole.getStatistics().getDistinctCount());
            parentBucket.setStatistics(parentStats);
           // parentBucket.setFrequency(candidateHole.getFrequency());
           // parentBucket.setDistinct(candidateHole.getDistinct());
        }
        else {

            //STHolesBucket bn = new STHolesBucket(holeBoundaries, holeFrequency,null,parentBucket,distinct);
            candidateHole.setParent(parentBucket);

            parentBucket.addChild(candidateHole);

            bucketsNum += 1;

            for (STHolesBucket<R> bc : parentBucket.getChildren()) {

                if (candidateHole.getBox().contains(bc.getBox())){

                    bc.setParent(candidateHole);
                }
            }
        }
    }

    /**
     * merges superfluous buckets
     */
    private void compact() {

        // while too many buckets compute merge penalty for each parent-child
        // and sibling pair, find the one with the minimum penalty and
        // call merge(b1,b2,bn)
        while (bucketsNum > maxBucketsNum) {

             MergeInfo<R> bestMerge = findBestMerge(root);
             STHolesBucket<R> b1 = bestMerge.getB1();
             STHolesBucket<R> b2 = bestMerge.getB2();
             STHolesBucket<R> bn = bestMerge.getBn();

            STHolesBucket.merge(b1, b2, bn);
            bucketsNum -= 1;
        }
    }

    /**
     * identifies the merge with lowest penalty
     * and returns the buckets to be merged and
     * the resulting box
     * @param b bucket
     * @return best merge
     */
    private MergeInfo<R> findBestMerge(STHolesBucket<R> b) {

        MergeInfo<R> bestMerge;
        MergeInfo<R> candidateMerge;
        long minimumPenalty = Integer.MAX_VALUE;
        long penalty;
        Map.Entry<STHolesBucket<R>, Long> candidateMergedBucket;

        // Initialize buckets to be merged and resulting bucket
        STHolesBucket<R> b1 = b;
        STHolesBucket<R> b2 = b;
        STHolesBucket<R> bn = b;

        for (STHolesBucket<R> bi : b.getChildren()) {

            // Candidate parent-child merges
            candidateMergedBucket = getPCMergePenalty(b, bi);
            penalty = candidateMergedBucket.getValue();

            if (penalty  <= minimumPenalty) {

                minimumPenalty = penalty;
                b1 = b;
                b2 = bi;
                bn = candidateMergedBucket.getKey();
            }

            // Candidate sibling-sibling merges
            for (STHolesBucket<R> bj : b.getChildren()) {

                if (!bj.equals(bi)) {

                    candidateMergedBucket = getSSMergePenalty(b, bi);
                    penalty = candidateMergedBucket.getValue();

                    if (penalty  <= minimumPenalty) {

                        minimumPenalty = penalty;
                        b1 = bi;
                        b2 = bj;
                        bn = candidateMergedBucket.getKey();
                    }
                }
            }
        }

        // local best merge
        bestMerge = new MergeInfo<R>(b1, b2, bn, minimumPenalty);

        for (STHolesBucket<R> bc : b.getChildren()) {

            candidateMerge = findBestMerge(bc);

            if (candidateMerge.getPenalty() <= minimumPenalty) {

                bestMerge = candidateMerge;
            }
        }

        return bestMerge;
    }

    /**
     * computes the penalty of merging parent bucket {bp}
     * with child bucket {bp} and the resulting box
     * @param bp parent bucket
     * @param bc child bucket
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesBucket<R>, Long>
            getPCMergePenalty(STHolesBucket<R> bp, STHolesBucket<R> bc) {

        if (!bc.getParent().equals(bp)) {
            //todo: throw exception
            return null;
        }
        R newBox = bp.getBox();
        long newFreq = bp.getStatistics().getFrequency();
        List<Long> newDistinct = bp.getStatistics().getDistinctCount();
        STHolesBucket<R> newParent = bp.getParent();
        Stat newStatistics = new Stat(newFreq, newDistinct);

        STHolesBucket<R> bn = new STHolesBucket<R>(newBox, newStatistics, null, newParent);
        long penalty = Math.abs(estimate(bc.getBox()) - estimate(bp.getBox()));

        AbstractMap.SimpleEntry<STHolesBucket<R>, Long> res = new AbstractMap.SimpleEntry<STHolesBucket<R>, Long>(bn, penalty);

        return res;
    }

    /**
     * computes the penalty of merging siblings {b1} and {b2}
     * and the resulting box
     * @param b1 sibling 1
     * @param b2 sibling 2
     * @return pair of merge penalty and resulting box
     */
    private Map.Entry<STHolesBucket<R>, Long>
        getSSMergePenalty(STHolesBucket<R> b1, STHolesBucket<R> b2) {

        if (!b1.getParent().equals(b2.getParent())) {
            //todo: throw exception
            return null;
        }
        R newBox = getSiblingSiblingBox(b1,b2);
        // the smallest box that encloses both b1 and b2 but does not
        // intersect partially with any other of bp


        // I contains bp's children which are enclosed by bn box
        Collection<STHolesBucket<R>> I = new ArrayList<STHolesBucket<R>>();
        STHolesBucket<R> bp = b1.getParent();

        for (STHolesBucket<R> bi : bp.getChildren() ) {

            if (bi.getBox().contains(newBox)) {
                I.add(bi);
            }
        }

        // Set statistics
        long newFrequency = b1.getStatistics().getFrequency() + b2.getStatistics().getFrequency();
        List<Long> newDistinct = b1.getStatistics().getDistinctCount();
        List<Long> curDistinct = b2.getStatistics().getDistinctCount();

        for (int i = 0; i < newDistinct.size(); i++) {

            newDistinct.set(i, Math.max(newDistinct.get(i), curDistinct.get(i)));
        }

        for (STHolesBucket<R> bi : I) {

            curDistinct = bi.getStatistics().getDistinctCount();
            newFrequency += bi.getStatistics().getFrequency() ;

            for (int i = 0; i < newDistinct.size(); i++) {

                newDistinct.set(i,  Math.max(newDistinct.get(i), curDistinct.get(i)));
            }
        }

        //Add children
        Collection<STHolesBucket<R>> newChildren = new ArrayList<STHolesBucket<R>>();
        I.addAll(b1.getChildren());
        I.addAll(b2.getChildren());



        for (STHolesBucket<R> bi : I) {

            newChildren.add(bi);
        }


        // Create bn
        Stat newStatistics = new Stat(newFrequency, newDistinct);
        STHolesBucket<R> bn = new STHolesBucket<R>(newBox, newStatistics, newChildren, null);

        long penalty;
        penalty = Math.abs(b1.getEstimate(b1.getBox()) - bn.getEstimate(b1.getBox()))
        + Math.abs(b2.getEstimate(b2.getBox())-bn.getEstimate(b2.getBox()))
        + Math.abs(bp.getEstimate(bn.getBox()) - bn.getEstimate(bn.getBox()));

        AbstractMap.SimpleEntry<STHolesBucket<R>, Long> res =
                new AbstractMap.SimpleEntry<STHolesBucket<R>, Long>(bn, penalty);

        return res;
    }
}
