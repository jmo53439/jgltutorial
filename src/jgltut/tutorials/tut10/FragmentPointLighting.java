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

public class FragmentPointLighting extends Tutorial {
    
    private final int projectionBlockIndex = 2;
    
    private int projectionUniformBuffer;
    
    private ProgramData whiteDiffuseColor;
    private ProgramData vertexDiffuseColor;
    private ProgramData fragWhiteDiffuseColor;
    private ProgramData fragVertexDiffuseColor;
    
    private UnlitProgData unlit;
    
    private Mesh cylinderMesh;
    private Mesh planeMesh;
    private Mesh cubeMesh;
    
    private float lightHeight = 1.0f;
    private float lightRadius = 1.0f;
    private Timer lightTimer = new Timer(Timer.Type.LOOP, 5.0f);
    
    private boolean useFragmentLighting = true;
    private boolean drawColoredCyl;
    private boolean drawLight;
    private boolean scaleCyl;
    
    private ViewData initialViewData = new ViewData(
            new Vector3f(0.0f, 0.5f, 0.0f),
            new Quaternionf(0.3826834f, 0.0f, 0.0f, 0.92387953f),
            5.0f, 0.0f
    );
    
    private ViewScale viewScale = new ViewScale(
            3.0f, 20.0f,
            1.5f, 0.5f,
            0.0f, 0.0f,
            90.0f / 250.0f
    );
    
    private ObjectData initialObjectData = new ObjectData(
            new Vector3f(0.0f, 0.5f, 0.0f),
            new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
    );
    
    private ViewPole viewPole = new ViewPole(
            initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
    private ObjectPole objtPole = new ObjectPole(
            initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole);
    
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
    
        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex,
                projectionUniformBuffer, 0, ProjectionBlock.BYTES);
    
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
           
            if(action == GLFW_PRESS) {
                
                switch(key) {
                    
                    case GLFW_KEY_SPACE:
                        drawColoredCyl = !drawColoredCyl;
                        break;
                        
                    case GLFW_KEY_Y:
                        drawLight = !drawLight;
                        break;
                        
                    case GLFW_KEY_T:
                        scaleCyl = !scaleCyl;
                        break;
                        
                    case GLFW_KEY_H:
                        useFragmentLighting = !useFragmentLighting;
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
        
        ProgramData whiteProgram;
        ProgramData vertColorProgram;
        
        if(useFragmentLighting) {
            
            whiteProgram = fragWhiteDiffuseColor;
            vertColorProgram = fragVertexDiffuseColor;
        }
        else {
            
            whiteProgram = whiteDiffuseColor;
            vertColorProgram = vertexDiffuseColor;
        }
        
        glUseProgram(whiteProgram.theProgram);
        glUniform4f(whiteProgram.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(whiteProgram.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUseProgram(vertColorProgram.theProgram);
        glUniform4f(vertColorProgram.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(vertColorProgram.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUseProgram(0);
        
        {
            modelMatrix.pushMatrix();
            
            {
                modelMatrix.pushMatrix();
                
                glUseProgram(whiteProgram.theProgram);
                glUniformMatrix4fv(whiteProgram.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
    
                Matrix4f invTransform = new Matrix4f(modelMatrix).invert();
                Vector4f lightPosModelSpace = new Matrix4f(invTransform).transform(lightPosCameraSpace);
                glUniform3fv(whiteProgram.modelSpaceLightPosUnif, lightPosModelSpace.get(vec4Buffer));
    
                planeMesh.render();
                glUseProgram(0);
    
                modelMatrix.popMatrix();
            }
            
            {
                modelMatrix.pushMatrix();
                modelMatrix.mul(objtPole.calcMatrix());
                
                if(scaleCyl)
                    modelMatrix.scale(1.0f, 1.0f, 0.2f);
                
                Matrix4f invTransform = new Matrix4f(modelMatrix).invert();
                Vector4f lightPosModelSpace = new Matrix4f(invTransform).transform(lightPosCameraSpace);
                
                if(drawColoredCyl) {
                    
                    glUseProgram(vertColorProgram.theProgram);
                    glUniformMatrix4fv(vertColorProgram.modelToCameraMatrixUnif,
                            false, modelMatrix.get(mat4Buffer));
                    glUniform3fv(vertColorProgram.modelSpaceLightPosUnif, lightPosModelSpace.get(vec4Buffer));
    
                    cylinderMesh.render("lit-color");
                }
                else {
                    
                    glUseProgram(whiteProgram.theProgram);
                    glUniformMatrix4fv(whiteProgram.modelToCameraMatrixUnif,
                            false, modelMatrix.get(mat4Buffer));
                    glUniform3fv(whiteProgram.modelSpaceLightPosUnif, lightPosModelSpace.get(vec4Buffer));
    
                    cylinderMesh.render("lit");
                }
                
                glUseProgram(0);
    
                modelMatrix.popMatrix();
            }
            
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
        
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projectionBlockBuffer));
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
    
        if (lightRadius < 0.2f)
            lightRadius = 0.2f;
    }
    
    private void initializePrograms() {
    
        whiteDiffuseColor = loadLitProgram("ModelPosVertexLighting_PN.vert",
                "ColorPassthrough.frag");
        vertexDiffuseColor = loadLitProgram("ModelPosVertexLighting_PCN.vert",
                "ColorPassthrough.frag");
        fragWhiteDiffuseColor = loadLitProgram("FragmentLighting_PN.vert",
                "FragmentLighting.frag");
        fragVertexDiffuseColor = loadLitProgram("FragmentLighting_PCN.vert",
        "FragmentLighting.frag");
        unlit = loadUnlitProgram("PosTransform.vert", "UniformColor.frag");
    }
    
    private ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
        
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
    
        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
    
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.modelSpaceLightPosUnif = glGetUniformLocation(data.theProgram, "modelSpaceLightPos");
        data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");
        data.ambientIntensityUnif = glGetUniformLocation(data.theProgram, "imbientIntensity");
        
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);
        
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
        lightPos.z = (float)(Math.sin(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        
        return lightPos;
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut10/data/";
        new FragmentPointLighting().start(500, 500);
    }
    
    private class ProgramData {
        
        int theProgram;
        int modelSpaceLightPosUnif;
        int modelToCameraMatrixUnif;
        int lightIntensityUnif;
        int ambientIntensityUnif;
        
    }
    
    private class UnlitProgData {
        
        int theProgram;
        
        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }
}
