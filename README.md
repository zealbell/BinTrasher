[![Project Status: Active - Initial development has started, temporary release; work hasn't been stopped ](http://www.repostatus.org/badges/0.1.0/active.svg)](http://www.repostatus.org/#active)

BinTrasher
=============
A Binary Trashing android-API for Image-Preprocessing.

## Quick Start

> Gradle

```xml
   dependencies {

        compile 'org.deeplearning4j:deeplearning4j-nn:1.0.0-beta'
        compile 'org.nd4j:nd4j-native:1.0.0-beta'
        compile 'org.nd4j:nd4j-native:1.0.0-beta:android-x86'
        compile 'org.nd4j:nd4j-native:1.0.0-beta:android-arm'
        compile 'com.github.54LiNKeR:BinTrasher:1.0.0'
    }
```

> JAVA

- *constants*

```java
       public static final int ITU_R
       public static final int CCIR_601
       public static final int SMPTE_240M
       public static final int RGB_3D
       public static final int MONOCHROME_3D
       public static final int MONOCHROME_1D
       public static final int GREYSCALE_3D
       public static final int GREYSCALE_1D
```

- *public constructors*

```java
       public BinTrasher(int colorFormat,Bitmap rawTrash)
       public BinTrasher(int colorFormat,Bitmap rawTrash,int resizeWidth,int resizeHeight) throws IOException
       public BinTrasher(Bitmap rawTrash, int lumaFormat)
       public BinTrasher(Bitmap rawTrash, int lumaFormat,String thresholdScaling)
       public BinTrasher(int colorFormat,String dir) throws IOException
       public BinTrasher(int colorFormat,String dir,int resizeWidth,int resizeHeight) throws IOException
       public BinTrasher(String dir, int lumaFormat) throws IOException
       public BinTrasher(String dir, int lumaFormat,String thresholdScaling) throws IOException
```

- *public constructors*

```java
       public Bitmap get888BinaryImage()
       public Bitmap get565BinaryImage()
       public Bitmap getBinaryImage(boolean _565)
       public double[] getBinaryMatrix()
       public Bitmap getGreyStratifiedImage(int strata)
       public Bitmap getColoredStratifiedImage(int[] strataColors)
       public INDArray scaleColorChannels(float start, float end)
       public INDArray getColorChannels()
       public int getWidth()
       public int getHeight()
       public float getLuma(int pixel)
       public float getCCIR_601Luma(int pixel)
       public float getITU_RLuma(int pixel)
       public float getSMPTE_240MLuma(int pixel)
       public float[] getLuma(int pixels[])
       public static void writeBinaryImage(Bitmap binaryImage,String savelocation,String name)
```

> project is still under dev