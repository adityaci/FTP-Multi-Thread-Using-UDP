package ftpudp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Aditya.C.I
 */
public class Server {
    
    private DefaultTableModel dtm;
    
    public static void main(String[] args) {
        Server s = new Server();
        s.init();
    }
    
    public void init(){
        String column[] = {"File Name", "Status"};
        JFrame window = new JFrame("FTP Server");
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        
        JTable tableData = new JTable();
        dtm = new DefaultTableModel(0, 0);
        dtm.setColumnIdentifiers( column );
        tableData.setModel( dtm );
        tableData.setEnabled( false );
        tableData.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
        tableData.getColumn("Status").setMaxWidth(70);
        
        JScrollPane scrollPane = new JScrollPane( tableData );
        scrollPane.setBounds( 10, 10, 375, 370 );
        
        window.setSize( 400, 420 );
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(null);
        window.add( scrollPane );
        window.setVisible( true );
        
        StartListen sl = new StartListen();
        sl.start();
    }
    
    public class ReceiveThread extends Thread{

        private String dataString;
        private final DatagramSocket ds;
        private final int dataSize;
        
        public ReceiveThread( DatagramSocket socket, int dataSize ){
            this.ds = socket;
            this.dataSize = dataSize;
        }
        
        public void prosesTerima(){
            try {
                byte[] data = new byte[ dataSize ];
                DatagramPacket packet;
                packet = new DatagramPacket( data, dataSize );
                ds.receive( packet );

                dataString = new String( packet.getData(), 0, packet.getLength() );
                String namaFile = dataString.split(";")[0];
                FileOutputStream fos = new FileOutputStream("D:\\TargetFTP\\" + namaFile );
                byte[] dataFileByte = Base64.getDecoder().decode(dataString.split(";")[1]);
                fos.write( dataFileByte );    
                fos.close();
                dtm.addRow(new Object[] { namaFile, "Diterima" });
                
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public class StartListen extends Thread{
        private DatagramSocket ds;
        private boolean status = true;
        
        public void run(){
            try {
                this.ds = new DatagramSocket(3000);
            } catch (SocketException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            while( status ){
                try {
                    byte[] firstReq = new byte[ 5 ];
                    DatagramPacket packet;
                    packet = new DatagramPacket(firstReq, 5);
                    ds.receive(packet);
                    
                    ReceiveThread rt = new ReceiveThread( ds, Integer.parseInt(new String( packet.getData(), 0, packet.getLength() )) );
                    rt.prosesTerima();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}