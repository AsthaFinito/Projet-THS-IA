import java.util.Arrays;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import FFT.*;
import Neurone.*;

import javax.sound.sampled.AudioFormat;



public class DetecteurDeSon {
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println("Lecture du fichier WAV " + args[0]);
            Son son = lireFichierWAV(args[0]);
            System.out.println("Fichier " + args[0] + " : " + son.donnees().length + " échantillons à " + son.frequence() + "Hz");
           
            appliquerFFT(son);
            final float[][] entrees = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
            final float[] resultats = {0, 0, 0, 1};
            System.out.println("Initialisation des tableaux");
            final iNeurone n = new NeuroneHeaviside(entrees[0].length);
        } else {
            System.out.println("Veuillez donner le nom d'un fichier WAV en paramètre SVP.");
        }
    }

    private static Son lireFichierWAV(String fichier) {
        return new Son(fichier);
    }

    private static void appliquerFFT(Son son) {
        // Lecture d'un bloc de données
        float[] bloc = son.bloc_deTaille(0, 512);

        // Conversion du bloc en nombres complexes
        Complexe[] signalComplexe = new Complexe[bloc.length];
        for (int i = 0; i < bloc.length; i++) {
            signalComplexe[i] = new ComplexeCartesien(bloc[i], 0);
        }

        // Application de la FFT sur le bloc de données
        Complexe[] resultat = FFTCplx.appliqueSur(signalComplexe);

        // Analyse des résultats de la FFT
        System.out.println("Résultats de la FFT :");
        for (int i = 0; i < resultat.length; i++) {
            System.out.print(i + " : (" + (float) resultat[i].reel() + " ; " + (float) resultat[i].imag() + "i)");
            System.out.println(", (" + (float) resultat[i].mod() + " ; " + (float) resultat[i].arg() + " rad)");
        }
    }
}

class Son {
    private int frequence;
    private float[] donnees;

    public int frequence() { return frequence; }
    public float[] donnees() { return donnees; }
    public float[] bloc_deTaille(final int numeroBloc, final int tailleBloc) {
        final int from = numeroBloc * tailleBloc;
        final int to = from + tailleBloc;
        return Arrays.copyOfRange(donnees, from, to);
    }

    public Son(final String nomFichier) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(nomFichier));
            AudioFormat af = ais.getFormat();

            if (af.getChannels() == 1 && 
                af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && 
                af.getSampleSizeInBits() == 16) {
                final int NombreDonnees = ais.available();
                final byte[] bufferOctets = new byte[NombreDonnees];
                ais.read(bufferOctets);
                ais.close();
                ByteBuffer bb = ByteBuffer.wrap(bufferOctets);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                ShortBuffer donneesAudio = bb.asShortBuffer();
                donnees = new float[donneesAudio.capacity()];
                for (int i = 0; i < donnees.length; ++i)
                    donnees[i] = (float) donneesAudio.get(i);
                frequence = (int) af.getSampleRate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


