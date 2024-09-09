package presentacion;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class Biblioteca extends JFrame implements ActionListener {

    private JLabel tituloPrincipal = new JLabel("Biblioteca", JLabel.CENTER);
    private JLabel tituloLabel = new JLabel("Titulo: ");
    private JLabel autorLabel = new JLabel("Autor: ");
    private JLabel añoLabel = new JLabel("Año: ");
    private JLabel rateLabel = new JLabel("Rating: ");
    private JTextField tituloTxt = new JTextField();
    private JTextField autorTxt = new JTextField();
    private JTextField añoTxt = new JTextField();
    private JTextField rateTxt = new JTextField();
    private JPanel librosPanel = new JPanel();
    private JButton agregarButton = new JButton("Agregar Libro");
    private JButton eliminarButton = new JButton("Eliminar Libro");
    private JFileChooser fileChooser = new JFileChooser();
    private File archivoLibros = new File("libros.bin");

    /*
    Formato libros.bin
    String titulo
    String autor
    int año de publicacion
    double rating
    boolean tiene imagen?
     */
    public Biblioteca() {
        setTitle("Biblioteca Virtual");
        getContentPane().setBackground(new Color(255, 249, 249));
        setSize(700, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        tituloPrincipal.setBounds(230, 20, 200, 40);
        tituloPrincipal.setFont(new Font("Serif", Font.BOLD, 30));
        tituloPrincipal.setForeground(new Color(242, 191, 191));
        add(tituloPrincipal);

        tituloLabel.setBounds(50, 70, 100, 20);
        tituloTxt.setBounds(100, 70, 200, 20);
        add(tituloLabel);
        add(tituloTxt);

        autorLabel.setBounds(50, 100, 100, 20);
        autorTxt.setBounds(100, 100, 200, 20);
        add(autorLabel);
        add(autorTxt);

        añoLabel.setBounds(330, 70, 100, 20);
        añoTxt.setBounds(380, 70, 200, 20);
        add(añoLabel);
        add(añoTxt);

        rateLabel.setBounds(330, 100, 100, 20);
        rateTxt.setBounds(380, 100, 200, 20);
        add(rateLabel);
        add(rateTxt);

        agregarButton.setBounds(230, 140, 200, 30);
        agregarButton.setBackground(new Color(242, 191, 191));
        agregarButton.setForeground(Color.white);
        agregarButton.addActionListener(this);
        add(agregarButton);

        eliminarButton.setBounds(230, 180, 200, 30);
        eliminarButton.setBackground(new Color(242, 191, 191));
        eliminarButton.setForeground(Color.white);
        eliminarButton.addActionListener(this);
        add(eliminarButton);

        librosPanel.setLayout(new GridLayout(0, 4, 0, 0));
        librosPanel.setBackground(Color.white);
        JScrollPane scrollLibros = new JScrollPane(librosPanel);
        scrollLibros.setBounds(30, 220, 600, 350);
        add(scrollLibros);

        mostrarLibros();
        setVisible(true);
    }

    private void agregarLibro() {
        String añoString = añoTxt.getText();
        String rateString = rateTxt.getText();
        String titulo = tituloTxt.getText();
        String autor = autorTxt.getText();
        int año;
        double rate;

        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El campo de título no puede estar vacío.");
            return;
        }
        if (autor.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El campo de autor no puede estar vacío.");
            return;
        }
        try {
            año = Integer.parseInt(añoString);
            rate = Double.parseDouble(rateString);
            if (año <= 0) {
                JOptionPane.showMessageDialog(null, "El año debe ser un número positivo.");
                return;
            } else if (rate <= 0 || rate > 5) {
                JOptionPane.showMessageDialog(null, "Rating debe ser de 1-5");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Por favor ingrese un año válido.");
            return;
        }

        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagenes", "jpg", "png", "jpeg"));
        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar una imagen para el libro.");
            return;
        }

        File imagenFile = fileChooser.getSelectedFile();
        try (RandomAccessFile raf = new RandomAccessFile(archivoLibros, "rw")) {
            // Mover el puntero al final del archivo para agregar nuevo libro
            raf.seek(raf.length());

            // Agrega los datos al archivo
            raf.writeUTF(titulo);
            raf.writeUTF(autor);
            raf.writeInt(año);
            raf.writeDouble(rate);
            raf.writeBoolean(true);
            raf.writeLong(imagenFile.length());

            // Lee y escribe la imagen en el archivo
            try (FileInputStream fis = new FileInputStream(imagenFile)) {
                byte[] data = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(data)) != -1) {
                    raf.write(data, 0, bytesRead);
                }
            }

            JOptionPane.showMessageDialog(this, "Libro agregado correctamente.");
            mostrarLibros();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eliminarLibro() {
        String titulo = JOptionPane.showInputDialog("Ingrese el título del libro que desea eliminar:");
        if (titulo == null || titulo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El título no puede estar vacío.");
            return;
        }

        File tempFile = new File("librosTemp.dat");

        try (RandomAccessFile raf = new RandomAccessFile(archivoLibros, "r"); 
             RandomAccessFile tempRaf = new RandomAccessFile(tempFile, "rw")) {

            while (raf.getFilePointer() < raf.length()) {
                long pos = raf.getFilePointer();
                String tituloActual = raf.readUTF();
                String autor = raf.readUTF();
                int año = raf.readInt();
                double rate = raf.readDouble();
                boolean tieneImagen = raf.readBoolean();

                if (tituloActual.equals(titulo)) {
                    // Skip los bytes de la imagen
                    if (tieneImagen) {
                        long imagenLength = raf.readLong();
                        raf.skipBytes((int) imagenLength);
                    }
                    continue; 
                }

                // escribir los datos en tempRaf
                tempRaf.writeUTF(tituloActual);
                tempRaf.writeUTF(autor);
                tempRaf.writeInt(año);
                tempRaf.writeDouble(rate);
                tempRaf.writeBoolean(tieneImagen);

                if (tieneImagen) {
                    long imagenLength = raf.readLong();
                    tempRaf.writeLong(imagenLength);
                    byte[] imageBytes = new byte[(int) imagenLength];
                    raf.readFully(imageBytes);
                    tempRaf.write(imageBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reemplaza el archivo original con el temporal
        if (archivoLibros.delete()) {
            tempFile.renameTo(archivoLibros);
            JOptionPane.showMessageDialog(this, "Libro eliminado correctamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el archivo original.");
        }

        mostrarLibros();
    }

    private void mostrarLibros() {
        librosPanel.removeAll();
        if (archivoLibros.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(archivoLibros, "r")) {
                while (raf.getFilePointer() < raf.length()) {
                    String titulo = raf.readUTF();
                    String autor = raf.readUTF();
                    int año = raf.readInt();
                    double rate = raf.readDouble();
                    boolean tieneImagen = raf.readBoolean();

                    String libroInfo = "Titulo: " + titulo
                            + "\nAutor: " + autor
                            + "\nAño: " + año
                            + "\nRating: " + rate;
                    JLabel libroLabel = new JLabel("<html>" + libroInfo.replace("\n", "<br>") + "</html>");
                    librosPanel.add(libroLabel);

                    if (tieneImagen) {
                        long imagenLength = raf.readLong();
                        byte[] imagenBytes = new byte[(int) imagenLength];
                        try {
                            raf.readFully(imagenBytes);
                            ImageIcon imageIcon = new ImageIcon(imagenBytes);
                            Image img = imageIcon.getImage();
                            ImageIcon scaledImageIcon = new ImageIcon(img.getScaledInstance(100, 150, Image.SCALE_SMOOTH));
                            JLabel imagenLabel = new JLabel(scaledImageIcon);
                            librosPanel.add(imagenLabel);
                        } catch (EOFException e) {
                            System.err.println("No se pudo leer la imagen completa: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        librosPanel.revalidate();
        librosPanel.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == agregarButton) {
            agregarLibro();
        } else if (e.getSource() == eliminarButton){
            eliminarLibro();
        }
    }

    public static void main(String[] args) {
        new Biblioteca();
    }
}
