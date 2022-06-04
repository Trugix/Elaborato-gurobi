import gurobi.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Main {

    //muro di costanti
    private static final String PATH = "src/load.txt";
    private static int[][] costi;

    private static GRBVar[][] x;
    private static GRBVar[] u;
    private static ArrayList<Integer> ordineSol = new ArrayList<>();
    private static int DIM;

    private static GRBEnv env;
    private static GRBModel model;

    //costanti quesito 3
    private static final int verticeSpeciale = 30;
    private static final double A = 0.09;

    private static final int B1 = 37;
    private static final int B2 = 41;
    private static final int C = 125;

    private static final int D1 = 17;
    private static final int D2 = 4;
    private static final int E1 = 28;
    private static final int E2 = 11;
    private static final int F1 = 18;
    private static final int F2 = 12;

    private static final int G1 = 32;
    private static final int G2 = 41;
    private static final int H1 = 14;
    private static final int H2 = 21;
    private static final int I1 = 12;
    private static final int I2 = 7;
    private static final int L = 5;


    public static void main(String[] args) throws GRBException {
        try {
            leggiFile(PATH);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        initialize();

        model.dispose();
        env.dispose();

    }

    /**
     * Stampa a schermo le richieste dei quesiti
     */
    private static void stampaOut()  {
        System.out.println("\n\nGRUPPO 33\nComponenti: Cesari, Signoroni\n");
    }

    /**
     * Stampa a schermo le richieste del quesito 1
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void printQuesito1() throws GRBException {

        System.out.println("Quesito 1:");
        System.out.println("Funzione obiettivo: " + model.get(GRB.DoubleAttr.ObjVal)); //valore funzione obiettivo
        System.out.print("Ciclo ottimo 1: [0");
        ArrayList<Integer> ciclo = new ArrayList<>();
        for (GRBVar num : u) {
            ciclo.add((int) num.get(GRB.DoubleAttr.X));
        }
        Collections.sort(ciclo);//ordino il vettore in base all'ordine di percorrenza
        for (Integer n : ciclo) {
            for (GRBVar num : u) { //stampo l`ordine di percorrenza
                if (n == (int) num.get(GRB.DoubleAttr.X)) {
                    int t = num.index();
                    int r = t + 1;
                    ordineSol.add(r);
                    System.out.print(", " + (t + 1));
                    break;
                }
            }
        }
        System.out.println(", 0]");
    }

    /**
     * Stampa a schermo la richiesta del quesito 2
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void printQuesito2() throws GRBException {


        System.out.println("\n\nQuesito 2:");
        Collections.reverse(ordineSol);
        if (checkCosto(ordineSol))  //stampa il quesito 2 solo se il check ha successo
        {
            System.out.print("Ciclo ottimo 2: [0");
            for (Integer n : ordineSol)
                System.out.print(", " + n);
            System.out.println(", 0]");
        } else {
            System.out.print("Non vi è una seconda soluzione ottima con valore " + (int) model.get(GRB.DoubleAttr.ObjVal));
        }
    }

    /**
     * stampa a schermo il quesito 3
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void printQuesito3() throws GRBException
    {
        System.out.println("\n\nQuesito 3:");
        System.out.println("Funzione obiettivo: " + model.get(GRB.DoubleAttr.ObjVal)); //valore funzione obiettivo
        System.out.print("Ciclo ottimo 3: [0");
        ArrayList<Integer> ciclo = new ArrayList<>();
        for (GRBVar num : u) {
            ciclo.add((int) num.get(GRB.DoubleAttr.X));
        }
        Collections.sort(ciclo); //ordino il vettore in base all'ordine di percorrenza
        for (Integer n : ciclo) {
            for (GRBVar num : u) { //stampo l`ordine di percorrenza
                if (n == (int) num.get(GRB.DoubleAttr.X)) {
                    int t = num.index();
                    System.out.print(", " + (t+1));
                    break;
                }
            }
        }
        System.out.println(", 0]");
    }

    /**
     * l'unico ottimo con valore uguale a quello del questito 1 che certamente esiste è se stesso al contrario, dato il grafo non orientato,
     * questo metodo si assicura che i due percorsi siano uguali
     *
     * @param order Arraylist da controllare
     * @return true se il costo è lo stesso, false altrimenti
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static boolean checkCosto(ArrayList<Integer> order) throws GRBException {
        int somma = 0;
        boolean first = true;

        for (int i = 0; i < DIM - 1; i++) {
            if (first) {
                first = false;
                somma += costi[0][order.get(i)];
            } else {
                somma += costi[order.get(i - 1)][order.get(i)];
                if (i == DIM - 2)
                    somma += costi[0][order.get(i)];
            }
        }
        return somma == (int) model.get(GRB.DoubleAttr.ObjVal);
    }

    /**
     * inizializzo i parametri di gurobi e creo le variabili necessarie
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void initialize() throws GRBException {
        env = new GRBEnv("Elaborato2Coppia33.log");
        env.set(GRB.IntParam.Threads, 4);
        model = new GRBModel(env);
        x = new GRBVar[DIM][DIM];   //variabili binarie che rappresentano se un arco è usato o no
        u = new GRBVar[DIM - 1];    //variabili che rappresentano quando un nodo è stato raggiunto
        for (int i = 0; i < DIM - 1; i++)
            u[i] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "u_" + i);
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++)
                x[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "x_" + i + "_" + j);
        }
        model.set(GRB.IntParam.LogToConsole, 0);
        loadObj();
        loadConstr();
    }

    /**
     * creo la funzione obiettivo
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadObj() throws GRBException {
        GRBQuadExpr obj = creaObiettivoQuad();

        model.setObjective(obj, GRB.MINIMIZE);
    }



    /**
     * creo tutti i vari constraint
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadConstr() throws GRBException {
        loadConstrDiagonale();
        loadConstrSommaRighe();
        loadConstrSommaColonne();

        loadConstrOrdine();

        model.optimize();   //
        stampaOut();        //  Quesiti 1 e 2
        printQuesito1();    //
        printQuesito2();    //

        loadConstrCostoMaxV(); //3a
        loadConstrArcoB(); //3b
        loadConstrArcoD();  //3c
        loadConstrCostAgg();  //3d

        model.optimize();    // Quesito 3
        printQuesito3();     //
    }

    private static void loadConstrCostAgg() {

    }

    /**
     * per quesito 3
     * constraint che imponga che l'arco d1-d2 venga usato solo se vengono usati
     * gli archi e1-e2 ed f1-f2
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadConstrArcoD() throws GRBException
    {
        GRBLinExpr constrArcoDE = new GRBLinExpr();
        GRBLinExpr constrArcoDF = new GRBLinExpr();

        constrArcoDE.addTerm(1, x[E1][E2]); //arco "e' e suo simmetrico
        constrArcoDE.addTerm(1, x[E2][E1]);

        constrArcoDF.addTerm(1, x[F1][F2]); //arco "f' e suo simmetrico
        constrArcoDF.addTerm(1, x[F2][F1]);

        model.addConstr(constrArcoDE, GRB.GREATER_EQUAL, x[D1][D2], "constrArcoDE1");
        model.addConstr(constrArcoDE, GRB.GREATER_EQUAL, x[D2][D1], "constrArcoDE2");   //posso usare "d" solo se ho usato "e"

        model.addConstr(constrArcoDF, GRB.GREATER_EQUAL, x[D1][D2], "constrArcoDF1");   //posso usare "d" solo se ho usato "f"
        model.addConstr(constrArcoDF, GRB.GREATER_EQUAL, x[D2][D1], "constrArcoDF2");
        //alla fine posso usare "d" solo se tutti e quattro i constraint sono veri, quindi posso usare "d" "solo se uso "e" ed "f"
    }

    /**
     * per quesito 3
     * constraint che controlla che se il lato b1-b2 viene percorso il costo sia minore di c
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadConstrArcoB() throws GRBException
    {
        GRBQuadExpr constrArcoB1 = new GRBQuadExpr();
        GRBQuadExpr constrArcoB2 = new GRBQuadExpr();

        GRBLinExpr objFantoccio = creaObiettivo();

        for (int i=0; i<objFantoccio.size();i++) {
            constrArcoB1.addTerm(1, objFantoccio.getVar(i), x[B1][B2]); //valore della funzione obiettivo moltiplicata per b1-b2 (0 od 1)
            constrArcoB2.addTerm(1, objFantoccio.getVar(i), x[B2][B1]);//valore della funzione obiettivo moltiplicata per b2-b1 (0 od 1)
        }

        model.addQConstr(constrArcoB1, GRB.LESS_EQUAL, C, "constrArcoB1");
        model.addQConstr(constrArcoB2, GRB.LESS_EQUAL, C, "constrArcoB2");
    }

    /**
     * metodo di servizio
     * crea una funzione obiettivo
     *
     * @return funzione obiettivo
     */
    private static GRBLinExpr creaObiettivo() {
        GRBLinExpr obj = new GRBLinExpr();
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++)
                if (i != j)
                    obj.addTerm(costi[i][j], x[i][j]);
        }
        return obj;
    }
    /**
     * metodo di servizio
     * crea una funzione obiettivo
     *
     * @return funzione obiettivo
     */
    private static GRBQuadExpr creaObiettivoQuad() {
        GRBQuadExpr obj = new GRBQuadExpr();
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++)
                if (i != j)
                    obj.addTerm(costi[i][j], x[i][j]);
        }
        return obj;
    }

    /**
     * per quesito 3
     * constraint che stabilisca che il costo del vertice V (arco entrata + arco uscita) debba essere minore od uguale dell`A% del costo totale
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadConstrCostoMaxV() throws GRBException {
        //todo entrata + uscita o separati?
        GRBLinExpr constrVerticeSpeciale;
        constrVerticeSpeciale = new GRBLinExpr();
        GRBLinExpr objFantoccio = new GRBLinExpr();

        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++)
                if (i != j)
                    objFantoccio.addTerm(A * costi[i][j], x[i][j]);   //valore della funzione obiettivo all'A%
        }
        //creo il constraint
        for (int i = 0; i < DIM; i++) {
            constrVerticeSpeciale.addTerm(costi[i][verticeSpeciale], x[i][verticeSpeciale]);
            constrVerticeSpeciale.addTerm(costi[verticeSpeciale][i], x[verticeSpeciale][i]);
        }
        model.addConstr(constrVerticeSpeciale, GRB.LESS_EQUAL, objFantoccio, "constrVerticeSpeciale");
    }

    /**
     * per quesito 1
     * per verificare che non ci siano loop interni e che ci sia un unico
     * percorso totale abbiamo implementato i vincoli di Miller–Tucker–Zemlin
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadConstrOrdine() throws GRBException {
        GRBLinExpr constrOrd;
        for (int i = 1; i < DIM; i++) //primo constraint
        {
            for (int j = 1; j < DIM; j++)
            {
                if (i != j)
                {
                    constrOrd = new GRBLinExpr();
                    constrOrd.addTerm(1, u[i - 1]);
                    constrOrd.addTerm(-1, u[j - 1]);
                    constrOrd.addTerm(DIM - 1, x[i][j]);
                    model.addConstr(constrOrd, GRB.LESS_EQUAL, DIM - 2, "costrOrd_" + (i + 1) + "_" + (j + 1));
                }
            }
        }
        GRBLinExpr constrVettUMin;
        GRBLinExpr constrVettUMax;
        for (int i = 0; i < DIM - 1; i++) //secondo constraint
        {
            constrVettUMin = new GRBLinExpr();
            constrVettUMin.addTerm(1, u[i]);
            constrVettUMax = new GRBLinExpr();
            constrVettUMax.addTerm(1, u[i]);
            model.addConstr(constrVettUMin, GRB.GREATER_EQUAL, 1, "costrVettMin_" + (i));
            model.addConstr(constrVettUMax, GRB.LESS_EQUAL, DIM - 1, "costrVettMax_" + (i));
        }
    }

    /**
     * per quesito 1
     * constraint che impone che le diagonali siano zero
     * ovvero un nodo non può partire ed arrivare a se stesso
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadConstrDiagonale() throws GRBException
    {
        GRBLinExpr constrDiag;
        for (int i = 0; i < DIM; i++) {
            constrDiag = new GRBLinExpr();
            constrDiag.addTerm(1, x[i][i]);
            model.addConstr(constrDiag, GRB.EQUAL, 0, "cDiag_" + (i + 1));  //constraint per evitare che i nodi looppino con se stesso
        }
    }

    /**
     * per quesito 1
     * controlla che la somma di ogni colonna sia esattamente 1
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadConstrSommaColonne() throws GRBException {
        GRBLinExpr constrSommaColonna;
        for (int j = 0; j < DIM; j++) {
            constrSommaColonna = new GRBLinExpr();
            for (int i = 0; i < DIM; i++)
                constrSommaColonna.addTerm(1, x[i][j]);
            model.addConstr(constrSommaColonna, GRB.EQUAL, 1, "costrSommaColonna_" + (j + 1));  //constraint colonne
        }
    }

    /**
     * per quesito 1
     * controlla che la somma di ogni riga sia esattamente 1
     *
     * @throws GRBException eccezione di gurobi da gestire
     */
    private static void loadConstrSommaRighe() throws GRBException {
        GRBLinExpr constrSommaRiga;
        for (int i = 0; i < DIM; i++) {
            constrSommaRiga = new GRBLinExpr();
            for (int j = 0; j < DIM; j++)
                constrSommaRiga.addTerm(1, x[i][j]);
            model.addConstr(constrSommaRiga, GRB.EQUAL, 1, "costrSommaRiga_" + (i + 1));    //constraint righe
        }
    }

    /**
     * Metodo di servizio
     * da copia33.txt abbiamo creato un altro file con solo i nodi e gli archi per evitare di mettere tutto a mano
     *
     * @param filepath nome e percorso del file da leggere
     * @throws IOException eccezzione di Input-Output
     */
    private static void leggiFile(String filepath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        try {
            StringBuilder sb = new StringBuilder();
            String line = sb.toString();
            boolean first = true;  //viene usata per inizializzare le dimensioni della tabella
            while (true) {
                int start;  //nodo di partenza
                int end;    //nodo di arrivo
                int cost;   //costo dell'arco
                String[] riga;  //riga letta
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
                if (line == null)   // in caso il file sia finito il ciclo si interrompe
                    break;
                riga = line.split(" "); //usiamo lo spazio come separatore delle String
                if (first) {
                    end = Integer.parseInt(riga[1]);
                    first = false;
                    DIM = end;
                    costi = new int[DIM][DIM];
                } else //costruisco la tabella dei costi
                {
                    start = Integer.parseInt(riga[0]);
                    end = Integer.parseInt(riga[1]);
                    cost = Integer.parseInt(riga[2]);
                    costi[start][end] = cost;
                    costi[end][start] = cost; //la matrice è simmetrica
                }
            }
            for (int i = 0; i < DIM; i++) //riempio la diagonale con "-1"
                costi[i][i] = -1;
        } finally {
            br.close(); //chiudo il file
        }
    }
}
