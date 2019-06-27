package jgltut.tutorials.tut10;

import jglsdk.jglutil.MousePoles.*;
import jgltut.Tutorial;
import jgltut.commons.ProjectionBlock;
import jgltut.commons.UnProjectionBlock;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import jgltut.framework.MousePole;
import jgltut.framework.Timer;
import org.joml.*;
import org.lwjgl.opengl.GL15;

import java.lang.Math;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

public class FragmentAttenuation extends Tutorial {
    
    private final int projectionBlockIndex = 2;
    private final int unprojectionBlockIndex = 1;
    
    private ProgramData fragWhiteDiffuseColor;
    private ProgramData fragVertexDiffuseColor;
    private UnlitProgData unlit;
    
    private Mesh cylinderMesh;
    private Mesh planeMesh;
    private Mesh cubeMesh;
    
    private float lightHeight = 1.5f;
    private float lightRadius = 1.0f;
    private float lightAttenuation = 1.0f;
    
    private Timer lightTimer = new Timer(Timer.Type.LOOP, 5.0f);
    
    private boolean drawColoredCyl;
    private boolean drawLight;
    private boolean scaleCyl;
    private boolean useRSquare;
    
    private int projectionUniformBuffer;
    private int unprojectionUniformBuffer;
    
    private ViewData initialViewData = new ViewData(
            
            new Vector3f(0.0f, 0.5f, 0.0f),
            new Quaternionf(0.3826834f, 0.0f, 0.0f, 0.92387953f),
            5.0f,
            0.0f
    );
    
    
    private ViewScale viewScale = new ViewScale(
            
            3.0f, 20.0f,
            1.5f, 0.5f,
            0.0f, 0.0f,  // No camera movement.
            90.0f / 250.0f
    );
    
    private ObjectData initialObjectData = new ObjectData(
            
            new Vector3f(0.0f, 0.5f, 0.0f),
            new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
    );
    
    private ViewPole viewPole = new ViewPole(initialViewData,
            viewScale, MouseButtons.MB_LEFT_BTN);
    private ObjectPole objtPole = new ObjectPole(initialObjectData,
            90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole);
    
    
    @Override
    protected void init() {
    
        initializePrograms();
        
        cylinderMesh = new Mesh("UnitCylinder.xml");
        planeMesh = new Mesh("LargePlane.xml");
        cubeMesh = new Mesh("UnitCube.xml");
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
        
        final float depthZNear = 0.0f;
        final float depthZFar = 1.0f;
        
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(depthZNear, depthZFar);
        glEnable(GL_DEPTH_CLAMP);
        
        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.BYTES, GL_DYNAMIC_DRAW);
        
        unprojectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, unprojectionUniformBuffer);
        GL15.glBufferData(GL_UNIFORM_BUFFER, UnProjectionBlock.BYTES, GL_DYNAMIC_DRAW);
        
        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex,
                projectionUniformBuffer, 0, ProjectionBlock.BYTES);
        
        glBindBufferRange(GL_UNIFORM_BUFFER, unprojectionBlockIndex,
                unprojectionUniformBuffer, 0, UnProjectionBlock.BYTES);
        
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
           
            if(action == GLFW_PRESS) {
                
                switch(key) {
                
                    case GLFW_KEY_O:
                        if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                            
                            lightAttenuation *= 1.1f;
                        }
                        else {
                            
                            lightAttenuation *= 1.5;
                        }
                        
                        System.out.printf("Atten: %f\n", lightAttenuation);
                        break;
                        
                    case GLFW_KEY_U:
                        if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                            
                            lightAttenuation /= 1.1f;
                        }
                        else {
                            
                            lightAttenuation /= 1.5f;
                        }
                        
                        System.out.printf("Atten: %f\n", lightAttenuation);
                        break;
                        
                    case GLFW_KEY_Y:
                        drawLight = !drawLight;
                        break;
                        
                    case GLFW_KEY_T:
                        scaleCyl = !scaleCyl;
                        break;
                        
                    case GLFW_KEY_B:
                        lightTimer.togglePause();
                        break;
                        
                    case GLFW_KEY_H:
                        useRSquare = !useRSquare;
                        
                        if(useRSquare) {
                            
                            System.out.printf("Inverse Squared Attenuation\n");
                        }
                        else {
                            
                            System.out.printf("Plain Inverse Attenuation\n");
                        }
                        break;
                        
                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
                }
            }
        });
        
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
           
            boolean pressed = action == GLFW_PRESS;
            glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
            
            int x = (int) mouseBuffer1.get(0);
            int y = (int) mouseBuffer2.get(0);
    
            MousePole.forwardMouseButton(window, viewPole, button, pressed, x, y);
            MousePole.forwardMouseButton(window, objtPole, button, pressed, x, y);
        });
        
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
           
            if(isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
    
                MousePole.forwardMouseMotion(viewPole, (int) xpos, (int) ypos);
                MousePole.forwardMouseMotion(objtPole, (int) xpos, (int) ypos);
            }
        });
        
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
     
            glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
            
            int x = (int) mouseBuffer1.get(0);
            int y = (int) mouseBuffer2.get(0);
    
            MousePole.forwardMouseWheel(window, viewPole, (int) yoffset, x, y);
            MousePole.forwardMouseWheel(window, objtPole, (int) yoffset, x, y);
        });
    }
    
    @Override
    protected void display() {
    
        lightTimer.update(elapsedTime);
        
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());
        
        final Vector4f worldLightPos = calcLightPosition();
        final Vector4f lightPosCameraSpace = modelMatrix.transform(new Vector4f(worldLightPos));
        
        glUseProgram(fragWhiteDiffuseColor.theProgram);
        glUniform4f(fragWhiteDiffuseColor.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(fragWhiteDiffuseColor.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3fv(fragWhiteDiffuseColor.cameraSpaceLightPosUnif, lightPosCameraSpace.get(vec4Buffer));
        glUniform1f(fragWhiteDiffuseColor.lightAttenuationUnif, lightAttenuation);
        glUniform1i(fragWhiteDiffuseColor.useRSquareUnif, useRSquare ? 1 : 0);
        
        glUseProgram(fragVertexDiffuseColor.theProgram);
        glUniform4f(fragVertexDiffuseColor.lightAttenuationUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(fragVertexDiffuseColor.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3fv(fragVertexDiffuseColor.cameraSpaceLightPosUnif, lightPosCameraSpace.get(vec4Buffer));
        glUniform1f(fragVertexDiffuseColor.lightAttenuationUnif, lightAttenuation);
        glUniform1i(fragVertexDiffuseColor.useRSquareUnif, useRSquare ? 1 : 0);
        glUseProgram(0);
        
        {
            modelMatrix.pushMatrix();
            
            // render ground plane
            {
                modelMatrix.pushMatrix();
    
                Matrix3f normMatrix = new Matrix3f(modelMatrix);
                normMatrix.invert().transpose();
                
                glUseProgram(fragWhiteDiffuseColor.theProgram);
                glUniformMatrix4fv(fragWhiteDiffuseColor.modelToCameraMatrixUnif,
                        false, modelMatrix.get(mat4Buffer));
                
                glUniformMatrix3fv(fragWhiteDiffuseColor.normalModelToLightCameraMatrixUnif,
                        false, normMatrix.get(mat3Buffer));
                planeMesh.render();
                glUseProgram(0);
    
                modelMatrix.popMatrix();
            }
            
            // render cylinder
            {
                modelMatrix.pushMatrix();
    
                modelMatrix.mul(objtPole.calcMatrix());
                
                if(scaleCyl)
                    modelMatrix.scale(1.0f, 1.0f, 0.2f);
                
                Matrix3f normMatrix = new Matrix3f(modelMatrix);
                normMatrix.invert().transpose();
                
                if(drawColoredCyl) {
                    
                    glUseProgram(fragVertexDiffuseColor.theProgram);
                    glUniformMatrix4fv(fragVertexDiffuseColor.modelToCameraMatrixUnif,
                            false, modelMatrix.get(mat4Buffer));
                    glUniformMatrix3fv(fragVertexDiffuseColor.normalModelToLightCameraMatrixUnif,
                            false, normMatrix.get(mat3Buffer));
    
                    cylinderMesh.render("lit-color");
                }
                else {
                    
                    glUseProgram(fragWhiteDiffuseColor.theProgram);
                    glUniformMatrix4fv(fragWhiteDiffuseColor.modelToCameraMatrixUnif,
                            false, modelMatrix.get(mat4Buffer));
                    glUniformMatrix3fv(fragWhiteDiffuseColor.normalModelToLightCameraMatrixUnif,
                            false, normMatrix.get(mat3Buffer));
                    
                    cylinderMesh.render("lit");
                }
                
                glUseProgram(0);
    
                modelMatrix.popMatrix();
            }
            
            // render light
            if(drawLight) {
    
                modelMatrix.pushMatrix();
    
                modelMatrix.translate(worldLightPos.x, worldLightPos.y, worldLightPos.z);
                modelMatrix.scale(0.1f, 0.1f, 0.1f);
                
                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                glUniform4f(unlit.objectColorUnif, 0.8078f, 0.8706f, 0.9922f, 1.0f);
                cubeMesh.render("flat");
    
                modelMatrix.popMatrix();
            }
    
            modelMatrix.popMatrix();
        }
    }
    
    @Override
    protected void reshape(int w, int h) {
    
        float zNear = 1.0f;
        float zFar = 1000.0f;
        
        Matrix4f persMatrix = new Matrix4f();
        persMatrix.perspective((float) Math.toRadians(45.0f), (w / (float) h), zNear, zFar);
        
        ProjectionBlock projData = new ProjectionBlock();
        projData.cameraToClipMatrix = persMatrix;
        
        UnProjectionBlock unprojData = new UnProjectionBlock();
        unprojData.clipToCameraMatrix = new Matrix4f(persMatrix).invert();
        unprojData.windowSize = new Vector2i(w, h);
        
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projectionBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, unprojectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, unprojData.getAndFlip(unprojectionBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        glViewport(0, 0, w, h);
    }
    
    @Override
    protected void update() {
    
        final float scale = 5;
    
        if(isKeyPressed(GLFW_KEY_J)) {
            
            if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                
                lightRadius -= 0.05f * lastFrameDuration * scale;
            }
            else {
                
                lightRadius -= 0.2f * lastFrameDuration * scale;
            }
        }
        else if(isKeyPressed(GLFW_KEY_L)) {
            
            if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                
                lightRadius += 0.05f * lastFrameDuration * scale;
            }
            else {
                
                lightRadius += 0.2f * lastFrameDuration * scale;
            }
        }
    
        if(isKeyPressed(GLFW_KEY_I)) {
            
            if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                
                lightHeight += 0.05f * lastFrameDuration * scale;
            }
            else {
                
                lightHeight += 0.2f * lastFrameDuration * scale;
            }
        }
        else if(isKeyPressed(GLFW_KEY_K)) {
            
            if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                
                lightHeight -= 0.05f * lastFrameDuration * scale;
            }
            else {
                
                lightHeight -= 0.2f * lastFrameDuration * scale;
            }
        }
    
        if(lightRadius < 0.2f)
            lightRadius = 0.2f;
        
        if (lightAttenuation < 0.1f)
            lightAttenuation = 0.1f;
    }
    
    private void initializePrograms() {
    
        fragWhiteDiffuseColor = loadLitProgram(
                "FragLightAtten_PN.vert",
                "FragLightAtten.frag");
        fragVertexDiffuseColor = loadLitProgram(
                "FragLightAtten_PCN.vert",
                "FragLightAtten.frag");
        unlit = loadUnlitProgram("PosTransform.vert",
                "UniformColor.frag");
    }
    
    private ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
        
        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram,
                "modelToCameraMatrix");
        data.lightIntensityUnif = glGetUniformLocation(data.theProgram,
                "lightIntensity");
        data.ambientIntensityUnif = glGetUniformLocation(data.theProgram,
                "ambientIntensity");
    
        data.normalModelToLightCameraMatrixUnif = glGetUniformLocation(data.theProgram,
                "normalModelToCameraMatrix");
        data.cameraSpaceLightPosUnif = glGetUniformLocation(data.theProgram,
                "cameraSpaceLightPos");
        data.lightAttenuationUnif = glGetUniformLocation(data.theProgram,
                "lightAttenuation");
        data.useRSquareUnif = glGetUniformLocation(data.theProgram, "bUseRSquare");
        
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);
        
        int unprojectionBlock = glGetUniformBlockIndex(data.theProgram, "UnProjection");
        glUniformBlockBinding(data.theProgram, unprojectionBlock, unprojectionBlockIndex);
        
        return data;
    }
    
    private UnlitProgData loadUnlitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
        
        UnlitProgData data = new UnlitProgData();
        data.theProgram = Framework.createProgram(shaderList);
    
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.objectColorUnif = glGetUniformLocation(data.theProgram, "objectColor");
        
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);
        
        return data;
    }
    
    private Vector4f calcLightPosition() {
    
        float currTimeThroughLoop = lightTimer.getAlpha();
        
        Vector4f lightPos = new Vector4f(0.0f, lightHeight, 0.0f, 1.0f);
        lightPos.x = (float)(Math.cos(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        lightPos.y = (float)(Math.sin(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        
        return lightPos;
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut10/data/";
        new FragmentAttenuation().start(500, 500);
    }
    
    private class ProgramData {
        
        int theProgram;
        
        int modelToCameraMatrixUnif;
        int normalModelToLightCameraMatrixUnif;
        int cameraSpaceLightPosUnif;
        int lightAttenuationUnif;
        int useRSquareUnif;
        
        int lightIntensityUnif;
        int ambientIntensityUnif;
        
    }
    
    private class UnlitProgData {
        
        int theProgram;
        
        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }
}
