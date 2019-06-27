package jgltut.tutorials.tut06;

import jgltut.Tutorial;
import jgltut.framework.Framework;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Rotation extends Tutorial {
    
    private int theProgram;
    private int vertexBufferObject;
    private int indexBufferObject;
    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;
    private int vao;
    
    private Matrix4f cameraToClipMatrix = new Matrix4f();
    
    private Instance instanceList[] = {
    
            new NullRotation(new Vector3f(0.0f, 0.0f, -25.0f)),
            new RotateX(new Vector3f(-5.0f, -5.0f, -25.0f)),
            new RotateY(new Vector3f(-5.0f, 5.0f, -25.0f)),
            new RotateZ(new Vector3f(5.0f, 5.0f, -25.0f)),
            new RotateAxis(new Vector3f(5.0f, -5.0f, -25.0f))
            
    };
    
    private final float frustumScale = calcFrustumScale(45.0f);
    
    private final float vertexData[] = {
            
            +1.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,
            
            -1.0f, -1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,
            
            0.0f, 1.0f, 0.0f, 1.0f,  // GREEN_COLOR
            0.0f, 0.0f, 1.0f, 1.0f,  // BLUE_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,  // RED_COLOR
            0.5f, 0.5f, 0.0f, 1.0f,  // BROWN_COLOR
            
            0.0f, 1.0f, 0.0f, 1.0f,  // GREEN_COLOR
            0.0f, 0.0f, 1.0f, 1.0f,  // BLUE_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,  // RED_COLOR
            0.5f, 0.5f, 0.0f, 1.0f   // BROWN_COLOR
    };
    
    private final short indexData[] = {
            
            0, 1, 2,
            1, 0, 3,
            2, 3, 0,
            3, 2, 1,
            
            5, 4, 6,
            4, 5, 7,
            7, 6, 4,
            6, 7, 5
    };
    
    @Override
    protected void init() {
    
        initializeProgram();
        initializeVertexBuffer();
        
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        final int numberOfVertices = 8;
        int colorDataOffset = Float.BYTES * 3 * numberOfVertices;
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
        
        glBindVertexArray(0);
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
        
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
    }
    
    @Override
    protected void display() {
    
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        glUseProgram(theProgram);
        glBindVertexArray(vao);
        
        for(Instance currInst : instanceList) {
            
            final Matrix4f transformMatrix = currInst.constructMatrix(elapsedTime);
            glUniformMatrix4fv(modelToCameraMatrixUnif, false, transformMatrix.get(mat4Buffer));
            glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
        }
        
        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    @Override
    protected void reshape(int w, int h) {
    
        cameraToClipMatrix.m00(frustumScale / (w / (float) h));
        cameraToClipMatrix.m11(frustumScale);
    
        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
        glUseProgram(0);
    
        glViewport(0, 0, w, h);
    }
    
    @Override
    protected void update() {
    
    }
    
    private void initializeProgram() {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "PosColorLocalTransform.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "ColorPassthrough.frag"));
        theProgram = Framework.createProgram(shaderList);
        
        modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
        
        float zNear = 1.0f;
        float zFar = 61.0f;
        cameraToClipMatrix.m00(frustumScale);
        cameraToClipMatrix.m11(frustumScale);
        cameraToClipMatrix.m22((zFar + zNear) / (zNear - zFar));
        cameraToClipMatrix.m23(-1.0f);
        cameraToClipMatrix.m32((2 * zFar * zNear) / (zNear - zFar));
        
        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
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
    
    private float calcFrustumScale(float fovDeg) {
        
        float fovRad = (float) Math.toRadians(fovDeg);
        return (float)(1.0f / Math.tan(fovRad / 2.0f));
    }
    
    private float computeAngleRad(float elapsedTime, float loopDuration) {
        
        final float scale = 3.14159f * 2.0f / loopDuration;
        float currTimeThroughLoop = elapsedTime % loopDuration;
        
        return currTimeThroughLoop * scale;
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut06/data/";
        new Rotation().start(500, 500);
    }
    
    private class NullRotation extends Instance {
    
        NullRotation(Vector3f offset) {
            
            super(offset);
        }
        
        @Override
        Matrix3f calcRotation(float elapsedTime) {
        
            return new Matrix3f();
        }
    }
    
    private class RotateX extends Instance {
    
        RotateX(Vector3f offset) {
            
            super(offset);
        }
        
        @Override
        Matrix3f calcRotation(float elapsedTime) {
        
            float angRad = computeAngleRad(elapsedTime, 3.0f);
            float cos = (float) Math.cos(angRad);
            float sin = (float) Math.sin(angRad);
            
            Matrix3f theMat = new Matrix3f();
            theMat.m11(cos);
            theMat.m21(-sin);
            theMat.m12(sin);
            theMat.m22(cos);
            
            return theMat;
        }
    }
    
    private class RotateY extends Instance {
    
        RotateY(Vector3f offset) {
            
            super(offset);
        }
        
        @Override
        Matrix3f calcRotation(float elapsedTime) {
        
            float angRad = computeAngleRad(elapsedTime, 2.0f);
            float cos = (float) Math.cos(angRad);
            float sin = (float) Math.sin(angRad);
            
            Matrix3f theMat = new Matrix3f();
            theMat.m00(cos);
            theMat.m20(sin);
            theMat.m02(-sin);
            theMat.m22(cos);
            
            return theMat;
        }
    }
    
    private class RotateZ extends  Instance {
    
        RotateZ(Vector3f offset) {
        
            super(offset);
        }
        
        @Override
        Matrix3f calcRotation(float elapsedTime) {
    
            float angRad = computeAngleRad(elapsedTime, 2.0f);
            float cos = (float) Math.cos(angRad);
            float sin = (float) Math.sin(angRad);
    
            Matrix3f theMat = new Matrix3f();
            theMat.m00(cos);
            theMat.m10(-sin);
            theMat.m01(sin);
            theMat.m11(cos);
    
            return theMat;
        }
    }
    
    private class RotateAxis extends Instance {
    
        RotateAxis(Vector3f offset) {
            
            super(offset);
        }
        
        @Override
        Matrix3f calcRotation(float elapsedTime) {
    
            float angRad = computeAngleRad(elapsedTime, 2.0f);
            float cos = (float) Math.cos(angRad);
            float invCos = 1.0f - cos;
            float sin = (float) Math.sin(angRad);
            
            Vector3f axis = new Vector3f(1.0f, 1.0f, 1.0f);
            axis.normalize();
            
            Matrix3f theMat = new Matrix3f();
            
            theMat.m00((axis.x * axis.x) + ((1 - axis.x * axis.x) * cos));
            theMat.m10(axis.x * axis.y * (invCos) - (axis.z * sin));
            theMat.m20(axis.x * axis.z * (invCos) + (axis.y * sin));
            theMat.m01(axis.x * axis.y * (invCos) + (axis.z * sin));
            theMat.m11((axis.y * axis.y) + ((1 - axis.y * axis.y) * cos));
            theMat.m21(axis.y * axis.z * (invCos) - (axis.x * sin));
            theMat.m02(axis.x * axis.z * (invCos) - (axis.y * sin));
            theMat.m12(axis.y * axis.z * (invCos) + (axis.x * sin));
            theMat.m22((axis.z * axis.z) + ((1 - axis.z * axis.z) * cos));
            
            return theMat;
        }
    }
    
    private abstract class Instance {
        
        private Vector3f offset;
        
        Instance(Vector3f offset) {
            
            this.offset = offset;
        }
        
        Matrix4f constructMatrix(float elapsedTime) {
            
            final Matrix3f rotMatrix = calcRotation(elapsedTime);
            Matrix4f theMat = new Matrix4f(rotMatrix);
            theMat.setTranslation(offset);
            
            return theMat;
        }
        
        abstract Matrix3f calcRotation(float elapsedTime);
    }
}
