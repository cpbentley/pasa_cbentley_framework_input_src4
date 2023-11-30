package pasa.cbentley.framework.input.src4.gesture;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Gesture modifies a X,Y coordinate of an object.
 * <br>
 * <br>
 * A {@link Drawable} may request repaint events 
 * 
 * Boundary:
 * 
 * Issued with the scope of an {@link InputConfig}.
 * <br>
 * <br>
 * <b>Examples</b>:
 * <li>
 * <br>
 * @author Charles-Philip Bentley
 *
 */
public class GestureXY {

   /**
    * Modifies X and Y values until they reach their final value.
    * @author Charles-Philip Bentley
    *
    */
   public class RunTask extends TimerTask {

      private int xStep = 0;

      private int yStep = 0;

      /**
       * Will Run as long as there is a Region of the Image showing on Screen or a Pointer Pressed event is generated
       */
      public void run() {
         while (!xFinished || !yFinished) {
            if (!xFinished) {
               x += xMod(x, xStep);
               xStep++;
               boundaryX();
            }
            if (!yFinished) {
               y += yMod(y, yStep);
               yStep++;
               boundaryY();
            }
            System.out.println("x=" + x + " y=" + y);
            //repaint();
            try {
               Thread.sleep(10);
            } catch (InterruptedException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
   }

   public static int mininumDistance = 15;

   int               boxH;

   int               boxW;

   int               dx;

   int               dy;

   boolean           fullImage;

   //
   private int       incrRoot        = 2;

   private int       incrX           = 2;

   private int       incrY           = 2;

   int               mode;

   int               objectH;

   int               objectW;

   private long      pressedTime     = 0;

   private int       px              = 0;

   private int       py              = 0;

   //
   private int       rootIncr        = 2;

   private int       rx;

   private int       ry;

   private Timer     timer;

   int               x;

   /**
    * degree of pull along the x
    */
   int               xAmplitude;

   /**
    * When x movement is finished
    */
   boolean           xFinished;

   int               y;

   /**
    * Mesure of the force.
    */
   int               yAmplitude;

   boolean           yFinished;

   void boundaryX() {
      if (fullImage) {
         int diff = objectW - boxW;
         if (x < -diff) {
            x = -diff;
            xFinished = true;
         }
         if (x > 0) {
            x = 0;
            xFinished = true;
         }
      } else {
         if (x < -objectW) {
            x = -objectW;
            xFinished = true;
         }
         if (x > boxW) {
            x = boxW;
            xFinished = true;
         }
      }
   }

   void boundaryY() {
      if (fullImage) {
         int diff = objectH - boxH;
         if (y < -diff) {
            y = -diff;
            yFinished = true;
         }
         if (y > 0) {
            y = 0;
            yFinished = true;
         }
      } else {
         if (y < -objectH) {
            y = -objectH;
            yFinished = true;
         }
         if (y > boxH) {
            y = boxH;
            yFinished = true;
         }
      }
   }

   public void pointerDragged(int x, int y) {
      //when using finger, there is an instability on weak touch screen
      //so the user must tell if it is using a finer
      this.x -= dx - x;
      this.y -= dy - y;
      boundaryX();
      boundaryY();
      dx = x;
      dy = y;
   }

   /**
    * Computes the time difference between the pressed event.
    * <br>
    * The faster, the bigger the rootIncrement and thus the faster the final X,Y values will be reached.
    * <br>
    * @param x
    * @param y
    */
   public void pointerReleased(int x, int y) {
      long timeDiff = System.currentTimeMillis() - pressedTime;
      rx = x;
      ry = y;
      if (timeDiff < 600) {
         //300 = 4
         //200 = 6
         //100 = 10
         rootIncr = 2;
         if (timeDiff < 300)
            rootIncr = 4;
         if (timeDiff < 200)
            rootIncr = 6;
         if (timeDiff < 100) {
            rootIncr = 10;
         }
         int xDir = px - rx;
         System.out.println("xDir=" + xDir);
         int xMod = Math.abs(xDir) / 3;
         if (Math.abs(xDir) > mininumDistance) {
            xFinished = false;
            if (xDir < 0) {
               incrX = rootIncr + xMod;
            } else {
               incrX = -rootIncr - xMod;
            }
         }
         int yDir = py - ry;
         int yMod = Math.abs(yDir) / 3;
         if (Math.abs(yDir) > mininumDistance) {
            yFinished = false;
            if (yDir < 0) {
               incrY = rootIncr + yMod;
            } else {
               incrY = -rootIncr - yMod;
            }
         }
         timer.schedule(new RunTask(), 0);
      }
      //fire repaint of host
      //this.repaint();
   }

   /**
    * X Modification Function
    * @param x
    * @param step
    * @return
    */
   public int xMod(int x, int step) {
      switch (mode) {
         case 0:
            int v = incrX;
            if (incrX < 0) {
               if (incrX < -incrRoot) {
                  incrX++;
               }
            } else {
               if (incrX > incrRoot) {
                  incrX--;
               }
            }
            return v;
         default:
            throw new IllegalArgumentException();
      }
   }

   public int yMod(int x, int step) {
      switch (mode) {
         case 0:
            int v = incrY;
            if (incrY < 0) {
               if (incrY < -incrRoot) {
                  incrY++;
               }
            } else {
               if (incrY > incrRoot) {
                  incrY--;
               }
            }
            return v;
         default:
            throw new IllegalArgumentException();
      }
   }
}
