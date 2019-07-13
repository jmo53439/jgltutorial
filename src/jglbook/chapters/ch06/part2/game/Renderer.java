package jglbook.chapters.ch06.part2.game;


import jglbook.chapters.ch06.part2.engine.GameItem;
import jglbook.chapters.ch06.part2.engine.Utils;
import jglbook.chapters.ch06.part2.engine.Window;
import jglbook.chapters.ch06.part2.engine.graph.ShaderProgram;
import jglbook.chapters.ch06.part2.engine.graph.Transformation;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    
    private static final float FOV = (float) Math.toRadians(60.0f);
    
    private final Transformation transformation;
    
    private ShaderProgram shaderProgram;
    
    public Renderer() {
        
        transformation = new Transformation();
    }
    
    public void init(Window window) throws Exception {
    
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/jglbook/chapters/ch06/part2/vertex.vert"));
        shaderProgram.createFragmentShader(Utils.loadResource("/jglbook/chapters/ch06/part2/fragment.frag"));
        shaderProgram.link();
    
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("worldMatrix");
    
        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }
    
    public void render(Window window, GameItem[] gameItems) {
    
        clear();
        
        if(window.isResized()) {
            
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }
    
        shaderProgram.bind();
    
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(
                FOV, window.getWidth(), window.getWidth(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        
        for(GameItem gameItem : gameItems) {
            
            Matrix4f worldMatrix = transformation.getWorldMatrix(
                    gameItem.getPosition(), gameItem.getRotation(), gameItem.getScale());
            shaderProgram.setUniform("worldMatrix", worldMatrix);
    
            gameItem.getMesh().render();
        }
    
        shaderProgram.unbind();
    }
    
    public void clear() {
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
    
    public void cleanup() {
        
        if(shaderProgram != null)
            shaderProgram.cleanup();
    }
}
