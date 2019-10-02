package comp557.a2;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import mintools.viewer.EasyViewer;
import mintools.viewer.Interactor;

/**
 * OpenGL drawing canvas with a specific view.
 * 
 * @author kry
 *
 */
public class CanvasDOFCam implements GLEventListener, Interactor {

	GLCanvas glCanvas;
	
	private Scene scene;
	        
	/** Camera parameters to use when drawing on this canvas */
	private DOFCamera dofCam;
	
	/** Mouse position in window for selection */
	private Point mousePoint = new Point();
	
	/** Flag to trigger a focus point selection */
    private boolean focusSelectRequest = false;
    
    /** Flag to trigger a look at point selection */
    private boolean lookAtSelectRequest = false;
    
    /** Helper to identify image points in world coordiantes */
    private Selector selector = new Selector();
    
    /** Display list ID for quickly drawing the static scene geometry */
    private int list = -1;
    
    /** Accumulation buffer, used by default to help Max OS users */
    private final Accum accum = new Accum();
    
	public CanvasDOFCam( Scene scene, DOFCamera dofCam ) {
		this.scene = scene;
		this.dofCam = dofCam;
		GLProfile glp = GLProfile.getDefault();
        GLCapabilities glcap = new GLCapabilities(glp);
        glCanvas = new GLCanvas( glcap );
        glCanvas.setSize( 500, 500 );
        glCanvas.addGLEventListener(this);
        attach( glCanvas );
        final FPSAnimator animator; 
        animator = new FPSAnimator(glCanvas, 60);
        animator.start();
	}
	
	@Override
    public void attach(Component component) {
      	component.addMouseListener( new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				mousePoint.setLocation(e.getPoint());
				if ( e.getButton() == 1 ) {
					focusSelectRequest = true;
				} else if ( e.getButton() == 3 ) {
					lookAtSelectRequest = true;
				}
			}
		});
      	component.addMouseWheelListener( new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double v = dofCam.dolly.getValue();
				v += e.getPreciseWheelRotation();
				dofCam.dolly.setValue( v );
			}
		});
    }
    
    /** 
     * initializes the class for display 
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0f); // Black Background
        gl.glClearDepth(1.0f); // Depth Buffer Setup
        gl.glEnable(GL.GL_DEPTH_TEST); // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL); // The Type Of Depth Testing To Do
        gl.glEnable( GL2.GL_NORMALIZE ); // normals stay normal length under scale
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
    	GL2 gl = drawable.getGL().getGL2();
    	
    	dofCam.updateFilteredQuantities();
    	
    	// TODO OBJECTIVE 1: See that this display call needs these viewing and projection transforms!
        gl.glMatrixMode( GL2.GL_PROJECTION );
	    gl.glLoadIdentity();
	    dofCam.setupProjection( drawable, 0 ); // sample zero is unshifted
    	
	    gl.glMatrixMode( GL2.GL_MODELVIEW );
	    gl.glLoadIdentity();
	    dofCam.setupViewingTransformation( drawable, 0 ); // sample zero is unshifted

	    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	    list = scene.display( drawable, list );
        
	    if ( focusSelectRequest ) {
        	focusSelectRequest = false;
        	if ( selector.select( drawable, mousePoint ) ) {
        		dofCam.focusPoint.set( selector.selectedPoint );
        		
        		// TODO OBJECTIVE 5: Set the desired focus distance based on the selected point in the world

        		double val = 0; // change this!
        		
        		dofCam.focusDesired.setValue( val );
        	}
        }
        
        if ( lookAtSelectRequest ) {
        	lookAtSelectRequest = false;
        	if ( selector.select( drawable, mousePoint ) ) {
        		dofCam.lookAtDesired.set( selector.selectedPoint );
        		dofCam.dolly.setValue( dofCam.lookAtDesired.distance( dofCam.eyeDesired ) );        		
        	}
        }
        
        if ( dofCam.drawWithBlur.getValue() ) {
        	drawAccumulated(drawable);
        }
        
        EasyViewer.beginOverlay(drawable);
        EasyViewer.printTextLines( drawable, "DOF Camera", 10, 20, 12, GLUT.BITMAP_HELVETICA_18 );
        gl.glEnable( GL2.GL_LIGHTING );
        EasyViewer.endOverlay(drawable);
    }
    
    /** 
     * Use the accumulation buffer to average many views together 
     * @param drawable
     */
    private void drawAccumulated( GLAutoDrawable drawable ) {
        GL2 gl = drawable.getGL().getGL2();
        
        //gl.glAccum( GL2.GL_LOAD, 0f );  
        accum.glAccumLoadZero(drawable);  // glAccum equivalent
        
        int N = dofCam.samples.getValue();
        for ( int i = 0; i < N; i++ ) {
    	    
        	// TODO OBJECTIVE 7: See how different projections are averaged together for your DOF view in this method
        	gl.glMatrixMode( GL2.GL_PROJECTION );
    	    gl.glLoadIdentity();
    	    dofCam.setupProjection( drawable, i );
        	
    	    gl.glMatrixMode( GL2.GL_MODELVIEW );
    	    gl.glLoadIdentity();
    	    dofCam.setupViewingTransformation( drawable, i );

    	    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    	    scene.display( drawable, list );

            //gl.glAccum( GL2.GL_ACCUM, 1.0f/N );  
            accum.glAccum( drawable, 1.0f/N ); // glAccum GL_ACCUM equivalent
        }
        //gl.glAccum( GL2.GL_RETURN, 1 );   
        accum.glAccumReturn(drawable);  // glAccum GL_RETURN equivalent
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
    	// do nothing
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    	// do nothing, glViewPort already called by the component!
    }
	
}
