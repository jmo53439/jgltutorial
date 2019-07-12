package jglbook.chapters.ch02.game;

import jglbook.chapters.ch02.engine.GameEngine;
import jglbook.chapters.ch02.engine.IGameLogic;

public class Main {
    
    public static void main(String[] args) {
        
        try {
            
            boolean vSync = true;
            IGameLogic gameLogic = new DummyGame();
            GameEngine gameEngine = new GameEngine("Game", 600, 480, vSync, gameLogic);
            gameEngine.start();
        }
        catch(Exception e) {
            
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
