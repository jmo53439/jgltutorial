package jgltut.framework;

public class Timer {
    
    private Type type;
    private float secDuration;
    private boolean hasUpdated;
    private boolean isPaused;
    private float absPrevTime;
    private float secAccumTime;
    
    public Timer(Type type, float duration) {
        
        this.type = type;
        secDuration = duration;
    }
    
    public boolean update(float elapsedTime) {
        
        float absCurrTime = elapsedTime;
        
        if(!hasUpdated) {
            
            absPrevTime = absCurrTime;
            hasUpdated = true;
        }
        
        if(isPaused) {
            
            absPrevTime = absCurrTime;
            return false;
        }
        
        float deltaTime = absCurrTime - absPrevTime;
        secAccumTime += deltaTime;
        absPrevTime = absCurrTime;
        
        return type == Type.SINGLE && secAccumTime > secDuration;
    }
    
    public void rewind(float secRewind) {
        
        secAccumTime -= secRewind;
        
        if(secAccumTime < 0.0f)
            secAccumTime = 0.0f;
    }
    
    public float getAlpha() {
        
        switch(type) {
            
            case LOOP:
                return (secAccumTime % secDuration) / secDuration;
                
            case SINGLE:
                return Math.min(Math.max(secAccumTime / secDuration, 0.0f), 1.0f);
                
            default:
                break;
        }
        
        return -1.0f;
    }
    
    public void fastForward(float secFF) {
        
        secAccumTime += secFF;
    }
    
    public boolean isPaused() {
        
        return isPaused;
    }
    
    public boolean togglePause() {
        
        isPaused = !isPaused;
        return isPaused;
    }
    
    public void setPause(boolean pause) {
        
        isPaused = pause;
    }
    
    public enum Type {
        
        LOOP,
        SINGLE
    }
}
