package jgltut.tutorials.tut10;

import jglsdk.jglutil.MousePoles.*;
import jgltut.Tutorial;
import jgltut.commons.ProjectionBlock;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import jgltut.framework.MousePole;
import jgltut.framework.Timer;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

public class VertexPointLighting extends Tutorial {
    
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut10/data/";
        new VertexPointLighting().start(500, 500);
    }
    
    
    @Override
    protected void init() {
        initializePrograms();
        
        cylinderMesh = new Mesh("UnitCylinder.xml");
        planeMesh = new Mesh("LargePlane.xml");
        cubeMesh = new Mesh("UnitCube.xml");
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
        
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
        glEnable(GL_DEPTH_CLAMP);
        
        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.BYTES, GL_DYNAMIC_DRAW);
        
        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.BYTES);
        
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_SPACE:
                        drawColoredCyl = !drawColoredCyl;
                        break;
                    
                    case GLFW_KEY_Y:
                        drawLight = !drawLight;
                        break;
                    
                    case GLFW_KEY_B:
                        lightTimer.togglePause();
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
            if (isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
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
        
        Vector4f lightPosCameraSpace = modelMatrix.transform(new Vector4f(worldLightPos));
        
        glUseProgram(whiteDiffuseColor.theProgram);
        glUniform3fv(whiteDiffuseColor.lightPosUnif, lightPosCameraSpace.get(vec4Buffer));
        glUseProgram(vertexDiffuseColor.theProgram);
        glUniform3fv(vertexDiffuseColor.lightPosUnif, lightPosCameraSpace.get(vec4Buffer));
        
        glUseProgram(whiteDiffuseColor.theProgram);
        glUniform4f(whiteDiffuseColor.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(whiteDiffuseColor.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUseProgram(vertexDiffuseColor.theProgram);
        glUniform4f(vertexDiffuseColor.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(vertexDiffuseColor.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUseProgram(0);
        
        {
            modelMatrix.pushMatrix();
            
            // Render the ground plane.
            {
                modelMatrix.pushMatrix();
                
                glUseProgram(whiteDiffuseColor.theProgram);
                glUniformMatrix4fv(whiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                Matrix3f normMatrix = new Matrix3f(modelMatrix);
                glUniformMatrix3fv(whiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
                planeMesh.render();
                glUseProgram(0);
                
                modelMatrix.popMatrix();
            }
            
            // Render the Cylinder
            {
                modelMatrix.pushMatrix();
                
                modelMatrix.mul(objtPole.calcMatrix());
                
                if (drawColoredCyl) {
                    glUseProgram(vertexDiffuseColor.theProgram);
                    glUniformMatrix4fv(vertexDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                    Matrix3f normMatrix = new Matrix3f(modelMatrix);
                    glUniformMatrix3fv(vertexDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
                    cylinderMesh.render("lit-color");
                } else {
                    glUseProgram(whiteDiffuseColor.theProgram);
                    glUniformMatrix4fv(whiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                    Matrix3f normMatrix = new Matrix3f(modelMatrix);
                    glUniformMatrix3fv(whiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
                    cylinderMesh.render("lit");
                }
                glUseProgram(0);
                
                modelMatrix.popMatrix();
            }
            
            // Render the light
            if (drawLight) {
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
        
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projectionBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        glViewport(0, 0, w, h);
    }
    
    @Override
    protected void update() {
        final float scale = 5;
        
        if (isKeyPressed(GLFW_KEY_J)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightRadius -= 0.05f * lastFrameDuration * scale;
            } else {
                lightRadius -= 0.2f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_L)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightRadius += 0.05f * lastFrameDuration * scale;
            } else {
                lightRadius += 0.2f * lastFrameDuration * scale;
            }
        }
        
        if (isKeyPressed(GLFW_KEY_I)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightHeight += 0.05f * lastFrameDuration * scale;
            } else {
                lightHeight += 0.2f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_K)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightHeight -= 0.05f * lastFrameDuration * scale;
            } else {
                lightHeight -= 0.2f * lastFrameDuration * scale;
            }
        }
        
        
        if (lightRadius < 0.2f) {
            lightRadius = 0.2f;
        }
    }
    
    ////////////////////////////////
    private ProgramData whiteDiffuseColor;
    private ProgramData vertexDiffuseColor;
    private UnlitProgData unlit;
    
    private class ProgramData {
        int theProgram;
        
        int lightPosUnif;
        int lightIntensityUnif;
        int ambientIntensityUnif;
        
        int modelToCameraMatrixUnif;
        int normalModelToCameraMatrixUnif;
    }
    
    private class UnlitProgData {
        int theProgram;
        
        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }
    
    
    private void initializePrograms() {
        whiteDiffuseColor = loadLitProgram("PosVertexLighting_PN.vert", "ColorPassthrough.frag");
        vertexDiffuseColor = loadLitProgram("PosVertexLighting_PCN.vert", "ColorPassthrough.frag");
        unlit = loadUnlitProgram("PosTransform.vert", "UniformColor.frag");
    }
    
    private ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
        
        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
        data.lightPosUnif = glGetUniformLocation(data.theProgram, "lightPos");
        data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");
        data.ambientIntensityUnif = glGetUniformLocation(data.theProgram, "ambientIntensity");
        
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);
        
        return data;
    }
    
    private UnlitProgData loadUnlitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
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
    
    ////////////////////////////////
    private Mesh cylinderMesh;
    private Mesh planeMesh;
    private Mesh cubeMesh;
    
    private float lightHeight = 1.5f;
    private float lightRadius = 1.0f;
    private Timer lightTimer = new Timer(Timer.Type.LOOP, 5.0f);
    
    private boolean drawColoredCyl;
    private boolean drawLight;
    
    
    private Vector4f calcLightPosition() {
        float currTimeThroughLoop = lightTimer.getAlpha();
        
        Vector4f lightPos = new Vector4f(0.0f, lightHeight, 0.0f, 1.0f);
        lightPos.x = (float) (Math.cos(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        lightPos.z = (float) (Math.sin(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        return lightPos;
    }
    
    ////////////////////////////////
    // View / Object setup.
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
    
    
    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
    private ObjectPole objtPole = new ObjectPole(initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole);
    
    ////////////////////////////////
    private final int projectionBlockIndex = 2;
    
    private int projectionUniformBuffer;
}
