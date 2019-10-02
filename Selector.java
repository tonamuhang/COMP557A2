package comp557.a2;

import java.awt.Point;
import java.nio.IntBuffer;

import javax.vecmath.Point3d;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

/**
 * Helper class to identify the location of a pixel in world space
 * @author kry
 */
public class Selector {

	/** 
	 * The selected point, which will be on the far plane if there was
	 * nothing drawn at the pixel. 
	 */
	Point3d selectedPoint = new Point3d();
	
    /** For reading the depth of a visible pixel */
	private IntBuffer depthPixels = IntBuffer.allocate( 1 );

    /**
     * Selects and gets the world position of the pixel at the mouse point.
     * @param drawable
     * @param p	The mouse point
     */
    boolean select( GLAutoDrawable drawable, Point p ) {
    	GL2 gl = drawable.getGL().getGL2();
    	int h = drawable.getSurfaceHeight();
		int x = p.x;
		int y = h - p.y;
		depthPixels.rewind();
		gl.glReadPixels( x, y, 1, 1, GL2.GL_DEPTH_COMPONENT, GL.GL_UNSIGNED_INT, depthPixels );
		depthPixels.rewind();
		int zint = depthPixels.get(0);
		// System.out.println(Integer.toHexString(zint));
		// convert to [0,1] interval for unproject
		// bit gross, but necesary to go through a long for unsigned int
        long zL = Integer.toUnsignedLong( zint );
        float z = ( (float) zL ) / 0xffffffffL; 
    	unproject( drawable, p.x, h-p.y, z, selectedPoint );
    	return true;	       
    }

    /**
     * Unprojects the selected point, and stores the result in p.
     * @param drawable
     * @param x
     * @param y
     * @param z
     * @param p
     */
    private void unproject( GLAutoDrawable drawable, float x, float y, float z, Point3d p ) {
	    final int[] viewport = new int[4];
    	final float[] modelview = new float[16];
    	final float[] projection = new float[16];
        final float[] position = new float[4];
    	GL2 gl = drawable.getGL().getGL2();
	    gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        gl.glGetFloatv( GL2.GL_MODELVIEW_MATRIX, modelview, 0 );
        gl.glGetFloatv( GL2.GL_PROJECTION_MATRIX, projection, 0 );
        final GLU glu = new GLU();
        glu.gluUnProject( x, y, z, modelview, 0, projection, 0, viewport, 0, position, 0 );
        p.set( position[0], position[1], position[2] );
    }
}
