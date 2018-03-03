package gr.demokritos.iit.irss.semagrow.base;

public class Estimation {

	private Long currentEstimation;
	private Long metric1;
	private Long metric2;
	private Long metric3;
	private Long metric4;
	private Long metric5;
	
	
	public Estimation(){
		currentEstimation = (long) 0;
		metric1 = (long) 0;
		metric2 = (long) 0;
		metric3 = (long) 0;
		metric4 = (long) 0;
		metric5 = (long) 0;
	}
	
	public Estimation(Long c){
		currentEstimation = (long) c;
		metric1 = (long) c;
		metric2 = (long) c;
		metric3 = (long) c;
		metric4 = (long) c;
		metric5 = (long) c;
	}
	
	
	
	public Long getCurrentEstimation() {
		return currentEstimation;
	}
	public void setCurrentEstimation(Long currentEstimation) {
		this.currentEstimation = currentEstimation;
	}
	public Long getMetric1() {
		return metric1;
	}
	public void setMetric1(Long metric1) {
		this.metric1 = metric1;
	}
	public Long getMetric2() {
		return metric2;
	}
	public void setMetric2(Long metric2) {
		this.metric2 = metric2;
	}
	public Long getMetric3() {
		return metric3;
	}
	public void setMetric3(Long metric3) {
		this.metric3 = metric3;
	}
	public Long getMetric4() {
		return metric4;
	}
	public void setMetric4(Long metric4) {
		this.metric4 = metric4;
	}
	public Long getMetric5() {
		return metric5;
	}
	public void setMetric5(Long metric5) {
		this.metric5 = metric5;
	}
	
	public static Estimation add(Estimation e1, Estimation e2) {
		Estimation sum = new Estimation();
		
		sum.setCurrentEstimation( e1.getCurrentEstimation() + e2.getCurrentEstimation() );
		sum.setMetric1( e1.getMetric1() + e2.getMetric1() );
		sum.setMetric2( e1.getMetric2() + e2.getMetric2() );
		sum.setMetric3( e1.getMetric3() + e2.getMetric3() );
		sum.setMetric4( e1.getMetric4() + e2.getMetric4() );
		sum.setMetric5( e1.getMetric5() + e2.getMetric5() );
		
		return sum;
	}
	
	public static Estimation multiply(Estimation e1, Estimation e2) {
		Estimation product = new Estimation();
		
		product.setCurrentEstimation( e1.getCurrentEstimation() * e2.getCurrentEstimation() );
		product.setMetric1( e1.getMetric1() * e2.getMetric1() );
		product.setMetric2( e1.getMetric2() * e2.getMetric2() );
		product.setMetric3( e1.getMetric3() * e2.getMetric3() );
		product.setMetric4( e1.getMetric4() * e2.getMetric4() );
		product.setMetric5( e1.getMetric5() * e2.getMetric5() );
		
		return product;
	}
	
	public static Estimation multiplyByDouble(Estimation e1, Double c) {
		Estimation product = new Estimation();
		
		product.setCurrentEstimation( Math.round(e1.getCurrentEstimation() * c) );
		product.setMetric1( Math.round(e1.getMetric1() * c) );
		product.setMetric2( Math.round(e1.getMetric2() * c) );
		product.setMetric3( Math.round(e1.getMetric3() * c) );
		product.setMetric4( Math.round(e1.getMetric4() * c) );
		product.setMetric5( Math.round(e1.getMetric5() * c) );
		
		return product;
	}
	
	public static Estimation max(Estimation e1, Estimation e2 ) {
		Estimation max = new Estimation();
		
		max.setCurrentEstimation( Math.max(e1.getCurrentEstimation(), e2.getCurrentEstimation()) );
		max.setMetric1( Math.max(e1.getMetric1(), e2.getMetric1()) );
		max.setMetric2( Math.max(e1.getMetric2(), e2.getMetric2()) );
		max.setMetric3( Math.max(e1.getMetric3(), e2.getMetric3()) );
		max.setMetric4( Math.max(e1.getMetric4(), e2.getMetric4()) );
		max.setMetric5( Math.max(e1.getMetric5(), e2.getMetric5()) );
		
		return max;
	}
	
	public static Estimation min(Estimation e, Long c) {
		Estimation min = new Estimation();
		
		min.setCurrentEstimation( Math.min(e.getCurrentEstimation(), c) );
		min.setMetric1( Math.min(e.getMetric1(), c) );
		min.setMetric2( Math.min(e.getMetric2(), c) );
		min.setMetric3( Math.min(e.getMetric3(), c) );
		min.setMetric4( Math.min(e.getMetric4(), c) );
		min.setMetric5( Math.min(e.getMetric5(), c) );
		
		return min;
	}
	
	@Override
	public String toString() {
		String estimation = "";
		estimation += getCurrentEstimation()+"\t";
		estimation += getMetric1() + "\t";
		estimation += getMetric2() + "\t";
		estimation += getMetric3() + "\t";
		estimation += getMetric4() + "\t";
		estimation += getMetric5();
		
		return estimation;
		
	}
	
}
