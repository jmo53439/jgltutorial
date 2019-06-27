package jgltut.commons;

import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.nio.ByteBuffer;

public class UnProjectionBlock implements Bufferable {
    
    public static final int BYTES = Float.BYTES * (16) + Integer.BYTES * (2);
    
    public Matrix4f clipToCameraMatrix;
    public Vector2i windowSize;
    
    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        
        clipToCameraMatrix.get(buffer);
        buffer.position(buffer.position() + Float.BYTES * 16);
        
        windowSize.get(buffer);
        buffer.position(buffer.position() + Integer.BYTES * 2);
        
        return buffer;
    }
}
