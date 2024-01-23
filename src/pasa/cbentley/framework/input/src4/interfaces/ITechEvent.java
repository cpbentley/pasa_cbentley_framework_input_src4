package pasa.cbentley.framework.input.src4.interfaces;

import pasa.cbentley.core.src4.interfaces.ITech;

public interface ITechEvent extends ITech {

   /**
    * 
    */
   int EVID_00_UNDEFINED              = 0;
   /**
    * Event ID given, 
    */
   int EVID_01_KEYBOARD_PRESS         = 1;
   int EVID_02_KEYBOARD_RELEASE       = 2;
   int EVID_11_POINTER_PRESS          = 11;
   int EVID_12_POINTER_RELEASE        = 12;
   int EVID_13_POINTER_MOVE           = 13;
   int EVID_14_POINTER_DRAG           = 14;
   int EVID_15_PAD_PRESS              = 15;
   int EVID_16_PAD_RELEASE            = 16;
   int EVID_20_WHEEL                  = 20;
   int EVID_40_CANVAS                 = 40;
   int EVID_15_GESTURE                = 15;

}
