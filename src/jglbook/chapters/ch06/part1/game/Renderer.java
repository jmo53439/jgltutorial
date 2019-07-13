package jglbook.chapters.ch06.part1.game;

import jglbook.chapters.ch04.engine.Utils;
import jglbook.chapters.ch06.part1.engine.Window;
import jglbook.chapters.ch06.part1.engine.graph.Mesh;
import jglbook.chapters.ch06.part1.engine.graph.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderer {
    
    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    
    private Matrix4f projectionMatrix;
    private ShaderProgram shaderProgram;
    
    public Renderer() {
    
    }
    
    public void init(Window window) throws Exception {
    
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/jglbook/chapters/ch06/part1/vertex.vert"));
        shaderProgram.createFragmentShader(Utils.loadResource("/jglbook/chapters/ch06/part1/fragment.frag"));
        shaderProgram.link();
        
        float aspectRatio = (float) window.getWidth() / window.getHeight();
        projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projectionMatrix");
    }
    
    public void clear() {
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
    
    public void render(Window window, Mesh mesh) {
    
        clear();
        
        if(window.isResized()) {
            
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }
    
        shaderProgram.bind();
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        
        glBindVertexArray(mesh.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
        
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    
        shaderProgram.unbind();
    }
    
    public void cleanup() {
    
        if(shaderProgram != null)
            shaderProgram.cleanup();
    }
}
