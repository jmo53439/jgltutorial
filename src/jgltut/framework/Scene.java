package jgltut.framework;

import jglsdk.jglimg.DdsLoader;
import jglsdk.jglimg.ImageSet;
import jglsdk.jglimg.TextureGenerator;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL33.*;

public class Scene {
    
    private SceneImpl sceneImpl;
    
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
    
    public Scene(String filename) {
        
        sceneImpl = new SceneImpl(filename);
    }
    
    ////////////////////////////////
    public void render(Matrix4f cameraMatrix) {
        
        sceneImpl.render(cameraMatrix);
    }
    
    
    public SceneNode findNode(String nodeName) {
        
        return sceneImpl.findNode(nodeName);
    }
    
    public int findProgram(String progName) {
        
        return sceneImpl.findProgram(progName);
    }
    
    public Mesh findMesh(String meshName) {
        
        return sceneImpl.findMesh(meshName);
    }
    
    ////////////////////////////////
    private class SceneMesh {
    
        private Mesh mesh;
        
        SceneMesh(String filename) {
            
            mesh = new Mesh(filename);
        }
        
        void render() {
            
            mesh.render();
        }
        
        Mesh getMesh() {
            
            return mesh;
        }
    }
    
    private class SceneTexture {
    
        private int textureObj, textureType;
        
        SceneTexture(String filename, int creationFlags) {
            
            String filePath = Framework.findFileOrThrow(filename);
            ImageSet imageSet = null;
            
            String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
            
            if(fileExtension.equals("dds")) {
                
                try {
                    
                    imageSet = DdsLoader.loadFromFile(filePath);
                }
                catch (IOException e) {
                    
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            else {
                
                // ImageSet.reset(glimg::loaders::stb::LoadFromFile(pathname.c_str()));
                assert false : fileExtension + " loader not yet implemented";
            }
            
            textureObj = TextureGenerator.createTexture(imageSet, creationFlags);
            textureType = TextureGenerator.getTextureType(imageSet, creationFlags);
        }
        
        
        int getTexture() {
            
            return textureObj;
        }
        
        int getType() {
            
            return textureType;
        }
    }
    
    private class SceneProgram {
    
        private int programObj, matrixLoc, normalMatLoc;
        
        SceneProgram(int programObj, int matrixLoc, int normalMatLoc) {
            
            this.programObj = programObj;
            this.matrixLoc = matrixLoc;
            this.normalMatLoc = normalMatLoc;
        }
        
        void useProgram() {
            
            glUseProgram(programObj);
        }
        
        int getMatrixLoc() {
            
            return matrixLoc;
        }
        
        int getNormalMatLoc() {
            
            return normalMatLoc;
        }
        
        int getProgram() {
            
            return programObj;
        }
    }
    
    
    public class SceneNode {
    
        private SceneMesh mesh;
        private SceneProgram program;
    
        private ArrayList<SceneBinders.StateBinder> stateBinders;
        private ArrayList<TextureBinding> textureBindings;
    
        private Transform nodeTransform;
        private Transform objTransform;
        
        SceneNode(SceneMesh mesh, SceneProgram program, Vector3f nodePos,
                  ArrayList<TextureBinding> textureBindings) {
            
            this.mesh = mesh;
            this.program = program;
            
            stateBinders = new ArrayList<>();
            this.textureBindings = textureBindings;
            
            nodeTransform = new Transform();
            objTransform = new Transform();
            
            nodeTransform.trans = nodePos;
        }
        
        
        public void nodeSetOrient(Quaternionf orient) {
            
            nodeTransform.orient = new Quaternionf(orient);
        }
        
        public Quaternionf nodeGetOrient() {
            
            return nodeTransform.orient;
        }
        
        
        void setNodeScale(Vector3f nodeScale) {
            
            nodeTransform.scale = new Vector3f(nodeScale);
        }
        
        void setNodeOrient(Quaternionf nodeOrient) {
            
            nodeTransform.orient = new Quaternionf(nodeOrient).normalize();
        }
        
        void render(int samplers[], Matrix4f mat) {
            
            Matrix4f baseMat = new Matrix4f(mat);
            baseMat.mul(nodeTransform.getMatrix());
            Matrix4f objMat = new Matrix4f(baseMat).mul(objTransform.getMatrix());
            
            program.useProgram();
            glUniformMatrix4fv(program.getMatrixLoc(), false, objMat.get(mat4Buffer));
            
            if(program.getNormalMatLoc() != -1) {
                
                Matrix3f normMat = new Matrix3f(objMat).invert().transpose();
                glUniformMatrix3fv(program.getNormalMatLoc(), false, normMat.get(mat3Buffer));
            }
            
            for(SceneBinders.StateBinder stateBinder : stateBinders) {
                
                stateBinder.bindState(program.getProgram());
            }
            
            for(TextureBinding binding : textureBindings) {
                
                glActiveTexture(GL_TEXTURE0 + binding.textureUnit);
                glBindTexture(binding.texture.getType(), binding.texture.getTexture());
                glBindSampler(binding.textureUnit, samplers[binding.sampler.ordinal()]);
            }
            
            mesh.render();
            
            for(TextureBinding binding : textureBindings) {
                
                glActiveTexture(GL_TEXTURE0 + binding.textureUnit);
                glBindTexture(binding.texture.getType(), 0);
                glBindSampler(binding.textureUnit, 0);
            }
            
            for(SceneBinders.StateBinder stateBinder : stateBinders) {
                
                stateBinder.unbindState(program.getProgram());
            }
            
            glUseProgram(0);
        }
        
        void setStateBinder(SceneBinders.StateBinder stateBinder) {
            
            stateBinders.add(stateBinder);
        }
        
        int getProgram() {
            
            return program.getProgram();
        }
    }
    
    ////////////////////////////////
    private class Transform {
    
        private Quaternionf orient;
        private Vector3f scale, trans;
        
        Transform() {
            
            orient = new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
            scale = new Vector3f(1.0f, 1.0f, 1.0f);
            trans = new Vector3f(0.0f, 0.0f, 0.0f);
        }
        
        Matrix4f getMatrix() {
            
            Matrix4f ret = new Matrix4f();
            ret.translate(trans);
            ret.mul(orient.get(new Matrix4f()));
            ret.scale(scale);
            return ret;
        }
    }
    
    ////////////////////////////////
    private enum SamplerTypes {
        
        NEAREST,
        LINEAR,
        MIPMAP_NEAREST,
        MIPMAP_LINEAR,
        ANISOTROPIC,
        HALF_ANISOTROPIC,
        
        MAX_SAMPLERS
    }
    
    
    private SamplerTypes getTypeFromName(String samplerName) {
        
        final String[] samplerNames = {
                
                "nearest", "linear",
                "mipmap nearest", "mipmap linear",
                "anisotropic", "half anisotropic"
        };
        
        for(int nameIndex = 0; nameIndex < samplerNames.length; nameIndex++) {
            
            if(samplerNames[nameIndex].equals(samplerName))
                return SamplerTypes.values()[nameIndex];
        }
        
        throw new RuntimeException("Unknown sampler name: " + samplerName);
    }
    
    private void makeSamplerObjects(int[] samplers) {
        
        // Always repeat.
        for(int samplerIndex = 0; samplerIndex < SamplerTypes.MAX_SAMPLERS.ordinal(); samplerIndex++) {
            
            samplers[samplerIndex] = glGenSamplers();
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_S, GL_REPEAT);
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_T, GL_REPEAT);
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_R, GL_REPEAT);
        }
        
        glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        
        glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        
        glSamplerParameteri(samplers[2], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(samplers[2], GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        
        glSamplerParameteri(samplers[3], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[3], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        
        float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
        
        glSamplerParameteri(samplers[4], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[4], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameterf(samplers[4], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso / 2.0f);
        
        glSamplerParameteri(samplers[5], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[5], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameterf(samplers[5], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
    }
    
    ////////////////////////////////
    private class TextureBinding {
        
        SceneTexture texture;
        int textureUnit;
        SamplerTypes sampler;
    }
    
    ////////////////////////////////
    private class SceneImpl {
        
        private Map<String, SceneMesh> meshes;
        private Map<String, SceneTexture> textures;
        private Map<String, SceneProgram> programs;
        private Map<String, SceneNode> nodes;
        
        private ArrayList<SceneNode> rootNodes;
        
        private int[] samplers;
        
        private SceneImpl(String filename) {
            
            meshes = new HashMap<>();
            textures = new HashMap<>();
            programs = new HashMap<>();
            nodes = new HashMap<>();
            
            rootNodes = new ArrayList<>();
            
            samplers = new int[SamplerTypes.MAX_SAMPLERS.ordinal()];
            
            // Read the xml scene.
            Document document = null;
            
            try {
                
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                
                String xmlPath = Framework.findFileOrThrow(filename);
                InputStream xmlInputStream = Scene.class.getResourceAsStream(xmlPath);
                document = documentBuilder.parse(xmlInputStream);
            }
            catch(SAXException | ParserConfigurationException | IOException e) {
                
                e.printStackTrace();
                System.exit(-1);
            }
            
            Element sceneXmlNode = document.getDocumentElement();
            
            readMeshes(sceneXmlNode);
            readTextures(sceneXmlNode);
            readPrograms(sceneXmlNode);
            readNodes(sceneXmlNode);
            
            makeSamplerObjects(samplers);
        }
        
        ////////////////////////////////
        private void render(Matrix4f cameraMat) {
            
            for(SceneNode sceneNode : nodes.values()) {
                
                sceneNode.render(samplers, cameraMat);
            }
        }
        
        ////////////////////////////////
        private SceneNode findNode(String nodeName) {
            
            SceneNode node = nodes.get(nodeName);
            
            if(node == null)
                throw new RuntimeException("Could not find the node named: " + nodeName);
            
            return node;
        }
        
        private int findProgram(String progName) {
            
            SceneProgram program = programs.get(progName);
            
            if(program == null)
                throw new RuntimeException("Could not find the program named: " + progName);
            
            return program.getProgram();
        }
        
        private Mesh findMesh(String meshName) {
            
            SceneMesh mesh = meshes.get(meshName);
            
            if(mesh == null)
                throw new RuntimeException("Could not find the mesh named: " + meshName);
            
            return mesh.getMesh();
        }
        
        ////////////////////////////////
        private void readMeshes(Element sceneNode) {
            
            ArrayList<Element> meshElements = getElementsByTagName(sceneNode, "mesh");
            
            for(Element element : meshElements) {
                
                readMesh(element);
            }
        }
        
        private void readMesh(Element meshNode) {
            
            String nameNode = meshNode.getAttribute("xml:id");
            String filenameNode = meshNode.getAttribute("file");
            
            {
                if(nameNode.equals(""))
                    throw new RuntimeException("Mesh found with no `xml:id` name specified.");
                
                if(filenameNode.equals(""))
                    throw new RuntimeException("Mesh found with no `file` filename specified.");
                
                if(meshes.containsKey(nameNode))
                    throw new RuntimeException("The mesh named \"" + nameNode + "\" already exists.");
            }
            
            SceneMesh mesh = new SceneMesh(filenameNode);
            meshes.put(nameNode, mesh);
        }
        
        
        private void readTextures(Element sceneNode) {
            
            ArrayList<Element> textureElements = getElementsByTagName(sceneNode, "texture");
            
            for(Element element : textureElements) {
                
                readTexture(element);
            }
        }
        
        private void readTexture(Element textureNode) {
            
            String nameNode = textureNode.getAttribute("xml:id");
            String filenameNode = textureNode.getAttribute("file");
            
            {
                if(nameNode.equals(""))
                    throw new RuntimeException("Texture found with no `xml:id` name specified.");
                
                if(filenameNode.equals(""))
                    throw new RuntimeException("Texture found with no `file` filename specified.");
                
                if(textures.containsKey(nameNode))
                    throw new RuntimeException("The texture named \"" + nameNode + "\" already exists.");
            }
            
            int creationFlags = 0;
            
            if(textureNode.getAttribute("srgb").equals("true"))
                creationFlags = creationFlags | TextureGenerator.ForcedConvertFlags.FORCE_SRGB_COLORSPACE_FMT;
            
            SceneTexture texture = new SceneTexture(filenameNode, creationFlags);
            textures.put(nameNode, texture);
        }
        
        
        private void readPrograms(Element sceneNode) {
            
            ArrayList<Element> programElements = getElementsByTagName(sceneNode, "prog");
            
            for(Element element : programElements) {
                
                readProgram(element);
            }
        }
        
        private void readProgram(Element programNode) {
            
            String nameNode = programNode.getAttribute("xml:id");
            String vertexShaderNode = programNode.getAttribute("vert");
            String fragmentShaderNode = programNode.getAttribute("frag");
            String modelMatrixNode = programNode.getAttribute("model-to-camera");
            
            // Optional.
            String normalMatrixNode = programNode.getAttribute("normal-model-to-camera");
            String geometryShaderNode = programNode.getAttribute("geom");
            
            {
                if(nameNode.equals(""))
                    throw new RuntimeException("Program found with no `xml:id` name specified.");
                
                if(vertexShaderNode.equals(""))
                    throw new RuntimeException("Program found with no `vert` filename specified.");
                
                if(fragmentShaderNode.equals(""))
                    throw new RuntimeException("Program found with no `frag` name specified.");
                
                if(modelMatrixNode.equals(""))
                    throw new RuntimeException("Program found with no `model-to-camera` filename specified.");
                
                if(programs.containsKey(nameNode))
                    throw new RuntimeException("The program named \"" + nameNode + "\" already exists.");
            }
            
            int program;
            
            {
                ArrayList<Integer> shaders = new ArrayList<>();
                shaders.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderNode));
                shaders.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderNode));
                
                if(!geometryShaderNode.equals(""))
                    shaders.add(Framework.loadShader(GL_GEOMETRY_SHADER, geometryShaderNode));
                
                program = Framework.createProgram(shaders);
                
                for(Integer integer : shaders) {
                    
                    glDeleteShader(integer);
                }
            }
            
            int matrixLoc = glGetUniformLocation(program, modelMatrixNode);
            
            if(matrixLoc == -1) {
                
                glDeleteProgram(program);
                throw new RuntimeException("Could not find the matrix uniform " +
                        modelMatrixNode + " in program " + nameNode);
            }
            
            int normalMatLoc = -1;
            
            if(!normalMatrixNode.equals("")) {
                
                normalMatLoc = glGetUniformLocation(program, normalMatrixNode);
                
                if(normalMatLoc == -1) {
                    
                    glDeleteProgram(program);
                    throw new RuntimeException("Could not find the normal matrix uniform " +
                            normalMatrixNode + " in program " + nameNode);
                }
            }
            
            programs.put(nameNode, new SceneProgram(program, matrixLoc, normalMatLoc));
            readProgramContents(program, programNode);
        }
        
        private void readProgramContents(int program, Element programNode) {
            
            Set<String> blockBindings = new HashSet<>();
            Set<String> samplerBindings = new HashSet<>();
            
            {
                NodeList nodes = programNode.getElementsByTagName("block");
                
                for(int nodeIx = 0; nodeIx < nodes.getLength(); nodeIx++) {
                    
                    Node node = nodes.item(nodeIx);
                    
                    if(node.getParentNode().isEqualNode(programNode)) {
                        
                        if(node.getNodeType() == Node.ELEMENT_NODE) {
                            
                            Element element = (Element) node;
                            
                            String nameNode = element.getAttribute("name");
                            String bindingNode = element.getAttribute("binding");
                            
                            {
                                if(nameNode.equals(""))
                                    throw new RuntimeException("Program `block` element with no `name`.");
                                
                                if(bindingNode.equals(""))
                                    throw new RuntimeException("Program `block` element with no `binding`.");
                                
                                if(blockBindings.contains(nameNode)) {
                                    throw new RuntimeException("The uniform block " + nameNode +
                                            " is used twice in the same program.");
                                }
                                else {
                                    
                                    blockBindings.add(nameNode);
                                }
                            }
                            
                            int blockIx = glGetUniformBlockIndex(program, nameNode);
                            
                            if(blockIx == GL_INVALID_INDEX)
                                System.out.println("Warning: the uniform block " + nameNode + " could not be found.");
                            
                            
                            int bindPoint = Integer.parseInt(bindingNode);
                            glUniformBlockBinding(program, blockIx, bindPoint);
                        }
                    }
                }
            }
            
            {
                NodeList nodes = programNode.getElementsByTagName("sampler");
                
                for(int nodeIx = 0; nodeIx < nodes.getLength(); nodeIx++) {
                    
                    Node node = nodes.item(nodeIx);
                    
                    if(node.getParentNode().isEqualNode(programNode)) {
                        
                        if(node.getNodeType() == Node.ELEMENT_NODE) {
                            
                            Element element = (Element) node;
                            
                            String nameNode = element.getAttribute("name");
                            String texunitNode = element.getAttribute("unit");
                            
                            {
                                if(nameNode.equals(""))
                                    throw new RuntimeException("Program `sampler` element with no `name`.");
                                
                                if(texunitNode.equals(""))
                                    throw new RuntimeException("Program `sampler` element with no `unit`.");
                                
                                if(samplerBindings.contains(nameNode)) {
                                    
                                    throw new RuntimeException("A sampler " + nameNode + " is used twice within the same program.");
                                }
                                else {
                                    
                                    samplerBindings.add(nameNode);
                                }
                            }
                            
                            int samplerLoc = glGetUniformLocation(program, nameNode);
                            
                            if(samplerLoc == -1)
                                System.out.println("Warning: the sampler " + nameNode + " could not be found.");
                            
                            int textureUnit = Integer.parseInt(texunitNode);
                            glUseProgram(program);
                            glUniform1i(samplerLoc, textureUnit);
                            glUseProgram(0);
                        }
                    }
                }
            }
        }
        
        private void readNodes(Element sceneNode) {
            
            ArrayList<Element> nodeElements = getElementsByTagName(sceneNode, "node");
            
            for(Element element : nodeElements) {
                
                readNode(null, element);
            }
        }
        
        private void readNode(SceneNode parent, Element nodeNode) {
            
            String nameNode = nodeNode.getAttribute("name");
            String meshNode = nodeNode.getAttribute("mesh");
            String progNode = nodeNode.getAttribute("prog");
            
            String positionNode = nodeNode.getAttribute("pos");
            String orientNode = nodeNode.getAttribute("orient");
            String scaleNode = nodeNode.getAttribute("scale");
            
            {
                if(nameNode.equals(""))
                    throw new RuntimeException("Node found with no `name` name specified.");
                
                if(meshNode.equals(""))
                    throw new RuntimeException("Node found with no `mesh` filename specified.");
                
                if(progNode.equals(""))
                    throw new RuntimeException("Node found with no `prog` name specified.");
                
                if(positionNode.equals(""))
                    throw new RuntimeException("Node found with no `pos` name specified.");
                
                if(nodes.containsKey(nameNode))
                    throw new RuntimeException("The node named \"" + nameNode + "\" already exists.");
                
                if(!meshes.containsKey(meshNode)) throw new RuntimeException("The node named \"" + nameNode +
                        "\" references the mesh \"" + meshNode + "\" which does " + "not exist.");
                
                if(!programs.containsKey(progNode)) throw new RuntimeException("The node named \"" + nameNode +
                        "\" references the program \"" + progNode + "\" which does not exist.");
            }
            
            Vector3f nodePos = attribToVec3(positionNode);
            SceneNode node = new SceneNode(meshes.get(meshNode), programs.get(progNode),
                    nodePos, readNodeTextures(nodeNode));
            nodes.put(nameNode, node);
            
            // TODO: parent/child nodes.
            if(parent == null)
                rootNodes.add(node);
            
            if(!orientNode.equals(""))
                node.setNodeOrient(attribToQuat(orientNode));
            
            if(!scaleNode.equals("")) {
                
                if(attribIsVec3(scaleNode)) {
                    
                    node.setNodeScale(attribToVec3(scaleNode));
                }
                else {
                    
                    float unifScale = attribToFloat(scaleNode);
                    node.setNodeScale(new Vector3f(unifScale));
                }
            }
        }
        
        private ArrayList<TextureBinding> readNodeTextures(Element node) {
            
            ArrayList<TextureBinding> textureBindings = new ArrayList<>();
            Set<Integer> textureUnits = new HashSet<>();
            
            ArrayList<Element> textureElements = getElementsByTagName(node, "texture");
            
            for(Element textureElement : textureElements) {
                
                String nameNode = textureElement.getAttribute("name");
                String unitName = textureElement.getAttribute("unit");
                String samplerName = textureElement.getAttribute("sampler");
                
                {
                    if(nameNode.equals(""))
                        throw new RuntimeException("Textures on nodes must have a `name` attribute.");
                    
                    if(unitName.equals(""))
                        throw new RuntimeException("Textures on nodes must have a `unit` attribute.");
                    
                    if(samplerName.equals(""))
                        throw new RuntimeException("Textures on nodes must have a `sampler` attribute.");
                    
                    if(!textures.containsKey(nameNode))
                        throw new RuntimeException("The node texture named \"" + nameNode + "\" is a texture which does not exist.");
                }
                
                TextureBinding binding = new TextureBinding();
                binding.texture = textures.get(nameNode);
                binding.textureUnit = attribToInt(unitName);
                binding.sampler = getTypeFromName(samplerName);
                
                {
                    if (textureUnits.contains(binding.textureUnit))
                        throw new RuntimeException("Multiply bound texture unit in node texture " + nameNode);
                }
                
                textureBindings.add(binding);
                textureUnits.add(binding.textureUnit);
            }
            
            return textureBindings;
        }
        
        ////////////////////////////////
        private ArrayList<Element> getElementsByTagName(Element parent, String name) {
            
            ArrayList<Element> elements = new ArrayList<>();
            
            NodeList childs = parent.getChildNodes();
            
            for(int nodeIx = 0; nodeIx < childs.getLength(); nodeIx++) {
                
                Node node = childs.item(nodeIx);
                
                if(node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(name))
                    elements.add((Element) node);
            }
            
            return elements;
        }
        
        
        private int attribToInt(String attrib) {
            
            return Integer.parseInt(attrib);
        }
        
        private float attribToFloat(String attrib) {
            
            return Float.parseFloat(attrib);
        }
        
        private boolean attribIsVec3(String attrib) {
            
            try {
                
                attribToVec3(attrib);
                return true;
            }
            catch (RuntimeException e) {
                
                return false;
            }
        }
        
        private Vector3f attribToVec3(String attrib) {
            
            Scanner scanner = new Scanner(attrib);
            Vector3f vec = new Vector3f();
            vec.x = Float.parseFloat(scanner.next());
            vec.y = Float.parseFloat(scanner.next());
            vec.z = Float.parseFloat(scanner.next());
            scanner.close();
            
            return vec;
        }
        
        private Quaternionf attribToQuat(String attrib) {
            
            Scanner scanner = new Scanner(attrib);
            Quaternionf quat = new Quaternionf();
            quat.x = Float.parseFloat(scanner.next());
            quat.y = Float.parseFloat(scanner.next());
            quat.z = Float.parseFloat(scanner.next());
            quat.w = Float.parseFloat(scanner.next());
            scanner.close();
            
            return quat;
        }
    }
}
