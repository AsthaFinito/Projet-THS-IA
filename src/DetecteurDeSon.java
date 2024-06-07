import java.util.Arrays;
import java.util.Random;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import FFT.*;
import Neurone.*;
import Son.*;

import javax.sound.sampled.AudioFormat;



public class DetecteurDeSon {
    private static iNeurone[] neurones;
    private static int TailleCalcul=512; //Taille de la FFT
    public static void main(String[] args) {

        if (args.length == 1) {
            // Initialiser les fichiers et les neurones
            String[][] fichiers = initFichiers();
            initNeurones(fichiers.length);
            initialiserNeurones(fichiers.length, TailleCalcul);
            // Entraîner les neurones
          
            entrainerNeurones(fichiers);
            System.out.println("Fin apprentissage");
            
            //lireSynapseEtBiais(); //Voir l'état des poids et du biais
            
            prediction(args[0]);

            //calculerSortieNeurones()
            
        } else {
            System.out.println("Veuillez donner le nom d'un fichier WAV en paramètre SVP.");
        }
    }

    public float[][] lireTousLesBlocs(final int tailleBloc,float[] donnees) {
        int nombreTotalBlocs = donnees.length / tailleBloc; // Calculer le nombre total de blocs
        float[][] tousLesBlocs = new float[nombreTotalBlocs][tailleBloc]; // Initialiser un tableau pour stocker tous les blocs
    
        // Parcourir tous les blocs
        for (int i = 0; i < nombreTotalBlocs; i++) {
            tousLesBlocs[i] = bloc_deTaille(i, tailleBloc,donnees); // Lire chaque bloc et l'ajouter au tableau
        }
    
        return tousLesBlocs; // Retourner tous les blocs
    }
    
   

    //Initialisation des neurones avant entrainement
    private static void initialiserNeurones(int nombreDeNeurones, int tailleDesEntrees) {
        neurones = new iNeurone[nombreDeNeurones];
        for (int i = 0; i < nombreDeNeurones; i++) {
            neurones[i] = new NeuroneSigmoid(tailleDesEntrees);
        }
    }
    //Genere un tableau de 1
    private static float[] genererTableau1(int taille) {
        float[] tableau = new float[taille];
        for (int i = 0; i < taille; i++) {
            tableau[i] =1;
        }
        return tableau;
    }
    //genere un tableau de 0
    private static float[] genererTableau0(int taille) {
        float[] tableau = new float[taille];
        for (int i = 0; i < taille; i++) {
            tableau[i] =0;
        }
        return tableau;
    }
    //Initialisation des fichiers d'entrainements
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
    //Resume les poids et le biais
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
    //Initialisation du tableau de neurone
    private static void initNeurones(int length) {
        neurones = new iNeurone[length];
    }

    private static void entrainerNeurones(String[][] fichiers) {
        
        // Pour chaque neurone
        for (int i = 0; i < neurones.length; i++) {
            System.out.println("Entraînement du neurone numéro : " + i);
            float[][] entrees;
            float[] resultats;
    
            // On sélectionne le fichier correspondant à l'indice du neurone pour l'entraînement
            int fichierIndex = i % fichiers.length; // Utilisation du modulo pour répéter les fichiers si nécessaire
            resultats = genererTableau1(TailleCalcul); // Résultats attendus pour le neurone en cours d'entraînement
            Son son = lireFichierWAV(fichiers[fichierIndex][0]); // On lit le son du fichier correspondant
            Complexe[] RecupFFT = appliquerFFT(son); // On récupère les données FFT

            // System.out.println("Sortie de la FFT pour le neurone " + i + " :");
            // for (int x = 0; x < RecupFFT.length; x++) {
            //     System.out.println("Amplitude " + x + " : " + (float) RecupFFT[x].mod());
            // }
            entrees = extraireCaracteristiques(RecupFFT); // On récupère les caractéristiques
            
            // Normaliser les données
            entrees = normaliserDonnees(entrees);
            //System.out.println("Taille entree : "+entrees[0].length+" Taille resultat : "+resultats.length);
            // // Afficher les données d'entrée pour vérification
            // System.out.println("Données d'entrée pour le neurone " + i + " :");
        
            //System.out.println("Entrée "  + " : " + Arrays.toString(entrees[0]) + ", Résultat attendu : " + resultats[0]);
            
    
            // // Entraîner le neurone avec les données
            System.out.println("Nombre de tours : " + neurones[i].apprentissage(entrees, resultats) + " pour le signal " + fichierIndex);
        }
    }
    //Retourne une classe son pour lire wav
    private static Son lireFichierWAV(String fichier) {
        return new Son(fichier);
    }
    //Faire la fft
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
    //Extrait seulement le module et la phase de nos données pour anaylse neurone
    public static float[][] extraireCaracteristiques(Complexe[] fftResult) {
        int n = fftResult.length ; // On prend seulement la moitié du spectre FFT, car l'autre moitié est symétrique
        float[][] features = new float[1][n]; // Initialisation du tableau de tableaux
    
        // Extraction des amplitudes de la FFT
        for (int i = 0; i < n; i++) {
            features[0][i] = (float) fftResult[i].mod(); // Amplitude
        }
    
        return features;
    }
    //Duplicate fonction pour la prédiction (pour l'instant useless)
    public static float[] extraireCaracteristiquesPrediction(Complexe[] fftResult) {
        int n = fftResult.length; // Nombre de valeurs dans le résultat de la FFT
        float[] amplitudes = new float[n]; // Initialisation du tableau d'amplitudes
    
        // Extraction des amplitudes
        for (int i = 0; i < n; i++) {
            amplitudes[i] = (float) fftResult[i].mod(); // Amplitude
        }
    
        return amplitudes;
    }
    
    private static float[] calculerSortieNeurones(float[] entree) {
        float[] sorties = new float[neurones.length]; // Initialisation du tableau de sorties des neurones
    
        // Pour chaque neurone
        for (int i = 0; i < neurones.length; i++) {
            // Calculer la sortie du neurone pour l'entrée donnée
            neurones[i].metAJour(entree);
            sorties[i] = neurones[i].sortie();
        }
    
        return sorties; // Retourner les sorties des neurones
    }
    
    //Fonction useless pour l'instant 
    private static void prediction(String fichier) {
        Son son = lireFichierWAV(fichier);
        Complexe[] RecupFFT = appliquerFFT(son);
        float[] features = extraireCaracteristiquesPrediction(RecupFFT);

        float maxPrediction = -Float.MAX_VALUE;
        int bestNeuroneIndex = -1;
        for (int i = 0; i < neurones.length; i++) {
            neurones[i].metAJour(features);
            float prediction = neurones[i].sortie();
            System.out.println("Prédiction du neurone " + i + " : " + prediction);
            if (prediction > maxPrediction) {
                maxPrediction = prediction;
                bestNeuroneIndex = i;
            }
        }

        // Afficher la prédiction finale
        String[] typesDeSignal = {"Carré", "Sinusoïde", "Bruit", "Sinusoïde 3 Harmoniques", "Sinusoïde 2", "Combinaison"};
        System.out.println("Le fichier " + fichier + " est prédit comme étant de type : " + typesDeSignal[bestNeuroneIndex]);

// }
    }
    
    
    //On normalise les données pour facilier l'entrainement
    // On normalise seulement les amplitudes des données pour faciliter l'entrainement
// On normalise seulement les amplitudes des données pour faciliter l'entrainement
private static float[][] normaliserDonnees(float[][] donnees) {
    int n = donnees.length; // Nombre de lignes (nombre de vecteurs)
    int m = donnees[0].length; // Nombre de colonnes (nombre de caractéristiques par vecteur)
    float[][] normalisees = new float[n][m];

    for (int i = 0; i < n; i++) {
        // Trouver le min et le max de l'amplitude uniquement
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int j = 0; j < m; j++) {
            if (donnees[i][j] < min) min = donnees[i][j];
            if (donnees[i][j] > max) max = donnees[i][j];
        }

        float range = max - min;

        // Vérifier si le range est non nul
        if (range != 0) {
            // Normaliser les amplitudes seulement
            for (int j = 0; j < m; j++) {
                normalisees[i][j] = (donnees[i][j] - min) / range;
            }
        } else {
            // Si le range est nul, réinitialiser toutes les amplitudes à zéro
            for (int j = 0; j < m; j++) {
                normalisees[i][j] = 0;
            }
        }
    }
    return normalisees;
}
public float[] bloc_deTaille(final int numeroBloc, final int tailleBloc,float[] donnees) {
    final int from = numeroBloc * tailleBloc;
    final int to = from + tailleBloc;
    return Arrays.copyOfRange(donnees, from, to);
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


