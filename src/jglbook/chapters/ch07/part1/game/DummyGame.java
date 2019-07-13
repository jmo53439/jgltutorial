package jglbook.chapters.ch07.part1.game;

import jglbook.chapters.ch07.part1.engine.GameItem;
import jglbook.chapters.ch07.part1.engine.IGameLogic;
import jglbook.chapters.ch07.part1.engine.Window;
import jglbook.chapters.ch07.part1.engine.graph.Mesh;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {
    
    private final Renderer renderer;
    
    private GameItem[] gameItems;
    
    private int dxInc = 0;
    private int dyInc = 0;
    private int dzInc = 0;
    private int scaleInc = 0;
    
    public DummyGame() {
        
        renderer = new Renderer();
    }
    
    @Override
    public void init(Window window) throws Exception {
    
        renderer.init(window);
        
        float[] positions = new float[] {
        
                // VO
                -0.5f,  0.5f,  0.5f,
                // V1
                -0.5f, -0.5f,  0.5f,
                // V2
                0.5f, -0.5f,  0.5f,
                // V3
                0.5f,  0.5f,  0.5f,
                // V4
                -0.5f,  0.5f, -0.5f,
                // V5
                0.5f,  0.5f, -0.5f,
                // V6
                -0.5f, -0.5f, -0.5f,
                // V7
                0.5f, -0.5f, -0.5f
        };
        
        float[] colors = new float[] {
        
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f
        };
        
        int[] indices = new int[] {
        
                // Front face
                0, 1, 3, 3, 1, 2,
                // Top Face
                4, 0, 3, 5, 4, 3,
                // Right face
                3, 2, 7, 5, 3, 7,
                // Left face
                6, 1, 0, 6, 0, 4,
                // Bottom face
                2, 1, 6, 2, 6, 7,
                // Back face
                7, 6, 4, 7, 4, 5
        };
    
        Mesh mesh = new Mesh(positions, colors, indices);
        GameItem gameItem = new GameItem(mesh);
        gameItem.setPosition(0, 0, -2);
        gameItems = new GameItem[] {gameItem};
    }
    
    @Override
    public void input(Window window) {
    
        dyInc = 0;
        dxInc = 0;
        dzInc = 0;
        scaleInc = 0;
    
        if(window.isKeyPressed(GLFW_KEY_UP)) {
            
            dyInc = 1;
        }
        else if(window.isKeyPressed(GLFW_KEY_DOWN)) {
            
            dyInc = -1;
        }
        else if(window.isKeyPressed(GLFW_KEY_LEFT)) {
            
            dxInc = -1;
        }
        else if(window.isKeyPressed(GLFW_KEY_RIGHT)) {
            
            dxInc = 1;
        }
        else if(window.isKeyPressed(GLFW_KEY_A)) {
            
            dzInc = -1;
        }
        else if(window.isKeyPressed(GLFW_KEY_Q)) {
            
            dzInc = 1;
        }
        else if(window.isKeyPressed(GLFW_KEY_Z)) {
            
            scaleInc = -1;
        }
        else if(window.isKeyPressed(GLFW_KEY_X)) {
            
            scaleInc = 1;
        }
    }
    
    @Override
    public void update(float interval) {
    
        for(GameItem gameItem : gameItems) {
    
            Vector3f itemPos = gameItem.getPosition();
            
            float px = itemPos.x + dxInc * 0.01f;
            float py = itemPos.y + dyInc * 0.01f;
            float pz = itemPos.z + dzInc * 0.01f;
    
            gameItem.setPosition(px, py, pz);
            
            float scale = gameItem.getScale();
            scale += scaleInc * 0.05f;
            
            if(scale < 0)
                scale = 0;
    
            gameItem.setScale(scale);
            
            float rotation = gameItem.getRotation().x + 1.5f;
            
            if(rotation > 360)
                rotation = 0;
    
            gameItem.setRotation(rotation, rotation, rotation);
        }
    }
    
    @Override
    public void render(Window window) {
    
        renderer.render(window, gameItems);
    }
    
    @Override
    public void cleanup() {
    
        renderer.cleanup();
        
        for(GameItem gameItem : gameItems) {
    
            gameItem.getMesh().cleanup();
        }
    }
}
