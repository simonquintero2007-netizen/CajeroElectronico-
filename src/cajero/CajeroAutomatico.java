package cajero;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class CajeroAutomatico extends JFrame {

    private static final int[] DENOMINACIONES = {10000, 20000, 50000, 100000};
    private static final int CANTIDAD_INICIAL = 100;
    private static final int MAX_DIGITOS = 7;
   
    private static final int RETIRO_MINIMO = 10000;

    private static final int RETIRO_MAXIMO = 1000000;

    private HashMap<Integer, Integer> billetes = new HashMap<>();
    private StringBuilder entrada = new StringBuilder();

    // Sirve para mostrar los números con puntos de miles
    private NumberFormat formato = NumberFormat.getInstance(Locale.forLanguageTag("es-CO"));

    // INTERFAZ 
    private JTextArea pantalla;         // pantalla donde se escribe y se muestran resultados

    private JTable tabla;                    // tabla de billetes

    private DefaultTableModel modeloTabla;   // datos de la tabla (filas y columnas)
    private JLabel lblTotal;                 // texto con el total de dinero del cajero

    public CajeroAutomatico() {
        inicializarBilletes();
        construirInterfaz();
        actualizarTabla();
        mostrarEntrada();

        setTitle("Cajero Electrónico");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();                                  // ajusta el tamaño de la ventana a los componentes
        setLocationRelativeTo(null);             // centra la ventana en la pantalla
        setResizable(false);                     // no se puede cambiar el tamaño
    }

    private void inicializarBilletes() {
        for (int denominacion : DENOMINACIONES) {

            billetes.put(denominacion, CANTIDAD_INICIAL);
        }
    }

    //CONSTRUCCIÓN DE LA INTERFAZ
    private void construirInterfaz() {
        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Pantalla
        pantalla = new JTextArea(8, 25);   // 8 filas y 25 columnas;   // 5 filas y 25 columnas
        pantalla.setEditable(false);       // el usuario no puede escribir con el teclado del PC
        pantalla.setFont(new Font("Monospaced", Font.BOLD, 20));
        pantalla.setLineWrap(true);        // salta de línea si el texto es muy largo
        pantalla.setWrapStyleWord(true);   // el salto lo hace entre palabras, no a mitad
        add(new JScrollPane(pantalla), BorderLayout.NORTH);

        // teclado a la izquierda y botones a la derecha
        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(crearTeclado(), BorderLayout.CENTER);
        centro.add(crearBotonesAccion(), BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);

        // la tabla de billetes
        add(crearPanelTabla(), BorderLayout.SOUTH);
    }

    // teclado numérico
    private JPanel crearTeclado() {
        // cuadrícula de 4 filas x 3 columnas, con 5 px de separación
        JPanel panel = new JPanel(new GridLayout(4, 3, 5, 5));
        String[] teclas = {"7", "8", "9",
            "4", "5", "6",
            "1", "2", "3",
            "0", "00", "000"};

        //botón por cada tecla
        for (String tecla : teclas) {
            JButton boton = crearBoton(tecla);
            // Al pulsar el botón se agregan sus dígitos a la entrada
            boton.addActionListener(e -> agregarDigitos(tecla));
            panel.add(boton);
        }
        return panel;
    }

    private JPanel crearBotonesAccion() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5)); // 3 filas, 1 columna
        panel.setPreferredSize(new Dimension(130, 0));         // ancho fijo de 130 px

        JButton btnRetirar = crearBoton("Retirar");

        JButton btnLimpiar = crearBoton("Limpiar");

        JButton btnBorrar = crearBoton("Borrar");
        btnRetirar.addActionListener(e -> retirar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnBorrar.addActionListener(e -> retroceso());

        panel.add(btnRetirar);
        panel.add(btnLimpiar);
        panel.add(btnBorrar);
        return panel;
    }

    private JPanel crearPanelTabla() {
        modeloTabla = new DefaultTableModel(
                new String[]{"Denominación", "Cantidad", "Valor total"}, 0) {

            public boolean isCellEditable(int fila, int columna) {

                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(25);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabla.getTableHeader().setReorderingAllowed(false); // no mover columnas
        tabla.setPreferredScrollableViewportSize(new Dimension(400, 100));

        lblTotal = new JLabel("", SwingConstants.RIGHT); // texto alineado a la derecha
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER); // JScrollPane muestra los encabezados
        panel.add(lblTotal, BorderLayout.SOUTH);
        return panel;
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 18));
        boton.setFocusPainted(false);
        return boton;
    }

    //TABLA
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        long totalCajero = 0;

        for (int denominacion : DENOMINACIONES) {
            int cantidad = billetes.get(denominacion);

            long valorTotal = (long) denominacion * cantidad;

            totalCajero += valorTotal;

            modeloTabla.addRow(new Object[]{
                "$ " + formato.format(denominacion),
                cantidad,
                "$ " + formato.format(valorTotal)
            });
        }
        lblTotal.setText("Total en el cajero: $ " + formato.format(totalCajero));
    }

    //PANTALLA Y TECLADO
    private void agregarDigitos(String digitos) {
        if (entrada.length() + digitos.length() > MAX_DIGITOS) {
            return;
        }

        if (entrada.length() == 0 && digitos.startsWith("0")) {
            return;
        }
        entrada.append(digitos);
        mostrarEntrada();
    }

    private void limpiar() {
        entrada.setLength(0);
        mostrarEntrada();
    }

    private void retroceso() {
        if (entrada.length() > 0) {
            entrada.deleteCharAt(entrada.length() - 1);
        }
        mostrarEntrada();
    }

    private void mostrarEntrada() {
        String valor = "0";
        
        
        if (entrada.length() > 0) {
            
                   valor = formato.format(Long.parseLong(entrada.toString()));
        }
           pantalla.setText("Valor a retirar:\n$ " + valor);
    } 
        
   
    private void retirar() {
        
        
        
        
        if (entrada.length() == 0) {
          
            
            mostrarMensaje("Error: digite el valor a retirar.");
            return;
        }

        int monto = Integer.parseInt(entrada.toString());

  
        if (monto % 10 != 0) {
            
                       mostrarMensaje("Error: el valor debe ser múltiplo de 10.");
            return;
        }

        if (monto < RETIRO_MINIMO) {
            
            
                     mostrarMensaje("Error: el retiro minimo es de $ " + formato.format(RETIRO_MINIMO));
            return;
        }
        if (monto > RETIRO_MAXIMO) {
           
                       mostrarMensaje("Error: el retiro maximo es de $ " + formato.format(RETIRO_MAXIMO));
            return;
        }

        if (monto > calcularTotalCajero()) {
            
            
                        mostrarMensaje("Error: el cajero no tiene saldo suficiente.");
            return;
        }

        HashMap<Integer, Integer> entrega = calcularEntrega(monto);
                 
              if (entrega == null) {
           
            
            
            mostrarMensaje("Error: no se puede entregar $ " + formato.format(monto)
                    + " con los billetes disponibles.");
            return;
        }

              
        descontarBilletes(entrega);
        actualizarTabla();

        mostrarMensaje("Retiro exitoso: $ " + formato.format(monto)
                + "\n\nBilletes entregados:\n" + armarDistribucion(entrega));

        entrada.setLength(0);
    }

   
          private int calcularTotalCajero() {
           
              int total = 0;

        for (int denominacion : DENOMINACIONES) {
            
             
                   int cantidad = billetes.get(denominacion);
                    total += denominacion * cantidad;
        }
        return total;
    }

 
    private void mostrarMensaje(String mensaje) {
        
        pantalla.setText(mensaje);
    }
    

 
                     private HashMap<Integer, Integer> calcularEntrega(int monto) {
        
        HashMap<Integer, Integer> entrega = new HashMap<>();
                 int restante = monto; 
                 
        for (int denominacion : DENOMINACIONES) {
            entrega.put(denominacion, 0);
        }

        if (monto == 50000 && hayBilletes(20000, 2) && hayBilletes(10000, 1)) {
            entrega.put(20000, 2);
            entrega.put(10000, 1);
            return entrega;
        }

  
        if (monto >= 100000 && hayBilletes(50000, 1)
                && hayBilletes(20000, 2) && hayBilletes(10000, 1)) {
            entrega.put(50000, 1);
            entrega.put(20000, 2);
            entrega.put(10000, 1);
            restante -= 100000;
        }

        for (int i = DENOMINACIONES.length - 1; i >= 0; i--) {
            int denominacion = DENOMINACIONES[i];
            
            
                       int necesarios = restante / denominacion;
             int disponibles = billetes.get(denominacion) - entrega.get(denominacion);             
             
            int usados = Math.min(necesarios, disponibles);

                       entrega.put(denominacion, entrega.get(denominacion) + usados);                     
                       restante -= usados * denominacion;
                       
        }

        if (restante != 0) {
            return null;
        }
        
        
        
          return entrega;
    }
                     
    
    private void descontarBilletes(HashMap<Integer, Integer> entrega) {
                  for (int denominacion : DENOMINACIONES) {
            
            
            
                    int nuevaCantidad = billetes.get(denominacion) - entrega.get(denominacion);
           
                                  billetes.put(denominacion, nuevaCantidad);
        }
                  
                  
                  
    }
                     
    private boolean hayBilletes(int denominacion, int cantidad) {
        
        return billetes.get(denominacion) >= cantidad;
    }

    private String armarDistribucion(HashMap<Integer, Integer> entrega) {
                   StringBuilder texto = new StringBuilder();
        
                   for (int i = DENOMINACIONES.length - 1; i >= 0; i--) {
                     int denominacion = DENOMINACIONES[i];
                     
            
                     int cantidad = entrega.get(denominacion);
                     
                     
            if (cantidad > 0) {
                
                
                texto.append(cantidad).append(" de $ ")
                        
                        
                        
                        .append(formato.format(denominacion)).append("\n");
            }
        }
        return texto.toString();
    }

}
