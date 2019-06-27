package jgltut.tutorials.tut14;

import jgltut.Tutorial;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import org.joml.MatrixStackf;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

public class PerspectiveInterpolation extends Tutorial {
    
    private Mesh realHallway;
    private Mesh fauxHallway;
    
    private ProgramData smoothInterp;
    private ProgramData linearInterp;
    
    private boolean useSmoothInterpolation = true;
    private boolean useFakeHallway;
    
    @Override
    protected void init() {

        initializePrograms();
        
        realHallway = new Mesh("RealHallway.xml");
        fauxHallway = new Mesh("FauxHallway.xml");
        
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
           
            if(action == GLFW_PRESS) {
                
                switch(key) {
                    
                    case GLFW_KEY_S:
                        useFakeHallway = !useFakeHallway;
                        
                        if(useFakeHallway) {
                            
                            System.out.printf("Fake Hallway.\n");
                        }
                        else {
                            
                            System.out.printf("Real Hallway.\n");
                        }
                        break;
                        
                    case GLFW_KEY_P:
                        useSmoothInterpolation = !useSmoothInterpolation;
                        
                        if(useSmoothInterpolation) {
                            
                            System.out.printf("Perspective Correct Interpolation.\n");
                        }
                        else {
                            
                            System.out.printf("Just Linear Interpolation.\n");
                        }
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
        
        if(useSmoothInterpolation) {
            
            glUseProgram(smoothInterp.theProgram);
        }
        else {
            
            glUseProgram(linearInterp.theProgram);
        }
        
        if(useFakeHallway) {
            
            fauxHallway.render();
        }
        else {
    
            realHallway.render();
        }
        
        glUseProgram(0);
    }

    @Override
    protected void reshape(int w, int h) {

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {

    }
    
    private void initializePrograms() {
    
        smoothInterp = loadProgram(
                "SmoothVertexColors.vert",
                "SmoothVertexColors.frag");
        linearInterp = loadProgram(
                "NoCorrectVertexColors.vert",
                "NoCorrectVertexColors.frag");
        
        float zNear = 1.0f;
        float zFar = 1000.0f;
    
        MatrixStackf persMatrix = new MatrixStackf();
        persMatrix.perspective((float) Math.toRadians(60.0f), 1.0f, zNear, zFar);
        
        glUseProgram(smoothInterp.theProgram);
        glUniformMatrix4fv(smoothInterp.cameraToClipMatrixUnif, false, persMatrix.get(mat4Buffer));
        glUseProgram(linearInterp.theProgram);
        glUniformMatrix4fv(linearInterp.cameraToClipMatrixUnif, false, persMatrix.get(mat4Buffer));
        glUseProgram(0);
    }
    
    private ProgramData loadProgram(String vertexShaderFileName, String fragmentShaderFileName) {
    
        ArrayList<Integer> shaderList = new ArrayList <>();
    
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));
        
        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.cameraToClipMatrixUnif = glGetUniformLocation(data.theProgram, "cameraToClipMatrix");
        
        return data;
    }
    
    public static void main(String[] args) {
    
        Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut14/data/";
        new PerspectiveInterpolation().start(500, 500);
    }
    
    private class ProgramData {
        
        int theProgram;
        int cameraToClipMatrixUnif;
    }
}
