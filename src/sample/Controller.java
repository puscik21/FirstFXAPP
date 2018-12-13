package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    @FXML
    private Button start_btn;
    @FXML
    private ImageView currentFrame;
    @FXML
    private CheckBox haarClassifier;
    @FXML
    private CheckBox lbpClassifier;

    // object used to capturing the video
    private VideoCapture capture;
    // timer for acquiring frames
    private ScheduledExecutorService timer;
    private boolean cameraActive = false;
    // here can be various values, in my example '2' worked
    private static int cameraId = 2;

    // face cascade classifier
    private CascadeClassifier faceCascade;
    private int absoluteFaceSize;


    // initializing method
    void init(){
        this.capture = new VideoCapture();
        this.faceCascade = new CascadeClassifier();
        this.absoluteFaceSize = 0;

        // probably not needed?
//        currentFrame.setFitWidth(600);
//        currentFrame.setPreserveRatio(true);
    }

    @FXML
    protected void startCamera(ActionEvent event) {
        // start recording when camera is off
        if (!cameraActive) {
            // start video capture
            this.capture.open(cameraId);

            // one cannot change checkboxes when capture is in progress
            this.haarClassifier.setDisable(true);
            this.lbpClassifier.setDisable(true);

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

                // update text of the button
                this.start_btn.setText("Stop Camera");
            }
            else
                System.err.println("Impossible to open the camera connection");
        }
        else{
            this.cameraActive = false;
            this.start_btn.setText("Start Camera");
            // enable checkboxes of classifiers
            this.lbpClassifier.setDisable(false);
            this.haarClassifier.setDisable(false);

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
                    this.detectAndDisplay(frame);
                }
            }
            catch (Exception e){
                System.err.println("Exception during image capturing " + e);
            }
        }
        return frame;
    }

    // method that detect face in the frame
    private void detectAndDisplay(Mat frame){
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // arguments go like this: actual, destination, transformation
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // this extends pixels values, so in a result give a better contrast of the image
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height)
        if (this.absoluteFaceSize == 0){
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0)
                this.absoluteFaceSize = Math.round(height * 0.2f);
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        // drawing each face rectangle
        Rect[] facesArray = faces.toArray();
        for (Rect rect : facesArray){
            // tl - top left point, br - bottom right point of the rectangle
            Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);
        }
    }

    // Action triggered by the haar classifier checkbox. Loads set used to frontal face detection
    @FXML
    void haarSelected(Event e){
        if (this.haarClassifier.isSelected())
            this.haarClassifier.setSelected(false);

        this.checkboxSelection("resources/haarcascades/haarcascade_frontalface_alt.xml");
    }

    // Action triggered by the LBP classifier checkbox
    @FXML
    void lbpSelected(Event e){
        if (this.lbpClassifier.isSelected())
            this.lbpClassifier.setSelected(false);

        this.checkboxSelection("resources/lbpcascades/lbpcascade_frontalface.xml");
    }

    // to load classifier trained set
    private void checkboxSelection(String classifierPath){
        this.faceCascade.load(classifierPath);

        // now video capture can start
        this.start_btn.setDisable(false);
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
