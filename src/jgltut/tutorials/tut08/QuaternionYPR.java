package jgltut.tutorials.tut08;

import jgltut.Tutorial;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class QuaternionYPR extends Tutorial {
    
    private final float frustumScale = calcFrustumScale(20.0f);
    
    private int theProgram;
    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;
    private int baseColorUnif;
    
    private boolean rightMultiply = true;
    
    private Matrix4f cameraToClipMatrix = new Matrix4f();
    private Quaternionf orientation = new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
    
    private Mesh ship;
    
    @Override
    protected void init() {
    
        initializeProgram();
        
        ship = new Mesh("Ship.xml");
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
        
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
           
            if(action == GLFW_PRESS) {
                
                switch(key) {
                    
                    case GLFW_KEY_SPACE:
                        rightMultiply = !rightMultiply;
                        System.out.printf(rightMultiply ? "Right-multiply\n" : "Left-multiply\n");
                        break;
                        
                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
                }
            }
        });
    }
    
    @Override
    protected void display() {
    
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
        MatrixStackf currMatrix = new MatrixStackf();
        currMatrix.translate(0.0f, 0.0f, -200.0f);
        currMatrix.mul(orientation.get(new Matrix4f()));
        
        glUseProgram(theProgram);
    
        currMatrix.scale(3.0f, 3.0f, 3.0f);
        currMatrix.rotateX((float) Math.toRadians(-90.0f));
        
        glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
        glUniformMatrix4fv(modelToCameraMatrixUnif, false, currMatrix.get(mat4Buffer));
    
        ship.render("tint");
        glUseProgram(0);
    }
    
    @Override
    protected void reshape(int w, int h) {
    
        cameraToClipMatrix.m00(frustumScale * (h / (float) w));
        cameraToClipMatrix.m11(frustumScale);
        
        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
        glUseProgram(0);
        glViewport(0, 0, w, h);
    }
    
    @Override
    protected void update() {
    
        final float SMALL_ANGLE_INCREMENT = 9.0f;
        final float scale = 10;
        
        if(isKeyPressed(GLFW_KEY_W)) {
            
            offsetOrientation(new Vector3f(1.0f, 0.0f, 0.0f),
                    SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }
        else if(isKeyPressed(GLFW_KEY_S)) {
            
            offsetOrientation(new Vector3f(1.0f, 0.0f, 0.0f),
                    -SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }
        
        if(isKeyPressed(GLFW_KEY_A)) {
            
            offsetOrientation(new Vector3f(0.0f, 0.0f, 1.0f),
                    SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }
        else if(isKeyPressed(GLFW_KEY_D)) {
            
            offsetOrientation(new Vector3f(0.0f, 0.0f, 1.0f),
                    -SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }
        
        if(isKeyPressed(GLFW_KEY_Q)) {
            
            offsetOrientation(new Vector3f(0.0f, 1.0f, 0.0f),
                    SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }
        else if(isKeyPressed(GLFW_KEY_E)) {
            
            offsetOrientation(new Vector3f(0.0f, 1.0f, 0.0f),
                    -SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }
    }
    
    private void initializeProgram() {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "PosColorLocalTransform.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "ColorMultUniform.frag"));
        theProgram = Framework.createProgram(shaderList);
        
        modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
        baseColorUnif = glGetUniformLocation(theProgram, "baseColor");
        
        float zNear = 1.0f;
        float zFar = 600.0f;
        
        cameraToClipMatrix.m00(frustumScale);
        cameraToClipMatrix.m11(frustumScale);
        cameraToClipMatrix.m22((zFar + zNear) / (zNear - zFar));
        cameraToClipMatrix.m23(-1.0f);
        cameraToClipMatrix.m32((2 * zFar * zNear) / (zNear - zFar));
        
        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
        glUseProgram(0);
    }
    
    private float calcFrustumScale(float fovDeg) {
        
        float fovRad = (float) Math.toRadians(fovDeg);
        return (float)(1.0f / Math.tan(fovRad / 2.0f));
    }
    
    private void offsetOrientation(Vector3f axis, float angDeg) {
        
        float angRad = (float) Math.toRadians(angDeg);
        axis.normalize();
        axis.mul((float) Math.sin(angRad / 2.0f));
        
        float scalar = (float) Math.cos(angRad / 2.0f);
        Quaternionf offset = new Quaternionf(axis.x, axis.y, axis.z, scalar);
        
        if(rightMultiply) {
            
            orientation.mul(offset);
        }
        else {
            
            orientation = offset.mul(orientation);
        }
    
        orientation.normalize();
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut08/data/";
        new QuaternionYPR().start(500, 500);
    }
}
