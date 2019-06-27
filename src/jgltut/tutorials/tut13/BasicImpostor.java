package jgltut.tutorials.tut13;

import jglsdk.jglutil.MousePoles.*;
import jgltut.Tutorial;
import jgltut.commons.LightBlock;
import jgltut.commons.MaterialBlock;
import jgltut.commons.PerLight;
import jgltut.commons.ProjectionBlock;
import jgltut.framework.*;
import org.joml.*;
import org.lwjgl.opengl.GL15;

import java.lang.Math;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

public class BasicImpostor extends Tutorial {
    
    private final int materialBlockIndex = 0;
    private final int lightBlockIndex = 1;
    private final int projectionBlockIndex = 2;
    
    private int materialUniformBuffer;
    private int materialBlockOffset;
    private int lightUniformBuffer;
    private int projectionUniformBuffer;
    
    private ProgramMeshData litMeshProg;
    private UnlitProgData unlit;
    
    private Mesh planeMesh;
    private Mesh sphereMesh;
    private Mesh cubeMesh;
    
    private Impostors currImpostor = Impostors.BASIC;
    
    private int impostorVAO;
    
    private boolean[] drawImpostor = {false, false, false, false};
    private boolean drawCameraPos;
    private boolean drawLights = true;
    
    private Timer sphereTimer = new Timer(Timer.Type.LOOP, 6.0f);
    
    private ProgramImposData[] litImpProgs = new ProgramImposData[Impostors.NUM_IMPOSTORS.ordinal()];
    
    private String[] impShaderFileNames = new String[] {
        
            "BasicImpostor.vert", "BasicImpostor.frag",
            "PerspImpostor.vert", "PerspImpostor.frag",
            "DepthImpostor.vert", "DepthImpostor.frag"
    };
    
    private ViewData initialViewData = new ViewData(
            new Vector3f(0.0f, 30.0f, 25.0f),
            new Quaternionf(0.3826834f, 0.0f, 0.0f, 0.92387953f),
            10.0f,
            0.0f
    );
    
    private ViewScale viewScale = new ViewScale(
            3.0f, 70.0f,
            3.5f, 1.5f,
            5.0f, 1.0f,
            90.0f / 250.0f
    );
    
    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
    
    
    @Override
    protected void init() {
    
        initializePrograms();
        
        planeMesh = new Mesh("LargePlane.xml");
        sphereMesh = new Mesh("UnitSphere.xml");
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
        
        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        GL15.glBufferData(GL_UNIFORM_BUFFER, LightBlock.BYTES, GL_DYNAMIC_DRAW);
        
        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        GL15.glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.BYTES, GL_DYNAMIC_DRAW);
        
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.BYTES);
        
        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex,
                projectionUniformBuffer, 0, ProjectionBlock.BYTES);
        
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        impostorVAO = glGenVertexArrays();
        glBindVertexArray(impostorVAO);
        
        createMaterials();
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
        
            if(action == GLFW_PRESS) {
                
                switch(key) {
                    
                    case GLFW_KEY_P:
                        sphereTimer.togglePause();
                        break;
                        
                    case GLFW_KEY_MINUS:
                        sphereTimer.rewind(0.5f);
                        break;
                        
                    case GLFW_KEY_EQUAL:
                        sphereTimer.fastForward(0.5f);
                        
                    case GLFW_KEY_T:
                        drawCameraPos = !drawCameraPos;
                        break;
                        
                    case GLFW_KEY_G:
                        drawLights = !drawLights;
                        break;
                        
                    case GLFW_KEY_1:
                        drawImpostor[0] = !drawImpostor[0];
                        break;
    
                    case GLFW_KEY_2:
                        drawImpostor[1] = !drawImpostor[1];
                        break;
    
                    case GLFW_KEY_3:
                        drawImpostor[2] = !drawImpostor[2];
                        break;
    
                    case GLFW_KEY_4:
                        drawImpostor[3] = !drawImpostor[3];
                        break;
                        
                    case GLFW_KEY_L:
                        currImpostor = Impostors.BASIC;
                        break;
                        
                    case GLFW_KEY_J:
                        currImpostor = Impostors.PERSPECTIVE;
                        break;
                        
                    case GLFW_KEY_H:
                        currImpostor = Impostors.DEPTH;
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
    
        sphereTimer.update(elapsedTime);
        
        glClearColor(0.75f, 0.75f, 1.0f, 1.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());
        final Matrix4f worldToCamMat = modelMatrix;
        
        LightBlock lightData = new LightBlock();
        lightData.ambientIntensity = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
        
        float halfLightDistance = 25.0f;
        lightData.lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);
    
        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = new Matrix4f(worldToCamMat)
                .transform(new Vector4f(0.707f, 0.707f, 0.0f, 0.0f));
        lightData.lights[0].lightIntensity = new Vector4f(0.6f, 0.6f, 0.6f, 1.0f);
    
        lightData.lights[1] = new PerLight();
        lightData.lights[1].cameraSpaceLightPos = new Matrix4f(worldToCamMat).transform(calcLightPosition());
        lightData.lights[1].lightIntensity = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
        
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.getAndFlip(lightBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        {
            glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer,
                    MaterialNames.TERRAIN.ordinal() * materialBlockOffset, MaterialBlock.BYTES);
            
            Matrix3f normMatrix = new Matrix3f(modelMatrix);
            normMatrix.invert().transpose();
            
            glUseProgram(litMeshProg.theProgram);
            glUniformMatrix4fv(litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniformMatrix3fv(litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
            
            planeMesh.render();
            
            glUseProgram(0);
            glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
        }
        
        drawSphere(modelMatrix, new Vector3f(0.0f, 10.0f, 0.0f),
                4.0f, MaterialNames.BLUE_SHINY, drawImpostor[0]);
        drawSphereOrbit(modelMatrix, new Vector3f(0.0f, 10.0f, 0.0f),
                new Vector3f(0.6f, 0.8f, 0.0f), 20.0f, sphereTimer.getAlpha(),
                2.0f, MaterialNames.DULL_GREY, drawImpostor[1]);
        drawSphereOrbit(modelMatrix, new Vector3f(-10.0f, 1.0f, 0.0f),
                new Vector3f(0.0f, 1.0f, 0.0f), 10.0f, sphereTimer.getAlpha(),
                1.0f, MaterialNames.BLACK_SHINY, drawImpostor[2]);
        drawSphereOrbit(modelMatrix, new Vector3f(10.0f, 1.0f, 0.0f),
                new Vector3f(0.0f, 1.0f, 0.0f), 10.0f,
                sphereTimer.getAlpha() * 2.0f,
                1.0f, MaterialNames.GOLD_METAL, drawImpostor[3]);
        
        if(drawLights) {
    
            modelMatrix.pushMatrix();
            
            Vector4f v = calcLightPosition();
            modelMatrix.translate(new Vector3f(v.x, v.y, v.z));
            modelMatrix.scale(0.5f);
            
            glUseProgram(unlit.theProgram);
            glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            
            Vector4f lightColor = new Vector4f(1.0f);
            glUniform4fv(unlit.objectColorUnif, lightColor.get(vec4Buffer));
            cubeMesh.render("flat");
    
            modelMatrix.popMatrix();
        }
        
        if(drawCameraPos) {
    
            modelMatrix.pushMatrix();
            
            modelMatrix.identity();
            modelMatrix.translate(new Vector3f(0.0f, 0.0f, -viewPole.getView().radius));
            
            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
            
            glUseProgram(unlit.theProgram);
            glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
            cubeMesh.render("flat");
            
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
            glUniform4f(unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            cubeMesh.render("flat");
    
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
    
        final float scale = 10;
        
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
    
        litMeshProg = loadLitMeshProgram("PN.vert", "Lighting.frag");
        
        for(int progIndex = 0; progIndex < Impostors.NUM_IMPOSTORS.ordinal(); progIndex++) {
            
            litImpProgs[progIndex] = new ProgramImposData();
            litImpProgs[progIndex] = loadLitImposProgram(impShaderFileNames[progIndex * 2],
                    impShaderFileNames[progIndex * 2 + 1]);
        }
        
        unlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
    }
    
    private void drawSphere(MatrixStackf modelMatrix, Vector3f position,
            float radius, MaterialNames material, boolean drawImpostor) {
    
        glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer,
                material.ordinal() * materialBlockOffset, MaterialBlock.BYTES);
        
        if(drawImpostor) {
            
            Vector4f cameraSpherePos = new Matrix4f(modelMatrix).transform(new Vector4f(position, 1.0f));
            glUseProgram(litImpProgs[currImpostor.ordinal()].theProgram);
            glUniform3fv(litImpProgs[currImpostor.ordinal()].cameraSpherePosUnif, cameraSpherePos.get(vec4Buffer));
            glUniform1f(litImpProgs[currImpostor.ordinal()].sphereRadiusUnif, radius);
            
            glBindVertexArray(impostorVAO);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 1, GL_FLOAT, false, 0, 0);
            
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            
            glBindVertexArray(0);
            glUseProgram(0);
        }
        else {
    
            modelMatrix.pushMatrix();
    
            modelMatrix.translate(position);
            modelMatrix.scale(radius * 2.0f);
            
            Matrix3f normMatrix = new Matrix3f(modelMatrix);
            normMatrix.invert().transpose();
            
            glUseProgram(litMeshProg.theProgram);
            glUniformMatrix4fv(litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniformMatrix3fv(litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
            
            sphereMesh.render("lit");
            
            glUseProgram(0);
            modelMatrix.popMatrix();
        }
        
        glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
    }
    
    private void drawSphereOrbit(MatrixStackf modelMatrix, Vector3f orbitCenter, Vector3f orbitAxis,
            float orbitRadius, float orbitAlpha, float sphereRadius, MaterialNames material, boolean drawImpostor) {
    
        modelMatrix.pushMatrix();
    
        modelMatrix.translate(orbitCenter);
        modelMatrix.rotate((float) Math.toRadians(360.0f * orbitAlpha), orbitAxis);
        
        Vector3f offsetDir = new Vector3f(orbitAxis).cross(new Vector3f(0.0f, 1.0f, 0.0f));
        
        if(offsetDir.length() < 0.001f)
            offsetDir = new Vector3f(orbitAxis).cross(new Vector3f(1.0f, 0.0f, 0.0f));
        
        offsetDir.normalize();
    
        modelMatrix.translate(offsetDir.mul(orbitRadius));
        
        drawSphere(modelMatrix, new Vector3f(0.0f), sphereRadius, material, drawImpostor);
    
        modelMatrix.popMatrix();
    }
    
    private void createMaterials() {
    
        UniformBlockArray<MaterialBlock> ubArray = new UniformBlockArray <>(
                MaterialBlock.BYTES, MaterialNames.NUM_MATERIALS.ordinal());
        materialBlockOffset = ubArray.getArrayOffset();
        
        MaterialBlock matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        matBlock.specularColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        matBlock.specularShininess = 0.6f;
        ubArray.set(MaterialNames.TERRAIN.ordinal(), matBlock);
    
        matBlock.diffuseColor = new Vector4f(0.1f, 0.1f, 0.8f, 1.0f);
        matBlock.specularColor = new Vector4f(0.8f, 0.8f, 0.8f, 1.0f);
        matBlock.specularShininess = 0.1f;
        ubArray.set(MaterialNames.BLUE_SHINY.ordinal(), matBlock);
    
        matBlock.diffuseColor = new Vector4f(0.803f, 0.709f, 0.15f, 1.0f);
        matBlock.specularColor = new Vector4f(0.803f, 0.709f, 0.15f, 1.0f).mul(0.75f);
        matBlock.specularShininess = 0.18f;
        ubArray.set(MaterialNames.GOLD_METAL.ordinal(), matBlock);
    
        matBlock.diffuseColor = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
        matBlock.specularColor = new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);
        matBlock.specularShininess = 0.8f;
        ubArray.set(MaterialNames.DULL_GREY.ordinal(), matBlock);
    
        matBlock.diffuseColor = new Vector4f(0.05f, 0.05f, 0.05f, 1.0f);
        matBlock.specularColor = new Vector4f(0.95f, 0.95f, 0.95f, 1.0f);
        matBlock.specularShininess = 0.3f;
        ubArray.set(MaterialNames.BLACK_SHINY.ordinal(), matBlock);
        
        materialUniformBuffer = ubArray.createBufferObject();
    }
    
    private ProgramMeshData loadLitMeshProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
        
        ProgramMeshData data = new ProgramMeshData();
        data.theProgram = Framework.createProgram(shaderList);
    
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
        
        int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
        int lightBlock = glGetUniformLocation(data.theProgram, "Light");
        int projectionBlock = glGetUniformLocation(data.theProgram, "Projection");
        
        glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
        glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);
        
        return data;
    }
    
    private ProgramImposData loadLitImposProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
    
        ProgramImposData data = new ProgramImposData();
        data.theProgram = Framework.createProgram(shaderList);
        data.sphereRadiusUnif = glGetUniformLocation(data.theProgram, "sphereRadius");
        data.cameraSpherePosUnif = glGetUniformLocation(data.theProgram, "cameraSpherePos");
    
        int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
        int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
    
        glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
        glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
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
    
    private Vector4f calcLightPosition() {
    
        final float scale = 3.14159f * 2.0f;
        float timeThroughLoop = sphereTimer.getAlpha();
        float lightHeight = 20.0f;
        
        Vector4f lightPos = new Vector4f(0.0f, lightHeight, 0.0f, 1.0f);
        lightPos.x = (float)(Math.cos(timeThroughLoop * scale) * 20.0f);
        lightPos.y = (float)(Math.sin(timeThroughLoop * scale) * 20.0f);
        
        return lightPos;
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut13/data/";
        new BasicImpostor().start(500, 500);
    }
    
    private class ProgramMeshData {
        
        int theProgram;
        int modelToCameraMatrixUnif;
        int normalModelToCameraMatrixUnif;
    }
    
    private class ProgramImposData {
        
        int theProgram;
        int sphereRadiusUnif;
        int cameraSpherePosUnif;
    }
    
    private class UnlitProgData {
        
        int theProgram;
        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }
    
    private enum Impostors {
        
        BASIC,
        PERSPECTIVE,
        DEPTH,
        NUM_IMPOSTORS
    }
    
    private enum MaterialNames {
        
        TERRAIN,
        BLUE_SHINY,
        GOLD_METAL,
        DULL_GREY,
        BLACK_SHINY,
        NUM_MATERIALS
    }
}
