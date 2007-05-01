/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package samples.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.Checkmark;
import edu.uci.ics.jung.visualization.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.DefaultVertexImageTransformer;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.graphics.GraphicsContext;
import edu.uci.ics.jung.visualization.graphics.Image;
import edu.uci.ics.jung.visualization.graphics.LayeredImage;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicVertexRenderer;
import edu.uci.ics.jung.visualization.swt.FourPassImageShaper;
import edu.uci.ics.jung.visualization.swt.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.swt.VisualizationComposite;
import edu.uci.ics.jung.visualization.swt.decorators.VertexImageShapeTransformer;
import edu.uci.ics.jung.visualization.swt.graphics.SWTImageImpl;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

/**
 * Demonstrates the use of images to represent graph vertices.
 * The images are supplied via the VertexShapeFunction so that
 * both the image and its shape can be utilized.
 * 
 * The images used in this demo (courtesy of slashdot.org) are
 * rectangular but with a transparent background. When vertices
 * are represented by these images, it looks better if the actual
 * shape of the opaque part of the image is computed so that the
 * edge arrowheads follow the visual shape of the image. This demo
 * uses the FourPassImageShaper class to compute the Shape from
 * an image with transparent background.
 * 
 * @author Tom Nelson
 * 
 */
public class SWTVertexImageShaperDemo extends Composite {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4332663871914930864L;
	
	private static final int VERTEX_COUNT=11;

	/**
     * the graph
     */
    DirectedSparseMultigraph<Number, Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationComposite<Number, Number> vv;
    
    /**
     * some icon names to use
     */
    String[] iconNames = {
            "apple",
            "os",
            "x",
            "linux",
            "inputdevices",
            "wireless",
            "graphics3",
            "gamespcgames",
            "humor",
            "music",
            "privacy"
    };
    
    public SWTVertexImageShaperDemo(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(5, false));
        
        // create a simple graph for the demo
        graph = new DirectedSparseMultigraph<Number,Number>();
        createGraph(VERTEX_COUNT);
        
        // a Map for the labels
        Map<Number,String> map = new HashMap<Number,String>();
        for(int i=0; i<VERTEX_COUNT; i++) {
            map.put(i, iconNames[i%iconNames.length]);
        }
        
        // a Map for the Icons
        Map<Number,Image> iconMap = new HashMap<Number,Image>();
        for(int i=0; i<VERTEX_COUNT; i++) {
            String name = "/images/topic"+iconNames[i]+".gif";
            ImageLoader loader = new ImageLoader();
            
            InputStream is = VertexImageShaperDemo.class.getResourceAsStream(name);
            ImageData[] idataa = loader.load(is);
            ImageData idata = idataa[0];
//            int tpixel = idata.transparentPixel;
//            System.err.println(name + ": " + tpixel + ", " + idata.getTransparencyType() + " " + idata);
            org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(getDisplay(), idata);
//            System.err.println(name + ": " + image.getImageData().getTransparencyType() + " " + image.getImageData());
            
            try {
                Image icon = 
                    new LayeredImage(new SWTImageImpl(image));
                iconMap.put(i, icon);
            } catch(Exception ex) {
                System.err.println("You need slashdoticons.jar in your classpath to see the image "+name);
            }
        }
        
        FRLayout<Number, Number> layout = new FRLayout<Number, Number>(graph);
        layout.setMaxIterations(100);
        
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(this, SWT.NONE, layout, new Dimension(600,600));
		GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 5;
        panel.setLayoutData(gridData);
		
		vv =  panel.vv;
		vv.setBackground(Color.white);

		
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		vv.getComposite().setLayoutData(gd);

        
        // This demo uses a special renderer to turn outlines on and off.
        // you do not need to do this in a real application.
        // Instead, just let vv use the Renderer it already has
        vv.getRenderer().setVertexRenderer(new DemoRenderer<Number,Number>());

        Transformer<Number,Paint> vpf = 
            new PickableVertexPaintTransformer<Number>(vv.getServer().getPickedVertexState(), Color.white, Color.yellow);
        vv.getRenderContext().setVertexFillPaintTransformer(vpf);
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number, Number>(vv.getServer().getPickedEdgeState(), Color.black, Color.cyan));

        vv.setBackground(Color.white);
        
        
        final Transformer<Number,String> vertexStringerImpl = 
            new VertexStringerImpl<Number,String>(map);
        vv.getRenderContext().setVertexLabelTransformer(vertexStringerImpl);
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
//        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Number,String>() {
//        	URL url = getClass().getResource("/images/lightning-s.gif");
//			public String transform(Number input) {
//				
//				return "<html><img src="+url+" height=10 width=21>"+input.toString();
//			}});
        
        // For this demo only, I use a special class that lets me turn various
        // features on and off. For a real application, use VertexIconShapeTransformer instead.
        final DemoVertexImageShapeTransformer<Number> vertexIconShapeTransformer =
            new DemoVertexImageShapeTransformer<Number>(new EllipseVertexShapeTransformer<Number>());
        
        final DemoVertexImageTransformer<Number> vertexImageTransformer =
        	new DemoVertexImageTransformer<Number>();
        
        vertexIconShapeTransformer.setIconMap(iconMap);
        vertexImageTransformer.setImageMap(iconMap);
        
        vv.getRenderContext().setVertexShapeTransformer(vertexIconShapeTransformer);
        vv.getRenderContext().setVertexImageTransformer(vertexImageTransformer);
        
        // un-comment for RStar Tree visual testing
        //vv.addPostRenderPaintable(new BoundingRectanglePaintable(vv.getRenderContext(), vv.getGraphLayout()));

        // Get the pickedState and add a listener that will decorate the
        // Vertex images with a checkmark icon when they are picked
        PickedState<Number> ps = vv.getServer().getPickedVertexState();
        ps.addItemListener(new PickWithIconListener<Number>(vertexImageTransformer));
        
        vv.getServer().addPostRenderPaintable(new VisualizationServer.Paintable(){
            int x;
            int y;
            Font font;
            int swidth;
            int sheight;
            String str = "Thank You, slashdot.org, for the images!";
            
            public void paint(GraphicsContext g) {
                Dimension d = vv.getSize();
                if(font == null) {
                    font = new Font(g.getFont().getName(), Font.BOLD, 20);
                }
                g.setFont(font);
                swidth = g.getStringWidth(str);
                sheight = g.getFontAscent()+g.getFontDescent();
                x = (d.width-swidth)/2;
                y = (int)(d.height-sheight*1.5);
                
                Color oldColor = g.getColor();
                g.setColor(Color.lightGray);
                g.drawString(str, x, y);
                g.setColor(oldColor);
            }
            public boolean useTransform() {
                return false;
            }
        });

        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        
        
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());
        final ScalingControl scaler = new CrossoverScalingControl();

        GridData fillerLeftGD = new GridData();
        fillerLeftGD.grabExcessHorizontalSpace = true;
        Label fillerLeft = new Label(this, SWT.NONE);
        fillerLeft.setLayoutData(fillerLeftGD);
        
        SWTUtils.createHorizontalZoomControls(this, scaler, vv);
		
		Group effects = new Group(this, SWT.NONE);
		GridLayout ecl = new GridLayout();
		ecl.numColumns = 3;
		effects.setLayout(ecl);
		effects.setText("Image Effects");

		final Button shape = new Button(effects, SWT.CHECK);
		shape.setText("Shape");
		shape.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				vertexIconShapeTransformer.setShapeImages(shape.getSelection());
                vv.repaint();
			}
		});
        shape.setSelection(true);

        
        final Button fill = new Button(effects, SWT.CHECK);
        fill.setText("Fill");
        fill.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				vertexImageTransformer.setFillImages(fill.getSelection());
                vv.repaint();
			}
		});
        fill.setSelection(true);
        

        final Button drawOutlines = new Button(effects, SWT.CHECK);
        drawOutlines.setText("Outline");
        drawOutlines.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				vertexImageTransformer.setOutlineImages(drawOutlines.getSelection());
                vv.repaint();
			}
		});

        
        SWTUtils.createSimpleMouseControl(this, graphMouse, vv);
        
        GridData fillerRightGD = new GridData();
        fillerRightGD.grabExcessHorizontalSpace = true;
        Label fillerRight = new Label(this, SWT.NONE);
        fillerRight.setLayoutData(fillerRightGD);
    }
    
    /**
     * When Vertices are picked, add a checkmark icon to the imager.
     * Remove the icon when a Vertex is unpicked
     * @author Tom Nelson
     *
     */
    public static class PickWithIconListener<V> implements ItemListener {
        DefaultVertexImageTransformer<V> imager;
        Image checked;
        
        public PickWithIconListener(DefaultVertexImageTransformer<V> imager) {
            this.imager = imager;
            checked = new Checkmark();
        }

        public void itemStateChanged(ItemEvent e) {
            edu.uci.ics.jung.visualization.graphics.Image icon = imager.transform((V)e.getItem());
            if(icon != null && icon instanceof LayeredImage) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    ((LayeredImage)icon).add(checked);
                } else {
                    ((LayeredImage)icon).remove(checked);
                }
            }
        }
    }
    /**
     * A simple implementation of VertexStringer that
     * gets Vertex labels from a Map  
     * 
     * @author Tom Nelson
     *
     *
     */
    public static class VertexStringerImpl<V,S> implements Transformer<V,String> {
        
        Map<V,String> map = new HashMap<V,String>();
        
        boolean enabled = true;
        
        public VertexStringerImpl(Map<V,String> map) {
            this.map = map;
        }
        
        /* (non-Javadoc)
         * @see edu.uci.ics.jung.graph.decorators.VertexStringer#getLabel(edu.uci.ics.jung.graph.Vertex)
         */
        public String transform(V v) {
            if(isEnabled()) {
                return map.get(v);
            } else {
                return "";
            }
        }
        
        /**
         * @return Returns the enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }
        
        /**
         * @param enabled The enabled to set.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    /**
     * create some vertices
     * @param count how many to create
     * @return the Vertices in an array
     */
    private void createGraph(int vertexCount) {
        for (int i = 0; i < vertexCount; i++) {
            graph.addVertex(i);
        }
    	int j=0;
        graph.addEdge(j++, 0, 1, EdgeType.DIRECTED);
        graph.addEdge(j++, 3, 0, EdgeType.DIRECTED);
        graph.addEdge(j++, 0, 4, EdgeType.DIRECTED);
        graph.addEdge(j++, 4, 5, EdgeType.DIRECTED);
        graph.addEdge(j++, 5, 3, EdgeType.DIRECTED);
        graph.addEdge(j++, 2, 1, EdgeType.DIRECTED);
        graph.addEdge(j++, 4, 1, EdgeType.DIRECTED);
        graph.addEdge(j++, 8, 2, EdgeType.DIRECTED);
        graph.addEdge(j++, 3, 8, EdgeType.DIRECTED);
        graph.addEdge(j++, 6, 7, EdgeType.DIRECTED);
        graph.addEdge(j++, 7, 5, EdgeType.DIRECTED);
        graph.addEdge(j++, 0, 9, EdgeType.DIRECTED);
        graph.addEdge(j++, 9, 8, EdgeType.DIRECTED);
        graph.addEdge(j++, 7, 6, EdgeType.DIRECTED);
        graph.addEdge(j++, 6, 5, EdgeType.DIRECTED);
        graph.addEdge(j++, 4, 2, EdgeType.DIRECTED);
        graph.addEdge(j++, 5, 4, EdgeType.DIRECTED);
        graph.addEdge(j++, 4, 10, EdgeType.DIRECTED);
        graph.addEdge(j++, 10, 4, EdgeType.DIRECTED);
    }

    /** 
     * this class exists only to provide settings to turn on/off shapes and image fill
     * in this demo.
     * In a real application, use DefaultVertexIconTransformer instead.
     * 
     */
    public static class DemoVertexImageTransformer<V> extends DefaultVertexImageTransformer<V>
    	implements Transformer<V,Image> {
        
        boolean fillImages = true;
        boolean outlineImages = false;

        /**
         * @return Returns the fillImages.
         */
        public boolean isFillImages() {
            return fillImages;
        }
        /**
         * @param fillImages The fillImages to set.
         */
        public void setFillImages(boolean fillImages) {
            this.fillImages = fillImages;
        }

        public boolean isOutlineImages() {
            return outlineImages;
        }
        public void setOutlineImages(boolean outlineImages) {
            this.outlineImages = outlineImages;
        }
        
        public Image transform(V v) {
            if(fillImages) {
                return imageMap.get(v);
            } else {
                return null;
            }
        }
    }
    
    /** 
     * this class exists only to provide settings to turn on/off shapes and image fill
     * in this demo.
     * In a real application, use VertexIconShapeTransformer instead.
     * 
     */
    public static class DemoVertexImageShapeTransformer<V> extends VertexImageShapeTransformer<V> {
        
        boolean shapeImages = true;

        public DemoVertexImageShapeTransformer(Transformer<V,Shape> delegate) {
            super(delegate);
        }

        /**
         * @return Returns the shapeImages.
         */
        public boolean isShapeImages() {
            return shapeImages;
        }
        /**
         * @param shapeImages The shapeImages to set.
         */
        public void setShapeImages(boolean shapeImages) {
            shapeMap.clear();
            this.shapeImages = shapeImages;
        }

        public Shape transform(V v) {
        	Image icon = iconMap.get(v);
        	while (icon instanceof LayeredImage) {
        		icon = ((LayeredImage)icon).getBaseImage();
        	}
			if (icon != null && icon instanceof SWTImageImpl) {
				Shape shape = (Shape) shapeMap.get(icon);
				if (shape == null) {
					org.eclipse.swt.graphics.Image image = ((SWTImageImpl) icon).getSWTImage();
					if (shapeImages) {
						shape = FourPassImageShaper.getShape(image, 30);
						System.err.println("calling " + new java.util.Date());
					} else {
						shape = new Rectangle2D.Float(0, 0, 
								image.getBounds().width, image.getBounds().height);
					}
                    if(shape.getBounds().getWidth() > 0 && 
                            shape.getBounds().getHeight() > 0) {
                        int width = image.getBounds().width;
                        int height = image.getBounds().height;
                        AffineTransform transform = 
                            AffineTransform.getTranslateInstance(-width / 2, -height / 2);
                        shape = transform.createTransformedShape(shape);
                        shapeMap.put(icon, shape);
                    }
				}
				return shape;
			} else {
				return delegate.transform(v);
			}
		}
    }
    
    /**
     * a special renderer that can turn outlines on and off
     * in this demo.
     * You won't need this for a real application.
     * Use BasicVertexRenderer instead
     * 
     * @author Tom Nelson
     *
     */
    class DemoRenderer<V,E> extends BasicVertexRenderer<V,E> {
        public void paintIconForVertex(RenderContext<V,E> rc, V v, Layout<V,E> layout) {
        	
            Point2D p = layout.transform(v);
            p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
            float x = (float)p.getX();
            float y = (float)p.getY();

            GraphicsDecorator g = rc.getGraphicsContext();
            boolean outlineImages = false;
            Transformer<V,edu.uci.ics.jung.visualization.graphics.Image> vertexImageFunction = rc.getVertexImageTransformer();
            
            if(vertexImageFunction instanceof DemoVertexImageTransformer) {
                outlineImages = ((DemoVertexImageTransformer)vertexImageFunction).isOutlineImages();
            }
            edu.uci.ics.jung.visualization.graphics.Image icon = vertexImageFunction.transform(v);
            if(icon == null || outlineImages) {
                
                Shape s = AffineTransform.getTranslateInstance(x,y).
                    createTransformedShape(rc.getVertexShapeTransformer().transform(v));
                paintShapeForVertex(rc, v, s);
            }
            if(icon != null) {
            	int xLoc = (int)x - icon.getWidth()/2;
                int yLoc = (int)y - icon.getHeight()/2;
                rc.getGraphicsContext().drawImage(icon, xLoc, yLoc);
            }
        }
    }
    
    /**
	 * a driver for this demo
	 */
    public static void main(String[] args) {
    	Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("SWT VertexImageShaperDemo");
		shell.setLayout(new FillLayout());

        new SWTVertexImageShaperDemo(shell, SWT.NONE);

        shell.open ();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
    }
}
