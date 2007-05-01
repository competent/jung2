package edu.uci.ics.jung.visualization.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.util.LinkedHashSet;

import edu.uci.ics.jung.visualization.awt.graphics.G2DGraphicsContext;
import edu.uci.ics.jung.visualization.event.Event;
import edu.uci.ics.jung.visualization.event.KeyEvent;
import edu.uci.ics.jung.visualization.event.KeyListener;
import edu.uci.ics.jung.visualization.event.MouseEvent;
import edu.uci.ics.jung.visualization.event.MouseListener;
import edu.uci.ics.jung.visualization.event.MouseMotionListener;
import edu.uci.ics.jung.visualization.event.MouseWheelEvent;
import edu.uci.ics.jung.visualization.event.MouseWheelListener;
import edu.uci.ics.jung.visualization.event.ScreenDeviceListener;
import edu.uci.ics.jung.visualization.graphics.GraphicsContext;

/**
 * An AWT implementation of the screen device.
 * @author Jason A Wrang
 *
 */
public class ScreenDevice<C extends Component> implements edu.uci.ics.jung.visualization.ScreenDevice<C> {
	C comp;
	LinkedHashSet<ScreenDeviceListener<C>> screenDeviceListeners = new LinkedHashSet<ScreenDeviceListener<C>>();
	LinkedHashSet<KeyListener> keyListeners = new LinkedHashSet<KeyListener>();
	LinkedHashSet<MouseListener> mouseListeners = new LinkedHashSet<MouseListener>();
	LinkedHashSet<MouseMotionListener> mouseMotionListeners = new LinkedHashSet<MouseMotionListener>();
	LinkedHashSet<MouseWheelListener> mouseWheelListeners = new LinkedHashSet<MouseWheelListener>();
	
	ComponentListener componentListener;
	java.awt.event.KeyListener keyListener;
	java.awt.event.MouseListener mouseListener;
	java.awt.event.MouseMotionListener mouseMotionListener;
	java.awt.event.MouseWheelListener mouseWheelListener;
	
	private void createComponentListener() {
		componentListener = new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				synchronized (screenDeviceListeners) {
					for (ScreenDeviceListener<C> l : screenDeviceListeners) {
						l.screenResized(ScreenDevice.this);
					}
				}
			}
		};
	}
	
//	private static void debugModifiers(String prefix, int modifiers) {
//		System.out.print(prefix+" mouse event modifiers: ");
//		if ( (modifiers & Event.SHIFT_MASK) != 0) System.out.print("SHIFT ");
//		if ( (modifiers & Event.CTRL_MASK) != 0) System.out.print("CTRL ");
//		if ( (modifiers & Event.ALT_MASK) != 0) System.out.print("ALT ");
//		if ( (modifiers & Event.META_MASK) != 0) System.out.print("META ");
//		if ( (modifiers & Event.BUTTON1_MASK) != 0) System.out.print("BUTTON1 ");
//		if ( (modifiers & Event.BUTTON1_DOWN_MASK) != 0) System.out.print("BUTTON1_DOWN ");
//		if ( (modifiers & Event.BUTTON2_MASK) != 0) System.out.print("BUTTON2 ");
//		if ( (modifiers & Event.BUTTON2_DOWN_MASK) != 0) System.out.print("BUTTON2_DOWN ");
//		if ( (modifiers & Event.BUTTON3_MASK) != 0) System.out.print("BUTTON3 ");
//		if ( (modifiers & Event.BUTTON3_DOWN_MASK) != 0) System.out.print("BUTTON3_DOWN ");
//		System.out.println();
//	}
	
	
	private static int getModifiers(java.awt.event.InputEvent event) {
		int modifiers = 0;
		if ( event.isShiftDown() ) modifiers |= Event.SHIFT_MASK;
		if ( event.isControlDown() ) modifiers |= Event.CTRL_MASK;
		if ( event.isAltDown() ) modifiers |= Event.ALT_MASK;
		if ( event.isMetaDown() ) modifiers |= Event.META_MASK;
		
		return modifiers;
	}
	
	private static int getModifiers(java.awt.event.MouseEvent event) {
		int modifiers = getModifiers((java.awt.event.InputEvent)event);
		
		if ( (event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			modifiers |= Event.BUTTON1_MASK;
		}
		if ( (event.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			modifiers |= Event.BUTTON2_MASK;
		}
		if ( (event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			modifiers |= Event.BUTTON3_MASK;
		}
		
		int button = event.getButton();  // returns the button #
		if (button == java.awt.event.MouseEvent.BUTTON1) {
			modifiers |= Event.BUTTON1_MASK;
		} else if (button == java.awt.event.MouseEvent.BUTTON2) {
			modifiers |= Event.BUTTON2_MASK;
		} else if (button == java.awt.event.MouseEvent.BUTTON3) {
			modifiers |= Event.BUTTON3_MASK;
		}
		
		return modifiers;
	}
	
	private static int getExtendedModifiers(java.awt.event.MouseEvent event) {
		int modifiers = 0;
		
		if ( (event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			modifiers |= Event.BUTTON1_DOWN_MASK;
		}
		if ( (event.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			modifiers |= Event.BUTTON2_DOWN_MASK;
		}
		if ( (event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			modifiers |= Event.BUTTON3_DOWN_MASK;
		}
		
		return modifiers;
	}
	
	public static <E extends java.awt.event.MouseEvent> MouseEvent<E> createEvent(E event) {
		int modifiers = getModifiers(event);
		int extendedModifiers = getExtendedModifiers(event);
		
		int button = event.getButton();  // returns the button #
		if (button == java.awt.event.MouseEvent.BUTTON1) {
			button = 1;
		} else if (button == java.awt.event.MouseEvent.BUTTON2) {
			button = 2;
		} else if (button == java.awt.event.MouseEvent.BUTTON3) {
			button = 3;
		} else {
			button = 0;
		}
		
//		debugModifiers("reg:",modifiers);
//		debugModifiers("ext:",extendedModifiers);
		MouseEvent<E> e = new MouseEvent<E>(event, event.getSource(), 
				event.getWhen(), modifiers, extendedModifiers,
				button, event.getX(), event.getY(), event.isPopupTrigger(), event.getClickCount());

		return e;
	}
	
	public static <E extends java.awt.event.MouseWheelEvent> MouseWheelEvent<E> createMouseWheelEvent(E event) {
		MouseEvent<E> evt = ScreenDevice.<E>createEvent(event);

		int scrollType = event.getScrollType();
		if (scrollType == java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL)
			scrollType = Event.WHEEL_UNIT_SCROLL;
		else if (scrollType == java.awt.event.MouseWheelEvent.WHEEL_BLOCK_SCROLL)
			scrollType = Event.WHEEL_BLOCK_SCROLL;
		else scrollType = Event.WHEEL_UNIT_SCROLL;
		
		int scrollAmount = event.getScrollAmount();
		int wheelRotation = event.getWheelRotation();
		
		MouseWheelEvent<E> e = new MouseWheelEvent<E>(evt.getUiEvent(), evt.getSource(), 
				evt.getTime(), evt.getModifiers(), evt.getExtendedModifiers(),
				evt.getButton(), evt.getX(), evt.getY(), evt.isPopupTrigger(), evt.getClickCount(),
				scrollType, scrollAmount, wheelRotation);
		return e;
	}
	
	private void createMouseListener() {
		mouseListener = new java.awt.event.MouseListener() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				synchronized (mouseListener) {
					MouseEvent<java.awt.event.MouseEvent> evt = createEvent(e);
					for (MouseListener<java.awt.event.MouseEvent> l : mouseListeners) {
						if ( evt.isConsumed() ) break;
						l.mouseClicked(evt);
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent e) {
				synchronized (mouseListener) {
					MouseEvent<java.awt.event.MouseEvent> evt = createEvent(e);
					for (MouseListener<java.awt.event.MouseEvent> l : mouseListeners) {
						if ( evt.isConsumed() ) break;
						l.mouseEntered(evt);
					}
				}
			}

			public void mouseExited(java.awt.event.MouseEvent e) {
				synchronized (mouseListener) {
					MouseEvent<java.awt.event.MouseEvent> evt = createEvent(e);
					for (MouseListener<java.awt.event.MouseEvent> l : mouseListeners) {
						if ( evt.isConsumed() ) break;
						l.mouseExited(evt);
					}
				}
			}

			public void mousePressed(java.awt.event.MouseEvent e) {
				synchronized (mouseListener) {
					MouseEvent<java.awt.event.MouseEvent> evt = createEvent(e);
					for (MouseListener<java.awt.event.MouseEvent> l : mouseListeners) {
						if ( evt.isConsumed() ) break;
						l.mousePressed(evt);
					}
				}
			}

			public void mouseReleased(java.awt.event.MouseEvent e) {
				synchronized (mouseListener) {
					MouseEvent<java.awt.event.MouseEvent> evt = createEvent(e);
					for (MouseListener<java.awt.event.MouseEvent> l : mouseListeners) {
						if ( evt.isConsumed() ) break;
						l.mouseReleased(evt);
					}
				}
			}
		};
	}
	
	private void createMouseMotionListener() {
		mouseMotionListener = new java.awt.event.MouseMotionListener() {
			public void mouseDragged(java.awt.event.MouseEvent e) {
				synchronized (mouseMotionListeners) {
					MouseEvent<java.awt.event.MouseEvent> evt = 
						ScreenDevice.<java.awt.event.MouseEvent>createEvent(e);
					for (MouseMotionListener<java.awt.event.MouseEvent> l : mouseMotionListeners) {
						if ( evt.isConsumed() ) break;
						l.mouseDragged(evt);
					}
				}
			}

			public void mouseMoved(java.awt.event.MouseEvent e) {
				synchronized (mouseMotionListeners) {
					MouseEvent<java.awt.event.MouseEvent> evt = 
						ScreenDevice.<java.awt.event.MouseEvent>createEvent(e);
					for (MouseMotionListener<java.awt.event.MouseEvent> l : mouseMotionListeners) {
						if ( evt.isConsumed() ) break;
						l.mouseMoved(evt);
					}
				}
			}
		};
	}
	
	private void createMouseWheelListener() {
		mouseWheelListener = new java.awt.event.MouseWheelListener() {
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
				synchronized (mouseWheelListeners) {
					MouseWheelEvent<java.awt.event.MouseWheelEvent> evt = createMouseWheelEvent(e);
					for (MouseWheelListener<java.awt.event.MouseWheelEvent> l : mouseWheelListeners) {
						if ( evt.isConsumed() ) break;
						l.mouseWheelMoved(evt);
					}
				}
			}
		};
	}
	
	
	public static <E extends java.awt.event.KeyEvent> KeyEvent<E> createEvent(E event) {
		int modifiers = getModifiers(event);
		int extendedModifiers = 0;
		
		char keyChar = event.getKeyChar();  // returns the key char
		KeyEvent<E> e = new KeyEvent<E>(event, event.getSource(), 
				event.getWhen(), modifiers, extendedModifiers,
				keyChar);

		return e;
	}
	
	
	private void createKeyListener() {
		keyListener = new java.awt.event.KeyListener() {
			public void keyPressed(java.awt.event.KeyEvent e) {
				synchronized (keyListeners) {
					KeyEvent<java.awt.event.KeyEvent> evt = 
						ScreenDevice.<java.awt.event.KeyEvent>createEvent(e);
					for (KeyListener<java.awt.event.KeyEvent> l : keyListeners) {
						if ( evt.isConsumed() ) break;
						l.keyPressed(evt);
					}
				}
			}

			public void keyReleased(java.awt.event.KeyEvent e) {
				synchronized (keyListeners) {
					KeyEvent<java.awt.event.KeyEvent> evt = 
						ScreenDevice.<java.awt.event.KeyEvent>createEvent(e);
					for (KeyListener<java.awt.event.KeyEvent> l : keyListeners) {
						if ( evt.isConsumed() ) break;
						l.keyReleased(evt);
					}
				}
			}

			public void keyTyped(java.awt.event.KeyEvent e) {
				synchronized (keyListeners) {
					KeyEvent<java.awt.event.KeyEvent> evt = 
						ScreenDevice.<java.awt.event.KeyEvent>createEvent(e);
					for (KeyListener<java.awt.event.KeyEvent> l : keyListeners) {
						if ( evt.isConsumed() ) break;
						l.keyTyped(evt);
					}
				}
			}
		};
	}
	
	
	public ScreenDevice(C comp) {
		this.comp = comp;
	}
	
	public C getUIComponent() {
		return comp;
	}
	
	public Color getBackground() {
		return comp.getBackground();
	}
	public Rectangle getBounds() {
		return comp.getBounds();
	}
	public Font getFont() {
		return comp.getFont();
	}
	public Color getForeground() {
		return comp.getForeground();
	}
	public Dimension getSize() {
		return comp.getSize();
	}
	public void repaint() {
		comp.repaint();
	}
	public void setBackground(Color c) {
		comp.setBackground(c);
	}
	public void setFont(Font font) {
		comp.setFont(font);
	}
	public void setForeground(Color c) {
		comp.setForeground(c);
	}


	public void addScreenDeviceListener(ScreenDeviceListener<C> l) {
		synchronized (screenDeviceListeners) {
			if ( screenDeviceListeners.isEmpty() ) {
				createComponentListener();
				comp.addComponentListener(componentListener);
			}
			screenDeviceListeners.add(l);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#removeScreenDeviceListener(edu.uci.ics.jung.visualization.event.ScreenDeviceListener)
	 */
	public void removeScreenDeviceListener(ScreenDeviceListener l) {
		synchronized (screenDeviceListeners) {
			screenDeviceListeners.remove(l);
			if ( screenDeviceListeners.isEmpty() ) {
				comp.removeComponentListener(componentListener);
				componentListener = null;
			}
		}
	}

	public ScreenDeviceListener[] getScreenDeviceListeners() {
		synchronized (screenDeviceListeners) {
			return screenDeviceListeners.toArray(new ScreenDeviceListener[screenDeviceListeners.size()]);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#addKeyListener(edu.uci.ics.jung.visualization.event.MouseListener)
	 */
	public void addKeyListener(KeyListener l) {
		synchronized (keyListeners) {
			if ( keyListeners.isEmpty() ) {
				createKeyListener();
				comp.addKeyListener(keyListener);
			}
			keyListeners.add(l);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#removeKeyListener(edu.uci.ics.jung.visualization.event.MouseListener)
	 */
	public void removeKeyListener(KeyListener l) {
		synchronized (keyListeners) {
			keyListeners.remove(l);
			if ( keyListeners.isEmpty() ) {
				comp.removeKeyListener(keyListener);
				keyListener = null;
			}
		}
	}
	
	public KeyListener[] getKeyListeners() {
		synchronized (keyListeners) {
			return keyListeners.toArray(new KeyListener[keyListeners.size()]);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#addMouseListener(edu.uci.ics.jung.visualization.event.MouseListener)
	 */
	public void addMouseListener(MouseListener l) {
		synchronized (mouseListeners) {
			if ( mouseListeners.isEmpty() ) {
				createMouseListener();
				comp.addMouseListener(mouseListener);
			}
			mouseListeners.add(l);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#removeMouseListener(edu.uci.ics.jung.visualization.event.MouseListener)
	 */
	public void removeMouseListener(MouseListener l) {
		synchronized (mouseListeners) {
			mouseListeners.remove(l);
			if ( mouseListeners.isEmpty() ) {
				comp.removeMouseListener(mouseListener);
				mouseListener = null;
			}
		}
	}
	
	public MouseListener[] getMouseListeners() {
		synchronized (mouseListeners) {
			return mouseListeners.toArray(new MouseListener[mouseListeners.size()]);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#addMouseMotionListener(edu.uci.ics.jung.visualization.event.MouseMotionListener)
	 */
	public void addMouseMotionListener(MouseMotionListener l) {
		synchronized (mouseMotionListeners) {
			if ( mouseMotionListeners.isEmpty() ) {
				createMouseMotionListener();
				comp.addMouseMotionListener(mouseMotionListener);
			}
			mouseMotionListeners.add(l);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#removeMouseMotionListener(edu.uci.ics.jung.visualization.event.MouseMotionListener)
	 */
	public void removeMouseMotionListener(MouseMotionListener l) {
		synchronized (mouseMotionListeners) {
			mouseMotionListeners.remove(l);
			if ( mouseMotionListeners.isEmpty() ) {
				comp.removeMouseMotionListener(mouseMotionListener);
				mouseMotionListener = null;
			}
		}
	}
	
	public MouseMotionListener[] getMouseMotionListeners() {
		synchronized (mouseMotionListeners) {
			return mouseMotionListeners.toArray(new MouseMotionListener[mouseMotionListeners.size()]);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#addMouseWheelListener(edu.uci.ics.jung.visualization.event.MouseWheelListener)
	 */
	public void addMouseWheelListener(MouseWheelListener l) {
		synchronized (mouseWheelListeners) {
			if ( mouseWheelListeners.isEmpty() ) {
				createMouseWheelListener();
				comp.addMouseWheelListener(mouseWheelListener);
			}
			mouseWheelListeners.add(l);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.ScreenDevice#removeMouseWheelListener(edu.uci.ics.jung.visualization.event.MouseWheelListener)
	 */
	public void removeMouseWheelListener(MouseWheelListener l) {
		synchronized (mouseWheelListeners) {
			mouseWheelListeners.remove(l);
			if ( mouseWheelListeners.isEmpty() ) {
				comp.removeMouseWheelListener(mouseWheelListener);
				mouseWheelListener = null;
			}
		}
	}
	
	public MouseWheelListener[] getMouseWheelListeners() {
		synchronized (mouseWheelListeners) {
			return mouseWheelListeners.toArray(new MouseWheelListener[mouseWheelListeners.size()]);
		}
	}

	public GraphicsContext getGraphicsContext() {
		return new G2DGraphicsContext( (Graphics2D)comp.getGraphics() );
	}


	
}
