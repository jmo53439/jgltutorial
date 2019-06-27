package jgltut.commons;

import java.nio.ByteBuffer;

public interface Bufferable {
    
    ByteBuffer get(ByteBuffer buffer);
    
    default ByteBuffer getAndFlip(ByteBuffer buffer) {
    
        buffer.clear();
        get(buffer);
        buffer.flip();
        
        return buffer;
    }
}
