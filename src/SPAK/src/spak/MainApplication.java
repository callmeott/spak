package spak;

import javax.swing.UIManager;
import java.awt.*;

/**
 * <p>Title: Hui First JBuilder Project</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vuthichai
 * @version 1.0
 */

public class MainApplication {
  boolean packFrame = false;

  //Construct the application
  public MainApplication(String[] args) {
    ApplicationFrame frame = new ApplicationFrame(args);
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  //Main method
  public static void main(String[] args) {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
      // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
      //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

      // Use Kunststoff Look&Feel only on Windows !
      if (System.getProperty("os.name").indexOf("indows") >= 0)
        UIManager.setLookAndFeel(new com.incors.plaf.kunststoff.
                                 KunststoffLookAndFeel());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new MainApplication(args);
  }
}
