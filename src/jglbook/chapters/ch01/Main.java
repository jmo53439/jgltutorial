package jglbook.chapters.ch01;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    
    private long window;
    
    public void run() {
    
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        
        try {
            
            init();
            loop();
            
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        }
        finally {
            
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }
    
    private void init() {
    
        GLFWErrorCallback.createPrint(System.err).set();
        
        if(!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        
        int width = 300, height = 300;
        
        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        
        if(window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        
        // key callback. called each time a key is pressed
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
           
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });
        
        // primary monitor resolution
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        
        // center
        glfwSetWindowPos(window, (vidMode.width() - width) / 2,
                (vidMode.height() - height) / 2);
        
        // self explanatory
        glfwMakeContextCurrent(window);
        
        // vsync
        glfwSwapInterval(1);
        
        // window visible
        glfwShowWindow(window);
    }
    
    private void loop() {
    
        GL.createCapabilities();
        
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        
        // run rendering loop till user == close window/ ESC
        while(!glfwWindowShouldClose(window)) {
            
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glfwSwapBuffers(window);
            
            // poll for window evts. invoke key cb
            glfwPollEvents();
        }
    }
    
    public static void main(String[] args) {
        
        // red screen
        new Main().run();
    }
}
