package jgltut.tutorials.tut12;

import jglsdk.jglutil.MousePoles.*;
import jgltut.Tutorial;
import jgltut.commons.ProjectionBlock;
import jgltut.framework.Framework;
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

public class GammaCorrection extends Tutorial {
    
    private final int materialBlockIndex = 0;
    private final int lightBlockIndex = 1;
    private final int projectionBlockIndex = 2;
    
    private final Vector4f skyDaylightColor = new Vector4f(0.65f, 0.65f, 1.0f, 1.0f);
    
    private float gammaValue = 2.0f;
    
    private int lightUniformBuffer;
    private int projectionUniformBuffer;
    
    private UnlitProgData unlit;
    
    private Scene scene;
    
    private boolean drawCameraPos;
    private boolean isGammaCorrect;
    
    private LightManager lights = new LightManager();
    private LightManager.TimerTypes timerMode = LightManager.TimerTypes.ALL;
    
    private Scene.ProgramData[] programs = new Scene.ProgramData[
            Scene.LightingProgramTypes.MAX_LIGHTING_PROGRAM_TYPES.ordinal()];
    
    private Shaders[] shaderFileNames = new Shaders[] {
            
            new Shaders("PCN.vert", "DiffuseSpecularGamma.frag"),
            new Shaders("PCN.vert", "DiffuseOnlyGamma.frag"),
            new Shaders("PN.vert", "DiffuseSpecularMtlGamma.frag"),
            new Shaders("PN.vert", "DiffuseOnlyMtlGamma.frag")
    };
    
    private ViewData initialViewData = new ViewData(
            
            new Vector3f(-59.5f, 44.0f, 95.0f),
            new Quaternionf(0.3826834f, 0.0f, 0.0f, 0.92387953f),
            50.0f, 0.0f
    );
    
    private ViewScale viewScale = new ViewScale(
            
            3.0f, 80.0f,
            4.0f, 1.0f,
            5.0f, 1.0f,
            90.0f / 250.0f
    );
    
    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
    
    @Override
    protected void init() {
    
        initializePrograms();
        
        scene = new Scene() {
    
            @Override
            ProgramData getProgram(LightingProgramTypes lightingProgramType) {
        
                return programs[lightingProgramType.ordinal()];
            }
        };
        
        setupHDRLighting();
        lights.createTimer("tetra", Timer.Type.LOOP, 2.5f);
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
        
        final float depthZNear = 1.0f;
        final float depthZFar = 1.0f;
        
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(depthZNear, depthZFar);
        glEnable(GL_DEPTH_CLAMP);
        
        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, LightManager.LightBlockGamma.BYTES, GL_DYNAMIC_DRAW);
        
        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.BYTES, GL_DYNAMIC_DRAW);
        
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer,
                0, LightManager.LightBlockGamma.BYTES);
        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex,
                projectionUniformBuffer, 0, ProjectionBlock.BYTES);
        
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
        
            if(action == GLFW_PRESS) {
                
                switch(key) {
                    
                    case GLFW_KEY_P:
                        lights.togglePause(timerMode);
                        break;
                        
                    case GLFW_KEY_MINUS:
                        lights.rewindTime(timerMode, 1.0f);
                        break;
                        
                    case GLFW_KEY_EQUAL:
                        lights.fastForwardTime(timerMode, 1.0f);
                        break;
                        
                    case GLFW_KEY_T:
                        drawCameraPos = !drawCameraPos;
                        break;
                        
                    case GLFW_KEY_1:
                        timerMode = LightManager.TimerTypes.ALL;
                        System.out.printf("All\n");
                        break;
                        
                    case GLFW_KEY_2:
                        timerMode = LightManager.TimerTypes.SUN;
                        System.out.printf("Sun\n");
                        break;
                        
                    case GLFW_KEY_3:
                        timerMode = LightManager.TimerTypes.LIGHTS;
                        System.out.printf("Lights\n");
                        break;
                        
                    case GLFW_KEY_L:
                        if(isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                            
                            setupGammaLighting();
                        }
                        else {
                            
                            setupHDRLighting();
                        }
                        break;
                        
                    case GLFW_KEY_K:
                        isGammaCorrect = !isGammaCorrect;
                        
                        if(isGammaCorrect) {
                            
                            System.out.printf("Gamma on!\n");
                        }
                        else {
                            
                            System.out.printf("Gammaoff!\n");
                        }
                        break;
                        
                    case GLFW_KEY_Y:
                        gammaValue += 0.1f;
                        System.out.printf("Gamma: %f\n", gammaValue);
                        break;
                        
                    case GLFW_KEY_H:
                        gammaValue -= 0.1f;
                        
                        if(gammaValue < 1.0f)
                            gammaValue = 1.0f;
                        
                        System.out.printf("Gamma: %f\n", gammaValue);
                        break;
                        
                    case GLFW_KEY_SPACE:
                        float sunAlpha = lights.getSunTime();
                        float sunTimeHours = sunAlpha * 24.0f + 12.0f;
                        sunTimeHours = sunTimeHours > 24.0f ? sunTimeHours - 24.0f : sunTimeHours;
                        int sunHours = (int) sunTimeHours;
                        float sunTimeMinutes = (sunTimeHours - sunHours) * 60.0f;
                        int sunMinutes = (int) sunTimeMinutes;
                        System.out.printf("%02d:%02d\n", sunHours, sunMinutes);
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
        });
        
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
           
            if(isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) ||
                    isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                
                MousePole.forwardMouseMotion(viewPole, (int) xpos, (int) ypos);
            }
        });
        
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
        
            glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
            
            int x = (int) mouseBuffer1.get(0);
            int y = (int) mouseBuffer2.get(0);
            
            MousePole.forwardMouseWheel(window, viewPole, (int) yoffset, x, y);
        });
    }
    
    @Override
    protected void display() {
    
        lights.updateTime(elapsedTime);
        
        float gamma = isGammaCorrect ? gammaValue : 1.0f;
        
        Vector4f bkg = gammaCorrect(lights.getBackgroundColor(), gamma);
        
        glClearColor(bkg.x, bkg.y, bkg.z, bkg.w);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());
        
        final Matrix4f worldToCamMat = modelMatrix;
        LightManager.LightBlockGamma lightData = lights.getLightInformationGamma(worldToCamMat);
        lightData.gamma = gamma;
        
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.getAndFlip(lightBlockBuffer));
        
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        {
            modelMatrix.pushMatrix();
            scene.draw(modelMatrix, materialBlockIndex, lights.getTimerValue("tetra"));
            modelMatrix.popMatrix();
        }
        
        {
            modelMatrix.pushMatrix();
            
            // render sun
            {
                modelMatrix.pushMatrix();
                
                Vector4f temp = lights.getSunlightDirection();
                Vector3f sunlightDir = new Vector3f(temp.x, temp.y, temp.z);
                modelMatrix.translate(sunlightDir.mul(500.0f));
                modelMatrix.scale(30.0f, 30.0f, 30.0f);
                
                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                
                Vector4f lightColor = lights.getSunlightIntensity();
                glUniform4fv(unlit.objectColorUnif, lightColor.get(vec4Buffer));
                scene.getSphereMesh().render("flat");
    
                modelMatrix.popMatrix();
            }
            
            // render lights
            {
                for(int light = 0; light < lights.getNumberOfPointLights(); light++) {
    
                    modelMatrix.pushMatrix();
    
                    modelMatrix.translate(lights.getWorldLightPosition(light));
                    
                    glUseProgram(unlit.theProgram);
                    glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                    
                    Vector4f lightColor = lights.getPointLightIntensity(light);
                    glUniform4fv(unlit.objectColorUnif, lightColor.get(vec4Buffer));
                    scene.getCubeMesh().render("flat");
    
                    modelMatrix.popMatrix();
                }
            }
            
            if(drawCameraPos) {
    
                modelMatrix.pushMatrix();
    
                modelMatrix.identity();
                modelMatrix.translate(0.0f, 0.0f, -viewPole.getView().radius);
                
                glDisable(GL_DEPTH_TEST);
                glDepthMask(false);
                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                glUniform4f(unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
                scene.getCubeMesh().render("flat");
                glDepthMask(true);
                glEnable(GL_DEPTH_TEST);
                glUniform4f(unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
                scene.getCubeMesh().render("flat");
    
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
    
        final float scale = 20;
    
        if(isKeyPressed(GLFW_KEY_W)) {
            
            viewPole.charPress(GLFW_KEY_W, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
        else if(isKeyPressed(GLFW_KEY_S)) {
            
            viewPole.charPress(GLFW_KEY_S, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
    
        if(isKeyPressed(GLFW_KEY_D)) {
            
            viewPole.charPress(GLFW_KEY_D, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
        else if(isKeyPressed(GLFW_KEY_A)) {
            
            viewPole.charPress(GLFW_KEY_A, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
    
        if(isKeyPressed(GLFW_KEY_E)) {
            
            viewPole.charPress(GLFW_KEY_E, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
        else if(isKeyPressed(GLFW_KEY_Q)) {
            
            viewPole.charPress(GLFW_KEY_Q, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
    }
    
    private void initializePrograms() {
    
        for(int progIndex = 0; progIndex < Scene.LightingProgramTypes
                .MAX_LIGHTING_PROGRAM_TYPES.ordinal(); progIndex++) {
            
            programs[progIndex] = new Scene.ProgramData();
            programs[progIndex] = loadLitProgram(
                    shaderFileNames[progIndex].vertexShaderFileName,
                    shaderFileNames[progIndex].fragmentShaderFileName);
        }
        
        unlit = loadUnlitProgram("PosTransform.vert", "UniformColor.frag");
    }
    
    private Scene.ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
        
        Scene.ProgramData data = new Scene.ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
    
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
        
        int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
        int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        
        if(materialBlock != GL_INVALID_INDEX)
            glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
        
        glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
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
    
    private void setupHDRLighting() {
    
        LightManager.SunlightValueHDR values[] = {
                
                new LightManager.SunlightValueHDR(0.0f / 24.0f,
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vector4f(skyDaylightColor), 3.0f),
        
                new LightManager.SunlightValueHDR(4.5f / 24.0f,
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vector4f(skyDaylightColor), 3.0f),
        
                new LightManager.SunlightValueHDR(6.5f / 24.0f,
                        new Vector4f(0.225f, 0.075f, 0.075f, 1.0f),
                        new Vector4f(0.45f, 0.15f, 0.15f, 1.0f),
                        new Vector4f(skyDaylightColor), 1.5f),
        
                new LightManager.SunlightValueHDR(8.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(skyDaylightColor), 1.0f),
        
                new LightManager.SunlightValueHDR(18.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        1.0f),
                
                new LightManager.SunlightValueHDR(19.5f / 24.0f,
                        new Vector4f(0.225f, 0.075f, 0.075f, 1.0f),
                        new Vector4f(0.45f, 0.15f, 0.15f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f),
                        1.5f),
                
                new LightManager.SunlightValueHDR(20.5f / 24.0f,
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vector4f(skyDaylightColor),3.0f),
        };
    
        lights.setSunlightValues(values, 7);
    
        lights.setPointLightIntensity(0, new Vector4f(0.6f, 0.6f, 0.6f, 1.0f));
        lights.setPointLightIntensity(1, new Vector4f(0.0f, 0.0f, 0.7f, 1.0f));
        lights.setPointLightIntensity(2, new Vector4f(0.7f, 0.0f, 0.0f, 1.0f));
    }
    
    private void setupGammaLighting() {
    
        Vector4f sunlight = new Vector4f(6.5f, 6.5f, 6.5f, 1.0f);
        Vector4f brightAmbient = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
    
        LightManager.SunlightValueHDR values[] = {
                
                new LightManager.SunlightValueHDR(
                        0.0f / 24.0f,
                        brightAmbient,
                        sunlight,
                        new Vector4f(0.65f, 0.65f, 1.0f, 1.0f),
                        10.0f),
                
                new LightManager.SunlightValueHDR(
                        4.5f / 24.0f,
                        brightAmbient,
                        sunlight,
                        new Vector4f(skyDaylightColor),
                        10.0f),
                
                new LightManager.SunlightValueHDR(
                        6.5f / 24.0f,
                        new Vector4f(0.01f, 0.025f, 0.025f, 1.0f),
                        new Vector4f(2.5f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f),
                        5.0f),
                
                new LightManager.SunlightValueHDR(
                        8.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        3.0f),
                
                new LightManager.SunlightValueHDR(
                        18.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        3.0f),
                
                new LightManager.SunlightValueHDR(
                        19.5f / 24.0f,
                        new Vector4f(0.01f, 0.025f, 0.025f, 1.0f),
                        new Vector4f(2.5f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f),
                        5.0f),
                
                new LightManager.SunlightValueHDR(
                        20.5f / 24.0f,
                        brightAmbient,
                        sunlight,
                        new Vector4f(skyDaylightColor),
                        10.0f)
        };
    
        lights.setSunlightValues(values, 7);
    
        lights.setPointLightIntensity(0, new Vector4f(0.6f, 0.6f, 0.6f, 1.0f));
        lights.setPointLightIntensity(1, new Vector4f(0.0f, 0.0f, 0.7f, 1.0f));
        lights.setPointLightIntensity(2, new Vector4f(0.7f, 0.0f, 0.0f, 1.0f));
    }
    
    private Vector4f gammaCorrect(Vector4f input, float gamma) {
    
        Vector4f inputCorrected = new Vector4f();
    
        inputCorrected.x = (float) Math.pow(input.x, 1.0f / gamma);
        inputCorrected.y = (float) Math.pow(input.y, 1.0f / gamma);
        inputCorrected.z = (float) Math.pow(input.z, 1.0f / gamma);
        inputCorrected.w = input.w;
        
        return inputCorrected;
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut12/data/";
        new GammaCorrection().start(700, 700);
    }
    
    private class Shaders {
        
        String vertexShaderFileName;
        String fragmentShaderFileName;
        
        Shaders(String vertexShaderFileName, String fragmentShaderFileName) {
            
            this.vertexShaderFileName = vertexShaderFileName;
            this.fragmentShaderFileName = fragmentShaderFileName;
        }
    }
    
    private class UnlitProgData {
        
        int theProgram;
        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }
}
