package jglbook.chapters.ch06.part2.engine;

public interface IGameLogic {
    
    void init(Window window) throws Exception;
    void input(Window window);
    void update(float interval);
    void render(Window window);
    void cleanup();
}
