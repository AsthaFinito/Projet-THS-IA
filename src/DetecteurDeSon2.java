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

public class DetecteurDeSon2 {
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println("Lecture du fichier WAV " + args[0]);
            Son son = lireFichierWAV(args[0]);
            System.out.println("Fichier " + args[0] + " : " + son.donnees().length + " échantillons à " + son.frequence() + "Hz");

            // Définir les paramètres d'apprentissage
            int tailleBloc = 512;
            double seuilAmplitude = 10.0; // Seuil pour différencier bruit et signal

            // Préparer les données d'apprentissage
            float[][] donnees = extraireDonnees(son, tailleBloc);
            float[] etiquettes = etiqueterDonnees(donnees, seuilAmplitude);

            // Créer et entraîner le neurone
            NeuroneHeaviside neurone = new NeuroneHeaviside(tailleBloc);
            int echecs = neurone.apprentissage(donnees, etiquettes);
            System.out.println("Entraînement terminé avec " + echecs + " échecs.");

            // Test de classification avec le neurone
            float[] blocTest = son.bloc_deTaille(0, tailleBloc);
            Complexe[] signalComplexe = new Complexe[tailleBloc];
            for (int i = 0; i < tailleBloc; i++) {
                signalComplexe[i] = new ComplexeCartesien(blocTest[i], 0);
            }
            Complexe[] resultatFFT = FFTCplx.appliqueSur(signalComplexe);
            float[] amplitudesTest = new float[tailleBloc];
            for (int i = 0; i < resultatFFT.length; i++) {
                amplitudesTest[i] = (float) resultatFFT[i].mod();
            }
            neurone.metAJour(amplitudesTest);
            float sortie = neurone.sortie();
            System.out.println("Classification du test: " + (sortie == 1.0 ? "Signal" : "Bruit"));

        } else {
            System.out.println("Veuillez donner le nom d'un fichier WAV en paramètre SVP.");
        }
    }

    private static Son lireFichierWAV(String fichier) {
        return new Son(fichier);
    }

    private static float[][] extraireDonnees(Son son, int tailleBloc) {
        int nombreBlocs = son.donnees().length / tailleBloc;
        float[][] donnees = new float[nombreBlocs][];

        for (int i = 0; i < nombreBlocs; i++) {
            float[] bloc = son.bloc_deTaille(i, tailleBloc);
            Complexe[] signalComplexe = new Complexe[tailleBloc];

            for (int j = 0; j < tailleBloc; j++) {
                signalComplexe[j] = new ComplexeCartesien(bloc[j], 0);
            }

            Complexe[] resultatFFT = FFTCplx.appliqueSur(signalComplexe);
            float[] amplitudes = new float[tailleBloc];

            for (int j = 0; j < resultatFFT.length; j++) {
                amplitudes[j] = (float) resultatFFT[j].mod();
            }

            donnees[i] = amplitudes;
        }
        return donnees;
    }

    private static float[] etiqueterDonnees(float[][] donnees, double seuilAmplitude) {
        float[] etiquettes = new float[donnees.length];

        for (int i = 0; i < donnees.length; i++) {
            etiquettes[i] = estUnSignal(donnees[i], seuilAmplitude) ? 1.0f : 0.0f;
        }

        return etiquettes;
    }

    private static boolean estUnSignal(float[] amplitudes, double seuilAmplitude) {
        for (float amplitude : amplitudes) {
            if (amplitude > seuilAmplitude) {
                return true;
            }
        }
        return false;
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
