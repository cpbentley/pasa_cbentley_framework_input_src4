package pasa.cbentley.framework.input.src4.ctx;

import pasa.cbentley.core.src4.logging.ToStringStaticBase;
import pasa.cbentley.framework.input.src4.event.jobs.ITechInputJob;
import pasa.cbentley.framework.input.src4.interfaces.ITechPaintThread;

public class ToStringStaticInput extends ToStringStaticBase {

   public static final String toStringThreadingMode(int mode) {
      switch (mode) {
         case ITechPaintThread.THREADING_0_ONE_TO_RULE_ALL:
            return "Passive";
         case ITechPaintThread.THREADING_1_UI_UPDATERENDERING:
            return "Active Basic";
         case ITechPaintThread.THREADING_2_UIUPDATE_RENDERING:
            return "UiUpdate Rendering";
         case ITechPaintThread.THREADING_3_THREE_SEPARATE:
            return "Active 3 Threads";
         default:
            return "Unknown" + mode;
      }
   }

   public static final String toStringJobState(int state) {
      switch (state) {
         case ITechInputJob.JOB_STATE_0_WAITING:
            return "Waiting";
         case ITechInputJob.JOB_STATE_1_RUN:
            return "Running";
         case ITechInputJob.JOB_STATE_2_CANCELED:
            return "Canceled";
         case ITechInputJob.JOB_STATE_3_FINALIZED:
            return "Finalized";
         default:
            return "UnknownState" + state;
      }
   }

   public static String toStringTypeBO(int type) {
      switch (type) {
         case IBOTypesInput.TYPE_1_TECH_CANVAS_APPLI:
            return "CanvasAppli";
         default:
            return null;
      }
   }

}
