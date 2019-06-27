package jgltut.tutorials.tut12;

import jgltut.Tutorial;
import jgltut.commons.MaterialBlock;
import jgltut.framework.Mesh;
import org.joml.Matrix3f;
import org.joml.MatrixStackf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;

public abstract class Scene {
    
    private Mesh terrainMesh;
    private Mesh cubeMesh;
    private Mesh tetraMesh;
    private Mesh cylMesh;
    private Mesh sphereMesh;
    
    private int sizeMaterialBlock;
    private int materialUniformBuffer;
    
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
    
    Scene() {
    
        terrainMesh = new Mesh("Ground.xml");
        cubeMesh = new Mesh("UnitCube.xml");
        tetraMesh = new Mesh("UnitTetrahedron.xml");
        cylMesh = new Mesh("UnitCylinder.xml");
        sphereMesh = new Mesh("UnitSphere.xml");
        
        int uniformBufferAlignSize = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
        
        sizeMaterialBlock = MaterialBlock.BYTES;
        sizeMaterialBlock += uniformBufferAlignSize - (sizeMaterialBlock % uniformBufferAlignSize);
        
        int MATERIAL_COUNT = 6;
        int sizeMaterialUniformBuffer = sizeMaterialBlock * MATERIAL_COUNT;
        
        ArrayList<MaterialBlock> materials = new ArrayList <>(MATERIAL_COUNT);
        getMaterials(materials);
    
        ByteBuffer materialsBuffer = BufferUtils.createByteBuffer(sizeMaterialUniformBuffer);
        
        for(MaterialBlock materialBlock : materials) {
            
            materialBlock.get(materialsBuffer);
            materialsBuffer.position(materialsBuffer.position() + (sizeMaterialBlock - MaterialBlock.BYTES));
        }
    
        materialsBuffer.flip();
        
        materialUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, materialUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, materialsBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }
    
    void draw(MatrixStackf modelMatrix, int materialBlockIndex, float alphaTetra) {
    
        // render ground plane
        {
            modelMatrix.pushMatrix();
            modelMatrix.rotateX((float) Math.toRadians(-90.0f));
            
            drawObject(terrainMesh, getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE),
                    materialBlockIndex, 0, modelMatrix);
    
            modelMatrix.popMatrix();
        }
        
        // render tetra object
        {
            modelMatrix.pushMatrix();
    
            modelMatrix.translate(75.0f, 5.0f, 75.0f);
            modelMatrix.rotateY((float) Math.toRadians(360.0f * alphaTetra));
            modelMatrix.scale(10.0f, 10.0f, 10.0f);
            modelMatrix.translate(0.0f, (float) Math.sqrt(2.0f), 0.0f);
            modelMatrix.rotate((float) Math.toRadians(54.735f), new Vector3f(-0.707f, 0.0f, -0.707f));
            
            drawObject(tetraMesh, "lit-color",
                    getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR),
                    materialBlockIndex, 1, modelMatrix);
    
            modelMatrix.popMatrix();
        }
        
        // render monolith object
        {
    
            modelMatrix.pushMatrix();
    
            modelMatrix.translate(88.0f, 5.0f, -80.0f);
            modelMatrix.scale(4.0f, 4.0f, 4.0f);
            modelMatrix.scale(4.0f, 9.0f, 1.0f);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);
            
            drawObject(cubeMesh, "lit",
                    getProgram(LightingProgramTypes.MTL_COLOR_DIFFUSE_SPECULAR),
                    materialBlockIndex, 2, modelMatrix);
    
            modelMatrix.popMatrix();
        }
        
        // render cube object
        {
            modelMatrix.pushMatrix();
    
            modelMatrix.translate(-52.5f, 14.0f, 65.0f);
            modelMatrix.rotateZ((float) Math.toRadians(50.0f));
            modelMatrix.rotateY((float) Math.toRadians(-10.0f));
            modelMatrix.scale(20.0f, 20.0f, 20.0f);
            
            drawObject(cubeMesh, "lit-color",
                    getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR),
                    materialBlockIndex, 3, modelMatrix);
    
            modelMatrix.popMatrix();
        }
        
        // render cylinder
        {
            modelMatrix.pushMatrix();
    
            modelMatrix.translate(-7.0f, 30.0f, -14.0f);
            modelMatrix.scale(15.0f, 55.0f, 15.0f);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);
            
            drawObject(cylMesh, "lit-color",
                    getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR),
                    materialBlockIndex, 4, modelMatrix);
    
            modelMatrix.popMatrix();
        }
        
        // render sphere
        {
            modelMatrix.pushMatrix();
    
            modelMatrix.translate(-83.0f, 14.0f, -77.0f);
            modelMatrix.scale(20.0f, 20.0f, 20.0f);
            
            drawObject(sphereMesh, "lit",
                    getProgram(LightingProgramTypes.MTL_COLOR_DIFFUSE_SPECULAR),
                    materialBlockIndex, 5, modelMatrix);
    
            modelMatrix.popMatrix();
        }
    }
    
    void drawObject(Mesh mesh, ProgramData progData, int materialBlockIndex,
            int materialIndex, MatrixStackf modelMatrix) {
    
        glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer,
                materialIndex * sizeMaterialBlock, MaterialBlock.BYTES);
    
        Matrix3f normMatrix = new Matrix3f(modelMatrix);
        normMatrix.invert().transpose();
        
        glUseProgram(progData.theProgram);
        glUniformMatrix4fv(progData.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
        
        glUniformMatrix3fv(progData.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
        mesh.render();
        
        glUseProgram(0);
        glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
    }
    
    void drawObject(Mesh mesh, String meshName, ProgramData progData,
            int materialBlockIndex, int materialIndex, MatrixStackf modelMatrix) {
    
        glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer,
                materialIndex * sizeMaterialBlock, MaterialBlock.BYTES);
        
        Matrix3f normMatrix = new Matrix3f(modelMatrix);
        normMatrix.invert().transpose();
        
        glUseProgram(progData.theProgram);
        glUniformMatrix4fv(progData.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
        glUniformMatrix3fv(progData.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
        mesh.render(meshName);
        
        glUseProgram(0);
        glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
    }
    
    private void getMaterials(ArrayList<MaterialBlock> materials) {
    
        MaterialBlock matBlock;
        
        // ground
        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(1.0f);
        matBlock.specularColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        matBlock.specularShininess = 0.6f;
        materials.add(matBlock);
        
        // tetra
        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(0.5f);
        matBlock.specularColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        matBlock.specularShininess = 0.5f;
        materials.add(matBlock);
        
        // monolith
        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(0.5f);
        matBlock.specularColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        matBlock.specularShininess = 0.4f;
        materials.add(matBlock);
    
        // cube
        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(0.5f);
        matBlock.specularColor = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
        matBlock.specularShininess = 0.1f;
        materials.add(matBlock);
        
        // cylinder
        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(0.5f);
        matBlock.specularColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        matBlock.specularShininess = 0.6f;
        materials.add(matBlock);
        
        // sphere
        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(0.63f, 0.60f, 0.02f, 1.0f);
        matBlock.specularColor = new Vector4f(0.22f, 0.20f, 0.0f, 1.0f);
        matBlock.specularShininess = 0.3f;
        materials.add(matBlock);
    }
    
    Mesh getSphereMesh() {
        
        return sphereMesh;
    }
    
    Mesh getCubeMesh() {
        
        return cubeMesh;
    }
    
    enum LightingProgramTypes {
        
        VERT_COLOR_DIFFUSE_SPECULAR,
        VERT_COLOR_DIFFUSE,
        MTL_COLOR_DIFFUSE_SPECULAR,
        MTL_COLOR_DIFFUSE,
        MAX_LIGHTING_PROGRAM_TYPES
    }
    
    static class ProgramData {
        
        int theProgram;
        int modelToCameraMatrixUnif;
        int normalModelToCameraMatrixUnif;
    }
    
    abstract ProgramData getProgram(LightingProgramTypes lightingProgramType);
}
