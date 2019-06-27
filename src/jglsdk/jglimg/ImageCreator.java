package jglsdk.jglimg;

import jglsdk.jglimg.ImageFormat.PixelDataType;
import jglsdk.jglimg.ImageSet.Dimensions;
import jglsdk.jglimg.Util.CompressedBlockData;

import java.util.ArrayList;
import java.util.Arrays;

class ImageCreator {
    
    private ImageFormat imageFormat;
    private Dimensions imageDimensions;
    
    private int mipmapCount;
    private int arrayCount;
    private int faceCount;
    
    private ArrayList<byte[]> imageData;
    private int[] imageSizes;
    
    ImageCreator(ImageFormat ddsFormat, Dimensions ddsDimensions, int mipmapCount, int arrayCount, int faceCount) {
        
        imageFormat = ddsFormat;
        imageDimensions = new Dimensions(ddsDimensions);
        this.mipmapCount = mipmapCount;
        this.arrayCount = arrayCount;
        this.faceCount = faceCount;
        
        if(faceCount != 6 && faceCount != 1)
            throw new RuntimeException("Bad face count.");
        
        if(faceCount == 6 && ddsDimensions.numDimensions != 2)
            throw new RuntimeException("Cubemaps must be 2D.");
        
        if(ddsDimensions.numDimensions == 3 && arrayCount != 1)
            throw new RuntimeException("No 3D texture array.");
        
        if(mipmapCount <= 0 || arrayCount <= 0)
            throw new RuntimeException("No images specified.");
        
        imageData = new ArrayList<>(mipmapCount);
        imageSizes = new int[mipmapCount];
        
        // Allocate the memory for our data.
        for(int mipmapLevel = 0; mipmapLevel < mipmapCount; mipmapLevel++) {
            
            Dimensions mipmapLevelDimensions = Util.calcMipmapLevelDimensions(ddsDimensions, mipmapLevel);
            
            int mipmapLevelSize = Util.calcMipmapLevelSize(ddsFormat, mipmapLevelDimensions);
            imageSizes[mipmapLevel] = mipmapLevelSize;
            
            byte[] mipmapLevelData = new byte[mipmapLevelSize * faceCount * arrayCount];
            imageData.add(mipmapLevelData);
        }
    }
    
    ////////////////////////////////
    void setImageData(byte sourceData[], boolean isTopLeft, int mipmapLevel, int arrayIx, int faceIx) {
        
        if(imageData.isEmpty())
            throw new RuntimeException("ImageSet already created.");
        
        // Check inputs.
        if((arrayIx < 0) || (arrayCount <= arrayIx))
            throw new ArrayIndexOutOfBoundsException();
        
        if((faceIx < 0) || (faceCount <= faceIx))
            throw new RuntimeException("Face index out of bounds.");
        
        if((mipmapLevel < 0) || (mipmapCount <= mipmapLevel))
            throw new RuntimeException("Mipmap layer out of bounds.");
        
        // Get the image relative to mipmapLevel
        byte[] imageData = this.imageData.get(mipmapLevel);
        
        if(!isTopLeft) {
            
            throw new RuntimeException("Not implemented.");
        }
        else {
            
            int imageDataOffset = ((arrayIx * faceCount) + faceIx) * imageSizes[mipmapLevel];
            copyImageFlipped(sourceData, imageData, imageDataOffset, mipmapLevel);
        }
    }
    
    ImageSet createImage() {
        
        if(imageData.isEmpty())
            throw new RuntimeException("ImageSet already created.");
        
        return new ImageSet(imageFormat, imageDimensions,
                mipmapCount, arrayCount, faceCount, imageData, imageSizes);
    }
    
    ////////////////////////////////
    private void copyImageFlipped(byte[] sourceData, byte[] imageData, int imageDataOffset, int mipmapLevel) {
        
        assert(sourceData.length * faceCount * arrayCount) == imageData.length;
        
        Dimensions mipmapImageDimensions = Util.calcMipmapLevelDimensions(new Dimensions(imageDimensions), mipmapLevel);
        
        if(imageFormat.getPixelDataType().ordinal() < PixelDataType.NUM_UNCOMPRESSED_TYPES.ordinal()) {
            
            copyPixelsFlipped(imageFormat, sourceData, imageData, imageDataOffset, imageSizes[mipmapLevel], mipmapImageDimensions);
        }
        else {
            
            // Have to decode the pixel data and flip it manually.
            switch(imageFormat.getPixelDataType()) {
                
                case COMPRESSED_BC1:
                    copyBCFlipped(imageFormat, sourceData, imageData, imageDataOffset,
                            imageSizes[mipmapLevel], mipmapImageDimensions, ImageCreator::copyBlockBC1Flipped);
                    break;
                
                default:
                    throw new RuntimeException("Not implemented.");
            }
        }
    }
    
    private void copyPixelsFlipped(ImageFormat imageFormat, byte[] sourceData, byte[] imageData, int imageDataOffset,
                                   int imageSize, Dimensions imageDimensions) {
        
        // Flip the data. Copy line by line.
        final int numLines = imageDimensions.calcNumLines();
        final int lineSize = imageFormat.alignByteCount(Util.calcBytesPerPixel(imageFormat) * imageDimensions.width);
        
        // Flipped: start from last line of source, going backward
        int sourceLineOffset = imageSize - lineSize;  // start from last line
        int imageDataLineOffset = imageDataOffset;    // start from imageDataOffset
        
        for(int line = 0; line < numLines; line++) {
            
            byte[] sourceLine = Arrays.copyOfRange(sourceData, sourceLineOffset, sourceLineOffset + lineSize);
            
            // Copy the source line into imageData
            System.arraycopy(sourceLine, 0, imageData, imageDataLineOffset, lineSize);
            
            // Update indices
            sourceLineOffset -= lineSize;
            imageDataLineOffset += lineSize;
        }
    }
    
    private void copyBCFlipped(ImageFormat imageFormat, byte[] sourceData, byte[] imageData, int imageDataOffset,
                               int imageSize, Dimensions imageDimensions, FlippingFunc flippingFunc) {
        
        // No support for 3D compressed formats.
        assert imageDimensions.numDimensions != 3 : "No support for 3D compressed formats.";
        
        CompressedBlockData blockData = Util.getBlockCompressionData(imageFormat.getPixelDataType());
        final int blocksPerLine = (imageDimensions.width + (blockData.dimensions.width - 1))
                / blockData.dimensions.width;
        
        final int blockLineSize = blocksPerLine * blockData.byteCount;
        final int numTotalBlocks = imageSize / blockData.byteCount;
        final int numLines = numTotalBlocks / blocksPerLine;
        
        // Copy each block.
        int sourceBlockOffset = imageSize - blockLineSize;  // start from last block
        int imageDataBlockOffset = imageDataOffset;         // start from imageDataOffset
        for(int line = 0; line < numLines; ++line) {
            
            for(int block = 0; block < blocksPerLine; ++block) {
                
                byte[] sourceBlock = Arrays.copyOfRange(sourceData, sourceBlockOffset,
                        sourceBlockOffset + blockData.byteCount);
                flippingFunc.call(sourceBlock, imageData, imageDataBlockOffset);
                sourceBlockOffset += blockData.byteCount;
                imageDataBlockOffset += blockData.byteCount;
            }
            
            // First goes back to beginning, second goes back one row.
            sourceBlockOffset -= blockLineSize;
            sourceBlockOffset -= blockLineSize;
        }
    }
    
    ////////////////////////////////
    private interface FlippingFunc {
        
        void call(byte[] sourceData, byte[] imageData, int imageDataOffset);
    }
    
    private static void copyBlockBC1Flipped(byte[] sourceData, byte[] imageData, int imageDataOffset) {
        
        assert sourceData.length == 8;
        
        // First 4 bytes are 2 16-bit colors. Keep them the same.
        System.arraycopy(sourceData, 0, imageData, imageDataOffset, 4);
        
        // Next four bytes are 16 2-bit values, in row-major, top-to-bottom order,
        // representing the 4x4 pixel data for the block. So copy the bytes in reverse order.
        imageData[imageDataOffset + 4] = sourceData[7];
        imageData[imageDataOffset + 5] = sourceData[6];
        imageData[imageDataOffset + 6] = sourceData[5];
        imageData[imageDataOffset + 7] = sourceData[4];
    }
}
