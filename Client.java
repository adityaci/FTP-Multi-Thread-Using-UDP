package ftpudp;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Aditya.C.I
 */

public class Client {
    
    private DefaultTableModel dtm;
    
    public static void main(String[] args) {
        Client c = new Client();
        c.init();
    }
    
    public void init(){
        String column[] = {"File Name", "Hapus"};
        ArrayList<String> data = new ArrayList<String>();
        ArrayList<String> dataNamaFile = new ArrayList<String>();
        
        JFrame window = new JFrame("FTP Client");
        
        JLabel pilihFileText = new JLabel("Pilih File : ");
        pilihFileText.setBounds( 10, 20, 55, 19 );
        
        JTextField pilihFile = new JTextField("Pilih File...");
        pilihFile.setBounds( 70, 20, 230, 20 );
        pilihFile.setEditable(false);
        pilihFile.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                pilihFile( data, dataNamaFile );
            }
        });
        
        JButton tombolPilihFile = new JButton("Browse");
        tombolPilihFile.setBounds( 305, 20, 80, 20 );
        tombolPilihFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pilihFile( data, dataNamaFile );
            }
        });
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        
        JTable tableData = new JTable();
        dtm = new DefaultTableModel(0, 0);
        dtm.setColumnIdentifiers( column );
        tableData.setModel( dtm );
        tableData.setEnabled( false );
        tableData.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
        tableData.getColumn("Hapus").setMaxWidth(60);
        tableData.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableData.rowAtPoint(evt.getPoint());
                int col = tableData.columnAtPoint(evt.getPoint());
                if ( col == 1) {
                    data.remove( row );
                    dataNamaFile.remove( row );
                    dtm.removeRow( row );
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane( tableData );
        scrollPane.setBounds( 10, 50, 365, 370 );
        
        JButton tombolProses = new JButton("Proses");
        tombolProses.setBounds( 150, 425, 100, 40 );
        tombolProses.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SendThread st;
                for(int i=0; i<data.size(); i++){
                    st = new SendThread( data.get(i), dataNamaFile.get(i) );
                    st.prosesKirim();
                }
                data.clear();
                dataNamaFile.clear();
                dtm.setRowCount(0);
            }
        });
        
        window.setSize( 400, 500 );
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(null);
        window.add( pilihFileText );
        window.add( pilihFile );
        window.add( tombolPilihFile );
        window.add( scrollPane );
        window.add( tombolProses );
        window.setVisible( true );
    }
    
    public void pilihFile( ArrayList<String> data, ArrayList<String> dataNamaFile ){
        FileDialog dialog = new FileDialog((Frame)null, "Pilih File");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String file = dialog.getFile();
        if(file != null ){
            String path = dialog.getDirectory() + dialog.getFile();
            if( (int)new File(path).length() < 50000 ){
                data.add( new File( path ).toString() );
                dataNamaFile.add( file );
                dtm.addRow(new Object[] { file, "Hapus" });
            }else
                JOptionPane.showMessageDialog(null, "File size tidak boleh lebih dari 50Kb!");
        }
    }
    
    public class SendThread extends Thread{
        
        private final String filePath, namaFile;
        
        public SendThread( String filePath, String namaFile ){
            this.filePath = filePath;
            this.namaFile = namaFile;
        }
        
        public void prosesKirim(){  
            try {
                String data = "";
                String dataSize = "";
                DatagramPacket dp;
                FileInputStream fis = new FileInputStream( new File(filePath) );
                byte[] dataBytes = new byte[ (int)new File(filePath).length() ];
                fis.read(dataBytes, 0, dataBytes.length);
                fis.close();

                DatagramSocket ds = new DatagramSocket();
                InetAddress ip = InetAddress.getByName("127.0.0.1");
                data = namaFile + ";" + Base64.getEncoder().encodeToString( dataBytes );
                dataSize = String.valueOf( data.length() );
                dp = new DatagramPacket(dataSize.getBytes(), dataSize.length(), ip, 3000);
                ds.send(dp);
                dp = new DatagramPacket(data.getBytes(), data.length(), ip, 3000);
                ds.send(dp);  
                ds.close();
                Thread.currentThread().interrupt();
                
            } catch (SocketException | UnknownHostException | FileNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}