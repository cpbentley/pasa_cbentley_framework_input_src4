package pasa.cbentley.framework.input.src4;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.coreui.src4.event.BEvent;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;

/**
 * Control how event state is added to the {@link InputState}.
 * <br>
 * It is a layer  that may decide to drop and postpone events because of {@link CanvasAppliInput} settings 
 * such as thread configurations.
 * <br>
 * @author Charles Bentley
 *
 */
public abstract class EventController implements IStringable {

   protected CanvasAppliInput canvas;

   protected final InputCtx   ic;

   public EventController(InputCtx ic, CanvasAppliInput canvas) {
      this.ic = ic;
      this.canvas = canvas;
   }

   /**
    * 
    * Fetch the current {@link InputState}, i.e. the very latest {@link InputState}.
    * <br>
    * The {@link InputState} is updated by the {@link ITechPaintThread#THREAD_0_HOST_HUI} by external
    * events.
    * <br>
    * In Threading Configuration 
    * <li>{@link ITechPaintThread#THREADING_1_UI_UPDATERENDERING} 
    * <li>{@link ITechPaintThread#THREADING_3_THREE_SEPARATE} 
    * <br>
    * In Threading Configuration
    * <li>{@link ITechPaintThread#THREADING_2_UIUPDATE_RENDERING} 
    * <br>
    * 
    * @return
    */
   public abstract InputState getState();

   public abstract void event(BEvent g, CanvasAppliInput canvas);

   /**
    * 
    * @param is
    * @param g
    * @return
    */
   public boolean event(InputState is, BEvent g, CanvasAppliInput canvas) {
      boolean isAccepted = is.addEvent(g, canvas);
      return isAccepted;
   }

   public void paintStart() {

   }

   /**
    * Called in the render thread.
    */
   public void paintFinished() {

   }

   /**
    * Called at the end of event processing
    * @param is
    */
   public void endOfEvent(InputState is) {

   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, EventController.class);
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, EventController.class);
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return ic.getUC();
   }

   //#enddebug

}
