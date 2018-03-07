/**
 *
 * @author brom
 */

package DCAD;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author brom
 */
public class GObject implements Serializable{
	private Shape s;
	private Color c;
	private int x, y, width, height;
	private UUID m_id;
	// Note that the x and y coordinates are relative to the top left corner of
	// the
	// graphics context in which the object is to be drawn - NOT the top left
	// corner
	// of the GUI window.

	public GObject(Shape s, Color c, int x, int y, int width, int height) {
		this.s = s;
		this.c = c;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void setShape(Shape s) {
		this.s = s;
	}

	public void setColor(Color c) {
		this.c = c;
	}

	public void setCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setDimensions(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Shape getShape() {
		return s;
	}

	public Color getColor() {
		return c;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public UUID getId() {
		return m_id;
	}
	
	public void setId(UUID id) {
		m_id = id;
	}
	
	public String convertToString(){
		return s.toString() + ";" + c + ";" + x + ";" + y + ";" + width + ";" + height;
	}

	public void draw(Graphics g) {
		g.setColor(c);
		int drawX = x, drawY = y, drawWidth = width, drawHeight = height;

		// Convert coordinates and dimensions if objects are not drawn from top
		// left corner to
		// bottom right.
		if (width < 0) {
			drawX = x + width;
			drawWidth = -width;
		}

		if (height < 0) {
			drawY = y + height;
			drawHeight = -height;
		}

		// Use string comparison to allow comparison of shapes even if the
		// objects
		// have different nodes of origin

		if (s.toString().compareTo(Shape.OVAL.toString()) == 0) {
			g.drawOval(drawX, drawY, drawWidth, drawHeight);
		} else if (s.toString().compareTo(Shape.RECTANGLE.toString()) == 0) {
			g.drawRect(drawX, drawY, drawWidth, drawHeight);
		} else if (s.toString().compareTo(Shape.LINE.toString()) == 0) {
			g.drawLine(x, y, x + width, y + height);
		} else if (s.toString().compareTo(Shape.FILLED_RECTANGLE.toString()) == 0) {
			g.fillRect(drawX, drawY, drawWidth, drawHeight);
		} else if (s.toString().compareTo(Shape.FILLED_OVAL.toString()) == 0) {
			g.fillOval(drawX, drawY, drawWidth, drawHeight);
		}
	}
}
