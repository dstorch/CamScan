package vision;

import java.awt.Point;
import java.util.ArrayList;
import javax.vecmath.Vector2d;
import core.Corners;

/*
 * Represents a set of potential corners and related logic to evaluate how "good" they are.
 */

public class PotentialCorners implements Comparable {
	public Corners corners;
	public double score;
	public PotentialCorners(){
		this.score = 0;
	}
	
	private double getAngle(Point center, Point arm1, Point arm2){
		Vector2d v1 = new Vector2d(arm1.x-center.x, arm1.y-center.y);
		Vector2d v2 = new Vector2d(arm2.x-center.x, arm2.y-center.y);
		return v1.angle(v2);
	}
	
	//assigns self score based on how symmetric the angles are
	private double metric1(){
		double tl_angle = getAngle(this.corners.upleft(), this.corners.upright(), this.corners.downleft());
		double tr_angle = getAngle(this.corners.upright(), this.corners.upleft(), this.corners.downright());
		double bl_angle = getAngle(this.corners.downleft(), this.corners.upleft(), this.corners.downright());
		double br_angle = getAngle(this.corners.downright(), this.corners.downleft(), this.corners.upright());
		
		return -(Math.pow(tl_angle-br_angle,2) + Math.pow(tr_angle-bl_angle,2));
	}
	
	//score according to how close to 90 degrees they all are
	private double metric2(){
		double tl_angle = getAngle(this.corners.upleft(), this.corners.upright(), this.corners.downleft());
		double tr_angle = getAngle(this.corners.upright(), this.corners.upleft(), this.corners.downright());
		double bl_angle = getAngle(this.corners.downleft(), this.corners.upleft(), this.corners.downright());
		double br_angle = getAngle(this.corners.downright(), this.corners.downleft(), this.corners.upright());
		
		return -(Math.pow(tl_angle-Math.PI/2,2) + Math.pow(tr_angle-Math.PI/2,2) + Math.pow(bl_angle-Math.PI/2,2) + Math.pow(br_angle-Math.PI/2,2));
	}
	
	public void metrics(){
		this.score = this.metric2();
	}
	
	//potential other heuristics
	
	//metric 2: total area covered
	//corner weight
	//whether they have corners near the image corners
	//common aspect ratios
	//straightness of line
	
	public int compareTo(Object a) {
		if (a instanceof PotentialCorners){
			PotentialCorners other = (PotentialCorners)a;
			if (other.score < this.score){
				return -1;
			}else if (this.score < other.score){
				return 1;
			}
		}
		return 0;
	}
	
	public String toString(){
		return "<PotentialCorners: " + this.corners.toString() + " score: " + this.score + ">";
	}
}
