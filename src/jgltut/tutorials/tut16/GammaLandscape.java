package jgltut.tutorials.tut16;

import jglsdk.jglimg.DdsLoader;
import jglsdk.jglimg.ImageSet;
import jglsdk.jglimg.ImageSet.Dimensions;
import jglsdk.jglimg.ImageSet.SingleImage;
import jglsdk.jglimg.TextureGenerator;
import jglsdk.jglutil.MousePoles.MouseButtons;
import jglsdk.jglutil.MousePoles.ViewData;
import jglsdk.jglutil.MousePoles.ViewPole;
import jglsdk.jglutil.MousePoles.ViewScale;
import jgltut.Tutorial;
import jgltut.commons.LightBlock;
import jgltut.commons.ProjectionBlock;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import jgltut.framework.MousePole;
import org.joml.*;
import org.lwjgl.opengl.GL15;

import java.lang.Math;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;

public class GammaLandscape extends Tutorial {
    
    private ProgramData progStandard;
    private UnlitProgData progUnlit;
    
    private Mesh terrain;
    private Mesh sphere;
    
    private final int projectionBlockIndex = 0;
    private final int lightBlockIndex = 1;
    
    private final int colorTexUnit = 0;
    
    private final int NUM_SAMPLERS = 2;
    private int[] samplers = new int[NUM_SAMPLERS];
    private int currSampler;
    
    private int projectionUniformBuffer;
    private int lightUniformBuffer;
    private int linearTexture;
    
    private LightEnv lightEnv;
    
    private boolean useGammaDisplay = true;
    private boolean drawCameraPos;
    
    private ViewData initialView = new ViewData(
            
            new Vector3f(-60.257084f, 10.947238f, 62.636356f),
            new Quaternionf(-0.099283f, -0.211198f, -0.020028f, -0.972817f),
            30.0f,
            0.0f
    );
    
    private ViewScale initialViewScale = new ViewScale(
            
            5.0f, 90.0f,
            2.0f, 0.5f,
            4.0f, 1.0f,
            90.0f / 250.0f
    );
    
    private ViewPole viewPole = new ViewPole(initialView, initialViewScale, MouseButtons.MB_LEFT_BTN);
    
    @Override
    protected void init() {
    
        lightEnv = new LightEnv("LightEnv.xml");
        
        initializePrograms();
        
        terrain = new Mesh("terrain.xml");
        sphere = new Mesh("UnitSphere.xml");
        
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
        
        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        GL15.glBufferData(GL_UNIFORM_BUFFER, LightBlock.BYTES, GL_STREAM_DRAW);
        
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.BYTES);
        
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        loadTextures();
        createSamplers();
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
           
            if(action == GLFW_PRESS) {
                
                switch(key) {
                    
                    case GLFW_KEY_SPACE:
                        useGammaDisplay = !useGammaDisplay;
                        break;
                        
                    case GLFW_KEY_MINUS:
                        lightEnv.rewindTime(1.0f);
                        break;
                        
                    case GLFW_KEY_EQUAL:
                        lightEnv.fastForwardTime(1.0f);
                        break;
                        
                    case GLFW_KEY_T:
                        drawCameraPos = !drawCameraPos;
                        break;
                        
                    case GLFW_KEY_P:
                        lightEnv.togglePause();
                        break;
                        
                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
                }
                
                if(GLFW_KEY_1 <= key && key <= GLFW_KEY_9) {
                
                    int number = key - GLFW_KEY_1;
                    
                    if(number < NUM_SAMPLERS)
                        currSampler = number;
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
        
            if(isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                
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
    
        if(useGammaDisplay) {
            
            glEnable(GL_FRAMEBUFFER_SRGB);
        }
        else {
            
            glDisable(GL_FRAMEBUFFER_SRGB);
        }
    
        lightEnv.updateTime(elapsedTime);
    
        Vector4f bgColor = lightEnv.getBackgroundColor();
        glClearColor(bgColor.x, bgColor.y, bgColor.z, bgColor.w);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());
        
        LightBlock lightData = lightEnv.getLightBlock(viewPole.calcMatrix());
        
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, lightData.getAndFlip(lightBlockBuffer), GL_STREAM_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    
        modelMatrix.pushMatrix();
        modelMatrix.rotateX((float) Math.toRadians(-90.0f));
        
        glUseProgram(progStandard.theProgram);
        glUniformMatrix4fv(progStandard.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
        glUniform1i(progStandard.numberOfLightsUnif, lightEnv.getNumLights());
        
        glActiveTexture(GL_TEXTURE0 + colorTexUnit);
        glBindTexture(GL_TEXTURE_2D, linearTexture);
        glBindSampler(colorTexUnit, samplers[currSampler]);
        
        terrain.render("lit-tex");
        
        glBindSampler(colorTexUnit, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        glUseProgram(0);
    
        modelMatrix.popMatrix();
        
        // render sun
        {
            modelMatrix.pushMatrix();
            
            Vector4f tmp = lightEnv.getSunlightDirection();
            Vector3f sunlightDir = new Vector3f(tmp.x, tmp.y, tmp.z);
            modelMatrix.translate(sunlightDir.mul(500.0f));
            modelMatrix.scale(30.0f, 30.0f, 30.0f);
            
            glUseProgram(progUnlit.theProgram);
            glUniformMatrix4fv(progUnlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            
            Vector4f lightColor = lightEnv.getSunlightScaledIntensity();
            glUniform4fv(progUnlit.objectColorUnif, lightColor.get(vec4Buffer));
            sphere.render("flat");
    
            modelMatrix.popMatrix();
        }
        
        // draw lights
        for(int light = 0; light < lightEnv.getNumPointLights(); light++) {
    
            modelMatrix.pushMatrix();
    
            modelMatrix.translate(lightEnv.getPointLightWorldPos(light));
            
            glUseProgram(progUnlit.theProgram);
            glUniformMatrix4fv(progUnlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            
            Vector4f lightColor = lightEnv.getPointLightScaledIntensity(light);
            glUniform4fv(progUnlit.objectColorUnif, lightColor.get(vec4Buffer));
            sphere.render("flat");
    
            modelMatrix.popMatrix();
        }
        
        if(drawCameraPos) {
    
            modelMatrix.pushMatrix();
    
            modelMatrix.identity();
            modelMatrix.translate(0.0f, 0.0f, -viewPole.getView().radius);
            
            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
            glUseProgram(progUnlit.theProgram);
            glUniformMatrix4fv(progUnlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(progUnlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
            sphere.render("flat");
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
            glUniform4f(progUnlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            sphere.render("flat");
    
            modelMatrix.popMatrix();
        }
    }
    
    @Override
    protected void reshape(int w, int h) {
    
        float zNear = 1.0f;
        float zFar = 1000.0f;
    
        Matrix4f persMatrix = new Matrix4f();
        persMatrix.perspective((float) Math.toRadians(60.0f), (w / (float) h), zNear, zFar);
        
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
    
        progStandard = loadProgram("PNT.vert", "litTexture.frag");
        progUnlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
    }
    
    private ProgramData loadProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
        
        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
    
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.numberOfLightsUnif = glGetUniformLocation(data.theProgram, "numberOfLights");
        
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);
        
        int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
        glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
        
        int colorTextureUnif = glGetUniformLocation(data.theProgram, "diffuseColorTex");
        glUseProgram(data.theProgram);
        glUniform1i(colorTextureUnif, colorTexUnit);
        glUseProgram(0);
        
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
    
    private void loadTextures() {
    
        try {
    
            String filePath = Framework.findFileOrThrow("terrain_tex.dds");
            ImageSet imageSet = DdsLoader.loadFromFile(filePath);
    
            linearTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, linearTexture);
    
            TextureGenerator.OpenGLPixelTransferParams xfer = TextureGenerator.getUploadFormatType(
                    imageSet.getFormat(), 0);
    
            for(int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
        
                SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
                Dimensions imageDimensions = image.getDimensions();
        
                glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8_ALPHA8, imageDimensions.width, imageDimensions.height,
                        0, xfer.format, xfer.type, image.getImageData());
            }
    
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);
    
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        catch(Exception e) {
            
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private void createSamplers() {
    
        for(int samplerIndex = 0; samplerIndex < NUM_SAMPLERS; samplerIndex++) {
            
            samplers[samplerIndex] = glGenSamplers();
            
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_S, GL_REPEAT);
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_T, GL_REPEAT);
        }
        
        // linear mipmap linear
        glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        
        // max anisotropic
        float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
        
        glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameterf(samplers[1], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut16/data/";
        new GammaLandscape().start(700, 700);
    }
    
    private class ProgramData {
        
        int theProgram;
        int modelToCameraMatrixUnif;
        int numberOfLightsUnif;
    }
    
    private class UnlitProgData {
        
        int theProgram;
        int modelToCameraMatrixUnif;
        int objectColorUnif;
    }
}
