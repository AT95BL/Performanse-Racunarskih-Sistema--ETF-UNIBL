package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import frame.Frame;

public class Main {

    public static void main(String[] args) {

        Frame f=new Frame();
    }

    public static double loadFTable(int d1, int d2) {
        try {
            List<String> lines= Files.readAllLines(Paths.get("src/main/fDistribution_95.txt"));
            // kljucevi su broj stepeni slobode, a vrijednosti su redovi/kolone F tabele
            HashMap<Integer, Integer> redovi=new HashMap<>();
            for(int i=1;i<31;i++)
                redovi.put(i, i);
            redovi.put(40, 31);
            redovi.put(60, 32);
            redovi.put(120,33);

            HashMap<Integer, Integer> kolone=new HashMap<>();
            for(int i=1;i<11;i++)
                kolone.put(i, i);
            kolone.put(12, 11);
            kolone.put(15, 12);
            kolone.put(20, 13);
            kolone.put(24, 14);
            kolone.put(30, 15);
            kolone.put(40, 16);
            kolone.put(60, 17);
            kolone.put(120, 18);

            int kolona=kolone.getOrDefault(d1, -1);
            int prethodnaKolona=-1, iducaKolona=-1;
            if(d1>120)
                kolona=kolone.get(120);

            if(kolona==-1) {
                int i=1;
                while (prethodnaKolona==-1) {
                    prethodnaKolona=kolone.getOrDefault(d1-i,  -1);
                    i++;
                }
                i=1;
                while (iducaKolona==-1) {
                    iducaKolona=kolone.getOrDefault(d1+i,  -1);
                    i++;
                }
            }

            int red = redovi.get(d2);
            if(d2>120)
                red=redovi.get(120);

            double fTable;
            if(kolona!=-1)
                fTable= dohvatiIzTabele(lines, red, kolona);
            else
                fTable= dohvatiIzTabele(lines, red, prethodnaKolona, iducaKolona);

            return fTable;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double dohvatiIzTabele(List<String> lines, int red, int kolona) {
        //System.out.println("red je "+red+" a kolona "+kolona);
        String s = lines.get(red-1);
        String s2 = s.split("\\s")[kolona-1];
        //System.out.println(s2);
        return Double.parseDouble(s2);
    }

    public static double dohvatiIzTabele(List<String> lines, int red, int prethodnaKolona, int iducaKolona) {
        //System.out.println("red je "+red+" a kolone "+prethodnaKolona+" "+iducaKolona);
        String s = lines.get(red-1);
        double max= Double.parseDouble(s.split("\\s")[prethodnaKolona-1]);
        double min= Double.parseDouble(s.split("\\s")[iducaKolona-1]);
//		System.out.println(max +" "+min);
//		System.out.println((max+min)/2);
        return (max+min)/2;
    }

    public static double loadTTable(int brojStepeniSlobode) {
        // Koristim t raspodjelu sa 90% povjerenja, a to je druga kolona u fajlu
        int kolona=2;
        try {
            List<String> lines= Files.readAllLines(Paths.get("src/main/tDistribution.txt"));
            // kljucevi su broj stepeni slobode, a vrijednosti su redovi/kolone F tabele
            HashMap<Integer, Integer> redovi=new HashMap<>();
            for(int i=1;i<31;i++)
                redovi.put(i, i);
            redovi.put(60, 31);
            redovi.put(120, 32);

            int red=redovi.getOrDefault(brojStepeniSlobode,-1);
            int prethodniRed=-1, iduciRed=-1;
            if(red>120)
                red=redovi.get(120);

            if(red==-1) {
                int i=1;
                while (prethodniRed==-1) {
                    prethodniRed=redovi.getOrDefault(brojStepeniSlobode-i,  -1);
                    i++;
                }
                i=1;
                while (iduciRed==-1) {
                    iduciRed=redovi.getOrDefault(brojStepeniSlobode+i,  -1);
                    i++;
                }
            }

            if(red!=-1)
                return dohvatiIzTabele(lines, red, kolona);
            else
                return dohvatiIzTabele(lines, red, prethodniRed, iduciRed);
        }
        catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
