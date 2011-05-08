package vision;

import java.awt.Point;


/*
 * Because Java doesn't have tuples.
 */
public class MergeZone {
	public int count;
	public Point point;
	public MergeZone(Point p){
		this.point = p;
	}
	public void merge(Point p){
		this.point = new Point( (this.point.x*this.count+p.x)/(this.count+1), (this.point.y*this.count+p.y)/(this.count+1) );
		this.count++;
	}
	public double distance(Point p){
		return Math.sqrt( (this.point.x-p.x)*(this.point.x-p.x) + (this.point.y-p.y)*(this.point.y-p.y) );
	}
}
