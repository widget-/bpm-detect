import java.awt.Color;
import java.awt.Point;
import javafx.geometry.Point2D;


public class ColorPrograms {

    public static enum Program {
        ACTIVE_SOLID,
        ACTIVE_PULSE,
        ACTIVE_FADE,
        ACTIVE_SPIN,
        ACTIVE_CHASE1,
        ACTIVE_CHASE2,
        ACTIVE_EXPLODE,
        ACTIVE_DIAMOND,
        ACTIVE_FROMPT,
        ACTIVE_FROMPT2,
        PASSIVE_DFADE,
        PASSIVE_HFADE,
        PASSIVE_FLATFADE,
        PASSIVE_FMOSAIC,
        RESPONSE_ACROSS,
        RESPONSE_ALONG
    }


    private ColorScheme      colorScheme                 = new ColorScheme();
    private Program          currentProgram              = Program.PASSIVE_DFADE;

    private boolean          specificProgram             = false;

    private static boolean   wasBreakdown                = false;

    private int              beat                        = 0;
    private int              lastBeat                    = 0;
    private double           phase                       = 0;

    private double           bass                        = 0;
    private double           mid                         = 0;
    private double           treb                        = 0;

    private int              r                           = 0, g = 0, b = 0;

    // private List<Color> colorCache = new ArrayList<Color>();
    private double[]         cachedBMT                   = { 0f, 0f, 0f };

    private static Program[] availableActivePrograms     = {
            Program.ACTIVE_SOLID,
            Program.ACTIVE_PULSE,
            Program.ACTIVE_FADE,
            Program.ACTIVE_SPIN,
            Program.ACTIVE_CHASE1,
            Program.ACTIVE_CHASE2,
            Program.ACTIVE_EXPLODE,
            Program.ACTIVE_DIAMOND,
            Program.ACTIVE_FROMPT,
            Program.ACTIVE_FROMPT2                      };
    private static Program[] availableFadePrograms       = { Program.PASSIVE_DFADE, Program.PASSIVE_HFADE, Program.PASSIVE_FLATFADE, Program.PASSIVE_FMOSAIC };
    private static Program[] availableResponsivePrograms = { Program.RESPONSE_ACROSS, Program.RESPONSE_ALONG };


    public ColorPrograms() {
    }

    public String getProgramName() {
        return currentProgram.name();
    }

    public Color getColorFor(Light light) {
        // /*!DEBUG*/currentProgram =RESPONSE_ACROSS;/*!DEBUG*/
        Color c = Color.black;
        if (GUI.lightMode == GUI.LIGHT_MODE_FADE)
            c = program_DiagonalFades(light, 20000);
        else if (GUI.lightMode == GUI.LIGHT_MODE_SOLID)
            c = GUI.colors.getStaticColor();
        else if (GUI.lightMode == GUI.LIGHT_MODE_WHITE)
            c = Color.white;
        else if (GUI.lightMode == GUI.LIGHT_MODE_OFF)
            c = Color.black;
        else if (GUI.lightMode == GUI.LIGHT_MODE_MOSAIC)
            c = (program_Mosaic(light));
        else if (GUI.lightMode == GUI.LIGHT_MODE_CHANNEL)
            c = program_Channel(light);
        else if (GUI.lightMode == GUI.LIGHT_MODE_AUTO) {

            switch (currentProgram) {
                case ACTIVE_SOLID:
                    c = program_ActiveSolid(light);
                    break;
                case ACTIVE_PULSE:
                    c = program_ActivePulse(light);
                    break;
                case ACTIVE_FADE:
                    c = program_ActiveFade(light);
                    break;
                case ACTIVE_SPIN:
                    c = program_ActiveSpin(light);
                    break;
                case ACTIVE_CHASE1:
                    c = program_ActiveChase1(light);
                    break;
                case ACTIVE_CHASE2:
                    c = program_ActiveChase2(light);
                    break;
                case ACTIVE_EXPLODE:
                    c = program_ActiveExplode(light);
                    break;
                case ACTIVE_DIAMOND:
                    c = program_ActiveDiamond(light);
                    break;
                case ACTIVE_FROMPT:
                    c = program_ActiveFromPoint(light);
                    break;
                case ACTIVE_FROMPT2:
                    c = program_ActiveFromPoint2(light);
                    break;

                case PASSIVE_DFADE:
                    c = program_DiagonalFades(light, 5000);
                    break;
                case PASSIVE_HFADE:
                    c = program_HorizontalFades(light, 5000);
                    break;
                case PASSIVE_FLATFADE:
                    c = program_FlatFades(light, 5000);
                    break;
                case PASSIVE_FMOSAIC:
                    c = program_MosaicFast(light);
                    break;

                case RESPONSE_ACROSS:
                    c = program_ResponsiveAcross(light);
                    break;
                case RESPONSE_ALONG:
                    c = program_ResponsiveAlong(light);
                    break;
            }

        }

        return c;
    }

    public Color getStaticColor() {
        return new Color(r / 255f, g / 255f, b / 255f);
    }

    public void updateParameters(int beat, double phase, double bass, double mid, double treb) {
        lastBeat = this.beat;
        this.beat = beat;
        this.phase = phase;
        this.bass = bass;
        this.mid = mid;
        this.treb = treb;
        
        boolean isBreakdown = BPMDetect.isBreakdown(); 
        
        if ((lastBeat - 16) > this.beat)
            changeProgram(isBreakdown);
        if (wasBreakdown != isBreakdown && this.beat % 4 == 0) {
            changeProgram(isBreakdown);
            wasBreakdown = isBreakdown;
        }
    }

    public void changeProgram(boolean breakdown) {
        colorScheme.generateColorScheme();
        if (specificProgram)
            return;
        boolean responsive = (Math.random() < 0.35);
        if (breakdown) {
            if (responsive)
                currentProgram = availableResponsivePrograms[(int) (Math.random() * availableResponsivePrograms.length)];
            else
                currentProgram = availableFadePrograms[(int) (Math.random() * availableFadePrograms.length)];
        } else {
            if (responsive)
                currentProgram = availableResponsivePrograms[(int) (Math.random() * availableResponsivePrograms.length)];
            else
                currentProgram = availableActivePrograms[(int) (Math.random() * availableActivePrograms.length)];
        }
    }

    public void changeToProgram(boolean specified, Program program) {
        if (specified) {
            currentProgram = program;
            specificProgram = true;
        } else {
            specificProgram = false;
        }
    }

    public void updateStaticColors(int r, int g, int b) {
        // please someone explain why i have to flip g and b for this to be right
        this.r = r;
        this.g = g;
        this.b = b;
    }

    private Color program_ActiveSolid(Light light) {
        return shiftColor(colorScheme.getPrimaryColor(), beat / 6f, 0, 0);
    }

    private Color program_ActivePulse(Light light) {
        return shiftColor(colorScheme.getPrimaryColor(), beat / 6f, 0, (float) (-phase));
    }

    private Color program_ActiveFade(Light light) {
        return shiftColor(colorScheme.getPrimaryColor(), (beat / 6f) + ((float) Math.max(0, (phase - 0.66) * 3) / 6f), 0, 0);
    }

    private Color program_ActiveSpin(Light light) {
        return shiftColor(colorScheme.getPrimaryColor(), (beat / 4f) + (phase / 8f) + (((light.getUniqueID() + 4) % 8) + 4) % 8 / 16f, 0, 0);
        // return Color.black;
    }

    private Color program_ActiveChase1(Light light) {
        if (Math.abs(light.getUniqueID() - ((int) ((beat + phase) * 8) % 16)) < 2)
            return shiftColor(colorScheme.getPrimaryColor(), beat / 6f, 0, 0);
        else
            return shiftColor(colorScheme.getSecondaryColor(), beat / 6f, 0, -.8f);
    }

    private Color program_ActiveChase2(Light light) {
        if (Math.abs(light.getUniqueID() - ((int) ((beat + phase) * 8) % 16)) < 2)
            return shiftColor(colorScheme.getPrimaryColor(), beat / 6f, 0, 0);
        else
            return Color.black;
    }

    private Color program_ActiveDiamond(Light light) {
        Color c = shiftColor(colorScheme.getPrimaryColor(), beat / 6f, 0, 0);
        if (Math.abs(light.getX()) > 0.4) // outside
            return shiftColor(c, -1f / 6f, 0, 0);
        else if ((Math.abs(light.getX()) < 0.4) && (Math.abs(light.getY()) < 0.5)) // inside
            return shiftColor(c, 0, 0, 0);
        // else if (Math.abs(light.getX()) < 0.4 && Math.abs(light.getY()) >= 0.5) //top/bot
        // return shiftColor(c, 0, 0, (phase > speed) ? 0:-1);

        return Color.black;
    }

    private Color program_ActiveExplode(Light light) {
        Color c = shiftColor(colorScheme.getPrimaryColor(), beat / 6f, 0, 0);
        final float speed = 0.15f;

        if (Math.abs(light.getX()) > 0.4)
            return shiftColor(c, 0, 0, (phase >= (speed * 2)) ? 0 : -1);
        else if ((Math.abs(light.getX()) < 0.4) && (Math.abs(light.getY()) < 0.5))
            return shiftColor(c, 0, 0, (phase < speed) ? 0 : -1);
        else if ((Math.abs(light.getX()) < 0.4) && (Math.abs(light.getY()) >= 0.5))
            return shiftColor(c, 0, 0, ((phase < (speed * 2)) && (phase >= speed)) ? 0 : -1);

        return Color.black;
    }

    private Color program_ActiveFromPoint(Light light) {
        double theta = ((beat * 57.32) - Math.floor(beat * 42.89)) * Math.PI * 2;
        Point2D p = new Point2D(Math.sin(theta), Math.cos(theta));
        Point2D l = new Point2D(light.getX(), light.getY());

        return shiftColor(colorScheme.getPrimaryColor(), beat * .55, 0, -0.5 + 2 * ((phase * 2) - p.distance(l)));
    }

    private Color program_ActiveFromPoint2(Light light) {
        double theta = (beat * 52.841 + 12.8) % 41.91;
        Point2D p = new Point2D(Math.sin(theta), Math.cos(theta));
        Point2D l = new Point2D(light.getX(), light.getY());

        Color newColor = shiftColor(colorScheme.getPrimaryColor(), (beat + 1) * .55, 0, 1);
        Color oldColor = shiftColor(colorScheme.getPrimaryColor(), (beat) * .55, 0, 1 - phase);


        return averageColors(oldColor, newColor, (p.distance(l) - phase * p.distance(l)) * 2 - 1);
    }

    private Color program_DiagonalFades(Light light, int speed) {
        // in this case a higher speed value = slower lights
        float time = (System.currentTimeMillis() % speed) / (float) speed;
        return new Color(Color.HSBtoRGB(((float) (light.getX() + light.getY()) / 8f) + time, .5f, 1f));
    }

    private Color program_HorizontalFades(Light light, int speed) {
        // in this case a higher speed value = slower lights
        float time = (System.currentTimeMillis() % speed) / (float) speed;
        return new Color(Color.HSBtoRGB(((float) (light.getX()) / 4f) + time, .5f, 1f));
    }

    private Color program_FlatFades(Light light, int speed) {
        // in this case a higher speed value = slower lights
        float time = (System.currentTimeMillis() % speed) / (float) speed;
        return new Color(Color.HSBtoRGB(time, .5f, 1f));
    }

    private Color program_ResponsiveAcross(Light light) {
        if (light.getUniqueID() == 0) {
            if (cachedBMT[0] > bass)
                bass = Math.max(cachedBMT[0] - 0.1, bass);
            cachedBMT[0] = bass;
            if (cachedBMT[1] > mid)
                mid = Math.max(cachedBMT[1] - 0.1, mid);
            cachedBMT[1] = mid;
            if (cachedBMT[2] > treb)
                treb = Math.max(cachedBMT[2] - 0.1, treb);
            cachedBMT[2] = treb;
        }

        if (Math.abs(light.getX()) > 0.4)
            return shiftColor(colorScheme.getPrimaryColor(), 0, 0, (float) (bass - 1));
        else if ((Math.abs(light.getX()) < 0.4) && (Math.abs(light.getY()) < 0.5))
            return shiftColor(colorScheme.getSecondaryColor(), 0, 0, (float) (mid - 1));
        else if ((Math.abs(light.getX()) < 0.4) && (Math.abs(light.getY()) >= 0.5))
            return shiftColor(colorScheme.getTertiaryColor(), 0, 0, (float) (treb - 1));

        // failsafe
        return Color.black;
    }

    private Color program_ResponsiveAlong(Light light) {
        if (light.getUniqueID() == 0) {
            if (cachedBMT[0] > bass)
                bass = Math.max(cachedBMT[0] - 0.03, bass);
            cachedBMT[0] = bass;
            if (cachedBMT[1] > mid)
                mid = Math.max(cachedBMT[1] - 0.02, mid);
            cachedBMT[1] = mid;
            if (cachedBMT[2] > treb)
                treb = Math.max(cachedBMT[2] - 0.02, treb);
            cachedBMT[2] = treb;
        }

        if (light.getIndex() < 4) {
            return shiftColor(colorScheme.getPrimaryColor(), 0, 0, -1 + Math.max(0, ((float) bass * 4) - light.getIndex()));
        }
        if ((light.getUniqueID() >= 3) && (light.getUniqueID() < 8)) {
            return shiftColor(colorScheme.getSecondaryColor(), 0, 0, -1 + Math.max(0, (((float) mid * 4) - 8) + light.getUniqueID()));
        }
        if ((light.getUniqueID() >= 12) && (light.getUniqueID() < 16)) {
            return shiftColor(colorScheme.getTertiaryColor(), 0, 0, -1 + Math.max(0, (((float) treb * 4) - 16) + light.getUniqueID()));
        }

        return Color.black;
    }

    private Color program_Channel(Light light) {
        if (GUI.channel == light.portRed)
            return Color.red;
        if (GUI.channel == light.portGreen)
            return Color.green;
        if (GUI.channel == light.portBlue)
            return Color.blue;
        return Color.black;
    }

    private Color program_Mosaic(Light light) {
        float h = (float) Math.sin(((light.getUniqueID() + 1) * System.nanoTime()) / 500000000000d);
        return scaleRGB(new Color(Color.HSBtoRGB(h, .5f, 1)));
    }

    private Color program_MosaicFast(Light light) {
        float h = (float) Math.sin(((light.getUniqueID() % 3.21 + 1) * System.nanoTime()) / 10000000000d);
        return new Color(Color.HSBtoRGB(h, .5f, 1));
    }

    private Color shiftColor(Color color, double h, double s, double b) {
        return shiftColor(color, (float) h, (float) s, (float) b);
    }

    private Color shiftColor(Color color, float h, float s, float b) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[0] += h;
        hsb[1] = Math.min(Math.max(hsb[1] + s, 0f), 1f);
        hsb[2] = Math.min(Math.max(hsb[2] + b, 0f), 1f);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    private Color averageColors(Color c1, Color c2, double mix) {
        float[] rgb = new float[3];
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];
        c1.getColorComponents(rgb1);
        c2.getColorComponents(rgb2);
        rgb[0] = (float) Math.min(Math.max((rgb1[0] * mix + rgb2[0] * (1 - mix)), 0), 1);
        rgb[1] = (float) Math.min(Math.max((rgb1[1] * mix + rgb2[1] * (1 - mix)), 0), 1);
        rgb[2] = (float) Math.min(Math.max((rgb1[2] * mix + rgb2[2] * (1 - mix)), 0), 1);
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    private Color averageColorsGeometric(Color c1, Color c2, double mix) {
        float[] rgb = new float[3];
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];
        c1.getColorComponents(rgb1);
        c2.getColorComponents(rgb2);
        rgb[0] = (float) Math.sqrt(rgb1[0] * mix * rgb2[0] * (1 - mix));
        rgb[1] = (float) Math.sqrt(rgb1[1] * mix * rgb2[1] * (1 - mix));
        rgb[2] = (float) Math.sqrt(rgb1[2] * mix * rgb2[2] * (1 - mix));
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    private Color scaleRGB(Color color) {
        float[] rgb = new float[3];
        color.getColorComponents(rgb);
        rgb[0] = (rgb[0] * r) / 255f;
        rgb[1] = (rgb[1] * g) / 255f;
        rgb[2] = (rgb[2] * b) / 255f;
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

}
