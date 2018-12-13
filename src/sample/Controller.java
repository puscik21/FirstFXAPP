package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;

    private VideoCapture capture = new VideoCapture();
    //timer for acquiring frames
    private ScheduledExecutorService timer;

    private boolean cameraActive = false;
    // here can be various values, in my example '2' worked
    private static int cameraId = 2;

    @FXML
    protected void startCamera(ActionEvent event) {
        // start recording when camera is off
        if (!cameraActive) {
            this.capture.open(cameraId);

            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // it is a thread that grabs a new frame for each 33 ms (30 fps)
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        Mat frame = grabFrame();
                        MatOfByte buffer = new MatOfByte();
                        Imgcodecs.imencode(".png", frame, buffer);

                        Image imageToShow = new Image(new ByteArrayInputStream(buffer.toArray()));
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                currentFrame.setImage(imageToShow);
                            }
                        });


                        // kinda another way to do that
//                        Image imageToShow = Utils.mat2Image(frame);
//                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            }
            else
                System.err.println("Impossible to open the camera connection");
        }
        else{
            this.cameraActive = false;
            this.button.setText("Start Camera");

            this.stopAcquisition();
        }
    }

    private Mat grabFrame(){
        Mat frame = new Mat();

        if (this.capture.isOpened()){
            try{
                // read the frame
                this.capture.read(frame);

                // if the frame is not empty make it grey!
                if (!frame.empty()){
                    // arguments go like this: actual, destination, transformation
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                }
            }
            catch (Exception e){
                System.err.println("Exception during image capturing " + e);
            }
        }
        return frame;
    }

    private void stopAcquisition()
    {
        if (this.timer!=null && !this.timer.isShutdown())
        {
            try
            {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened())
        {
            // release the camera
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image)
    {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed()
    {
        this.stopAcquisition();
    }

}
