import gurobi.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Main {

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
        //env.set(GRB.IntParam.PoolSearchMode, 2);
        //env.set(GRB.IntParam.PoolSolutions, 10);
        model.optimize();
        //todo     printCiclo();

        //System.out.println(model.get(GRB.IntAttr.Status));
        model.dispose();
        env.dispose();

    }

    private static void stampaOut() throws GRBException {
        System.out.println("\n\nGRUPPO 33\nComponenti: Cesari, Signoroni\n");
        printQuesito1();
        printQuesito2();
    }

    private static void printQuesito2() throws GRBException {


        System.out.println("\n\nQuesito 2:");
        Collections.reverse(ordineSol);
        if(checkOttimo(ordineSol)) {
            System.out.print("Ciclo ottimo 2: [0");
            for (Integer n : ordineSol)
                System.out.print(", " + n);
            System.out.println(", 0]");
        }else {
            System.out.print("Non vi è una seconda soluzione ottima con valore " + (int)model.get(GRB.DoubleAttr.ObjVal));
        }

    }



    /**
     * l'unico ottimo con valore uguale a quello del questito 1 che certamente esiste è se stesso al contrario, dato il grafo non orientato,
     * questo metodo si assicura che i due percorsi siano uguali
     * @param order
     * @return
     * @throws GRBException
     */
    private static boolean checkOttimo(ArrayList<Integer> order) throws GRBException {
        int somma = 0;
        boolean first = true;
      /*for (int i =0; i<ordineSol.size();i++)
            System.out.print(ordineSol.get(i)+" ");*/

        for (int i = 0; i < DIM - 1; i++) {
            if (first) {
                first = false;
                somma += costi[0][ordineSol.get(i)];
                //System.out.print(costi[0][ordineSol.get(i)]+" ");
            } else {
                somma += costi[ordineSol.get(i-1)][ordineSol.get(i)];
                //System.out.print(costi[ordineSol.get(i-1)][ordineSol.get(i)]+" ");
                if (i == DIM-2) {
                    somma+=costi[0][ordineSol.get(i)];
                    //System.out.print(costi[0][ordineSol.get(i)]+" ");
                }
            }
        }
        if (somma==(int)model.get(GRB.DoubleAttr.ObjVal))
            return true;
        return false;
    }

    private static void printQuesito1() throws GRBException {

        System.out.println("Quesito 1:");
        System.out.println("Funzione obiettivo: " + model.get(GRB.DoubleAttr.ObjVal));
        System.out.print("Ciclo ottimo 1: [0");
        ArrayList<Integer> ciclo = new ArrayList<>();
        for (GRBVar num : u) {
            ciclo.add((int) num.get(GRB.DoubleAttr.X));
        }
        Collections.sort(ciclo);
        for (Integer n : ciclo) {
            for (GRBVar num : u) {
                if (n == (int) num.get(GRB.DoubleAttr.X)) {
                    int t = num.index();
                    int r = t+1;
                    ordineSol.add(r);
                    System.out.print(", " + (t + 1));
                    break;
                }
            }
        }
        System.out.println(", 0]");
    }

    private static void printCiclo() throws GRBException {
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++)
                System.out.print(x[i][j].get(GRB.DoubleAttr.X) + "\t\t");
            System.out.println();
        }
        System.out.println();
        System.out.println();
        ArrayList<Integer> ciclo = new ArrayList<>();
        for (GRBVar num : u) {
            ciclo.add((int) num.get(GRB.DoubleAttr.X));
            System.out.print(num.get(GRB.DoubleAttr.X) + "\t");
        }
        Collections.sort(ciclo);
        System.out.println();
        System.out.println();
        System.out.println();
        for (Integer n : ciclo) {
            //System.out.print(n+"\t");
            for (GRBVar num : u) {
                if (n == (int) num.get(GRB.DoubleAttr.X)) {
                    System.out.print((num.index() + 1) + "\t");
                    break;
                }
            }
        }
    }//todo


    /**
     * da copia33.txt abbiamo creato un altro file con solo i nodi e gli archi per evitare di mettere tutto a mano
     *
     * @param filepath
     * @throws IOException
     */
    private static void leggiFile(String filepath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        try {
            StringBuilder sb = new StringBuilder();
            String line = sb.toString();
            boolean first = true;  //viene usata per inizializzare le dimensioni della tabella
            while (line != null) {
                int start;
                int end;
                int cost;
                String[] riga;
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
                if (line == null)
                    break;
                riga = line.split(" ");
                if (first) {
                    end = Integer.parseInt(riga[1]);
                    first = false;
                    DIM = end;
                    costi = new int[DIM][DIM];
                } else {
                    start = Integer.parseInt(riga[0]);
                    end = Integer.parseInt(riga[1]);
                    cost = Integer.parseInt(riga[2]);
                    costi[start][end] = cost;
                    costi[end][start] = cost;
                }
            }
            for (int i = 0; i < DIM; i++)
                costi[i][i] = -1;

          /*  for (int i = 0; i < DIM; i++) {
                for (int j = 0; j < DIM; j++)
                    System.out.print(costi[i][j] + "\t");
                System.out.println();
            }*/
        } finally {
            br.close();
        }
    }

    private static void initialize() throws GRBException {
        env = new GRBEnv("Elaborato2Coppia33.log");
        env.set(GRB.IntParam.Threads, 12);//todo
        model = new GRBModel(env);
        x = new GRBVar[DIM][DIM];
        u = new GRBVar[DIM - 1];
        for (int i = 0; i < DIM - 1; i++) //todo vedere se va bene
            u[i] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "u_" + i);
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++)
                x[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "x_" + i + "_" + j);
        }
        model.set(GRB.IntParam.LogToConsole, 0);
        loadObj();
        loadConstr();
    }

    private static void loadObj() throws GRBException {
        GRBQuadExpr obj = creaObiettivoQuad();

        model.setObjective(obj, GRB.MINIMIZE);
    }



    private static void loadConstr() throws GRBException {
        loadConstrDiagonale();
        loadConstrSommaRighe();
        loadConstrSommaColonne();

        loadConstrOrdine();

        model.optimize();
        stampaOut();
        printQuesito1();
        printQuesito2();

        loadConstrCostoMaxV(); //3a
        loadConstrArcoB(); //3b
        loadConstrArcoD();  //3c
        loadConstrCostAgg();  //3d

        model.optimize();
        printQuesito3();
    }

    private static void loadConstrCostAgg() {

    }

    private static void loadConstrArcoD() throws GRBException
    {
        GRBLinExpr constrArcoD1 = new GRBLinExpr();
        GRBLinExpr constrArcoD2 = new GRBLinExpr();

        constrArcoD1.addTerm(1, x[D1][D2]);
        constrArcoD2.addTerm(1, x[D2][D1]);
        constrArcoD1.addConstant(1);
        constrArcoD2.addConstant(1);

        GRBLinExpr usoArchiCD = new GRBLinExpr();
        usoArchiCD.addTerm(1, x[E1][E2]);
        usoArchiCD.addTerm(1, x[E2][E1]);
        usoArchiCD.addTerm(1, x[F1][F2]);
        usoArchiCD.addTerm(1, x[F2][F1]);

        model.addConstr(constrArcoD1, GRB.LESS_EQUAL, usoArchiCD, "constrArcoD1");
        model.addConstr(constrArcoD2, GRB.LESS_EQUAL, usoArchiCD, "constrArcoD2");

    }


    private static void loadConstrArcoB() throws GRBException {
        GRBQuadExpr constrArcoB1 = new GRBQuadExpr();
        GRBQuadExpr constrArcoB2 = new GRBQuadExpr();//todo vedere se è giusto

        GRBLinExpr objFantoccio1 = creaObiettivo();
       /* GRBLinExpr arcoUsato1 = new GRBLinExpr();
        arcoUsato1.addTerm(1, x[nodoB1][nodoB2]);
        objFantoccio1.multAdd(1, arcoUsato1);*/

        GRBLinExpr objFantoccio2 = creaObiettivo();
      /*  GRBLinExpr arcoUsato2 = new GRBLinExpr();
        arcoUsato2.addTerm(1, x[nodoB2][nodoB1]);
        objFantoccio2.multAdd(1, arcoUsato2);*/
        for (int i=0; i<objFantoccio1.size();i++) {
            constrArcoB1.addTerm(1, objFantoccio1.getVar(i), x[B1][B2]);
        }
        for (int i=0; i<objFantoccio2.size();i++) {
            constrArcoB2.addTerm(1, objFantoccio2.getVar(i), x[B2][B1]);
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
     * constrain che stabilisca che il costo del vertice V debba essere minore od uguale dell`A% del costo totale
     *
     * @throws GRBException
     */
    private static void loadConstrCostoMaxV() throws GRBException {
        // GRBVar sommaSpeciale = model.addVar(0, GRB.INFINITY, 0 , GRB.INTEGER, "sommaSpeciale");
        //todo teoricamente fatto, maybe è giusto, maybe no
        GRBLinExpr constrVertice1;
        GRBLinExpr constrVertice2;
        constrVertice1 = new GRBLinExpr();
        GRBLinExpr objFantoccio = new GRBLinExpr();

        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++)
                if (i != j)
                    objFantoccio.addTerm(A * costi[i][j], x[i][j]);   //valore della funzione obiettivo all'A%
        }
        //creo i due constrain
        for (int i = 0; i < DIM; i++)
            constrVertice1.addTerm(costi[i][verticeSpeciale], x[i][verticeSpeciale]);
        constrVertice2 = new GRBLinExpr();
        for (int i = 0; i < DIM; i++)
            constrVertice2.addTerm(costi[verticeSpeciale][i], x[verticeSpeciale][i]);

        model.addConstr(constrVertice1, GRB.LESS_EQUAL, objFantoccio, "constrVertice1");
        model.addConstr(constrVertice2, GRB.LESS_EQUAL, objFantoccio, "constrVertice2");
    }

    private static void loadConstrOrdine() throws GRBException {
        GRBLinExpr constrOrd;
        for (int i = 1; i < DIM; i++) {

            for (int j = 1; j < DIM; j++) {
                if (i != j) {
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
        for (int i = 0; i < DIM - 1; i++) {
            constrVettUMin = new GRBLinExpr();
            constrVettUMin.addTerm(1, u[i]);
            constrVettUMax = new GRBLinExpr();
            constrVettUMax.addTerm(1, u[i]);
            model.addConstr(constrVettUMin, GRB.GREATER_EQUAL, 1, "costrVettMin_" + (i));
            model.addConstr(constrVettUMax, GRB.LESS_EQUAL, DIM - 1, "costrVettMax_" + (i));
        }

    }

    private static void loadConstrDiagonale() throws GRBException { //TODO se non funzia mettere if in tutti constr

        GRBLinExpr constrDiag;
        for (int i = 0; i < DIM; i++) {
            constrDiag = new GRBLinExpr();
            constrDiag.addTerm(1, x[i][i]);
            model.addConstr(constrDiag, GRB.EQUAL, 0, "cDiag_" + (i + 1));
        }
    }

    private static void loadConstrSommaColonne() throws GRBException {
        GRBLinExpr constrSommaColonna;
        for (int j = 0; j < DIM; j++) {
            constrSommaColonna = new GRBLinExpr();
            for (int i = 0; i < DIM; i++)
                constrSommaColonna.addTerm(1, x[i][j]);
            model.addConstr(constrSommaColonna, GRB.EQUAL, 1, "costrSommaColonna_" + (j + 1));
        }
    }

    private static void loadConstrSommaRighe() throws GRBException {
        GRBLinExpr constrSommaRiga;
        for (int i = 0; i < DIM; i++) {
            constrSommaRiga = new GRBLinExpr();
            for (int j = 0; j < DIM; j++)
                constrSommaRiga.addTerm(1, x[i][j]);
            model.addConstr(constrSommaRiga, GRB.EQUAL, 1, "costrSommaRiga_" + (i + 1));
        }
    }
}
