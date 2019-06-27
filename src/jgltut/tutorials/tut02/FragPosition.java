package jgltut.tutorials.tut02;

import jgltut.Tutorial;
import jgltut.framework.Framework;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class FragPosition extends Tutorial {
    
    private int theProgram;
    private int vertexBufferObject;
    
    private final float[] vertexData = {
        
            0.75f, 0.75f, 0.0f, 1.0f,
            0.75f, -0.75f, 0.0f, 1.0f,
            -0.75f, -0.75f, 0.0f, 1.0f
    };
    
    @Override
    protected void init() {
    
        initializeProgram();
        initializeVertexBuffer();
        
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
    }
    
    @Override
    protected void display() {
    
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        glUseProgram(theProgram);
        
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
        
        glDrawArrays(GL_TRIANGLES, 0, 3);
        
        glDisableVertexAttribArray(0);
        glUseProgram(0);
    }
    
    @Override
    protected void reshape(int w, int h) {
    
        glViewport(0, 0, w, h);
    }
    
    @Override
    protected void update() {
    
    }
    
    private void initializeProgram() {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "FragPosition.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "FragPosition.frag"));
        theProgram = Framework.createProgram(shaderList);
    }
    
    private void initializeVertexBuffer() {
    
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(vertexData.length);
        vertexDataBuffer.put(vertexData);
        vertexDataBuffer.flip();
        
        vertexBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut02/data/";
        new FragPosition().start(500, 500);
    }
}
