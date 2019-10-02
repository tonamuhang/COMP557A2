package comp557.a2;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import mintools.swing.ControlFrame;

/**
 * Assignment 2 
 * 
 * We will use an accumulation buffer (a CPU implementation to avoid 
 * problems with Mac OS) to emulate depth of field of a 35 mm camera.
 * 
 * The Accumulation Buffer: Hardware Support for High-Quality Rendering
 * Paul Haeberli and Kurt Akeley
 * SIGGRAPH 1990
 * http://www.cs.duke.edu/courses/fall01/cps124/resources/p309-haeberli.pdf
 * 
 * GPU Gems [2007] has a slightly more recent survey of modern techniques.
 * http://http.developer.nvidia.com/GPUGems/gpugems_ch23.html
 *
 * @author kry
 */
public class A2App {
         
    /**
     * Launches the application creating the windows.
     * @param args
     */
    public static void main(String[] args) {
    	DOFCamera dofCam = new DOFCamera();
    	Scene scene = new Scene( dofCam );
    	
		CanvasDOFCam canvas1 = new CanvasDOFCam( scene, dofCam );
        CanvasCam2 canvas2 = new CanvasCam2( scene, dofCam );

        Dimension controlSize = new Dimension(640, 640);
        Dimension size = new Dimension(1024, 512);
        ControlFrame controlFrame = new ControlFrame("Controls");
       
        controlFrame.add("DOF Camera", dofCam.getControls() );
        controlFrame.add("Camera 2 TBC", canvas2.tbc.getControls() );
        controlFrame.add("Scene", scene.getControls());
        
        controlFrame.setSelectedTab("DOF Camera");
        controlFrame.setSize(controlSize.width, controlSize.height);
        controlFrame.setLocation(size.width + 20, 0);
        controlFrame.setVisible(true);    
        
        JFrame jframe = new JFrame( "Comp 557 Assignment 2 Dolly Zoom and Focus - YOUR NAME HERE" );
        jframe.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent e ) {
                System.exit(0);
            }
        });
        jframe.getContentPane().setLayout( new GridLayout( 1, 2, 2, 2 ) );
        jframe.getContentPane().add( canvas1.glCanvas );
        jframe.getContentPane().add( canvas2.glCanvas );
        jframe.setLocation(0,0);        
        jframe.setSize( jframe.getContentPane().getPreferredSize() );
        jframe.setVisible( true );            
    }    
}
