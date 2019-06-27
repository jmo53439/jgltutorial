package jgltut.tutorials.tut03;

import jgltut.Tutorial;
import jgltut.framework.Framework;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class VertCalcOffset extends Tutorial {
    
    private int theProgram;
    private int positionBufferObject;
    private int elapsedTimeUniform;
    
    private final float[] vertexPositions = {
            
            0.25f, 0.25f, 0.0f, 1.0f,
            0.25f, -0.25f, 0.0f, 1.0f,
            -0.25f, -0.25f, 0.0f, 1.0f
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
        
        glUniform1f(elapsedTimeUniform, elapsedTime);
        
        glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
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
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "CalcOffset.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "Standard.frag"));
        theProgram = Framework.createProgram(shaderList);
        
        elapsedTimeUniform = glGetUniformLocation(theProgram, "time");
        
        glUseProgram(theProgram);
        
        int loopDurationUnf = glGetUniformLocation(theProgram, "loopDuration");
        glUniform1f(loopDurationUnf, 5.0f);
        glUseProgram(0);
    }
    
    private void initializeVertexBuffer() {
    
        FloatBuffer vertexPositionsBuffer = BufferUtils.createFloatBuffer(vertexPositions.length);
        vertexPositionsBuffer.put(vertexPositions);
        vertexPositionsBuffer.flip();
    
        positionBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexPositionsBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut03/data/";
        new VertCalcOffset().start(500, 500);
    }
}
