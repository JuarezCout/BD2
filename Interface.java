import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Interface extends Application {
    public static void render(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        int numeroTabelas = getNumeroTabelas();

        GridPane gridPane = new GridPane();

        for(int i = 0; i < numeroTabelas; i++ ){
            Text tableLabel = new Text("Tabela " + i);

            ObservableList<String> colunas = FXCollections.observableArrayList(getNomeColunas(i));
            ListView<String> listaColunas = new ListView<String>(colunas);
            listaColunas.setPrefHeight(colunas.size() * 24 + 2);
            listaColunas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            if(i == 0) {
                gridPane.add(tableLabel, i, i);
                gridPane.add(listaColunas, i, i + 1);
            } else {
                gridPane.add(tableLabel, i, i - 1);
                gridPane.add(listaColunas, i, i);
            }


        }


        Button executar = new Button("Executar Join");
        executar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleExecutar();
            }
        });
        gridPane.add(executar, 0,3);

        Text resultadosLabel = new Text("Resultados");
        gridPane.add(resultadosLabel, 1, 2);

        ListView<String> listaResultados = new ListView<String>();
        listaResultados.setPrefHeight(150);
        gridPane.add(listaResultados, 1, 3);

        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setMinSize(500,400);
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setAlignment(Pos.CENTER);
        Scene scene = new Scene(gridPane);
        stage.setScene(scene);

        stage.setTitle("Hash Double Join");
        stage.show();
    }

    public void handleExecutar() {
        System.out.println("Hi there! You clicked me!");
    }

    public int getNumeroTabelas(){
        return Leitor.containers.size();
    }

    public String[] getNomeColunas(int numTabela){
        Container container = Leitor.containers.get(numTabela);
        byte[] blocoControle = container.getControle().dados;
        int deslocamentoLinha = Bloco.byte2ToInt(Bloco.getBytes(blocoControle, 9, 2));
        String linhaColunas = Bloco.byteToString(Bloco.getBytes(blocoControle, 11, deslocamentoLinha));
        return linhaColunas.split("\\|");
    }

}
