package jglsdk.jglimg;

import java.util.Arrays;

import static jglsdk.jglimg.ImageFormat.*;
import static jglsdk.jglimg.ImageFormat.PixelComponents.*;
import static jglsdk.jglimg.ImageSet.*;

class Util {
    
    public static Dimensions calcMipmapLevelDimensions(Dimensions ddsDimensions, int mipmapLevel) {
    
        Dimensions mipmapLevelDimensions = new Dimensions(ddsDimensions);
        
        for(int i = 0; i < mipmapLevel; i++) {
            
            mipmapLevelDimensions.width /= 2;
            mipmapLevelDimensions.height /= 2;
            mipmapLevelDimensions.depth /= 2;
        }
        
        return mipmapLevelDimensions;
    }
    
    static int calcBytesPerPixel(ImageFormat format) {
        
        int bytesPerPixel = 0;
        
        switch(format.getBitDepth()) {
            
            case COMPRESSED:
                return 0;
    
            case PER_COMP_8:
                bytesPerPixel = 1;
                break;
    
            case PER_COMP_16:
                bytesPerPixel = 2;
                break;
    
            case PER_COMP_32:
                bytesPerPixel = 4;
                break;
    
            case PACKED_16_BIT_565:
                bytesPerPixel = 2;
                break;
    
            case PACKED_16_BIT_5551:
                bytesPerPixel = 2;
                break;
    
            case PACKED_16_BIT_4444:
                bytesPerPixel = 2;
                break;
    
            case PACKED_32_BIT_8888:
                bytesPerPixel = 4;
                break;
    
            case PACKED_32_BIT_1010102:
                bytesPerPixel = 4;
                break;
    
            case PACKED_32_BIT_248:
                bytesPerPixel = 4;
                break;
    
            case PACKED_16_BIT_565_REV:
                bytesPerPixel = 2;
                break;
    
            case PACKED_16_BIT_1555_REV:
                bytesPerPixel = 2;
                break;
    
            case PACKED_16_BIT_4444_REV:
                bytesPerPixel = 2;
                break;
    
            case PACKED_32_BIT_8888_REV:
                bytesPerPixel = 4;
                break;
    
            case PACKED_32_BIT_2101010_REV:
                bytesPerPixel = 4;
                break;
    
            case PACKED_32_BIT_101111_REV:
                bytesPerPixel = 4;
                break;
    
            case PACKED_32_BIT_5999_REV:
                bytesPerPixel = 4;
                break;
        }
        
        if(format.getBitDepth().ordinal() < BitDepth.NUM_PER_COMPONENT.ordinal())
            bytesPerPixel *= calcComponentCount(format.getPixelComponents());
        
        return bytesPerPixel;
    }
    
    static int calcComponentCount(PixelComponents component) {
        
        PixelComponents[] twoCompFormats = {COLOR_RG, DEPTH_X};
        PixelComponents[] threeCompFormats = {COLOR_RGB, COLOR_RGB_SRGB};
        PixelComponents[] fourCompFormats = {COLOR_RGBX, COLOR_RGBA, COLOR_RGBX_SRGB, COLOR_RGBA_SRGB};
    
        if(Arrays.asList(twoCompFormats).contains(component))
            return 2;
        
        if(Arrays.asList(threeCompFormats).contains(component))
            return 3;
        
        if(Arrays.asList(fourCompFormats).contains(component))
            return 4;
        
        return 1;
    }
    
    static CompressedBlockData getBlockCompressionData(PixelDataType pixelDataType) {
        
        assert pixelDataType.ordinal() >= PixelDataType.NUM_UNCOMPRESSED_TYPES.ordinal();
        
        CompressedBlockData blockData = new CompressedBlockData();
        blockData.dimensions = new Dimensions();
        blockData.dimensions.numDimensions = 2;
        blockData.dimensions.width = 4;
        blockData.dimensions.height = 4;
        
        switch(pixelDataType) {
    
            case COMPRESSED_BC1:
            
            case COMPRESSED_UNSIGNED_BC4:
            
            case COMPRESSED_SIGNED_BC4:
                blockData.byteCount = 8;
                break;
    
            default:
                blockData.byteCount = 16;
                break;
        }
        
        return blockData;
    }
    
    static int calcMipmapLevelSize(ImageFormat imageFormat, Dimensions mipmapLevelDimensions) {
    
        if(imageFormat.getPixelDataType().ordinal() >= PixelDataType.NUM_UNCOMPRESSED_TYPES.ordinal()) {
            
            assert mipmapLevelDimensions.numDimensions != 3;
        
            CompressedBlockData blockData = getBlockCompressionData(imageFormat.getPixelDataType());
            int width = (mipmapLevelDimensions.width + (blockData.dimensions.width - 1))
                    / blockData.dimensions.width;
            int height;
        
            if(mipmapLevelDimensions.numDimensions > 1) {
                
                height = (mipmapLevelDimensions.height + (blockData.dimensions.height - 1))
                        / blockData.dimensions.height;
            }
            else {
                
                assert blockData.dimensions.numDimensions >= 2;
                height = blockData.dimensions.height;
            }
        
            return width * height * blockData.byteCount;
        }
        else {
            
            int bytesPerPixel = calcBytesPerPixel(imageFormat);
            int lineSize = imageFormat.alignByteCount(bytesPerPixel * mipmapLevelDimensions.width);
        
            if(mipmapLevelDimensions.numDimensions > 1)
                lineSize *= mipmapLevelDimensions.height;
            
            if(mipmapLevelDimensions.numDimensions == 3)
                lineSize *= mipmapLevelDimensions.depth;
            
            return lineSize;
        }
    }
    
    static class CompressedBlockData {
        
        Dimensions dimensions;
        int byteCount;
    }
}
