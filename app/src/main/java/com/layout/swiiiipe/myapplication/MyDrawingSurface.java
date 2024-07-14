package com.layout.swiiiipe.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;


public class MyDrawingSurface extends SurfaceView {

    private static final String TAG = MyDrawingSurface.class.getName();

    private Matrix mMatrix;

    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Path> calPath = new ArrayList<>();

    private Matrix inverseMatrix = new Matrix();



    int mx , my;

    Bitmap paintBmp;


    Path mainPath = new Path();


    private int MODE;
    private static final int INVALID_POINTER_ID = -1;
    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1f;

    private float[] mMatrixValues = new float[9];

    Paint pathPaint;

    Paint transPAint;
    Canvas paintCnvs;

    float dx;
    float dy;

    SurfaceHolder holder;
    Bitmap bitmap;

    MyDrawingThread myDrawingThread;

    ProgressDialog progressDialog;


    public MyDrawingSurface(Context context , Bitmap bitmap) {
        this(context , null , bitmap);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.create();




//        holder = getHolder();
//        holder.addCallback(this);
    }

    public MyDrawingSurface(Context context, AttributeSet attrs , Bitmap bitmap) {
        super(context, attrs);

        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setColor(getResources().getColor(R.color.white));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeWidth(30);

        transPAint = new Paint();
        transPAint.setAntiAlias(true);
        transPAint.setColor(getResources().getColor(android.R.color.transparent));
        transPAint.setStyle(Paint.Style.STROKE);
        transPAint.setStrokeJoin(Paint.Join.ROUND);
        transPAint.setStrokeCap(Paint.Cap.ROUND);
        transPAint.setStrokeWidth(30);

        holder = getHolder();
        this.bitmap = bitmap;
        mScaleDetector = new ScaleGestureDetector(getContext() , new ScaleListener());

        myDrawingThread = new MyDrawingThread("DrawingThread");
        myDrawingThread.start();

        myDrawingThread.holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {

                myDrawingThread.handler.post(new Runnable() {
                    @Override
                    public void run() {


                        myDrawingThread.mCanvas.drawBitmap(bitmap , myDrawingThread.matrix , null);
                        myDrawingThread.matrix.invert(inverseMatrix);

                        Canvas canvas = holder.lockCanvas();
                        //Bitmap bitmap = BitmapFactory.decodeResource(getResources() , R.drawable.indiagate);

                        canvas.drawBitmap(bitmap,myDrawingThread.matrix , null);

                        MyDrawingSurface.this.holder.unlockCanvasAndPost(canvas);

                    }
                });
            }




            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

               // Log.d(TAG, "surfaceChanged: " + width + " " + height);

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {

                if (MODE == 1) {

                    float[] mappedPoints = mapPoints(event.getX(), event.getY());
                    float mappedX = mappedPoints[0];
                    float mappedY = mappedPoints[1];

//

                    mainPath.moveTo(mappedX , mappedY);


                } else {

                    Log.d(CropView.class.getName(), "onTouchEvent: ");
                    final float x = event.getX();
                    final float y = event.getY();
                    mLastTouchX = x;
                    mLastTouchY = y;

                    mx = (int) event.getX();
                    my = (int) event.getY();

                    mActivePointerId = event.getPointerId(0);


                }


                break;
            }
            case MotionEvent.ACTION_MOVE: {

                if (MODE == 1) {

                    float[] mappedPoints = mapPoints(event.getX(), event.getY());
                    float mappedX = mappedPoints[0];
                    float mappedY = mappedPoints[1];



                    mainPath.lineTo(mappedX , mappedY);


                    //mainPath = new Path();
                    //paintCnvs.drawLine(mx , my , (int)(event.getX()) , (int)(event.getY()) , pathPaint);
//                    mainPath.lineTo(mx , my);
                    paths.add(mainPath);

                    myDrawingThread.handler.post(new Runnable() {
                        @Override
                        public void run() {

                            //myDrawingThread.matrix.reset();
                            //myDrawingThread.matrix.postTranslate(dx , dy);

                            myDrawingThread.mCanvas.drawPath(mainPath , pathPaint);
                            Canvas c = myDrawingThread.holder.lockCanvas();
                            c.drawBitmap(myDrawingThread.bitmap , myDrawingThread.matrix ,  null);
                            myDrawingThread.holder.unlockCanvasAndPost(c);




//                            for (Path path : paths) {

//                                Canvas c = myDrawingThread.holder.lockCanvas();
//                                //c.drawColor(Color.BLACK);
//                                //c.drawPath(mainPath , pathPaint);
//                                c.drawBitmap(MyDrawingSurface.this.bitmap , myDrawingThread.matrix , null);
//                                c.drawPath(mainPath , pathPaint);
//                                myDrawingThread.holder.unlockCanvasAndPost(c);


                          //  }


                        }
                    });



                } else if (MODE == 2) {


                    Log.d(CropView.class.getName(), "onTouchMoe: ");

                    final int pointerIndex = event.findPointerIndex(mActivePointerId);
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);
                    if (!mScaleDetector.isInProgress()) {
                        Log.d(CropView.class.getName(), "onTouchMoew: ");
                        dx = x - mLastTouchX;
                        dy = y - mLastTouchY;
                        myDrawingThread.matrix.getValues(mMatrixValues);
                        float[] translated = {dx, dy};
                        myDrawingThread.matrix.mapVectors(translated);
                        float translatedX = translated[0];
                        float translatedY = translated[1];
                        myDrawingThread.matrix.postTranslate(translatedX, translatedY);
                        myDrawingThread.handler.post(new Runnable() {
                            @Override
                            public void run() {

                                Canvas canvas = myDrawingThread.holder.lockCanvas();
                                canvas.drawColor(Color.BLACK);
                                canvas.drawBitmap(myDrawingThread.bitmap , myDrawingThread.matrix , null);
                                //myDrawingThread.mCanvas.drawBitmap(MyDrawingSurface.this.bitmap , myDrawingThread.matrix , null);



                                Bitmap bitmap1 = Bitmap.createBitmap(myDrawingThread.bitmap.getWidth(),myDrawingThread.bitmap.getHeight() , Bitmap.Config.ARGB_8888);
                                //myDrawingThread.mCanvas = new Canvas(bitmap1);
                                myDrawingThread.mCanvas.drawBitmap(bitmap1 ,myDrawingThread.matrix  , null);
                                myDrawingThread.matrix.invert(inverseMatrix);

                                myDrawingThread.holder.unlockCanvasAndPost(canvas);

                            }
                        });
                    }
                    mLastTouchX = x;
                    mLastTouchY = y;
                }
                break;
            }
            case MotionEvent.ACTION_UP:

                if (MODE == 3) {

                    progressDialog.setCancelable(false);
                    progressDialog.show();


                        myDrawingThread.handler.post(new Runnable() {
                            @Override
                            public void run() {

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {

                                        progressDialog.show();

                                    }
                                });


                                int px = (int) event.getX();
                                int py = (int) event.getY();

                                if (px>=0 && px<=myDrawingThread.bitmap.getWidth() && py>0 && py<=myDrawingThread.bitmap.getHeight()) {


                                    int targetColor = myDrawingThread.bitmap.getPixel(px, py);

                                    // Get the grayscale value of the target color
                                    int targetGray = (int) (Color.red(targetColor) * 0.3 + Color.green(targetColor) * 0.59 + Color.blue(targetColor) * 0.11);

//                                    int red = Color.red(pxColor);
//                                    int green = Color.green(pxColor);
//                                    int blue = Color.blue(pxColor);

                                    //int color = Color.rgb(red, green, blue);

                                    for (int x = 0; x < myDrawingThread.bitmap.getWidth(); x++) {

                                        for (int y = 0; y < myDrawingThread.bitmap.getHeight(); y++) {

                                            int currentColor = myDrawingThread.bitmap.getPixel(x, y);
//
//                                            int cred = Color.red(convColor);
//                                            int cgreen = Color.green(convColor);
//                                            int cblue = Color.blue(convColor);
//
//                                            int cColor = Color.rgb(cred, cgreen, cblue);

                                            int currentGray = (int) (Color.red(currentColor) * 0.3 + Color.green(currentColor) * 0.59 + Color.blue(currentColor) * 0.11);

//                                            if (targetColor == currentColor) {

                                            if (currentGray > 90) {
                                                // Erase or set to white (Color.WHITE) or any erase color
                                                myDrawingThread.bitmap.setPixel(x, y, Color.WHITE);
                                                // }


                                            }

                                            // Compare grayscale values


//                                            if (pxColor == convColor) {
//
//                                                myDrawingThread.bitmap.setPixel(x, y, Color.WHITE);
//                                            }
                                        }

                                    }
                                }


                                Canvas c = myDrawingThread.holder.lockCanvas();
                                c.drawBitmap(myDrawingThread.bitmap , myDrawingThread.matrix , null);
                                myDrawingThread.holder.unlockCanvasAndPost(c);

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {

                                        progressDialog.dismiss();

                                    }
                                });

                            }
                        });



                } else {

                    mx = (int) event.getX();
                    my = (int) event.getY();
                }

                break;



            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    public void setMode(int i) {

        this.MODE = i;

    }

    private float[] mapPoints(float x, float y) {
        float[] pts = new float[] { x, y };
        inverseMatrix.mapPoints(pts);
        return pts;
    }

    private class MyDrawingThread extends HandlerThread {

        ImageView view = new ImageView(getContext());




        SurfaceHolder holder = MyDrawingSurface.this.getHolder();

        Looper looper = Looper.myLooper();
        Handler handler = new Handler(looper);
        ProgressDialog progressDialog;

        Bitmap bitmap = Bitmap.createBitmap(MyDrawingSurface.this.bitmap.getWidth()
                , MyDrawingSurface.this.bitmap.getHeight()
         , Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(bitmap);


        Matrix matrix = new Matrix();

        public MyDrawingThread(String name) {
            super(name);
            mCanvas.drawBitmap(MyDrawingSurface.this.bitmap , matrix , null);





        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        float mScaleFactor = 1.0f;

        int focusX , focusY;



        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            myDrawingThread.matrix.setScale(mScaleFactor, mScaleFactor , mx , my);
            myDrawingThread.handler.post(new Runnable() {
                @Override
                public void run() {



                    Canvas canvas = myDrawingThread.holder.lockCanvas();
                    canvas.drawColor(Color.BLACK);
                    canvas.drawBitmap(myDrawingThread.bitmap , myDrawingThread.matrix , null);
                    myDrawingThread.matrix.invert(inverseMatrix);
                    //myDrawingThread.matrix.reset();
                    //myDrawingThread.mCanvas.drawBitmap(MyDrawingSurface.this.bitmap , myDrawingThread.matrix , null);

                    myDrawingThread.holder.unlockCanvasAndPost(canvas);

                }
            });
            return true;
        }
    }


}
