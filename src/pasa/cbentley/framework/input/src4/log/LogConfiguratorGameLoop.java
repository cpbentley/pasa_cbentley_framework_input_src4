package pasa.cbentley.framework.input.src4.log;

import pasa.cbentley.core.src4.logging.IDLogConfig;
import pasa.cbentley.core.src4.logging.ILogConfigurator;
import pasa.cbentley.framework.coreui.src4.engine.CanvasAppliAbstract;
import pasa.cbentley.framework.input.src4.game.FrameData;
import pasa.cbentley.framework.input.src4.game.GameLoopX;
import pasa.cbentley.framework.input.src4.threading.GameLoop;
import pasa.cbentley.framework.input.src4.threading.Simulation;

public class LogConfiguratorGameLoop implements ILogConfigurator {

   public LogConfiguratorGameLoop() {
   }

   public void apply(IDLogConfig log) {
      
      
      log.setLevelGlobal(LVL_03_FINEST);
      
      log.setFlagMaster(MASTER_FLAG_03_ONLY_POSITIVES, true);
      log.setFlagMaster(MASTER_FLAG_05_IGNORE_FLAGS, true);
      log.setFlagMaster(MASTER_FLAG_09_TREAT_STRINGABLE_CLASS, true);
      
      log.setClassPositives(Simulation.class, true);
      log.setClassPositives(FrameData.class, true);
      log.setClassPositives(GameLoop.class, true);
      log.setClassPositives(GameLoopX.class, true);
      log.setClassPositives(CanvasAppliAbstract.class, true);

   }



}
