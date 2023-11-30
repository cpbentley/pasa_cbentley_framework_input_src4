package pasa.cbentley.framework.input.src4.event.keys;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coreui.src4.ctx.ToStringStaticCoreUi;
import pasa.cbentley.framework.coreui.src4.event.BEvent;
import pasa.cbentley.framework.coreui.src4.event.DeviceEventXY;
import pasa.cbentley.framework.coreui.src4.event.EventKey;
import pasa.cbentley.framework.coreui.src4.event.GestureArea;
import pasa.cbentley.framework.coreui.src4.event.GestureUtils;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

/**
 * Activates when pointer changes grid area
 * <br>
 * There is no specific position. 
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class EventKeyGridCrossing extends EventKey {

   private int         grid;

   private int         pointerID;

   private int         previous;

   private GestureArea ga;

   public EventKeyGridCrossing(InputCtx ic, int keyType, int grid, int pointerID, GestureArea ga) {
      super(ic.getCUC(), keyType);
      this.grid = grid;
      this.pointerID = pointerID;
      this.ga = ga;
   }

   public boolean isKeyActivated(BEvent be) {
      if (be instanceof DeviceEventXY) {
         DeviceEventXY de = (DeviceEventXY) be;
         if (de.getDeviceID() == pointerID) {
            int x = de.getX();
            int y = de.getY();
            int currentGrid = GestureUtils.computeXYGridPosition(x, y, ga, grid);
            if (currentGrid != previous) {
               previous = currentGrid;
               return true;
            }
         }
      }
      return false;
   }

   public String getUserLineString() {
      return "Grid " + " " + ToStringStaticCoreUi.getStringGridType(grid) + " " + ToStringStaticCoreUi.toStringKeyEventUserLine(patternAction);
   }

   public int getCurrentGrid() {
      return previous;
   }

   public boolean isEquals(EventKey ek) {
      // TODO Auto-generated method stub
      return false;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "EventKeyGrid");
      super.toString(dc.sup());
      dc.appendVarWithSpace("PointerID", pointerID);
      dc.appendVarWithSpace("Grid", ToStringStaticCoreUi.getStringGridType(grid));
      dc.nlLvl(ga);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "EventKeyGrid");
      dc.appendVarWithSpace("PointerID", pointerID);
      dc.appendVarWithSpace("Grid", ToStringStaticCoreUi.getStringGridType(grid));
      dc.nlLvl(ga);
      super.toString1Line(dc.sup1Line());
   }
   //#enddebug
}
