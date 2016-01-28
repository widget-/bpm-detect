# BPM Detection and Lights Thing #
### Java 7/8 application which can analyze audio, perform some spectrum analysis and fancy peak analysis, generate color schemes, and decide what the lights on my living room ceiling should be doing all in real-time.

## Demo: https://youtu.be/RZSnrHpecsM
(Also I wrote the song in the demo too: https://soundcloud.com/djwidget/v-alpha)

![screenshot](http://i.imgur.com/6a82RHc.jpg)

This program was designed for the lights I had designed and installed on my ceiling on my old apartment. It was made up of 16 3-foot-long 12V RGB LED strips. Each strip was individually controlled by hooking up an Arduino to a set of 4 TLC5940 chips, with each channel amplified by a transistor and an external 12V power supply. The lights were arranged in a "two diamonds" pattern as pictured roughly 12'x18' in size.

## License
This code is garbage so it's under the MIT license for anyone to attempt to use.

This project makes use of the following 3rd-party libraries:
 - Google Guava (Apache) https://github.com/google/guava
 - JTransforms (BSD) https://github.com/wendykierp/JTransforms
 - RXTX Java Fork (GPL) http://fizzed.com/oss/rxtx-for-java
 - Jogamp JOGL (BSD) http://jogamp.org/jogl/www/

## Building
It's been awhile, just load it into Eclipse and see what happens or use one of the binaries.

## Running
I think all the libraries are packaged in. `java -jar bpm9_1_128000--2015-04-17.jar` should run it.

Make sure your default recording device is a stereo input such as Stereo Mix or another loopback device.

Or you could just watch the Youtube video since it covers most of the lighting programs.

## Documentation
There's hardly any, so I'll outline the files:

### Main
 - GUI.java
	 - Provides the `main()` entry point, which creates a `new GUI()`. The GUI is responsible for coordinating everything.
	 - The GUI sets up an audio buffer, and then proceeds to make a Swing GUI.
	 - In the Swing GUI it adds a GLCanvas, which is the main graphical output of the program.
	 - It starts a SerialWrite, which sends the Arduino the data for which ports should be which colors.
	 - Finally, it sets up some timers to re-analyze the audio buffer for tempo changes.
### IO / buffering
 - AudioCapture.java
	 - Soundcard stuff
 - SampleHistory.java
	 - Some float arrays for each band as well as convenience functions for adding samples and kicking old ones out
 - SerialWriter.java / SerialCache.java
	 - Sends data to the Arduino as fast as it can. The cache checks if a light's state hasn't changed before sending it since the TLC5940 will remember its last state.
 - FFTer.java
	 - Runs FFTs on the audio before it gets put into the SampleHistory, since we don't use raw amplitude for anything.
### UI
 - ColoredScrollbar.java
	 - The fancy scrollbars on the side, used when setting the lights to "solid" since our living room didn't have normal ceiling lights and was pretty dark. Since the LEDs had a pretty strong green tint, I added sliders to change the color of them back to soft white manually, but I didn't want ugly scrollbars...
 - WaveformPanel
	 - Not used any more, but was the original visual output, based on a swing Panel and super slow.
 - GLPanelRenderer
	 - I wanted to learn OpenGL while I was doing this, so I wrote a loop to output the FFT sample buffer to OpenGL. It also has a sort of peakmeter on the side.
	 - It displays a live version of what the ceiling lights were doing, in case you didn't want to stare at them directly.
	 - In addition, it has a small amount of debugging information related to the audio analysis, mostly in the form of confusing numbers and confidences that never make any sense even when it works.
	 - It looked pretty badass on our projector.
### IRL lights
 - Light.java
	 - Base light class, contains x and y midpoints used for calculations, plus x and y start- and end-points for showing on the screen.
 - LightCollection.java
	 - Where all the lights' physical locations on the ceiling were recorded, plus the ports that happened to be hooked up at the time.
 - ColorScheme.java
	 - Generated color schemes, using the 6 primaries/secondaries plus cyan.
 - ColorProgram.java
	 - Base class for the various color programs, including useful functions like color shifts.
 - ColorPrograms.java
	 - The thing in charge of the various lighting programs, which were split into:
		 - Active:
			 - Used the detected tempo to create intense patterns.
			 - Since the tempo and a downbeat were known, the lights were generally lag-free since they could predict the next 50ms or so required to not have that "slightly off" feeling.
		 - Responsive:
			 - Used realtime information about the audio, e.g. frequency band amplitudes.
		 - Passive:
			 - Slower time-based patterns, generally used if it detected a breakdown or otherwise passive moment in the song.
	 - Programs were written with the position of the light, its index in the list, beat of the song relative to the downbeat, and phase within a beat (how close it was to the next beat.)
 - LightGrid.java
	 - Deprecated, the old way of deciding what the lights should do.
### Analysis
 - BPMDetect.java
	 - This is pretty confusing and I probably should have documented what I was doing as I wrote it, but here's the vague steps:
	 - Each [interval] ms and for each frequency band (bass/mid/treble):
		 - Go through once and detect all peaks in the band
			 - Filter out noisy signals, don't allow two peaks to happen too quickly.
			 - Peaks are marked with flags in the UI
		 - For each pair of peaks in the band, divide or multiply the time between them by 2 until it's between 60bpm and 180bpm.
		 - Add all these time distances to a big list.
		 - Sort the big list into smaller piles.
		 - Keep the biggest pile.
		 - That's our estimate for this band, so add it to the BPMBestGuess HashMap.
		 - Also, if there's two or so beats of silence followed by a beat, that's our new downbeat until we find another one.
 - BPMBestGuess.java
	 - This one keeps a bunch of individual tempo guesses.
	 - Each guess will slowly decay, and guesses below a threshold are culled.
	 - Generate a confidence rating for each set of guesses.
	 - Highest confidence wins! That's our tempo.

## Programs
### Active
**Solid**: Change colors on the beat with all lights the same color.
**Pulse**: Same as solid but fade immediately after the beat, making it "pulse" different colors.
**Fade**: Close to solid but before the end of each beat smoothly fade into the next color.
**Spin**: Slowly rotates colors in a gradient but also rotates another 60º or so on each beat.
**Chase1**: Only one light will light up at a time, snaking around the lights such that a full loop occurs every two beats.
**Chase2**: Same as Chase1 but the rest of the lights will be dimly lit another color.
**Explode**: "Explodes" from the center diamond to the outer diamond each beat.
**Diamond**: Inner and outer diamonds are two colors, each beat they change like Solid but the outer one will gain the inner one's color.
**FromPt**: (From Point) Each beat, light starts from a point on the graph and expands outward from there.
**FromPt2** Same as FromPt but doesn't reset to black at the beginning of each beat.
###Responsive
**Across**: Top half of the lights are bass, one length on the other side is mid, and the other is treble.
**Along**: Inner diamond is bass, outer diamond is treble, and the other ones are mid.
###Passive
**DFade**: Diagonal slow fade.
**HFade**: Horizontal slow fade.
**FlatFade**: No gradient, slowly fade as one color.
###Other modes
**Solid**: Manually pick a color with the sliders
**White**: When lazy
**Off**: Off
**Fade**: Chill super-slow fade.
**Mosaic**: This was supposed to be similar to theater lighting where you have multiple colored lights that all average out to white (which really adds vividness without being obvious how), but I never got the algorithm right and every now and then they all sync up at some color which is kinda cool to experience in its own right.
**Channel**: Turns only one Arduino channel on: for figuring out where we plugged all the lights into.

### Hardware
The Java program communicates to an Arduino via USB Serial.

The Arduino loops, taking two numbers, a port and a value, and directly outputs them to the TLC5940 via SPI.

The TLC5940 has 16 outputs, each of those is hooked up to an external 12V power source through a transistor. All these were mounted on custom-printed boards that I also designed.

Front: ![TLC5940 Front](http://i.imgur.com/Am3Ww1X.png)
Back: ![TLC5940 Back](http://i.imgur.com/QsoLoDL.png)

To save space, I didn't label the boards' inputs and outputs on the far left/right edges, but from top to bottom they are:
 - **SCLK**: Signal Clock
 - **SIN/SOUT**: Signal In/Out
 - **BLANK**: Reset all the outputs (when turning on usually)
 - **XLAT**: Dunno
 - **GSCLK**: Dunno
 - **+5V**: Signal level voltage
 - **+12V**: Light level voltage
 - **GND**: Ground

In theory 48 of these boards could be daisy chained but I used 4 and the signal was already pretty noisy at the end.