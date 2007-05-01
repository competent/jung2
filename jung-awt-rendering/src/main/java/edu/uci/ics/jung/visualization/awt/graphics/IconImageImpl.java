package edu.uci.ics.jung.visualization.awt.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import edu.uci.ics.jung.visualization.awt.FourPassImageShaper;
import edu.uci.ics.jung.visualization.graphics.GraphicsContext;
import edu.uci.ics.jung.visualization.graphics.Image;
import edu.uci.ics.jung.visualization.graphics.ImageDrawDelegate;
import edu.uci.ics.jung.visualization.graphics.ShapeProducer;

public class IconImageImpl extends Image implements ImageDrawDelegate {
	Icon icon;
	
	public Icon getIcon() {
		return icon;
	}
	
	public IconImageImpl(Icon icon) {
		this.icon = icon;
	}
	
	

	public int getWidth() {
		return icon.getIconWidth();
	}
	
	public int getHeight() {
		return icon.getIconHeight();
	}

	public Image getScaledInstance(int width, int height, int hints) {
		throw new UnsupportedOperationException();
	}


	public GraphicsContext getGraphicsContext() {
		throw new UnsupportedOperationException();
	}
}
