package wrestling.model.utility;

import java.io.File;
import java.util.Random;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public final class UtilityFunctions {

    //returns a random int between the two passed ints
    public static int randRange(int low, int high) {
        Random r = new Random();
        return r.nextInt(high - low) + low;
    }

    //shows an image if it exists, handles hide/show of image frame
    public static void showImage(File imageFile, StackPane imageFrame, ImageView imageView) {
        
        if (imageFile.exists() && !imageFile.isDirectory()) {
            //show the border if it is not visible
            if (!imageFrame.visibleProperty().get()) {
                imageFrame.setVisible(true);
            }
            Image image = new Image("File:" + imageFile);
            imageView.setImage(image);
        } else //hide the border if it is visible
         if (imageFrame.visibleProperty().get()) {
                imageFrame.setVisible(false);
            }
    }
}
