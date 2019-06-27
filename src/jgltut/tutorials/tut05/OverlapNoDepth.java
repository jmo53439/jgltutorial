package jgltut.tutorials.tut05;

import jgltut.Tutorial;
import jgltut.framework.Framework;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_BACK;
import static org.lwjgl.opengl.GL15.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL15.GL_CULL_FACE;
import static org.lwjgl.opengl.GL15.GL_CW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glClear;
import static org.lwjgl.opengl.GL15.glClearColor;
import static org.lwjgl.opengl.GL15.glCullFace;
import static org.lwjgl.opengl.GL15.glDrawElements;
import static org.lwjgl.opengl.GL15.glEnable;
import static org.lwjgl.opengl.GL15.glFrontFace;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glViewport;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class OverlapNoDepth extends Tutorial {
    
    private final float TOP_EXTENT = 0.20f;
    private final float MIDDLE_EXTENT = 0.0f;
    private final float BOTTOM_EXTENT = -TOP_EXTENT;
    private final float FRONT_EXTENT = -1.25f;
    private final float REAR_EXTENT = -1.75f;
    private final float RIGHT_EXTENT = 0.8f;
    private final float LEFT_EXTENT = -RIGHT_EXTENT;
    
    private int theProgram;
    private int offsetUniform;
    private int perspectiveMatrixUnif;
    private int vertexBufferObject;
    private int indexBufferObject;
    private int vaoObject1;
    private int vaoObject2;
    
    private float perspectiveMatrix[];
    private final float frustumScale = 1.0f;
    
    private final float vertexData[] = {
            
            // Object 1 positions
            LEFT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            LEFT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            
            LEFT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,
            LEFT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,
            
            LEFT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            LEFT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            LEFT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,
            
            RIGHT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            RIGHT_EXTENT, MIDDLE_EXTENT, FRONT_EXTENT,
            RIGHT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,
            
            LEFT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,
            LEFT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            RIGHT_EXTENT, TOP_EXTENT, REAR_EXTENT,
            RIGHT_EXTENT, BOTTOM_EXTENT, REAR_EXTENT,
            
            //  0, 2, 1,
            //  3, 2, 0,
            
            // Object 2 positions
            TOP_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            MIDDLE_EXTENT, RIGHT_EXTENT, FRONT_EXTENT,
            MIDDLE_EXTENT, LEFT_EXTENT, FRONT_EXTENT,
            TOP_EXTENT, LEFT_EXTENT, REAR_EXTENT,
            
            BOTTOM_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            MIDDLE_EXTENT, RIGHT_EXTENT, FRONT_EXTENT,
            MIDDLE_EXTENT, LEFT_EXTENT, FRONT_EXTENT,
            BOTTOM_EXTENT, LEFT_EXTENT, REAR_EXTENT,
            
            TOP_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            MIDDLE_EXTENT, RIGHT_EXTENT, FRONT_EXTENT,
            BOTTOM_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            
            TOP_EXTENT, LEFT_EXTENT, REAR_EXTENT,
            MIDDLE_EXTENT, LEFT_EXTENT, FRONT_EXTENT,
            BOTTOM_EXTENT, LEFT_EXTENT, REAR_EXTENT,
            
            BOTTOM_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            TOP_EXTENT, RIGHT_EXTENT, REAR_EXTENT,
            TOP_EXTENT, LEFT_EXTENT, REAR_EXTENT,
            BOTTOM_EXTENT, LEFT_EXTENT, REAR_EXTENT,
            
            // Object 1 colors
            0.75f, 0.75f, 1.0f, 1.0f,   // BLUE_COLOR
            0.75f, 0.75f, 1.0f, 1.0f,
            0.75f, 0.75f, 1.0f, 1.0f,
            0.75f, 0.75f, 1.0f, 1.0f,
            
            0.0f, 0.5f, 0.0f, 1.0f,     // GREEN_COLOR
            0.0f, 0.5f, 0.0f, 1.0f,
            0.0f, 0.5f, 0.0f, 1.0f,
            0.0f, 0.5f, 0.0f, 1.0f,
            
            1.0f, 0.0f, 0.0f, 1.0f,     // RED_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            
            0.8f, 0.8f, 0.8f, 1.0f,     // GREY_COLOR
            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f,
            
            0.5f, 0.5f, 0.0f, 1.0f,     // BROWN_COLOR
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,
            
            // Object 2 colors
            1.0f, 0.0f, 0.0f, 1.0f,     // RED_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            
            0.5f, 0.5f, 0.0f, 1.0f,     // BROWN_COLOR
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,
            
            0.0f, 0.5f, 0.0f, 1.0f,     // GREEN_COLOR
            0.0f, 0.5f, 0.0f, 1.0f,
            0.0f, 0.5f, 0.0f, 1.0f,
            
            0.75f, 0.75f, 1.0f, 1.0f,   // BLUE_COLOR
            0.75f, 0.75f, 1.0f, 1.0f,
            0.75f, 0.75f, 1.0f, 1.0f,
            
            0.8f, 0.8f, 0.8f, 1.0f,     // GREY_COLOR
            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f
    };
    
    private final short indexData[] = {
            
            0, 2, 1,
            3, 2, 0,
            
            4, 5, 6,
            6, 7, 4,
            
            8, 9, 10,
            11, 13, 12,
            
            14, 16, 15,
            17, 16, 14
    };
    
    @Override
    protected void init() {
    
        initializeProgram();
        initializeVertexBuffer();
        initializeVertexArrayObjects();
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
    }
    
    @Override
    protected void display() {
    
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        glUseProgram(theProgram);
        
        glBindVertexArray(vaoObject1);
        glUniform3f(offsetUniform, 0.0f, 0.0f, 0.0f);
        glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
        
        glBindVertexArray(vaoObject2);
        glUniform3f(offsetUniform, 0.0f, 0.0f, -1.0f);
        glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
        
        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    @Override
    protected void reshape(int w, int h) {
    
        perspectiveMatrix[0] = frustumScale * (h / (float) w);
        perspectiveMatrix[5] = frustumScale;
    
        FloatBuffer perspectiveMatrixBuffer = BufferUtils.createFloatBuffer(perspectiveMatrix.length);
        perspectiveMatrixBuffer.put(perspectiveMatrix);
        perspectiveMatrixBuffer.flip();
    
        glUseProgram(theProgram);
        glUniformMatrix4fv(perspectiveMatrixUnif, false, perspectiveMatrixBuffer);
        glUseProgram(0);
    
        glViewport(0, 0, w, h);
    }
    
    @Override
    protected void update() {
    
    }
    
    private void initializeProgram() {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "Standard.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "Standard.frag"));
        theProgram = Framework.createProgram(shaderList);
    
        offsetUniform = glGetUniformLocation(theProgram, "offset");
        perspectiveMatrixUnif = glGetUniformLocation(theProgram, "perspectiveMatrix");
    
        float zNear = 1.0f;
        float zFar = 3.0f;
        perspectiveMatrix = new float[16];
        perspectiveMatrix[0] = frustumScale;
        perspectiveMatrix[5] = frustumScale;
        perspectiveMatrix[10] = (zFar + zNear) / (zNear - zFar);
        perspectiveMatrix[14] = (2 * zFar * zNear) / (zNear - zFar);
        perspectiveMatrix[11] = -1.0f;
    
        FloatBuffer perspectiveMatrixBuffer = BufferUtils.createFloatBuffer(perspectiveMatrix.length);
        perspectiveMatrixBuffer.put(perspectiveMatrix);
        perspectiveMatrixBuffer.flip();
    
        glUseProgram(theProgram);
        glUniformMatrix4fv(perspectiveMatrixUnif, false, perspectiveMatrixBuffer);
        glUseProgram(0);
    }
    
    private void initializeVertexBuffer() {
    
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(vertexData.length);
        vertexDataBuffer.put(vertexData);
        vertexDataBuffer.flip();
    
        vertexBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    
        ShortBuffer indexDataBuffer = BufferUtils.createShortBuffer(indexData.length);
        indexDataBuffer.put(indexData);
        indexDataBuffer.flip();
    
        indexBufferObject = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    private void initializeVertexArrayObjects() {
    
        vaoObject1 = glGenVertexArrays();
        glBindVertexArray(vaoObject1);
        
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        
        final int numberOfVertices = 36;
        int colorDataOffset = Float.BYTES * 3 * numberOfVertices;
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
        
        glBindVertexArray(0);
        
        vaoObject2 = glGenVertexArrays();
        glBindVertexArray(vaoObject2);
        
        // use the same buffer objet previously bound to GL_ARRAY_BUFFER
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        
        int posDataOffset = Float.BYTES * 3 * (numberOfVertices / 2);
        colorDataOffset += Float.BYTES * 4 * (numberOfVertices / 2);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, posDataOffset);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
        
        glBindVertexArray(0);
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut05/data/";
        new OverlapNoDepth().start(500, 500);
    }
}
