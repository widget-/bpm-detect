import java.awt.Color;
import java.awt.Font;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.util.awt.TextRenderer;

public class GLPanelRenderer implements GLEventListener {

    private SampleHistory sh;
    private TextRenderer  renderer;
    private int           w = 0;
    private int           h = 0;

    private GUI           gui;

    public GLPanelRenderer(SampleHistory sh, GUI gui) {
        System.out.println("GLPanelRenderer: " + Thread.currentThread().getName());
        this.sh = sh;
        this.gui = gui;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        update();
        render(drawable);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_DST_ALPHA);

        gl.glClearColor(0f, 0f, 0f, 1f);
        gl.getGL2().glShadeModel(GLLightingFunc.GL_SMOOTH);
        gl.getGL2().glEnable(GL.GL_LINE_SMOOTH);
        gl.getGL2().glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
        gl.getGL2().glHint(GL2GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
        gl.getGL2().glEnable(GL.GL_MULTISAMPLE);

        renderer = new TextRenderer(new Font("Courier New", Font.BOLD, 36));
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        this.w = drawable.getWidth();
        this.h = drawable.getHeight();
    }

    private void update() {
    }

    private void render(GLAutoDrawable drawable) {

        GL gl = drawable.getGL();
        GL2 gl2 = gl.getGL2();

        gl2.glClear(GL.GL_COLOR_BUFFER_BIT);

        gl2.glBegin(GL2GL3.GL_QUADS);
        {
            gl2.glColor4d(0, 0, 0, 1);
            gl2.glVertex2d(-1, .5);
            gl2.glVertex2d(1, .5);
            gl2.glColor4d(.1, .1, .1, 1);
            gl2.glVertex2d(1, 1);
            gl2.glVertex2d(-1, 1);
        }
        gl2.glEnd();
        gl2.glBegin(GL2GL3.GL_QUADS);
        {
            gl2.glColor4d(0, 0, 0, 1);
            gl2.glVertex2d(-1, -.5);
            gl2.glVertex2d(1, -.5);
            gl2.glColor4d(.1, .1, .1, 1);
            gl2.glVertex2d(1, -1);
            gl2.glVertex2d(-1, -1);
        }
        gl2.glEnd();

        double max = sh.getMaxInArray();
        for (int sampleType = 3; sampleType > 0; sampleType--) {
            float[] samples = sh.getFftSamples(sampleType);

            Color color = null;
            switch (sampleType) {
                case SampleHistory.FFT_BASS:
                    color = Color.RED;
                    break;
                case SampleHistory.FFT_MID:
                    color = Color.GREEN;
                    break;
                case SampleHistory.FFT_TREBLE:
                    color = new Color(0, 127, 255);
                    break;
            }

            for (int i = 0; i < (samples.length - 1); i++) {
                double x1 = (double) i / (double) samples.length - 0.5;
                double x2 = (double) (i + 1) / (double) samples.length - 0.5;
                double y1 = (samples[i] / max);
                double y2 = (samples[i + 1] / max);
                double zero = 0;
                if (sampleType == SampleHistory.FFT_BASS) {

                } else if (sampleType == SampleHistory.FFT_MID) {
                    zero = -0.5;
                    y1 = y1 / 2.0 - 0.5;
                    y2 = y2 / 2.0 - 0.5;
                    if (y1 > 0)
                        y1 = 0;
                    if (y2 > 0)
                        y2 = 0;
                } else if (sampleType == SampleHistory.FFT_TREBLE) {
                    zero = -0.5;
                    y1 = y1 / -2.0 - 0.5;
                    y2 = y2 / -2.0 - 0.5;
                }

                gl2.glBegin(GL2GL3.GL_QUADS);
                {
                    gl2.glColor4d(color.getRed() / 255, color.getGreen() / 255F, color.getBlue() / 255, .3);
                    gl2.glVertex2d(x1 * 1.9 - 0.05, zero);
                    gl2.glColor4d(color.getRed() / 255, color.getGreen() / 255F, color.getBlue() / 255, 1);
                    gl2.glVertex2d(x1 * 1.9 - 0.05, y1);
                    gl2.glVertex2d(x2 * 1.9 - 0.05, y2);
                    gl2.glColor4d(color.getRed() / 255, color.getGreen() / 255F, color.getBlue() / 255, .3);
                    gl2.glVertex2d(x2 * 1.9 - 0.05, zero);
                }
                gl2.glEnd();

            }
            // if (sampleType == SampleHistory.FFT_BASS) {
            float localMax = 0;
            for (int i = 0; i < GUI.sampleRate / 60 * 2; i++)
                // interpolate 2 frames
                if (localMax < samples[samples.length - 1 - i])
                    localMax = samples[samples.length - 1 - i];
            gl2.glBegin(GL.GL_TRIANGLES);
            {
                gl2.glColor4d(color.getRed() / 255, color.getGreen() / 255, color.getBlue() / 255, 1);
                gl2.glVertex2d(0.9, -1);
                gl2.glVertex2d(0.85 + (sampleType * 0.05), (localMax / max) * 2 - 1);
                gl2.glVertex2d(1.0, -1);
            }
            gl2.glEnd();

            // Draw line
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl2.glLineWidth(.3F);
            gl2.glColor4d(1, 1, 1, .2);

            int count = 0;
            Color[] flagColors = new Color[BPMDetect.bflags.size()];
            for (int i = 0; i < BPMDetect.bflags.size(); i++)
                flagColors[i] = Color.getHSBColor((i * 300 / BPMDetect.bflags.size()) / 255F, 1, 1);
            for (Integer e : BPMDetect.bflags) {
                gl2.glColor4d(flagColors[count].getRed() / 255.0, flagColors[count].getGreen() / 255.0, flagColors[count++].getBlue() / 255.0, .5);
                gl2.glBegin(GL.GL_TRIANGLES);
                {
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 1, 1);
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 1, 0.9);
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 0.985, 1);
                }
                gl2.glEnd();
            }
            count = 0;
            flagColors = new Color[BPMDetect.mflags.size()];
            for (int i = 0; i < BPMDetect.mflags.size(); i++)
                flagColors[i] = Color.getHSBColor((i * 300 / BPMDetect.mflags.size()) / 255F, 1, 1);
            for (Integer e : BPMDetect.mflags) {
                gl2.glColor4d(flagColors[count].getRed() / 255.0, flagColors[count].getGreen() / 255.0, flagColors[count++].getBlue() / 255.0, .5);
                gl2.glBegin(GL.GL_TRIANGLES);
                {
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 1, 0);
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 1, -.1);
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 0.985, 0);
                }
                gl2.glEnd();
                gl2.glBegin(GL.GL_LINES);
                {
                }
                gl2.glEnd();
            }
            flagColors = new Color[BPMDetect.tflags.size()];
            for (int i = 0; i < BPMDetect.tflags.size(); i++)
                flagColors[i] = Color.getHSBColor((i * 300 / BPMDetect.tflags.size()) / 255F, 1, 1);
            count = 0;
            for (Integer e : BPMDetect.tflags) {
                gl2.glColor4d(flagColors[count].getRed() / 255.0, flagColors[count].getGreen() / 255.0, flagColors[count++].getBlue() / 255.0, .5);
                gl2.glBegin(GL.GL_TRIANGLES);
                {
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 1, -1);
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 1, -0.9);
                    gl2.glVertex2d((double) e / samples.length * 1.9 - 0.985, -1);
                }
                gl2.glEnd();
            }
            

            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            // weighted average lines
            gl2.glLineWidth(.3F);
            gl2.glColor4d(1, 1, 1, 0.2);
            //System.out.println(FFTer.avgBassLow / max);
            gl2.glBegin(GL.GL_LINES); {
                gl2.glVertex2d(-1, FFTer.avgBassLow / max);
                gl2.glVertex2d(0.9, FFTer.avgBassLow / max);
            } gl2.glEnd();
            gl2.glBegin(GL.GL_LINES); {
                gl2.glVertex2d(-1, FFTer.avgBassHigh / max);
                gl2.glVertex2d(0.9, FFTer.avgBassHigh / max);
            } gl2.glEnd();
            gl2.glColor4d(1, 1, 1, 0.05);
            gl2.glBegin(GL2GL3.GL_QUADS); {
                gl2.glVertex2d(-1, FFTer.avgBassHigh / max);
                gl2.glVertex2d(0.9, FFTer.avgBassHigh / max);
                gl2.glVertex2d(0.9, FFTer.avgBassLow / max);
                gl2.glVertex2d(-1, FFTer.avgBassLow / max);
            } gl2.glEnd();

            String sBPM = "";
            // Draw text
            renderer.beginRendering(drawable.getWidth(), drawable.getHeight());
            renderer.setUseVertexArrays(true);
            renderer.setSmoothing(true);

            
            double bpm = gui.dbpm;
            for (int i = 0; i <= 2; i++) {

                switch (i + 1) {
                    case SampleHistory.FFT_BASS:
                        color = Color.RED;
                        break;
                    case SampleHistory.FFT_MID:
                        color = Color.GREEN;
                        break;
                    case SampleHistory.FFT_TREBLE:
                        color = new Color(0, 127, 255);
                        break;
                }
                sBPM = "BPM: " + String.valueOf(Math.round(BPMDetect.bpm[i] * 10000) / 10000.0);
                if (BPMDetect.confidence[i] > BPMDetect.NUM_FLAGS * 3 / 4)
                    renderer.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, Math.max(.1F, (float) ((BPMDetect.confidence[i] - 50.0) / 50.0)));
                else
                    renderer.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
                renderer.draw(sBPM, 25, 25 + 50 * i);
                renderer.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1F);
                renderer.draw(String.valueOf(Math.max(0, BPMDetect.confidence[i]) + "% Confidence"), 525, 25 + 50 * i);
                renderer.setColor(Color.WHITE);
                renderer.draw("Ramp amount: " + String.valueOf(BPMDetect.rampRequired), 25, 175);
            }
            renderer.setColor(Color.WHITE);
            renderer.draw("BPM: " + Math.round(bpm * 10000) / 10000.0, 25, h - 50);
            renderer.draw("Confidence: " + Math.round(GUI.getBPM().getConfidence() * 10000) / 10000.0, 25, h - 100);
            renderer.draw("Break: " + BPMDetect.isBreakdown(), 25, h - 150);
            renderer.draw("Downbeat: " + gui.downbeat, 25, h - 200);
            renderer.draw("Beat: " + BPMDetect.getBeat(), 25, h - 250);
            renderer.draw("Program: " + GUI.colors.getProgramName(), (int) (w - 25 - renderer.getBounds("Program: " + GUI.colors.getProgramName()).getWidth()), h - 50);
            renderer.endRendering();

            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_DST_ALPHA);
        }

        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl2.glBegin(GL2GL3.GL_QUADS);
        {
            gl2.glColor4d(0, 0, 0, Math.max(0, GUI.LightScale - .3f));
            gl2.glVertex2f(-1, -1);
            gl2.glVertex2f(1, -1);
            gl2.glVertex2f(1, 1);
            gl2.glVertex2f(-1, 1);
        }
        gl2.glEnd();
        // Draw lines, shifted to top of window
        for (Light light : GUI.lights) {
            Color c = GUI.colors.getColorFor(light);
            // begin foreground line (representative of light)
            gl2.glLineWidth((float) ((w + h) / 150d * GUI.LightScale)); // make line width kinda match window size
            gl2.glBegin(GL.GL_LINES);
            {
                gl2.glColor3f(c.getRed() / 255f, c.getBlue() / 255f, c.getGreen() / 255f);
                gl2.glVertex2d(light.getDebug_startX() * GUI.LightScale, light.getDebug_startY() * GUI.LightScale + (1 - GUI.LightScale));
                gl2.glVertex2d(light.getDebug_endX() * GUI.LightScale, light.getDebug_endY() * GUI.LightScale + (1 - GUI.LightScale));
            }
            gl2.glEnd();
        }
        // lines end

    }

}
