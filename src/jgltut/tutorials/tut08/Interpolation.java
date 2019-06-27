package jgltut.tutorials.tut08;

import jgltut.Tutorial;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import jgltut.framework.Timer;
import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Interpolation extends Tutorial {
    
    private final float frustumScale = calcFrustumScale(20.0f);
    
    private int theProgram;
    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;
    private int baseColorUnif;
    
    private Matrix4f cameraToClipMatrix = new Matrix4f();
    
    private Mesh ship;
    private Orientation orient = new Orientation();
    
    private Quaternionf orients[] = {
        
            new Quaternionf(0.7071f, 0.0f, 0.0f, 0.7071f),
            new Quaternionf(0.5f, -0.5f, 0.5f, 0.5f),
            new Quaternionf(-0.7892f, -0.3700f, -0.02514f, -0.4895f),
            new Quaternionf(0.7892f, 0.3700f, 0.02514f, 0.4895f),
        
            new Quaternionf(-0.1591f, -0.7991f, -0.4344f, 0.3840f),
            new Quaternionf(0.5208f, 0.6483f, 0.0410f, 0.5537f),
            new Quaternionf(0.0f, 1.0f, 0.0f, 0.0f)
    };
    
    private int orientKeys[] = {
        
            GLFW_KEY_Q,
            GLFW_KEY_W,
            GLFW_KEY_E,
            GLFW_KEY_R,
        
            GLFW_KEY_T,
            GLFW_KEY_Y,
            GLFW_KEY_U
    };
    
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
                
                for(int orientIndex = 0; orientIndex < orientKeys.length; orientIndex++) {
                    
                    if(key == orientKeys[orientIndex]) {
                        
                        applyOrientation(orientIndex);
                        break;
                    }
                }
                
                switch(key) {
                    
                    case GLFW_KEY_SPACE:
                        boolean slerp = orient.toggleSlerp();
                        System.out.printf(slerp ? "Slerp\n" : "Lerp\n");
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
    
        orient.updateTime();
        
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
        MatrixStackf currMatrix = new MatrixStackf();
        currMatrix.translate(0.0f, 0.0f, -200.0f);
        currMatrix.mul(orient.getOrient().get(new Matrix4f()));
        
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
    
    private void applyOrientation(int orientationIndex) {
    
        if(!orient.isAnimating)
            orient.animateToOrient(orientationIndex);
    }
    
    private Quaternionf slerp(Quaternionf v0, Quaternionf v1, float alpha) {
    
        final float DOT_THRESHOLD = 0.9995f;
        float dot = v0.dot(v1);
        
        if(dot > DOT_THRESHOLD)
            return lerp(v0, v1, alpha);
        
        dot = Math.min(Math.max(dot, -1.0f), 1.0f);
        float theta_0 = (float) Math.acos(dot);
        float theta = theta_0 * alpha;
        
        Vector4f p = vectorize(v0).mul(dot).negate();
        Vector4f v2 = vectorize(v1).add(p).normalize();
        Vector4f a = vectorize(v0).mul((float) Math.cos(theta));
        Vector4f b = new Vector4f(v2).mul((float) Math.sin(theta));
        Vector4f res = a.add(b);
        
        return new Quaternionf(res.x, res.y, res.z, res.w);
    }
    
    private Quaternionf lerp(Quaternionf v0, Quaternionf v1, float alpha) {
    
        Vector4f start = vectorize(v0);
        Vector4f end = vectorize(v1);
        Vector4f interp = new Vector4f(start).lerp(end, alpha);
        
        System.out.printf("alpha: %f, (%f, %f, %f, %f)\n",
                alpha, interp.w, interp.x, interp.y, interp.z);
        interp.normalize();
        
        return new Quaternionf(interp.x, interp.y, interp.z, interp.w);
    }
    
    private Vector4f vectorize(Quaternionf q) {
    
        Vector4f vec = new Vector4f();
        vec.x = q.x;
        vec.y = q.y;
        vec.z = q.z;
        vec.w = q.w;
        
        return vec;
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut08/data/";
        new Interpolation().start(500, 500);
    }
    
    private class Orientation {
        
        boolean isAnimating;
        boolean slerp;
        int currOrientIndex;
        
        Animation anim = new Animation();
        
        class Animation {
            
            int finalOrientIndex;
            Timer currTimer;
            
            boolean updateTime() {
                
                return currTimer.update(elapsedTime);
            }
            
            void startAnimation(int destinationIndex, float duration) {
                
                finalOrientIndex = destinationIndex;
                currTimer = new Timer(Timer.Type.SINGLE, duration);
            }
            
            Quaternionf getOrient(Quaternionf initial, boolean slerp) {
                
                if(slerp) {
                    
                    return slerp(initial, orients[finalOrientIndex], currTimer.getAlpha());
                }
                else {
                    
                    return lerp(initial, orients[finalOrientIndex], currTimer.getAlpha());
                }
            }
            
            int getFinalIndex() {
                
                return finalOrientIndex;
            }
        }
        
        void updateTime() {
        
            if(isAnimating) {
                
                boolean isFinished = anim.updateTime();
                
                if(isFinished) {
                    
                    isAnimating = false;
                    currOrientIndex = anim.getFinalIndex();
                }
            }
        }
        
        void animateToOrient(int destinationIndex) {
        
            if(currOrientIndex == destinationIndex)
                return;
    
            anim.startAnimation(destinationIndex, 1.0f);
            isAnimating = true;
        }
        
        boolean toggleSlerp() {
        
            slerp = !slerp;
            return slerp;
        }
        
        Quaternionf getOrient() {
        
            if(isAnimating) {
                
                return anim.getOrient(orients[currOrientIndex], slerp);
            }
            else {
                
                return orients[currOrientIndex];
            }
        }
        
        boolean isAnimating() {
            
            return isAnimating;
        }
    }
}
