package linkersoft.blackpanther.binarytrasher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;


import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public  class BinTrasher {

            private Bitmap rawTrash;
            public static final int ITU_R=0x0;
            public static final int CCIR_601=0x1;
            public static final int SMPTE_240M=0x2;
            private static final int WHITE= Color.WHITE;
            private static final int BLACK=Color.BLACK;
            public static final int RGB_3D =0x0;
            public static final int MONOCHROME_3D =0x1;
            public static final int MONOCHROME_1D =0x2;
            public static final int GREYSCALE_3D =0x3;
            public static final int GREYSCALE_1D =0x4;
            private int lumaFormat,width,height,thresholdScaling=-111,colorFormat;
            private float  highestLuma = -1,lowestLuma = -1;
            private float[] LumaRay;

            public BinTrasher(int colorFormat,Bitmap rawTrash){
                this.rawTrash =rawTrash;
                width=rawTrash.getWidth();
                height=rawTrash.getHeight();
                this.colorFormat =colorFormat;
            }
            public BinTrasher(int colorFormat,Bitmap rawTrash,int resizeWidth,int resizeHeight) throws IOException {
                this(colorFormat,rawTrash);
                if(width!=resizeWidth||height!=resizeHeight)resize(resizeWidth, resizeHeight);
            }
            public BinTrasher(Bitmap rawTrash, int lumaFormat){
                this.rawTrash =rawTrash;
                this.lumaFormat=lumaFormat;
                width=rawTrash.getWidth();
                height=rawTrash.getHeight();
            }
            public BinTrasher(Bitmap rawTrash, int lumaFormat,String thresholdScaling){
                /*thresholdScaling=> how much above/below the minimum-luma(the initial threshold) to use as the new threshold
                * so if it's 50% then new-threshold= old-threshold +((old-threshold-lowestLuma)*(50/100F)
                * or if*it's -25% then new-threshold= old-threshold +((old-threshold-lowestLuma)*(-25/100F) */
                this(rawTrash,lumaFormat);
                this.thresholdScaling=Integer.parseInt(thresholdScaling.split("%")[0]);
            }
            public BinTrasher(int colorFormat,String dir) throws IOException {
                this(colorFormat, BitmapFactory.decodeFile(dir));
            }
            public BinTrasher(int colorFormat,String dir,int resizeWidth,int resizeHeight) throws IOException {
                this(colorFormat, BitmapFactory.decodeFile(dir),resizeWidth,resizeHeight);
            }
            public BinTrasher(String dir, int lumaFormat) throws IOException {
                this( BitmapFactory.decodeFile(dir),lumaFormat);
            }
            public BinTrasher(String dir, int lumaFormat,String thresholdScaling) throws IOException {
                this( BitmapFactory.decodeFile(dir),lumaFormat,thresholdScaling);
            }

            private void resize(int width,int height) throws IOException {
                rawTrash=  Bitmap.createScaledBitmap(rawTrash,width, height,false);
                this.width=width;
                this.height=height;
            }
            public Bitmap get888BinaryImage(){
                return getBinaryImage(false);
            }
            public Bitmap get565BinaryImage(){
                return getBinaryImage(true);
            }
            public Bitmap getBinaryImage(boolean _565){
                float lumaThreshold;
                int pixels[]=new int[width*height];
                rawTrash.getPixels(pixels,0,width,0,0,width,height);
                LumaRay=getLuma(pixels);
                lumaThreshold=getLumaThreshold();
                if(thresholdScaling!=-111)lumaThreshold=lumaThreshold+((lumaThreshold-lowestLuma)*(thresholdScaling/100F));
                for(int i = 0; i < LumaRay.length; i++){
                    if(LumaRay[i]>=lumaThreshold)pixels[i]=WHITE;
                    else pixels[i]=BLACK;
                }
                Bitmap bmapImage=Bitmap.createBitmap(width,height,_565? Bitmap.Config.RGB_565: Bitmap.Config.ARGB_8888);
                bmapImage.setPixels(pixels,0,width,0,0,width,height);
                return bmapImage;
            }
            public double[] getBinaryMatrix(){
                double matrix[]=new double[width*height];
                if(rawTrash==null){
                    float lumaThreshold;
                    int pixels[]=new int[width*height];
                    rawTrash.getPixels(pixels,0,width,0,0,width,height);
                    LumaRay=getLuma(pixels);
                    lumaThreshold=getLumaThreshold();
                    if(thresholdScaling!=-111)lumaThreshold=lumaThreshold+((lumaThreshold-lowestLuma)*(thresholdScaling/100F));
                    for(int i = 0; i < LumaRay.length; i++){
                        if(LumaRay[i]>=lumaThreshold)matrix[i]=0;
                        else matrix[i]=1;
                    }return matrix;
                }else {
                    int pixels[]=new int[width*height];
                    rawTrash.getPixels(pixels,0,width,0,0,width,height);
                    for (int i = 0; i < pixels.length; i++)matrix[i]=(pixels[i]==BLACK)?1:0;
                    return matrix;
                }
            }
            public Bitmap getGreyStratifiedImage(int strata){//returns BinTrasher image of strata shades of black2white

                int pixels[]=new int[width*height];
                rawTrash.getPixels(pixels,0,width,0,0,width,height);
                LumaRay=getLuma(pixels);
                getMinMaxLuma();
                float unitLum=(highestLuma-lowestLuma)/(float)strata;
                int strataColors[]=new  int[strata],unitstra=256/strata;
                float[] straLums=new float[strata];
                strataColors[0]=BLACK;straLums[0]=lowestLuma+unitLum;
                strataColors[strata-1]=WHITE;straLums[strata-1]=lowestLuma+(strata*unitLum);
                float lstra=straLums[0];
                for (int i = 1,stra=unitstra; i < strata-2; i++,stra+=unitstra){
                    strataColors[i]= Color.rgb(stra,stra,stra);
                    lstra+=unitLum;
                    straLums[i]=lstra;
                }for (int i = 0; i < LumaRay.length; i++){
                    float lum=LumaRay[i];
                    for (int j = 0; j < strata; j++) {
                        if(lum<=straLums[j]){
                            pixels[i]=strataColors[j];
                            break;
                        }
                    }
                }
                Bitmap greyStrat=Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
                greyStrat.setPixels(pixels,0,width,0,0,width,height);
                return greyStrat;
            }
            public Bitmap getColoredStratifiedImage(int[] strataColors){//probably if u want a colorfully stratified Img
                int strata=strataColors.length;
                int pixels[]=new int[width*height];
                rawTrash.getPixels(pixels,0,width,0,0,width,height);
                LumaRay=getLuma(pixels);
                getMinMaxLuma();
                float unitLum=(highestLuma-lowestLuma)/(float)strata;

                float[] straLums=new float[strata];
                straLums[0]=lowestLuma+unitLum;
                straLums[strata-1]=lowestLuma+(strata*unitLum);
                float lstra=straLums[0];
                for (int i = 1; i < strata-2; i++){
                    lstra+=unitLum;
                    straLums[i]=lstra;
                }for (int i = 0; i < LumaRay.length; i++){
                    float lum=LumaRay[i];
                    for (int j = 0; j < strata; j++) {
                        if(lum<=straLums[j]){
                            pixels[i]=strataColors[j];
                            break;
                        }
                    }
                }
                Bitmap coloredStrat=Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
                coloredStrat.setPixels(pixels,0,width,0,0,width,height);
                return coloredStrat;
            }
            public INDArray scaleColorChannels(float start, float end){
                int pixels[]=new int[width*height];
                rawTrash.getPixels(pixels,0,width,0,0,width,height);
                INDArray colorspace;
                int cellno=0;

                switch (colorFormat){
                    case RGB_3D:

                    double[][] Rchannel = new double[height][width];
                    double[][] Gchannel = new double[height][width];
                    double[][] Bchannel = new double[height][width];
                    int R,G,B;
                    float scaledcolor,frac;
                    for (int y = 0; y < height; y++){
                        for (int x = 0; x < width; x++) {
                            int pixel=pixels[cellno];

                            R = Color.red(pixel);
                            frac=R/255F;
                            scaledcolor=(1-frac)*start+frac*end;
                            Rchannel[y][x]=scaledcolor;

                            G =  Color.green(pixel);
                            frac=G/255F;
                            scaledcolor=(1-frac)*start+frac*end;
                            Gchannel[y][x]=scaledcolor;

                            B = Color.blue(pixel);
                            frac=B/255F;
                            scaledcolor=(1-frac)*start+frac*end;
                            Bchannel[y][x]=scaledcolor;

                            cellno++;
                         }
                    }
                    colorspace =  Nd4j.create(new double[][][]{Rchannel, Gchannel, Bchannel});
                     return colorspace;
                    case MONOCHROME_1D:
                     double[] Mchannel1D = new double[height*width];
                     for (int i = 0; i < Mchannel1D.length;i++)
                     if (pixels[i] == BLACK)Mchannel1D[i]=end;
                     colorspace =  Nd4j.create(Mchannel1D);
                     return colorspace;
                    case MONOCHROME_3D:
                        double[][] Mchannel3D = new double[height][width];
                        for (int y = 0; y < height; y++){
                            for (int x = 0; x < width; x++){
                                if (pixels[cellno] == BLACK)Mchannel3D[y][x]=end;
                                else Mchannel3D[y][x]=start;
                                cellno++;
                            }
                        }colorspace =  Nd4j.create(new double[][][]{Mchannel3D});
                         return colorspace;
                    case GREYSCALE_1D:
                        double[] GYchannel1D = new double[height*width];
                        for (int i = 0; i < GYchannel1D.length;i++){
                            int pixel=pixels[cellno];
                            int RGB = Color.red(pixel)+Color.blue(pixel)+Color.green(pixel);
                            frac=RGB/765F;
                            scaledcolor=(1-frac)*end+frac*start;
                            GYchannel1D[i]=scaledcolor;
                        }colorspace =  Nd4j.create(GYchannel1D);
                        return colorspace;
                    case GREYSCALE_3D:
                        double[][] GYchannel3D = new double[height][width];
                        for (int y = 0; y < height; y++){
                            for (int x = 0; x < width; x++){
                                int pixel=pixels[cellno];
                                int RGB = Color.red(pixel)+Color.blue(pixel)+Color.green(pixel);
                                frac=RGB/765F;
                                scaledcolor=(1-frac)*end+frac*start;
                                GYchannel3D[y][x]=scaledcolor;
                                cellno++;
                            }
                        }colorspace =  Nd4j.create(new double[][][]{GYchannel3D});
                        return colorspace;
                }return null;
            }
            public INDArray getColorChannels(){
                int pixels[]=new int[width*height];
                rawTrash.getPixels(pixels,0,width,0,0,width,height);
                INDArray colorspace;
                int cellno=0;
                switch (colorFormat){
                    case RGB_3D:
                        double[][] Rchannel = new double[height][width];
                        double[][] Gchannel = new double[height][width];
                        double[][] Bchannel = new double[height][width];
                        for (int y = 0; y < height; y++){
                            for (int x = 0; x < width; x++) {
                                int pixel=pixels[cellno];
                                Rchannel[y][x]= Color.red(pixel);
                                Gchannel[y][x]=Color.green(pixel);
                                Bchannel[y][x]= Color.blue(pixel);
                                cellno++;
                            }
                        }colorspace =  Nd4j.create(new double[][][]{Rchannel, Gchannel, Bchannel});
                        return colorspace;
                    case MONOCHROME_1D:
                        double[] Mchannel1D = new double[height*width];
                        for (int i = 0; i < Mchannel1D.length; i++)
                        if (pixels[i] == BLACK)Mchannel1D[i]=1;
                        colorspace =  Nd4j.create(Mchannel1D);
                        return colorspace;
                    case MONOCHROME_3D:
                        double[][] Mchannel3D = new double[height][width];
                        for (int y = 0; y < height; y++){
                            for (int x = 0; x < width; x++){
                                if (pixels[cellno] == BLACK)Mchannel3D[y][x]=1;
                                cellno++;
                            }
                        }colorspace =  Nd4j.create(new double[][][]{Mchannel3D});
                        return colorspace;
                    case GREYSCALE_1D:
                        double[] GYchannel1D = new double[height*width];
                        for (int i = 0; i < GYchannel1D.length;i++){
                            int pixel=pixels[cellno];
                            int RGB = Color.red(pixel)+Color.green(pixel)+Color.blue(pixel);
                            float grey=RGB/765F;
                            GYchannel1D[i]= grey;
                        }colorspace =  Nd4j.create(GYchannel1D);
                        return colorspace;
                    case GREYSCALE_3D:
                        double[][] GYchannel3D = new double[height][width];
                        for (int y = 0; y < height; y++){
                            for (int x = 0; x < width; x++){
                                int pixel=pixels[cellno];
                                int RGB = Color.red(pixel)+Color.green(pixel)+Color.blue(pixel);
                                float grey=RGB/765F;
                                GYchannel3D[y][x]=grey;
                                cellno++;
                            }
                        }colorspace =  Nd4j.create(new double[][][]{GYchannel3D});
                        return colorspace;
                }return null;
            }
            public int getWidth(){
                return width;
            }
            public int getHeight(){
                return height;
            }
            public float getLuma(int pixel){
                switch (lumaFormat){
                    case CCIR_601: return  getCCIR_601Luma(pixel);
                    case ITU_R: return getITU_RLuma(pixel);
                    case SMPTE_240M: return getSMPTE_240MLuma(pixel);
                }return 0;
            }
            public float getCCIR_601Luma(int pixel){
                int R = Color.red(pixel),G =  Color.green(pixel),B =Color.blue(pixel);
                return (R*0.2989F)+(G*0.5870F)+(B*0.1140F);
            }
            public float getITU_RLuma(int pixel){
                int R = Color.red(pixel),G =  Color.green(pixel),B =Color.blue(pixel);
                return (R*0.2126F)+(G*0.7152F)+(B*0.0722F);
            }
            public float getSMPTE_240MLuma(int pixel){
                int R = Color.red(pixel),G =  Color.green(pixel),B =Color.blue(pixel);
                return  (R*0.2120F)+(G*0.7010F)+(B*0.0870F);
            }
            public float[] getLuma(int pixels[]){
                highestLuma = -Float.MAX_VALUE;lowestLuma = Float.MAX_VALUE;
                LumaRay=new float[pixels.length];
                float lumlum;
                switch (lumaFormat){
                    case CCIR_601:
                        highestLuma = getLuma(WHITE);lowestLuma =  getLuma(BLACK);
                        for (int k = 0; k < pixels.length; k++){
                            lumlum=getCCIR_601Luma(pixels[k]);
                            LumaRay[k]=lumlum;
                            if (lumlum > highestLuma)highestLuma = lumlum;
                            if (lumlum < lowestLuma)lowestLuma =lumlum;
                        }break;
                    case ITU_R:
                        for (int k = 0; k < pixels.length; k++){
                            lumlum=getITU_RLuma(pixels[k]);
                            LumaRay[k]=lumlum;
                            if (lumlum > highestLuma)highestLuma = lumlum;
                            if (lumlum < lowestLuma)lowestLuma =lumlum;
                        }break;
                    case SMPTE_240M:
                        for (int k = 0; k < pixels.length; k++){
                            lumlum=getSMPTE_240MLuma(pixels[k]);
                            LumaRay[k]=lumlum;
                            if (lumlum > highestLuma)highestLuma = lumlum;
                            if (lumlum < lowestLuma)lowestLuma =lumlum;
                        }break;
                }return LumaRay;
            }
            private float getLumaThreshold(){
                return lowestLuma+((highestLuma-lowestLuma)/2F);
            }
            private void getMinMaxLuma(){
                highestLuma = -Float.MAX_VALUE;lowestLuma = Float.MAX_VALUE;
                for (int k = 0; k < LumaRay.length; k++){
                    if (LumaRay[k] > highestLuma)highestLuma = LumaRay[k];
                    if (LumaRay[k] < lowestLuma)lowestLuma = LumaRay[k];
                }
            }
            public static void writeBinaryImage(Bitmap binaryImage,String savelocation,String name) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                binaryImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] byt = baos.toByteArray();
                File f = new File(savelocation + "/" + name);
                try {
                    f.createNewFile();
                    FileOutputStream tasha = new FileOutputStream(f);
                    tasha.write(byt);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }


