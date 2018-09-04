import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import data.Line;

public class LineHough {
	int[] input;
	int[] output;
	int width;
	int height;
	int[] acc;
	int minimumLengthOfLine;
	List<Line> results;
	FindParallelogram parallelogramDetector;
	
	public void init(int[] inputIn, int widthIn, int heightIn) {
		width = widthIn;
		height = heightIn;
		input = new int[width * height];
		output = new int[width * height];
		input = inputIn;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				output[y * width + x] = 0xff000000;
			}
		}
	}

	public void setLength(int len) {
		minimumLengthOfLine = len;
	}

	// hough transform for lines (polar), returns the accumulator array
	public int[] process() {
		// for polar we need accumulator of 360 degress * the longest length in
		// the image
		int rmax = (int) Math.sqrt(width * width + height * height);
		acc = new int[rmax * 360];
		int r;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// foreground 0xff000000
				if ((input[y * width + x] & 0xff) == 0) {
					for (int angle = 0; angle < 360; angle++) {
						r = (int) (x * Math.cos(((angle) * Math.PI) / 180) + y * Math.sin(((angle) * Math.PI) / 180));
						if ((r > 0) && (r <= rmax))
							acc[r * 360 + angle] = acc[r * 360 + angle] + 1;
					}
				}
			}
		}

		// now normalise to 255 and put in format for a pixel array
		int max = 0;

		// Find max acc value
		for (r = 0; r < rmax; r++) {
			for (int angle = 0; angle < 360; angle++) {
				if (acc[r * 360 + angle] > max) {
					//System.out.println("r =" + r + ", theta = " + angle * 3 + 1);
					max = acc[r * 360 + angle];
				}
			}
		}

		// System.out.println("Max :" + max);

		// Normalise all the values
		int value;
		for (r = 0; r < rmax; r++) {
			for (int angle = 0; angle < 360; angle++) {
				value = (int) (((double) acc[r * 360 + angle] / (double) max) * 255.0);
				acc[r * 360 + angle] = 0xff000000 | (value << 16 | value << 8 | value);
			}
		}

		threshold();
		
		//find parallelograms
		parallelogramDetector = new FindParallelogram(width, height, new ArrayList<Line>(results));
		parallelogramDetector.findParallelogram();
		
		return output;
	}

	private void threshold() {
		// for polar we need accumulator of 360degress * the longest length in
		// the image
		int rmax = (int) Math.sqrt(width * width + height * height);
		results = new ArrayList<>();

		for (int r = 0; r < rmax; r++) {
			for (int angle = 0; angle < 360; angle++) {
				int value = (acc[r * 360 + angle] & 0xff);
				// if its higher than the threshold add it
				if (value >= minimumLengthOfLine) {
					Line line = new Line(value, r, angle);
					results.add(line);
				}
			}
		}

		for (Line line : results) {
			drawPolarLine(line.value, line.r, line.angle);
		}
	}

	// draw a line given polar coordinates (and an input image to allow drawing
	// more than one line)
	private void drawPolarLine(int value, int r, int angle) {
		for (int x = 0; x < width; x++) {

			for (int y = 0; y < height; y++) {

				int temp = (int) (x * Math.cos(((angle) * Math.PI) / 180) + y * Math.sin(((angle) * Math.PI) / 180));
				if ((temp - r) == 0)
					output[y * width + x] = 0xff000000 | (value << 16 | value << 8 | value);

			}
		}
	}

}