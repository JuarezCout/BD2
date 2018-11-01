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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Interface extends Application {
    List<ListView> listas = new ArrayList<ListView>();
    HashMap<Integer, int []> selecoes = new HashMap<>();
    int numeroTabelas = getNumeroTabelas();
    GerenciadorBucket gereciadorBucket = new GerenciadorBucket(numeroTabelas);

    public static void render(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        GridPane gridPane = new GridPane();

        for(int i = 0; i < numeroTabelas; i++ ){
            Text tableLabel = new Text("Tabela " + i);

            ObservableList<String> colunas = FXCollections.observableArrayList(getNomeColunas(i));
            ListView<String> listaColunas = new ListView<String>(colunas);
            listaColunas.setPrefHeight(colunas.size() * 24 + 2);
            listaColunas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listas.add(listaColunas);

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
        getSelecoes();
        Pagina pagina;

       for(int i = 0; i < numeroTabelas; i++){
            for(int j = 1; j < 1000000000; j++){
                pagina = new Pagina(i + 1, j, 0);
                Bloco bloco = GerenciadorBuffer.getBlocoBuffer(pagina);
                if(bloco == null) {
                    break;
                }
                executaHash(bloco, i);
            }
        }

        gereciadorBucket.comparaBuckects(selecoes);

    }

    void executaHash(Bloco bloco, int idTabela) {
        int i = 8;
        int tamBloco = Bloco.byteToInt(Bloco.getBytes(bloco.dados, 5, 3));

        while(tamBloco > i){ // percorre as tuplas do bloco
            int totalHash = 0;
            int numColuna = 0;
            int tamTupla = Bloco.byteToInt(Bloco.getBytes(bloco.dados, i , 3));
            int[] selecoesDaTabela = selecoes.get(idTabela);
            int h = i + 4;

            while(tamTupla + i > h) { //percorre colunas da tupla

                for (int j = 0; j < selecoesDaTabela.length; j++) {
                    if (numColuna == selecoesDaTabela[j]) {
                        byte[] dados = Bloco.getBytes(bloco.dados, h + 2, 2);
                        totalHash += funcaoHash(dados);
                    }
                }

                int tamColuna = Bloco.byte2ToInt(Bloco.getBytes(bloco.dados, h, 2));
                h += tamColuna + 2;
                numColuna++;
            }

            byte[] dadosTupla = Bloco.getBytes(bloco.dados, i, tamTupla + numColuna * 2 + 4);
            gereciadorBucket.adicionarTupla(dadosTupla, totalHash, idTabela);
            i += tamTupla + numColuna*2 + 4 ;
        }
    }

    int funcaoHash(byte[] dados){
        int totalHash = 0;

        for(int i = 0; i < dados.length; i++){
            totalHash += dados[i];
        }

        return totalHash % 3;
    }


    private void getSelecoes() {

        for(int i = 0; i < numeroTabelas; i++ ){
           ObservableList<Integer> selecoesTabela = listas.get(i).getSelectionModel().getSelectedIndices();
           int[] selecoesDaTabela = new int[selecoesTabela.size()];
           for(int j = 0; j < selecoesTabela.size(); j++){
               selecoesDaTabela[j] = selecoesTabela.get(j);
           }
           selecoes.put(i, selecoesDaTabela);
        }
    }


    public int getNumeroTabelas(){
        return Leitor.containers.size();
    }

    String[] getNomeColunas(int numTabela){
        Container container = Leitor.containers.get(numTabela);
        byte[] blocoControle = container.getControle().dados;
        int deslocamentoLinha = Bloco.byte2ToInt(Bloco.getBytes(blocoControle, 9, 2));
        String linhaColunas = Bloco.byteToString(Bloco.getBytes(blocoControle, 11, deslocamentoLinha));
        return linhaColunas.split("\\|");
    }

}
