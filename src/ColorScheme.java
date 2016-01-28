import java.awt.Color;


public class ColorScheme {
    
    private static Color[] PRIMARY_COLORS = {Color.red, Color.orange, Color.yellow, Color.green,
                                             Color.cyan, Color.blue, Color.getHSBColor(270, 1f, 1f),
                                             Color.magenta};
    private Color primary = Color.WHITE;
    private Color secondary = Color.BLUE;
    private Color tertiary = Color.BLACK;

    public ColorScheme() {
    	generateColorScheme();
    }
    
    public Color getPrimaryColor() {
    	return primary;
    }
    
    public Color getSecondaryColor() {
    	return secondary;
    }
    
    public Color getTertiaryColor() {
    	return tertiary;
    }
    
    public void generateColorScheme() {
    	int primaryIndex = (int)(Math.random() * PRIMARY_COLORS.length);
    	int secondaryIndex = (int)(Math.random() * PRIMARY_COLORS.length);;
    	while (primaryIndex == secondaryIndex) // prevent duplicate colors
    		secondaryIndex = (int)(Math.random() * PRIMARY_COLORS.length);
    	if (Math.random() > 0.5)
    		tertiary = Color.BLACK;
    	else {
    		int tertiaryIndex = (int)(Math.random() * PRIMARY_COLORS.length);
        	while (tertiaryIndex == primaryIndex || tertiaryIndex == secondaryIndex) // prevent duplicate colors
        		tertiaryIndex = (int)(Math.random() * PRIMARY_COLORS.length);
        	tertiary = PRIMARY_COLORS[tertiaryIndex];
    	}
    	primary = PRIMARY_COLORS[primaryIndex];
    	secondary = PRIMARY_COLORS[secondaryIndex];
    }

}
