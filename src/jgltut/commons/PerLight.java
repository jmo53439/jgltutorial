package jgltut.commons;

import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class PerLight implements Bufferable {
    
    public static final int BYTES = Float.BYTES * (4 + 4);
    
    public Vector4f cameraSpaceLightPos;
    public Vector4f lightIntensity;
    
    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        
        cameraSpaceLightPos.get(buffer);
        buffer.position(buffer.position() + Float.BYTES * 4);
        
        lightIntensity.get(buffer);
        buffer.position(buffer.position() + Float.BYTES * 4);
        
        return buffer;
    }
}
