package comp557.a2;

import java.nio.ByteBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;

import mintools.viewer.EasyViewer;

/**
 * Emulates the accumulation buffer with main memory using glReadPixels 
 * and the drawing of a textured rectangle.  Useful for Mac OS drives
 * that do not support deprecated glAccum OpenGL functions.
 * @author kry
 */
public class Accum {

	/** Buffer for reading pixels, and can be reused later for texture */
	private ByteBuffer pixels;
	
	/** Data for accumulating pixels */
	private float[] data;
	
	/** Capacity of the allocated pixel buffer */
	private int capacity;

	private Texture texture = new Texture(GL.GL_TEXTURE_2D);
	
	private TextureData textureData;
	
	/**
	 * Zero out the accumulator
	 * @param drawable
	 */
	public void glAccumLoadZero( GLAutoDrawable drawable ) {
		// do some initialization code here for the given window size 
		int w = drawable.getSurfaceWidth();
		int h = drawable.getSurfaceHeight();
		if ( pixels == null || capacity < 3*w*h ) {
			// need a buffer, or a bigger buffer, make one...
			// note that I'm unsure how and if garbage collection will happen on this memory when reallocating
			pixels = ByteBuffer.allocate( 3*w*h );
			data = new float[ 3*w*h ];
			int internalFormat = GL.GL_RGB8;
			int pixelFormat = GL.GL_RGB; 
			// Set up the data... but we'll likely need to call an updateImage each time we change the pixels data
			pixels.rewind();
			textureData = new TextureData(drawable.getGLProfile(),internalFormat,w,h,0,pixelFormat,GL.GL_UNSIGNED_BYTE,false,false,false,pixels,null);
		}
		java.util.Arrays.fill( data, 0 );
	}
	
	/**
	 * Accumulate the current draw buffer 
	 * @param drawable
	 * @param factor
	 */
	public void glAccum( GLAutoDrawable drawable, float factor ) {
		GL2 gl = drawable.getGL().getGL2();
		int w = drawable.getSurfaceWidth();
		int h = drawable.getSurfaceHeight();
		pixels.rewind(); // use the buffer from the beginning!
		gl.glReadPixels(0, 0, w, h, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixels); // should hope that this is the back buffer
		pixels.rewind();
		for ( int i = 0; i < 3*w*h; i++ ) {
			// need the bitwise and to force the byte to be treated as unsigned
			data[i] += (pixels.get() & 0x0ff) * factor;
		}
	}
	
	/**
	 * Draws the accumulated image as a textured rectangle to replace the current draw buffer.
	 * @param drawable
	 */
	public void glAccumReturn( GLAutoDrawable drawable ) {
		GL2 gl = drawable.getGL().getGL2();
		int w = drawable.getSurfaceWidth();
		int h = drawable.getSurfaceHeight();
		pixels.rewind();
		for ( int i = 0; i < 3*w*h; i++ ) {
			pixels.put( (byte) data[i] );
		}

		// update the texture image with the main memory pixels
		pixels.rewind();
		texture.updateImage( gl, textureData );

		EasyViewer.beginOverlay(drawable);
		gl.glDisable( GL.GL_DEPTH_TEST );
		texture.bind(gl);
		texture.enable(gl);

		gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP );
        gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP );
        gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST ); 
        gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST ); 
        gl.glTexEnvf( GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL );

        gl.glColor4f(1,1,1,1);
        
        gl.glBegin( GL2.GL_QUADS );
        gl.glTexCoord2d( 0, 1 ); gl.glVertex3d( 0, 0, 0 );
        gl.glTexCoord2d( 0, 0 ); gl.glVertex3d( 0, h, 0 );
        gl.glTexCoord2d( 1, 0 ); gl.glVertex3d( w, h, 0 );
        gl.glTexCoord2d( 1, 1 ); gl.glVertex3d( w, 0, 0 );
		gl.glEnd();

		texture.disable(gl);
		gl.glEnable( GL.GL_DEPTH_TEST );
		EasyViewer.endOverlay(drawable);
	}
	
}