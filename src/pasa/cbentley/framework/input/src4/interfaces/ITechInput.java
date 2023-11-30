package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.byteobjects.src4.tech.ITechByteObject;
import pasa.cbentley.framework.input.src4.threading.GameLoop;

public interface ITechInput extends ITechByteObject {

   /**
    * Host UI events are generated by host ui thread or an event controller
    * thread
    */
   public static final int THREAD_0_HOST_HUI              = 0;

   public static final int THREAD_1_UPDATE                = 1;

   public static final int THREAD_2_RENDER                = 2;

   /**
    * 1 Thread for 
    * <li>Host UI Events
    * <li>Update Simulation
    * <li>Rendering
    * <br>
    * <br>
    * The default behavior. Used by most UI frameworks. Everything is done in the UI thread.
    * Commands run worker tasks for long running operations.
    */
   public static final int THREADING_0_ONE_TO_RULE_ALL    = 0;

   /**
    * Uses a {@link GameLoop} single thread.<br>
    * 1 Thread
    * <li>Host UI Events
    * <br>
    * 1 Thread
    * <li>Update
    * <li>Rendering
    * <li> {@link Runnable}
    * That thread waits and executes Update and Render jobs from a single queue
    * <br>
    */
   public static final int THREADING_1_UI_UPDATERENDERING = 1;

   /**
    * <li>THREADING_2_UIUPDATE_RENDERING Break Down</li>
    * <b>1 Thread</b> for
    * <li>Host UI Events</li>
    * <li>Update</li>
    * <b>1 Thread</b> for
    * <li>Rendering</li>
    * <br>
    * <br>
    * In this mode, the rendering is done in its own thread.
    * The Host UI thread?
    */
   public static final int THREADING_2_UIUPDATE_RENDERING = 2;

   /**
    * 1 Thread
    * <li>Host UI Events</li>
    * 1 Thread
    * 
    * <li>Update</li>
    * 1 Thread
    * <li>Rendering</li>
    * <br>
    * <br>
    * When rendering has many animations....
    */
   public static final int THREADING_3_THREE_SEPARATE     = 3;

   /**
    * 1 Thread
    * <li>Host UI Events
    * 1 Thread
    * <li>Clocks rendering and Update ticks
    * 
    * 
    */
   public static final int THREADING_4_UI_CLOCKING        = 4;

   /**
    * Set to true when  all the items (background, drawables, animations, debug, menubar etc.) have to be repainted.
    * <br>
    * The whole screen is erased with Canvas bgColor, background is drawn, drawables unless hidden and then animated images
    */
   public static final int REPAINT_01_FULL                = 1 << 0;

   /**
    * Repaint is external. 
    */
   public static final int REPAINT_02_EXTERNAL            = 1 << 1;

   /**
    * 
    */
   public static final int EVID_00_UNDEFINED              = 0;

   /**
    * Event ID given, 
    */
   public static final int EVID_01_KEYBOARD_PRESS         = 1;

   public static final int EVID_02_KEYBOARD_RELEASE       = 2;

   public static final int EVID_11_POINTER_PRESS          = 11;

   public static final int EVID_12_POINTER_RELEASE        = 12;

   public static final int EVID_13_POINTER_MOVE           = 13;

   public static final int EVID_14_POINTER_DRAG           = 14;

   public static final int EVID_15_PAD_PRESS              = 15;

   public static final int EVID_16_PAD_RELEASE            = 16;

   public static final int EVID_20_WHEEL                  = 20;

   public static final int EVID_40_CANVAS                 = 40;

   public static final int EVID_15_GESTURE                = 15;

}