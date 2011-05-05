package centralwidget;

public class PointTransform {

	public double dx;
	public double dy;
	public double dragX;
	public double dragY;
	public boolean dragging;
	
	public PointTransform() {
		this.dragging = false;
		this.dx = 0;
		this.dy = 0;
		this.dragX = 0;
		this.dragY = 0;
	}
	
}
