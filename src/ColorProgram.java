import java.awt.Color;


/**
 * 
 */

/** @author Widget */
public abstract class ColorProgram {

    protected int    beat;
    protected double phase;
    protected double bass, mid, treb;

    protected int    programID;
    protected String programName;

    private int      r, g, b;


    public ColorProgram() {

    }

    public abstract Color getColor(Light light, ColorScheme colorScheme);

    public abstract String getProgramName();
    public abstract int getProgramID();

    public void updateParameters(int beat, double phase, double bass, double mid, double treb) {
        this.beat = beat;
        this.phase = phase;
        this.bass = bass;
        this.mid = mid;
        this.treb = treb;
    }

    public void updateStaticColors(int r, int g, int b) {
        // please someone explain why i have to flip g and b for this to be right
        this.r = r;
        this.g = b;
        this.b = g;
    }


    protected Color shiftColor(Color color, float h, float s, float b) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[0] += h;
        hsb[1] = Math.min(Math.max(hsb[1] + s, 0f), 1f);
        hsb[2] = Math.min(Math.max(hsb[2] + b, 0f), 1f);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    protected Color scaleRGB(Color color) {
        float[] rgb = new float[3];
        color.getColorComponents(rgb);
        rgb[0] = (rgb[0] * r) / 255f;
        rgb[1] = (rgb[1] * g) / 255f;
        rgb[2] = (rgb[2] * b) / 255f;
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public Color getStaticColor() {
        return new Color(r / 255f, g / 255f, b / 255f);
    }
}
