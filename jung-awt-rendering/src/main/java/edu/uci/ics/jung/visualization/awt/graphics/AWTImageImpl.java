package edu.uci.ics.jung.visualization.awt.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import edu.uci.ics.jung.visualization.awt.FourPassImageShaper;
import edu.uci.ics.jung.visualization.graphics.GraphicsContext;
import edu.uci.ics.jung.visualization.graphics.Image;
import edu.uci.ics.jung.visualization.graphics.ShapeProducer;

public class AWTImageImpl extends Image implements ShapeProducer {
	java.awt.Image image;
	
	public AWTImageImpl(int width, int height) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	
	public java.awt.Image getAWTImage() {
		return image;
	}
	
	public AWTImageImpl(java.awt.Image image) {
		this.image = image;
	}
	
	public GraphicsContext getGraphicsContext() {
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		return new G2DGraphicsContext(g2d);
	}

	public int getWidth() {
		return image.getWidth(null);
	}
	
	public int getHeight() {
		return image.getHeight(null);
	}

	public Image getScaledInstance(int width, int height, int hints) {
		java.awt.Image i = image.getScaledInstance(width, height, hints);
		return new AWTImageImpl(i);
	}

	
	public Shape getShape() {
		return FourPassImageShaper.getShape(image, 30);
	}
}
