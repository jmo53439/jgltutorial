package jglsdk.jglutil;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    
    public static int compileShader(int shaderType, String shaderCode) {
        
        int shader = glCreateShader(shaderType);
        
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        
        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if(status == GL_FALSE) {
            
            glDeleteShader(shader);
            
            throw new CompileLinkShaderException(shader);
        }
        
        return shader;
    }
    
    public static int linkProgram(ArrayList<Integer> shaders) {
        
        int program = glCreateProgram();
        return linkProgram(program, shaders);
    }
    
    private static int linkProgram(int program, ArrayList<Integer> shaders) {
        
        for(Integer shader : shaders) {
            
            glAttachShader(program, shader);
        }
        
        glLinkProgram(program);
        
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if(status == GL_FALSE) {
            
            glDeleteProgram(program);
            throw new CompileLinkProgramException(program);
        }
        
        for(Integer shader : shaders) {
            
            glDetachShader(program, shader);
        }
        
        return program;
    }
    
    private static class CompileLinkShaderException extends RuntimeException {
        
        CompileLinkShaderException(int shader) {
            
            super(glGetShaderInfoLog(shader,
                    glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
        }
    }
    
    private static class CompileLinkProgramException extends RuntimeException {
        
        CompileLinkProgramException(int program) {
            
            super(glGetShaderInfoLog(program,
                    glGetShaderi(program, GL_INFO_LOG_LENGTH)));
        }
    }
}
