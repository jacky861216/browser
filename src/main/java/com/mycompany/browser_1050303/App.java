package com.mycompany.browser_1050303;


import java.io.File;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    private final String HOME_PAGE = "https://www.google.com";

    private WebView webView;
    private WebEngine webEngine;
    private WebHistory webHistory;
    private TextField addressBar;
    private Button backButton;
    private Button forwardButton;
    private Button reloadButton;
    private Button homeButton;
    private Button addTabButton;
    private Button zoomInButton;
    private Button zoomOutButton;
    private ListView<String> historyList;
    private MenuBar menuBar;
    private Menu bookmarkMenu;
    private MenuItem addBookmarkMenuItem;
    private MenuItem openBookmarkMenuItem;
    private TabPane tabPane;
    private List<Bookmark> bookmarks = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        // Create the WebView and WebEngine
        webView = new WebView();
        webEngine = webView.getEngine();

        // Create the address bar
        addressBar = new TextField(HOME_PAGE);
        addressBar.setOnAction(e -> {
            String searchText = addressBar.getText();
            WebEngine currentEngine = getCurrentWebEngine();
            String searchUrl = "https://www.google.com/search?q=" + searchText;
            currentEngine.load(searchUrl);
        });

        //Create 上一頁 button
        backButton = new Button();
        backButton.setOnAction(e -> {
            WebHistory webHistory = getCurrentHistory();
            int currentIndex = webHistory.getCurrentIndex();
            if (currentIndex > 0) {
                webHistory.go(-1);
            }
        });
        setButtonImage(backButton, "left.png", 20, 20);
        setButtonTooltip(backButton, "上一頁");

        //Create 下一頁 button
        forwardButton = new Button();
        forwardButton.setOnAction(e -> {
            WebHistory webHistory = getCurrentHistory();
            int currentIndex = webHistory.getCurrentIndex();
            if (currentIndex < webHistory.getEntries().size() - 1) {
                webHistory.go(1);
            }
        });
        setButtonImage(forwardButton, "right.png", 20, 20);
        setButtonTooltip(forwardButton, "下一頁");

        //Create 首頁 button
        homeButton = new Button();
        homeButton.setOnAction(e -> {
            WebEngine currentEngine = getCurrentWebEngine();
            currentEngine.load(HOME_PAGE);
        });
        setButtonImage(homeButton, "home.png", 20, 20);
        setButtonTooltip(homeButton, "首頁");

        //Create 重新載入 button
        reloadButton = new Button();
        reloadButton.setOnAction(e -> {
            WebEngine currentEngine = getCurrentWebEngine();
            currentEngine.reload();
        });
        setButtonImage(reloadButton, "refresh.png", 20, 20);
        setButtonTooltip(reloadButton, "重新載入");

        //Create 新增分頁 button
        addTabButton = new Button();
        addTabButton.setOnAction(e -> addNewTab());
        setButtonImage(addTabButton, "plus.png", 20, 20);
        setButtonTooltip(addTabButton, "新增分頁");

        //Create 書籤 menu
        menuBar = new MenuBar();
        bookmarkMenu = new Menu("書籤");
        addBookmarkMenuItem = new MenuItem("新增書籤");
        addBookmarkMenuItem.setOnAction(e -> addBookmark());
        openBookmarkMenuItem = new MenuItem("開啟書籤");
        openBookmarkMenuItem.setOnAction(e -> openBookMark());
        bookmarkMenu.getItems().addAll(addBookmarkMenuItem, openBookmarkMenuItem);
        menuBar.getMenus().add(bookmarkMenu);

        //zoom in
        zoomInButton = new Button();
        zoomInButton.setOnAction(e -> zoomIn());
        setButtonImage(zoomInButton, "zoom-in.png", 20, 20);
        setButtonTooltip(zoomInButton, "放大頁面");

        //zoom out
        zoomOutButton = new Button();
        zoomOutButton.setOnAction(e -> zoomOut());
        setButtonImage(zoomOutButton, "zoom-out.png", 20, 20);
        setButtonTooltip(zoomOutButton, "縮小頁面");

        // history
        historyList = new ListView<>();
        historyList.setPrefWidth(200);
        historyList.setOnMouseClicked(e -> {
            String url = historyList.getSelectionModel().getSelectedItem();
            WebEngine currentEngine = getCurrentWebEngine();
            currentEngine.load(url);
        });

        // Create the TabPane and add a default tab
        tabPane = new TabPane();
        addNewTab();

        //判斷在哪個分頁
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                WebHistory currentHistory = getCurrentHistory();
                // 判斷是否啟用上一頁按鈕
                //上一頁要在currentIndex > 0的時候才啟動
                backButton.setDisable(currentHistory.getCurrentIndex() == 0);
                // 判斷是否啟用下一頁按鈕
                //下一頁要在currentIndex < webHistory.getEntries().size() - 1 的時候才啟動，getCurrentIndex()從0開始，size()是從1開始
                forwardButton.setDisable(currentHistory.getCurrentIndex() >= currentHistory.getEntries().size() - 1); 
            }
        });

        //讓addressBar隨著網頁改變而變更網址
        WebEngine currentEngine = getCurrentWebEngine();
        // 監聽locationProperty()屬性
        currentEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            // 更新TextField的內容
            addressBar.setText(newValue);
        });

        // Create the layout
        BorderPane root = new BorderPane();
        VBox vbox = new VBox();
        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(backButton, forwardButton, homeButton, addTabButton, addressBar, reloadButton, menuBar, zoomInButton, zoomOutButton);
        vbox.getChildren().add(toolBar);
        root.setTop(vbox);
        root.setLeft(historyList);
        root.setCenter(tabPane);

        // Create the scene and show the stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("瀏覽器");
        primaryStage.show();
    }

    //functions
    //放button的icon
    private void setButtonImage(Button button, String imagePath, double width, double height) {
        Image image = new Image(new File(imagePath).toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        button.setGraphic(imageView);
    }

    //設定滑鼠移上去的字
    private void setButtonTooltip(Button button, String tooltipText) {
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.ZERO);  //設定多久會顯示字
        button.setTooltip(tooltip);
    }

    //網頁
    //tabPane.getSelectionModel()：獲取 TabPane 的選取模型（SelectionModel），選取模型負責管理選取的分頁。
    //getSelectedItem()：從選取模型中獲取當前選取的分頁項目。
    //getContent()：獲取選取的分頁項目的內容，取得的類型是 Node。
    //(WebView)：這是一個類型轉換（Type Casting）的語法，將分頁的內容物件轉換為 WebView 類型。
    private WebView getCurrentWebView() {
        WebView selectedWebView = (WebView) tabPane.getSelectionModel().getSelectedItem().getContent();
        return selectedWebView;
    }
    
    //getEngine() ：獲取 WebView 的引擎物件，也就是用來控制 WebView 內容的工具。
    private WebEngine getCurrentWebEngine() {
        WebView selectedWebView = (WebView) tabPane.getSelectionModel().getSelectedItem().getContent();
        WebEngine currentWebEngine = selectedWebView.getEngine();
        return currentWebEngine;
    }

    //上一頁、下一頁用的歷史紀錄
    private WebHistory getCurrentHistory() {
        WebView selectedWebView = (WebView) tabPane.getSelectionModel().getSelectedItem().getContent();
        WebEngine currentWebEngine = selectedWebView.getEngine();
        WebHistory webHistory = currentWebEngine.getHistory();
        return webHistory;
    }
    
    //更新上一頁跟下一頁的狀態
    private void updateButtonState() {
        WebHistory webHistory = getCurrentHistory();
        int currentIndex = webHistory.getCurrentIndex();
        backButton.setDisable(currentIndex == 0); //上一頁要在currentIndex > 0的時候才啟動
        forwardButton.setDisable(currentIndex >= webHistory.getEntries().size() - 1);//下一頁要在currentIndex < webHistory.getEntries().size() - 1 的時候才啟動
    }

    //新增分頁
    private void addNewTab() {
        Tab newTab = new Tab("新分頁");
        WebView newWebView = new WebView();
        WebEngine newWebEngine = newWebView.getEngine();
        //監聽newWebEngine的載入狀態
        newWebEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {                       //若載入成功
                addressBar.setText(newWebEngine.getLocation());
                historyList.getItems().add(newWebEngine.getLocation());
                String pageTitle = newWebEngine.getTitle();
                if (pageTitle != null && !pageTitle.isEmpty()) {
                    newTab.setText(pageTitle);
                }
                updateButtonState();
            }
        });
        //當網頁變化時，更新addressBar的網址
        newWebEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            addressBar.setText(newValue);
        });
        newWebEngine.load(HOME_PAGE);
        newTab.setContent(newWebView);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        webHistory = newWebEngine.getHistory();
        //當歷史紀錄變化時，更新按鈕的狀態。
        newWebEngine.getHistory().getEntries().addListener((ListChangeListener.Change<? extends WebHistory.Entry> c) -> {
            updateButtonState();
        });
        updateButtonState();
    }
    
    
    //新增書籤
    private void addBookmark() {
        BorderPane root = new BorderPane();
        WebEngine currentEngine = getCurrentWebEngine();
        String url = currentEngine.getLocation();
        String title = currentEngine.getTitle();
        TextField text1 = new TextField();
        TextField text2 = new TextField();
        Text titletext = new Text("網站名稱: ");
        Text urltext = new Text("網址: ");
        Button checkButton = new Button("確定");
        checkButton.setOnAction(event -> {
            String url_new = text2.getText();
            String title_new = text1.getText();
            boolean urlExists = isUrlExist(url_new);
            boolean titleExists = isTitleExist(title_new);
            if (urlExists && titleExists) {
                showAlertBook("該網站名稱和網址已存在於書籤中!");
            } else if (urlExists) {
                showAlertBook("該網址已存在於書籤中!");
            } else if (titleExists) {
                showAlertBook("該網站名稱已存在於書籤中!");
            } else {
                saveBookmark(url_new, title_new);
                Stage currentStage = (Stage) checkButton.getScene().getWindow();
                currentStage.close();
            }
        });
        Button cancelButton = new Button("取消");
        cancelButton.setOnAction(event -> {
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });
        text1.setText(title);
        text2.setText(url);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.add(titletext, 0, 0);
        gridPane.add(text1, 1, 0);
        gridPane.add(urltext, 0, 1);
        gridPane.add(text2, 1, 1);
        gridPane.add(checkButton, 0, 2);
        gridPane.add(cancelButton, 1, 2);
        root.setCenter(gridPane);
        Scene scene = new Scene(root, 250, 150);
        Stage stage1 = new Stage();
        stage1.setScene(scene);
        stage1.setTitle("加入書籤");
        stage1.show();

    }
    
    //打開書籤
    private void openBookMark() {
        List<Bookmark> bookmarks = getBookmarks();
        if (!bookmarks.isEmpty()) {
            BorderPane root = new BorderPane();
            ListView<Bookmark> bookmarkListView = new ListView<>();
            bookmarkListView.getItems().addAll(bookmarks);
            bookmarkListView.setCellFactory(param -> new ListCell<Bookmark>() {
                @Override
                protected void updateItem(Bookmark item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getTitle() == null) {
                        setText(null);
                    } else {
                        setText(item.getTitle());
                    }
                }
            });
            //監聽bookmarkListView的點擊，若有點擊就導向該網站
            bookmarkListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    WebEngine currentEngine = getCurrentWebEngine();
                    currentEngine.load(newValue.getUrl());
                    Stage currentStage = (Stage) bookmarkListView.getScene().getWindow();
                    currentStage.close();
                }
            });
            root.setCenter(bookmarkListView);
            Scene scene = new Scene(root, 400, 400);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("書籤");
            stage.show();
        } else {
            showAlert("您還沒有加入任何書籤!!");
        }
    }

    //放大
    private void zoomIn() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        WebView currentWebView = (WebView) selectedTab.getContent();
        if (currentWebView.getZoom() < 1.5) {
            if (selectedTab != null) {
                currentWebView.setZoom(currentWebView.getZoom() + 0.1);
            }
        } else {
            showAlert("已經放到最大!");
        }
    }

    //縮小
    private void zoomOut() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        WebView currentWebView = (WebView) selectedTab.getContent();
        if (currentWebView.getZoom() > 0.5) {
            if (selectedTab != null) {
                currentWebView.setZoom(currentWebView.getZoom() - 0.1);
            }
        } else {
            showAlert("已經縮到最小!");
        }
    }

    private void showAlert(String message) {
        WebView currentWebView = (WebView) tabPane.getSelectionModel().getSelectedItem().getContent();
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("訊息");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(currentWebView.getScene().getWindow()); //alert的擁有者，會顯示在視窗中央。若沒有這行會顯示在螢幕中央。
        alert.showAndWait();
    }

    private void showAlertBook(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("警告");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    
    
    
    
    

    private void saveBookmark(String url, String title) {
        BookmarksManager manager = new BookmarksManager();
        manager.addBookmark(title, url);
    }

    private List<Bookmark> getBookmarks() {
        BookmarksManager manager = new BookmarksManager();
        return manager.getAllBookmarks();
    }

    private static class BookmarksManager {

        private static List<Bookmark> bookmarks = new ArrayList<>();

        public static void addBookmark(String title, String url) {
            Bookmark bookmark = new Bookmark(title, url);
            bookmarks.add(bookmark);
        }

        public List<Bookmark> getAllBookmarks() {
            return bookmarks;
        }
    }

    private static class Bookmark {

        private String title;
        private String url;

        public Bookmark(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private boolean isUrlExist(String url) {
        List<Bookmark> bookmarks = getBookmarks();
        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTitleExist(String title) {
        List<Bookmark> bookmarks = getBookmarks();
        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
