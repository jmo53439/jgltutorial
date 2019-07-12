package jglbook.chapters.ch04.engine;

public class Timer {
    
    private double prevLoopTime;
    
    public void init() {
        
        prevLoopTime = getTime();
    }
    
    public double getTime() {
        
        return System.nanoTime() / 1_000_000_000.0;
    }
    
    public float getElapsedTime() {
        
        double time = getTime();
        float elapsedTime = (float)(time - prevLoopTime);
        prevLoopTime = time;
        
        return elapsedTime;
    }
    
    public double getPrevLoopTime() {
        
        return prevLoopTime;
    }
}
