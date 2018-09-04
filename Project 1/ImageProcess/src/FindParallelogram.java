import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.Line;
import data.Parallelogram;
import data.Point;

public class FindParallelogram {

	int width;
	int height;
	List<Line> lines;
	List<Parallelogram> parallelograms;
	Map<Point, Integer> points;
	
	
	public FindParallelogram(int width, int height, List<Line> lines) {
		super();
		this.width = width;
		this.height = height;
		this.lines = lines;
	}
	
	public void findParallelogram() {
		//HashMap is used to reduce duplicate find
		points = new HashMap<>();
		List<Parallelogram> parallelograms = new ArrayList<>();
		//For each line we find with Hough Transform
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			List<Line> parallels = findParallel(line);
			List<Line> crossLines = findCrossLine(line);
			for (Line parallel : parallels) {
				for (Line crossLine : crossLines) {
					Point p1 = findIntersection(crossLine, line);
					Point p2 = findIntersection(crossLine, parallel);
					//The intersection has to be valid and not already been added to the map
					if (isPointValid(p1) && isPointValid(p2) && !points.containsKey(p1) && !points.containsKey(p2)) {
						points.put(p1, 1);
						points.put(p2, 1);
						List<Line> parallelsOfCrossLine = findParallel(crossLine);
						for (Line parallelOfCrossLine : parallelsOfCrossLine) {
							Point p3 = findIntersection(parallelOfCrossLine, line);
							Point p4 = findIntersection(parallelOfCrossLine, parallel);
							if (isPointValid(p3) && isPointValid(p4) && !points.containsKey(p3) && !points.containsKey(p4)) {
								parallelograms.add(new Parallelogram(p1, p2, p3, p4));
								points.put(p3, 1);
								points.put(p4, 1);
							}
						}
					}	
				}
			}
		}
		for (Parallelogram parallelogram : parallelograms) {
			System.out.println(parallelogram.toString());
		}	
	}
	
	
	private Point findIntersection(Line line1, Line line2) {
		int angle = Math.abs(line1.angle - line2.angle);
		//We don't find intersections for two lines that are almost parallel
		if (angle > 10 && angle < 350) {
			int r1 = line1.r, r2 = line2.r;
			int a1 = line1.angle, a2 = line2.angle;
			int x = (int) ((r2*Math.sin(((a1)*Math.PI)/180) - r1*Math.sin(((a2)*Math.PI)/180))
					/(Math.cos(((a2)*Math.PI)/180) * Math.sin(((a1)*Math.PI)/180) - Math.cos(((a1)*Math.PI)/180)*Math.sin(((a2)*Math.PI)/180)));
			int y = (int) ((r2*Math.cos(((a1)*Math.PI)/180) - r1*Math.cos(((a2)*Math.PI)/180))
					/(Math.cos(((a1)*Math.PI)/180) * Math.sin(((a2)*Math.PI)/180) - Math.cos(((a2)*Math.PI)/180)*Math.sin(((a1)*Math.PI)/180)));
			Point point = new Point(x, y);
			return point;
		} else {
			return new Point(-1, -1);
		}
		
	}
	
	private boolean isPointValid(Point point) {
		//intersection must me in the image
		if (point.x < width && point.x >= 0 && point.y >= 0 && point.y < height) return true;
		else return false;
	}
	
	//If two lines almost coincide, they are not regarded as parallel here
	//There should be some distance between two parallel lines, the threshold here is 50
	private List<Line> findParallel(Line line) {
		List<Line> parallels = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			int diff = Math.abs(line.angle - lines.get(i).angle); 
			if (((diff >= 0 && diff <= 4) || (diff <= 182 && diff >= 178)) 
					&& Math.abs(line.r - lines.get(i).r) > 50)
				parallels.add(lines.get(i));
		}
		return parallels;
	}
	
	private List<Line> findCrossLine(Line line) {
		List<Line> crossLines = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			Point intersection = findIntersection(line, lines.get(i));
			if (isPointValid(intersection)) {
				crossLines.add(lines.get(i));
			}
		}
		return crossLines;
	}
	
}
