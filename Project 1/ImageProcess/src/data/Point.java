package data;
public class Point {
	public int x,y;
	public Point(int x, int y) {
		this.x = x;
		this.y = y;		
	}
	public String toString() {
		String str = "(" + x + ", " + y + ")"; 
		return str;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 7;
		result = prime * result + 11;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (Math.abs(x - other.x) > 3)
			return false;
		if (Math.abs(y - other.y) > 3)
			return false;
		return true;
	}
	
}