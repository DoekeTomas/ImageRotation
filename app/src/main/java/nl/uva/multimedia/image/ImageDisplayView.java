/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 */

package nl.uva.multimedia.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import android.util.Log;
import android.view.View;

import nl.uva.multimedia.ImageActivity;


/*
 * This is a View that displays incoming images.
 */
public class ImageDisplayView extends View implements ImageListener {

    /*** Constructors ***/

    public ImageDisplayView(Context context) {
        super(context);
    }

    public ImageDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageDisplayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*** Image drawing ***/

    private int[] currentImage = null;
    private int imageWidth, imageHeight;

    @Override
    public void onImage(int[] argb, int width, int height) {
        /* When we recieve an image, simply store it and invalidate the View so it will be
         * redrawn. */
        this.currentImage = argb;
        this.imageWidth = width;
        this.imageHeight = height;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        double rotation = Math.toRadians(ImageActivity.rotation);

        /* If there is an image to be drawn: */
        if (this.currentImage != null) {

            /* Center van de afbeelding */
            int imageCenterX = this.imageWidth / 2;
            int imageCenterY = this.imageHeight / 2;

            /* Maak een 2D array voor de pixels van de input */
            int imagePixelsNr = this.imageWidth * this.imageHeight;
            int[][] input = new int[this.imageWidth][this.imageHeight];

            int imageX, imageY;

            /* input[x,y] = color */
            for (int i = 0; i < imagePixelsNr; i++) {
                imageX = i % this.imageWidth;
                imageY = i / this.imageWidth;
                input[imageX][imageY] = this.currentImage[i];
            }

            /* Grote van de output */
            int outputWidth, outputHeight;
            if (this.imageWidth > this.imageHeight)
                outputWidth = outputHeight = (int)(this.imageWidth * 1.3);
            else
                outputWidth = outputHeight = (int)(this.imageHeight * 1.3);

            /* Center van de output */
            int outputCenterX = outputWidth / 2;
            int outputCenterY = outputHeight / 2;

            /* Maak een 2D en 1D array voor de pixels van de output */
            int outputPixelsNr = outputWidth * outputHeight;
            int[][] output = new int[outputWidth][outputHeight];
            int[] outputRgba = new int[outputPixelsNr];

            int outputX, outputY;
            double rotatedX, rotatedY;

            /* Benodigde variabelen voor bilinear interpolatie */
            int[] LT = new int[4], LB = new int[4], RT = new int[4], RB = new int[4];
            int[] interpolatedTop = new int[4], interpolatedBottom = new int[4];
            int[] interpolatedTotal = new int[4];

            /* Loop door alle pixels van de output */
            for (int i = 0; i < outputPixelsNr; i++) {
                outputX = i % outputWidth;
                outputY = i / outputHeight;

                outputX -= outputCenterX;
                outputY -= outputCenterY;

                /* Bereken de inverse van de rotatie */
                rotatedX = outputX * Math.cos(rotation) + outputY * Math.sin(rotation);
                rotatedY = -outputX * Math.sin(rotation) + outputY * Math.cos(rotation);

                outputX += outputCenterX;
                outputY += outputCenterY;

                rotatedX += imageCenterX;
                rotatedY += imageCenterY;

                if (rotatedX >= 0 && rotatedY >= 0 && rotatedX < this.imageWidth - 1 && rotatedY < this.imageHeight - 1) {
                    /* Nearest Neighbor */
                    if (ImageActivity.rotationMethod == 1) {
                        output[outputX][outputY] = input[(int)(rotatedX)][(int)(rotatedY)];
                    }

                    /* Bilinear */
                    else {
                        for (int j = 0; j < 4; j++) {
                            /* ARGB waardes van omliggende pixels */
                            LT[j] = (input[(int)(Math.floor(rotatedX))][(int)(Math.floor(rotatedY))] >> ((3 - j) * 8)) & 0xFF;
                            LB[j] = (input[(int)(Math.floor(rotatedX))][(int)(Math.ceil(rotatedY))] >> ((3 - j) * 8)) & 0xFF;
                            RT[j] = (input[(int)(Math.ceil(rotatedX))][(int)(Math.floor(rotatedY))] >> ((3 - j) * 8)) & 0xFF;
                            RB[j] = (input[(int)(Math.ceil(rotatedX))][(int)(Math.ceil(rotatedY))] >> ((3 - j) * 8)) & 0xFF;

                            /* Gecombineerde waardes van linker en rechter pixel */
                            if (Math.ceil(rotatedX) != rotatedX) {
                                /* Boven */
                                interpolatedTop[j] = (int)((Math.ceil(rotatedX) - rotatedX) * LT[j] +
                                        (rotatedX - Math.floor(rotatedX)) * RT[j]);

                                /* Onder */
                                interpolatedBottom[j] = (int)((Math.ceil(rotatedX) - rotatedX) * LB[j] +
                                        (rotatedX - Math.floor(rotatedX)) * RB[j]);
                            }

                            /* Als rotatedX op een geheel getal valt */
                            else {
                                interpolatedTop[j] = LT[j];
                                interpolatedBottom[j] = LB[j];
                            }

                            /* Gecombineerde waardes van boven en onder */
                            if (Math.ceil(rotatedY) != rotatedY) {
                                interpolatedTotal[j] = (int)((Math.ceil(rotatedY) - rotatedY) * interpolatedTop[j] +
                                        (rotatedY - Math.floor(rotatedY)) * interpolatedBottom[j]);
                            }

                            /* Als rotatedY op een geheel getal valt */
                            else {
                                interpolatedTotal[j] = interpolatedTop[j];
                            }
                        }

                        /* Zet RGBA waardes om naar int */
                        output[outputX][outputY] = Color.argb(interpolatedTotal[0], interpolatedTotal[1],
                                interpolatedTotal[2], interpolatedTotal[3]);

                    }
                }

                /* 2D -> 1D */
                if (outputX >= 0 && outputY >= 0 && outputX < outputWidth && outputY < outputHeight) {
                    outputRgba[outputY * outputWidth + outputX] = output[outputX][outputY];
                }
            }

            /* Teken de bitmap op de canvas */
            int top = (this.getWidth() - outputWidth) / 2;
            int left = (this.getHeight() - outputHeight) / 2;
            canvas.drawBitmap(outputRgba, 0, outputWidth, left, top, outputWidth, outputHeight, true, null);
        }
    }


    /*** Source selection ***/

    private ImageSource source = null;

    public void setImageSource(ImageSource source) {
        if (this.source != null) {
            this.source.setOnImageListener(null);
        }
        source.setOnImageListener(this);
        this.source = source;
    }

    public ImageSource getImageSource() {
        return this.source;
    }

}
