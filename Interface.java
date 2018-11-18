import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interface extends Application {
    boolean executado = false;
    List<ListView> listas = new ArrayList<ListView>();
    static HashMap<Integer, int []> selecoes = new HashMap<>();
    HashMap<Integer, Integer> numTuplas = new HashMap<>();
    int numeroTabelas = getNumeroTabelas();
    GerenciadorBucket gereciadorBucket = new GerenciadorBucket(numeroTabelas);
    TableView<String[]> tabela = new TableView<>();
    static ObservableList<String[]> data = FXCollections.observableArrayList();
    static int tabelasBuildadas = 0;

    public static void render(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        GridPane gridPaneSelecoes = new GridPane();
        GridPane gridPaneResultado = new GridPane();

        for(int i = 0; i < numeroTabelas; i++ ){
            Text tableLabel = new Text("Tabela " + i);

            ObservableList<String> colunas = FXCollections.observableArrayList(getNomeColunas(i));
            ListView<String> listaColunas = new ListView<String>(colunas);
            listaColunas.setPrefHeight(colunas.size() * 24 + 2);
            listaColunas.setMaxWidth(200);
            listaColunas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listas.add(listaColunas);

            if(i == 0) {
                gridPaneSelecoes.add(tableLabel, i, i);
                gridPaneSelecoes.add(listaColunas, i, i + 1);
            } else {
                gridPaneSelecoes.add(tableLabel, i, i - 1);
                gridPaneSelecoes.add(listaColunas, i, i);
            }


        }


        Button executar = new Button("Executar Join");
        executar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleExecutar();
            }
        });
        gridPaneSelecoes.add(executar, 2,1);

        Text resultadosLabel = new Text("Resultados");
        gridPaneResultado.add(resultadosLabel, 0, 1);

        int totalColunas = 0;
        for(int i = 0; i < numeroTabelas; i++){
            String[] nomeColunas = getNomeColunas(i);

            for(int j = 0; j < nomeColunas.length; j++){
                TableColumn col = new TableColumn(nomeColunas[j]);

                int finalTotalColunas = totalColunas;
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> p) {
                        return new SimpleStringProperty((p.getValue()[finalTotalColunas]));
                    }
                });

                totalColunas++;
                tabela.getColumns().add(col);
            }
        }

        tabela.setPrefHeight(300);
        tabela.setItems(data);
        gridPaneResultado.add(tabela, 0, 2);

        gridPaneSelecoes.setPadding(new Insets(5, 10, 5, 0));
        gridPaneSelecoes.setVgap(5);
        gridPaneSelecoes.setHgap(5);
        gridPaneSelecoes.setAlignment(Pos.CENTER_LEFT);

        gridPaneResultado.add(gridPaneSelecoes, 0, 0);

        gridPaneResultado.setPadding(new Insets(10, 10, 10, 10));
        gridPaneResultado.setMinSize(500,400);
        gridPaneResultado.setVgap(5);
        gridPaneResultado.setHgap(5);
        gridPaneResultado.setAlignment(Pos.CENTER);
        Scene scene = new Scene(gridPaneResultado);
        stage.setScene(scene);

        stage.setTitle("Hash Double Join");
        stage.show();
    }

    public void handleExecutar() {
        getSelecoes();

        if(executado == true){
            tabelasBuildadas = 0;
            gereciadorBucket.apagaBuckets(numeroTabelas);
            numTuplas.clear();
            tabela.getItems().clear();
        }

        //gereciadorBucket.setBucketMontagem(true);

       for(int i = 0; i < numeroTabelas; i++){
           executaHashContainer(i);
           //mostrarDistribuicaoBuckets(i);
       }
       gereciadorBucket.criaThreadProbe();
       gereciadorBucket.criaThreadProbe();

        //gereciadorBucket.setBucketMontagem(false);

        executado = true;


        //resultados = gereciadorBucket.comparaBuckets(selecoes);
        //gereciadorBucket.limpaMemoria();
        //resultados.trimToSize();
        //data.addAll(resultados);

    }

    //cria thread que executa o build
    void executaHashContainer(int idTabela) {
        new Thread() {
            Pagina pagina;

            public void run() {
                System.out.println("Iniciando thread de build da tabela " + idTabela + " ...");
                for(int j = 1; j < 1000000000; j++){
                    pagina = new Pagina(idTabela + 1, j, 0);
                    Bloco bloco = GerenciadorBuffer.getBlocoBuffer(pagina);
                    if(bloco == null) {
                        tabelasBuildadas++;
                        break;
                    }
                    executaHash(bloco, idTabela);
                }
            }
        }.start();
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
                int tamColuna = Bloco.byte2ToInt(Bloco.getBytes(bloco.dados, h, 2));

                for (int j = 0; j < selecoesDaTabela.length; j++) {
                    byte[] dados;

                    if (numColuna == selecoesDaTabela[j]) {
                        if(tamColuna == 1){
                            dados = Bloco.getBytes(bloco.dados, h + 2, 1);
                        } else if(tamColuna == 2){
                            dados = Bloco.getBytes(bloco.dados, h + 2, 2);
                        } else {
                            dados = Bloco.getBytes(bloco.dados, h + 2, 3);
                        }
                        totalHash += funcaoHash(dados);
                    }
                }


                h += tamColuna + 2;
                numColuna++;
            }

            if(numTuplas.get(totalHash) == null){
                numTuplas.put(totalHash, 1);
            } else {
                numTuplas.put(totalHash, numTuplas.get(totalHash) + 1);
            }

            byte[] dadosTupla = Bloco.getBytes(bloco.dados, i, tamTupla + numColuna * 2 + 4);
            gereciadorBucket.adicionarTupla(dadosTupla, totalHash, idTabela);
            i += tamTupla + numColuna*2 + 4 ;
        }
    }

    int funcaoHash(byte[] dados){
        int totalHash = 0;

        for(int i = 1; i <= dados.length; i++){
            totalHash = totalHash + (i * dados[i - 1]);
        }

        return totalHash;
    }


    private void getSelecoes() {
        selecoes.clear();
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

    void mostrarDistribuicaoBuckets(int idTabela){
        for (Map.Entry<Integer, Integer> entry : numTuplas.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();

            System.out.println();
            System.out.println("O bucket " + key + " da tabela " + idTabela + " recebeu " + value + " tuplas");
        }
    }

}
