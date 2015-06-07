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

        // TODO: Hier wordt een afbeelding op het scherm laten zien!
        // Je zou hier dus code kunnen plaatsen om iets anders weer te geven.

        /* If there is an image to be drawn: */
        if (this.currentImage != null) {

            /* Width & Height */
            int width   = this.imageWidth;
            int height  = this.imageHeight;

            double halfWidth = 0.5 * width;
            double halfHeight = 0.5 * height;

            /* Center the image... */
            int left    = (this.getWidth() - width) / 2;
            int top     = (this.getHeight() - height) / 2;

            /* Centerpoint of the image */
            int xCenter = width / 2;
            int yCenter = height / 2;

            /* x & y declareren */
            int x, y;
            int x_in, y_in;
            int x_floor, x_ceiling, y_floor, y_ceiling;
            double x_inp, y_inp;

            /* kleuren */
            int[] topLeft       = new int[4];
            int[] topRight      = new int[4];
            int[] bottomLeft    = new int[4];
            int[] bottomRight   = new int[4];

            /* interpolated waardes */
            double[] interpolatedTop    = new double[4];
            double[] interpolatedBottom = new double[4];
            int[] interpolatedFinal     = new int[4];

            int pixels = width * height;
            int[] output = new int[pixels];

            int element;
            int RGB;

            boolean billinear = true;

            double rotation = Math.toRadians(10);

            for (int i = 0; i < pixels; i++) {
                if (billinear) {
                    /* Omzetten naar cartesische coordinaten */
                    x = i % width - xCenter;
                    y = yCenter - i / width;

                    /* Input coordinaten */
                    x_inp = x * Math.cos(rotation) + y * Math.sin(rotation);
                    y_inp = -x * Math.sin(rotation) + y * Math.cos(rotation);

                    /* Cartesisch -> 2D-raster */
                    x_inp = x_inp + halfWidth;
                    y_inp = halfHeight - y_inp;

                    /* Omliggende pixels */
                    x_floor     = (int)Math.floor(x_inp);
                    x_ceiling   = (int)Math.ceil(x_inp);
                    y_floor     = (int)Math.floor(y_inp);
                    y_ceiling   = (int)Math.ceil(y_inp);

                    /* Check bereik & domein */
                    if (x_floor < 0 || x_ceiling < 0 || y_floor < 0 || y_ceiling < 0 || x_floor >= width || x_ceiling >= width || y_floor >= height || y_ceiling >= height) {
                        continue;
                    }

                    /* Kleurwaardes toekennen */
                    topLeft[0]  = Color.red(this.currentImage[(int) (x_floor + y_floor * width)]);
                    topLeft[1]  = Color.green(this.currentImage[(int) (x_floor + y_floor * width)]);
                    topLeft[2]  = Color.blue(this.currentImage[(int) (x_floor + y_floor * width)]);
                    topLeft[3]  = Color.alpha(this.currentImage[(int) (x_floor + y_floor * width)]);

                    topRight[0] = Color.red(this.currentImage[(int) (x_ceiling + y_floor * width)]);
                    topRight[1] = Color.green(this.currentImage[(int) (x_ceiling + y_floor * width)]);
                    topRight[2] = Color.blue(this.currentImage[(int) (x_ceiling + y_floor * width)]);
                    topRight[3] = Color.alpha(this.currentImage[(int) (x_ceiling + y_floor * width)]);

                    bottomLeft[0] = Color.red(this.currentImage[(int) (x_floor + y_ceiling * width)]);
                    bottomLeft[1] = Color.green(this.currentImage[(int) (x_floor + y_ceiling * width)]);
                    bottomLeft[2] = Color.blue(this.currentImage[(int) (x_floor + y_ceiling * width)]);
                    bottomLeft[3] = Color.alpha(this.currentImage[(int) (x_floor + y_ceiling * width)]);

                    bottomRight[0] = Color.red(this.currentImage[(int) (x_ceiling + y_ceiling * width)]);
                    bottomRight[1] = Color.green(this.currentImage[(int) (x_ceiling + y_ceiling * width)]);
                    bottomRight[2] = Color.blue(this.currentImage[(int) (x_ceiling + y_ceiling * width)]);
                    bottomRight[3] = Color.alpha(this.currentImage[(int) (x_ceiling + y_ceiling * width)]);

                    /* Interpoleer de bovenste punten */

                    for (int j = 0; j < 4; j++ ) {
                        interpolatedTop[j] = (1 - x_inp - x_floor) * topLeft[j] + (x_inp - x_floor) * topRight[j];
                    }

                    /* Interpoleer de onderste punten */

                    for (int j = 0; j < 4; j++ ) {
                        interpolatedBottom[j] = (1 - x_inp - x_floor) * bottomLeft[j] + (x_inp - x_floor) * bottomRight[j];
                    }

                    /* Interpoleer de geinterpoleerde waardes nog eens */
                    for (int j = 0; j < 4; j++) {
                        interpolatedFinal[j] = (int)(Math.round((1 - y_inp - y_floor) * interpolatedTop[j] + (y_inp - y_floor) * interpolatedBottom[j]));


                        /* Check bereik */
                        if (interpolatedFinal[j] < 0 && j < 3) {
                            interpolatedFinal[j] = 0;
                        }
                        else if(interpolatedFinal[j] > 255 && j < 3) {
                            interpolatedFinal[j] = 255;
                        }
                        else if(interpolatedFinal[j] < 0 && j == 3) {
                            interpolatedFinal[j] = 0;
                        }
                        else if(interpolatedFinal[j] > 255 && j ==3) {
                            interpolatedFinal[j] = 255;
                        }
                    }

                    RGB = interpolatedFinal[3] << 24 | interpolatedFinal[0] << 16 | interpolatedFinal[1] << 8 | interpolatedFinal[2];

                    output[i] = RGB;

                } else {
                    /* Omzetten naar cartesische coordinaten */
                    x = i % width - xCenter;
                    y = yCenter - i / width;


                    /* Input coordinaten */
                    x_in = (int) (x * Math.cos(rotation) + y * Math.sin(rotation));
                    y_in = (int) (-x * Math.sin(rotation) + y * Math.cos(rotation));

                    /* Als ze buiten bereik vallen */
                    if (x_in < -halfWidth || x_in > halfWidth || y_in < -halfHeight || y_in > halfHeight) {
                        output[i] = Color.YELLOW;
                    }

                    /* Cartesische coordinaten -> 1D-array */
                    element = (int) ((0.5 * height - y_in) * width + 0.5 * width + x_in);

                    if (element < pixels && element > 0) {
                        output[i] = this.currentImage[element];
                    }
                }
            }
            canvas.drawBitmap(output, 0, width, left, top - 100, width, height, true, null);
            canvas.drawBitmap(this.currentImage, 0 , width, left, top + height, width, height, true, null);
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
