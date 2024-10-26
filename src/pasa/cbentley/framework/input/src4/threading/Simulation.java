package pasa.cbentley.framework.input.src4.threading;

import java.util.Enumeration;
import java.util.Vector;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.core.ui.src4.input.InputState;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;
import pasa.cbentley.framework.input.src4.ctx.ObjectIC;
import pasa.cbentley.framework.input.src4.engine.InputStateCanvas;
import pasa.cbentley.framework.input.src4.game.FrameData;
import pasa.cbentley.framework.input.src4.interfaces.ITechThreadPaint;
import pasa.cbentley.framework.input.src4.interfaces.ISimulationUpdatable;

/**
 * 
 * @author Charles Bentley
 *
 */
public class Simulation extends ObjectIC implements IStringable {

   private Vector simObjects = new Vector();

   public Simulation(InputCtx ic) {
      super(ic);
   }

   /**
    * tick simulations running
    * @param is
    */
   public void simulationUpdate(InputStateCanvas is) {
      Enumeration en = simObjects.elements();
      FrameData data = is.getFrameData();
      if (data == null) {
         throw new NullPointerException("Framedata cannot be null. Must be set");
      }
      while (en.hasMoreElements()) {
         ISimulationUpdatable runnableSim = (ISimulationUpdatable) en.nextElement();
         runnableSim.update(data);
      }
   }

   /**
    * {@link ITechThreadPaint#THREADING_0_ONE_TO_RULE_ALL}, the call be done serially
    * @param sim
    */
   public void simulationAdd(ISimulationUpdatable sim) {
      simObjects.addElement(sim);
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, Simulation.class, toStringGetLine(60));
      toStringPrivate(dc);
      super.toString(dc.sup());

      int num = simObjects.size();
      dc.appendVar("number of simulations", num);
      for (int i = 0; i < num; i++) {
         ISimulationUpdatable e = (ISimulationUpdatable) simObjects.elementAt(i);
         dc.nlLvl(e, "#" + (i + 1));
      }
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, Simulation.class, toStringGetLine(60));
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }
   //#enddebug

}
