package pasa.cbentley.framework.input.src4.event.ctrl;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.engine.CanvasAppliInput;
import pasa.cbentley.framework.input.src4.engine.InputRequests;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;

/**
 * 
 * This class sends an aggregate of all drag events received during the rendering.
 * 
 * For more info see {@link DragController3}
 * 
 * This is used with {@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}.
 * 
 *  * Note about MIDP 2.0 <br>
 * 
 * However in the scrolling business, our framework needs to deal with execissve queued key event that create noticeable lag.
 * For {@link IDrawable}, dragging the pointer generates lots of events in a very short amount of time.
 * One rule would be to use another repeater thread for drag events. Every events checks to the EventThread, that will be run. 
 * <br>
 * Framework  <br>
 * You cannot flush the queued events of the MIDP 2.0. So you have to implement the drop of overflowing input events or
 * the MIDP thread updates the InputConfig.
 * 
 * Controller command matching codes may run in 3 threads:
 * <p>
 * 
 * With the {@link Repeater}.<br>
 * <b>Example with the Drag</b>: <br>
 * <li>Drag #0 on scrollbar is recieved and ignored (perf on slow CPU).
 * <li>Drag #1 on scrollbar is recieved.
 * <li> Paint is generated.
 * <li>Paint starts for Drag #1.
 * <li>Drag event #2  recieved. Queued. Dropped
 * <li>Drag event #3 recieved. Update x,y of the previous event. Dropped
 * <li>Drag event #4 recieved. Update x,y of the previous event.
 * <li>Paint finishes
 * <li>Pending drag is process with the #4 x,y.
 * </p>
 * 
 * <p>
 * Dropping an Event is user friendly only with continuous events like dragging and navigational keys + other
 * keys that advetise are dropping friendly.
 * </p>
 * 
 * @author Charles Bentley
 *
 */
public class EventControllerOneThreadCtrled extends EventControllerOneThread {

   /**
    * 
    */
   private DragController3 dragCtrl;

   public EventControllerOneThreadCtrled(InputCtx ic, CanvasAppliInput canvas) {
      super(ic, canvas);
      dragCtrl = new DragController3(ic, this);
      Thread t = new Thread(dragCtrl, "Drag");
      t.start();
   }

   public void paintFinished() {
      super.paintFinished();
      dragCtrl.paintFinished();

   }

   public void paintStart() {
      super.paintStart();
      dragCtrl.paintStart();
   }

   public void pointerDragged(int x, int y, int id) {
      dragCtrl.addEventDragged(x, y, id);
   }

   public void pointerDraggedCtrl(int x, int y, int id) {
   }

   public void pointerPressed(int x, int y, int id, int button) {
      dragCtrl.addEventPress(x, y, id, button);
   }

   /**
    * When a Press is first computed, the code
    * looks for the interest of a command reacting
    * on a long or repetition.
    * This request is made through {@link InputRequests}.
    * <br>
    * If both long and repetition interests, a long event will first
    * be generated and will be repeated.
    * This depends on the context of event.
    * If a command manager has the long press 5 secs and swipe up and down
    * @param x
    * @param y
    * @param id
    */
   public void pointerPressedCtrl(int x, int y, int id, int button) {
   }

   public void pointerReleased(int x, int y, int id, int button) {
      dragCtrl.addEventRelease(x, y, id, button);
   }

   /**
    * Android Long Presses can be used for default timers. If commands
    * requires a custom long press timer, we will use one here.
    * <br>
    * @param x
    * @param y
    * @param id
    */
   public void pointerReleasedCtrl(int x, int y, int id, int button) {
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, EventControllerOneThreadCtrled.class, 85);
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, EventControllerOneThreadCtrled.class, 85);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }
   //#enddebug

}
