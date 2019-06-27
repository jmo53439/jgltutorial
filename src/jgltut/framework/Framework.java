package jgltut.framework;

import jglsdk.jglutil.Shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.glDeleteShader;

public class Framework {
    
    public static String COMMON_PATH = "/jgltut/data/";
    public static String CURRENT_TUTORIAL_PATH = null;
    
    public static String findFileOrThrow(String fileName) {
    
        InputStream fileStream = Framework.class
                .getResourceAsStream(CURRENT_TUTORIAL_PATH + fileName);
        
        if(fileStream != null)
            return CURRENT_TUTORIAL_PATH + fileName;
        
        fileStream = Framework.class
                .getResourceAsStream(COMMON_PATH + fileName);
        if(fileStream != null)
            return COMMON_PATH + fileName;
        
        throw new RuntimeException("Could not find the file " + fileName);
    }
    
    public static int loadShader(int shaderType, String shaderFilename) {
    
        String filePath = Framework.findFileOrThrow(shaderFilename);
        String shaderCode = loadShaderFile(filePath);
        
        return Shader.compileShader(shaderType, shaderCode);
    }
    
    private static String loadShaderFile(String shaderFilePath) {
        
        StringBuilder sb = new StringBuilder();
        
        try {
    
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Framework.class.getResourceAsStream(shaderFilePath)));
            String line;
            
            while((line = br.readLine()) != null) {
                
                sb.append(line).append("\n");
            }
            
            br.close();
        }
        catch(IOException e) {
            
            e.printStackTrace();
        }
        
        return sb.toString();
    }
    
    public static int createProgram(ArrayList<Integer> shaders) {
        
        try {
            
            int prog = Shader.linkProgram(shaders);
            return prog;
        }
        finally {
            
            for(Integer shader : shaders) {
                
                glDeleteShader(shader);
            }
        }
    }
}
