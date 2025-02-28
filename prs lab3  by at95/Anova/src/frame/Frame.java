package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableCellRenderer;

import myTableModel.MyTableModel;
import main.Main;

public class Frame extends JFrame implements ActionListener {
    JTable table;
    JButton izracunaj, SSAbutton, SSEbutton, SAbutton, SEbutton, Fbutton, FtabelarnoButton, FzakljucakButton, FzakljucakButton2;
    JTextPane pane;
    int alternatives, measurements;
    float ukupnaSrednjaVrijednost=0, SSE, SSA;
    float srednjaVrijednostPoKolonoma[], slucajneGreske[][], efekatAlternative[];

    public Frame() {
        super("ANOVA");
        setLayout(new GridLayout(3,0));
        loadMenu();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void loadMenu() {
        boolean validInput = false;

        while (!validInput) {
            try {
                alternatives = Integer.parseInt(JOptionPane.showInputDialog("Enter number of alternatives:"));
                if (alternatives <= 0) {
                    throw new IllegalArgumentException("Number of alternatives must be greater than zero.");
                }

                measurements = Integer.parseInt(JOptionPane.showInputDialog("Enter number of measurements:"));
                if (measurements <= 0) {
                    throw new IllegalArgumentException("Number of measurements must be greater than zero.");
                }

                validInput = true; // Postavljamo validInput na true ako su unosi ispravni
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                // Ponovni pokušaj unosa ili druga reakcija na grešku...
            }
        }

        table=new JTable(new MyTableModel(measurements+3,alternatives+2));
        table.setBorder(BorderFactory.createLineBorder(Color.black,3));
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setBackground(Color.DARK_GRAY);
        table.getTableHeader().setForeground(Color.green);
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setBackground(Color.DARK_GRAY);
        render.setForeground(Color.green);
        table.getColumnModel().getColumn(0).setCellRenderer(render);
        table.getColumnModel().getColumn(0).setPreferredWidth(table.getValueAt(measurements+1, 0).toString().length()*7);
        table.getColumnModel().getColumn(alternatives+1).setPreferredWidth(table.getValueAt(0,alternatives+1).toString().length()*6);
        table.getColumnModel().getColumn(alternatives+1).setCellRenderer(render);
        add(table);

        JPanel fTestPanel=new JPanel(new GridLayout(4,2));
        SSAbutton=new JButton("SSA (n*suma(Aj)2=");
        SSEbutton=new JButton("SSE (suma(Eij)2=");
        SAbutton=new JButton("kvadratna Sa (SSA/(alternative-1) =");
        SEbutton=new JButton("kvadratna Se (SSE/(alternative(mjerenja-1)) =");
        Fbutton= new JButton("F izracunato = ");
        FtabelarnoButton=new JButton("F tabelarno = ");
        FzakljucakButton=new JButton("Sa nivoom povjerenja od 95% moze se zakljuciti da postoji statisticki znacajna razlika izmedju sistema: ");
        FzakljucakButton2=new JButton();
        fTestPanel.add(SSAbutton);	fTestPanel.add(SSEbutton);
        fTestPanel.add(SAbutton); fTestPanel.add(SEbutton);
        fTestPanel.add(Fbutton); fTestPanel.add(FtabelarnoButton);
        fTestPanel.add(FzakljucakButton); fTestPanel.add(FzakljucakButton2);
        add(fTestPanel);

        JPanel contrastPanel=new JPanel(new BorderLayout());
        JScrollPane scroll=new JScrollPane();
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pane=new JTextPane();
        pane.setBackground(Color.DARK_GRAY);
        pane.setForeground(Color.GREEN);
        scroll.setViewportView(pane);
        contrastPanel.add(scroll, BorderLayout.CENTER);
        izracunaj=new JButton("SPROVEDI ANOVA TEST");
        izracunaj.setBorder(BorderFactory.createLineBorder(Color.black,3));
        izracunaj.addActionListener(this);
        contrastPanel.add(izracunaj, BorderLayout.SOUTH);
        add(contrastPanel);

        double[] vrijednosti1= {0.0972,0.0971, 0.0969, 0.1954,0.0974};
        double[] vrijednosti2= {0.1382,0.1432, 0.1382, 0.1730,0.1383};
        double[] vrijednosti3= {0.7966,0.53, 0.5152, 0.6675,0.5298};
        for(int i=0;i<vrijednosti1.length;i++) {
            table.setValueAt(String.valueOf(vrijednosti1[i]), i+1, 1);
            table.setValueAt(String.valueOf(vrijednosti2[i]), i+1, 2);
            table.setValueAt(String.valueOf(vrijednosti3[i]), i+1, 3);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        izracunajUkupnuSrednjuVrijednost();
        izracunajProsjekAlternativa();
        izracunajSlucajneGreske();
        izracunajEfekteAlternativa();
        izracunajSSE();
        izracunajSSA();
        fTest();
    }

    private void izracunajUkupnuSrednjuVrijednost() {
        // Prvo saberemo sva mjerenja
        for(int i=0;i<measurements;i++)
            for(int j=0;j<alternatives;j++)
                ukupnaSrednjaVrijednost+=Float.parseFloat(table.getValueAt(i+1, j+1).toString());
        // Onda tu sumu podijelimo sa ukupnim brojem mjerenja
        ukupnaSrednjaVrijednost=ukupnaSrednjaVrijednost/(measurements*alternatives);
        table.setValueAt(String.valueOf(String.format("%.4f",ukupnaSrednjaVrijednost)), measurements+1, alternatives+1);
    }

    private void izracunajProsjekAlternativa() {
        srednjaVrijednostPoKolonoma =new float[alternatives];

        for(int j=0;j<alternatives;j++) {
            float sumaKolone=0;
            int i=0;
            // prvo saberemo sva mjerenja u j-toj koloni
            for(;i<measurements;i++)
                sumaKolone+=Float.parseFloat((String) table.getValueAt(i+1, j+1));
            // a onda dobijenu sumu podijelimo sa brojem mjerenja
            table.setValueAt(String.valueOf(String.format("%.4f",sumaKolone/measurements)), i+1,j+1);
            srednjaVrijednostPoKolonoma[j]=sumaKolone/measurements;
        }
    }

    private void izracunajSlucajneGreske() {
        slucajneGreske =new float[measurements] [alternatives];
        // Racunamo eij po kolonama, dobijemo kad od svakog mjerenja u j-toj koloni oduzmemo prosjek te kolone
        for(int j=0;j<alternatives;j++) {
            for(int i=0;i<measurements;i++)
                slucajneGreske[i] [j]=Float.parseFloat((String) table.getValueAt(i+1, j+1)) - srednjaVrijednostPoKolonoma[j];
        }
    }

    private void izracunajEfekteAlternativa() {
        efekatAlternative =new float[alternatives];
        // Efekat alternative aj dobijemo kad od srednje vrijednosti mjerenja te alternative oduzmemo srednje vrijednosti svih mjerenja
        for(int i=0;i<alternatives;i++) {
            efekatAlternative[i]=srednjaVrijednostPoKolonoma[i] - ukupnaSrednjaVrijednost;
            table.setValueAt(String.valueOf(String.format("%.4f",efekatAlternative[i])), measurements+2, i+1);
        }
    }

    private void izracunajSSE() {
        //SSE predstavlja varijaciju usljed gresaka u mjerenju. Racuna se kao suma kvadrata eij
        SSE=0;
        for(int i=0;i<measurements;i++)
            for(int j=0;j<alternatives;j++)
                SSE+=slucajneGreske[i][j] * slucajneGreske[i][j];
        SSEbutton.setText("SSE (suma(Eij)2= "+String.format("%.4f",SSE));
    }

    private void izracunajSSA() {
        //SSA predstavlja varijaciju usljed efekata alternativa aj. Racuna se kao suma kvadrata aj, pa dobijenu sumu pomnozimo brojem svih mjerenja
        SSA=0;
        for(int i=0;i<alternatives;i++)
            SSA+= efekatAlternative[i]*efekatAlternative[i];
        SSA*=measurements;
        SSAbutton.setText("SSA (n*suma(Aj)2= "+String.format("%.4f",SSA));
    }

    private void fTest() {
        // Ako su razlike izmedju alternativa posljedica pravih razlika a ne gresaka u mjerenjima, realno je ocekivati da SSA bude statisticki znacajno vece od SSE.
        // F-test se zasniva na poredjenju varijanse SSA i varijanse SSE, ako je dobijena vrijednost bliska 1 znaci da ne postoji znacajna razlika.
        // Varijansa SSA se dobija dijeljenjem SSA sa brojem stepeni njene slobode
        float kvadratnaVarijansaSSA = SSA/(alternatives-1);
        SAbutton.setText("kvadratna Sa (SSA/(alternative-1) = "+String.format("%.4f",kvadratnaVarijansaSSA));
        // Varijansa SSE se dobija kad SSE podijelimo sa brojem stepeni njene slobode: na svakoj alternativi se vrsi n mjerenja
        float kvadratnaVarijansaSSE = SSE/(alternatives*(measurements-1));
        SEbutton.setText("kvadratna Se (SSE/alt.*(mj. - 1) = "+String.format("%.4f",kvadratnaVarijansaSSE));
        System.out.println("varijansa sse: "+kvadratnaVarijansaSSE);
        System.out.println("varijansa ssa: "+kvadratnaVarijansaSSA);

        float SST = SSA+SSE;
        System.out.println("Varijacija izmedju sistema zbog stvarnih razlika: "+String.format("%.4f",(SSA/SST)*100));
        System.out.println("Varijacija izmedju sistema zbog gresaka u mjerenju: "+String.format("%.4f",(SSE/SST)*100));

        double F=kvadratnaVarijansaSSA/kvadratnaVarijansaSSE;
        Fbutton.setText("F izracunato = "+String.format("%.2f",F));
        // Ako je izracunata vrijednost veca od tabelarne, onda se moze reci da je razlika izmedju alternativa posljedica pravih razlika izmedju sistema.
        double Ftable= Main.loadFTable(alternatives-1, alternatives*(measurements-1));
        FtabelarnoButton.setText("F tabelarno = "+String.format("%.2f",Ftable));
        if (F>Ftable) {
            FzakljucakButton2.setText("DA");
            kontrastTest(kvadratnaVarijansaSSE);
        }
        else {
            FzakljucakButton2.setText("NE");
            pane.setText("\n\tPosto F test nije dao znacajne razlike izmedju sistema, nema svrhe sprovoditi kontrast test");
        }
    }

    private void kontrastTest(float varijansaSSE) {
        String tekst="";
        // Sc je standardna devijacija za sve kontraste potrebna za racunanje intervala povjerenja. Racuna se pomocu formule Se(varijansaSSE)*kv.korijen iz 2/k*n
        double Sc=Math.sqrt(varijansaSSE)*Math.sqrt(2.0/(measurements*alternatives));
        double tTable=Main.loadTTable(alternatives*(measurements-1));
        tekst+="\t...........sprovodim kontrast test.............\n\t\tSumarna devijacija Sc = "+String.format("%.4f",Sc)+";	Za nivo povjerenja od 90% T je "+tTable+"\n";
        tekst+="============================================================================================================================================\n";
        for (int i=0;i<efekatAlternative.length-1;i++)
            for(int j=i+1;j<efekatAlternative.length;j++) {
                double c=efekatAlternative[i]*1 + efekatAlternative[j]*(-1);
                tekst+="\t\t\t\tRezultati kontrast testa izmedju sistema "+(i+1)+" i "+(j+1)+"\n"+"Kontrast = "+String.format("%.4f",c);
                // formula za nivo povjerenja [c +- tTable*Sc
                double c1= c-tTable*Sc;
                double c2= c+tTable*Sc;
                tekst+="\tInterval povjerenja: ["+String.format("%.4f",c1)+", "+String.format("%.4f",+c2)+"]";
                if(0>=c1 && 0<=c2)
                    tekst+="\tInterval povjerenja ukljucuje nulu pa se zakljucuje da ne postoji statisticki znacajna razlika izmedju sistema "+(i+1)+" i "+(j+1)+"\n";
                else
                    tekst+="\tInterval povjerenja ne ukljucuje 0, pa se moze zakljuciti da je ispoljena razlika izmedju alternativa "
                            + "posljedica razlika izmedju sistema "+(i+1)+" i "+(j+1)+"\n";
                tekst+="============================================================================================================================================\n";
            }

        pane.setText(tekst);
    }
}
