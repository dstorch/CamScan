package vision;

import java.awt.Point;


/*
 * Because Java doesn't have tuples.
 * Keeps track of a set of values for a potential cluster: position, weight, number of underlying variables...
 */
public class MergeZone implements Comparable{
	public int count;
	public Point point;
	public double weight;
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
	
	
	public int compareTo(Object a) {
		if (a instanceof MergeZone){
			MergeZone other = (MergeZone)a;
			if (other.weight < this.weight){
				return -1;
			}else if (this.weight < other.weight){
				return 1;
			}
		}
		return 0;
	}
}
