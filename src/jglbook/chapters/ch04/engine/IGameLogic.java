package jglbook.chapters.ch04.engine;

public interface IGameLogic {
    
    void init() throws Exception;
    void input(Window window);
    void update(float interval);
    void render(Window window);
    void cleanup();
}
