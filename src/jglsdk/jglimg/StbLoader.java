package jglsdk.jglimg;

import jglsdk.jglimg.ImageFormat.BitDepth;
import jglsdk.jglimg.ImageFormat.PixelComponents;
import jglsdk.jglimg.ImageFormat.UncheckedImageFormat;
import jglsdk.jglimg.ImageSet.Dimensions;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

import static org.lwjgl.stb.STBImage.stbi_load;

public class StbLoader {
    
    private static ImageSet buildImageSetFromIntegerData(ByteBuffer image, int width, int height, int numComponents) {
        
        Dimensions imageDimensions = new Dimensions();
        imageDimensions.numDimensions = 2;
        imageDimensions.depth = 0;
        imageDimensions.width = width;
        imageDimensions.height = height;
        
        UncheckedImageFormat uncheckedImageFormat = new UncheckedImageFormat();
        uncheckedImageFormat.type = ImageFormat.PixelDataType.NORM_UNSIGNED_INTEGER;
        
        switch(numComponents) {
            
            case 1:
                uncheckedImageFormat.format = PixelComponents.COLOR_RED;
                break;
            
            case 2:
                uncheckedImageFormat.format = PixelComponents.COLOR_RG;
                break;
            
            case 3:
                uncheckedImageFormat.format = PixelComponents.COLOR_RGB;
                break;
            
            case 4:
                uncheckedImageFormat.format = PixelComponents.COLOR_RGBA;
                break;
        }
        
        uncheckedImageFormat.order = ImageFormat.ComponentOrder.RGBA;
        uncheckedImageFormat.bitDepth = BitDepth.PER_COMP_8;
        uncheckedImageFormat.lineAlignment = 1;
        
        byte[] imageData = new byte[width * height * numComponents];
        image.get(imageData);
        
        ImageCreator imgCreator = new ImageCreator(new ImageFormat(uncheckedImageFormat), imageDimensions, 1, 1, 1);
        imgCreator.setImageData(imageData, true, 0, 0, 0);
        
        return imgCreator.createImage();
    }
    
    public static ImageSet loadFromFile(String imagePath) throws URISyntaxException {
        String path = Paths.get(StbLoader.class.getResource(imagePath).toURI()).toString();
        int[] x = new int[1];
        int[] y = new int[1];
        int[] comp = new int[1];
        ByteBuffer image = stbi_load(path, x, y, comp, 0);
        
        return buildImageSetFromIntegerData(image, x[0], y[0], comp[0]);
    }
}
