package jglbook.chapters.ch06.part2.game;

import jglbook.chapters.ch06.part2.engine.GameItem;
import jglbook.chapters.ch06.part2.engine.IGameLogic;
import jglbook.chapters.ch06.part2.engine.Window;
import jglbook.chapters.ch06.part2.engine.graph.Mesh;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {
    
    private int displayXInc = 0;
    private int displayYInc = 0;
    private int displayZInc = 0;
    private int scaleInc = 0;
    
    private final Renderer renderer;
    private GameItem[] gameItems;
    
    public DummyGame() {
        
        renderer = new Renderer();
    }
    
    @Override
    public void init(Window window) throws Exception {
    
        renderer.init(window);
        
        float[] positions = new float[] {
        
                -0.5f,  0.5f,  0.5f,
                -0.5f, -0.5f,  0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f,  0.5f,  0.5f
        };
        
        float[] colors = new float[] {
        
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f
        };
        
        int[] indices = new int[] {
                
                0, 1, 3, 3, 1, 2
        };
        
        Mesh mesh = new Mesh(positions, colors, indices);
        GameItem gameItem = new GameItem(mesh);
        gameItem.setPosition(0, 0, -2);
        gameItems = new GameItem[] {gameItem};
    }
    
    @Override
    public void input(Window window) {
    
        displayXInc = 0;
        displayYInc = 0;
        displayZInc = 0;
        scaleInc = 0;
        
        if(window.isKeyPressed(GLFW_KEY_UP)) {
            
            displayYInc = 1;
        }
        else if(window.isKeyPressed(GLFW_KEY_DOWN)) {
            
            displayYInc = -1;
        }
        else if(window.isKeyPressed(GLFW_KEY_LEFT)) {
            
            displayXInc = -1;
        }
        else if(window.isKeyPressed(GLFW_KEY_RIGHT)) {
            
            displayXInc = 1;
        }
        else if(window.isKeyPressed(GLFW_KEY_A)) {
            
            displayZInc = -1;
        }
        else if(window.isKeyPressed(GLFW_KEY_Q)) {
            
            displayZInc = 1;
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
            
            float posX = itemPos.x + displayXInc * 0.01f;
            float posY = itemPos.y + displayYInc * 0.01f;
            float posZ = itemPos.z + displayZInc * 0.01f;
    
            gameItem.setPosition(posX, posY, posZ);
            
            float scale = gameItem.getScale();
            scale += scaleInc * 0.05f;
            
            if(scale < 0)
                scale = 0;
    
            gameItem.setScale(scale);
            
            float rotation = gameItem.getRotation().z + 1.5f;
            
            if(rotation > 360)
                rotation = 0;
            
            gameItem.setRotation(0, 0, rotation);
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
