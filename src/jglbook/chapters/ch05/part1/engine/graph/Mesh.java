package jglbook.chapters.ch05.part1.engine.graph;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {
    
    private final int vaoId;
    private final int posVboId;
    private final int idxVboId;
    private final int vertexCount;
    
    public Mesh(float[] positions, int[] indices) {
    
        FloatBuffer posBuffer = null;
        IntBuffer indicesBuffer = null;
        
        try {
            
            vertexCount = indices.length;
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);
            
            posVboId = glGenBuffers();
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            
            idxVboId = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
            
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
        finally {
            
            if(posBuffer != null)
                MemoryUtil.memFree(posBuffer);
            
            if(indicesBuffer != null)
                MemoryUtil.memFree(indicesBuffer);
        }
    }
    
    public int getVaoId() {
        
        return vaoId;
    }
    
    public int getVertexCount() {
        
        return vertexCount;
    }
    
    public void cleanup() {
        
        glDisableVertexAttribArray(0);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(idxVboId);
        
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
