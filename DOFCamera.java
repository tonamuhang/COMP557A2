package comp557.a2;

import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.parameters.Parameter;
import mintools.parameters.ParameterListener;
import mintools.swing.VerticalFlowPanel;

/**
 * Depth of field (DOF) Dolly camera parameters
 * 
 * Note that many of these parameters have limits that help avoid bad settings
 * but not everything will necessarily work perfectly in some settings.  For 
 * instance, nothing is preventing you from setting your near plane past your 
 * far plane!  There will be other bad settings in this assignment too.
 * 
 * @author kry
 */
public class DOFCamera {

	DoubleParameter near = new DoubleParameter( "near plane distance m", 0.25, 0.25, 20 );
	DoubleParameter far  = new DoubleParameter( "far plane distance m", 80, 10, 200 );
	DoubleParameter focusDesired = new DoubleParameter( "focus plane distance m", 15, 0.4, 200 );
	DoubleParameter fstop = new DoubleParameter( "f-stop", 1.2, 1.2, 22 );
	DoubleParameter focalLength = new DoubleParameter( "focal length mm", 35, 18, 500 );     
	DoubleParameter fovy = new DoubleParameter( "fov y degrees", 34, 5, 90 );
	DoubleParameter sensorHeight = new DoubleParameter( "sensor height mm", 24, 10, 100 ); // limits?
	DoubleParameter dolly = new DoubleParameter( "dolly m", 15, 0.5, 30 );
	
	/** Enables follow focus when you change the dolly parameter */
	BooleanParameter dollyFocus = new BooleanParameter( "enable dolly focus", false );
	
	/** Enables follow zoom when you change the dolly parameter */
	BooleanParameter dollyZoom = new BooleanParameter( "enable dolly zoom", false );
	
	/** This exponential filter parameter provides a bit of an animation effect to some parameters */
	DoubleParameter filterRate = new DoubleParameter( "filter rate", 0.5, 0, 1 );

	/** Focus point in world */
	Point3d focusPoint = new Point3d( 0, 0, 0 );
	
	/** desired look at point in world, which drives the actual temporally filtered position */
	Point3d lookAtDesired = new Point3d( 0, 0.5, 0 );
	/** desired eye position in world, which drives the actual temporally filtered position */
	Point3d eyeDesired = new Point3d( -5, 0.75, 10 );

	/** temporally filtered focus parameter */
	double focusDistance = 15; 
	/** temporally filtered look at position */
	Point3d lookAt = new Point3d( 0, 1, 0 );
	/** temporally filtered eye position */
	Point3d eye = new Point3d( 0, 0, 10 );
	
	/** samples to average to create depth of field blur */
	IntParameter samples = new IntParameter( "samples", 8, 1, 20 );    

	BooleanParameter drawWithBlur = new BooleanParameter("draw with DOF blur", false );
	
    /** Helper class for creating a number of Poisson disk random samples in a region */
    private final FastPoissonDisk fpd = new FastPoissonDisk();
    
    /**
     * Gentle temporal filtering of the look at point, eye position, and focus distance
     * TODO OBJECTIVE 8: Note you will want to use the filtered values for dolly focus and dolly zoom 
     */
    public void updateFilteredQuantities() {
    	double alpha = filterRate.getValue();
    	final Vector3d v = new Vector3d();
    	
    	focusDistance = alpha * focusDistance + (1-alpha) * focusDesired.getValue();
    	    	
    	v.scale( 1 - alpha, lookAtDesired );
    	lookAt.scale( alpha );
    	lookAt.add( v );
    	    	
    	v.scale( 1 - alpha, eyeDesired );
    	eye.scale( alpha );
    	eye.add( v );    
    	
		if ( dollyFocus.getValue() ) {
	    	// TODO OBJECTIVE 8: Set the focusDistance based on the dolly


		}
		
		if ( dollyZoom.getValue() ) {
			// TODO OBJECTIVE 8: Set the focal length based on the dolly
			// Hint: store the size (e.g., height) of the focus plane rectangle
			// and make sure it stays constant with respect to the other parameters

		}
    }
	
    /** Used to let dependent parameters update one another */
    boolean ignoreParameterChangeCallback = false;
     
    JPanel getControls() {
    	VerticalFlowPanel vfp = new VerticalFlowPanel();
    	vfp.add( near.getSliderControls(false) );
    	vfp.add( far.getSliderControls(false) );
    	vfp.add( focusDesired.getSliderControls(false) );
    	vfp.add( focalLength.getSliderControls(false) );
    	vfp.add( fovy.getSliderControls(false) );
    	vfp.add( fstop.getSliderControls(false) );
    	vfp.add( sensorHeight.getSliderControls(false) );
    	vfp.add( dolly.getSliderControls(false) );    	
    	vfp.add( dollyFocus.getControls() );
    	vfp.add( dollyZoom.getControls() );    	
    	vfp.add( filterRate.getSliderControls( false ) );
    	vfp.add( samples.getSliderControls() );
    	vfp.add( drawWithBlur.getControls() );
    	
    	dolly.addParameterListener( new ParameterListener<Double>() {			
			@Override
			public void parameterChanged(Parameter<Double> parameter) {
				// update the target eye position given the new dolly parameters
				double v = dolly.getValue();
				final Vector3d viewDir = new Vector3d();
				viewDir.sub( lookAtDesired, eyeDesired );
				viewDir.normalize();
				viewDir.scale( v );
				eyeDesired.sub( lookAtDesired, viewDir );
			}
		});
    	focalLength.addParameterListener( new ParameterListener<Double>() {
    		@Override
    		public void parameterChanged(Parameter<Double> parameter) {
    			if ( ignoreParameterChangeCallback ) return;
    			// TODO OBJECTIVE 4: compute fovy, field of view in the y direction

    			
    			double value = 0; // change this!

    			ignoreParameterChangeCallback = true;
    			fovy.setValue(value);
    			ignoreParameterChangeCallback = false;
    		}
		});
    	fovy.addParameterListener( new ParameterListener<Double>() {
    		@Override
    		public void parameterChanged(Parameter<Double> parameter) {
    			if ( ignoreParameterChangeCallback ) return;
    			// TODO OBJECTIVE 4: compute focal length given the field of view in the y direction
    			
    			
    			double value = 0; // change this!
    			
    			ignoreParameterChangeCallback = true;
    			focalLength.setValue( value );
    			ignoreParameterChangeCallback = false;
    		}
		});
    	
    	return vfp.getPanel();
    }
    
    /**
     * Calls glFrustum with appropriate parameters.
     * @param drawable
     * @param i which sample point to use to create a shifted frustum
     */
    public void setupProjection( GLAutoDrawable drawable, int i ) {
    	GL2 gl = drawable.getGL().getGL2();
    	
    	// TODO OBJECTIVE 1: Compute parameters to call glFrustum
    	// TODO OBJECTIVE 7: revisit this function for shifted perspective projection
    	
    }
    
    
    /**
     * Creates a viewing transformation.
     * Note that you may or may not need to know which aperture sample is being drawn
     * @param drawable
     * @param i identifies which aperture sample
     */
    public void setupViewingTransformation( GLAutoDrawable drawable, int i ) {
    	GL2 gl = drawable.getGL().getGL2();

    	// TODO OBJECTIVE 1: Set up the viewing transformation
    	// TODO OBJECTIVE 7: revisit this function for shifted perspective projection, if necessary

    }
    
    /**
     * Draw the focus plane rectangle that exactly fits inside the frustum.
     * This is the rectangle that must stay fixed for all shifted perspective
     * frustums you create for a depth of field blur.
     * (expects to be drawing in coordinates of the camera view frame) 
     * @param drawable
     */
    public void drawFocusPlane( GLAutoDrawable drawable ) {
    	GL2 gl = drawable.getGL().getGL2();

    	// TODO OBJECTIVE 6: Draw the focus plane rectangle

    }

    /** 
     * Draws the sensor plane 
     * (expects to be drawing in coordinates of the camera view frame) 
     */
    public void drawSensorPlane( GLAutoDrawable drawable ) {
	   
    	// TODO OBJECTIVE 2: Draw the sensor plane rectangle
    	
    	
	    GL2 gl = drawable.getGL().getGL2();
	    gl.glColor3f(0,1,0); 
    	gl.glPushMatrix();
    	gl.glDisable( GL2.GL_LIGHTING );
	    gl.glBegin( GL2.GL_LINE_LOOP );
	    // use gl.glVertex3d calls to specify the 4 corners of the rectangle
	    gl.glEnd();
	    gl.glPopMatrix();
    }

    /**
     * Draws samples on the aperture, i.e., the centers of projection 
     * for shifted perspective projections necessary to generate a 
     * depth of field blur.
     * (expects to be drawing in coordinates of the camera view frame) 
     * @param drawable
     */
    public void drawAperture( GLAutoDrawable drawable ) {
    	GL2 gl = drawable.getGL().getGL2();
    	gl.glDisable( GL2.GL_LIGHTING );
    	gl.glPointSize(3f);
    	gl.glBegin( GL.GL_POINTS );
    	final Point2d p = new Point2d();
        double s = getEffectivePupilRadius();
    	for ( int i = 0; i < samples.getValue(); i++ ) {
	    	fpd.get( p, i, samples.getValue() );
	        double ox = s * p.x; // eye offset from center + effective aperture displacement 
	        double oy = s * p.y;
	        gl.glVertex3d( ox, oy, 0 );
    	}
        gl.glEnd();
    }
    
    /** 
     * Computes the radius of displaced sample points to emulate a given f-stop 
     * for the current focal length and focus distance settings.
     * @return
     */
    private double getEffectivePupilRadius() {
	    double fl = focalLength.getValue() / 1000;
	    double fd = -focusDistance; 
	    double f = 1/(1/fd+1/fl);
	    double r = f / fstop.getValue() / 2; // divide by 2 to get radius of effective aperture 
		return r; 
    }
    
}
