package jgltut;

import jgltut.framework.Framework;
import jgltut.tutorials.tut01.Tut1;
import jgltut.tutorials.tut02.FragPosition;
import jgltut.tutorials.tut02.VertexColor;
import jgltut.tutorials.tut03.CpuPositionOffset;
import jgltut.tutorials.tut03.FragChangeColor;
import jgltut.tutorials.tut03.VertCalcOffset;
import jgltut.tutorials.tut03.VertPositionOffset;
import jgltut.tutorials.tut04.AspectRatio;
import jgltut.tutorials.tut04.MatrixPerspective;
import jgltut.tutorials.tut04.OrthoCube;
import jgltut.tutorials.tut04.ShaderPerspective;
import jgltut.tutorials.tut05.*;
import jgltut.tutorials.tut06.Hierarchy;
import jgltut.tutorials.tut06.Rotation;
import jgltut.tutorials.tut06.Scale;
import jgltut.tutorials.tut06.Translation;
import jgltut.tutorials.tut07.WorldScene;
import jgltut.tutorials.tut07.WorldWithUBO;
import jgltut.tutorials.tut08.CameraRelative;
import jgltut.tutorials.tut08.GimbalLock;
import jgltut.tutorials.tut08.Interpolation;
import jgltut.tutorials.tut08.QuaternionYPR;
import jgltut.tutorials.tut09.AmbientLighting;
import jgltut.tutorials.tut09.BasicLighting;
import jgltut.tutorials.tut09.ScaleAndLighting;
import jgltut.tutorials.tut10.FragmentAttenuation;
import jgltut.tutorials.tut10.FragmentPointLighting;
import jgltut.tutorials.tut10.VertexPointLighting;
import jgltut.tutorials.tut11.BlinnVsPhongLighting;
import jgltut.tutorials.tut11.GaussianSpecularLighting;
import jgltut.tutorials.tut11.PhongLighting;
import jgltut.tutorials.tut12.GammaCorrection;
import jgltut.tutorials.tut12.HDRLighting;
import jgltut.tutorials.tut12.SceneLighting;
import jgltut.tutorials.tut13.BasicImpostor;
import jgltut.tutorials.tut13.GeomImpostor;
import jgltut.tutorials.tut14.BasicTexture;
import jgltut.tutorials.tut14.MaterialTexture;
import jgltut.tutorials.tut14.PerspectiveInterpolation;
import jgltut.tutorials.tut15.ManyImages;
import jgltut.tutorials.tut16.GammaCheckers;
import jgltut.tutorials.tut16.GammaLandscape;
import jgltut.tutorials.tut16.GammaRamp;
import jgltut.tutorials.tut17.CubePointLight;
import jgltut.tutorials.tut17.DoubleProjection;
import jgltut.tutorials.tut17.ProjectedLight;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class TutorialChooser extends JPanel implements TreeSelectionListener {
    
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Click on a tutorials to run it");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        JPanel tutorialsViewer = new TutorialChooser();
        frame.setContentPane(tutorialsViewer);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    
    private TutorialChooser() {
        
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("jgltut");
        createNodes(top);
        
        tree = new JTree(top);
        tree.addTreeSelectionListener(this);
        
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(350, 400));
        add(treeView);
    }
    
    
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if(node != null && node.isLeaf()) {
            
            final String selectedTutorial = (String) node.getUserObject();
            
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    
                    switch(selectedTutorial) {
                        
                        case "Tut1":
                            new Tut1().start(500, 500);
                            break;
                        
                        case "FragPosition":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut02/data/";
                            new FragPosition().start(500, 500);
                            break;
                        case "VertexColor":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut02/data/";
                            new VertexColor().start(500, 500);
                            break;
                        
                        
                        case "CpuPositionOffset":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut03/data/";
                            new CpuPositionOffset().start(500, 500);
                            break;
                        case "VertPositionOffset":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut03/data/";
                            new VertPositionOffset().start(500, 500);
                            break;
                        case "VertCalcOffset":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut03/data/";
                            new VertCalcOffset().start(500, 500);
                            break;
                        case "FragChangeColor":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut03/data/";
                            new FragChangeColor().start(500, 500);
                            break;
                        
                        case "OrthoCube":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut04/data/";
                            new OrthoCube().start(500, 500);
                            break;
                        case "ShaderPerspective":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut04/data/";
                            new ShaderPerspective().start(500, 500);
                            break;
                        case "MatrixPerspective":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut04/data/";
                            new MatrixPerspective().start(500, 500);
                            break;
                        case "AspectRatio":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut04/data/";
                            new AspectRatio().start(500, 500);
                            break;
                        
                        case "OverlapNoDepth":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut05/data/";
                            new OverlapNoDepth().start(500, 500);
                            break;
                        case "BaseVertexOverlap":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut05/data/";
                            new BaseVertexOverlap().start(500, 500);
                            break;
                        case "DepthBuffer":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut05/data/";
                            new DepthBuffer().start(500, 500);
                            break;
                        case "VertexClipping":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut05/data/";
                            new VertexClipping().start(500, 500);
                            break;
                        case "DepthClamping":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut05/data/";
                            new DepthClamping().start(500, 500);
                            break;
                        
                        case "Translation":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut06/data/";
                            new Translation().start(500, 500);
                            break;
                        case "Scale":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut06/data/";
                            new Scale().start(500, 500);
                            break;
                        case "Rotation":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut06/data/";
                            new Rotation().start(500, 500);
                            break;
                        case "Hierarchy":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut06/data/";
                            new Hierarchy().start(700, 700);
                            break;
                        
                        case "World Scene":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut07/data/";
                            new WorldScene().start(700, 700);
                            break;
                        case "World With UBO":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut07/data/";
                            new WorldWithUBO().start(700, 700);
                            break;
                        
                        case "GimbalLock":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut08/data/";
                            new GimbalLock().start(500, 500);
                            break;
                        case "QuaternionYPR":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut08/data/";
                            new QuaternionYPR().start(500, 500);
                            break;
                        case "CameraRelative":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut08/data/";
                            new CameraRelative().start(500, 500);
                            break;
                        case "Interpolation":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut08/data/";
                            new Interpolation().start(500, 500);
                            break;
                        
                        
                        case "Basic Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut09/data/";
                            new BasicLighting().start(500, 500);
                            break;
                        case "Scale and Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut09/data/";
                            new ScaleAndLighting().start(500, 500);
                            break;
                        case "Ambient Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut09/data/";
                            new AmbientLighting().start(500, 500);
                            break;
                        
                        case "Vertex Point Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut10/data/";
                            new VertexPointLighting().start(500, 500);
                            break;
                        case "Fragment Point Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut10/data/";
                            new FragmentPointLighting().start(500, 500);
                            break;
                        case "Fragment Attenuation":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut10/data/";
                            new FragmentAttenuation().start(500, 500);
                            break;
                        
                        case "Phong Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut11/data/";
                            new PhongLighting().start(500, 500);
                            break;
                        case "Blinn vs Phong Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut11/data/";
                            new BlinnVsPhongLighting().start(500, 500);
                            break;
                        case "Gaussian Specular Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut11/data/";
                            new GaussianSpecularLighting().start(500, 500);
                            break;
                        
                        case "Scene Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut12/data/";
                            new SceneLighting().start(700, 700);
                            break;
                        case "HDR Lighting":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut12/data/";
                            new HDRLighting().start(700, 700);
                            break;
                        case "Gamma Correction":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut12/data/";
                            new GammaCorrection().start(700, 700);
                            break;
                        
                        case "BasicImpostor":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut13/data/";
                            new BasicImpostor().start(500, 500);
                            break;
                        case "GeomImpostor":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut13/data/";
                            new GeomImpostor().start(500, 500);
                            break;
                        
                        
                        case "Basic Texture":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut14/data/";
                            new BasicTexture().start(500, 500);
                            break;
                        case "Perspective Interpolation":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut14/data/";
                            new PerspectiveInterpolation().start(500, 500);
                            break;
                        case "Material Texture":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut14/data/";
                            new MaterialTexture().start(500, 500);
                            break;
                        
                        case "Many Images":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut15/data/";
                            new ManyImages().start(500, 500);
                            break;
                        
                        case "GammaRamp":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut16/data/";
                            new GammaRamp().start(500, 195);
                            break;
                        case "Gamma Checkers":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut16/data/";
                            new GammaCheckers().start(500, 500);
                            break;
                        case "Gamma Landscape":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut16/data/";
                            new GammaLandscape().start(700, 700);
                            break;
                        
                        case "Double Projection":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut17/data/";
                            new DoubleProjection().start(700, 350);
                            break;
                        case "Projected Light":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut17/data/";
                            new ProjectedLight().start(500, 500);
                            break;
                        case "Cube Point Light":
                            Framework.CURRENT_TUTORIAL_PATH = "/jgltut/tutorials/tut17/data/";
                            new CubePointLight().start(500, 500);
                            break;
                        
                        
                        default:
                            throw new RuntimeException("Unexpected string!");
                    }
                }
            }).start();
        }
    }
    
    ////////////////////////////////
    private final JTree tree;
    
    ////////////////////////////////
    private void createNodes(DefaultMutableTreeNode top) {
        
        DefaultMutableTreeNode part1TreeNode = new DefaultMutableTreeNode("I. The Basics");
        
        String[] tutorialFiles1 = {"Tut1"};
        addTutorials(part1TreeNode, "Tut 01 Hello Triangle", tutorialFiles1);
        
        String[] tutorialFiles2 = {"FragPosition", "VertexColor"};
        addTutorials(part1TreeNode, "Tut 02 Playing with Colors", tutorialFiles2);
        
        top.add(part1TreeNode);
        
        
        DefaultMutableTreeNode part2TreeNode = new DefaultMutableTreeNode("II. Positioning");
        
        String[] tutorialFiles3 = {"CpuPositionOffset", "VertPositionOffset", "VertCalcOffset", "FragChangeColor"};
        addTutorials(part2TreeNode, "Tut 03 OpenGLs Moving Triangle", tutorialFiles3);
        
        String[] tutorialFiles4 = {"OrthoCube", "ShaderPerspective", "MatrixPerspective", "AspectRatio"};
        addTutorials(part2TreeNode, "Tut 04 Objects at Rest", tutorialFiles4);
        
        String[] tutorialFiles5 = {"OverlapNoDepth", "BaseVertexOverlap", "DepthBuffer", "VertexClipping",
                "DepthClamping"
        };
        addTutorials(part2TreeNode, "Tut 05 Objects in Depth", tutorialFiles5);
        
        String[] tutorialFiles6 = {"Translation", "Scale", "Rotation", "Hierarchy"};
        addTutorials(part2TreeNode, "Tut 06 Objects in Motion", tutorialFiles6);
        
        String[] tutorialFiles7 = {"World Scene", "World With UBO"};
        addTutorials(part2TreeNode, "Tut 07 World in Motion", tutorialFiles7);
        
        String[] tutorialFiles8 = {"GimbalLock", "QuaternionYPR", "CameraRelative", "Interpolation"};
        addTutorials(part2TreeNode, "Tut 08 Getting Oriented", tutorialFiles8);
        
        top.add(part2TreeNode);
        
        
        DefaultMutableTreeNode part3TreeNode = new DefaultMutableTreeNode("III. Illumination");
        
        String[] tutorialFiles9 = {"Basic Lighting", "Scale and Lighting", "Ambient Lighting"};
        addTutorials(part3TreeNode, "Tut 09 Lights on", tutorialFiles9);
        
        String[] tutorialFiles10 = {"Vertex Point Lighting", "Fragment Point Lighting", "Fragment Attenuation"};
        addTutorials(part3TreeNode, "Tut 10 Plane Lights", tutorialFiles10);
        
        String[] tutorialFiles11 = {"Phong Lighting", "Blinn vs Phong Lighting", "Gaussian Specular Lighting"};
        addTutorials(part3TreeNode, "Tut 11 Shinies", tutorialFiles11);
        
        String[] tutorialFiles12 = {"Scene Lighting", "HDR Lighting", "Gamma Correction"};
        addTutorials(part3TreeNode, "Tut 12 Dynamic Range", tutorialFiles12);
        
        String[] tutorialFiles13 = {"BasicImpostor", "GeomImpostor"};
        addTutorials(part3TreeNode, "Tut 13 Impostors", tutorialFiles13);
        
        top.add(part3TreeNode);
        
        
        DefaultMutableTreeNode part4TreeNode = new DefaultMutableTreeNode("IV. Texturing");
        
        String[] tutorialFiles14 = {"Basic Texture", "Perspective Interpolation", "Material Texture"};
        addTutorials(part4TreeNode, "Tut 14 Textures Are Not Pictures", tutorialFiles14);
        
        String[] tutorialFiles15 = {"Many Images"};
        addTutorials(part4TreeNode, "Tut 15 Many Images", tutorialFiles15);
        
        String[] tutorialFiles16 = {"GammaRamp", "Gamma Checkers", "Gamma Landscape"};
        addTutorials(part4TreeNode, "Tut 16 Gamma and Textures", tutorialFiles16);
        
        String[] tutorialFiles17 = {"Double Projection", "Projected Light", "Cube Point Light"};
        addTutorials(part4TreeNode, "Tut 17 Spotlight on Textures", tutorialFiles17);
        
        top.add(part4TreeNode);
    }
    
    private void addTutorials(DefaultMutableTreeNode partTreeNode, String tutorial, String[] tutorialFiles) {
        
        DefaultMutableTreeNode tutorialTreeNode = new DefaultMutableTreeNode(tutorial);
        
        for(String tutorialFile : tutorialFiles) {
            
            tutorialTreeNode.add(new DefaultMutableTreeNode(tutorialFile));
        }
        
        partTreeNode.add(tutorialTreeNode);
    }
}
