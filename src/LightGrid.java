import java.awt.Color;


public class LightGrid {
    
    private int sizex;
    private int sizey;
    private Color[] colorScheme;
    private int schemeMode;
    
    private boolean changed = false;

    private static int SCHEME_COUNT_DANCE = 7;
    private static int SCHEME_COUNT_CHILL = 3;
    
    private static int SCHEME_COUNT = Math.max(SCHEME_COUNT_DANCE, SCHEME_COUNT_CHILL) - 1;

    public LightGrid(int sizex, int sizey) {
        this.sizex = sizex;
        this.sizey = sizey;
        newScheme();
    }

    private Color hueShift(Color color, float amount) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0] + amount, hsb[1], hsb[2]);
    }
    private Color satShift(Color color, float amount) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], Math.min(hsb[1] + amount, 1), hsb[2]);
    }
    private Color hueAverage(Color color1, Color color2, float hue1amt) {
        float hue2amt = hue1amt;
        float[] hsb1 = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] hsb2 = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
        return Color.getHSBColor(hsb1[0] * hue2amt + hsb2[0] * (1 - hue2amt), hsb1[1], hsb1[2]);
    }
    
    private Color rgbAverage(Color color1, Color color2, float rgb1amt) {
        float rgb2amt = 1 - rgb1amt;
        int red = (int) (color1.getRed() * rgb2amt + color2.getRed() * (1 - rgb2amt));
        int green = (int) (color1.getGreen() * rgb2amt + color2.getGreen() * (1 - rgb2amt));
        int blue = (int) (color1.getBlue() * rgb2amt + color2.getBlue() * (1 - rgb2amt));
        return new Color(red, green, blue);
    }
    
    public Color[][] getLightColors(int mode, double bpm, int beat, double phase, boolean breakdown, double b, double m, double t, int red, int green, int blue) {
        switch (mode) {
        case GUI.LIGHT_MODE_OFF:   return black();
        case GUI.LIGHT_MODE_WHITE: return white();
        case GUI.LIGHT_MODE_FADE:  return gentleRainbowSuperSlow(0, 0, 0);
        case GUI.LIGHT_MODE_SOLID:  return solid(red, green, blue);
        }
        if (beat == 1) {
            if (!changed) {
                newScheme();
                changed = true;
            }
        } else
            changed = false;
        beat = beat % 16;
        if (!breakdown)
            switch (schemeMode % SCHEME_COUNT_DANCE) {
                case 0: return filter(solidColors(bpm, beat, phase));
                case 1: return filter(solidThenFade(bpm, beat, phase));
                case 2: return filter(rotate(bpm, beat, phase));
                case 3: return filter(rotateFade(bpm, beat, phase));
                case 4: return filter(rotateThenHueFadeFade(bpm, beat, phase));
                case 5: return filter(rgbBMT(Math.min(b*2, 1), Math.min(m*2, 1), Math.min(t*2, 1)));
                case 6: return filter(rgbBMTHueShift(Math.min(b*2, 1), Math.min(m*2, 1), Math.min(t*2, 1), beat, phase));
                case 7: return filter(hueBass(Math.min(b*2, 1), Math.min(m*2, 1), Math.min(t*2, 1), beat, phase));
            }
        else
            switch (schemeMode % SCHEME_COUNT_CHILL) {
                case 0: return filter(gentleRainbow(bpm, beat, phase));
                case 1: return filter(gentleRainbowRotateFade(bpm, beat, phase));
                case 2: return filter(gentleRainbowSuperSlow(bpm, beat, phase));
                case 3: return filter(white());
            }
        return null;
    }
    
    public void newScheme()  {
//        colorScheme = ColorScheme.getNewColorScheme();
        schemeMode = (int)(Math.random() * SCHEME_COUNT);
    }
    
    private Color[][] filter(Color[][] colors) {
//        Color[][] result = new Color[colors.length][colors[0].length];
//        for (int x = 0; x < colors.length; x++)
//            for (int y = 0; y < colors[0].length; y++)
//                result[x][y] = satShift(colors[x][y], -.5f);
//        return result;
        return colors;
    }

    private Color[][] solidColors(double bpm, int beat, double phase) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = colorScheme[(x + 4 * y) % 8];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = hueShift(grid[x][y], beat * .25f);
        return grid;
    }

    private Color[][] solidThenFade(double bpm, int beat, double phase) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = colorScheme[(x + 4 * y) % 8];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = hueShift(grid[x][y], (float) (beat * .25f + Math.max(0, phase - .75f)));
        return grid;
    }

    private Color[][] rotate(double bpm, int beat, double phase) {
        if (colorScheme[0].equals(colorScheme[1]) && colorScheme[1].equals(colorScheme[2]) && colorScheme[2].equals(colorScheme[3]))
            newScheme();
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = colorScheme[(x + 4 * y + beat) % 8];
        return grid;
    }

    private Color[][] rotateFade(double bpm, int beat, double phase) {
        if (colorScheme[0].equals(colorScheme[1]) && colorScheme[1].equals(colorScheme[2]) && colorScheme[2].equals(colorScheme[3]))
            newScheme();
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = rgbAverage(colorScheme[(x + 4 * y + beat) % 8], colorScheme[(x + 4 * y + beat + 1) % 8], (float) Math.max(0, phase - .5f) * 2);
        return grid;
    }

    private Color[][] rotateThenHueFadeFade(double bpm, int beat, double phase) {
        if (colorScheme[0].equals(colorScheme[1]) && colorScheme[1].equals(colorScheme[2]) && colorScheme[2].equals(colorScheme[3]))
            newScheme();
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = rgbAverage(colorScheme[(x + 4 * y + beat) % 8], colorScheme[(x + 4 * y + beat + 1) % 8], (float) Math.max(0, phase - .5f) * 2);
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = hueShift(grid[x][y], (float) (beat * .1f + Math.max(0, phase - .9f)));
        return grid;
    }
    
    private Color[][] gentleRainbow(double bpm, int beat, double phase) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = hueShift(colorScheme[(x + 4 * y) % 8], (float)(beat / 16f + phase / 16f));
        return grid;
    }
    
    private Color[][] gentleRainbowRotateFade(double bpm, int beat, double phase) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = hueAverage(colorScheme[(x + 4 * y) % 8], colorScheme[(x + 4 * y + 1) % 8], (float) (Math.max(0, (beat+phase)/16f)));
        return grid;
    }

    private Color[][] gentleRainbowSuperSlow(double bpm, int beat, double phase) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = hueShift(Color.red, (float)(System.currentTimeMillis() % 1000000d) / 50000f);
        return grid;
    }

    private Color[][] white() {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = Color.white;
        return grid;
    }

    private Color[][] black() {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = Color.black;
        return grid;
    }
    
    private Color[][] solid(int r, int g, int b) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = new Color(r, g, b);
        return grid;
    }

    private Color[][] rgbBMT(double b, double m, double t) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = new Color((float)b, (float)m, (float)t);
        return grid;
    }

    private Color[][] rgbBMTHueShift(double b, double m, double t, int beat, double phase) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = new Color((float)b, (float)m, (float)t);
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = satShift(hueShift(grid[x][y], (float)(beat+phase) / 16f), .3f);
        return grid;
    }
    private Color[][] hueBass(double b, double m, double t, int beat, double phase) {
        Color[][] grid = new Color[sizex][sizey];
        for (int x = 0; x < sizex; x++)
            for (int y = 0; y < sizey; y++)
                grid[x][y] = hueShift(Color.red, (float)b);
        return grid;
    }
}
