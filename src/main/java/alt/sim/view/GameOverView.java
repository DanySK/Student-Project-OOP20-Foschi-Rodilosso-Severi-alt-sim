package alt.sim.view;

import alt.sim.Main;
import alt.sim.view.pages.Page;
import alt.sim.view.pages.PageLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class GameOverView {

    @FXML
    public void initialize() {

    }

    @FXML
    public void onHomeClick(final ActionEvent event) {
        ((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
        new PageLoader().loadPage(Main.getStage(), Page.HOME);
    }

    @FXML
    public void onQuitClick(final ActionEvent event) {
        ((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
        Main.getStage().close();
    }
}
