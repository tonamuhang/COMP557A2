package comp557.a2;

import javax.swing.JPanel;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

import mintools.parameters.DoubleParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;

/**
 * Class containing the scene, and draws in world coordinates
 * 
 * @author kry
 *
 */
public class Scene {

	private GLUT glut = new GLUT();

	/** 
	 * The scene includes the depth of field camera because we want
	 * to visualize the focus point in both the DOF camera view and the
	 * camera 2 view
	 */
	DOFCamera dofCam;
		
	/**
	 * Scene is constructed with the depth of field camera because some settings of the camera will be drawn in the 
	 * world as part of the scene.
	 * @param dofCam
	 */
	public Scene( DOFCamera dofCam ) {
		this.dofCam = dofCam;
	}
	
    // The scene parameters are for adjusting how the scene looks, e.g., put it in front or behind the screen rectangle.    
    private DoubleParameter sceneDistanceFromScreen = new DoubleParameter("scene distance", 0, -2, 2);    
    private DoubleParameter sceneScale = new DoubleParameter("scene scale", 1, 0.01, 100);
    private DoubleParameter sceneTilt = new DoubleParameter("scene tilt", 0, -90, 90 );
    private DoubleParameter sceneRotate = new DoubleParameter("scene rotate", 0, -90, 90 );

    DoubleParameter x = new DoubleParameter("light x", 2, -5, 5);
    DoubleParameter y = new DoubleParameter("light y", 10, 0, 10);
    DoubleParameter z = new DoubleParameter("light z", 5, -5, 10);

    DoubleParameter a = new DoubleParameter("attenuation a (constant)", 1, 0, 1);
    DoubleParameter b = new DoubleParameter("attenuation b (linear)", 0.02, 0, .1);
    DoubleParameter c = new DoubleParameter("attenuation c (quadratic)", 0.00, 0, .1);
        
	final float[] white = {1,1,1,1};
	final float[] grey = {0.75f,0.75f,0.75f,1f};

	final float[] black = {0,0,0,1};
	float[][] colours = new float[][] {
		{.75f,1,1,1},
		{1,.75f,1,1},
		{1,1,.75f,1},
		{.75f,.75f,1,1},
		{1,.75f,.75f,1},
		{0.25f,0.25f,0.25f,1},
		{.75f,1,.75f,1}
	};

	/** 
	 * Sets up the lights 
	 * Expects that OpenGL is drawing in world coordinates when this is called
	 */
    private void setLights( GLAutoDrawable drawable ) {
    	GL2 gl = drawable.getGL().getGL2();
		gl.glEnable( GL2.GL_LIGHTING );
		gl.glEnable( GL2.GL_LIGHT0 );
		// WATCH OUT: need to provide homogeneous coordinates to many calls!! 
		float[] lightPosition = {x.getFloatValue(),y.getFloatValue(),z.getFloatValue(),1}; 
		float[] dark = new float[] {0.1f,0.1f,0.1f,1};
		gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0 );
		gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_DIFFUSE, white, 0 );
		gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_AMBIENT, black, 0);
		gl.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, dark, 0);
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, a.getFloatValue()); 
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, b.getFloatValue() );
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, c.getFloatValue() ); 
    }
        
    /**
     * Draws a test scene.  Position and size are adjustable with the controls.
     * @param drawable
     * @param list		display list to use, or -1 to make one, or zero to just draw slowly
     */
    public int display( GLAutoDrawable drawable, int list ) {
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glPushMatrix();

	    gl.glPushMatrix();
	    gl.glTranslated( dofCam.focusPoint.x, dofCam.focusPoint.y, dofCam.focusPoint.z );
	    EasyViewer.glut.glutSolidSphere( 0.1, 10, 10 );
	    gl.glPopMatrix();
        
        gl.glTranslated( 0,0, sceneDistanceFromScreen.getValue());
        gl.glRotated( sceneTilt.getValue(), 1,0,0);
        gl.glRotated( sceneRotate.getValue(), 0, 1, 0 );
        double ss = sceneScale.getValue();
        gl.glScaled( ss,ss,ss );
        
        setLights( drawable );
        
        boolean makingList = false;
        if ( list > 0 ) {
        	gl.glCallList(list);
        	gl.glPopMatrix();
        	return list;
        	
        } else if ( list == -1 ){
        	makingList = true;
        	list = gl.glGenLists(1);
        	gl.glNewList(list, GL2.GL_COMPILE_AND_EXECUTE );
        }
        
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, black, 0 );
        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50 );

        // This is REALLY DUMB! 
        // It would be much better as textured geometry with per pixel lighting
        for ( int i = -20; i < 20; i++ ) {
        	for ( int j = -20; j <= 10; j++ ) {
                gl.glBegin( GL2.GL_QUAD_STRIP );
                gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ((i+j)%2)==0?grey:white, 0 );
                gl.glNormal3f(0,1,0);
                gl.glVertex3d( i, -1, j );
                gl.glVertex3d( i, -1, j+1 );
                gl.glVertex3d( i+1, -1, j );
                gl.glVertex3d( i+1, -1, j+1 );        
                gl.glEnd();
        	}
        }
        double r = 20.0/Math.PI;
        for ( int i = -20; i < 20; i++ ) {
        	for ( int j = 0; j <= 9; j++ ) {
                gl.glBegin( GL2.GL_QUAD_STRIP );
                gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ((i+j)%2)==0?grey:white, 0 );
                gl.glNormal3f(0,0,1);
                double y1 = -r * Math.sin(     j/10.0*Math.PI/2 ); 
                double z1 = -r * Math.cos(     j/10.0*Math.PI/2 ); 
                double y2 = -r * Math.sin( (j+1)/10.0*Math.PI/2 ); 
                double z2 = -r * Math.cos( (j+1)/10.0*Math.PI/2 ); 
                gl.glNormal3d(0,-y1,-z1);
                gl.glVertex3d(   i, r + y1-1, -20+z1 );
                gl.glNormal3d(0,-y2,-z2);
                gl.glVertex3d(   i, r + y2-1, -20+z2 );
                gl.glNormal3d(0,-y1,-z1);
                gl.glVertex3d( i+1, r + y1-1, -20+z1 );
                gl.glNormal3d(0,-y2,-z2);
                gl.glVertex3d( i+1, r + y2-1, -20+z2 );
                gl.glEnd();
        	}
        }
        // a small vertical strip at the back...
        for ( int i = -20; i < 20; i++ ) {
        	for ( int j = 0; j <= 10; j++ ) {
                gl.glBegin( GL2.GL_QUAD_STRIP );
                gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ((i+j+1)%2)==0?grey:white, 0 );
                gl.glNormal3f(0,0,1);
                gl.glVertex3d( i,  r-1 +j, -20-r);
                gl.glVertex3d( i,  r-1 +j+1,-20-r );
                gl.glVertex3d( i+1, r-1 + j, -20-r );
                gl.glVertex3d( i+1, r-1 +j+1, -20-r );        
                gl.glEnd();
        	}
        }
        gl.glTranslated(-3.5,0,3.5);
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[0], 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, white, 0 );
        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 127 );
        gl.glPushMatrix();                
        gl.glRotated( -10, 0,1,0);
        gl.glTranslated( 0,-.25,0);
        glut.glutSolidTeapot(1);
        gl.glPopMatrix();
        
        gl.glTranslated(0.85,0,-3);
        gl.glRotated(-30,0,1,0);
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[1], 0 );
        glut.glutSolidCylinder(1, 1, 20, 20);
        
        gl.glTranslated(0.85,0,-3);
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[2], 0 );
        glut.glutSolidDodecahedron();

        gl.glTranslated(0.85,0,-3);
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[3], 0 );
        glut.glutSolidRhombicDodecahedron();
        
        gl.glTranslated(0.85,0,-3);
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[4], 0 );
        glut.glutSolidCone(1, 2, 10, 10);
        
        gl.glTranslated(0.85,0,-3);
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[5], 0 );
        glut.glutSolidSphere(1, 20, 20 );
        
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, colours[6], 0 );

        gl.glTranslated( 1,0,-3 );
        gl.glRotated(-45,0,1,0);
        glut.glutSolidTorus( 0.25, 0.75, 10, 25 );	

        if ( makingList ) {
        	gl.glEndList();
	    }
        	
        gl.glPopMatrix();
        return list;
    }
    
    public JPanel getControls() {
    	VerticalFlowPanel vfp = new VerticalFlowPanel();  	
        vfp.add( sceneTilt.getSliderControls(false));       
        vfp.add( sceneRotate.getSliderControls(false));
        vfp.add( sceneScale.getSliderControls(true ) );
        vfp.add ( sceneDistanceFromScreen.getSliderControls(false) );
        vfp.add( a.getSliderControls(false));
        vfp.add( b.getSliderControls(false));
        vfp.add( c.getSliderControls(false));
        vfp.add( x.getSliderControls(false));
        vfp.add( y.getSliderControls(false));
        vfp.add( z.getSliderControls(false));
    	return vfp.getPanel();
    }
}
