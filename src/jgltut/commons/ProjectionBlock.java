package jgltut.commons;

import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class ProjectionBlock implements Bufferable {
    
    public static final int BYTES = Float.BYTES * (16);
    
    public Matrix4f cameraToClipMatrix;
    
    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        
        cameraToClipMatrix.get(buffer);
        buffer.position(buffer.position() + Float.BYTES * 16);
        
        return buffer;
    }
}
