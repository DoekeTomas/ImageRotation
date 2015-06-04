/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 */

package nl.uva.multimedia.image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Arrays;

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

            /* Center van de canvas */
            int centerCanvasX = this.getWidth() / 2;
            int centerCanvasY = this.getHeight() / 2;

            /* Center van de afbeelding */
            int centerImageX = this.imageWidth / 2;
            int centerImageY = this.imageHeight / 2;

            int pixelsNr = this.imageWidth * this.imageHeight;
            int[][] pixels = new int[pixelsNr][3];

            /* Sla info per pixel op: [x, y, color] */
            for (int i = 0; i < pixelsNr; i++) {
                pixels[i][0] = ((i % imageWidth) - centerImageX) * -1;
                pixels[i][1] = (i / imageHeight) - centerImageY;
                pixels[i][2] = currentImage[i];
            }

            double radians = Math.PI / 4;

            int tmpX;
            /* Bereken de geroteerde x en y per pixel */
            for (int i = 0; i < pixelsNr; i++) {
                tmpX = pixels[i][0];
                pixels[i][0] = (int)((pixels[i][0] * Math.cos(radians)) + (pixels[i][1] * Math.sin(radians)));
                pixels[i][1] = (int)((-tmpX * Math.sin(radians)) + (pixels[i][1] * Math.cos(radians)));
            }

            int[] cornersX = {pixels[0][0], pixels[imageWidth - 1][0], pixels[pixelsNr - imageWidth][0], pixels[pixelsNr - 1][0]};
            int[] cornersY = {pixels[0][1], pixels[imageWidth - 1][1], pixels[pixelsNr - imageWidth][1], pixels[pixelsNr - 1][1]};

            int minX, maxX, minY, maxY;
            minX = maxX = minY = maxY = 0;

            for (int i = 0; i < 4; i++) {
                if (cornersX[i] < minX) minX = cornersX[i];
                if (cornersX[i] > maxX) maxX = cornersX[i];
                if (cornersY[i] < minY) minY = cornersY[i];
                if (cornersY[i] > maxY) maxY = cornersY[i];
            }

            int rotatedImageWidth = maxX - minX;
            int rotatedImageHeight = maxY - minY;
            int rotatedPixelsNr = rotatedImageWidth * (rotatedImageHeight + 10);

            Paint paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(Color.RED);

            //canvas.drawRect(centerCanvasX - rotatedImageWidth/2, centerCanvasY - rotatedImageHeight/2,
                    //centerCanvasX + rotatedImageWidth/2, centerCanvasY + rotatedImageHeight/2 ,paint);

            int[] currentImageRotated = new int[rotatedPixelsNr];

            int rgbaX, rgbaY;
            for (int i = 0; i < pixelsNr; i++) {
                rgbaX = (pixels[i][0] - minX);
                rgbaY = (pixels[i][1] - minY);

                currentImageRotated[rgbaY * rotatedImageWidth + rgbaX] = pixels[i][2];
            }

            /* Center afbeelding */
            int left = centerCanvasX - rotatedImageWidth / 2;
            int top = centerCanvasY - rotatedImageHeight / 2;

            canvas.drawBitmap(currentImageRotated, 0, rotatedImageWidth, left, top, rotatedImageWidth,
                    rotatedImageHeight, true, null);
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
