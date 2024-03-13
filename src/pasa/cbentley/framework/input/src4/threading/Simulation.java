package pasa.cbentley.framework.input.src4.threading;

import java.util.Enumeration;
import java.util.Vector;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.input.src4.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.game.FrameData;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;
import pasa.cbentley.framework.input.src4.interfaces.IUpdatableSim;

/**
 * 
 * @author Charles Bentley
 *
 */
public class Simulation implements IStringable {

   private Vector           simObjects = new Vector();

   protected final InputCtx ic;

   public Simulation(InputCtx ic) {
      this.ic = ic;
   }

   /**
    * tick simulations running
    * @param is
    */
   public void simulationUpdate(InputState is) {
      Enumeration en = simObjects.elements();
      FrameData data = is.getFrameData();
      while (en.hasMoreElements()) {
         IUpdatableSim runnableSim = (IUpdatableSim) en.nextElement();
         runnableSim.update(data);
      }
   }

   /**
    * {@link ITechPaintThread#THREADING_0_ONE_TO_RULE_ALL}, the call be done serially
    * @param sim
    */
   public void simulationAdd(IUpdatableSim sim) {
      simObjects.addElement(sim);
   }
   
   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, Simulation.class, "@line5");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, Simulation.class);
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return ic.getUC();
   }

   //#enddebug
   

}
