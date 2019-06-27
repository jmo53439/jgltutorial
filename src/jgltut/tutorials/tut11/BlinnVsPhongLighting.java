package jgltut.tutorials.tut11;

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

public class BlinnVsPhongLighting extends Tutorial {
    
    private final Vector4f darkColor = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
    private final Vector4f lightColor = new Vector4f(1.0f);
    
    private LightingModel lightModel = LightingModel.BLINN_SPECULAR;
    private MaterialParams matParams = new MaterialParams();
    
    private final int projectionBlockIndex = 2;
    
    private int projectionUniformBuffer;
    
    private final String[] lightModelNames = {
            
            "Phong Specular.",
            "Phong Only.",
            "Blinn Specular.",
            "Blinn Only."
    };
    
    private UnlitProgData unlit;
    
    private Mesh cylinderMesh;
    private Mesh planeMesh;
    private Mesh cubeMesh;
    
    private float lightHeight = 1.5f;
    private float lightRadius = 1.0f;
    
    private Timer lightTimer = new Timer(Timer.Type.LOOP, 5.0f);
    
    private boolean drawColoredCyl;
    private boolean drawLightSource;
    private boolean scaleCyl;
    private boolean drawDark;
    
    private ProgramPairs[] programs = new ProgramPairs[LightingModel.MAX_LIGHTING_MODEL.ordinal()];
    
    private ShaderPairs[] shaderFileNAmes = new ShaderPairs[] {
            
            new ShaderPairs(
                    "PN.vert",
                    "PCN.vert",
                    "PhongLighting.frag"),
            new ShaderPairs(
                    "PN.vert",
                    "PCN.vert",
                    "PhongOnly.frag"),
            new ShaderPairs(
                    "PN.vert",
                    "PCN.vert",
                    "BlinnLighting.frag"),
            new ShaderPairs(
                    "PN.vert",
                    "PCN.vert",
                    "BlinnOnly.frag")
    };
    
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
    
    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
    private ObjectPole objtPole = new ObjectPole(initialObjectData, 90.0f / 250.0f,
            MouseButtons.MB_RIGHT_BTN, viewPole);
    
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
        
        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex,
                projectionUniformBuffer, 0, ProjectionBlock.BYTES);
        
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
           
            if(action == GLFW_PRESS) {
                
                switch(key) {
                    
                    case GLFW_KEY_SPACE:
                        drawColoredCyl = !drawColoredCyl;
                        break;
                        
                    case GLFW_KEY_O:
                        if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
    
                            matParams.increment(false);
                        }
                        else {
    
                            matParams.increment(true);
                        }
                        
                        System.out.printf("Shiny: %f\n", (float) matParams.getSpecularValue());
                        break;
                        
                    case GLFW_KEY_U:
                        if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
    
                            matParams.decrement(false);
                        }
                        else {
    
                            matParams.decrement(true);
                        }
                        
                        System.out.printf("Shiny: %f\n", (float) matParams.getSpecularValue());
                        break;
                        
                    case GLFW_KEY_Y:
                        drawLightSource = !drawLightSource;
                        break;
                        
                    case GLFW_KEY_T:
                        scaleCyl = !scaleCyl;
                        break;
                        
                    case GLFW_KEY_B:
                        lightTimer.togglePause();
                        break;
                        
                    case GLFW_KEY_G:
                        drawDark = !drawDark;
                        break;
                        
                    case GLFW_KEY_H:
                        if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                            
                            int index;
                            
                            if(lightModel.ordinal() % 2 != 0) {
                                
                                index = lightModel.ordinal() - 1;
                            }
                            else {
                                
                                index = lightModel.ordinal() + 1;
                            }
                            
                            index %= LightingModel.MAX_LIGHTING_MODEL.ordinal();
                            lightModel = LightingModel.values()[index];
                        }
                        else {
                            
                            int index = lightModel.ordinal() - 2;
                            index %= LightingModel.MAX_LIGHTING_MODEL.ordinal();
                            lightModel = LightingModel.values()[index];
                        }
                        
                        System.out.printf("%s\n", lightModelNames[lightModel.ordinal()]);
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
        
        ProgramData whiteProg = programs[lightModel.ordinal()].whiteProg;
        ProgramData colorProg = programs[lightModel.ordinal()].colorProg;
        
        glUseProgram(whiteProg.theProgram);
        glUniform4f(whiteProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(whiteProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3fv(whiteProg.cameraSpaceLightPosUnif, lightPosCameraSpace.get(vec4Buffer));
        
        float lightAttenuation = 1.2f;
        glUniform1f(whiteProg.lightAttenuationUnif, lightAttenuation);
        glUniform1f(whiteProg.shininessFactorUnif, matParams.getSpecularValue());
        glUniform4fv(whiteProg.baseDiffuseColorUnif,
                drawDark ? darkColor.get(vec4Buffer) : lightColor.get(vec4Buffer));
        
        glUseProgram(colorProg.theProgram);
        glUniform4f(colorProg.lightAttenuationUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(colorProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3fv(colorProg.cameraSpaceLightPosUnif, lightPosCameraSpace.get(vec4Buffer));
        glUniform1f(colorProg.lightAttenuationUnif, lightAttenuation);
        glUniform1f(colorProg.shininessFactorUnif, matParams.getSpecularValue());
        
        glUseProgram(0);
        
        {
            modelMatrix.pushMatrix();
            
            {
                modelMatrix.pushMatrix();
                
                Matrix3f normMatrix = new Matrix3f(modelMatrix);
                normMatrix.invert().transpose();
                
                glUseProgram(whiteProg.theProgram);
                glUniformMatrix4fv(whiteProg.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                glUniformMatrix3fv(whiteProg.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
    
                planeMesh.render();
                
                glUseProgram(0);
                modelMatrix.popMatrix();
            }
    
            {
                modelMatrix.pushMatrix();
        
                modelMatrix.mul(objtPole.calcMatrix());
        
                if (scaleCyl)
                    modelMatrix.scale(1.0f, 1.0f, 0.2f);
                
        
                Matrix3f normMatrix = new Matrix3f(modelMatrix);
                normMatrix.invert().transpose();
        
                ProgramData prog = drawColoredCyl ? colorProg : whiteProg;
                glUseProgram(prog.theProgram);
                glUniformMatrix4fv(prog.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
        
                glUniformMatrix3fv(prog.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
        
                if(drawColoredCyl) {
                    
                    cylinderMesh.render("lit-color");
                }
                else {
                    
                    cylinderMesh.render("lit");
                }
        
                glUseProgram(0);
        
                modelMatrix.popMatrix();
            }
    
            if(drawLightSource) {
    
                modelMatrix.pushMatrix();
    
                modelMatrix.translate(worldLightPos.x, worldLightPos.y, worldLightPos.z);
                modelMatrix.scale(0.1f, 0.1f, 0.1f);
                
                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                glUniform4f(unlit.objectColorUnif, 0.8078f, 0.8706f, 0.8822f, 1.0f);
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
    
        for(int progIndex = 0; progIndex < LightingModel.MAX_LIGHTING_MODEL.ordinal(); progIndex++) {
            
            programs[progIndex] = new ProgramPairs();
            programs[progIndex].whiteProg = loadLitProgram(
                    shaderFileNAmes[progIndex].whiteVertShaderFileName,
                    shaderFileNAmes[progIndex].fragmentShaderFileName);
            programs[progIndex].colorProg = loadLitProgram(
                    shaderFileNAmes[progIndex].colorVertShaderFileName,
                    shaderFileNAmes[progIndex].fragmentShaderFileName);
        }
        
        unlit = loadUnlitProgram("PosTransform.vert", "UniformColor.frag");
    }
    
    private ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
        
        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
    
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");
        data.ambientIntensityUnif = glGetUniformLocation(data.theProgram, "ambientIntensity");
    
        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
        data.cameraSpaceLightPosUnif = glGetUniformLocation(data.theProgram, "cameraSpaceLightPos");
        data.lightAttenuationUnif = glGetUniformLocation(data.theProgram, "lightAttenuation");
        data.shininessFactorUnif = glGetUniformLocation(data.theProgram, "shininessFactor");
        data.baseDiffuseColorUnif = glGetUniformLocation(data.theProgram, "baseDiffuseColor");
        
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);
        
        return data;
    }
    
    private UnlitProgData loadUnlitProgram(String vertexShaderFileNAme, String fragmentShaderFileNAme) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileNAme));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileNAme));
        
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
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut11/data/";
        new BlinnVsPhongLighting().start(500, 500);
    }
    
    private enum LightingModel {
        
        PHONG_SPECULAR,
        PHONG_ONLY,
        BLINN_SPECULAR,
        BLINN_ONLY,
        MAX_LIGHTING_MODEL
    }
    
    private class ProgramData {
        
        int theProgram;
        int modelToCameraMatrixUnif;
        int lightIntensityUnif;
        int ambientIntensityUnif;
        int normalModelToCameraMatrixUnif;
        int cameraSpaceLightPosUnif;
        int lightAttenuationUnif;
        int shininessFactorUnif;
        int baseDiffuseColorUnif;
    }
    
    private class UnlitProgData {
        
        int theProgram;
        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }
    
    private class ProgramPairs {
        
        ProgramData whiteProg;
        ProgramData colorProg;
    }
    
    private class ShaderPairs {
        
        String whiteVertShaderFileName;
        String colorVertShaderFileName;
        String fragmentShaderFileName;
        
        ShaderPairs(String whiteVertShaderFileName, String colorVertShaderFileName, String fragmentShaderFileName) {
            
            this.whiteVertShaderFileName = whiteVertShaderFileName;
            this.colorVertShaderFileName = colorVertShaderFileName;
            this.fragmentShaderFileName = fragmentShaderFileName;
        }
    }
    
    private class MaterialParams {
        
        float phongExponent;
        float blinnExponent;
        
        MaterialParams() {
            
            phongExponent = 4.0f;
            blinnExponent = 4.0f;
        }
        
        void increment(boolean isLarge) {
        
            switch(lightModel) {
                
                case PHONG_SPECULAR:
                
                case PHONG_ONLY:
                    if(isLarge) {
                        phongExponent += 0.5f;
                    }
                    else {
                        phongExponent += 0.1f;
                    }
                    break;
                    
                case BLINN_SPECULAR:
                
                case BLINN_ONLY:
                    if(isLarge) {
                        blinnExponent += 0.5f;
                    }
                    else {
                        blinnExponent += 0.1f;
                    }
                    break;
                    
                default:
                    break;
            }
            
            clampParam();
        }
        
        void decrement(boolean isLarge) {
    
            switch(lightModel) {
        
                case PHONG_SPECULAR:
        
                case PHONG_ONLY:
                    if(isLarge) {
                        phongExponent -= 0.5f;
                    }
                    else {
                        phongExponent -= 0.1f;
                    }
                    break;
        
                case BLINN_SPECULAR:
        
                case BLINN_ONLY:
                    if(isLarge) {
                        blinnExponent -= 0.5f;
                    }
                    else {
                        blinnExponent -= 0.1f;
                    }
                    break;
        
                default:
                    break;
            }
    
            clampParam();
        }
        
        float getSpecularValue() {
        
            switch(lightModel) {
                
                case PHONG_SPECULAR:
                
                case PHONG_ONLY:
                    return phongExponent;
                    
                case BLINN_SPECULAR:
                
                case BLINN_ONLY:
                    return blinnExponent;
                    
                default:
                    return 0.0f;
            }
        }
        
        private void clampParam() {
        
            switch(lightModel) {
                
                case PHONG_SPECULAR:
                
                case PHONG_ONLY:
                    if(phongExponent <= 0.0f)
                        phongExponent = 0.0001f;
                    
                    break;
                    
                case BLINN_SPECULAR:
                
                case BLINN_ONLY:
                    if(blinnExponent <= 0.0f)
                        blinnExponent = 0.0001f;
                    
                    break;
                    
                default:
                    break;
            }
        }
    }
}
