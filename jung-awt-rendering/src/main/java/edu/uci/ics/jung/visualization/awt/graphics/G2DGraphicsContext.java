package edu.uci.ics.jung.visualization.awt.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import javax.swing.CellRendererPane;
import javax.swing.JLabel;

import edu.uci.ics.jung.visualization.graphics.GraphicsContext;
import edu.uci.ics.jung.visualization.graphics.Image;
import edu.uci.ics.jung.visualization.graphics.Label;

public class G2DGraphicsContext implements GraphicsContext {
	protected Graphics2D delegate;
	protected CellRendererPane rendererPane = new CellRendererPane();
    
    public G2DGraphicsContext() {
        this(null);
    }
    public G2DGraphicsContext(Graphics2D delegate) {
        this.delegate = delegate;
    }
    
    public void setDelegate(Graphics2D delegate) {
        this.delegate = delegate;
    }
    
    public Graphics2D getDelegate() {
        return delegate;
    }
	
    
   
	public int getCharWidth(char c) {
		return delegate.getFontMetrics().charWidth(c);
	}
	public int getFontAscent() {
		return delegate.getFontMetrics().getAscent();
	}
	public int getFontDescent() {
		return delegate.getFontMetrics().getDescent();
	}
	public int getFontHeight() {
		return delegate.getFontMetrics().getHeight();
	}
	public int getStringWidth(String str) {
		return delegate.getFontMetrics().stringWidth(str);
	}
	
	public Boolean getAntialiasing() {
		Object value = delegate.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		if (value == RenderingHints.VALUE_ANTIALIAS_ON) return Boolean.TRUE;
		if (value == RenderingHints.VALUE_ANTIALIAS_OFF) return Boolean.FALSE;
		if (value == RenderingHints.VALUE_ANTIALIAS_DEFAULT) return null;

		return null;
	}
	public Boolean getTextAntialiasing() {
		Object value = delegate.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		if (value == RenderingHints.VALUE_TEXT_ANTIALIAS_ON) return Boolean.TRUE;
		if (value == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) return Boolean.FALSE;
		if (value == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT) return null;
		
		return null;
	}
	public void setAntialiasing(Boolean on) {
		if (on == null) delegate.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		else if (on) delegate.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		else delegate.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	public void setTextAntialiasing(Boolean on) {
		if (on == null) delegate.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
		else if (on) delegate.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		else delegate.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}
	
	
	public void dispose() {
    	delegate.dispose();
	}
	public Label createLabel() {
    	return new LabelImpl();
    }
    
    public Image createImage(int width, int height) {
    	return new AWTImageImpl(width, height);
    }
	
	public void clearRect(int x, int y, int width, int height) {
		delegate.clearRect(x, y, width, height);
	}

	public void clip(Shape s) {
		delegate.clip(s);
	}

	public void clipRect(int x, int y, int width, int height) {
		delegate.clipRect(x, y, width, height);
	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		delegate.copyArea(x, y, width, height, dx, dy);
	}

	public void draw(Shape s) {
		delegate.draw(s);
	}

	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		delegate.draw3DRect(x, y, width, height, raised);
	}

	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		delegate.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	public void drawChars(char[] data, int offset, int length, int x, int y) {
		delegate.drawChars(data, offset, length, x, y);
	}

	public void drawImage(Image img, int x, int y) {
		if (img instanceof AWTImageImpl) {
			delegate.drawImage(
					((AWTImageImpl)img).image, x, y, null);
		} else if (img instanceof IconImageImpl) {
			((IconImageImpl)img).getIcon().paintIcon(null, delegate, x, y);
		} else {
			System.err.println("Could not draw image: " + img);
		}
	}
	
	public void drawImage(Image img, AffineTransform xform) {
		AffineTransform cur = getTransform();
		AffineTransform newXForm = new AffineTransform(cur);
		newXForm.concatenate(xform);
		setTransform(newXForm);
		drawImage(img, 0, 0);
		setTransform(cur);
	}

	
	public void drawLabel(Label label, int x, int y, int w, int h) {
		rendererPane.paintComponent(delegate, (JLabel)label, null, x, y, w, h, true);
	}
	public void drawLabel(Label label, int x, int y) {
		Dimension s = label.getPreferredSize();
		rendererPane.paintComponent(delegate, (JLabel)label, null, x, y, s.width, s.height, true);
	}
	
	
	public void drawLine(int x1, int y1, int x2, int y2) {
		delegate.drawLine(x1, y1, x2, y2);
	}

	public void drawOval(int x, int y, int width, int height) {
		delegate.drawOval(x, y, width, height);
	}

	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		delegate.drawPolygon(xPoints, yPoints, nPoints);
	}

	public void drawPolygon(Polygon p) {
		delegate.drawPolygon(p);
	}

	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		delegate.drawPolyline(xPoints, yPoints, nPoints);
	}

	public void drawRect(int x, int y, int width, int height) {
		delegate.drawRect(x, y, width, height);
	}

	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		delegate.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void drawString(String s, float x, float y) {
		delegate.drawString(s, x, y);
	}

	public void drawString(String str, int x, int y) {
		delegate.drawString(str, x, y);
	}

	public void fill(Shape s) {
		delegate.fill(s);
	}

	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		delegate.fill3DRect(x, y, width, height, raised);
	}

	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		delegate.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	public void fillOval(int x, int y, int width, int height) {
		delegate.fillOval(x, y, width, height);
	}

	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		delegate.fillPolygon(xPoints, yPoints, nPoints);
	}

	public void fillPolygon(Polygon p) {
		delegate.fillPolygon(p);
	}

	public void fillRect(int x, int y, int width, int height) {
		delegate.fillRect(x, y, width, height);
	}

	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		delegate.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public Color getBackground() {
		return delegate.getBackground();
	}

	public Shape getClip() {
		return delegate.getClip();
	}

	public Rectangle getClipBounds() {
		return delegate.getClipBounds();
	}

	public Rectangle getClipBounds(Rectangle r) {
		return delegate.getClipBounds(r);
	}

	public Font getFont() {
		return delegate.getFont();
	}

	public Color getColor() {
		return delegate.getColor();
	}

	public Stroke getStroke() {
		return delegate.getStroke();
	}

	public AffineTransform getTransform() {
		return delegate.getTransform();
	}
	
	 public boolean hit(Rectangle rect,
				Shape s,
				boolean onStroke) {
		 return delegate.hit(rect, s, onStroke);
	 }

	public void setBackground(Color color) {
		delegate.setBackground(color);
	}

	public void setClip(int x, int y, int width, int height) {
		delegate.setClip(x, y, width, height);
	}

	public void setClip(Shape clip) {
		delegate.setClip(clip);
	}

	public void setFont(Font font) {
		delegate.setFont(font);
	}

	public void setColor(Color c) {
		delegate.setColor(c);
	}

	public void setStroke(Stroke s) {
		delegate.setStroke(s);
	}

	public void setTransform(AffineTransform Tx) {
		delegate.setTransform(Tx);
	}
	public Paint getPaint() {
		return delegate.getPaint();
	}
	public void setPaint(Paint paint) {
		delegate.setPaint(paint);
	}
}
