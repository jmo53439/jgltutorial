package jglbook.chapters.ch06.part1.game;

import jglbook.chapters.ch06.part1.engine.GameEngine;
import jglbook.chapters.ch06.part1.engine.IGameLogic;

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
