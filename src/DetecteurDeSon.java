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
    private static iNeurone[] neurones;
    private static int TailleCalcul=512;
    public static void main(String[] args) {

        if (args.length == 1) {
            // Initialiser les fichiers et les neurones
            String[][] fichiers = initFichiers();
            initNeurones(fichiers.length);

            // Entraîner les neurones

            float[][] resultats = initResultats();
            initialiserNeurones(fichiers.length, 2);
            entrainerNeurones(fichiers);
            System.out.println("Fin apprentissage");
            //lireSynapseEtBiais();
            
            //prediction(args[0]);

            // final float[][] entrees = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
            // final float[] resultats = {0, 0, 0, 1};
            // final iNeurone n = new NeuroneHeaviside(entrees[0].length);
            // System.out.println("Nombre de tours : "+n.apprentissage(entrees, resultats));
            
        } else {
            System.out.println("Veuillez donner le nom d'un fichier WAV en paramètre SVP.");
        }
    }
    private static void initialiserNeurones(int nombreDeNeurones, int tailleDesEntrees) {
        neurones = new iNeurone[nombreDeNeurones];
        for (int i = 0; i < nombreDeNeurones; i++) {
            neurones[i] = new NeuroneHeaviside(tailleDesEntrees);
        }
    }
    private static float[] genererTableau(int taille, int indice) {
        float[] tableau = new float[taille];
        for (int i = 0; i < taille; i++) {
            tableau[i] = (i == indice) ? 1 : 0;
        }
        return tableau;
    }
    private static String[][] initFichiers() {
        return new String[][] {
            {"./Sources sonores/Carre.wav"},
            {"./Sources sonores/Sinusoide.wav"},
            {"./Sources sonores/Bruit.wav"},
            {"./Sources sonores/Sinusoide3Harmoniques.wav"},
            {"./Sources sonores/Sinusoide2.wav"},
            {"./Sources sonores/Combinaison.wav"}
        };
    }
    private static void lireSynapseEtBiais() {
        for (int i = 0; i < neurones.length; i++) {
            if (neurones[i] instanceof Neurone) {
                Neurone neurone = (Neurone) neurones[i];
                System.out.print("Neurone " + i + " - Synapses : ");
                for (float f : neurone.synapses())
                    System.out.print(f + " ");
                System.out.print("\nBiais : ");
                System.out.println(neurone.biais());
            }
        }
    }
    private static void initNeurones(int length) {
        neurones = new iNeurone[length];
    }
    private static float[][] initResultats() {
        return new float[][] {
            {1}, // Carre
            {0}, // Sinusoide
            {0}, // Bruit
            {0}, // Sinusoide3Harmoniques
            {0}, // Sinusoide2
            {0}  // Combinaison
        };
    }
    private static void entrainerNeurones(String[][] fichiers) {
        // Pour chaque neurone
        for (int i = 0; i < neurones.length; i++) {
            System.out.println("Neurone numéro :"+i);
            float[][] entrees = new float[TailleCalcul/2][];
            float[] resultats = new float[TailleCalcul/2];
 
            // Pour chaque fichier, calculer les caractéristiques et apprendre
            for (int j = 0; j < fichiers.length; j++) {
                if(j==i){
                    resultats=genererTableau(TailleCalcul/2, 0);
                }
                else{
                    resultats=genererTableau(TailleCalcul/2, -1);
                }
                Son son = lireFichierWAV(fichiers[j][0]); // On lit le son
                Complexe[] RecupFFT = appliquerFFT(son); // On récupère les données FFT
    
                
    
                entrees = extraireCaracteristiques(RecupFFT);
                
                // // //System.out.println("Données d'entrée pour le neurone " + i + " :");
                // for (int x = 0; x < TailleCalcul/2; x++) {
                //      //System.out.println("Entrée " + x + " : " + Arrays.toString(entrees[x]) + ", Résultat attendu : " + resultats[x]);
                //  }
                
                System.out.println("Nombre de tours : "+neurones[i].apprentissage(entrees, resultats)+"pour le signal "+j);
                
            }
           
            
    
           break;
           // lireSynapseEtBiais();
        }
    }
    
    private static Son lireFichierWAV(String fichier) {
        return new Son(fichier);
    }
    
    private static Complexe[] appliquerFFT(Son son) {
        // Lecture d'un bloc de données
        float[] bloc = son.bloc_deTaille(0, TailleCalcul);

        // Conversion du bloc en nombres complexes
        Complexe[] signalComplexe = new Complexe[bloc.length];
        for (int i = 0; i < bloc.length; i++) {
            signalComplexe[i] = new ComplexeCartesien(bloc[i], 0);
        }

        // Application de la FFT sur le bloc de données
        Complexe[] resultat = FFTCplx.appliqueSur(signalComplexe);

        // Analyse des résultats de la FFT
        //System.out.println("Résultats de la FFT :");
        for (int i = 0; i < resultat.length; i++) {
            //System.out.print(i + " : (" + (float) resultat[i].reel() + " ; " + (float) resultat[i].imag() + "i)");
            //System.out.println(", (" + (float) resultat[i].mod() + " ; " + (float) resultat[i].arg() + " rad)");
        }
       // System.out.println("FFT FINI");
        return resultat;
    }
    public static float[][] extraireCaracteristiques(Complexe[] fftResult) {
        int n = fftResult.length / 2; // On prend seulement la moitié du spectre FFT, car l'autre moitié est symétrique
        float[][] features = new float[n][2];
        for (int i = 0; i < n; i++) {
            features[i][0] = (float) fftResult[i].mod(); // Amplitude
            features[i][1] = (float) fftResult[i].arg(); // Phase
        }
        return features;
    }
//     private static void prediction(String fichier) {
//         Son son = lireFichierWAV(fichier);
//         Complexe[] RecupFFT = appliquerFFT(son);
//         float[]features = extraireCaracteristiques(RecupFFT);

//         // Afficher les valeurs des caractéristiques extraites pour le fichier de test
//         System.out.println("Caractéristiques extraites pour le fichier de test : ");
        

//         // Utiliser chaque neurone pour faire une prédiction
//         float maxPrediction = -Float.MAX_VALUE;
//         int bestNeuroneIndex = -1;
//         for (int i = 0; i < neurones.length; i++) {
//             neurones[i].metAJour(features);
//             float prediction = neurones[i].sortie();
//             System.out.println("Prédiction du neurone " + i + " : " + prediction);
//             if (prediction > maxPrediction) {
//                 maxPrediction = prediction;
//                 bestNeuroneIndex = i;
//             }
//         }

//         // Afficher la prédiction finale
//         String[] typesDeSignal = {"Carré", "Sinusoïde", "Bruit", "Sinusoïde 3 Harmoniques", "Sinusoïde 2", "Combinaison"};
//         System.out.println("Le fichier " + fichier + " est prédit comme étant de type : " + typesDeSignal[bestNeuroneIndex]);
    
// }
    
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


