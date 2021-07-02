package alt.sim.view;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import alt.sim.controller.engine.GameEngineAreaTest;
import alt.sim.model.PlaneMovement;
import alt.sim.model.plane.Plane;
import javafx.animation.PathTransition;
import javafx.animation.Animation.Status;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class TransitionTest extends Application {
    private Pane paneRoot;
    private PlaneMovement planeMove;
    private List<Plane> planes;
    private Plane plane;
    private Plane plane2;
    private Plane plane3;
    private Plane plane4;
    private Canvas canvas;

    private GameEngineAreaTest engine;
    private PathTransition pathTransition;
    private Path path;
    private List<Point2D> planeCoordinates;
    private List<Line> linesPath;
    private GraphicsContext gc;

    // Area Collision
    private Rectangle rectangleWall;

    @Override
    public void start(final Stage stage) throws Exception {
        paneRoot = new Pane();
        canvas = new Canvas(MainPlaneView.getScreenWidth(), MainPlaneView.getScreenHeight());
        engine = new GameEngineAreaTest(this);

        pathTransition = new PathTransition();
        path = new Path();

        planeCoordinates = new ArrayList<Point2D>();
        planeMove = new PlaneMovement();
        linesPath = new ArrayList<>();

        planes = new ArrayList<>();
        plane = new Plane("images/map_components/airplane.png");
        plane2 = new Plane("images/map_components/airplane.png");
        plane3 = new Plane("images/map_components/airplane.png");
        plane4 = new Plane("images/map_components/airplane.png");

        plane.connetToController(this);
        plane2.connetToController(this);
        plane3.connetToController(this);
        plane4.connetToController(this);
        planes.add(plane);
        planes.add(plane2);
        planes.add(plane3);
        planes.add(plane4);

        rectangleWall = new Rectangle(800, 600, 40, 30);
        // Inizio funzionamento della View

        // Calculating the Proportion --> (Image:Screen)
        plane.getSpritePlane().getImageSpriteResized().resizeImageSprite(true);
        plane2.getSpritePlane().getImageSpriteResized().resizeImageSprite(true);
        plane3.getSpritePlane().getImageSpriteResized().resizeImageSprite(true);
        plane4.getSpritePlane().getImageSpriteResized().resizeImageSprite(true);
        
        // View Plane demonstrating:
        paneRoot.resize(MainPlaneView.getScreenWidth(), MainPlaneView.getScreenHeight());

        // Insert Plane test into view:
        plane2.getImagePlane().setX(500);
        plane2.getImagePlane().setY(500);
        plane3.getImagePlane().setX(800);
        plane3.getImagePlane().setY(100);
        plane4.getImagePlane().setX(800);
        plane4.getImagePlane().setY(800);

        // Section Canvas + WallCollision + ImageTest
        paneRoot.getChildren().addAll(canvas, rectangleWall, plane.getImagePlane(), plane2.getImagePlane());
        paneRoot.getChildren().addAll(plane3.getImagePlane(), plane4.getImagePlane());
        gc = canvas.getGraphicsContext2D();
        gc.save();

        // Avvio del GameLoop
        class ThreadEngine implements Runnable {

            @Override
            public void run() {
                engine.mainLoop();
            }
        }

        Thread t = new Thread(new ThreadEngine());
        t.start();

        EventHandler<MouseEvent> handlerMouseMoved = event -> gc.moveTo(event.getX(), event.getY());

        EventHandler<MouseEvent> handlerMouseDragged = event -> {
            if (planeCoordinates.size() < PlaneMovement.COORDINATES_LIMIT) {
                planeCoordinates.add(new Point2D(event.getX(), event.getY()));
                //gc.lineTo(event.getX(), event.getY());
                //gc.setStroke(Color.LIMEGREEN);
                //gc.stroke();
            }
        };

        EventHandler<MouseEvent> handlerMouseReleased = event -> {
            convertCoordinatesToLine();

            for (Plane planeSelected:planes) {

                if (planeSelected.getIsPlaneSelectedForBeenMoved()) {
                    paneRoot.getChildren().addAll(planeSelected.getPlaneLinesPath());
                }
            }

            // 2)
            // Blocca il ciclo in GameEngineImpl per resettare le nuove coordinate.
            if (pathTransition.getStatus() == Status.RUNNING) {
                pathTransition.stop();
                engine.stopPathTransition();
                engine.setReadyToStart(false);
            }

            planeMove.setPlaneCoordinatesList(clearListCoordinates(planeCoordinates));
            copyCoordinatesInPath(planeCoordinates);
            selectedPathNode();
            clearPlaneSelectedForBeenMoved();
            //connectPlaneToPathTransition();

            // Quando viene rilasciato il Mouse le coordinate salvate vengono liberate
            planeCoordinates.clear();
        };

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, handlerMouseReleased);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, handlerMouseMoved);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, handlerMouseDragged);

        Scene scene = new Scene(paneRoot, MainPlaneView.getScreenWidth(), MainPlaneView.getScreenHeight());
        stage.setScene(scene);
        stage.show();
    }

    private void convertCoordinatesToLine() {
        for (int i = 0; i < planeCoordinates.size(); i++) {
            try {
                if (planeCoordinates.get(i + 1) != null) {
                    linesPath.add(new Line(planeCoordinates.get(i).getX(), planeCoordinates.get(i).getY(), planeCoordinates.get(i + 1).getX(), planeCoordinates.get(i + 1).getY()));
                }
            } catch (Exception e) {};
        }

        for (Plane planeSelected:planes) {

            if (planeSelected.getIsPlaneSelectedForBeenMoved()) {
                clearingLinesPathToPaneRoot(planeSelected.getPlaneLinesPath());
                planeSelected.setPlaneLinesPath(linesPath);
            }
        }

        paneRoot.getChildren().addAll(linesPath);
        linesPath.clear();
    }

    private void selectedPathNode() {
        for (Plane planeSelected:planes) {

            if (planeSelected.getIsPlaneSelectedForBeenMoved()) {
                engine.setCoordinates(planeCoordinates);
                engine.setPathTransition(pathTransition);
                engine.setReadyToStart(true);
                engine.setPlane(planeSelected);
                engine.connectPlaneToPathTransition(plane);
                planeSelected.setIsPlaneSelectedForBeenMoved(false);
            } 
            planeSelected.setIsPlaneSelectedForBeenMoved(false);
        }
    }

    public void clearPlaneSelectedForBeenMoved() {
        for (Plane planeSelected:planes) {
            planeSelected.setIsPlaneSelectedForBeenMoved(false);
        }
    }

    public void clearingLinesPathToPaneRoot(final List<Line> linesPathToRemove) {
        paneRoot.getChildren().remove(linesPathToRemove);
    }

    /*
     * private void connectPlaneToPathTransition(final Plane plane) { int pathLenght
     * = 0;
     * 
     * PathTransition pathTransitionPlane = new PathTransition();
     * pathTransitionPlane.setPath(path);
     * pathTransitionPlane.setNode(plane.getImagePlane());
     * pathTransitionPlane.setOrientation(OrientationType.ORTHOGONAL_TO_TANGENT);
     * 
     * pathTransitionPlane.play();
     * 
     * //engine.setCoordinates(planeCoordinates);
     * //engine.setPathTransition(pathTransition); //engine.setReadyToStart(false);
     * }
     */

    private void copyCoordinatesInPath(final List<Point2D> planeCoordinates) {
        // Ripuliamo le coordinate presenti dal path prima
        path = new Path();

        for (int k = 0; k < planeCoordinates.size(); k++) {

            if (k == 0) {
                path.getElements().add(new MoveTo(planeCoordinates.get(k).getX(), planeCoordinates.get(k).getY()));
            } else {
                path.getElements().add(new LineTo(planeCoordinates.get(k).getX(), planeCoordinates.get(k).getY()));
            }
        }
    }

    private List<Point2D> clearListCoordinates(final List<Point2D> planeCoordinates) {
        Set<Point2D> s = new LinkedHashSet<>(planeCoordinates);
        List<Point2D> copyListCleaned = new ArrayList<>(s);

        return copyListCleaned;
    }

    public static void main(final String[] args) {
        launch(args);
    }

    public Rectangle getRectangleWall() {
        return this.rectangleWall;
    }

    public ImageView getPlaneSprite() {
        return this.plane.getImagePlane();
    }
}
