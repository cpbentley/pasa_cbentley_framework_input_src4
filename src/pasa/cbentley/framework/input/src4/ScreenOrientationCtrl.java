package pasa.cbentley.framework.input.src4;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.coreui.src4.tech.IBCodes;
import pasa.cbentley.framework.coreui.src4.tech.ITechCanvasHost;
import pasa.cbentley.framework.coreui.src4.tech.ITechFeaturesUI;
import pasa.cbentley.framework.input.src4.ctx.ITechCtxSettingsInput;
import pasa.cbentley.framework.input.src4.ctx.InputCtx;

/**
 * The screen control class
 * @author Charles Bentley
 *
 */
public class ScreenOrientationCtrl implements ITechCtxSettingsInput, IStringable {

   public static String toStringConfig(int c) {
      switch (c) {
         case ITechCanvasHost.SCREEN_0_TOP_NORMAL:
            return "Normal";
         case ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN:
            return "UpsideDown";
         case ITechCanvasHost.SCREEN_2_LEFT_ROTATED:
            return "Left";
         case ITechCanvasHost.SCREEN_3_RIGHT_ROTATED:
            return "Right";
         default:
            return "UnknownConfig" + c;
      }
   }

   private CanvasAppliInput canvas;

   protected final InputCtx ic;

   private int              screenConfig;

   public ScreenOrientationCtrl(InputCtx ic, CanvasAppliInput canvas) {
      this.ic = ic;
      this.canvas = canvas;
   }

   public int getOrientation() {
      return screenConfig;
   }

   public boolean isRotated() {
      return screenConfig != ITechCanvasHost.SCREEN_0_TOP_NORMAL;
   }

   /**
    * Check if Host had its own way to flipping the screen
    */
   public void rotate(int newConfig) {
      boolean supportRot = canvas.hasCanvasFeatureSupport(ITechFeaturesUI.SUP_ID_05_SCREEN_ROTATIONS);
      //Can device Android can check if screen rotation is locked?
      if (supportRot) {
         //check if hardware did right. otherwise use 
      } else {
         //ask the canvas to deal with new config
      }
      canvas.postRotation();
   }

   /**
    * Called
    */
   public void rotateLeft() {
      if (screenConfig == ITechCanvasHost.SCREEN_0_TOP_NORMAL) {
         screenConfig = ITechCanvasHost.SCREEN_2_LEFT_ROTATED;
      } else if (screenConfig == ITechCanvasHost.SCREEN_2_LEFT_ROTATED) {
         screenConfig = ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN;
      } else if (screenConfig == ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN) {
         screenConfig = ITechCanvasHost.SCREEN_3_RIGHT_ROTATED;
      } else {
         screenConfig = ITechCanvasHost.SCREEN_0_TOP_NORMAL;
      }
      rotate(screenConfig);
   }

   public void rotateRight() {
      if (screenConfig == ITechCanvasHost.SCREEN_0_TOP_NORMAL) {
         screenConfig = ITechCanvasHost.SCREEN_3_RIGHT_ROTATED;
      } else if (screenConfig == ITechCanvasHost.SCREEN_3_RIGHT_ROTATED) {
         screenConfig = ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN;
      } else if (screenConfig == ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN) {
         screenConfig = ITechCanvasHost.SCREEN_2_LEFT_ROTATED;
      } else {
         screenConfig = ITechCanvasHost.SCREEN_0_TOP_NORMAL;
      }
      rotate(screenConfig);
   }

   int rotationKeyChange(int gk) {
      if (gk == IBCodes.KEY_UP) {
         if (screenConfig == ITechCanvasHost.SCREEN_2_LEFT_ROTATED) {
            gk = IBCodes.KEY_LEFT;
         } else if (screenConfig == ITechCanvasHost.SCREEN_3_RIGHT_ROTATED) {
            gk = IBCodes.KEY_RIGHT;
         } else if (screenConfig == ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN) {
            gk = IBCodes.KEY_DOWN;
         }
      } else if (gk == IBCodes.KEY_DOWN) {
         if (screenConfig == ITechCanvasHost.SCREEN_2_LEFT_ROTATED) {
            gk = IBCodes.KEY_RIGHT;
         } else if (screenConfig == ITechCanvasHost.SCREEN_3_RIGHT_ROTATED) {
            gk = IBCodes.KEY_LEFT;
         } else if (screenConfig == ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN) {
            gk = IBCodes.KEY_UP;
         }
      } else if (gk == IBCodes.KEY_LEFT) {
         if (screenConfig == ITechCanvasHost.SCREEN_2_LEFT_ROTATED) {
            gk = IBCodes.KEY_DOWN;
         } else if (screenConfig == ITechCanvasHost.SCREEN_3_RIGHT_ROTATED) {
            gk = IBCodes.KEY_UP;
         } else if (screenConfig == ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN) {
            gk = IBCodes.KEY_DOWN;
         }
      } else if (gk == IBCodes.KEY_RIGHT) {
         if (screenConfig == ITechCanvasHost.SCREEN_2_LEFT_ROTATED) {
            gk = IBCodes.KEY_UP;
         } else if (screenConfig == ITechCanvasHost.SCREEN_3_RIGHT_ROTATED) {
            gk = IBCodes.KEY_DOWN;
         } else if (screenConfig == ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN) {
            gk = IBCodes.KEY_UP;
         }
      }
      return gk;
   }

   int rotationXChange(int x, int y) {
      if (screenConfig == ITechCanvasHost.SCREEN_2_LEFT_ROTATED) {
         return y;
      } else if (screenConfig == ITechCanvasHost.SCREEN_3_RIGHT_ROTATED) {
         // 0,0 = 0,y
         return canvas.getWidth() - y;
      } else if (screenConfig == ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN) {
         // 0,0 = x,y
         return canvas.getWidth() - x;
      }
      return x;
   }

   int rotationYChange(int x, int y) {
      if (screenConfig == ITechCanvasHost.SCREEN_2_LEFT_ROTATED) {
         return canvas.getHeight() - x;
      } else if (screenConfig == ITechCanvasHost.SCREEN_3_RIGHT_ROTATED) {
         return x;
      } else if (screenConfig == ITechCanvasHost.SCREEN_1_BOT_UPSIDEDOWN) {
         return canvas.getHeight() - y;
      }
      return y;
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "ScreenOrientationCtrl");
      dc.appendVarWithSpace("Config", toStringConfig(screenConfig));
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "ScreenOrientationCtrl");
      dc.appendVarWithSpace("Config", toStringConfig(screenConfig));
   }

   public UCtx toStringGetUCtx() {
      return ic.getUCtx();
   }
   //#enddebug
}
