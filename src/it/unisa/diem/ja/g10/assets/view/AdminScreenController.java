
package it.unisa.diem.ja.g10.assets.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author grego
 */
public class AdminScreenController implements Initializable {

    @FXML
    private ListView<?> documentListView;
    @FXML
    private Button uploadDocumentButton;
    @FXML
    private TextArea stopwordsTextArea;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void loadDocument(ActionEvent event) {
    }
    
}
