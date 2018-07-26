[![Project Status: Active - Initial development has started, temporary release; work hasn't been stopped ](http://www.repostatus.org/badges/0.1.0/active.svg)](http://www.repostatus.org/#active)

BinTrasher
=============
A Binary Trashing android-API for fast image pre-processing.

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

- *public static constants*

| **TYPE**  | **NAME**   | **WHAT IT DOES**  |
|:---:| :---: | :---: |
| int | ITU_R | specifies `itu_r` LumaFormat |
| int | CCIR_601| specifies `ccir_601` LumaFormat |
| int | SMPTE_240M| specifies `smpte_240m` LumaFormat |
| int | RGB_3D | specifies 3 color-channels(*R-INDArray,G-INDArray,B-INDArray*)<br> for the 4D INDArray to be returned<br> whenever `getColorChannels()` is called |
| int | MONOCHROME_3D | specifies  3 color-channels(*R-INDArray,G-INDArray,B-INDArray*) of<br> only two colors(*BLACK n WHITE*) for the 4D INDArray to be returned<br> whenever `getColorChannels()` is called |
| int | MONOCHROME_1D | specifies 1 color-channel(*BW-INDArray*) <br> for the 2D INDArray to be returned<br> whenever `getColorChannels()` is called |
| int | GREYSCALE_3D | specifies 3 color-channels(*R-INDArray,G-INDArray,B-INDArray*) of only grey colors<br> for the 4D INDArray to be returned<br> whenever `getColorChannels()` is called |
| int | GREYSCALE_1D | specifies 1 color-channel(*GREY-INDArray*) for the 2D INDArray to be returned<br> whenever `getColorChannels()` is called |


- *public constructors*

| **CONSTRUCTOR**  |**HOW THEY CONSTRUCT**  |**SIGNATURE-ASSOCIATED METHODS**  |
|:---:|-----|:---:|
| **BinTrasher**(Bitmap rawTrash, boolean isBinary) | `rawTrash` = Your Bitmap(no Offense), `isBinary` = tell the truth if rawTrash is made up of only 2-colors(*BLACK n WHITE*) | `getBinaryMatrix()` |
| **BinTrasher**(int colorFormat,Bitmap rawTrash) | `colorFormat` = RGB_3D / MONOCHROME_3D <br>/ MONOCHROME_1D / GREYSCALE_3D / GREYSCALE_1D<br> `rawTrash` = Your Bitmap(no Offense)  | `getColorChannels()`,`getScaleColorChannels(~)` |
| **BinTrasher**(int colorFormat,Bitmap rawTrash,int resizeWidth,int resizeHeight) **throws IOException** | Same as above with the functionality of resizing<br> i.e. resizing your `rawTrash`(no Offense) | `getColorChannels()`,`getScaleColorChannels(~)`|
| **BinTrasher**(Bitmap rawTrash, int lumaFormat) | `rawTrash` = Your Bitmap(no Offense), `lumaFormat` = ITU_R / CCIR_601 / SMPTE_240M |`get888BinaryImage()`,`get565BinaryImage()`,`getBinaryMatrix()`<br>`getGreyStratifiedImage(~)`,`getColoredStratifiedImage(~)` |
| **BinTrasher**(int colorFormat,String dir) **throws IOException** | `colorFormat` = `*as above*`  , `dir` = path to your Bitmap |  `getColorChannels()`,`getScaleColorChannels(~)` |
| **BinTrasher**(int colorFormat,String dir,int resizeWidth,int resizeHeight) **throws IOException** | Same as above with the functionality of resizing<br> i.e. resizing your rawTrash(no Offense) |  `getColorChannels()`,`getScaleColorChannels(~)` |
| **BinTrasher**(String dir, int lumaFormat) **throws IOException** | `dir` = path to your Bitmap, `lumaFormat` = `*as above*` | `get888BinaryImage()`,`get565BinaryImage()`,`getBinaryMatrix()`<br>`getGreyStratifiedImage(~)`,`getColoredStratifiedImage(~)` |
| **BinTrasher**(String dir, int lumaFormat,String thresholdScaling) **throws IOException** | `dir` = path to your Bitmap, `lumaFormat` = `*as above*`, `thresholdScaling` = how much above/below the minimum-luma(the initial threshold) to use as the new threshold<br> so if it's 50% then new-threshold= old-threshold +((old-threshold-lowestLuma)\*(50/100F)<br>  or if it's -25% then new-threshold= old-threshold +((old-threshold-lowestLuma)\*(-25/100F) | `get888BinaryImage()`,`get565BinaryImage()`,`getBinaryMatrix()`<br>`getGreyStratifiedImage(~)`,`getColoredStratifiedImage(~)` |
| **BinTrasher**(Bitmap rawTrash, int lumaFormat,String thresholdScaling) | `rawTrash` =`*as above*`,`lumaFormat` = `*as above*`,`thresholdScaling` = `*as above*` | `get888BinaryImage()`,`get565BinaryImage()`,`getBinaryMatrix()`<br>`getGreyStratifiedImage(~)`,`getColoredStratifiedImage(~)` |

<center>Centered text</center>
- *public methods*

 | **NAME**  | **RETURN**  | **WHAT THEY DO**  |
 |:---:|:---:|:---:|
 | get888BinaryImage() | Bitmap | *returns* a MonoChrome Bitmap(ARGB_8888) |
 | get565BinaryImage()  | Bitmap | *returns* a MonoChrome Bitmap(RGB_565) |
 | getBinaryMatrix() | double[] | *returns* a double array of 1s and 0s(*BLACK n WHITE*) |
 | getGreyStratifiedImage(int strata) | Bitmap | *returns* a Bitmap stratified into the number of `_**strata**_` specified based on pixel-luma.<br>The image returned would be a Bitmap of `_**strata**_` colors  |
 | getColoredStratifiedImage(int[] strataColors) | Bitmap | *returns* a Bitmap stratified into the number of `_**strataColors.length**_` .<br>The image returned would be a Bitmap of `_**strataColors.length**_`  colors from your strataColors |
 | getScaleColorChannels(float start, float end) | INDArray | *returns* a flattened INDArray based on the **constructor** `colorFormat` specified.<br>The INDArray *returned*  would have its element's values range only 4rm `*start*` to `*end*` |
 | getColorChannels() | INDArray | *returns* an INDArray based on the **constructor** `colorFormat` specified  |
 | getWidth() | int  | *returns* the width of your `rawTrash`(no Offense) |
 | getHeight() | int  | *returns* the height of your `rawTrash`(no Offense) |
 | getLuma(int pixel) | float  | *returns* the Luma value of any `pixel` passed in |
 | getCCIR_601Luma(int pixel) | float  | *returns* a `ccir_601` Luma value of any `pixel` passed in  |
 | getITU_RLuma(int pixel) | float  | *returns* a `itu_r` Luma value of any `pixel` passed in  |
 | getSMPTE_240MLuma(int pixel) | float  | *returns* a `smpte_240m` Luma value of any pixel passed in  |
 | getLuma(int pixels[]) | float[]  | *returns* Luma Array of the pixels[] obtained from your `rawTrash`(no Offense)  |
 | setColorFormat(int colorFormat) | void  | *sets* `colorFormat` |
 | setLumaFormat(int lumaFormat) | void  | *sets* `lumaFormat` |
 | writeBinaryImage(String savelocation,String name) | void | *empties* `rawTrash` as a JPEG image |


