package Transformar;

import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

public class Transformador {

    private String nombre;
    private int numestados;
    private int estadoInicial;
    private TreeSet<String> alfabeto;
    private TreeSet<Integer> estadoFinal;
    private TreeSet<Integer>[][] tabtrans;

    public Automata minimizar(Automata automata) {

        nombre = automata.getNombre();
        numestados = automata.getnumEstados();
        alfabeto = automata.getAlfabeto();
        estadoInicial = automata.getEstadoInicial();
        estadoFinal = automata.getestadoFinal();
        tabtrans = automata.getTablaTransiciones();

        if (alfabeto.contains("E")) {
            JOptionPane.showMessageDialog(null, "Quitando tranciciones vacias");

            quitarTansicionesVacias();
            JOptionPane.showMessageDialog(null, "tranciciones vacias quitadas");

        } else {
            JOptionPane.showMessageDialog(null, "no tiene tranciciones vacias");


        }
        if (noEsDeterminista()) {
            JOptionPane.showMessageDialog(null, "Quitando indeterminismo");

            quitarIndeterminismo();
            JOptionPane.showMessageDialog(null, "Indeterminismo quitado");

        } else {
            JOptionPane.showMessageDialog(null, "Ya es determinista");

        }

        while (!verificarMinimo()) {
            minimizar();
        }
        JOptionPane.showMessageDialog(null, "Es minimo");

        return new Automata(nombre, numestados, alfabeto, estadoInicial, estadoFinal, tabtrans);

    }

    private void quitarIndeterminismo() {

        Vector<TreeSet> nuevosEstados = new Vector<TreeSet>();
        TreeSet<Integer> ts;
        TreeSet<Integer> ts2;

        TreeSet<Integer> c = new TreeSet<Integer>();
        c.add(0);
        nuevosEstados.add(c);

        for (String s : alfabeto) {
            for (int cont = 0; cont < numestados; cont++) {
                ts = obtenerTransicion(cont, s);
                if (ts.size() != 0 && !nuevosEstados.contains(ts)) {
                    nuevosEstados.add(ts);
                }
            }
        }
        Vector<TreeSet> temporal = (Vector<TreeSet>) nuevosEstados.clone();

        for (TreeSet<Integer> t : temporal) {
            ts2 = new TreeSet<Integer>();
            for (String s : alfabeto) {
                for (Integer i : t) {
                    ts2.addAll(obtenerTransicion(i, s));
                }
                if (ts2.size() != 0 && !nuevosEstados.contains(ts2)) {
                    nuevosEstados.add(ts2);
                }
            }
        }

        TreeSet<Integer>[][] tablaaux = new TreeSet[nuevosEstados.size()][alfabeto.size()];


        TreeSet<Integer> tranO, tran;
        for (String s : alfabeto) {
            for (TreeSet<Integer> t : nuevosEstados) {
                tranO = new TreeSet<Integer>();
                tran = new TreeSet<Integer>();
                for (Integer i : t) {
                    tranO.addAll(obtenerTransicion(i, s));
                }

                ///-nuevo
                if (nuevosEstados.indexOf(tranO) != -1) {
                    tran.add(nuevosEstados.indexOf(tranO));
                }

                Vector<String> a = new Vector<String>();
                a.addAll(alfabeto);
                tablaaux[nuevosEstados.indexOf(t)][a.indexOf(s)] = tran;
            }
        }

        TreeSet<Integer> finales = new TreeSet<Integer>();

        for (TreeSet<Integer> t : nuevosEstados) {
            for (Integer i : estadoFinal) {
                if (t.contains(i)) {
                    finales.add(nuevosEstados.indexOf(t));
                }
            }
        }

        numestados = nuevosEstados.size();
        estadoFinal = finales;
        tabtrans = tablaaux;
        System.out.println();
    }

    private boolean noEsDeterminista() {
        boolean b = false;
        TreeSet<Integer> ts = new TreeSet<Integer>();
        for (String s : alfabeto) {
            for (int cont = 0; cont < numestados; cont++) {
                ts = obtenerTransicion(cont, s);
                if (ts != null && ts.size() > 1) {
                    b = true;
                }
            }
        }
        return b;
    }

    private void quitarTansicionesVacias() {
        TreeSet<Integer> tran;
        TreeSet<Integer> clau;
        TreeSet<Integer> clau2;

        TreeSet<String> alfabetoTemp = (TreeSet<String>) alfabeto.clone();
        alfabetoTemp.remove("E");
        TreeSet<Integer>[][] tablatransicionesTemp = new TreeSet[numestados][alfabetoTemp.size()];

        for (int a = 0; a < alfabetoTemp.size(); a++) {
            for (int q = 0; q < numestados; q++) {
                tablatransicionesTemp[q][a] = new TreeSet<Integer>();
            }

        }

        for (String s : alfabeto) {
            if (!s.equals("E")) {
                for (int cont = 0; cont < numestados; cont++) {
                    //System.out.print(cont + " "+s+" -");

                    tran = new TreeSet<Integer>();
                    clau = cerrarVacias(cont);
                    clau2 = new TreeSet<Integer>();
                    for (Integer i : clau) {
                        tran.addAll(obtenerTransicion(i.intValue(), s));
                    }
                    for (Integer i : tran) {
                        clau2.addAll(cerrarVacias(i.intValue()));

                        Vector<String> a = new Vector<String>();
                        a.addAll(alfabetoTemp);
                        tablatransicionesTemp[cont][a.indexOf(s)].addAll(clau2);
                    }
                }
            }
        }

        TreeSet<Integer> f = cerrarVacias(estadoInicial);
        boolean cq0F = false;

        for (Integer i : estadoFinal) {
            if (f.contains(i)) {
                cq0F = true;
            }
        }

        if (cq0F) {
            estadoFinal.add(estadoInicial);
        }
        alfabeto = alfabetoTemp;
        tabtrans = tablatransicionesTemp;

        System.out.println();
    }

    private TreeSet<Integer> cerrarVacias(int q) {
        TreeSet<Integer> cierre = new TreeSet<Integer>();
        TreeSet<Integer> ts = new TreeSet<Integer>();
        Stack<TreeSet<Integer>> pila = new Stack<TreeSet<Integer>>();
        pila.push(obtenerTransicion(q, "E"));
        cierre.add(q);

        while (!pila.isEmpty()) {
            ts = pila.pop();

            for (Integer i : ts) {
                if (!cierre.contains(i.intValue())) {
                    pila.push(obtenerTransicion(i.intValue(), "E"));
                }
            }
            cierre.addAll(ts);
        }
        return cierre;
    }

    private TreeSet<Integer> obtenerTransicion(int q0, String e) {
        Vector<String> a = new Vector<String>();
        a.addAll(alfabeto);
        return tabtrans[q0][a.indexOf(e)];
    }

    private boolean verificarMinimo() {
        boolean f = true;

        int[][] estados = new int[numestados][numestados];
        TreeSet<Integer> r;
        TreeSet<Integer> t;
        int y;
        int x;
        int tamaño = 0;
        for (int cont = 1; cont < numestados; cont++) {
            for (int cont2 = 0; cont2 < cont; cont2++) {
                if ((estadoFinal.contains(cont) && !estadoFinal.contains(cont2)) || (estadoFinal.contains(cont2) && !estadoFinal.contains(cont))) {
                    estados[cont][cont2] = 1;
                }
                tamaño = 0;
                for (String s : alfabeto) {
                    r = obtenerTransicion(cont, s);
                    t = obtenerTransicion(cont2, s);
                    if (r.size() > 0 && t.size() > 0) {
                        tamaño++;
                        y = r.first().intValue();
                        x = t.first().intValue();

                        if (x < y) {
                            if (estados[y][x] == 1) {
                                estados[cont][cont2] = 1;
                            }
                        } else {
                            if (estados[x][y] == 1) {
                                estados[cont][cont2] = 1;
                            }
                        }
                        if (y != x) {
                            estados[cont][cont2] = 1;
                        }
                    }
                }
                if (tamaño != alfabeto.size()) {
                    estados[cont][cont2] = 1;
                }
            }
        }

        for (int cont = 1; cont < numestados; cont++) {
            for (int cont2 = 0; cont2 < cont; cont2++) {
                if (estados[cont][cont2] == 0) {
                    f = false;
                }
            }
        }

        return f;

    }

    private void minimizar() {

        int[][] estados = new int[numestados][numestados];
        TreeSet<Integer> r;
        TreeSet<Integer> t;
        int x;
        int y;
        int tamaño = 0;
        for (int cont = 1; cont < numestados; cont++) {
            for (int cont2 = 0; cont2 < cont; cont2++) {
                if ((estadoFinal.contains(cont) && !estadoFinal.contains(cont2)) || (estadoFinal.contains(cont2) && !estadoFinal.contains(cont))) {
                    estados[cont][cont2] = 1;
                }
                tamaño = 0;
                for (String s : alfabeto) {
                    r = obtenerTransicion(cont, s);
                    t = obtenerTransicion(cont2, s);
                    if (r.size() > 0 && t.size() > 0) {
                        tamaño++;
                        x = r.first().intValue();
                        y = t.first().intValue();

                        if (y < x) {
                            if (estados[x][y] == 1) {
                                estados[cont][cont2] = 1;
                            }
                        } else {
                            if (estados[y][x] == 1) {
                                estados[cont][cont2] = 1;
                            }
                        }
                        if (x != y) {
                            estados[cont][cont2] = 1;
                        }
                    }
                }
                if (tamaño != alfabeto.size()) {
                    estados[cont][cont2] = 1;
                }
            }
        }
        Vector<TreeSet> vector = new Vector<TreeSet>();
        TreeSet<Integer> ts;
        boolean f;

        for (int cont = 1; cont < numestados; cont++) {
            for (int cont2 = 0; cont2 < cont; cont2++) {
                if (estados[cont][cont2] == 0) {
                    ts = new TreeSet<Integer>();
                    f = true;

                    ts.add(cont);
                    ts.add(cont2);

                    for (TreeSet<Integer> tsmod : vector) {
                        if (tsmod.contains(cont) || tsmod.contains(cont)) {
                            tsmod.addAll(ts);
                            f = false;
                        }
                    }
                    if (f) {
                        vector.add(ts);
                    }
                }
            }
        }

        f = true;

        for (int cont = 0; cont < numestados; cont++) {
            f = true;
            for (TreeSet<Integer> tsmod : vector) {
                if (tsmod.contains(cont)) {
                    f = false;
                }
            }
            if (f) {
                ts = new TreeSet<Integer>();
                ts.add(cont);
                vector.add(ts);
            }
        }

        TreeSet<Integer>[][] tablaTemp = new TreeSet[vector.size()][alfabeto.size()];

        TreeSet<Integer> tran;
        int t0;
        TreeSet<Integer> t1;
        for (String s : alfabeto) {
            for (TreeSet<Integer> tsi : vector) {
                tran = new TreeSet<Integer>();
                for (Integer i : tsi) {
                    tran.addAll(obtenerTransicion(i, s));
                }

                t0 = vector.indexOf(tsi);
                t1 = new TreeSet<Integer>();
                for (TreeSet<Integer> tsi2 : vector) {
                    if (tran.size() > 0 && tsi2.containsAll(tran)) {
                        t1.add(vector.indexOf(tsi2));
                    }
                }

                Vector<String> a = new Vector<String>();
                a.addAll(alfabeto);
                tablaTemp[t0][a.indexOf(s)] = t1;

            }
        }

        TreeSet<Integer> finales = new TreeSet<Integer>();
        int q00 = estadoInicial;

        for (TreeSet<Integer> i : vector) {
            if (i.contains(estadoInicial)) {
                q00 = vector.indexOf(i);
            }

            for (Integer ii : estadoFinal) {
                if (i.contains(ii)) {
                    finales.add(vector.indexOf(i));
                }
            }
        }

        estadoInicial = q00;
        numestados = vector.size();
        estadoFinal = finales;
        tabtrans = tablaTemp;

        System.out.println();

    }
}
