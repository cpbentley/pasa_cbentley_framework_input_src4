package pasa.cbentley.framework.input.src4.game;

import pasa.cbentley.framework.coredraw.src4.interfaces.IGraphics;

public abstract class Layer {

   private int     h;

   private boolean isVisible;

   private int     w;

   private int     x;

   private int     y;

   /**
    *    Gets the current height of this layer, in pixels.
    * @return
    */
   public int getHeight() {
      return h;
   }

   /**
   Gets the current width of this layer, in pixels.
    * 
    * @return
    */
   public int getWidth() {
      return w;
   }

   /**
    * Gets the horizontal position of this Layer's upper-left corner in the painter's coordinate system.
    * @return
    */
   public int getX() {
      return x;
   }

   /**
   Gets the vertical position of this Layer's upper-left corner in the painter's coordinate system.
    * 
    * @return
    */
   public int getY() {
      return y;
   }

   /**
   Gets the visibility of this Layer.
    * 
    * @return
    */
   public boolean isVisible() {
      return isVisible;
   }

   /**
   Moves this Layer by the specified horizontal and vertical distances.
    * 
    * @param dx
    * @param dy
    */
   public void move(int dx, int dy) {

   }

   /**
   Paints this Layer if it is visible.
    * 
    * @param g
    */
   public abstract void paint(IGraphics g);

   /**
   Sets this Layer's position such that its upper-left corner is located at (x,y) in the painter's coordinate system.
    * 
    * @param x
    * @param y
    */
   public void setPosition(int x, int y) {
      this.x = x;
      this.y = y;
   }

   /**
   Sets the visibility of this Layer.
    * 
    * @param visible
    */
   public void setVisible(boolean visible) {
      isVisible = visible;
   }
}
