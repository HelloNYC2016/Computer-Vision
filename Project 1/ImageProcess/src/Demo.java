import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.imageio.*;
import javax.swing.event.*;

public class Demo extends JApplet {

	Image edgeDetectedImage, lineDetectedImage, parallelogramDetectedImage;
	MediaTracker tracker = null;
	PixelGrabber grabber = null;
	int width = 0, height = 0;
	String fileNames[] = { "TestImage1c.jpg", "TestImage2c.jpg", "TestImage3.jpg"};

	// slider constraints
	static final int TH_MIN = 0;
	static final int TH_MAX = 255;
	static final int TH_INIT = 40;
	int threshold = TH_INIT;
	int minimumLengthOfLines = 100;
	boolean thresholdActive = false;
	boolean isOverlaid = false;

	int imageNumber = 0;
	public int orig[] = null;

	Image image[] = new Image[fileNames.length];
	Image displayImage[] = new Image[fileNames.length];

	JPanel controlPanel, imagePanel;
	JLabel origLabel, edgeDetectedLabel, lineDetectedLabel, parallelogramDetectedLabel, comboLabel,
			minimumLengthOfLinesLabel;
	JSlider thresholdSlider, lengthSlider;
	JButton thresholding;
	JComboBox imSel;
	static Sobel edgedetector;
	static LineHough linedetector;
	static FindParallelogram parallelogramDetector;

	// Applet init function
	public void init() {

		tracker = new MediaTracker(this);
		for (int i = 0; i < fileNames.length; i++) {
			image[i] = getImage(this.getCodeBase(), fileNames[i]);
			tracker.addImage(image[i], i);
		}
		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			System.out.println("error: " + e);
		}

		Container cont = getContentPane();
		cont.removeAll();
		cont.setLayout(new BorderLayout());

		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(2, 4, 15, 0));
		imagePanel = new JPanel();

		// row 1, col 1
		comboLabel = new JLabel("SELECT AN IMAGE");
		comboLabel.setHorizontalAlignment(JLabel.CENTER);
		controlPanel.add(comboLabel);

		// row 1, col 2
		thresholding = new JButton("Thresholding Off");
		thresholding.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (thresholdActive == true) {
					thresholdActive = false;
					thresholding.setText("Thresholding Off");
					thresholdSlider.setEnabled(false);
				} else {
					thresholdActive = true;
					thresholding.setText("Threshold Value = " + threshold);
					thresholdSlider.setEnabled(true);
				}
				processImage();
			}
		});
		controlPanel.add(thresholding);

		// row 1, col 3
		minimumLengthOfLinesLabel = new JLabel("Minimum Length of Line detected = " + minimumLengthOfLines);
		minimumLengthOfLinesLabel.setHorizontalAlignment(JLabel.CENTER);
		controlPanel.add(minimumLengthOfLinesLabel);

		// row 2, col 1
		imSel = new JComboBox(fileNames);
		imageNumber = imSel.getSelectedIndex();
		imSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imageNumber = imSel.getSelectedIndex();
				// change size
				width = image[imageNumber].getWidth(null);
				height = image[imageNumber].getHeight(null);
				origLabel.setIcon(
						new ImageIcon(image[imageNumber].getScaledInstance(width / 3, -1, Image.SCALE_SMOOTH)));
				processImage();
			}
		});
		controlPanel.add(imSel);

		// row 2, col 2
		thresholdSlider = new JSlider(JSlider.HORIZONTAL, TH_MIN, TH_MAX, TH_INIT);
		thresholdSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					threshold = source.getValue();
					thresholding.setText("Threshold Value = " + threshold);
					processImage();
				}
			}
		});
		thresholdSlider.setMajorTickSpacing(40);
		thresholdSlider.setMinorTickSpacing(10);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		controlPanel.add(thresholdSlider);

		// row 2, col 3
		lengthSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, minimumLengthOfLines);
		lengthSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					minimumLengthOfLines = source.getValue();
					minimumLengthOfLinesLabel.setText("Minimum Length of Line detected = " + minimumLengthOfLines);
					processImage();
				}
			}
		});
		lengthSlider.setMajorTickSpacing(40);
		lengthSlider.setMinorTickSpacing(10);
		lengthSlider.setPaintTicks(true);
		lengthSlider.setPaintLabels(true);
		controlPanel.add(lengthSlider);

		// default size
		width = image[imageNumber].getWidth(null);
		height = image[imageNumber].getHeight(null);

		origLabel = new JLabel("Original Image",
				new ImageIcon(image[imageNumber].getScaledInstance(width / 3, -1, Image.SCALE_SMOOTH)), JLabel.CENTER);
		origLabel.setVerticalTextPosition(JLabel.BOTTOM);
		origLabel.setHorizontalTextPosition(JLabel.CENTER);
		origLabel.setForeground(Color.blue);
		imagePanel.add(origLabel);

		edgeDetectedLabel = new JLabel("Edge Detected",
				new ImageIcon(image[imageNumber].getScaledInstance(width / 3, -1, Image.SCALE_SMOOTH)), JLabel.CENTER);
		edgeDetectedLabel.setVerticalTextPosition(JLabel.BOTTOM);
		edgeDetectedLabel.setHorizontalTextPosition(JLabel.CENTER);
		edgeDetectedLabel.setForeground(Color.blue);
		imagePanel.add(edgeDetectedLabel);

		lineDetectedLabel = new JLabel("Hough Line Image",
				new ImageIcon(image[imageNumber].getScaledInstance(width / 3, -1, Image.SCALE_SMOOTH)), JLabel.CENTER);
		lineDetectedLabel.setVerticalTextPosition(JLabel.BOTTOM);
		lineDetectedLabel.setHorizontalTextPosition(JLabel.CENTER);
		lineDetectedLabel.setForeground(Color.blue);
		imagePanel.add(lineDetectedLabel);
		 
		cont.add(controlPanel, BorderLayout.NORTH);
		cont.add(imagePanel, BorderLayout.CENTER);

		processImage();

	}

	public int[] threshold(int[] original, int value) {
		for (int x = 0; x < original.length; x++) {
			if ((original[x] & 0xff) >= value)
				// foreground is set to black
				original[x] = 0xff000000;
			else
				// background is set to white
				original[x] = 0xffffffff;
		}
		return original;
	}

	private int[] overlayImage(int[] input) {

		int[] myImage = new int[width * height];

		PixelGrabber grabber = new PixelGrabber(image[imageNumber], 0, 0, width, height, myImage, 0, width);
		try {
			grabber.grabPixels();
		} catch (InterruptedException e2) {
			System.out.println("error: " + e2);
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if ((input[y * width + x] & 0xff) > 0)
					myImage[y * width + x] = 0xffff0000;
			}
		}

		return myImage;

	}

	private void processImage() {
		orig = new int[width * height];
		PixelGrabber grabber = new PixelGrabber(image[imageNumber], 0, 0, width, height, orig, 0, width);
		try {
			grabber.grabPixels();
		} catch (InterruptedException e2) {
			System.out.println("error: " + e2);
		}

		thresholdSlider.setEnabled(false);
		thresholding.setEnabled(false);
		imSel.setEnabled(false);
		edgeDetectedLabel.setEnabled(false);
		lengthSlider.setEnabled(false);

		edgedetector = new Sobel();
		linedetector = new LineHough();

		new Thread() {
			public void run() {
				// detect edge
				edgedetector.init(orig, width, height);
				orig = edgedetector.process();
				if (thresholdActive == true)
					orig = threshold(orig, threshold);
				edgeDetectedImage = createImage(new MemoryImageSource(width, height, orig, 0, width));

				// detect line
				if (thresholdActive == true) {
					linedetector.init(orig, width, height);
					linedetector.setLength(minimumLengthOfLines);
					orig = linedetector.process();
					lineDetectedImage = createImage(new MemoryImageSource(width, height, overlayImage(orig), 0, width));
				} else {
					lineDetectedImage = createImage(new MemoryImageSource(width, height, orig, 0, width));
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						edgeDetectedLabel.setIcon(
								new ImageIcon(edgeDetectedImage.getScaledInstance(width / 3, -1, Image.SCALE_SMOOTH)));
						lineDetectedLabel.setIcon(
								new ImageIcon(lineDetectedImage.getScaledInstance(width / 3, -1, Image.SCALE_SMOOTH)));
						if (thresholdActive == true) {
							thresholdSlider.setEnabled(true);
						}
						thresholding.setEnabled(true);
						imSel.setEnabled(true);
						edgeDetectedLabel.setEnabled(true);
						lengthSlider.setEnabled(true);
					}
				});

				// write image to file
				edgeDetectedLabel.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent event) {
						try {
							// Create a buffered image with transparency
							BufferedImage bimage = new BufferedImage(edgeDetectedImage.getWidth(null),
									edgeDetectedImage.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
							// Draw the image on to the buffered image
							Graphics2D bGr = bimage.createGraphics();
							bGr.drawImage(edgeDetectedImage, 0, 0, null);
							bGr.dispose();

							String filename = fileNames[imSel.getSelectedIndex()];
							filename = filename.substring(0, filename.lastIndexOf('.'));
							String path;
							if (thresholdActive == true) {
								path = String.format("/Users/shingshing/desktop/result/%s_T=%d.jpg", filename,
										threshold);
							} else {
								path = String.format("/Users/shingshing/desktop/result/%s_edge_detected.jpg", filename);
							}
							ImageIO.write(bimage, "jpg", new File(path));
						} catch (IOException e) {
							System.out.println(e);
						}
					}
				});

			}
		}.start();
	}
}