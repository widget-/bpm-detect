import java.util.ArrayList;


public class LightCollection extends ArrayList<Light> {

    private static final long serialVersionUID = 1L;

    public LightCollection() {

    }

    public Light addLight(int uniqueID, int index, double sx, double ex, double sy, double ey, int next, int prev) {
        Light light = new Light(uniqueID, index, sx, ex, sy, ey, next, prev);
        this.add(light);
        return light;
    }

    public boolean removeLightByUID(int uniqueID) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getUniqueID() == uniqueID) {
                this.remove(i);
                return true;
            }
        }
        return false;
    }

    public Light getLightByUID(int uniqueID) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getUniqueID() == uniqueID) {
                return this.get(i);
            }
        }
        return null;
    }

    public static LightCollection initializeLivingRoomLights() {
        /** DIAGRAM 
        
        /  \  |  /  \         y=1
      1/   2\ | /10  \9
              | 
     /      / | \      \
   0/    11/  |  \3     \8
--------------+---------------- 0
    \      \  |  /      /
   15\     4\ | /12    /7
              |
       \    / | \    /
      14\  /13| 5\  /6         -1
               
x=   -1         0          1              
      UID is listed next to light.
      Index is UID for 0 to 7, or (UID - 8) for 8 to 15.
      
                                                         */
        LightCollection lights = new LightCollection();

        lights.addLight(0, 0, -1, -2d / 3d, 0, .5d, 1, 15);
        lights.addLight(1, 1, -2d / 3d, -1d / 3d, .5d, 1, 2, 0);
        lights.addLight(2, 2, -1d / 3d, 0, 1, .5d, 3, 1);
        lights.addLight(3, 3, 0, 1d / 3d, .5d, 0, 4, 2);
        lights.addLight(4, 4, -1d / 3d, 0, 0, -.5d, 5, 3);
        lights.addLight(5, 5, 0, 1 / 3d, -.5d, -1, 6, 4);
        lights.addLight(6, 6, 1d / 3d, 2d / 3d, -1, -.5d, 7, 5);
        lights.addLight(7, 7, 2 / 3d, 1, -.5d, 0, 8, 6);

        lights.addLight(8, 0, 1, 2d / 3d, 0, .5d, 9, 7);
        lights.addLight(9, 1, 2d / 3d, 1d / 3d, .5d, 1, 2, 0);
        lights.addLight(10, 2, 1d / 3d, 0, 1, .5d, 3, 1);
        lights.addLight(11, 3, 0, -1d / 3d, .5d, 0, 4, 2);
        lights.addLight(12, 4, 1d / 3d, 0, 0, -.5d, 5, 3);
        lights.addLight(13, 5, 0, -1 / 3d, -.5d, -1, 6, 4);
        lights.addLight(14, 6, -1d / 3d, -2d / 3d, -1, -.5d, 7, 5);
        lights.addLight(15, 7, -2 / 3d, -1, -.5d, 0, 8, 6);

        lights.get(0).setPorts(99, 99, 99);
        lights.get(1).setPorts(39, 40, 38);
        lights.get(2).setPorts(42, 43, 41);
        lights.get(3).setPorts(52, 53, 51);
        lights.get(4).setPorts(4, 5, 3);
        lights.get(5).setPorts(26, 27, 25);
        lights.get(6).setPorts(23, 24, 22);
        lights.get(7).setPorts(99, 99, 99);

        lights.get(8).setPorts(99, 99, 99);
        lights.get(9).setPorts(36, 37, 35);
        lights.get(10).setPorts(58, 59, 57);
        lights.get(11).setPorts(33, 34, 32);
        lights.get(12).setPorts(10, 11, 9);
        lights.get(13).setPorts(7, 8, 6);
        lights.get(14).setPorts(1, 2, 0);
        lights.get(15).setPorts(99, 99, 99);


        return lights;
    }
}
