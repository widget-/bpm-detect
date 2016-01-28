import java.awt.Color;

/**
 * 
 */


/** @author Widget */
public class pgm_ActiveSolid extends ColorProgram {

    /*
     * (non-Javadoc)
     * @see ColorProgram#getColor(Light, ColorScheme)
     */
    @Override
    public Color getColor(Light light, ColorScheme colorScheme) {
        return shiftColor(colorScheme.getPrimaryColor(), beat / 6f, 0, 0);
    }

    /*
     * (non-Javadoc)
     * @see ColorProgram#getProgramName()
     */
    @Override
    public String getProgramName() {
        // TODO Auto-generated method stub
        return "Active: Solid";
    }

    /*
     * (non-Javadoc)
     * @see ColorProgram#getProgramID()
     */
    @Override
    public int getProgramID() {
        // TODO Auto-generated method stub
        return 101;
    }

}
