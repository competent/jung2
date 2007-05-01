package edu.uci.ics.jung.visualization.swt;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

/**
 * The SWTGraphics2D class wraps the Graphics2D around a given SWt GC and try to mimics as
 * much of the original Graphics2D behavior as possible, only using GC methods.
 * <p>
 * This class can be used wherever a standard Graphics2D object is legal. The main purpose
 * of this class is then to reuse Java2D code without changing it, while drawing directly
 * in an SWT Drawable.
 * </p>
 * 
 * @author Christophe Avare
 * @author Grant Slender
 * @version $Revision: 1.1.2.1 $
 * @since 1.0
 */
public class SWTGraphics2D extends Graphics2D {
	private final static AffineTransform IDENTITY = new AffineTransform();

	/**
	 * All current rendering hints. Only renderings hints meaningful to the current
	 * implementation are stored in this map, others are silently ignored.
	 */
	private RenderingHints hints = new RenderingHints(null);

	/**
	 * The last valus of GC.getBackground().
	 * 
	 * @see swapColors()
	 */
	private org.eclipse.swt.graphics.Color oldBg;

	/**
	 * The last valus of GC.getForeground().
	 * 
	 * @see swapColors()
	 */
	private org.eclipse.swt.graphics.Color oldFg;

	/**
	 * If true, this flag means the fg / bg colors must be swapped before rendering. This
	 * flag is normally set to true to prevent the fillXXX methods to render using the
	 * background color (the SWT behavior) instead of the foreground color (the AWT
	 * behavior).
	 */
	private boolean needSwap = true;

	private GC _gc;

	private Device _dev;

	public SWTGraphics2D(GC gc, Device dev) {
		super();
		_gc = gc;
		_dev = (dev == null) ? Display.getDefault() : dev;
		switch (_gc.getAntialias()) {
			case SWT.OFF :
				hints.put(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
				break;
			case SWT.ON :
				hints.put(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
				break;
			case SWT.DEFAULT :
				hints.put(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_DEFAULT);
				break;
		}
		switch (_gc.getTextAntialias()) {
			case SWT.OFF :
				hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
				break;
			case SWT.ON :
				hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				break;
			case SWT.DEFAULT :
				hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
				break;
		}
	}

	public SWTGraphics2D(GC gc) {
		this(gc, null);
	}

	/**
	 * Swap the GC foreground and background colors.
	 * <p>
	 * It is expected that methods like fillXXX have a call to this method before and
	 * after the actual call (calling swapColors in pairs restores the initial state).
	 * </p>
	 */
	private void swapColors() {
		if (needSwap) {
			oldBg = _gc.getBackground();
			oldFg = _gc.getForeground();
			_gc.setBackground(oldFg);
			_gc.setForeground(oldBg);
		}
	}

	/**
	 * Converts a Java2D AffineTransform into an SWT Transform.
	 * <p>
	 * The caller must call dispose() on the returned instance.
	 * </p>
	 * <p>
	 * This method is not static because the returned transform is bound to the same
	 * Device as the orginal GC this instance represents.
	 * </p>
	 * 
	 * @param t
	 *            The Java2D affine transform
	 * @return An SWT Transform initialized with the affine transform values
	 */
	public Transform toSWTTransform(AffineTransform t) {
		double[] m = new double[6];
		t.getMatrix(m);
		return new Transform(_dev, (float) m[0], (float) m[1], (float) m[2],
			(float) m[3], (float) m[4], (float) m[5]);
	}

	/**
	 * Converts an SWT Color into an AWT Color.
	 * <p>
	 * The returned Color have its alpha channel set to the current alpha value of the GC.
	 * As a consequence, colors cannot be cached or resused.
	 * </p>
	 * 
	 * @param rgb
	 *            The SWT color
	 * @return The AWT color
	 */
	public Color toAWTColor(org.eclipse.swt.graphics.Color rgb) {
		return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), _gc.getAlpha());
	}

	/**
	 * Converts an AWT Color into an SWT Color.
	 * <p>
	 * The alpha channel of the AWT color is lost in the process.
	 * </p>
	 * <p>
	 * The caller must call dispose() on the returned instance.
	 * </p>
	 * <p>
	 * This method is not static because the returned color is bound to the same Device as
	 * the orginal GC this instance represents.
	 * </p>
	 * 
	 * @param rgba
	 * @return
	 */
	public org.eclipse.swt.graphics.Color toSWTColor(Color rgba) {
		return new org.eclipse.swt.graphics.Color(_dev, rgba.getRed(), rgba.getGreen(),
			rgba.getBlue());
	}

	/**
	 * Converts a Java2D Shape into an SWT Path object.
	 * <p>
	 * Coordinates are supposed to be expressed in the original (untransformed) user space
	 * following the Java2D convention for coordinates.
	 * </p>
	 * <p>
	 * This method uses the PathIterator mechanism to iterate over the shape.
	 * </p>
	 * <p>
	 * The caller must call dispose() on the returned instance.
	 * </p>
	 * <p>
	 * This method is not static because the returned path is bound to the same Device as
	 * the orginal GC this instance represents.
	 * </p>
	 * 
	 * @param s
	 *            The Shape to convert
	 * @return The equivalent Path
	 */
	public Path toPath(Shape s) {
		float[] coords = new float[6];
		Path p = new Path(_dev);
		PathIterator it = s.getPathIterator(IDENTITY);
		while (!it.isDone()) {
			int type = it.currentSegment(coords);
			switch (type) {
				case PathIterator.SEG_MOVETO :
					p.moveTo(coords[0], coords[1]);
					break;
				case PathIterator.SEG_LINETO :
					p.lineTo(coords[0], coords[1]);
					break;
				case PathIterator.SEG_QUADTO :
					p.quadTo(coords[0], coords[1], coords[2], coords[3]);
					break;
				case PathIterator.SEG_CUBICTO :
					p.cubicTo(coords[0], coords[1], coords[2], coords[3], coords[4],
						coords[5]);
					break;
				// FIXME: after the SEG_CLOSE, we should be prepared for a new path.
				// FIXME: we should consider the winding rule.
				case PathIterator.SEG_CLOSE :
					p.close();
					break;
			}
			it.next();
		}
		return p;
	}

	@Override
	public void addRenderingHints(Map<?, ?> map) {
		setRenderingHints(map);
	}

	@Override
	public void clip(Shape s) {
		Path p = toPath(s);
		_gc.setClipping(p);
		p.dispose();
	}

	@Override
	public void draw(Shape s) {
		Path p = toPath(s);
		_gc.drawPath(p);
		p.dispose();
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		// FIXME
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		// FIXME
	}

	@Override
	public void drawString(String s, float x, float y) {
		drawString(s, (int) x, (int) y);
	}

	@Override
	public void drawString(String str, int x, int y) {
		int fh = _gc.getFontMetrics().getAscent();
		_gc.drawString(str, x, y - fh, true);
	}

	@Override
	public void fill(Shape s) {
		Path p = toPath(s);
		swapColors();
		_gc.fillPath(p);
		swapColors();
		p.dispose();
	}

	@Override
	public Color getBackground() {
		return toAWTColor(_gc.getBackground());
	}

	/**
	 * Only SRC rule is meaningful in SWT!
	 */
	@Override
	public Composite getComposite() {
		return AlphaComposite.Src;
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		// FIXME
		return null;
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		// FIXME
		return null;
	}

	@Override
	public Paint getPaint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		return hints.get(hintKey);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return hints;
	}

	@Override
	public Stroke getStroke() {
		// FIXME
		return null;
	}

	@Override
	public AffineTransform getTransform() {
		// FIXME
		return null;
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void rotate(double theta, double x, double y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotate(double theta) {
		Transform t = new Transform(_dev);
		_gc.getTransform(t);
		t.rotate((float) Math.toDegrees(theta));
		_gc.setTransform(t);
		t.dispose();
	}

	@Override
	public void scale(double sx, double sy) {
		Transform t = new Transform(_dev);
		_gc.getTransform(t);
		t.scale((float) sx, (float) sy);
		_gc.setTransform(t);
		t.dispose();
	}

	@Override
	public void setBackground(Color color) {
		_gc.setBackground(toSWTColor(color));
		_gc.setAlpha(color.getAlpha());
	}

	/**
	 * Composite ops are ignored for SWT compatibility.
	 */
	@Override
	public void setComposite(Composite comp) {
	}

	/**
	 * Approximate a Paint object with an SWT equivalent.
	 * 
	 * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
	 */
	@Override
	public void setPaint(Paint paint) {
		needSwap = true;
		if (paint instanceof Color) {
			setColor((Color) paint);
		} else if (paint instanceof GradientPaint) {
			GradientPaint gp = (GradientPaint) paint;
			Point2D p1 = gp.getPoint1();
			Point2D p2 = gp.getPoint2();
			org.eclipse.swt.graphics.Color c1 = toSWTColor(gp.getColor1());
			org.eclipse.swt.graphics.Color c2 = toSWTColor((gp.getColor2()));
			Pattern p = new Pattern(_dev, (float) p1.getX(), (float) p1.getY(),
				(float) p2.getX(), (float) p2.getY(), c1, c2);
			_gc.setBackgroundPattern(p);
			c1.dispose();
			c2.dispose();
			needSwap = false;
		} else if (paint instanceof TexturePaint) {
			TexturePaint tp = (TexturePaint) paint;
			BufferedImage awtImg = tp.getImage();
			org.eclipse.swt.graphics.Image swtImg = new org.eclipse.swt.graphics.Image(
				_dev, awtImg.getWidth(), awtImg.getHeight());
			// FIXME: convert a BufferedImage to an SWT Image
			Pattern p = new Pattern(_dev, swtImg);
			_gc.setForegroundPattern(p);
			swtImg.dispose();
		}
	}

	/**
	 * Only honor KEY_ANTIALIASING, KEY_TEXT_ANTIALIASING and KEY_INTERPOLATION.
	 */
	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		// FIXME
		if (hintKey == RenderingHints.KEY_ANTIALIASING) {
			if (hintValue == RenderingHints.VALUE_ANTIALIAS_OFF) {
				_gc.setAntialias(SWT.OFF);
			} else if (hintValue == RenderingHints.VALUE_ANTIALIAS_ON) {
				_gc.setAntialias(SWT.ON);
			} else {
				_gc.setAntialias(SWT.DEFAULT);
			}
			hints.put(hintKey, hintValue);
		}
		if (hintKey == RenderingHints.KEY_TEXT_ANTIALIASING) {
			if (hintValue == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) {
				_gc.setTextAntialias(SWT.OFF);
			} else if (hintValue == RenderingHints.VALUE_TEXT_ANTIALIAS_ON) {
				_gc.setTextAntialias(SWT.ON);
			} else {
				_gc.setTextAntialias(SWT.DEFAULT);
			}
			hints.put(hintKey, hintValue);
		}
		if (hintKey == RenderingHints.KEY_INTERPOLATION) {
			if (hintValue == RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
				_gc.setInterpolation(SWT.LOW);
			} else if ((hintValue == RenderingHints.VALUE_INTERPOLATION_BILINEAR)
				|| (hintValue == RenderingHints.VALUE_INTERPOLATION_BICUBIC)) {
				_gc.setTextAntialias(SWT.HIGH);
			} else {
				_gc.setTextAntialias(SWT.DEFAULT);
			}
			hints.put(hintKey, hintValue);
		}
	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		for (Object hint : hints.keySet()) {
			setRenderingHint((Key) hint, hints.get(hint));
		}
	}

	/**
	 * Convert a Stroke into chnage in the GC attributes.
	 * <p>
	 * The line width is a silly integer in SWT, which means it cannot be accurately used
	 * with non-identity transforms!
	 * </p>
	 * <p>
	 * The mitter limit value cannot be specified in SWT and thus is ignored.
	 * </p>
	 * <p>
	 * The dashes for custom line styles are, like the line width, expressed in pixels!
	 * </p>
	 */
	@Override
	public void setStroke(Stroke s) {
		// We can only do things for BasicStroke objects!
		if (s instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke) s;

			// Set the line width
			_gc.setLineWidth((int) bs.getLineWidth());

			// Set the line join
			switch (bs.getLineJoin()) {
				case BasicStroke.JOIN_BEVEL :
					_gc.setLineJoin(SWT.JOIN_BEVEL);
					break;
				case BasicStroke.JOIN_MITER :
					_gc.setLineJoin(SWT.JOIN_MITER);
					break;
				case BasicStroke.JOIN_ROUND :
					_gc.setLineJoin(SWT.JOIN_ROUND);
					break;
			}

			// set the line cap
			switch (bs.getEndCap()) {
				case BasicStroke.CAP_BUTT :
					_gc.setLineCap(SWT.CAP_FLAT);
					break;
				case BasicStroke.CAP_ROUND :
					_gc.setLineCap(SWT.CAP_ROUND);
					break;
				case BasicStroke.CAP_SQUARE :
					_gc.setLineCap(SWT.CAP_SQUARE);
					break;
			}

			// Set the line style to solid by default
			_gc.setLineStyle(SWT.LINE_SOLID);

			// Look for any line style
			float[] dashes = bs.getDashArray();
			if (dashes != null) {
				// Dumb approximation here!
				// FIXME: should look closer at units for lines dashes
				int[] a = new int[dashes.length];
				for (int i = 0; i < a.length; i++) {
					a[i] = (int) dashes[i];
				}
				// this also sets the line style to LINE_CUSTOM
				_gc.setLineDash(a);
				a = null;
				dashes = null;
			}
		}
	}

	@Override
	public void setTransform(AffineTransform tx) {
		Transform t = toSWTTransform(tx);
		_gc.setTransform(t);
		t.dispose();
	}

	/**
	 * The shear transform is manually computed, no SWT equivalent.
	 */
	@Override
	public void shear(double shx, double shy) {
		Transform t = new Transform(_dev);
		_gc.getTransform(t);
		Transform shear = new Transform(_dev, 1f, (float) shx, (float) shy, 1f, 0, 0);
		t.multiply(shear);
		_gc.setTransform(t);
		t.dispose();
	}

	@Override
	public void transform(AffineTransform Tx) {
		Transform t = new Transform(_dev);
		_gc.getTransform(t);
		t.multiply(toSWTTransform(Tx));
		_gc.setTransform(t);
		t.dispose();
	}

	@Override
	public void translate(double tx, double ty) {
		Transform t = new Transform(_dev);
		_gc.getTransform(t);
		t.translate((float) tx, (float) ty);
		_gc.setTransform(t);
		t.dispose();
	}

	@Override
	public void translate(int x, int y) {
		translate((double) x, (double) y);
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		int alpha = _gc.getAlpha();
		_gc.setAlpha(0);
		// FIXME: check which color is actually used in fillRectangle!
		_gc.fillRectangle(x, y, width, height);
		_gc.setAlpha(alpha);
	}

	/**
	 * clipRect() performs a cumulative intersection of the new rectangle and the current
	 * clip rectangle.
	 */
	@Override
	public void clipRect(int x, int y, int width, int height) {
		org.eclipse.swt.graphics.Rectangle clip = _gc.getClipping();
		clip.intersect(new org.eclipse.swt.graphics.Rectangle(x, y, width, height));
		_gc.setClipping(clip);
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		_gc.copyArea(x, y, width, height, dx, dy);
	}

	@Override
	public Graphics create() {
		return new SWTGraphics2D(_gc, _dev);
	}

	@Override
	public void dispose() {
		_gc = null;
		_dev = null;
		hints.clear();
		hints = null;
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		_gc.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
		ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
		Color bgcolor, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
		ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1,
		int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1,
		int sy1, int sx2, int sy2, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		_gc.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		_gc.drawOval(x, y, width, height);
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		int[] buf = new int[nPoints * 2];
		int j = 0;
		for (int i = 0; i < nPoints; i++) {
			buf[j++] = xPoints[i];
			buf[j++] = yPoints[i];
		}
		_gc.drawPolygon(buf);
		buf = null;
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		int[] buf = new int[nPoints * 2];
		int j = 0;
		for (int i = 0; i < nPoints; i++) {
			buf[j++] = xPoints[i];
			buf[j++] = yPoints[i];
		}
		_gc.drawPolyline(buf);
		buf = null;
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth,
		int arcHeight) {
		_gc.drawRoundRectangle(x, y, width, height, arcWidth, arcHeight);
	}

	/**
	 * This method must swap colors if required.
	 * 
	 */
	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		swapColors();
		_gc.fillArc(x, y, width, height, startAngle, arcAngle);
		swapColors();
	}

	/**
	 * This method must swap colors if required.
	 * 
	 */
	@Override
	public void fillOval(int x, int y, int width, int height) {
		swapColors();
		_gc.fillOval(x, y, width, height);
		swapColors();
	}

	/**
	 * This method must swap colors if required.
	 * 
	 */
	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		int[] buf = new int[nPoints * 2];
		int j = 0;
		for (int i = 0; i < nPoints; i++) {
			buf[j++] = xPoints[i];
			buf[j++] = yPoints[i];
		}
		swapColors();
		_gc.fillPolygon(buf);
		swapColors();
		buf = null;
	}

	/**
	 * This method must swap colors if required.
	 * 
	 */
	@Override
	public void fillRect(int x, int y, int width, int height) {
		swapColors();
		_gc.fillRectangle(x, y, width, height);
		swapColors();
	}

	/**
	 * This method must swap colors if required.
	 * 
	 */
	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth,
		int arcHeight) {
		swapColors();
		_gc.fillRoundRectangle(x, y, width, height, arcWidth, arcHeight);
		swapColors();
	}

	/**
	 * Because GC is missing a getClipping() method that returns the Path set on input,
	 * only the clip rectangle is available! This method is thus a synonym for
	 * getClipBounds().
	 */
	@Override
	public Shape getClip() {
		return getClipBounds();
	}

	@Override
	public Rectangle getClipBounds() {
		org.eclipse.swt.graphics.Rectangle r = _gc.getClipping();
		return new Rectangle(r.x, r.y, r.width, r.height);
	}

	@Override
	public Color getColor() {
		return toAWTColor(_gc.getForeground());
	}

	/**
	 * Font size in Java2D is expressed in pixels??? The javadoc says that font sizes are
	 * in points!
	 * <p>
	 * Font size is converted in pixels by multiplying SWT font size by screen DPI / 72.
	 * </p>
	 */
	@Override
	public Font getFont() {
		FontData f = _gc.getFont().getFontData()[0];
		int style = Font.PLAIN;
		if ((f.getStyle() & SWT.BOLD) != 0) {
			style = Font.BOLD;
		}
		if ((f.getStyle() & SWT.ITALIC) != 0) {
			style |= Font.ITALIC;
		}
		// FIXME: the font used is the font size not pixel size as per Java2D
		int pixels = (int) (f.getHeight() * _dev.getDPI().x / 72.0);
		return new Font(f.getName(), style, pixels);
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		return Toolkit.getDefaultToolkit().getFontMetrics(f);
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		_gc.setClipping(x, y, width, height);
	}

	@Override
	public void setClip(Shape clip) {
		Path p = toPath(clip);
		_gc.setClipping(p);
		p.dispose();
	}

	@Override
	public void setColor(Color c) {
		_gc.setForeground(toSWTColor(c));
		_gc.setAlpha(c.getAlpha());
		needSwap = true;
	}

	/**
	 * Font size in Java2D is expressed in pixels??? The javadoc says that font sizes are
	 * in points!
	 * <p>
	 * Font size is converted in points by multiplying AWT font size by 72/ screen DPI.
	 * </p>
	 */
	@Override
	public void setFont(Font font) {
		int style = SWT.NORMAL;
		if (font.isBold()) {
			style |= SWT.BOLD;
		}
		if (font.isItalic()) {
			style |= SWT.ITALIC;
		}
		// FIXME: the font used is the font size not pixel size as per Java2D
		int points = (int) (font.getSize2D() * 72.0 / _dev.getDPI().x);

		org.eclipse.swt.graphics.Font f = new org.eclipse.swt.graphics.Font(_dev, font
			.getFamily(), points, style);
		_gc.setFont(f);
		f.dispose();
	}

	@Override
	public void setPaintMode() {
		_gc.setXORMode(false);
	}

	@Override
	public void setXORMode(Color c1) {
		// FIXME: alternate color should be set to c1?
		_gc.setXORMode(true);
	}
}
