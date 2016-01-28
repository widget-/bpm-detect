import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class ColoredScrollbar extends BasicScrollBarUI {

	Color color = Color.WHITE;
	boolean inverted = false;
	
	private JButton b = new JButton() {
		// to get rid of warnings
		private static final long serialVersionUID = 1L;

		@Override
        public Dimension getPreferredSize() {
            return new Dimension(0, 0);
        }

    };

	public ColoredScrollbar() {
		super();
	}

	public ColoredScrollbar(Color color, boolean inverted) {
		super();
		this.color = color;
		this.inverted = inverted;
	}
	

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
		if (scrollbar.isEnabled())
			g.setColor(color.darker().darker().darker().darker());
		else
			g.setColor(color.darker().darker().darker().darker().darker().darker().darker());
		g.fillRect(r.x, r.y, r.width, r.height);
	}

	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
		double pos = (double)scrollbar.getValue() / (double)scrollbar.getMaximum();
		int red, grn, blu;
		if (!inverted) {
			red = (int)( pos * color.getRed() + (1 - pos) * Color.GRAY.getRed());
			grn = (int)(pos * color.getGreen() + (1 - pos) * Color.GRAY.getGreen());
			blu = (int)(pos * color.getBlue() + (1 - pos) * Color.GRAY.getBlue());
		} else {
			red = (int)((1 - pos) * color.getRed() + pos * Color.GRAY.getRed());
			grn = (int)((1 - pos) * color.getGreen() + pos * Color.GRAY.getGreen());
			blu = (int)((1 - pos) * color.getBlue() + pos * Color.GRAY.getBlue());
		}
		g.setColor(new Color(red, grn, blu));
		g.fillRoundRect(r.x, r.y, r.width - 1, r.height-1, 5, 5);
		if (scrollbar.isEnabled())
			g.setColor(Color.WHITE);
		else
			g.setColor(Color.BLACK);
		g.drawRoundRect(r.x, r.y, r.width - 1, r.height-1, 5, 5);
	}
    @Override
    protected JButton createDecreaseButton(int orientation) {
        return b;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return b;
    }
	
}
